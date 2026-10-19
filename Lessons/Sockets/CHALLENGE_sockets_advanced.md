# Challenge 2: Bi-Directional TCP Command Matrix Terminal (Advanced)

## 🎯 Goal
Build a reliable, bi-directional network console Activity entirely in code (no XML, no AndroidX) designed explicitly for **dual-device cross-communication over a shared Wi-Fi network local interface**. 

The activity will support a selectable mode matrix: it can act as a **TCP Master Server Listener** or an **Active Client Terminal Instance**.
Once connected via explicit TCP sockets, the two devices will transmit string messages back and forth through sequential stream buffers (`BufferedReader`/`BufferedWriter`),
handling network synchronization and terminal updates smoothly.

---

## 🏗️ Technical Specification & Architecture

### 1. View & UI Mode Selection Layout
Assemble a comprehensive network command terminal inside the Activity code within `onCreate`:
*   Use a vertical root container (`android.widget.LinearLayout` prefixed as `llRoot`).
*   Instantiate an address config input container row (`android.widget.LinearLayout` prefixed as `llConfigRow`) holding:
    *   `EditText` labeled IP targets prefixed as `etTargetIp` (defaulting to a local sample block configuration).
    *   `Button` labeled "START MASTER SERVER" prefixed as `btnStartServer`.
    *   `Button` labeled "CONNECT AS CLIENT" prefixed as `btnConnectClient`.
*   Instantiate an interactive messaging field layout row holding:
    *   `EditText` for custom content prefixed as `etMessageInput`.
    *   `Button` labeled "SEND" prefixed as `btnSendMessage`.
*   Instantiate a scrolling list view area (`android.widget.ScrollView` prefixed as `svTerminalScroll`) wrapping around an `android.widget.TextView` prefixed as `tvTerminalLog` to output data streams.

### 2. Dual-Device Network Execution Rules
*   **The Server Role Configuration:** Clicking `btnStartServer` launches a background worker loop that opens a native `ServerSocket(8888)`.
It blocks and awaits a connection from a second device using `serverSocket.accept()`. Once a connection hits, it holds the reference to that active `Socket` instance.
*   **The Client Role Configuration:** The second device enters the target IP of the first device into `etTargetIp` and clicks `btnConnectClient`.
This spawns a background thread to initiate a direct connection via `Socket(ipAddress, 8888)`.
*   **The Stream Parsing Pipeline:** Both modes must hand their connected `Socket` instances over to a persistent background communication thread.
This thread opens a `BufferedReader` over `socket.getInputStream()` and loops infinitely, waiting for lines terminated with a clear `\n` character wrapper to avoid stream reading blocks.

---

## 🔍 Step-by-Step Architecture Guide

### Step 1: Configuration Matrix Setup
Initialize your core synchronization primitives and prefix views under the `localhost.idroid.salamun` package scope:
```kotlin
package localhost.idroid.salamun

private val hMain = Handler(Looper.getMainLooper())
private var activeSocket: Socket? = null
private var socketReader: BufferedReader? = null
private var socketWriter: BufferedWriter? = null
```

### Step 2: Implement the Master TCP Acceptance Loop
```kotlin
Thread {
    try {
        val serverListener = ServerSocket(8888)
        hMain.post { tvTerminalLog.append("SERVER: Active on port 8888. Awaiting Client connection handshake...\n") }
        
        val clientSocket = serverListener.accept() // Blocks until second phone connects
        establishStreamChannels(clientSocket)
    } catch (e: Exception) {
        hMain.post { tvTerminalLog.append("SERVER ERROR: ${e.localizedMessage}\n") }
    }
}.start()
```

### Step 3: Implement Continuous Input Channel Streaming
Once a connection is established by either method, bind the text input buffers tightly to a read pipeline:
```kotlin
private fun establishStreamChannels(socket: Socket) {
    activeSocket = socket
    socketReader = socket.getInputStream().bufferedReader(Charsets.UTF_8)
    socketWriter = socket.getOutputStream().bufferedWriter(Charsets.UTF_8)
    
    Thread {
        try {
            while (activeSocket?.isConnected == true) {
                val incomingLine = socketReader?.readLine() ?: break // Returns null if socket drops
                hMain.post { tvTerminalLog.append("INCOMING DATA stream: $incomingLine\n") }
            }
        } catch (e: Exception) {
            hMain.post { tvTerminalLog.append("DISCONNECTED from node matrix.\n") }
        }
    }.start()
}
```

---

## 🧪 Verification & Acceptance Criteria
1.  **Guaranteed TCP Byte Isolation:** Data delivery is handled using `ServerSocket` and `Socket` streams. No data is lost or re-ordered across the Wi-Fi connection loop.
2.  **Shorthand Variable Compliance:** View references must implement explicit Hungarian shorthand notation styles (`hMain`, `llRoot`, `llConfigRow`, `etTargetIp`, `btnStartServer`, `btnConnectClient`, `etMessageInput`, `btnSendMessage`, `svTerminalScroll`, `tvTerminalLog`).
3.  **Dual-Device Verification:** The network layout functions across two separate physical Android devices connected to the same Wi-Fi router.
4.  **No Interface Lag:** Large text input transmission blocks never collide with the system UI main loop sequence.
