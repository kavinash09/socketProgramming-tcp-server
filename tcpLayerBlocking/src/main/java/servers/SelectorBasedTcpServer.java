//package servers;
//
//import connectionContext.ClientSessionConnectionContext;
//import protocol.RequestDecoder;
//import processor.RequestProcessor;
//import requests.TextRequest;
//import responses.Response;
//import taskqueue.SelectorTaskQueue;
//
//import java.io.IOException;
//import java.net.InetSocketAddress;
//import java.nio.ByteBuffer;
//import java.nio.channels.SelectionKey;
//import java.nio.channels.Selector;
//import java.nio.channels.ServerSocketChannel;
//import java.nio.channels.SocketChannel;
//import java.nio.charset.StandardCharsets;
//import java.util.Iterator;
//import java.util.Queue;
//import java.util.Set;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//public class SelectorBasedTcpServer implements TcpServer{
//    private final int port;
//    private final RequestDecoder requestDecoder;
//    private final RequestProcessor processor;
//    private final ExecutorService workerPool;
//    private final SelectorTaskQueue selectorTaskQueue;
//
//    public SelectorBasedTcpServer(int port, RequestDecoder requestDecoder, RequestProcessor processor, SelectorTaskQueue selectorTaskQueue) {
//
//        this.port = port;
//        this.requestDecoder = requestDecoder;
//        this.processor = processor;
//        this.workerPool = Executors.newFixedThreadPool(10);
//        this.selectorTaskQueue = selectorTaskQueue;
//    }
//
//    @Override
//    public void start() throws IOException {
//        Selector selector = Selector.open();
//
//        ServerSocketChannel serverChannel = ServerSocketChannel.open();
//        serverChannel.bind(new InetSocketAddress(port));
//        serverChannel.configureBlocking(false);
//        serverChannel.register(
//                selector, SelectionKey.OP_ACCEPT
//        );
//
//        while (true) {
//            selector.select();
//            selectorTaskQueue.runPendingTask();
//            processSelectedKey(selector);
//        }
//
//    }
//
//    private void processSelectedKey(Selector selector) throws IOException {
//        Set<SelectionKey> keys = selector.selectedKeys();
//        Iterator<SelectionKey> iterator = keys.iterator();
//
//        while (iterator.hasNext()) {
//            SelectionKey key = iterator.next();
//            iterator.remove();
//
//            if (key.isAcceptable()) {
//                handleAccept(selector, key);
//            }
//            if (key.isReadable()) {
//                handleRead(key);
//            }
//            if (key.isWritable()) {
//                handleWrite(key);
//            }
//        }
//    }
//
//    private void handleWrite(SelectionKey key) throws IOException {
//        SocketChannel channel = (SocketChannel) key.channel();
//        ClientSessionConnectionContext connectionContext = (ClientSessionConnectionContext) key.attachment();
//        Queue<ByteBuffer> byteBufferQueue = connectionContext.getOutgoingQueue();
//        while (!byteBufferQueue.isEmpty()) {
//            ByteBuffer buffer = byteBufferQueue.peek();
//            channel.write(buffer);
//            if (buffer.hasRemaining()) {
//                return;
//            }
//            byteBufferQueue.poll();
//        }
//        key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
//    }
//
//    private void handleRead(SelectionKey key) throws IOException {
//        SocketChannel client = (SocketChannel) key.channel();
//        ClientSessionConnectionContext connectionContext = (ClientSessionConnectionContext) key.attachment();
//        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
//        int bytesRead = client.read(byteBuffer);
//        if (bytesRead == -1) {
//            client.close();
//            return;
//        }
//        byteBuffer.flip();
//        String data = StandardCharsets.UTF_8.decode(byteBuffer).toString();
//        connectionContext.append(data);
//        if (connectionContext.hasCompleteRequest()) {
////            Request request =
////            String request = connectionContext.extractRequest();
////            submitToWorker(request, client);
//
//        }
//    }
//
//    private void submitToWorker(String request, SocketChannel client) {
//        workerPool.submit(()-> {
//            Response response = processor.process(new TextRequest(request));
//            try {
//                writeToResponse(response, client);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        });
//    }
//
//    private void writeToResponse(Response response, SocketChannel client) throws IOException {
//        ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(response.getPayload()+"\n");
//        client.write(byteBuffer);
//    }
//
//    private void handleAccept(Selector selector, SelectionKey key) throws IOException {
//        ServerSocketChannel server = (ServerSocketChannel) key.channel();
//        SocketChannel client = server.accept();
//        client.configureBlocking(false);
//        ClientSessionConnectionContext connectionContext = new ClientSessionConnectionContext();
//        SelectionKey clientKey = client.register(selector, SelectionKey.OP_READ);
//        connectionContext.setSelectionKey(clientKey);
//        clientKey.attach(connectionContext);
//        System.out.println("Client Connected to register for watching data and attached a connection context to it");
//    }
//}
