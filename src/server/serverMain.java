package server;

import java.net.*;
import java.io.*;

public class serverMain {
    public static void main(String[] args) throws Exception {

        // Initialize Singletons for the Multi-Tiered architecture
        // Cache handles memory, DataManager handles disk persistence
        cacheHandler cache = new cacheHandler(5);
        dataManager dataManager = new dataManager();

        ServerSocket server = new ServerSocket(5050);
        System.out.println("Server running on port 5050...");

        while (true) {
            Socket client = server.accept();
            System.out.println("Client connected from " + client.getInetAddress());

            // Inject dependencies into the handler thread
            Thread t = new Thread(new clientHandler(client, cache, dataManager));
            t.start();
        }
    }
}