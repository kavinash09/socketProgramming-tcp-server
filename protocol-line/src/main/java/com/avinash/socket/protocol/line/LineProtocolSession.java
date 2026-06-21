package com.avinash.socket.protocol.line;

import com.avinash.socket.application.command.CreateStudentCommand;
import com.avinash.socket.application.command.PingCommand;
import com.avinash.socket.application.command.TextCommand;
import com.avinash.socket.application.response.ErrorResponse;
import com.avinash.socket.application.response.SuccessResponse;
import com.avinash.socket.contracts.request.Request;
import com.avinash.socket.contracts.response.Response;
import com.avinash.socket.protocol.spi.ProtocolSession;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class LineProtocolSession implements ProtocolSession {

    private final StringBuilder unreadText = new StringBuilder();



    @Override
    public List<Request> onBytesReceived(ByteBuffer incomingBytes) {
        String recievedText = StandardCharsets.UTF_8.decode(incomingBytes).toString();
        unreadText.append(recievedText);

        List<Request> requests = new ArrayList<>();
        int newLineIndex ;
        while ((newLineIndex = unreadText.indexOf("\n")) >= 0 ) {
            String line = unreadText.substring(0, newLineIndex);
            unreadText.delete(0, newLineIndex+1);
            requests.add(parseLine(line));
        }

        return requests;
    }

    private Request parseLine(String line) {
        String[] fields = line.split("\\|", -1);
        String command = fields[0].trim();
        return switch (command) {
            case "PING" -> parsePing(fields);
            case "TEXT" -> parseText(fields);
            case "STUDENT_CREATE" -> parseStudentCreate(fields);
            default -> new InvalidProtocolRequest(
                    "UNKNOWN_COMMAND",
                    "Unsupported command: " + command
            );
        };
    }

    private Request parsePing(String[] fields) {
        if (fields.length != 1) {
            return new InvalidProtocolRequest(
                    "INVALID_REQUEST",
                    "PING does not accept fields"
            );
        }

        return new PingCommand();
    }

    private Request parseText(String[] fields) {
        if (fields.length != 2 || fields[1].isBlank()) {
            return new InvalidProtocolRequest(
                    "INVALID_REQUEST",
                    "TEXT requires one non-empty message"
            );
        }

        return new TextCommand(fields[1]);
    }

    private Request parseStudentCreate(String[] fields) {
        if (fields.length != 3) {
            return new InvalidProtocolRequest(
                    "INVALID_REQUEST",
                    "STUDENT_CREATE requires name and age"
            );
        }

        String name = fields[1].trim();

        if (name.isBlank()) {
            return new InvalidProtocolRequest(
                    "INVALID_REQUEST",
                    "Student name cannot be blank"
            );
        }

        try {
            int age = Integer.parseInt(fields[2].trim());

            if (age < 0) {
                return new InvalidProtocolRequest(
                        "INVALID_REQUEST",
                        "Age cannot be negative"
                );
            }

            return new CreateStudentCommand(name, age);

        } catch (NumberFormatException exception) {
            return new InvalidProtocolRequest(
                    "INVALID_REQUEST",
                    "Age must be a number"
            );
        }
    }


    @Override
    public ByteBuffer encode(Response response) {
        String line;
        if (response instanceof SuccessResponse successResponse) {
            line = "OK|"+ successResponse.message();
        } else if (response instanceof ErrorResponse errorResponse) {
            line = "ERROR|"
                    + errorResponse.code()
                    + "|"
                    + errorResponse.message();
        } else {
            line = "ERROR|INTERNAL_ERROR|Unsupported response type";
        }
        return StandardCharsets.UTF_8.encode(line+"\n");
    }
}
