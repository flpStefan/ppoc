package org.example.server;

import org.example.server.connection.ConnectionManager;
import org.example.server.connection.ProtocolHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final int port;
    private final ExecutorService threadPool;
    private final ConnectionManager connectionManager;

    public Server(int port, ConnectionManager connectionManager, ExecutorService threadPool) {
        this.port = port;
        this.connectionManager = connectionManager;
        this.threadPool = threadPool;
    }

    public void start() throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
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
