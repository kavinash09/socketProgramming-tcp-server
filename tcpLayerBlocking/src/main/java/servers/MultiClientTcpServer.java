//package servers;
//
//import connectionHandler.BlockingConnectionHandler;
//import processor.RequestProcessor;
//
//import java.io.IOException;
//import java.net.ServerSocket;
//import java.net.Socket;
//
//public class MultiClientTcpServer implements TcpServer{
//    private final int port;
//    private final RequestProcessor processor;
//
//    public MultiClientTcpServer(int port, RequestProcessor processor) {
//        this.port = port;
//        this.processor = processor;
//
//    }
//
//    @Override
//    public void start() throws IOException {
//
//        ServerSocket serverSocket = new ServerSocket(port);
//        while (true) {
//            System.out.println("Waiting for client to arrive");
//            Socket socket = serverSocket.accept();
//            System.out.println("Accepted the client request");
//            BlockingConnectionHandler handler = new BlockingConnectionHandler(socket, processor);
//            System.out.println("Handler created for the client");
//            Thread worker = new Thread(
//                    () -> {
//                        try {
//                            System.out.println("Going to sleep : "+ Thread.currentThread());
//                            Thread.sleep(1000 * 5);
//                            System.out.println("wake up from sleep"+ Thread.currentThread());
//                            handler.handle();
//                        } catch (IOException | InterruptedException e) {
//                            throw new RuntimeException(e);
//                        }
//                    }
//            );
//            worker.start();
//        }
//
//    }
//}
