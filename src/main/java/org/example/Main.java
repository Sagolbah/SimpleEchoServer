package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        final int PORT = 7932;

        System.out.println("Echo Server starting on port " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Echo Server is running...");

            while (true) {
                try {
                    // Wait for client connection
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());

                    // Create reader and writer for client communication
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                    String inputLine;
                    // Read data from client and echo it back until client closes connection
                    while ((inputLine = in.readLine()) != null) {
                        System.out.println("Received: " + inputLine);

                        // Echo the input back to client
                        out.println(inputLine);

                        // If client sends "exit", close the connection
                        if ("exit".equalsIgnoreCase(inputLine)) {
                            break;
                        }
                    }

                    // Close the client socket
                    clientSocket.close();
                    System.out.println("Client disconnected");

                } catch (IOException e) {
                    System.err.println("Error handling client: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + PORT);
            System.err.println("Error: " + e.getMessage());
        }
    }
}
