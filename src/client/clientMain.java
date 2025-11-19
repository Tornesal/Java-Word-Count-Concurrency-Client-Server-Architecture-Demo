package client;

import java.net.*;
import java.io.*;

public class clientMain {
    public static void main(String[] args) throws Exception {
        Socket s = new Socket("localhost", 5000);

        PrintWriter out = new PrintWriter(s.getOutputStream(), true);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(s.getInputStream())
        );

        out.println("Hello server!");
        String reply = in.readLine();

        System.out.println("Server replied: " + reply);

        s.close();
    }
}
