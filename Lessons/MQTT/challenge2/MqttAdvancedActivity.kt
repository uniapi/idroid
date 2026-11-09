/* \uFDFD
 *                           \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *              \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u062C\u0645\u0627\u062F\u0649 \u0627\u0644\u0623\u0648\u0644\u0649 \u06F2\u06F1
 */
package localhost.idroid.salamun

import android.util.Log
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.EditText
import android.widget.TextView
import android.widget.Button

class MqttAdvancedActivity : Activity(), MqttAdvancedService.AdvancedMqttListener {
	private companion object {
		const val TAG = "localhost.idroid.salamun"
	}
	private var sBoundAdvanced: MqttAdvancedService? = null
	private var isAdvancedBound = false
	private lateinit var llRoot: LinearLayout
	private lateinit var etBrokerIp: EditText
	private lateinit var etTopicInput: EditText
	private lateinit var etPayloadInput: EditText
	private lateinit var etSubscribeTopicInput: EditText
	private lateinit var btnToggleConnection: Button
	private lateinit var btnPublishTrigger: Button
	private lateinit var btnSubscribeTrigger: Button
	private lateinit var svConsoleScroll: ScrollView
	private lateinit var tvConsoleLog: TextView

	private val scAdvancedConnection = object : ServiceConnection {
		override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
			val binder = service as MqttAdvancedService.LocalAdvancedBinder
			sBoundAdvanced = binder.getService()
			isAdvancedBound = true
			sBoundAdvanced?.registerAdvancedListener(this@MqttAdvancedActivity)
		}
		override fun onServiceDisconnected(name: ComponentName?) {
			sBoundAdvanced?.registerAdvancedListener(null)
			sBoundAdvanced = null
			isAdvancedBound = false
		}
	}
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val llRoot = LinearLayout(this).apply {
			orientation = LinearLayout.VERTICAL
			setPadding(16, 16, 16, 16)
		}
		etBrokerIp = EditText(this).apply {
			hint = "broker.hivemq.com"
			setText("broker.hivemq.com")
		}
		btnToggleConnection = Button(this).apply {
			text = "INITIALIZE BROKER NODE CONNECTION"
		}
		etTopicInput = EditText(this).apply {
			hint = "Publish Topic: idroid/telemtry/humidity"
			setText("idroid/telemetry/humidity")
		}
		etPayloadInput = EditText(this).apply {
			hint = "Enter target payload string message text..."
		}
		btnPublishTrigger = Button(this).apply {
			text = "DISPATCH PUBLISH PAYLOAD"
			isEnabled = false
		}
		etSubscribeTopicInput = EditText(this).apply {
			hint = "Subscribe Topic: idroid/telemetry/humidity"
			setText("idroid/telemetry/humidity")
		}
		btnSubscribeTrigger = Button(this).apply {
			text = "EXECUTE TOPIC SUBSCRIPTION"
			isEnabled = false
		}
		svConsoleScroll = ScrollView(this)
		tvConsoleLog = TextView(this).apply {
			text = "[CONSOLE CONTEXT] Hooking application service connections...\n"
		}
		svConsoleScroll.addView(tvConsoleLog)
		with(llRoot) {
			addView(etBrokerIp)
			addView(btnToggleConnection)
			addView(etTopicInput)
			addView(etPayloadInput)
			addView(btnPublishTrigger)
			addView(etSubscribeTopicInput)
			addView(btnSubscribeTrigger)
			addView(svConsoleScroll)
		}
		setContentView(llRoot)

		btnToggleConnection.setOnClickListener {
			sBoundAdvanced?.let { service ->
				if (!service.isBrokerConnected()) {
					val address = etBrokerIp.text.toString().trim()
					if (address.isNotEmpty()) {
						val intent = Intent(this, MqttAdvancedService::class.java)
						startService(intent)
						service.startMqttPipeline(address)
					}
				} else {
					service.teardownEngine()
				}
			}
		}
		btnPublishTrigger.setOnClickListener {
			val topic = etTopicInput.text.toString().trim()
			val payload = etPayloadInput.text.toString()
			if (topic.isNotEmpty() && payload.isNotEmpty()) {
				sBoundAdvanced?.publishTelemetryMessage(topic, payload)
			}
		}
		btnSubscribeTrigger.setOnClickListener {
			val subTopic = etSubscribeTopicInput.text.toString().trim()
			if (subTopic.isNotEmpty()) {
				val topicsMap = mapOf(subTopic to 1)
				sBoundAdvanced?.subscribeToTopics(topicsMap)
			}
		}
	}
	override fun onStart() {
		super.onStart()
		val intent = Intent(this, MqttAdvancedService::class.java)
		bindService(intent, scAdvancedConnection, Context.BIND_AUTO_CREATE)
	}
	override fun onStop() {
		if (isAdvancedBound) {
			sBoundAdvanced?.registerAdvancedListener(null)
			unbindService(scAdvancedConnection)
			isAdvancedBound = false
		}
		super.onStop()
	}
	override fun onLogReceived(message: String) {
		tvConsoleLog.append(message)
		svConsoleScroll.post { svConsoleScroll.fullScroll(ScrollView.FOCUS_DOWN) }
	}
	override fun onStateChanged(connected: Boolean) {
		btnPublishTrigger.isEnabled = connected
		btnSubscribeTrigger.isEnabled = connected
		if (connected) {
			btnToggleConnection.text = "TERMINATE NODE PIPELINE"
		} else {
			btnToggleConnection.text = "INITIALIZE BROKER NODE CONNECTION"
		}
	}
	override fun onMessageReceived(topic: String, payload: String) {
		// Safely appends the clean text directly onto UI layout logger
		val visualAlert = "⚠️ INBOUND MESSAGE ALERT!\n -> [TOPIC] $topic\n -> [PAYLOAD] $payload\n\n"
		tvConsoleLog.append(visualAlert)
		svConsoleScroll.post { svConsoleScroll.fullScroll(ScrollView.FOCUS_DOWN) }
	}
}
