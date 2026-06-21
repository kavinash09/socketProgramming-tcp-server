package com.avinash.socket.protocol.line;


import com.avinash.socket.contracts.request.Request;

import java.util.Objects;

public class InvalidProtocolRequest implements Request {
    private final String code;
    private final String message;

    public InvalidProtocolRequest(String code, String message) {
        this.code = Objects.requireNonNull(code, "code");
        this.message = Objects.requireNonNull(message, "message");
    }
    public String code() {
        return code;
    }
    public String message() {
        return message;
    }
}
