package com.avinash.socket.application.handler;

import com.avinash.socket.application.command.CreateStudentCommand;
import com.avinash.socket.application.response.SuccessResponse;
import com.avinash.socket.contracts.request.Request;
import com.avinash.socket.contracts.response.Response;

public class CreateStudentRequestHandler implements RequestHandler{
    @Override
    public boolean supports(Request request) {
        return request instanceof CreateStudentCommand;
    }

    @Override
    public Response handle(Request request) {
        CreateStudentCommand command = (CreateStudentCommand) request;
        return new SuccessResponse("Student Created: " + command.name() + ", age=" + command.age());
    }
}
