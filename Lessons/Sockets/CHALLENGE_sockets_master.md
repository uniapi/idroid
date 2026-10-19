# Challenge 3: Multi-Client TLS Secure Chat Server & Frame Multiplexer (Master)

## 🎯 Goal
Build a high-throughput, multi-client Secure Chat Server Activity entirely in code (no XML layouts, no AndroidX components) under the `localhost.idroid.salamun` package layer. 

The application will initialize a persistent, long-running `javax.net.ssl.SSLServerSocket` on port `9999` using a sandboxed local cryptographic keystore database.
The server must handle **multiple concurrent encrypted client connections simultaneously**. Each connection node must be managed on an isolated worker thread.
When a client pumps an encrypted message block over the wire, the server must securely multiplex and broadcast that data frame to all other active connected client nodes
without dropping interface rendering frames.

---

## 🏗️ Technical Specification & Architecture

### 1. View & UI Monitor Hierarchy
Assemble your real-time secure network dashboard programmatically inside `onCreate`:
*   Use a manual vertical root container (`android.widget.LinearLayout` prefixed as `llRoot`).
*   Instantiate an active connection telemetry monitor field (`android.widget.TextView` prefixed as `tvActiveConnections`) showing the live count of clamped secure client nodes.
*   Instantiate an explicit `android.widget.ScrollView` container (`svTerminalScroll`) wrapping around an `android.widget.TextView` prefixed as `tvTerminalLog`
to view cryptographic handshake events, traffic loops, and disconnect traces.

### 2. Deep Under-The-Hood Secure Architecture Rules
*   **The Cryptographic Handshake:** You cannot accept raw text strings. You must bootstrap a native `SSLContext` configured with a local PKCS12 keystore file
containing a private key and self-signed certificate signature. This context builds the `SSLServerSocketFactory` used to allocate your secure server socket.
*   **Thread-Safe Concurrency Multiplexing:** The `sslServerSocket.accept()` block must run inside an infinite loop on a dedicated listener thread.
When a client socket completes its TLS handshake, append the node to a thread-safe tracker array (e.g., `java.util.concurrent.CopyOnWriteArrayList`) and spawn a distinct worker reading loop thread for it.
*   **Stream Boundary Delimitation:** Because TCP treats data as a single continuous byte channel, clients must wrap messages using character line delimiters (`+ "\n"`).
The worker thread loops on `BufferedReader.readLine()`, which blocks until a newline sequence matches, decrypts the text block transparently, and broadcasts it across all other connected socket writers.

---

## 🔍 Step-by-Step Architecture Guide

### Step 1: Initialize Secure Context and Prefix-Compliant Code Views
```kotlin
package localhost.idroid.salamun

private val hMain = Handler(Looper.getMainLooper())
private val activeConnectedClients = CopyOnWriteArrayList<Socket>()
private val atomicActiveCount = AtomicInteger(0)

// View references with strict Hungarian notation prefixes
private lateinit var tvActiveConnections: TextView
private lateinit var tvTerminalLog: TextView
```

### Step 2: Bootstrap the Native TLS SSLServerSocket Factory
Before starting the network loop, load your sandboxed keystore binary and initialize the encryption engine primitives:
```kotlin
val keyStore = KeyStore.getInstance("PKCS12")
val keystoreFile = File(filesDir, "idroid_keystore.p12")

FileInputStream(keystoreFile).use { fis ->
    keyStore.load(fis, "salamun_secret_pass".toCharArray())
}

val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()).apply {
    init(keyStore, "salamun_secret_pass".toCharArray())
}

val sslContext = SSLContext.getInstance("TLSv1.2").apply {
    init(kmf.keyManagers, null, null)
}

val sslServerSocket = sslContext.serverSocketFactory.createServerSocket(9999) as SSLServerSocket
```

### Step 3: Implement the Continuous Multi-Client Interception Loop
```kotlin
Thread {
    while (isServerRunning) {
        val secureClientSocket = sslServerSocket.accept() // Blocks waiting for clients
        activeConnectedClients.add(secureClientSocket)
        
        val currentTotal = atomicActiveCount.incrementAndGet()
        hMain.post { tvActiveConnections.text = "SECURE ACTIVE NODES: $currentTotal" }
        
        // Spawn a completely independent client reader lane thread
        spawnClientReaderPipeline(secureClientSocket)
    }
}.start()
```

---

## 🧪 Verification & Acceptance Criteria
1.  **True Encrypted Multi-Client Multiplexing:** Multiple terminal endpoints (physical devices, secure external tools, or custom `SSLSocket` clients) connect simultaneously, complete the TLS v1.2 handshake, and broadcast text streams cleanly.
2.  **Shorthand Variable Compliance:** Every view object, layout handle, and UI scheduler follows Hungarian type prefix conventions (`hMain`, `llRoot`, `tvActiveConnections`, `svTerminalScroll`, `tvTerminalLog`).
3.  **Thread Pool Resilience:** If one client forces a socket termination or drops its network connection interface, its dedicated background thread loop exits cleanly, closing its I/O handles without leaking thread memory or crashing the master server pipeline.

# 🔑 Certificate Keystore Generation Reference
To execute this Master Challenge, your environment must include a valid PKCS12 data package certificate layout signature. Execute the following native platform commands in your developer terminal workspace to compile the asset:

```bash
keytool -genkeypair \
  -alias idroid_server \
  -keyalg RSA \
  -keysize 2048 \
  -validity 365 \
  -keystore idroid_keystore.p12 \
  -storetype PKCS12 \
  -storepass salamun_secret_pass \
  -dname "CN=localhost, OU=IDroid, O=uniapi, L=Brest, C=BY"
```

Once the asset has been written, deploy it directly to your target emulator testing path by triggering an `adb` command frame:
```bash
adb push idroid_keystore.p12 /data/data/localhost.idroid.salamun/files/
```

Or supply with your app as assets and use `AssetManager` if no root access

### Additional Steps for API Level 10
```bash
openssl pkcs12 -in assets/idroid_keystore.p12 \
               -out assets/temp_bundle.pem \
               -passin pass:salamun_secret_pass \
               -nodes
```

```bash
openssl pkcs12 -export \
               -in assets/temp_bundle.pem \
               -out assets/idroid_keystore_legacy.p12 \
               -passout pass:salamun_secret_pass \
               -legacy
```

```bash
mv assets/idroid_keystore_legacy.p12 assets/idroid_keystore.p12
rm assets/temp_bundle.pem
```
