package com.avinash.socket.application.router;

import com.avinash.socket.application.handler.RequestHandler;
import com.avinash.socket.contracts.processing.RequestProcessor;
import com.avinash.socket.contracts.request.Request;
import com.avinash.socket.contracts.response.Response;

import java.util.List;
import java.util.Objects;

public final class RequestRouter implements RequestProcessor {
    private final List<RequestHandler> handlers;

    public RequestRouter(List<RequestHandler> handlers) {
        this.handlers = List.copyOf(Objects.requireNonNull(handlers, "handlers"));
    }

    public Response process(Request request) {
        Objects.requireNonNull(request, "request");
        for (RequestHandler handler: handlers) {
            if (handler.supports(request)) {
                return handler.handle(request);
            }
        }
        throw new IllegalArgumentException("No handlers registered for request type: "+ request.getClass().getName());
    }


}
