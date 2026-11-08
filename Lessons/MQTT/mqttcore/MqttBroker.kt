/* \uFDFD
 *                           \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *              \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u062C\u0645\u0627\u062F\u0649 \u0627\u0644\u0623\u0648\u0644\u0649 \u06F2\u06F8
 */

package mqttcore

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger
import java.net.ServerSocket
import java.net.Socket
import javax.net.ssl.SSLServerSocketFactory
import javax.net.ssl.SSLSocket

class MqttBroker {
	@Volatile private var isRunning = false
	private var serverSocket: ServerSocket? = null

	private val activeSessions = ConcurrentHashMap<String, MqttSession>()
	private val persistentBrokerQueues = ConcurrentHashMap<String, ConcurrentHashMap<Int, MqttPacket>>()
	private val retainedMessages = ConcurrentHashMap<String, MqttPacket>()
	private val clientWillProperties = ConcurrentHashMap<String, WillProperties>()

	private val topicRouter = TopicRouter()
	private val workerIdCounter = AtomicInteger(1)
	private val ioExecutor = Executors.newCachedThreadPool { runnable ->
		Thread(runnable).apply {
			name = "mqtt-broker-worker-${workerIdCounter.getAndIncrement()}"
			isDaemon = true
		}
	}
	fun start(useSecureTls: Boolean = false) {
		val targetPort = if (useSecureTls) 8883 else 1883
		isRunning = true
		Thread {
			try {
				serverSocket = if (useSecureTls) {
					SSLServerSocketFactory.getDefault().createServerSocket(targetPort)
				} else {
					ServerSocket(targetPort)
				}
				while (isRunning) {
					val rawSocket = serverSocket?.accept() ?: break
					val transport = if (useSecureTls && rawSocket is SSLSocket) {
						try {
							rawSocket.startHandshake()
						} catch (e: Exception) {
							try { rawSocket.close() } catch (_: Exception) {}
							continue
						}
						SecureTransport(rawSocket)
					} else {
						TextTransport(rawSocket)
					}
					handleNewConnection(transport)
				}
			} catch (e: Exception) {
				stop()
			}
		}.start()
	}
	private fun handleNewConnection(transport: MqttTransport) {
		val transientInflightMap = ConcurrentHashMap<Int, MqttPacket>()
		val brokerSession = MqttSession(
			transport = transport,
			ioExecutor = ioExecutor,
			inflightPackets = transientInflightMap,
			onPacketReceived = { session, packet -> processClientPacket(session, packet) },
			onSessionClosed = { session, _ -> removeSession(session) }
		)
		brokerSession.startReadingLoop()
	}
	private fun processClientPacket(session: MqttSession, packet: MqttPacket) {
		when (packet.type) {
			MqttPacketType.CONNECT -> {
				val clientId = packet.properties["clientId"] as String
				val cleanSession = packet.properties["cleanSession"] as Boolean
				session.clientId = clientId
				val oldSession = activeSessions.put(clientId, session)
				oldSession?.close(IllegalStateException("Connection superseded by new client login."))
				// Extract and store Last Will and Testament configurations if flags exist
				val hasWill = packet.properties["willFlag"] as Boolean
				if (hasWill) {
					val willTopic = packet.properties["willTopic"] as String
					val willPayload = packet.properties["willMessage"] as ByteArray
					val willQos = packet.properties["willQos"] as Int
					val willRetain = packet.properties["willRetain"] as Boolean
					clientWillProperties[clientId] = WillProperties(willTopic, willPayload, willQos, willRetain)
				}
				val clientQueue = if (!cleanSession) {
					persistentBrokerQueues.computeIfAbsent(clientId) { ConcurrentHashMap() }
				} else {
					persistentBrokerQueues.remove(clientId)
					clientWillProperties.remove(clientId)
					ConcurrentHashMap()
				}
				if (clientQueue.isNotEmpty()) {
					session.restoreInflightPackets(clientQueue)
				}
				session.send(MqttPacket(returnCode = 0, sessionPresent = !cleanSession && clientQueue.isNotEmpty()))
				session.getInflightPackets().values.forEach { pendingPacket ->
					val duplicatePacket = if (pendingPacket.type == MqttPacketType.PUBLISH) {
						MqttPacket(
							topic = pendingPacket.topic!!,
							payload = pendingPacket.payload,
							qos = (pendingPacket.flags ushr 1) and 0x03,
							retain = (pendingPacket.flags and 0x01) == 1,
							packetId = pendingPacket.packetId!!,
							duplicate = true
						)
					} else pendingPacket
					session.send(duplicatePacket)
				}
			}
			MqttPacketType.SUBSCRIBE -> {
				val clientId = session.clientId ?: return
				val subscriptions = MqttPayload.unpackSubscribe(packet.payload)
				val statusBytes = ByteArray(subscriptions.size)
				subscriptions.forEachIndexed { index, (topicFilter, requestedQos) ->
					topicRouter.subscribe(topicFilter, clientId, requestedQos)
					statusBytes[index] = requestedQos.toByte()
				}
				packet.packetId?.let { id ->
					session.send(MqttPacket(MqttPacketType.SUBACK, id, statusBytes))
				}
				// Check historical retained cache for newly discovered match criteria
				subscriptions.forEach { (topicFilter, requestedQos) ->
					retainedMessages.forEach { (topic, retainedPacket) ->
						val evaluationTree = TopicRouter().apply { subscribe(topicFilter, "lookup", requestedQos) }
						if (evaluationTree.findSubscribers(topic).containsKey("lookup")) {
							val targetQos = minOf((retainedPacket.flags ushr 1) and 0x03, requestedQos)
							val deliveryId = if (targetQos > 0) session.nextPacketId() else null
							session.send(MqttPacket(
								topic = topic,
								payload = retainedPacket.payload,
								qos = targetQos,
								retain = true,
								packetId = deliveryId,
								duplicate = false
							))
						}
					}
				}
			}
			MqttPacketType.UNSUBSCRIBE -> {
				val clientId = session.clientId ?: return
				val topics = MqttPayload.unpackUnsubscribe(packet.payload)
				topics.forEach { topicFilter ->
					topicRouter.unsubscribe(topicFilter, clientId)
				}
				packet.packetId?.let { id ->
					session.send(MqttPacket(MqttPacketType.UNSUBACK, id))
				}
			}
			MqttPacketType.PUBLISH -> {
				val topic = packet.topic ?: return
				val incomingQos = (packet.flags ushr 1) and 0x03
				val isRetain = (packet.flags and 0x01) == 1
				if (isRetain) {
					if (packet.payload.isEmpty()) {
						retainedMessages.remove(topic)
					} else {
						retainedMessages[topic] = packet
					}
				}
				dispatchBroadcast(topic, packet.payload, incomingQos, false)
				packet.packetId?.let { id ->
					when (incomingQos) {
						1 -> session.send(MqttPacket(MqttPacketType.PUBACK, id))
						2 -> session.send(MqttPacket(MqttPacketType.PUBREC, id))
					}
				}
			}
			MqttPacketType.PUBREC -> {
				packet.packetId?.let { id -> session.send(MqttPacket(MqttPacketType.PUBREL, id)) }
			}
			MqttPacketType.PUBREL -> {
				packet.packetId?.let { id -> session.send(MqttPacket(MqttPacketType.PUBCOMP, id)) }
			}
			MqttPacketType.PINGREQ -> {
				session.send(MqttPacket(MqttPacketType.PINGRESP))
			}
			MqttPacketType.DISCONNECT -> {
				val clientId = session.clientId
				if (clientId != null) {
					clientWillProperties.remove(clientId)	// Clear LWT on graceful disconnect
				}
				session.close()
			}
			else -> {}
		}
	}
	private fun dispatchBroadcast(topic: String, payload: ByteArray, incomingQos: Int, isWill: Boolean) {
		topicRouter.findSubscribers(topic).forEach { (subscriberId, grantedQos) ->
			val targetSession = activeSessions[subscriberId]
			if (targetSession != null) {
				val targetQos = minOf(incomingQos, grantedQos)
				val deliveryId = if (targetQos > 0) targetSession.nextPacketId() else null
				val deliveryPacket = MqttPacket(
					topic = topic,
					payload = payload,
					qos = targetQos,
					retain = isWill,		// If LWM message has retain setup, mirror it
					packetId = deliveryId,
					duplicate = false
				)
				targetSession.send(deliveryPacket)
			}
		}
	}
	private fun removeSession(session: MqttSession) {
		val clientId = session.clientId ?: return
		if (activeSessions.remove(clientId, session)) {
			val persistentQueue = persistentBrokerQueues[clientId]
			persistentQueue?.putAll(session.getInflightPackets())
			// Execute Last Will and Testament if client disconnected ungracefully
			val will = clientWillProperties.remove(clientId)
			if (will != null) {
				if (will.retain) {
					val fallbackPacket = MqttPacket(topic = will.topic, payload = will.payload, qos = will.qos, retain = true)
					retainedMessages[will.topic] = fallbackPacket
				}
				dispatchBroadcast(will.topic, will.payload, will.qos, true)
			}
			topicRouter.clearClient(clientId)
		}
	}
	fun stop() {
		isRunning = false
		try { serverSocket?.close() } catch (_: Exception) {}
		activeSessions.values.forEach { it.close() }
		activeSessions.clear()
		persistentBrokerQueues.clear()
		retainedMessages.clear()
		clientWillProperties.clear()
		ioExecutor.shutdown()
	}
	/**
	 * A high-performance, thread-safe Trie (Prefix Tree) topic matcher supporting MQTT wildcards (+ and #).
	 */
	class TopicRouter {
		private class Node {
			val children = ConcurrentHashMap<String, Node>()
			val subscribers = ConcurrentHashMap<String, Int>()	// Client ID -> Granted QoS
		}
		private val root = Node()
		private val clientSubscriptions = ConcurrentHashMap<String, CopyOnWriteArraySet<String>>()

		fun subscribe(topicFilter: String, clientId: String, qos: Int) {
			val levels = topicFilter.split("/")
			var current = root
			for (level in levels) {
				// Safe, atomiz lazy initialization of the Trie nodes
				current = current.children.computeIfAbsent(level) { Node() }
			}
			current.subscribers[clientId] = qos
			clientSubscriptions.computeIfAbsent(clientId) { CopyOnWriteArraySet() }.add(topicFilter)
		}
		fun unsubscribe(topicFilter: String, clientId: String) {
			// 1. Maintain the tracking index first
			clientSubscriptions[clientId]?.let { topics ->
				topics.remove(topicFilter)
				if (topics.isEmpty()) {
					clientSubscriptions.remove(clientId, topics)
				}
			}
			// 2. Prune the tree safely
			val levels = topicFilter.split("/")
			pruneTreePath(levels, clientId)
		}
		fun clearClient(clientId: String) {
			// Atomically hijack the client's subscriptions list to avoid inter-thread pollution
			val subscribedTopics = clientSubscriptions.remove(clientId) ?: return
			for (topicFilter in subscribedTopics) {
				val levels = topicFilter.split("/")
				// Bypass the tracking index since we already popped it, optimizing O(M * L) execution
				pruneTreePath(levels, clientId)
			}
		}
		fun findSubscribers(publishTopic: String): Map<String, Int> {
			val levels = publishTopic.split("/")
			val matchedSubscribers = HashMap<String, Int>()
			fun search(node: Node, levelIndex: Int) {
				// 1. Check multi-level wildcard '#'
				node.children["#"]?.let { multiLevelNode ->
					multiLevelNode.subscribers.forEach { (clientId, qos) ->
						val currentMax = matchedSubscribers[clientId] ?: -1
						if (qos > currentMax) matchedSubscribers[clientId] = qos
					}
				}
				// 2. Base Case: End of the published topic levels reached
				if (levelIndex == levels.size) {
					node.subscribers.forEach { (clientId, qos) ->
						val currentMax = matchedSubscribers[clientId] ?: -1
						if (qos > currentMax) matchedSubscribers[clientId] = qos
					}
					return
				}
				// 3. Exact matching level token
				val currentLevelToken = levels[levelIndex]
				node.children[currentLevelToken]?.let { search(it, levelIndex + 1) }
				// 4. Single-level wildcard '+' matching
				node.children["+"]?.let { search(it, levelIndex + 1) }
			}
			search(root, 0)
			return matchedSubscribers
		}
		/**
		 * Internal thread-safe tree pruning routine.
		 * Safely backtracks up the tree to erase dead branches without risking race condition corruptions.
		 */
		private fun pruneTreePath(levels: List<String>, clientId: String) {
			val path = mutableListOf<Pair<String, Node>>()
			var current = root
			// 1. Trace down the path to find the target leaf node
			for (level in levels) {
				val next = current.children[level] ?: return
				path.add(Pair(level, next))
				current = next
			}
			// 2. Clear the subscriber first
			current.subscribers.remove(clientId)
			// 3. Walk backwards up the tree to clean up dead parent nodes
			for (i in path.lastIndex downTo 0) {
				val (level, node) = path[i]
				val parent = if (i == 0) root else path[i - 1].second
				// A node can only be safely removed if it has zero active subscribers and zero children
				if (node.children.isEmpty() && node.subscribers.isEmpty()) {
					val removedSuccessfully = parent.children.remove(level, node)
					if (removedSuccessfully) {
						// Double check inside the race window: Did another thread grab this node mid-deletion?
						if (!node.children.isEmpty() || !node.subscribers.isEmpty()) {
							// Put it back to rescue it if a concurrent thread added a subscriber/child
							parent.children.putIfAbsent(level, node)
							break	// Stop pruning since this branch is alive again
						}
						// DO NOT break here! We want the loop to continue upward to clean up empty parent levels.
					} else {
						// If remove failed, parent.children[level] already points to a different instance; abort.
						break
					}
				} else {
					// As soon as we hit a node that isn't empty, we can stop evaluating higher parent nodes
					break
				}
			}
		}
	}
	/**
	 * Data container representing a client's Last Will and Testament configuration.
	 */
	data class WillProperties(val topic: String, val payload: ByteArray, val qos: Int, val retain: Boolean)
}
