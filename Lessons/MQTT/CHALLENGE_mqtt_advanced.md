### 🟢 Challenge 2: Persistent Pub/Sub Lifecycle & Heartbeat Service (Advanced — Persistent Foreground Service)
*   **Objective:** Relocate all socket streaming channels out of the fragile `Activity` context and into a bound Android `Service`.
You must implement a continuous dual-thread execution model to concurrently handle automated heartbeat tracking and instant binary stream evaluation.

#### 🪮 System Architecture Blueprint
```text
[MqttAdvancedActivity]  ◄═══ IPC Binding (ServiceConnection) ═══►  [MqttAdvancedService]
 (Rotates & Resumes Safely)                                         │ (Manages Live Sockets)
                                                                    ├─► Thread A: Inbound Network Loop & Stream Parsing
                                                                    └─► Thread B: Scheduled PINGREQ Heartbeat Loop (0xC0)
```

#### 🛠️ Deep Engineering Implementation Spec

##### Step 1: Establish the Inter-Process Communication (IPC) Binding Matrix
*   Create a nested `LocalBinder` class inside your service that returns the active service instance to the activity layer.
*   In your activity, override `onServiceConnected()` to capture this reference and attach a functional logging lambda `(String) -> Unit`.
When the activity undergoes an orientation rotation, the system will instantly re-bind and hook the lambda back into the engine without destroying the underlying TCP socket.

##### Step 2: Build the Bit-Shifted Inbound Packet Demultiplexer Loop
*   Launch a persistent background worker thread (`Thread`) enclosing a continuous blocking `while` loop that targets your network `InputStream`.
*   Read the very first byte of each incoming block. To decode the packet type, isolate the 4 upper bits by applying a bitwise shift right operation: `val packetType = fixedHeaderByte shr 4`.
*   Read the second byte to establish the frame's `Remaining Length`.
*   Implement a strict `when` statement matching the derived control nibble:
    *   **`0x02` (CONNACK):** Skip the remaining length bytes and update your local connection tracking booleans.
    *   **`0x09` (SUBACK):** Read the next 3 bytes to clear the packet identifier and confirm the granted QoS byte matches `0x00`.
    *   **`0x0D` (PINGRESP):** Intercept this packet, update your connection timeout parameters, and suppress the heartbeat countdown. Do *not* pass this up to the client telemetry tracking logs.
    *   **`0x03` (PUBLISH):** Implement dynamic byte-slicing.
	Read the next 2 bytes, evaluate them as an unsigned short to calculate the exact length of the topic string (N).
	Read N bytes into a buffer and transform it into UTF-8 text. Then, subtract 2 + N from your `Remaining Length` to find the precise byte size of the raw application payload remaining in the pipeline.

##### Step 3: Implement the Asynchronous Keep-Alive Ping Loop
*   Initialize a `ScheduledExecutorService` with a fixed thread pool size of 1. Configure it to run a tracking loop every 5 seconds.
*   Maintain a long integer timestamp (`lastActivityTimestamp`) that updates to `System.currentTimeMillis()` every single time the output stream writes data or the input stream successfully intercepts a packet.
*   During the recurring 5-second check, calculate the elapsed time since the last network event.
If the idle window exceeds 25 seconds (providing a 5-second safety buffer before the broker's 30-second drop limit),
clear the line and force-write a raw 2-byte array containing exactly `0xC0` followed by `0x00` down the output stream.

---
