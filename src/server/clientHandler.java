package server;

import java.net.*;
import java.io.*;

public class clientHandler implements Runnable {

    private final Socket socket;
    private final cacheHandler cache;
    private final dataManager dataManager;

    public clientHandler(Socket socket, cacheHandler cache, dataManager dataManager) {
        this.socket = socket;
        this.cache = cache;
        this.dataManager = dataManager;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            // Protocol Handshake
            String header = in.readLine();
            if (header == null) return;

            System.out.println("Received header: " + header);

            // Parse TAB-delimited protocol
            String[] parts = header.split("\t");
            if (parts.length < 2) {
                out.println("ERROR\tBAD_HEADER");
                return;
            }

            String command = parts[0];
            String filename = parts[1];
            // Safety check for optional flags (some commands don't use them)
            String flags = parts.length > 2 ? parts[2] : "";

            // Data Ingestion (for STORE/UPDATE)
            String data = null;
            if (command.equals("STORE") || command.equals("UPDATE")) {
                data = in.readLine();
                if (data == null) { out.println("ERROR\tNO_DATA"); return; }
            }

            // Command Processing
            switch (command) {
                case "STORE":
                case "UPDATE":
                    // Write-Through Strategy: Update Disk AND Cache
                    // Ensures consistency if the server crashes immediately after
                    dataManager.saveFile(filename, data);
                    cache.put(filename, data);

                    // Calculate stats for this specific request
                    wordCounts wc = wordCounter.count(data);

                    // Update the persistent global system totals
                    dataManager.updateTotals(wc.lineCount, wc.wordCount, wc.charCount);

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
                        content = dataManager.readFile(filename);
                        if (content != null) {
                            System.out.println("[DATA] Disk Read: " + filename);
                            // Populate cache to speed up future reads
                            cache.put(filename, content);
                        }
                    }

                    if (content != null) out.println("OK\t" + content);
                    else out.println("ERROR\tFILE_NOT_FOUND");
                    break;

                case "REMOVE":
                    // Maintain consistency by cleaning both layers
                    cache.remove(filename);
                    dataManager.deleteFile(filename);
                    out.println("OK\tREMOVED");
                    break;

                case "LIST":
                    // Directory lookup on the Data Layer
                    out.println("OK\tFILES=" + dataManager.getFileList());
                    break;

                case "TOTALS":
                    // Retrieve persistent system-wide statistics
                    out.println("OK\t" + dataManager.getTotals());
                    break;

                default:
                    out.println("ERROR\tUNKNOWN_COMMAND");
                    break;
            }

        } catch (IOException e) {
            System.out.println("Handler error: " + e.getMessage());
        } finally {
            try { socket.close(); } catch (IOException ignore) {}
        }
    }
}