package org.example.server.connection;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    private final ConcurrentHashMap<Integer, Socket> connections = new ConcurrentHashMap<>();

    public void addConnection(Integer port, Socket socket) {
        connections.put(port, socket);
    }

    public void removeConnection(Integer port) {
        connections.remove(port);
    }

    public Socket getConnection(Integer port) {
        return connections.get(port);
    }

    public boolean doesConnectionExist(Integer port) {
        return connections.containsKey(port);
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
}
