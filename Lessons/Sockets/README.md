# 🔌 Native Network Communications: Socket Primitives & TLS Transport

Welcome to the low-level Android Socket layer. In this module, we bypass all high-level application protocols to master Layer 4 transport components
using native Kotlin network socket loops directly over the Linux kernel interfaces.

---

## 🔬 Deep Architecture: The Layer 4 Infrastructure

### 1. Connectionless Packets (UDP)
User Datagram Protocol (UDP) operates entirely on a connectionless, "fire-and-forget" packet infrastructure.
*   **The OS Layer:** There is no kernel-level 3-way handshake, no delivery verification, no sliding-window congestion control, and no packet re-ordering guards.
Data is wrapped into a binary chunk called a `DatagramPacket` and transmitted over the network interface card immediately.
*   **Buffer Traversal:** The receiving system runs an infinite loop blocking on `DatagramSocket.receive()`.
If an incoming packet exceeds the allocated `ByteArray` buffer space, the trailing packet bytes are truncated and lost at the OS network stack layer.
*   **Tooling Primitives:** `java.net.DatagramSocket` and `java.net.DatagramPacket`.

### 2. Connection-Oriented Reliable Byte Streams (TCP)
Transmission Control Protocol (TCP) establishes a virtual, full-duplex persistent data pipe between nodes via an explicit kernel-level 3-way handshake (`SYN` ➔ `SYN-ACK` ➔ `ACK`).
*   **The Stream Boundary Problem:** Unlike UDP, TCP **does not have packet boundaries**. The operating system stack treats data transmission as a single continuous stream of incoming bytes.
If a client writes two strings (`"Hello"` and `"World"`) consecutively, the receiver's `InputStream.read()` may read them combined as a single block (`"HelloWorld"`),
or fragmented chunks (`"Hel"`, `"loWor"`, `"ld"`).
*   **The Solution Layout:** You must explicitly delimit frames using one of two primitive mechanisms:
    1.  *Character Delimitation:* Appending a specific marker like a trailing newline (`\n`) and reading via a buffered reader line tracker.
    2.  *Length Prefixing:* Writing a fixed-size header (e.g., a 4-byte Big-Endian Int) declaring the exact size of the trailing data payload block.
*   **Tooling Primitives:** `java.net.ServerSocket` and `java.net.Socket`.

### 3. Transport Layer Security (TLS / SSL Sockets)
When moving from standard TCP to Secure Sockets, a cryptographic layer (Layer 5) is injected directly between the raw TCP socket and your application stream handlers.
*   **The TLS Handshake Pipeline:** Before any application data bytes can traverse the stream, the `SSLSocket` forces a complex cryptographic handshake over the established TCP socket:
    1.  *ClientHello / ServerHello:* Negotiation of protocol versions (e.g., TLS v1.3) and cipher suites.
    2.  *Certificate Exchange:* The server presents its public security credentials to prove its identity.
    3.  *Key Exchange & Derivation:* Asymmetric cryptography is used to safely agree upon ephemeral, short-lived **symmetric keys**.
    4.  *Encrypted Handshake:* All future data passing through the `InputStream` and `OutputStream` is automatically encrypted and decrypted transparently by the platform security provider.
*   **Tooling Primitives:** `javax.net.ssl.SSLServerSocketFactory`, `javax.net.ssl.SSLSocketFactory`, `javax.net.ssl.SSLServerSocket`, and `javax.net.ssl.SSLSocket`.

---

## 🛠️ The Pure-Kotlin Tooling Rules Set

Since we operate **strictly without XML layouts, Jetpack, or third-party client components**, you must orchestrate your network loops using the following native platform configurations:

### A. Manifest Permissions Guard
Low-level socket allocation requires explicit permission blocks inside your app's manifest file to prevent the security kernel from throwing immediate security exceptions:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

### B. NetworkOnMainThreadException Shield
The Android operating system throws a fatal `NetworkOnMainThreadException` if any socket allocation, connection handshake loop,
or stream write transaction is invoked on the primary UI thread loop. You must leverage your background concurrency architecture from the **Concurrency & Handlers** module.
Open socket loops entirely inside an isolated `Thread`, and jump back to your programmatic prefixed views using `Handler(Looper.getMainLooper())`.

---

## 🏆 Module Progress Map
- **Challenge 1: Connectionless UDP Telemetry Pumper (Easy - Single Device Loopback)**
- **Challenge 2: Bi-Directional TCP Command Matrix Terminal (Advanced - Dual Device Network)**
- **Challenge 3: Multi-Client TLS Secure Chat Server & Frame Multiplexer (Master - Encrypted Multi-Node Network)**
