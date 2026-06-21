package encoderDecoder;

import requests.Request;
import requests.TextRequest;

public class TextRequestDecoder implements RequestDecoder {
    @Override
    public Request decode(String data) {
        return new TextRequest(data);
    }
}
