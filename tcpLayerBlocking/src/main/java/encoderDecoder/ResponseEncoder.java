package encoderDecoder;



import com.avinash.socket.contracts.response.Response;

import java.nio.ByteBuffer;

public interface ResponseEncoder {

    ByteBuffer encode(Response response);

}
