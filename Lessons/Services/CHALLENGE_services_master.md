# Challenge 3: Multi-Client TLS Cryptographic Multiplexer Service (Master)

## 🎯 Objective
Re-architect the *TLS Secure Multi-Client Chat Server* into an autonomous, headless **Foreground Service**.
The cryptographic TLS server socket loop must run continuously in the background, accepting incoming TLS handshakes, multiplexing payload frames across thread-safe client connection pools,
and logging real-time engine health matrix arrays—completely independent of the Activity UI lifecycle and fully protected against low-memory process evictions.

---

## 🖤 Cyberpunk & Matrix Styling Matrix

To maintain visual consistency across all programmatic layouts in this module, use the following hex-color grid directly inside your Kotlin code initialization blocks (`Color.parseColor(...)`)
or use your own.

| Layer Element | Color Name | Hex Code | Visual Preview Anchor |
| :--- | :--- | :--- | :--- |
| **Terminal Canvas** | Obsidian Core | `#0C0E14` | Ultra-dark terminal background |
| **Text Primary** | Phosphor Green | `#50FA7B` | System outputs, metrics, and logs |
| **Text Secondary** | Telemetry Cyan | `#8BE9FD` | Structural metadata, status tags |
| **Accent Primary** | Cyber Pink | `#FF79C6` | Core headers, interactive highlights |
| **Accent Secondary** | Alert Amber | `#FFB86C` | Warnings, server state shifts |
| **Control Default** | Cobalt System | `#82AAFF` | Buttons, interactive control states |

---

## 🏗 Architectural Requirements & Constraints
1. **100% Programmatic UI Layout:** Zero XML layouts or resources. The administrative server panel must be generated dynamically from native code.
2. **Pure Native APIs Only:** No Jetpack components or `NotificationCompat` dependencies. Use raw `android.app.Service` and native cryptography hooks (`javax.net.ssl.SSLServerSocket`).
3. **Headless Cryptographic Engine:** The entire server lifecycle must reside in a Foreground Service context. The cryptographic keystore (`BKS` or `PKCS12`) must be loaded from internal application storage or assets directly using the service context.
4. **Thread-Safe Multiplexing Architecture:** Incoming multi-client streams must be handled concurrently using thread-safe structures (e.g., `CopyOnWriteArrayList` or synchronized node tables).
5. **Strict Variable Shorthand Notation Compliance:** You must use the exact shorthand naming conventions enforced across the IDroid repository:
   * Main Handlers: `hMain`
   * View Groups: `llRoot`, `llServerPanel`
   * Interactive Handles: `btnStartEngine`, `btnStopEngine`, `tvClientCount`
   * Persistent Text Log Output: `svMuxLog`, `tvMuxConsole`
   * Notification Containers: `ncCryptoChannel`, `nEngineItem`
   * Core Sockets: `sssSecureListener`, `sClientNode`

---

## 🗺 Structural Blueprint & Code Templates

To solve this challenge, you will implement two separate core components: `TlsMuxEngineActivity` and `TlsMuxEngineService`.
Below are the structural templates establishing the decoupled architectural boundaries.

### 1. Headless TLS Multiplexer Service Blueprint (`TlsMuxEngineService.kt`)
```kotlin
class TlsMuxEngineService : Service() {
    private var sssSecureListener: SSLServerSocket? = null
    private val clientNodePool = java.util.concurrent.CopyOnWriteArrayList<Socket>()
    private var isMuxActive = false

    private val NOTIFICATION_ID = 3033
    private val CHANNEL_ID = "TLS_MUX_ENGINE_CHANNEL"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 1. Initialize low-level NotificationChannel with high priority
        // 2. Bind startForeground() to raise process rank above LMK thresholds
        // 3. Load KeyStore from Context inside a background thread pool
        // 4. Spin up the continuous blocking SSLServerSocket accept loop
        return START_STICKY
    }

    // Cryptographic stream handshakes and packet multiplexing pipelines go here
}
```

### 2. Administrative Control UI Frame (`TlsMuxEngineActivity.kt`)
```kotlin
class TlsMuxEngineActivity : Activity() {
    private val hMain = Handler(Looper.getMainLooper())
    private lateinit var llRoot: LinearLayout
    private lateinit var tvMuxConsole: TextView

    // Decoupled inter-process broadcast receiver for real-time telemetry updates
    private val muxTelemetryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Extract decrypted frame metrics and stream to tvMuxConsole
        }
    }

    // Programmatic UI instantiation loop goes here
}
```

---

## 🧪 Verification Matrix Checkpoints
1. **The Deep Sleep Persistence Test:** Spin up the TLS Engine, connect multiple independent remote nodes via terminal simulators, and swap payloads. Lock the Android device or put the emulator into a deep power-sleep state (`adb shell am set-inactive`). Send transmission frames from an external node; verify that the cryptographic Engine continues to instantly multiplex and process frames.
2. **Dynamic UI Re-Attach Checkpoint:** Connect multiple clients so that the console populates with active cryptographic handshakes. Close the administrative panel completely by killing the UI task hierarchy. Reopen the application launcher; the Activity must instantly re-synchronize, query the background state pool, and seamlessly display the running logs.
