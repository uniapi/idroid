# 🟢 Challenge 1: Minimal Stateless HTTP Info Host (Easy)

## 🔬 Challenge Architecture & Theory

To build a web server from scratch, you must transition from treating raw TCP socket communication as arbitrary byte chunks to treating it as a structured,
sequential text protocol matching the Layer 7 **HTTP/1.1 Specification**.

```text
[Browser Client] ──► (Connects to Port 8080) ──► [ServerSocket.accept()]
                                                          │
   [Close Connection] ◄── [Flush Socket Engine] ◄── [Parse Raw Headers Input]
```

### 1. The HTTP/1.1 Stateless Standard
Every HTTP interaction begins with a text-based request from a client browser and ends with a formatted text response from the server.
The protocol is inherently **stateless**; the connection is opened, a single transaction occurs, and the connection is closed.

### 2. Anatomy of an Incoming HTTP Request Line
When a client hits your server, the first line transmitted across the socket input stream is the **Request Line**. It contains exactly three pieces of metadata separated by spaces:
```text
GET /index.html HTTP/1.1\r\n
```
*   **Method (`GET`):** The action required. In this challenge, we only handle `GET`.
*   **Path (`/index.html`):** The resource target being requested.
*   **Protocol Version (`HTTP/1.1`):** Defines the standard constraints.

This line is immediately followed by a block of meta-headers (e.g., `Host`, `User-Agent`) ending with a strict **blank separator line** composed of a carriage return and a line feed: `\r\n\r\n`. 

### 3. The Minimum Compliant HTTP Response Structure
To make a standard web browser recognize your custom server output as a valid website rather than corrupted binary noise, your response *must* match this layout exactly:

```text
HTTP/1.1 200 OK\r\n
Content-Type: text/html; charset=utf-8\r\n
Content-Length: [Exact Byte Count]\r\n
Connection: close\r\n
\r\n
<!DOCTYPE html><html><body>... Your Payload Here ...</body></html>
```

*   **Status Code (`HTTP/1.1 200 OK`):** Tells the client browser the connection was successfully handled.
*   **Content-Type:** Declares the MIME type (`text/html`) so the browser renders it as a page rather than downloading it as an unknown binary file.
*   **Content-Length:** The precise number of bytes in the payload string. If this is missing or incorrect, browsers will hang indefinitely waiting for the remaining content.

---

## 🎯 Engineering Requirements

1.  **Thread Isolation:** Implement the server socket setup entirely away from the Android Main UI Thread to avoid a `NetworkOnMainThreadException`.
2.  **Raw Line Tokenization:** Use a memory-buffered text reader to capture lines coming from the client input socket. Parse at least the first line to confirm a `GET` action.
3.  **Dynamic Device Information Payload:** Programmatically pull live system metrics from the Android framework to compose the HTML page:
    *   `android.os.SystemClock.elapsedRealtime()` for system uptime.
    *   `android.os.Build.MODEL` and `android.os.Build.VERSION.RELEASE` for hardware fingerprint details.
4.  **Resource Cleanup:** Safely close both the input reader, the output writer, and the individual client socket immediately after flushing the payload buffer.

---

## 🧪 Verification & Testing Checklist

- [ ] **Network Redirect Setup:** Execute port mapping using your local terminal to allow communication across the emulator boundary:
  ```bash
  adb forward tcp:8080 tcp:8080
  ```
- [ ] **Browser Verification:** Open a browser window on your host computer and navigate to:
  ```text
  http://localhost:8080/
  ```
- [ ] **Visual Confirmation:** Verify that the page loads natively without throwing protocol errors and displays the correct dynamic Android system metadata.
