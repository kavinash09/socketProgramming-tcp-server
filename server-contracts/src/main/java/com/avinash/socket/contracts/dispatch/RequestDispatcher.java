package com.avinash.socket.contracts.dispatch;

import com.avinash.socket.contracts.request.Request;

public interface RequestDispatcher {
    void dispatch(Request request, ResponseCallback responseCallback);
}
