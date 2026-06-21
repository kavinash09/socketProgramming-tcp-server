//package dispatcher;
//
//import com.avinash.socket.contracts.processing.RequestProcessor;
//import com.avinash.socket.contracts.request.Request;
//import com.avinash.socket.contracts.response.Response;
//import connectionContext.ClientSessionConnectionContext;
//import encoderDecoder.ResponseEncoder;
//
//
//import taskqueue.SelectorTaskQueue;
//
//import java.nio.ByteBuffer;
//import java.nio.channels.SelectionKey;
//import java.nio.channels.Selector;
//import java.util.concurrent.ExecutorService;
//
//public class WorkerPoolDispatcher implements RequestDispatcher{
//    private final ExecutorService threadPool;
//    private final RequestProcessor processor;
//    private final ResponseEncoder encoder;
//    private final Selector selector;
//    SelectorTaskQueue selectorTaskQueue;
//
//
//    public WorkerPoolDispatcher(ExecutorService threadPool, RequestProcessor processor, ResponseEncoder encoder, Selector selector, SelectorTaskQueue selectorTaskQueue) {
//        this.threadPool = threadPool;
//        this.processor = processor;
//        this.encoder = encoder;
//        this.selector = selector;
//        this.selectorTaskQueue = selectorTaskQueue;
//    }
//
//    @Override
//    public void dispatch(Request request, ClientSessionConnectionContext connectionContext) {
//        threadPool.submit(() -> {
//            Response response = processor.process(request);
//            ByteBuffer byteBuffer = encoder.encode(response);
//            connectionContext.enqueResponse(byteBuffer);
//
//            selectorTaskQueue.submit(()->{
//                SelectionKey key = connectionContext.getSelectionKey();
//                key.interestOps(key.interestOps()| SelectionKey.OP_WRITE);
//            });
//            selector.wakeup();
//        });
//    }
//}
