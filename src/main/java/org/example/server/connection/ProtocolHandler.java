package org.example.server.connection;

import com.google.gson.Gson;
import org.example.utils.Message;

import java.io.*;
import java.net.Socket;

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
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String jsonMessage;
            while((jsonMessage = in.readLine()) != null) {
                Message message = gson.fromJson(jsonMessage, Message.class);

                if ("!connect".equals(message.getCommand())) { //String equals to message.getCommand() ca sa eviti nullpointer excepiton
                    connectionManager.addRequest(message.getPort(), socket);
                    System.out.println("You received a request from user: " + message.getPort() + "! Type !requests to see available requests.");
                } else if ("!acc".equals(message.getCommand())) {
                    connectionManager.addConnection(message.getPort(), socket);
                    System.out.println("User " + message.getPort() + " accepted your request");
                } else if ("!dec".equals(message.getCommand())) {
                    System.out.println("User " + message.getPort() + " declined your request");
                    socket.close();
                }
                else if("!bye".equals(message.getCommand())){
                    System.out.println("User " + message.getPort() + " disconnected");
                    connectionManager.removeConnection(message.getPort());
                }
                else if("!message".equals(message.getCommand())){
                    System.out.println("User " + message.getPort() + ": " + message.getMessage());
                }
            }

        } catch (Exception ignored) {}
    }
}
