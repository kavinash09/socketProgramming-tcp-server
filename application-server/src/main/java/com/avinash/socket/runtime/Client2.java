package com.avinash.socket.runtime;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Client2 {
    private static final String HOST = "localhost";
    private static final int PORT = 9090;
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket(HOST, PORT);
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                        socket.getOutputStream(),
                        StandardCharsets.UTF_8
                )
        );
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        System.out.println("Client Two connected");
        send(writer, "PING");
        printResponse(reader);
        send(writer, "TEXT|hello from client one");
        printResponse(reader);

    }

    private static void printResponse(BufferedReader reader) throws IOException {
        String response = reader.readLine();
        System.out.println("Client Two received "+ response);
    }

    private static void send(BufferedWriter writer, String request) throws IOException {
        System.out.println("Client Two sending: "+ request);
        writer.write(request);
        writer.newLine();
        writer.flush();
    }
}
