package com.avinash.socket.application.handler;

import com.avinash.socket.contracts.request.Request;
import com.avinash.socket.contracts.response.Response;

public interface RequestHandler {
    boolean supports(Request request);
    Response handle(Request request);
}
