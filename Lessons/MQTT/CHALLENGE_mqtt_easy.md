# 🟢 Challenge 1: Manual Binary Packet Assembler & Handshake Node (Easy)

## 🔬 Challenge Architecture & Theory

To interact with an industrial MQTT broker completely from scratch,
you must bypass high-level messaging drivers and manually write raw binary serialization pipelines that structure data directly into Layer 7 **MQTT v3.1.1 Binary Envelopes**.

```text
[Android App Engine] ──► Raw TCP Socket (Port 1883) ──► External MQTT Broker
        │                                                     │
        ├──► Serializes Manual [CONNECT] Array (0x10) ──►─────┤
        │                                                     │
        └──◄───── Receives Raw [CONNACK] Response (0x20) ◄────┘
```

### 1. The Persistent Binary Socket Pattern
Unlike HTTP, which opens a transient stream, shifts text, and immediately severs the pipeline, MQTT operates over a long-lived, persistent TCP socket connection.
The connection relies heavily on specific byte headers rather than text delimiters like `\r\n`.

### 2. Anatomy of a Manual `CONNECT` Frame Configuration
To announce presence to a broker, the client engine must compose a strict byte array payload matching the protocol structure exactly.
For a basic clean-session state connection with a 4-character client identifier name (e.g., `"TEST"`), the frame parses down to these bytes:

*   **Fixed Header Type:** `0x10` (Declares a `CONNECT` control packet).
*   **Remaining Length Size:** `0x10` (Declares that 16 bytes follow this length byte).
*   **Protocol String Length:** `0x00 0x04` (Most significant byte / Least significant byte of length tracker).
*   **Protocol Name Identifier:** `0x4D 0x51 0x54 0x54` (ASCII literal encoding for string `"MQTT"`).
*   **Protocol Level Generation:** `0x04` (Specifies MQTT v3.1.1 standard compliance).
*   **Connect Flag Matrix:** `0x02` (Enables the Clean Session bit flag parameter).
*   **Keep-Alive Timer Bounds:** `0x00 0x3C` (2-byte integer tracking a 60-second time boundary).
*   **Payload String Length:** `0x00 0x04` (2-byte integer mapping payload identifier length).
*   **Payload Client ID string:** `0x54 0x45 0x53 0x54` (ASCII literal string characters matching `"TEST"`).

### 3. Verification of the `CONNACK` Response Byte Matrix
After the broker processes the connection parameters, it returns an explicit 4-byte tracking frame down the open TCP pipe. The application engine must trap and analyze the stream instantly:
*   **Byte 0:** `0x20` (Confirming an incoming `CONNACK` command header block).
*   **Byte 1:** `0x02` (Indicates 2 remaining payload verification bytes follow).
*   **Byte 2:** Session Present Flag (`0x00` or `0x01`).
*   **Byte 3:** Connect Return Code (`0x00` declares connection accepted. Any non-zero payload indicates a protocol failure).

---

## 🎯 Engineering Requirements

1.  **Detached Thread Connection Matrix:** Initialize a raw TCP network socket stream (`java.net.Socket`) entirely away from the main thread loop context to safely bypass network thread allocation crashes.
2.  **Manual Protocol Serialization:** Programmatically synthesize the multi-segmented `CONNECT` control byte block entirely inside raw `ByteArray` structures without calling third-party wrapper libraries.
3.  **Synchronous Handshake Inspection:** Force a blocking read operation on the input socket stream to pull the exact 4-byte `CONNACK` frame, parsing the data array to confirm connection status indicators.
4.  **Zero-XML Terminal Log Interface:** Construct an interactive, pure-Kotlin view dashboard containing input address fields, a connect trigger mechanism, and dynamic trace buffers tracking Hungarian naming rules.

---

## 🔍 Code Skeleton Templates

### 1. Matrix Control Console UI Layout (`MqttEasyActivity.kt`)
Assemble an interactive telemetry visualization workspace completely in code. Adhere strictly to Hungarian prefix styles within package path `localhost.idroid.salamun`.

```kotlin
package localhost.idroid.salamun

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket

class MqttEasyActivity : Activity() {

    private val hMain = Handler(Looper.getMainLooper())
    private var tcpSocket: Socket? = null
    private var isConnectedState = false

    // TODO: Declare late-init Hungarian shorthand visual primitives: llRoot, etBrokerAddress, btnConnectBroker, svTelemetryScroll, tvTelemetryLog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Programmatically instantiate layout view containers (Zero XML / Zero AndroidX)
        // TODO: Wire up btnConnectBroker click listener to trigger executeHandshakePipeline()
    }

    private fun executeHandshakePipeline(address: String) {
        tvTelemetryLog.append("MQTT CONTEXT: Dispatching network connection request to \$address...\n")

        Thread {
            try {
                // TODO: Initialize connection via java.net.Socket on default MQTT port 1883
                // TODO: Invoke serialiseAndPushConnectPacket() over socket output stream
                // TODO: Invoke captureAndVerifyConnack() over socket input stream
                
                hMain.post {
                    // TODO: Mutate UI interactive elements to reflect verified connection state metrics
                }
            } catch (e: Exception) {
                hMain.post { tvTelemetryLog.append("MQTT EXCEPTION: \${e.localizedMessage}\n") }
                safelyTeardownChannels()
            }
        }.start()
    }

    private fun serialiseAndPushConnectPacket(outputStream: OutputStream) {
        // TODO: Construct a strict multi-segmented byte array manually containing:
        //       Fixed Header, Variable Header (Protocol parameters, Connect flags, Keep-alive), and ClientID payload
        // TODO: outputStream.write(connectPacketByteArray)
        // TODO: outputStream.flush()
    }

    private fun captureAndVerifyConnack(inputStream: InputStream) {
        val bufferResponse = ByteArray(4)
        // TODO: Force read exactly 4 bytes out of the stream into the validation buffer
        
        // TODO: Perform byte validation parsing:
        //       - Validate bufferResponse[0] == 0x20 (CONNACK packet type check)
        //       - Validate bufferResponse[3] == 0x00 (Connection accepted code check)
        
        hMain.post {
            tvTelemetryLog.append("MQTT HANDSHAKE: CONNACK verified. Connection accepted.\n")
        }
    }

    private fun safelyTeardownChannels() {
        // TODO: Close socket references safely and restore tracking configuration states
    }
}
```

---

## 🧪 Verification & Testing Checklist

- [ ] **Public Sandbox Address Setup:** Input a recognized public testing broker sandbox domain path (e.g., `://hivemq.com` or `test.mosquitto.org`) into `etBrokerAddress`.
- [ ] **Handshake Execution Tracking:** Click the interactive trigger control and inspect the real-time textual terminal readout frames.
- [ ] **Byte-Level Protocol Match:** Verify that the incoming byte layout maps exactly back to an accepted server code matching signature index value `0x20 0x02 0x00 0x00`.
