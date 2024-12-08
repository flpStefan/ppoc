package org.example.utils;

public class Message {
    private String command;
    private int port;
    private String message;

    public Message(String command, int port, String message) {
        this.command = command;
        this.port = port;
        this.message = message;
    }

    public Message(String command, int port) {
        this.command = command;
        this.port = port;
    }

    public Message(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public int getPort() {
        return port;
    }

    public String getMessage() {
        return message;
    }
}

