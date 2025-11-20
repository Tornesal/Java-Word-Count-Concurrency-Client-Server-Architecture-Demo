package client;

import java.net.*;
import java.io.*;

public class clientMain {
    public static void main(String[] args) throws Exception {
        Socket s = new Socket("localhost", 5050);
        PrintWriter out = new PrintWriter(s.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));

        String command = "STORE";
        String filename = "grammar_test.txt";
        String flags = "LWC";
        // Test String: "Hello-World; 123."
        // Lines: 1
        // Words: 3 ("Hello", "World", "123") -> Separators are - and ; and .
        // Chars: 13 (H,e,l,l,o,W,o,r,l,d,1,2,3) -> Separators are NOT counted as valid characters
        String fileData = "Hello-World; 123.";

        out.println(command + "\t" + filename + "\t" + flags);
        out.println(fileData);

        // 2. Get Response
        String reply = in.readLine();
        System.out.println("Server replied: " + reply);

        s.close();
    }
}