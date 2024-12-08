package org.example;

import org.example.client.Client;
import org.example.server.Server;
import org.example.server.connection.ConnectionManager;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        System.out.println("Enter your desired port (maximum 5 digits):");
        System.out.print("> ");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();

        if (input.length() > 5 || input.length() <= 0) {
            System.out.println("Invalid port! Shutting down...");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid port! Shutting down...");
            return;
        }

        ConnectionManager connectionManager = new ConnectionManager();
        ExecutorService threadPool = Executors.newCachedThreadPool();

        Server server = new Server(port, connectionManager, threadPool);
        Thread serverThread = new Thread(() -> {
            try {
                server.start();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                System.exit(0);
            }
        });
        serverThread.start();

        Client client = new Client(port, connectionManager, threadPool);
        client.start();
    }
}
