package server;

import java.net.*;
import java.io.*;

public class serverMain {
    public static void main(String[] args) throws Exception {

        // Configuration: Default cache size is 5, unless args[0] is provided
        int cacheSize = 5;
        if (args.length > 0) {
            try {
                cacheSize = Integer.parseInt(args[0]);
                System.out.println("Cache size configured to: " + cacheSize);
            } catch (NumberFormatException e) {
                System.out.println("Invalid cache size argument. Using default: 5");
            }
        } else {
            System.out.println("No cache size argument provided. Using default: 5");
        }

        // Initialize Singletons
        cacheHandler cache = new cacheHandler(cacheSize);
        dataManager dm = new dataManager();

        ServerSocket server = new ServerSocket(5050);
        System.out.println("Server running on port 5050...");

        while (true) {
            Socket client = server.accept();
            System.out.println("Client connected from " + client.getInetAddress());

            // Inject dependencies
            Thread t = new Thread(new clientHandler(client, cache, dm));
            t.start();
        }
    }
}