package encoderDecoder;

import requests.Request;

public interface RequestDecoder {
    Request decode(String data);
}
