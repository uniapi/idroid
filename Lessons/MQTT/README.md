# 📦 Native Application Protocols: Raw Socket MQTT Engine

Welcome to the low-level Android MQTT module.
In this module, we bypass all high-level client libraries (such as Eclipse Paho or HiveMQ Client) to manually construct a live,
asynchronous MQTT v3.1.1 protocol engine and a custom local broker service directly over raw TCP streaming buffers.

---

## 🛜 Network Architecture: How MQTT Operates

MQTT is an asynchronous, data-centric network protocol designed specifically for resource-constrained environments, high-latency links, or minimal data-overhead pipelines.

### 1. The Core Paradox: Decoupled Communications
Unlike traditional HTTP client-server setups where an application establishes direct contact with a target endpoint, MQTT nodes never transmit data directly to one another.
Communication is entirely distributed using a **Publish/Subscribe (Pub/Sub)** model:
*   **The Publisher (Upstream Data):** A device packs telemetry markers into a frame and transmits them out to the network.
It addresses the data not to a hardware node, but to an abstract string destination called a **Topic**.
*   **The Subscriber (Downstream Data):** A device registers its interest in a specific string topic path. It remains passive until a payload matching that string criteria becomes available.
*   **The Topic Matrix:** Topics are dynamic, text-based routing destinations separated by forward slashes (e.g., `home/livingroom/temperature`).
Topics require zero prior registration or server configuration—they are instantiated on the fly the exact millisecond a packet claims them.

### 2. The Broker: Central Post Office Engine
The **MQTT Broker** acts as the central server node of the entire network. Every client maintains an individual raw TCP/IP connection to the broker.
*   **Traffic Interception:** The broker accepts incoming raw packets, inspects their variable headers for topic criteria, and acts as a router.
*   **Asynchronous Distribution:** It keeps a clean session database of every active subscriber's state.
When a publisher pushes an update, the broker duplicates and pushes that payload down the respective streaming pipelines of matching subscriber sockets.
*   **Minimum Participant Topology:** The absolute minimum composition of an operational MQTT topology is **two participants**: exactly **one broker** and exactly **one client**.
A single client can safely initialize an abstract network loops-back test by subscribing and publishing to the identical dynamic topic concurrently.

---
## 🔬 Deep Architecture: The MQTT v3.1.1 Binary Specification

### 1. Anatomy of an MQTT Control Packet
Unlike text-based protocols like HTTP, MQTT is a lightweight, **binary-encoded** protocol. Every transaction is structured directly inside a raw byte array.
An MQTT control packet consists of three structural sections:

```text
┌───────────────────────────┬─────────────────────────────────┬─────────────────────────────┐
│ Fixed Header (2+ Bytes)   │ Variable Header (Optional)      │ Payload (Optional)          │
├─────────────┬─────────────┼─────────────────────────────────┼─────────────────────────────┤
│ Packet Type │ Control     │ Packet Identifier, Topic Name,  │ Client ID, Username,        │
│   (4 bits)  │ Flags(4bits)│ Connect Flags, Keep-Alive Timer │ Application Binary Message  │
└─────────────┴─────────────┴─────────────────────────────────┴─────────────────────────────┘
```

*   **The Control Packet Type Byte:** The first byte of every packet contains the packet type in the 4 most significant bits (e.g., `0x10` for `CONNECT`, `0x30` for `PUBLISH`, `0x80` for `SUBSCRIBE`)
and specific operational flags in the 4 least significant bits.
*   **The Remaining Length Calculation:** The second byte (and up to 3 following bytes) contains the total length of the variable header and payload segments combined.

### 2. The Fixed Header Flag Bitmask
While most MQTT control packets hardcode the 4 lower bits of Byte 1 to `0000`, the `PUBLISH` (`0x30`) packet maps these bits to critical transport flags:

| Bit Position | Flag Name | Technical Application |
| :--- | :--- | :--- |
| **Bit 3** | **DUP** (Duplicate) | Set to `1` when re-sending a packet because a required delivery acknowledgment timed out. |
| **Bits 2–1** | **QoS** (Quality of Service) | Delivery reliability mapping: `00` = QoS 0 (Fire & Forget); `01` = QoS 1 (At least once); `10` = QoS 2 (Exactly once). |
| **Bit 0** | **RETAIN** | Set to `1` to force the broker to cache this message as the "last known good" state for new subscribers. |

### 3. The Variable-Byte Remaining Length Encoding Scheme
To optimize bandwidth, MQTT encodes the length of a packet using a variable number of bytes.
Each byte uses the 7 lower bits for data (`0-127`) and the most significant bit (Bit 7) as a **Continuation Bit** flag.
*   If Bit 7 is set to `1`, it indicates that another length byte follows.
*   If Bit 7 is set to `0`, it marks the end of the length multiplier block.

#### 🧮 Manual Mathematical Encoding & Decoding Algorithm
```kotlin
fun writeRemainingLength(outputStream: OutputStream, length: Int) {
	require(length in 0..268435455) { "Malformed remaining legnth: $length" }
    var num = length
	do {
    	var encodedByte = num and 0x7F
        num = num ushr 7
        // If there are mote bytes to encode, set the continuation bit (7)
        if (num > 0) {
        	encodedByte = encodedByte or 0x80
        }
        outputStream.write(encodedByte)
    } while (num > 0)
}
fun readRemainingLength(inputStream: InputStream): Int {
	var multiplier = 1
    var length = 0
    var encodedByte: Int
    var bytesRead = 0
    do {
    	encodedByte = inputStream.read()
        if (encodedByte == -1) throw EOFException("Stream closed before packet was fully read.")
        	length += (encodedByte and 0x7F) * multiplier
            bytesRead++
            if (bytesRead > 4) throw IllegalArgumentException("Malformed remaining length")
            multiplier = multiplier shl 7
   } while (encodedByte and 0x80 != 0)
   return length
}
```
### 4. Binary Layout of Essential Control Packets

#### A. The `CONNECT` Packet (`0x10`) & `CONNACK` Response (`0x20`)
To initialize an active session on an external MQTT broker, the engine must construct an absolute binary block containing:
1.  **Fixed Header:** `0x10` followed by the variable length byte array.
2.  **Variable Header:** The protocol name string length prefix (`0x00 0x04`), the protocol name literal (`"MQTT"` in ASCII bytes), and the protocol level flag (`0x04` for v3.1.1).

This header immediately terminates into a single **Connect Flags Byte** structuring authentication and persistence guidelines:

```text
  Bit 7      Bit 6      Bit 5      Bits 4-3     Bit 2       Bit 1      Bit 0
┌──────────┬──────────┬──────────┬────────────┬──────────┬───────────┬──────────┐
│ Username │ Password │ Will Ret │  Will QoS  │ Will Flg | CleanSess │ Reserved │
└──────────┴──────────┴──────────┴────────────┴──────────┴───────────┴──────────┘
```
*   **Username / Password Flags (Bits 7–6):** Set to `1` if credentials follow in the payload.
*   **Will Flags (Bits 5–2):** Configures Last Will and Testament (LWT) distribution when unexpected socket drops occur.
*   **Clean Session (Bit 1):** Set to `1` to discard previous state and enforce a blank session. Set to `0` to survive disconnects and queue offline frames.

3.  **Payload:** A UTF-8 string prefixed with a 2-byte length tracker containing the unique **Client Identifier** string, followed by optional LWT strings and credentials based on the Connect Flags.

##### The `CONNACK` Frame Matrix (`0x20`)
The broker validates the configuration and responds with a static 4-byte frame tracking state approvals: `0x20 0x02 [Acknowledge_Flags] [Return_Code]`.
*   **Acknowledge Flags (Byte 3):** Specifically evaluates Bit 0 (**Session Present**). If `0`, the broker does not have any saved session state for this client ID.
If `1`, the broker has successfully matched an existing offline session cache matching the Client ID.
*   **Return Code (Byte 4):** Evaluation state. `0x00` means Connection Accepted. Values `0x01` through `0x05` indicate failures:
	* **`0x00`**: Connection Accepted
	* **`0x01`**: Connection Refused, Unacceptable Protocol Version
	* **`0x02`**: Connection Refused, Identifier Rejected
	* **`0x03`**: Connection Refused, Server Unavailable
	* **`0x04`**: Connection Refused, Bad User Name or Password
	* **`0x05`**: Connection Refused, Not Authorized


#### B. The `PUBLISH` Packet (`0x30`)
To transmit telemetry markers outwards from the Android device, the packet payload shifts dynamically:
1.  **Fixed Header:** `0x30` base signature adjusted by the Fixed Header flags (e.g., `0x32` if QoS 1 is flag-enabled, `0x31` for Retained QoS 0).
2.  **Variable Header:** A 2-byte length tracker followed by the raw string text bytes of the target topic path (e.g., `0x00 0x0C` followed by `"device/state"`).
If using QoS 1 or QoS 2, this section concludes with a 2-byte packet token identifier.
3.  **Payload:** The raw data body block bytes (unprefixed by length bytes; the boundary is derived explicitly from the fixed header length metadata).

#### C. The `SUBSCRIBE` Packet (`0x80`) & `SUBACK` Response (`0x40`)
To register a dynamic route on a server socket, a client pushes a subscription command framework containing:
1.  **Fixed Header:** `0x82` (`0x80` type indicator coupled with mandatory protocol flag padding bit `0x02`).
2.  **Variable Header:** A 2-byte mandatory **Packet Identifier** integer used to map client-server confirmation requests.
3.  **Payload:** A 2-byte string length token tracker, the string text bytes of the intended destination path (e.g., `"salamun/telemetry"`),
and a terminal 1-byte flag explicitly identifying the requested Quality of Service (QoS) tier (e.g., `0x00` for QoS 0).

##### The `SUBACK` Payload Array (`0x40`)
The Broker acknowledges this registration by echoing back a variable-length validation frame embedding matching identifier tokens: `0x40 [Length] [PacketID_MSB] [PacketID_LSB] [Return_Codes_Array]`.
*   The payload contains no bit flags; instead, it contains an explicitly ordered byte array matching each topic requested.
*   **Return Codes:** `0x00` (Success QoS 0), `0x01` (Success QoS 1), `0x02` (Success QoS 2), and `0x80` (Failure due to authorization limits or invalid wildcards).

#### D. The `PINGREQ` Packet (`0xC0`)
The simplest control mechanism.
To satisfy the broker keep-alive timer criteria during quiescent traffic periods, the application must repeatedly send a static, 2-byte boundary frame down the open TCP channel:
```text
0xC0 0x00
```
The broker will instantly process this block and send an identical 2-byte **`PINGRESP`** packet signature matching `0xD0 0x00`.

### 🗺️ Master MQTT v3.1.1 Packet Architecture Map

| Packet Type | Hex Code | Variable Header Contents | Payload Contents |
| :--- | :--- | :--- | :--- |
| **`CONNECT`** | `0x10` | Protocol Name (`"MQTT"`), Protocol Level (`0x04`), Connect Flags, Keep-Alive Timer (2 Bytes). | Client ID, (Optional: Will Topic, Will Message, Username, Password). |
| **`CONNACK`** | `0x20` | Connect Acknowledge Flags (Session Present), Connect Return Code. | *No payload.* |
| **`PUBLISH`** | `0x30` | Topic Name String. Optional 2-byte Packet Identifier (**only if QoS > 0**). | Raw application binary/text content (can be 0 bytes empty). |
| **`PUBACK`** | `0x40` | 2-byte Packet Identifier. | *No payload (QoS 1 Acknowledgment).* |
| **`PUBREC`** | `0x50` | 2-byte Packet Identifier. | *No payload (QoS 2 Step 1: Received).* |
| **`PUBREL`** | `0x60` | 2-byte Packet Identifier. *(Fixed Header flags must be `0x62`)*. | *No payload (QoS 2 Step 2: Release).* |
| **`PUBCOMP`** | `0x70` | 2-byte Packet Identifier. | *No payload (QoS 2 Step 3: Complete).* |
| **`SUBSCRIBE`** | `0x80` | 2-byte Packet Identifier. | List of Topic Filters paired with requested QoS tiers. |
| **`SUBACK`** | `0x90` | 2-byte Packet Identifier. | List of granted QoS return codes (or `0x80` failure flags). |
| **`UNSUBSCRIBE`**| `0xA0` | 2-byte Packet Identifier, followed by target Topic Filter arrays. | *No payload (Filters are structurally part of Variable Header).* |
| **`UNSUBACK`** | `0xB0` | 2-byte Packet Identifier. | *No payload.* |
| **`PINGREQ`** | `0xC0` | *No variable header.* | *No payload (Static 2-byte ping request).* |
| **`PINGRESP`** | `0xD0` | *No variable header.* | *No payload (Static 2-byte ping response).* |
| **`DISCONNECT`** | `0xE0` | *No variable header.* | *No payload.* |


### Variable Header Size Reference Table

| Packet Type | MQTT v3.1.1 Variable Header Length | MQTT v5.0 Variable Header Length | Key Components |
| :--- | :--- | :--- | :--- |
| **CONNECT** | **10 bytes** | **10 bytes** + Property Length + Property bytes | Protocol name (6 bytes), Level (1 byte), Flags (1 byte), Keep Alive (2 bytes), plus MQTT 5 properties. |
| **CONNACK** | **2 bytes** | **3 bytes** + Property Length + Property bytes | Acknowledge flags (1 byte), Return/Reason code (1 byte), plus MQTT 5 properties. |
| **PUBLISH** | **2** + Topic String Length (+ 2 bytes if QoS > 0) | **2** + Topic String Length (+ 2 if QoS > 0) + Property Length + Property bytes | Topic length indicator (2 bytes), Topic string, Packet ID (2 bytes, conditional), plus MQTT 5 properties. |
| **PUBACK / PUBREC / PUBREL / PUBCOMP** | **2 bytes** | **3 bytes** + Property Length + Property bytes | Packet Identifier (2 bytes), Reason Code (1 byte in v5), plus MQTT 5 properties. |
| **SUBSCRIBE / SUBACK** | **2 bytes** | **2 bytes** + Property Length + Property bytes | Packet Identifier (2 bytes), plus MQTT 5 properties. |
| **UNSUBSCRIBE / UNSUBACK** | **2 bytes** | **2 bytes** + Property Length + Property bytes | Packet Identifier (2 bytes), plus MQTT 5 properties. |
| **PINGREQ / PINGRESP** | **0 bytes** (No variable header) | **0 bytes** (No variable header) | Fixed header only. |
| **DISCONNECT** | **0 bytes** (No variable header) | **1 byte** + Property Length + Property bytes | Reason Code (1 byte in v5), plus MQTT 5 properties. |




## 🔒 5. Secure Transport Layer (TLS) & Authentication Mechanics

Because MQTT v3.1.1 transmits data payloads, usernames, and passwords in **plaintext**,
wrapping the raw TCP channel inside an encrypted transport layer is mandatory for production deployments (typically over port `8883`).

### 1. Architectural Communication & TLS Handshake Sequence
Before any MQTT control packets can be transmitted, the low-level engine must step up an unencrypted socket to an encrypted TLS session.
This protocol dance transitions a raw TCP pipeline into an isolated cryptographic stream.

```text
 Client Suffix (Android Node)                                    MQTT Broker Suffix
      │                                                                   │
      ├─────────────────────── 1. TCP SYN ───────────────────────────────>│ [Port 8883]
      │<────────────────────── 2. TCP SYN-ACK ────────────────────────────┤
      ├─────────────────────── 3. TCP ACK (Socket Connected) ────────────>│
      │                                                                   │
      │ ═══════════════════ TLS CRYPTOGRAPHIC HANDSHAKE ═════════════════ │
      ├─────────────────────── 4. ClientHello (Cipher Suites) ───────────>│
      │<────────────────────── 5. ServerHello & Broker Certificate ───────┤
      ├─────────────────────── 6. Key Exchange & Cipher Verification ────>│
      │<────────────────────── 7. Handshake Finished (Encrypted Tunnel) ──┤
      │                                                                   │
      │ ═══════════════════ AUTHENTICATED MQTT SESSION ══════════════════ │
      ├─────────────────────── 8. SECURE CONNECT (With Flags/Auth) ──────>│
      │<────────────────────── 9. SECURE CONNACK (Success Status) ────────┤
      │                                                                   │
```

### 2. Raw Socket Layer Upgrading
Instead of standard unencrypted sockets, the engine initializes communication using a secure layer factory instance before executing binary reads or writes:

```kotlin
import javax.net.ssl.SSLSocketFactory
import java.io.OutputStream
import java.io.InputStream

fun initializeSecureEngine(brokerIp: String, port: Int = 8883) {
    val socketFactory = SSLSocketFactory.getDefault()
    val secureSocket = socketFactory.createSocket(brokerIp, port)
    
    // Explicitly enforce modern TLS protocols prior to stream processing
    (secureSocket as javax.net.ssl.SSLSocket).enabledProtocols = arrayOf("TLSv1.2", "TLSv1.3")
    
    val outputStream: OutputStream = secureSocket.getOutputStream()
    val inputStream: InputStream = secureSocket.getInputStream()
    
    // Transparent binary streaming begins here over an encrypted pipe
}
```

### 3. User Credentials Binary Injection
When authentication is required, the `CONNECT` packet’s configuration must strictly manipulate the **Connect Flags Byte** bitmask and expand the tail end of the **Payload** segments.

#### 🛠️ Explicit Binary Formulation Example
To connect using the client ID `"Android"`, username `"admin"`, and password `"secret123"`, the binary data stream must resolve to the following precise layout:

```text
[Fixed Header]
0x10 0x27                  // Type: CONNECT, Remaining Length: 39 bytes

[Variable Header]
0x00 0x04 0x4D 0x51 0x54 0x54 // Protocol Name Length (4), Protocol Name ("MQTT")
0x04                       // Protocol Level: v3.1.1
0xC2                       // Connect Flags: 11000010 (Username=1, Password=1, CleanSession=1)
0x00 0x3C                  // Keep Alive: 60 Seconds

[Payload Array]
0x00 0x07 0x41 0x6E 0x64 0x72 0x6F 0x69 0x64 // ClientID Length (7), "Android"
0x00 0x05 0x61 0x64 0x6D 0x69 0x6E           // Username Length (5), "admin"
0x00 0x09 0x73 0x65 0x63 0x72 0x65 0x74 0x31 0x32 0x33 // Password Length (9), "secret123"
```
If the broker rejects these credentials during evaluation, it terminates the sequence at the socket layer
and replies immediately with a `CONNACK` frame containing a status code of `0x04` (Bad Username or Password) or `0x05` (Not Authorized):
```text
[Fixed Header]
0x20 0x02                  // Type: CONNACK (0x20), Remaining Length: 2 bytes

[Variable Header]
0x00                       // Connect Acknowledge Flags (Session Present = 0)
0x04                       // Connect Return Code: Connection Refused, bad username or password
```

## 🏗️ Custom Local Broker Engine Specification

For edge applications, sandboxed testing environments, or decentralized communication matrices,
the engine can spin up a fully compliant, self-contained `SSLServerSocket` architecture implementing secure local broker capabilities.

```text
                  ┌─────────────────────────────────┐
                  │   MqttSecureBrokerService       │
                  │   (Listens on TLS Port 8883)    │
                  └────────────────┬────────────────┘
                                   │  ◄── [Requires TLS Handshake & Auth]
         ┌─────────────────────────┼─────────────────────────┐
         ▼                         ▼                         ▼
┌──────────────────┐      ┌──────────────────┐      ┌──────────────────┐
│ Client Session 1 │      │ Client Session 2 │      │ Client Session 3 │
│ Authenticated    │      │ Authenticated    │      │ Authenticated    │
│ [Topic: A]       │      │ [Topic: B]       │      │ [Topic: A]       │
└──────────────────┘      └──────────────────┘      └──────────────────┘
```

### 1. Secure Multi-Threaded Connection Handling
The background service spawns a persistent loop binding onto secure **TLS Port `8883`** using standard Java `SSLServerSocket` instances configured with localized KeyStore credentials.
*   **Thread Isolation:** To preserve UI frame times, every execution layer is isolated. Incoming client connections are accepted via an asynchronous background thread and mapped across a thread-safe `CopyOnWriteArrayList` configuration layer.
*   **Executor Thread Pooling:** Processing pipelines for individual client connection byte streams are offloaded onto a fixed-size `ExecutorService` thread pool (e.g., 8 concurrent workers) to scale execution throughput safely.
*   **TLS Protocol Enforcement:** The server socket explicitly forces modern cryptographic negotiation (such as TLS v1.2 or v1.3) before exposing underlying binary streams.

### 2. Thread-Safe Label-Directed Packet Interception with Authentication
When incoming binary data sets arrive inside a specific client session context stream, the background worker routes the processing chunks via an intensive loop structure labeled directly to avoid multi-scope execution ambiguity. A boolean flag tracks the session's authentication state to guard post-connect functions:

```kotlin
var isAuthenticated = false

clientLoop@ while (isBrokerLive) {
    val fixedHeaderByte = inputStream.read()
    if (fixedHeaderByte == -1) break@clientLoop
    
    val packetType = fixedHeaderByte shr 4
    
    // Security Enforcement: Drop any packet that attempts to bypass the CONNECT handshake
    if (!isAuthenticated && packetType != 0x01) { // 0x01 is CONNECT (0x10)
        outputStream.write(byteArrayOf(0x20, 0x02, 0x00, 0x05)) // CONNACK Refused, Not Authorized
        outputStream.flush()
        break@clientLoop
    }

    when (packetType) {
        0x01 -> { // 0x10 CONNECT
            val (user, pass) = parseConnectCredentials(inputStream)
            if (user == "admin" && pass == "secret123") {
                isAuthenticated = true
                outputStream.write(byteArrayOf(0x20, 0x02, 0x00, 0x00)) // CONNACK Success
                outputStream.flush()
            } else {
                outputStream.write(byteArrayOf(0x20, 0x02, 0x00, 0x04)) // CONNACK Bad Credentials
                outputStream.flush()
                break@clientLoop // Terminate connection immediately
            }
        }
        0x08 -> { /* Map Dynamic Topic to Context Session & Flush SUBACK */ }
        0x03 -> { /* Parse & Asynchronously Broadcast Payload */ }
        0x0E -> { // DISCONNECT (0xE0)
            // Gracefully terminate the thread loop by targeting the label container
            break@clientLoop
        }
    }
}
```

### 3. Asynchronous Broadcast Engine
When an isolated, authenticated client connection socket invokes a `0x30` `PUBLISH` sequence, the worker parses the dynamic string targets out of the payload array on the fly. 

It loops over the globally shared client session registry matrix, performs literal character comparisons to find clients whose `registeredTopic` targets match the publication key,
and asynchronously mirrors the raw bytes directly to their respective outbound secure output streaming channels.


## 🏆 Module Progress Map
- **Challenge 1: Manual Binary Packet Assembler & Handshake Node (Easy - Single Packet Connection)**
- **Challenge 2: Persistent Pub/Sub Lifecycle & Heartbeat Service (Advanced — Persistent Foreground Service)**
- **Challenge 3: Encrypted Multi-Node Architecture & Auth Demultiplexer (Master — Full-Duplex Secure Topology)**

---
