package com.avinash.socket.protocol.spi;

import com.avinash.socket.contracts.request.Request;
import com.avinash.socket.contracts.response.Response;

import java.nio.ByteBuffer;
import java.util.List;

public interface ProtocolSession {
    List<Request> onBytesReceived(ByteBuffer incomingBytes);
    ByteBuffer encode(Response response);
}
