package com.avinash.socket.application.handler;

import com.avinash.socket.application.command.PingCommand;
import com.avinash.socket.application.response.SuccessResponse;
import com.avinash.socket.contracts.request.Request;
import com.avinash.socket.contracts.response.Response;

public class PingRequestHandler implements RequestHandler{
    @Override
    public boolean supports(Request request) {
        return request instanceof PingCommand;
    }

    @Override
    public Response handle(Request request) {
        return new SuccessResponse("PONG");
    }
}
