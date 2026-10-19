# Challenge 1: Connectionless UDP Telemetry Pumper (Easy)

## 🎯 Goal
Build a high-frequency coordinate tracking Activity completely in code (no XML layouts, no AndroidX components) running under the `localhost.idroid.salamun` package layer. 

The application will run **both a UDP Listener Thread and an interactive Client UI loop on the same device**.
Dragging your finger across a custom touch canvas view will dynamically serialize raw `(X, Y)` position strings into binary byte packets
and shoot them via loopback (`127.0.0.1`) into the background listener thread, which decodes them and renders the coordinates onto a live terminal text console view in real-time.

---

## 🏗️ Technical Specification & Architecture

### 1. View & UI Hierarchy
Build your UI layout programmatically inside `onCreate`:
*   Use a vertical root container (`android.widget.LinearLayout` prefixed as `llRoot`).
*   Instantiate an interactive coordinate transmission pad layout (`android.widget.FrameLayout` prefixed as `flTouchPad`) with a distinct background color.
*   Instantiate an `android.widget.TextView` prefixed as `tvTelemetryConsole` to stream received coordinate packet payloads.

### 2. Network & Threading Blueprint
*   **The Shared Port:** Choose a fixed non-privileged UDP target port (e.g., `9999`).
*   **The Background Listener:** On startup, spin up a long-running background thread. Open a `DatagramSocket(9999)` loop.
It must block and await incoming packets inside a continuous loop using an allocation buffer:
    ```kotlin
    val byteBuffer = ByteArray(256)
    val packet = DatagramPacket(byteBuffer, byteBuffer.size)
    datagramSocket.receive(packet) // Blocks until a packet hits port 9999
    ```
*   **The Touch Input Emitter:** Attach an `OnTouchListener` to `flTouchPad`. Capture `MotionEvent.ACTION_MOVE` coordinates. Format them into a text string (e.g., `X:342,Y:512`).
Instantly spawn an isolated worker thread to packetize the string and transmit it via a clean client `DatagramSocket` straight to `InetAddress.getByName("127.0.0.1")` on port `9999`.

---

## 🔍 Step-by-Step Architecture Guide

### Step 1: Initialize View Infrastructure with Hungarian Notation Prefixes
```kotlin
package localhost.idroid.salamun

val context = this
val llRoot = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
val flTouchPad = FrameLayout(context).apply { setBackgroundColor(Color.parseColor("#282A36")) }
val tvTelemetryConsole = TextView(context).apply { text = "TELEMETRY MONITOR ACTIVE:\n" }
```

### Step 2: Implement the Continuous UDP Server Loop
Initialize the background receiver socket within your thread architecture:
```kotlin
Thread {
    val socketReceiver = DatagramSocket(9999)
    val inputBuffer = ByteArray(256)
    
    while (!Thread.currentThread().isInterrupted) {
        val incomingPacket = DatagramPacket(inputBuffer, inputBuffer.size)
        socketReceiver.receive(incomingPacket) // Blocking primitive call
        
        val parsedData = String(incomingPacket.data, 0, incomingPacket.length, Charsets.UTF_8)
        
        hMain.post {
            tvTelemetryConsole.text = "RECEIVED PACKET DATA: $parsedData"
        }
    }
}.start()
```

---

## 🧪 Verification & Acceptance Criteria
1.  **Pure Datagram Packets:** Utilizes raw `DatagramSocket` and `DatagramPacket` primitives. No TCP connections or HTTP elements exist in the class.
2.  **Shorthand Variable Compliance:** View references must use exact prefix definitions (`hMain`, `llRoot`, `flTouchPad`, `tvTelemetryConsole`).
3.  **Frame Rate Stability:** Rapidly sliding your finger across the custom touch pad pumps out packet bursts continuously without causing UI stuttering or locking the rendering pipeline.
4.  **Resource Reclamation:** Exiting the activity closes the server socket loop immediately to release the system port allocation.
