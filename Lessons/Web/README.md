# 🌐 Native Application Protocols: Embedded Web Server & HTTPS Engine

Welcome to the low-level Android Web module. In this module, we bypass all high-level application server layers (such as Ktor, OkHttp, or NanoHTTPD)
to manually construct a live HTTP/HTTPS application host directly over raw socket execution matrices.

---

## 🔬 Deep Architecture: The Layer 7 HTTP/HTTPS Specification

### 1. The HTTP Text Specification Handshake
An HTTP Server is a persistent Layer 4 TCP server listener (java.net.ServerSocket) that blocks, accepts incoming connections,
and processes raw ASCII/UTF-8 text streams matching the strict Layer 7 Hypertext Transfer Protocol specification.
```text
       [Browser Client]                                 [Android ServerSocket]
              │                                                    │
              │ ─── (TCP 3-Way Handshake: SYN / ACK) ────────────► │
              │                                                    │
              │ ─── Raw ASCII Text Stream (\r\n) ────────────────► │ ──┐ Read first line
              │                                                    │   │ (Request Line)
              │                                                    │ ◄─┘
              │ ─── Header Fields (Key: Value\r\n) ──────────────► │ ──┐ Read headers until
              │                                                    │   │ blank separator
              │ ─── Blank Separator Line (\r\n\r\n) ──────────────►│ ◄─┘
              │                                                    │
```

* **The Request Structure**: A web client browser transmits a sequence of text strings over the raw input stream where each line must end with a carriage return and line feed combination (`\r\n`).
The entire header payload block concludes with a mandatory completely blank separator line (`\r\n\r\n`).
* **Parsing Protocol**: The server must extract and tokenize the first incoming string line (the Request Line) to isolate the active parameters:
```text
GET /index.html HTTP/1.1
Host: 192.168.1.50:8080
User-Agent: Mozilla/5.0 ...
```
The server splits this initial string by spaces ( ) to extract the HTTP Method verb (`GET`, `POST`, `PUT`, `DELETE`), the requested URL Route Path (e.g., /index.html), and the protocol version (HTTP/1.1).
* **The Response Specification**: To deliver data back to a client browser so it renders correctly, the raw byte output stream must map to the explicit HTTP structure.
The response consists of a status line, metadata headers containing content properties, a structural blank separator line, and the raw payload body:
```text
HTTP/1.1 200 OK\r\n
Content-Type: text/html; charset=UTF-8\r\n
Content-Length: 44\r\n
Connection: close\r\n
\r\n
<html><body><h1>System Online</h1></body></html>
```

### 2. HTTP POST Payload Extraction & Stream Splitting

Unlike `GET` requests where parameter states are bundled directly into the URL query string, an HTTP `POST` request pushes its transaction variables into a separate entity body section
appended after the double line break (`\r\n\r\n`) separator.
* **The Stream Termination Illusion**: Because a network socket stream remains persistent and open for potential pipelined requests,
reading the socket stream until it returns `-1` (end of stream) will block your server loop indefinitely. The client will wait forever, and the transaction will timeout.
* **The Content-Length Constraint**: To extract a POST body without hanging the socket thread,
the server state engine must actively scan the parsed header lines for the case-insensitive key `Content-Length: X`.
* **The Processing Loop**:
	1. Read the input stream line-by-line until an empty string ("") or single `\r\n` is encountered. This marks the end of the headers.
	2. Parse the integer value of `X` from the cached `Content-Length` header line.
	3. Initialize a discrete byte array of exactly size `X`.
	4. Read from the input stream using a bounded offset counter loop until exactly `X` bytes have been pulled out.
	This guarantees your code processes the complete body payload without drifting into blocking states.


### 3. Application Layer Security (HTTPS Encryption Architecture)
HTTPS is the identical Layer 7 HTTP text protocol running directly on top of an encrypted TLS socket channel (`SSLSocket`).
To initialize an embedded secure server on Android without external libraries, you must bootstrap three low-level security components:
1.  **A Keystore Container (`KeyStore`):** An encrypted local database file containing your server's **Private Key** and **Public Certificate**
(e.g., a `.p12` or `.bks` file stored in your internal sandboxed storage directories).
2.  **Key Managers (`KeyManagerFactory`):** Primitives that read your keystore, extract the private key material, and present it during the TLS cryptographic handshake.
3.  **An SSL Context (`SSLContext`):** The secure engine initialized with your key managers that acts as a factory to build your secure `SSLServerSocket`.
```text
[Keystore File (.p12)] ──► KeyManagerFactory ──► SSLContext ──► SSLServerSocket (Port 8443)
```
---

## 📚 Deep Dive: Expanded Low-Level Protocol Concepts

### 1. Finite State Machine Parsing of TCP Buffers
When building a production-grade raw socket parser, processing networks line-by-line using high-level readers like `BufferedReader.readLine()`
can lead to severe security vulnerabilities and memory starvation bugs (such as an attacker sending an infinite stream of bytes without a `\r\n` line ending). 

A safe engine tracks the input byte stream using a structured **Finite State Machine (FSM)**.
The engine reads raw bytes sequentially into a fixed circular byte ring buffer and moves between validation states:

```text
[STATE: METHOD] ──► (Space) ──► [STATE: URI] ──► (Space) ──► [STATE: HTTP_VERSION]
                                                                     │
  ┌──────────────────────────────────────────────────────────────────┘
  ▼
[STATE: HEADER_LINE] ──► (\r\n) ──► [STATE: HEADER_CRLF] ──► (\r\n\r\n) ──► [STATE: BODY_PARSING]
```

*   **Header Line Limits:** If any single line exceeds a safe diagnostic threshold (e.g., 8192 bytes),
the parser immediately responds with an isolated `HTTP/1.1 431 Request Header Fields Too Large` status line and drops the underlying raw file descriptor.
*   **Byte-Accurate Stream Splitting:** As soon as the transition sequence `0x0D 0x0A 0x0D 0x0A` (`\r\n\r\n`) passes the state comparator, the header section is declared closed.
The stream processing index shifts dynamically to read raw binary buffers up to the exact remaining `Content-Length` boundary limit.

### 2. Multi-Threaded Connection Demultiplexing
An embedded server running on Android cannot allow an active client connection to bottleneck or stall the arrival of concurrent external requests.
To maintain network synchronization, the engine separates the operational scopes into discrete thread domains:
*   **The Main Acceptance Thread Loop:** Run inside a persistent background execution loop that continuously invokes the blocking `ServerSocket.accept()` block.
*   **Worker Thread Allocation:** Once a client connection handshake lands, the generated `Socket` object is immediately offloaded into an isolated processing thread worker
or a structured execution thread pool matrix. The acceptance loop instantly recycles to track the next incoming connection frame.
*   **Keep-Alive vs Socket Close Management:** In an HTTP/1.1 loop, if the incoming headers contain `Connection: keep-alive`, the worker thread remains anchored to that specific client socket channel.
It sets a hardware read timeout using `socket.setSoTimeout(milliseconds)` and waits for subsequent transaction blocks on the same pipe until the timer expires.

### 3. Cryptographic Handshake Mechanics & TLS Architecture
During an encrypted HTTPS initialization sequence over an `SSLServerSocket`, the raw connection accept event triggers a hidden low-level cryptographic protocol choreography known as the **TLS Handshake**:

```text
Client                                                                Server
  │                                                                     │
  │  ───►───►─── ClientHello (Supported Ciphers, TLS version) ───►───   │
  │                                                                     │
  │  ◄───◄─── ServerHello (Selected Cipher) ──────────────────────────  │
  │  ◄───◄─── Certificate Payload (Public Key Exchange) ──────────────  │
  │                                                                     │
  │  ───►───►─── ClientKeyExchange (Pre-Master Secret encrypted) ───►─  │
  │                                                                     │
  │  ───►───►─── [ChangeCipherSpec] & Finished (Encrypted validation) ─ │
  │                                                                     │
  │  ◄───◄─── [ChangeCipherSpec] & Finished (Encrypted validation) ─    │
  │                                                                     │
  └────────────────── Encrypted Application Layer Data ─────────────────┘
```

*   **Cipher Suite Orchestration:** The `SSLContext` manages the dynamic negotiation of cipher profiles.
If a client attempts to connect using legacy, broken cryptographic suites (e.g., weak RC4 or unpadded MD5 hashes), the server runtime drops the pipeline instantly before text payloads can exchange.
*   **The Trust Management Paradigm:** Since the local embedded server utilizes a self-signed local certificate ecosystem generated directly inside the application workspace,
native web browsers connecting over the Wi-Fi framework will trigger a security alert layout (`ERR_CERT_AUTHORITY_INVALID`).
Developers must explicitly configure their connection matrix clients to trust the custom local certificate authority chain or match the certificate signature directly.

---

## 🏆 Module Progress Map
- **Challenge 1: Minimal Stateless HTTP Info Host (Easy - Single Text Page Server)**
- **Challenge 2: Dynamic REST Router & POST Payload Parser (Advanced - Remote View Controller)**
- **Challenge 3: Cryptographic Self-Signed Embedded HTTPS Server Engine (Master - Secure Local Host)**
