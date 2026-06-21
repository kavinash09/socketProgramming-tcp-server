package com.avinash.socket.execution;


import com.avinash.socket.contracts.dispatch.RequestDispatcher;
import com.avinash.socket.contracts.dispatch.ResponseCallback;
import com.avinash.socket.contracts.processing.RequestProcessor;
import com.avinash.socket.contracts.request.Request;
import com.avinash.socket.contracts.response.Response;

import java.util.concurrent.ExecutorService;

public class WorkerPoolRequestDispatcher implements RequestDispatcher {
    private final ExecutorService workerPool;
    private final RequestProcessor requestProcessor;

    public WorkerPoolRequestDispatcher(ExecutorService workerPool, RequestProcessor requestProcessor) {
        this.workerPool = workerPool;
        this.requestProcessor = requestProcessor;
    }

    @Override
    public void dispatch(Request request, ResponseCallback responseCallback) {
        workerPool.submit(() -> {
            Response response = requestProcessor.process(request);
            responseCallback.onResponse(response);
        });
    }
}
