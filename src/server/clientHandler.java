package server;

import java.net.*;
import java.io.*;

public class clientHandler implements Runnable {

    private final Socket socket;
    private final cacheHandler cache;
    private final dataManager dm;

    public clientHandler(Socket socket, cacheHandler cache, dataManager dm) {
        this.socket = socket;
        this.cache = cache;
        this.dm = dm;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String header;

            // Loop to handle multiple requests per connection
            while ((header = in.readLine()) != null) {

                System.out.println("Received header: " + header);

                // Parse TAB-delimited protocol
                String[] parts = header.split("\t");
                if (parts.length < 2) {
                    out.println("ERROR\tBAD_HEADER");
                    continue; // Skip to next request
                }

                String command = parts[0];
                String filename = parts[1];
                String flags = parts.length > 2 ? parts[2] : "";

                // Data Ingestion
                String data = null;
                if (command.equals("STORE") || command.equals("UPDATE")) {

                    StringBuilder sb = new StringBuilder();
                    String line;

                    // Sentinel Loop
                    // Reads lines until the magic word is found
                    while ((line = in.readLine()) != null) {
                        if (line.equals("__END__")) {
                            break;
                        }
                        sb.append(line).append("\n");
                    }

                    // Formatting Fix
                    // Remove the trailing newline from the last append operation
                    if (sb.length() > 0) {
                        sb.setLength(sb.length() - 1);
                    }

                    data = sb.toString();

                    // Validation check
                    if (data.isEmpty()) {
                        out.println("ERROR\tNO_DATA");
                        continue;
                    }
                }

                // Command Processing
                switch (command) {
                    case "STORE":
                    case "UPDATE":
                        // Write-Through Strategy: Update Disk AND Cache
                        dm.saveFile(filename, data);
                        cache.put(filename, data);

                        // Calculate stats for this specific request
                        wordCounts wc = wordCounter.count(data);

                        // Update the persistent global system totals
                        dm.updateTotals(wc.lineCount, wc.wordCount, wc.charCount);

                        // Build response based on user flags
                        StringBuilder resp = new StringBuilder("OK");
                        if (flags.contains("L")) resp.append("\tLINES=").append(wc.lineCount);
                        if (flags.contains("W")) resp.append("\tWORDS=").append(wc.wordCount);
                        if (flags.contains("C")) resp.append("\tCHARS=").append(wc.charCount);
                        out.println(resp.toString());
                        break;

                    case "READ":
                        String content = null;

                        // Read-Through Strategy
                        // 1. Check RAM (Cache) for speed
                        if (cache.contains(filename)) {
                            content = cache.get(filename);
                        }
                        // 2. Fallback to Data Layer (Disk)
                        else {
                            content = dm.readFile(filename);
                            if (content != null) {
                                System.out.println("[DATA] Disk Read: " + filename);
                                // Populate cache to speed up future reads
                                cache.put(filename, content);
                            }
                        }

                        if (content != null) {
                            // Send Status
                            out.println("OK");

                            // Send Content
                            out.print(content);

                            // Formatting for Sentinel
                            if (!content.endsWith("\n")) {
                                out.println();
                            }

                            // Send Sentinel
                            out.println("__END__");
                        }
                        else {
                            out.println("ERROR\tFILE_NOT_FOUND");
                        }
                        break;

                    case "REMOVE":
                        // Maintain consistency by cleaning both layers
                        cache.remove(filename);
                        dm.deleteFile(filename);
                        out.println("OK\tREMOVED");
                        break;

                    case "LIST":
                        // Directory lookup on the Data Layer
                        out.println("OK\tFILES=" + dm.getFileList());
                        break;

                    case "TOTALS":
                        // Retrieve persistent system-wide statistics
                        out.println("OK\t" + dm.getTotals());
                        break;

                    default:
                        out.println("ERROR\tUNKNOWN_COMMAND");
                        break;
                }
            }

        } catch (IOException e) {
            System.out.println("Handler error (Client Disconnected): " + e.getMessage());
        } finally {
            try { socket.close(); } catch (IOException ignore) {}
        }
    }
}