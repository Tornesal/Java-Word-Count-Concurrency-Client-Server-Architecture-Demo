package server;

import java.net.*;
import java.io.*;

public class clientHandler implements Runnable {

    private final Socket socket;

    public clientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream())
                );
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            // Read first line: COMMAND<TAB>FILENAME<TAB>FLAGS
            String header = in.readLine();
            if (header == null) {
                System.out.println("Client disconnected before sending header.");
                return;
            }

            System.out.println("Received header: " + header);

            String[] parts = header.split("\t");
            if (parts.length < 3) {
                out.println("ERROR\tBAD_HEADER");
                System.out.println("Bad header from client.");
                return;
            }

            String command = parts[0];   // e.g., STORE
            String filename = parts[1];  // e.g., notes.txt
            String flags = parts[2];     // e.g., LWC

            // For commands that need file data, read one more line
            String data = null;
            if (command.equals("STORE") || command.equals("UPDATE")) {
                data = in.readLine(); // for now: single-line file contents
                if (data == null) {
                    out.println("ERROR\tNO_DATA");
                    System.out.println("Expected data line but got null.");
                    return;
                }
                System.out.println("Received data: " + data);
            }

            // TODO: later, call persistence + word count + cache here.
            // For now, just echo something back so we can test the protocol.
            switch (command) {
                case "STORE":
                    wordCounts wc = wordCounter.count(data);

                    StringBuilder resp = new StringBuilder("OK");

                    if (flags.contains("L")) {
                        resp.append("\tLINES=").append(wc.lineCount);
                    }
                    if (flags.contains("W")) {
                        resp.append("\tWORDS=").append(wc.wordCount);
                    }
                    if (flags.contains("C")) {
                        resp.append("\tCHARS=").append(wc.charCount);
                    }

                    out.println(resp.toString());
                    break;

                case "LIST":
                    out.println("OK\tFILES=stub1.txt,stub2.txt");
                    break;
                default:
                    out.println("ERROR\tUNKNOWN_COMMAND");
                    break;
            }

        } catch (IOException e) {
            System.out.println("Client handler error: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException ignore) {}
        }
    }
}
