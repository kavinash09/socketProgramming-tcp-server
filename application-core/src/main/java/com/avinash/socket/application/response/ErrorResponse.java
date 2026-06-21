package com.avinash.socket.application.response;

import com.avinash.socket.contracts.response.Response;

import java.util.Objects;

public class ErrorResponse implements Response {
    private final String code;
    private final String message;
    public ErrorResponse(String code, String message) {
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
