package encoderDecoder;


import com.avinash.socket.contracts.response.Response;
import responses.TextResponse;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class TextResponseEncoder implements ResponseEncoder {
    @Override
    public ByteBuffer encode(Response response) {
        TextResponse textResponse = (TextResponse) response;
        return StandardCharsets.UTF_8.encode(textResponse.getPayload()+"\n");
    }
}
