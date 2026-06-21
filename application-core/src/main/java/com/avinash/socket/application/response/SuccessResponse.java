package com.avinash.socket.application.response;

import com.avinash.socket.contracts.response.Response;

import java.util.Objects;

public class SuccessResponse implements Response {
    private final String message;
    public SuccessResponse(String message) {
        this.message = Objects.requireNonNull(message, "message");
    }
    public String message() {
        return message;
    }
}
