package com.avinash.socket.transport.tcp.selector;

import com.avinash.socket.protocol.spi.ProtocolSession;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.ArrayDeque;
import java.util.Deque;

public class ConnectionContext {
    private final ProtocolSession protocolSession;
    private final Deque<ByteBuffer> outboundBuffers = new ArrayDeque<>();
    private SelectionKey selectionKey;
    public ConnectionContext(ProtocolSession protocolSession) {
        this.protocolSession = protocolSession;
    }
    public ProtocolSession protocolSession() {
        return this.protocolSession;
    }
    public SelectionKey selectionKey() {
        if (selectionKey == null) {
            throw new IllegalStateException("SelectionKey has not been assigned");
        }
        return selectionKey;
    }
    public void assignSelectionKey(SelectionKey selectionKey) {
        if (this.selectionKey != null) {
            throw new IllegalStateException("SelectionKey already assigned");
        }
        this.selectionKey = selectionKey;
    }
    public void enqueueOutbound(ByteBuffer buffer) {
        outboundBuffers.addLast(buffer);
    }
    public ByteBuffer currentOutboundBuffer() {
        return outboundBuffers.peekFirst();
    }
    public void removeCurrentOutboundBuffer() {
        if (outboundBuffers.pollFirst() == null) {
            throw new IllegalStateException("No outbound buffer exists");
        }

    }
    public boolean hasOutboundData() {
        return !outboundBuffers.isEmpty();
    }
}
