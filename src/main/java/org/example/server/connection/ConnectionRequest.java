package org.example.server.connection;

import java.net.Socket;

public class ConnectionRequest {
    private final int port;
    private final Socket socket;

    public ConnectionRequest(int port, Socket socket) {
        this.port = port;
        this.socket = socket;
    }

    public int getPort() {
        return port;
    }

    public Socket getSocket() {
        return socket;
    }
}

