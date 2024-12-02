package org.example.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

public class Client {
    private final int clientPort;
    private final Gson gson;
    private final HashMap<Integer, Socket> connections;

    public Client(int clientPort) {
        this.clientPort = clientPort;
        this.gson = new Gson();
        this.connections = new HashMap<>();
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Commands:\n -> !connect PORT\n -> !bye PORT\n -> !message PORT\n -> !byebye");
            String input = scanner.nextLine();

            if ("!byebye".equals(input)) {
                System.out.println("Quitting...");
                System.exit(0);
            }

            String[] parts = input.split(" ");
            if (parts.length < 2) {
                System.out.println("Invalid command!");
                continue;
            }

            String command = parts[0];
            int port;

            try {
                port = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port number!");
                continue;
            }

            switch (command) {
                case "!connect":
                    if (connections.containsKey(port)) {
                        System.out.println("Already connected to port " + port);
                    } else {
                        requestConnection(port);
                    }
                    break;
                case "!bye":
                    if (!connections.containsKey(port)) {
                        System.out.println("No connection exists with port " + port);
                    } else {
                        handleDisconnect(port);
                    }
                    break;
                case "!message":
                    if (!connections.containsKey(port)) {
                        System.out.println("No connection exists with port " + port);
                    } else {
                        handleCommunication(connections.get(port));
                    }
                    break;
                default:
                    System.out.println("Unknown command!");
            }
        }
    }

    private void requestConnection(int port) {
        String host = "localhost";
        try {
            Socket socket = new Socket(host, port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Send connection request
            JsonObject handshakeRequest = new JsonObject();
            handshakeRequest.addProperty("command", "!connect");
            handshakeRequest.addProperty("port", clientPort);
            out.println(gson.toJson(handshakeRequest));

            // Wait for the server's response
            String response = in.readLine();
            if (response == null) {
                System.out.println("No response received from server.");
                socket.close();
                return;
            }

            JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);
            String command = jsonResponse.get("command").getAsString();

            if ("!accepted".equals(command)) {
                System.out.println("Connection accepted by port: " + port);
                connections.put(port, socket); // Save the connection
            } else if ("!rejected".equals(command)) {
                System.out.println("Connection rejected by port: " + port);
                socket.close(); // Close the socket on rejection
            } else {
                System.out.println("Unexpected response from server: " + command);
                socket.close();
            }
        } catch (IOException e) {
            System.out.println("Unable to connect to port: " + port);
        }
    }


    private void handleDisconnect(Integer port) {
        try {
            Socket socket = connections.get(port);
            if (socket != null) {
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                JsonObject disconnectRequest = new JsonObject();
                disconnectRequest.addProperty("command", "!bye");
                disconnectRequest.addProperty("port", clientPort);
                out.println(gson.toJson(disconnectRequest));
                socket.close();
                connections.remove(port);
                System.out.println("Disconnected from port: " + port);
            }
        } catch (IOException e) {
            System.out.println("Error while disconnecting: " + e.getMessage());
        }
    }

    private void handleCommunication(Socket socket) {
        Scanner scanner = new Scanner(System.in);
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("Enter your message:");
            String message = scanner.nextLine();
            JsonObject messageRequest = new JsonObject();
            messageRequest.addProperty("command", "!message");
            messageRequest.addProperty("port", clientPort);
            messageRequest.addProperty("message", message);
            out.println(gson.toJson(messageRequest));
        } catch (IOException e) {
            System.out.println("Error while sending message: " + e.getMessage());
        }
    }
}
