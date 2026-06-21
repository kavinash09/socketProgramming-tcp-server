package com.avinash.socket.transport.tcp.selector;

import com.avinash.socket.contracts.dispatch.RequestDispatcher;
import com.avinash.socket.contracts.request.Request;
import com.avinash.socket.contracts.response.Response;
import com.avinash.socket.protocol.spi.ProtocolSessionFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class SelectorBasedTcpServer {
    private static final int READ_BUFFER_SIZE = 4 * 1024;
    private final int port;
    private final ProtocolSessionFactory protocolSessionFactory;
    private final RequestDispatcher requestDispatcher;
    private volatile boolean running;
    private SelectorTaskQueue selectorTaskQueue;

    public SelectorBasedTcpServer(int port, ProtocolSessionFactory protocolSessionFactory, RequestDispatcher requestDispatcher) {
        this.port = port;
        this.protocolSessionFactory = protocolSessionFactory;
        this.requestDispatcher = requestDispatcher;

    }


    public void start() {
        running = true;
        try (Selector selector = Selector.open();
             ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();){
            selectorTaskQueue = new SelectorTaskQueue(selector);
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(port));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("Selector TCP server listening on port :"+ port);
            while (running) {
                selectorTaskQueue.runPendingTask();
                selector.select();

                selectorTaskQueue.runPendingTask();
                processSelectedKeys(selector, selectorTaskQueue);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void stop() {
        running = false;
    }

    private void processSelectedKeys(Selector selector, SelectorTaskQueue selectorTaskQueue) {
        Set<SelectionKey> selectedKeys = selector.selectedKeys();
        Iterator<SelectionKey> iterator = selectedKeys.iterator();

        while (iterator.hasNext()) {
            SelectionKey key = iterator.next();
            iterator.remove();
            if(!key.isValid()) {
                continue;
            }

            try {
                // isAcceptable
                if (key.isAcceptable()) {
                    handleAccept(key, selector);
                }
                // isReadable
                if (key.isValid() && key.isReadable()) {
                    handleRead(key, selectorTaskQueue);
                }
                // isWritable
                if(key.isValid() && key.isWritable()) {
                    handleWrite(key, selectorTaskQueue);
                }
            } catch (CancelledKeyException ignored) {

            } catch (IOException exception) {
                closeConnection(key);
            }
        }
    }

    private void handleAccept(SelectionKey key, Selector selector) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();

        SocketChannel clientChannel = serverChannel.accept();

        if (clientChannel == null) {
            return;
        }
        clientChannel.configureBlocking(false);
        ConnectionContext context = new ConnectionContext(protocolSessionFactory.create());
        SelectionKey clientKey = clientChannel.register(selector, SelectionKey.OP_READ, context);
        context.assignSelectionKey(clientKey);

        System.out.println("Client connected: "+ clientChannel.getRemoteAddress());

    }

    private void closeConnection(SelectionKey key) {
        try {
            key.cancel();
            key.channel().close();
        } catch (IOException ignored) {

        }
    }

    private void handleWrite(SelectionKey key, SelectorTaskQueue selectorTaskQueue) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ConnectionContext context = (ConnectionContext) key.attachment();

        while (context.hasOutboundData()) {
            ByteBuffer currentBuffer = context.currentOutboundBuffer();
            clientChannel.write(currentBuffer);

            if (currentBuffer.hasRemaining()) {
                return;
            }
            context.removeCurrentOutboundBuffer();
        }
        disableWriteInterest(key);
    }

    private void disableWriteInterest(SelectionKey key) {
        key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
    }

    private void handleRead(SelectionKey key, SelectorTaskQueue selectorTaskQueue) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ConnectionContext context = (ConnectionContext) key.attachment();
        ByteBuffer readBuffer = ByteBuffer.allocate(1024);
        int bytesRead = clientChannel.read(readBuffer);
        if (bytesRead == -1) {
            closeConnection(key);
            return;
        }
        if (bytesRead == 0) {
            return;
        }
        readBuffer.flip();

        List<Request> requests = context.protocolSession().onBytesReceived(readBuffer);

        for (Request request: requests) {
            requestDispatcher.dispatch(request, response -> submitResponseToSelector(selectorTaskQueue, context, response));
        }
    }

    private void submitResponseToSelector(SelectorTaskQueue selectorTaskQueue, ConnectionContext context, Response response) {
        selectorTaskQueue.submit(()-> {
            if (!context.selectionKey().isValid()) {
                return;
            }
            ByteBuffer encodedResponse = context.protocolSession().encode(response);
            context.enqueueOutbound(encodedResponse);
            enableWriteInterest(context.selectionKey());
        });
    }

    private void enableWriteInterest(SelectionKey selectionKey) {
        selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
    }

}
