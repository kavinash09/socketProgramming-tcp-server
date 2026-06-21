package com.avinash.socket.application.command;



import com.avinash.socket.contracts.request.Request;

import java.util.Objects;

public final class CreateStudentCommand implements Request {
    private final String name;
    private final int age;
    public CreateStudentCommand(String name, int age) {
        this.name = Objects.requireNonNull(name, "name");
        this.age = age;
    }
    public String name() {
        return name;
    }
    public int age() {
        return age;
    }
}
