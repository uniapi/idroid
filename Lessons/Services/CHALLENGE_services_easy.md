# Challenge 1: Connectionless Background UDP Pulse Engine (Easy - Bound Service Protection)

## 🎯 Objective
Refactor a connectionless UDP socket receiver and telemetry transmitter to run inside an independent, decoupled native Android **Bound Service**.
The application must maintain its low-level network datagram background thread and incoming packet cache flawlessly
when the parent Activity is completely destroyed and recreated during an orientation change (hardware screen rotation).

---

## 🎨 Clean IDE & High-Contrast Styling Matrix

To maintain absolute visual consistency across all programmatic layouts in this module, the codebase utilizes the following accessible,
high-contrast color scheme directly inside the Kotlin source initialization blocks (`Color.parseColor(...)`), optimized with a high-contrast black primary text configuration.

| Layer Element | Color Name | Hex Code | Visual Benefit |
| :--- | :--- | :--- | :--- |
| **Terminal Canvas** | Slate Dark | `#1E1E24` | Relaxed dark interface background minimizing eye strain |
| **Text Primary** | Absolute Black | `#000000` | Maximum-contrast black output for dedicated text layout fields |
| **Text Secondary** | Muted Silver | `#A0A5B5` | Clean visibility for structural input hints and metadata |
| **Accent Primary** | Cobalt Professional | `#2F66DF` | High-visibility primary action buttons and focused states |
| **Accent Secondary** | Soft Amber | `#F5A623` | Distinguishable warnings, alerts, and engine state shifts |
| **Control Default** | Steel Gray | `#3A3F4D` | Low-fatigue structural containers and secondary buttons |

---

## 🏗 Architectural Requirements & Constraints
1. **100% Programmatic UI Layout:** Zero XML layout files or asset configurations.
All view hierarchies, dimensions, single-line text constraints, and vertical alignments are instantiated dynamically inside the Kotlin source via native framework APIs.
2. **Pure Native APIs Only:** No Jetpack libraries, AndroidX wrappers, or lifecycle extension components.
Inherit directly from `android.app.Activity` and `android.app.Service`. Use basic framework view objects (`LinearLayout`, `EditText`, `Button`, `ScrollView`, `TextView`).
3. **Local Binder Architecture:** Implement a subclassed `android.os.Binder` instance inside the service.
This allows the Activity to securely hook a direct memory reference to the active service pipeline upon configuration changes.
4. **Strict Variable Shorthand Notation Compliance:** The codebase enforces the standard shorthand repository variable structure:
   * Main Thread Handlers: `hMain`
   * Root View Groups: `llRoot`
   * Configuration Rows: `llConfigRow`
   * Input Controls: `etIp`, `etPort`, `etMsg`
   * Action Items: `btnBind`, `btnUnbind`, `btnSend`
   * Persistent Log Containers: `svConsole`, `tvLog`
   * Active Service Link: `sSvc`
   * Bound Interface Bridge: `bBinder`
   * Low-Level Sockets: `dsNode`

---

## 🧪 Verification Matrix Checkpoints
1. **The Rotation Test:** Tap `Start Engine`. Once the console logs `[STATUS] UDP pipeline actively listening on port 8080`, physically change your device or emulator orientation.
The system context resets and runs through `onCreate`, but the text cache is synchronized instantly, the items stay perfectly aligned on a single row via `Gravity.CENTER_VERTICAL`,
and the network background thread remains unbroken.
2. **Datagram Injection Verification:** Because UDP runs a connectionless socket model, you can instantly inject testing payloads from your development terminal via Netcat. 

   *The target IP input field (`etIp`) is utilized uniquely for routing outbound transmissions (`OUTBOUND`) from the device,
while the service captures inbound datagram packets on any available interface listening on the specified port configuration.*

   ```bash
   # If utilizing an Android emulator, map the interface port first:
   adb forward tcp:8080 tcp:8080
   
   # Inject your test payload packet:
   echo "MATRIX_PULSE" | nc -u -w1 10.0.2.2 8080
   ```
