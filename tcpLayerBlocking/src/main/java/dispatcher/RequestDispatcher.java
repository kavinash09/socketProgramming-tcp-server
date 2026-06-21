package dispatcher;

import connectionContext.ClientSessionConnectionContext;
import requests.Request;

public interface RequestDispatcher {
    public void dispatch(Request request, ClientSessionConnectionContext connectionContext);
}
