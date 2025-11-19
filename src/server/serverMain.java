package server;

import java.net.*;
import java.io.*;

public class serverMain {
    public static void main(String[] args) throws Exception {

        ServerSocket server = new ServerSocket(5050); // keep 5050
        System.out.println("Server running on port 5050...");

        while (true) {
            Socket client = server.accept();
            System.out.println("Client connected from " + client.getInetAddress());

            // Spawn a new thread for this client
            Thread t = new Thread(new clientHandler(client));
            t.start();
        }
    }
}