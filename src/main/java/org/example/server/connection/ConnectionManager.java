package org.example.server.connection;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    private final ConcurrentHashMap<Integer, Socket> connections = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Socket> requests = new ConcurrentHashMap<>();

    public void addConnection(Integer port, Socket socket) {
        connections.put(port, socket);
    }

    public void removeConnection(Integer port) {
        connections.remove(port);
    }

    public Socket getConnection(Integer port) {
        return connections.get(port);
    }

    public ConcurrentHashMap<Integer, Socket> getConnections() {
        return connections;
    }

    public boolean doesConnectionExist(Integer port) {
        return connections.containsKey(port);
    }

    public void addRequest(Integer port, Socket socket) {
        requests.put(port, socket);
    }

    public void removeRequest(Integer port) {
        requests.remove(port);
    }

    public Socket getRequest(Integer port) {
        return requests.get(port);
    }

    public boolean doesRequestExist(Integer port) {
        return requests.containsKey(port);
    }

    public ConcurrentHashMap<Integer, Socket> getRequests() {
        return requests;
    }

    public void closeAllConnections() {
        connections.forEach((port, socket) -> {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error closing socket: " + e.getMessage());
            }
        });
        connections.clear();
    }

    public void closeAllRequests() {
        requests.forEach((port, socket) -> {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error closing socket: " + e.getMessage());
            }
        });
        requests.clear();
    }
}
