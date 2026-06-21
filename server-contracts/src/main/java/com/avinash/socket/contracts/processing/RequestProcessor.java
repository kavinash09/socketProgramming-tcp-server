package com.avinash.socket.contracts.processing;

import com.avinash.socket.contracts.request.Request;
import com.avinash.socket.contracts.response.Response;

public interface RequestProcessor {
    Response process(Request request);
}
