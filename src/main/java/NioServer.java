import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

public class NioServer {
    public void start(final int portNumber) throws IOException {
        try ( var serverSocketChannel = ServerSocketChannel.open();
                var selector = Selector.open()) {
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(portNumber));
            serverSocketChannel.register(selector, SelectionKey.OP)
        }
    }
}
