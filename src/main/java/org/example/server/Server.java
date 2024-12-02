package org.example.server;

import org.example.server.connection.ConnectionManager;
import org.example.server.connection.ProtocolHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final int port;
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private final ConnectionManager connectionManager = new ConnectionManager();

    public Server(int port) {
        this.port = port;
    }

    public void start() throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadPool.submit(new ProtocolHandler(clientSocket, connectionManager));
            }
        } catch (IOException e) {
            throw new Exception("Port is not available: " + port);
        } finally {
            threadPool.shutdown();
        }
    }
}
