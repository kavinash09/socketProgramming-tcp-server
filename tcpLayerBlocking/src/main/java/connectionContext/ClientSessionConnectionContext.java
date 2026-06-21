package connectionContext;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientSessionConnectionContext {
    private final StringBuilder incomingBuffer = new StringBuilder();
    private final Queue<ByteBuffer> outgoingQueue = new ConcurrentLinkedQueue<>();
    private SelectionKey selectionKey;
    public void setSelectionKey(SelectionKey key) {
        this.selectionKey = key;
    }
    public SelectionKey getSelectionKey() {
        return this.selectionKey;
    }
    public boolean hasCompleteRequest() {
        return incomingBuffer.indexOf("\n") >= 0;
    }



    public void append(String data) {
        incomingBuffer.append(data);
    }
    public String extractRequest() {
        int end = incomingBuffer.indexOf("\n");
        if (end < 0) {
            throw new IllegalStateException("No complete request is available");
        }
        String request = incomingBuffer.substring(0, end);
        incomingBuffer.delete(0, end+1);
        return request;
    }
    public Queue<ByteBuffer> getOutgoingQueue() {
        return this.outgoingQueue;
    }

    public void enqueResponse(ByteBuffer byteBuffer) {
        this.outgoingQueue.add(byteBuffer);
    }
}
