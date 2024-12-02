package org.example.server.connection;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ProtocolHandler implements Runnable {
    private final Socket socket;
    private final ConnectionManager connectionManager;
    private final Gson gson = new Gson();

    public ProtocolHandler(Socket socket, ConnectionManager connectionManager) {
        this.socket = socket;
        this.connectionManager = connectionManager;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String message = in.readLine();
            JsonObject request = gson.fromJson(message, JsonObject.class);
            String command = request.get("command").getAsString();
            Integer port = request.get("port").getAsInt();

            if ("!connect".equals(command)) {
                boolean accepted = requestUserApproval(port);

                if (accepted) {
                    out.println(createResponse("!accepted"));
                    connectionManager.addConnection(port, socket);
                } else {
                    out.println(createResponse("!rejected"));
                    socket.close();
                }
            } else if ("!message".equals(command)) {
                if (connectionManager.doesConnectionExist(port))
                    handleClientCommunication(request);
            } else if ("!bye".equals(command)) {
                if (connectionManager.doesConnectionExist(port))
                    handleClientDisconnected(port);
            }

        } catch (IOException e) {
            System.out.println("Connection lost: " + socket.getRemoteSocketAddress());
        }
    }

    private boolean requestUserApproval(Integer port) {
        System.out.println("Connection request from: " + port + ". Accept? (yes/no)");
        Scanner scanner = new Scanner(System.in);
        String response = scanner.nextLine();
        return response.equalsIgnoreCase("yes");
    }

    private void handleClientCommunication(JsonObject request) {
        Integer port = request.get("port").getAsInt();
        String message = request.get("message").getAsString();
        System.out.println("Received a message from " + port + ": '" + message + "'");
    }

    private void handleClientDisconnected(Integer port) {
        System.out.println("Client " + port + " disconnected!");
        connectionManager.removeConnection(port);
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("Error closing socket: " + e.getMessage());
        }
    }

    private String createResponse(String command) {
        JsonObject response = new JsonObject();
        response.addProperty("command", command);
        return gson.toJson(response);
    }
}
