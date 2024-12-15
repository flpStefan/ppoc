package org.example.client;

import com.google.gson.Gson;
import org.example.server.connection.ConnectionManager;
import org.example.server.connection.ProtocolHandler;
import org.example.utils.Message;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;


public class Client {
    private final int userPort;
    private final String host = "localhost";
    private final ConnectionManager connectionManager;
    private final ExecutorService threadPool;
    private final Gson gson;

    public Client(int userPort, ConnectionManager connectionManager, ExecutorService threadPool) {
        this.userPort = userPort;
        this.connectionManager = connectionManager;
        this.threadPool = threadPool;
        this.gson = new Gson();
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Sucessfully connected!");
        while (true) {
            System.out.print("> ");
            String text;
            text = scanner.nextLine();

            if (text.equals("!byebye")) {
                handleDisconnectAll();
                threadPool.shutdown();
                System.exit(0);
            }

            String[] parts = text.split("\\s+");
            String command = parts[0];

            switch (command){
                case "!connect":
                    handleSendRequest(parts);
                    break;
                case "!bye":
                    handleDisconnectUser(parts);
                    break;
                case "!message":
                    handleSendMessage(parts);
                    break;
                case "!requests":
                    showRequests();
                    break;
                case "!connections":
                    showPeers();
                    break;
                case "!acc":
                    handleAcceptRequest(parts);
                    break;
                case "!dec":
                    handleDeclineRequest(parts);
                    break;
                case "!help":
                    showCommands();
                    break;
                default:
                    System.out.println("Unknown command! Use !help");
            }
        }


    }

    private void handleSendRequest(String[] parts) {
        String command = parts[0];
        int port = validateCommand(parts);
        if(port == -1) return;

        if(connectionManager.doesConnectionExist(port)){
            System.out.println("You are already connected to user: " + port);
            return;
        }

        try {
            Socket socket = new Socket(host, port);
//            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            threadPool.submit(new ProtocolHandler(socket, connectionManager));

            Message message = new Message(command, userPort);
            out.println(gson.toJson(message));
            System.out.println("Request sent!");

//            String reply = in.readLine();
//            Message rep = gson.fromJson(reply, Message.class);
//            System.out.println(rep);
        }
        catch (IOException exception) {
            System.out.println("Error in sending request to user: " + port);
        }
    }

    private void handleAcceptRequest(String[] parts) {
        int port = validateCommand(parts);
        if(port == -1) return;

        if(!connectionManager.doesRequestExist(port)){
            System.out.println("You have no request from " + port);
            return;
        }

        Socket socket = connectionManager.getRequest(port);
        try{
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            connectionManager.addConnection(port, socket);

            Message message = new Message(parts[0], userPort);
            out.println(gson.toJson(message));
            System.out.println("You can now communicate with user: " + port);
        }
        catch (IOException exception){
            System.out.println("Error in accepting request from " + port);
        }
        finally {
            connectionManager.removeRequest(port);
        }
    }

    private void handleDeclineRequest(String[] parts) {
        int port = validateCommand(parts);
        if(port == -1) return;

        if(connectionManager.doesRequestExist(port)){
            System.out.println("You have no request from " + port);
            return;
        }

        Socket socket = connectionManager.getRequest(port);
        try{
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            Message message = new Message(parts[0], userPort);
            out.println(gson.toJson(message));
        }
        catch (IOException exception){
            System.out.println("Error in declining request from " + port);
        }
        finally {
            connectionManager.removeRequest(port);
        }
    }

    private void handleSendMessage(String[] parts) {
        String command = parts[0];
        int port = validateCommand(parts);
        if(port == -1) return;

        if(!connectionManager.doesConnectionExist(port)){
            System.out.println("You are not connected with " + port + ". Use !connect to send a request.");
            return;
        }

        Socket socket = connectionManager.getConnection(port);
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            String text = "";
            for(int i = 2; i < parts.length; i++){
                text += parts[i];
                text += " ";
            }

            Message message = new Message(command, userPort, text);
            out.println(gson.toJson(message));
            System.out.println("Message sent!");
        }
        catch (IOException exception) {
            System.out.println("Error in sending message to user: " + port);
            connectionManager.removeConnection(port);
        }
    }

    private void handleDisconnectUser(String[] parts) {
        String command = parts[0];
        int port = validateCommand(parts);
        if(port == -1) return;

        if(!connectionManager.doesConnectionExist(port)){
            System.out.println("There is no connection with user " + port);
            return;
        }

        Socket socket = connectionManager.getConnection(port);
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            Message message = new Message(command, userPort);
            out.println(gson.toJson(message));
            socket.close();
            System.out.println("Disconnected from user " + port + "!");
        }
        catch (Exception exception) {
            System.out.println("User " + port + " is already disconnected");
        }
        finally {
            connectionManager.removeConnection(port);
        }
    }

    private void handleDisconnectAll() {
        connectionManager.getConnections().forEach((port, socket) -> {
            try {
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                Message message = new Message("!bye", userPort);
                out.println(gson.toJson(message));
            }
            catch (IOException ignored) {}
        });

        connectionManager.closeAllRequests();
        connectionManager.closeAllConnections();
    }

    private void showRequests() {
        if(connectionManager.getRequests().isEmpty()){
            System.out.println("You have no requests at the moment");
            return;
        }

        System.out.println("You have requests from: ");
        connectionManager.getRequests().keySet().forEach(key -> System.out.println("-> " + key));
        System.out.println("You can accept/decline requests by typing !acc/!dec PORT");
    }

    private void showPeers() {
        if(connectionManager.getConnections().isEmpty()){
            System.out.println("You have no peers");
            return;
        }

        System.out.println("List of peers: ");
        connectionManager.getConnections().keySet().forEach(key -> System.out.println("-> " + key));
    }

    private void showCommands() {
        System.out.println("Available commands:");
        System.out.println("-> !connect PORT");
        System.out.println("-> !message PORT MESSAGE");
        System.out.println("-> !bye PORT");
        System.out.println("-> !requests");
        System.out.println("-> !peers");
        System.out.println("-> !byebye");
    }

    private Integer validateCommand(String[] parts){
        if(parts.length < 2 || parts[1].isEmpty() || parts[1].length() > 5){
            System.out.println("Incorrect use of command, please enter a valid port!");
            return -1;
        }

        int port;
        try{
            port = Integer.parseInt(parts[1]);
        }
        catch(NumberFormatException exception){
            System.out.println("Incorrect use of command, please enter a valid port!");
            return -1;
        }

        return port;
    }
}
