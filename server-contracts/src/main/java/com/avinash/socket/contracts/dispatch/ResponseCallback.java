package com.avinash.socket.contracts.dispatch;

import com.avinash.socket.contracts.response.Response;

public interface ResponseCallback {
    void onResponse(Response response);
}
