# 🟡 Challenge 2: Dynamic REST Router & POST Payload Parser (Advanced)

## 🔬 Challenge Architecture & Theory

Moving beyond a simple single-page web host, a modern embedded network node must act as a **Remote View Controller**.
This requires building a dynamic **REST Router Engine** that parses query targets and strips HTTP `POST` request payload parameters down to byte-level accuracy.

```text
               ┌──► GET /api/status  ──► [Read System States Block]
               │
[Router Matrix]├──► POST /api/config ──► [Parse Content-Length] ──► [Extract Form Body]
               │                                                          │
               └──► Any Unmatched    ──► [Render Code 404 Failure Page] ──► [Post to UI Handler]
```

### 1. Multi-Threaded Executor Pools
Spawning a manual `Thread` for every connection becomes unsustainable under heavy network loads, leading to execution thrashing and system memory fragmentation.
A robust network engine manages incoming pipelines through a bounded **Executor Thread Pool (`java.util.concurrent.ExecutorService`)**.
The main background listener continuously queues incoming raw `Socket` handles, allowing a managed array of background worker nodes to pick up and parse transaction data concurrently.

### 2. Byte-Accurate POST Body Parsing Mechanics
Unlike `GET` requests where metadata is fully isolated inside the URL request line, `POST` parameters reside inside a separate trailing body payload segment.
Because a continuous TCP stream remains connected waiting for future packets, the input stream will **never** return an End-of-Stream (`-1`) flag while processing an active channel.
*   **Header Scanning Block:** The server must parse incoming text lines sequentially until it reaches the empty structural separator sequence (`\r\n\r\n`).
*   **Content-Length Extraction:** The engine must inspect the isolated header metadata case-insensitively for the `Content-Length` identifier,
extracting the raw string value into a clean integer count (`X`).
*   **Direct Buffer Read:** To capture the exact request body payload without hanging or stalling the connection pipe,
the server must transition away from line-oriented text readers and pull precisely `X` bytes directly out of the raw underlying stream container buffer into a dedicated byte array.

### 3. Application/X-WWW-Form-Urlencoded Decoding
Web browser engines package HTML interaction forms into percentage-encoded ASCII streams (e.g., `ui_title=IDroid+Terminal&bg_color=%23ff0000`). The router must:
1.  Convert raw payload buffer arrays into structured text strings.
2.  Split the data sequence using the standard key-value delimiter token (`&`).
3.  Isolate individual transaction keys and data attributes via the property assignment token (`=`).
4.  Apply `java.net.URLDecoder.decode(value, "UTF-8")` to reverse percentage escapes and whitespace modifications.

---

## 🎯 Engineering Requirements

1.  **Fixed Thread Pool Isolation:** Transition connection scaling mechanics from loose manual threads to a structured `Executors.newFixedThreadPool(4)` execution layer.
2.  **REST Endpoint Resolution Table:** Build a custom string matching matrix to intercept and handle at least three discrete endpoint channels:
    *   `GET /api/status` $\rightarrow$ Return JSON text payload displaying current uptime counter metrics.
    *   `POST /api/config` $\rightarrow$ Extract form input parameters to dynamically update application components.
    *   *Default Target Fallback* $\rightarrow$ Return a compliant `404 Not Found` HTML layout window.
3.  **UI Core Transformation Pipeline:** Extract data strings from incoming `POST` blocks (e.g., matching a variable key `server_label`)
and push them onto the main thread via `android.os.Handler` execution contexts to dynamically alter the text attributes of your active UI views.
4.  **Byte-Safe Reading Guard:** Implement validation mechanisms to drop connection streams instantly
if an incoming request attempts a `POST` transaction without specifying a valid `Content-Length` parameter.

---

## 🔍 Code Skeleton Templates

### 1. View Matrix Architecture Layout (`WebAdvancedActivity.kt`)
Implement a zero-XML framework landing controller surface. Manage framework bindings safely to synchronize background worker logging loops into interactive UI space.

```kotlin
package localhost.idroid.salamun

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class WebAdvancedActivity : Activity(), DynamicRouterService.RouterStatusListener {

    private var sBoundRouter: DynamicRouterService? = null
    private var isRouterBound = false

    // TODO: Declare late-init Hungarian shorthand visual primitives: llRoot, btnToggleEngine, tvServerTitle, svConsoleScroll, tvConsoleOutput

    private val scRouterConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            // TODO: Extract local service binder reference and register status listener matrix interface
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            // TODO: Handle clean de-allocation and state reset
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // TODO: Build vertical layout matrix programmatically (No XML Allowed)
        // TODO: Wire up click listeners tracking service engine state flags
    }

    override fun onStart() {
        super.onStart()
        // TODO: Invoke context bind calls onto DynamicRouterService matrix
    }

    override fun onStop() {
        // TODO: Safely unbind service connection to prevent framework contextual leaks
        super.onStop()
    }

    override fun onLogReceived(message: String) {
        // TODO: Append message metrics and trail scroll layouts downwards
    }

    override fun onTitleUpdateRequired(newTitle: String) {
        // TODO: Safely update tvServerTitle surface configuration text layout
    }

    override fun onEngineStateChanged(running: Boolean) {
        // TODO: Synchronize toggle button text label matching state execution flags
    }
}
```

### 2. Multi-Threaded Service Router Engine (`DynamicRouterService.kt`)
Orchestrate a sticky application daemon layer utilizing fixed concurrency structures to process incoming HTTP parameters concurrently.

```kotlin
package localhost.idroid.salamun

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import java.io.InputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService

class DynamicRouterService : Service() {

    private val bLocalRouter = LocalRouterBinder()
    private val hMain = Handler(Looper.getMainLooper())
    private var tpExecutor: ExecutorService? = null
    private var serverSocket: ServerSocket? = null
    private var isEngineRunning = false
    private var routerListener: RouterStatusListener? = null

    interface RouterStatusListener {
        fun onLogReceived(message: String)
        fun onTitleUpdateRequired(newTitle: String)
        fun onEngineStateChanged(running: Boolean)
    }

    inner class LocalRouterBinder : Binder() {
        fun getService(): DynamicRouterService = this@DynamicRouterService
    }

    override fun onBind(intent: Intent?): IBinder = bLocalRouter

    fun startRouterEngine() {
        if (isEngineRunning) return
        isEngineRunning = true
        
        // TODO: Initialize bounded fixed execution pool engine via java.util.concurrent.Executors
        // TODO: Spawn acceptance background worker loop thread container
    }

    private fun handleRouteTransaction(socket: Socket) {
        try {
            val inputRaw = socket.getInputStream()
            val reader = inputRaw.bufferedReader(Charsets.UTF_8)
            val outputRaw = socket.getOutputStream()

            var requestLine: String? = null
            var contentLength = -1

            // 1. TODO: Loop through and tokenized text lines until empty spacer sequence (\r\n\r\n) passes
            // 2. TODO: If a "Content-Length:" metadata block appears, extract integer sizing metric

            // 3. TODO: Parse HTTP request string signature tokens (Method & Path Target)
            
            // 4. TODO: Branch processing workflow using a when operational block matrix:
            //          - GET /api/status -> Return compliant application/json buffer
            //          - POST /api/config -> Perform byte-accurate input stream extraction matching Content-Length,
            //                                url-decode variables, and dispatch UI transformations via hMain
            //          - Else -> Fallback standard 404 html frame rendering routine

        } catch (e: Exception) {
            // TODO: Pass exception trace context to onLogReceived visual buffers
        } finally {
            // TODO: Enforce strict client containment closing execution phase
        }
    }

    private fun writeResponseFrame(output: java.io.OutputStream, status: String, contentType: String, payload: ByteArray) {
        // TODO: Assemble standard Layer 7 plain-text metadata configuration framing bytes 
        //       complying exactly with: Status Line, Content-Type, Content-Length, Connection: close
    }

    fun stopRouterEngine() {
	// TODO: Shut down ServerSocket execution, terminate tpExecutor contexts, reset active flags
	}
	override fun onDestroy() {
		stopRouterEngine()
		super.onDestroy()
	}
}
```


🧪 Verification & Testing Checklist

* **Network Map Configuration**: Route localhost framework pipelines onto the active running emulator instance:
 `bash adb forward tcp:8080 tcp:8080`
* **REST JSON Verification**: Fire an external `GET` testing command block from your local terminal workspace:
` bash curl http://localhost:8080/api/status`
_Expectation_: Verify that a clean JSON payload maps back with precise structural uptime integers.
* **POST Command Injection**: Execute a web form submission sequence targeting your remote routing configuration table:
`bash curl -X POST -d "server_label=PROD_NODE_MATRIX" http://localhost:8080/api/config`
_Expectation_: Check the device window interface surface. Verify that `tvServerTitle` instantly renders the incoming updated string parameter value safely.
