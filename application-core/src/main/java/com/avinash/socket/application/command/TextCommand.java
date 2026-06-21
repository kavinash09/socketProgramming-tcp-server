package com.avinash.socket.application.command;


import com.avinash.socket.contracts.request.Request;

public final class TextCommand implements Request {
    private final String message;
    public TextCommand(String msg) {
        this.message = msg;
    }
    public String message() {
        return message;
    }
}
