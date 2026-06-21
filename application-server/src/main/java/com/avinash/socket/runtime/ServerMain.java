package com.avinash.socket.runtime;

import com.avinash.socket.application.handler.CreateStudentRequestHandler;
import com.avinash.socket.application.handler.PingRequestHandler;
import com.avinash.socket.application.handler.RequestHandler;
import com.avinash.socket.application.handler.TextRequestHandler;
import com.avinash.socket.application.router.RequestRouter;
import com.avinash.socket.contracts.dispatch.RequestDispatcher;

import com.avinash.socket.contracts.processing.RequestProcessor;

import com.avinash.socket.execution.WorkerPoolRequestDispatcher;
import com.avinash.socket.protocol.line.LineProtocolSessionFactory;
import com.avinash.socket.protocol.spi.ProtocolSessionFactory;
import com.avinash.socket.transport.tcp.selector.SelectorBasedTcpServer;



import javax.sound.sampled.Line;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerMain {
    private static final int PORT = 9090;
    private static final int WORKER_THREAD_COUNT = 8;
    public static void main(String[] args) {
        startServer();
    }

    private static void startServer() {
            List<RequestHandler> handlers = List.of(
                                                    new PingRequestHandler(),
                                                    new TextRequestHandler(),
                                                    new CreateStudentRequestHandler()
                                                );

            RequestProcessor requestProcessor = new RequestRouter(handlers);
            ExecutorService workerPool = Executors.newFixedThreadPool(WORKER_THREAD_COUNT);
            RequestDispatcher requestDispatcher = new WorkerPoolRequestDispatcher(workerPool, requestProcessor);

        ProtocolSessionFactory protocolSessionFactory = new LineProtocolSessionFactory();

        SelectorBasedTcpServer server = new SelectorBasedTcpServer(PORT, protocolSessionFactory, requestDispatcher);
        try {
            server.start();
        } finally {
            workerPool.shutdown();
        }
    }
}
