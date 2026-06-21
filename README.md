# Selector-Based TCP Server

A modular Java TCP server built with Java NIO `Selector`, a line-based protocol, and a worker pool for application processing.

The design separates:

* TCP connection lifecycle
* protocol framing and encoding
* application request handling
* worker-thread execution
* runtime wiring

---

## Architecture at a glance

```text
Client Socket
    |
    v
transport-tcp-selector
    |
    | reads bytes
    v
protocol-line
    |
    | decodes complete requests
    v
server-execution
    |
    | schedules work on worker pool
    v
application-core
    |
    | processes request and returns response
    v
server-execution
    |
    | invokes response callback
    v
transport-tcp-selector
    |
    | queues encoded bytes and writes to socket
    v
Client Socket
```

---

## Module structure

```text
socket-programming/
├── server-contracts/
├── protocol-spi/
├── protocol-line/
├── application-core/
├── server-execution/
├── transport-tcp-selector/
└── server-runtime/
```

---

## Module responsibilities

### `server-contracts`

Contains shared contracts only.

```text
Request
Response
RequestProcessor
RequestDispatcher
ResponseCallback
```

This module must not know about:

```text
TCP
Selector
ByteBuffer
worker pools
protocol syntax
business handlers
```

---

### `protocol-spi`

Defines the protocol boundary.

```text
ProtocolSession
ProtocolSessionFactory
```

Its responsibility is:

```text
bytes <-> Request / Response
```

It does not know about:

```text
SocketChannel
Selector
worker threads
business handlers
```

---

### `protocol-line`

Implements the current line-based protocol.

Examples:

```text
PING
TEXT|hello
STUDENT_CREATE|Avinash|25
```

Each request ends with a newline:

```text
PING\n
TEXT|hello\n
```

The line protocol session is responsible for:

```text
incoming bytes
    ->
partial-line accumulation
    ->
split at \n
    ->
List<Request>
```

One socket read can produce:

```text
zero requests
one request
many requests
```

Example:

```text
PING\nTEXT|hello\n
```

becomes:

```text
PingCommand
TextCommand("hello")
```

---

### `application-core`

Contains pure application behavior.

```text
RequestRouter
RequestHandler
PingHandler
TextHandler
CreateStudentHandler
services
commands
responses
```

Its responsibility is:

```text
Request -> business processing -> Response
```

It must not know about:

```text
SocketChannel
Selector
ByteBuffer
ExecutorService
worker pool
protocol line syntax
```

---

### `server-execution`

Contains execution policy.

```text
WorkerPoolRequestDispatcher
```

Its responsibility is:

```text
Request
    ->
submit to worker pool
    ->
RequestProcessor.process(request)
    ->
ResponseCallback.onResponse(response)
```

It knows only contracts:

```text
RequestProcessor
RequestDispatcher
ResponseCallback
Request
Response
```

It must not import:

```text
application.router.RequestRouter
SocketChannel
Selector
ProtocolSession
```

At runtime, `RequestRouter` is passed into it as a `RequestProcessor`.

---

### `transport-tcp-selector`

Contains TCP/NIO transport behavior.

```text
SelectorBasedTcpServer
ConnectionContext
SelectorTaskQueue
```

It owns:

```text
Selector
ServerSocketChannel
SocketChannel
SelectionKey
OP_ACCEPT
OP_READ
OP_WRITE
ConnectionContext
outbound ByteBuffer queue
```

It must not contain business rules.

---

### `server-runtime`

The composition root.

This is the only module allowed to know all concrete implementations.

It creates and wires:

```text
handlers
RequestRouter
ExecutorService
WorkerPoolRequestDispatcher
LineProtocolSessionFactory
SelectorBasedTcpServer
```

---

## Dependency direction

```text
server-contracts
    <- application-core
    <- server-execution
    <- protocol-spi

protocol-spi
    <- protocol-line
    <- transport-tcp-selector

server-execution
    <- server-runtime

application-core
    <- server-runtime

protocol-line
    <- server-runtime

transport-tcp-selector
    <- server-runtime
```

Important rule:

```text
server-execution must depend on server-contracts only.
```

It must not directly depend on `application-core`.

---

## Core contracts

```text
RequestProcessor
    Request -> Response

RequestDispatcher
    Request + ResponseCallback -> schedules execution

ResponseCallback
    Response -> returns response to transport
```

The important split is:

```text
application-core:
    what the request means

server-execution:
    which thread processes it

transport:
    how bytes enter and leave the socket
```

---

## ConnectionContext

Each accepted client gets one `ConnectionContext`.

```text
Client A SocketChannel
    ->
ConnectionContext A

Client B SocketChannel
    ->
ConnectionContext B
```

It contains per-connection state:

```text
ConnectionContext
├── ProtocolSession
├── outbound ByteBuffer queue
└── SelectionKey
```

### Why ProtocolSession is inside ConnectionContext

TCP can split one request across multiple reads.

Example:

```text
First read:
TEXT|hel

Second read:
lo\n
```

The same connection's protocol session remembers:

```text
TEXT|hel
```

and combines it with:

```text
lo\n
```

to create:

```text
TextCommand("hello")
```

Every client needs a separate protocol session so partial data from different clients never mixes.

---

## Thread ownership

### Selector thread owns

```text
accepting clients
registering channels
reading SocketChannel bytes
calling ProtocolSession
ConnectionContext mutation
outbound queue mutation
SelectionKey.interestOps
writing SocketChannel bytes
closing connections
```

### Worker thread owns

```text
RequestProcessor.process(request)
business handlers
services
database calls
external API calls
creating Response
```

### Worker threads must not directly touch

```text
SocketChannel
SelectionKey
ConnectionContext
ProtocolSession
outbound ByteBuffer queue
```

---

## Full request-response flow

```text
1. Client connects

2. Selector accepts SocketChannel

3. Server creates:
   - ProtocolSession
   - ConnectionContext

4. Server registers client channel with OP_READ

5. Client sends:
   TEXT|hello\n

6. Selector receives OP_READ

7. Selector reads bytes from SocketChannel

8. Selector passes bytes to:
   context.protocolSession().onBytesReceived(...)

9. Protocol session returns:
   TextCommand("hello")

10. Selector calls:
    requestDispatcher.dispatch(request, callback)

11. WorkerPoolRequestDispatcher submits work to ExecutorService

12. Worker thread calls:
    requestProcessor.process(request)

13. RequestRouter finds TextHandler

14. TextHandler returns:
    SuccessResponse("Echo: hello")

15. Worker calls:
    responseCallback.onResponse(response)

16. Callback submits selector task

17. SelectorTaskQueue wakes selector

18. Selector task:
    - encodes response using ProtocolSession
    - calls context.enqueueOutbound(encodedResponse)
    - enables OP_WRITE

19. Selector receives OP_WRITE

20. handleWrite drains outbound queue

21. SocketChannel.write(...) sends response to client
```

---

## Why SelectorTaskQueue exists

A worker thread receives a `Response`, but it must not directly do this:

```text
SocketChannel.write(...)
SelectionKey.interestOps(...)
ConnectionContext.enqueueOutbound(...)
```

Those objects belong to the selector thread.

Instead:

```text
worker thread
    ->
SelectorTaskQueue.submit(task)
    ->
selector.wakeup()
    ->
selector thread runs task
```

The selector task safely:

```text
encodes response
queues ByteBuffer
enables OP_WRITE
```

---

## Outbound queue and partial writes

A socket write may write only part of a response.

Example response:

```text
OK|Echo: hello\n
```

The socket may accept only:

```text
OK|Echo:
```

The `ByteBuffer` remains in the outbound queue with its position advanced.

On the next `OP_WRITE` event:

```text
SocketChannel.write(buffer)
```

continues from the remaining bytes.

When the buffer is fully written:

```text
context.removeCurrentOutboundBuffer()
```

When the queue becomes empty:

```text
remove OP_WRITE
```

This is important because leaving `OP_WRITE` enabled causes the selector to wake repeatedly and consume CPU.

---

## Worked example: two clients

### Client One sends

```text
TEXT|hel
```

No newline exists yet.

```text
LineProtocolSession returns:
[]
```

The protocol session remembers:

```text
TEXT|hel
```

### Client Two sends

```text
PING\n
```

Client Two has a different `ConnectionContext` and a different `ProtocolSession`.

It returns:

```text
[PingCommand]
```

Worker processing returns:

```text
SuccessResponse("PONG")
```

Client Two receives:

```text
OK|PONG
```

### Client One sends later

```text
lo\n
```

Client One's protocol session combines both reads:

```text
TEXT|hel + lo\n
```

It returns:

```text
[TextCommand("hello")]
```

Client One receives:

```text
OK|Echo: hello
```

No bytes, partial messages, outbound buffers, or responses are shared between the two clients.

---

## Current protocol examples

```text
PING
```

Response:

```text
OK|PONG
```

```text
TEXT|hello
```

Response:

```text
OK|Echo: hello
```

```text
STUDENT_CREATE|Avinash|25
```

Response:

```text
OK|Student created: Avinash, age=25
```

Every request must end with a newline.

---

## Current limitations

This is a correct learning implementation, but not yet a complete production server.

Not implemented yet:

```text
protocol error responses
worker-pool rejection handling
worker exception handling
request ordering per connection
backpressure and outbound queue limits
maximum request size
idle connection timeout
graceful shutdown
structured logging
metrics
authentication and authorization
TLS
```

The next important design concern is request ordering.

If one client sends multiple requests quickly, a general worker pool can complete them out of order:

```text
Request 1 starts first but is slow
Request 2 starts second but is fast
Response 2 may return before Response 1
```

Whether that is allowed depends on the protocol contract.
