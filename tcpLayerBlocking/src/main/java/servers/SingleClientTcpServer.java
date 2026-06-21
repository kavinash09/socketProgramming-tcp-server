//package servers;
//
//import connectionHandler.BlockingConnectionHandler;
//import processor.RequestProcessor;
//
//import java.io.IOException;
//import java.net.ServerSocket;
//import java.net.Socket;
//
//public class SingleClientTcpServer implements TcpServer{
//    private final int port;
//    private final RequestProcessor processor;
//
//    public SingleClientTcpServer(int port, RequestProcessor processor) {
//        this.port = port;
//        this.processor = processor;
//    }
//
//    @Override
//    public void start() throws IOException {
//        ServerSocket serverSocket = new ServerSocket(port);
//        while (true) {
//            System.out.println("Inside loop waiting for connection...");
//            Socket socket = serverSocket.accept();
//            System.out.println("Received a connection from socket: "+ socket.getInetAddress() + "  "+ socket.getLocalAddress());
//            BlockingConnectionHandler handler = new BlockingConnectionHandler(socket, processor);
//            System.out.println("Created handler for the socket to process");
//            handler.handle();
//            System.out.println("Processed request successfully");
//            socket.close();
//        }
//
//    }
//}
