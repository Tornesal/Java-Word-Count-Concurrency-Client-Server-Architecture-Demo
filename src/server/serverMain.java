package server;

import java.net.*;
import java.io.*;

public class serverMain {
    public static void main(String[] args) throws Exception {

        ServerSocket server = new ServerSocket(5000);
        System.out.println("Server running...");

        while (true) {
            Socket client = server.accept();
            System.out.println("Client connected!");

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(client.getInputStream())
            );

            PrintWriter out = new PrintWriter(
                    client.getOutputStream(), true
            );

            String msg = in.readLine();
            System.out.println("Client said: " + msg);

            out.println("ACK: " + msg);
        }
    }
}
