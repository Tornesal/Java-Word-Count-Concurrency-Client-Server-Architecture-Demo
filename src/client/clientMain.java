package client;

import java.net.*;
import java.io.*;

public class clientMain {
    public static void main(String[] args) throws Exception {

        Socket s = new Socket("localhost", 5050);

        PrintWriter out = new PrintWriter(s.getOutputStream(), true);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(s.getInputStream())
        );

        // Build a test STORE request using TABS
        String command = "STORE";
        String filename = "test.txt";
        String flags = "LWC"; // L = lines, W = words, C = chars

        String header = command + "\t" + filename + "\t" + flags;
        String fileData = "Hello there this is a test file.";

        // Send header line
        out.println(header);
        // Send data line (because STORE needs data)
        out.println(fileData);

        // Read server response
        String reply = in.readLine();
        System.out.println("Server replied: " + reply);

        s.close();
    }
}
