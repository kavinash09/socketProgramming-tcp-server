package com.avinash.socket.application.handler;

import com.avinash.socket.application.command.TextCommand;
import com.avinash.socket.application.response.SuccessResponse;
import com.avinash.socket.contracts.request.Request;
import com.avinash.socket.contracts.response.Response;

public class TextRequestHandler implements RequestHandler{
    @Override
    public boolean supports(Request request) {
        return request instanceof TextCommand;
    }

    @Override
    public Response handle(Request request) {
        TextCommand command = (TextCommand) request;
        return new SuccessResponse("Echo: "+ command.message());
    }
}
