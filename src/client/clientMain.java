package client;

import java.net.*;
import java.io.*;
import java.util.Scanner;

public class clientMain {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Socket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;

        System.out.println("----- Word Count Client -----");

        try {
            // Initialize connection once (Persistent Connection)
            System.out.println("Connecting to server...");
            socket = new Socket("localhost", 5050);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("Connected!");

            // Loop until user chooses Exit
            boolean running = true;
            while (running) {
                System.out.println("\nSelect Option:");
                System.out.println("1. Store/Update File");
                System.out.println("2. Read File");
                System.out.println("3. Remove File");
                System.out.println("4. List Files");
                System.out.println("5. Get System Totals");
                System.out.println("6. Exit");
                System.out.print("> ");

                String choice = scanner.nextLine();

                String request = null;
                String extraData = null;

                // Determine Action based on User Input
                switch (choice) {
                    // STORE
                    case "1":
                        System.out.print("Enter filename on Server: ");
                        String remoteName = scanner.nextLine().trim();

                        System.out.print("Enter local file path (or 'manual'): ");
                        String localPath = scanner.nextLine().trim();

                        StringBuilder contentBuilder = new StringBuilder();

                        // Manual Entry Mode
                        if (localPath.equalsIgnoreCase("manual")) {
                            System.out.println("Type content ('DONE' on new line to finish):");

                            // Sentinel Loop for Input
                            while (true) {
                                String line = scanner.nextLine();
                                if (line.equals("DONE")) break;
                                contentBuilder.append(line).append("\n");
                            }
                        }
                        // File Reading Mode
                        else {
                            String fileContent = readLocalFile(localPath);
                            if (fileContent == null) {
                                System.out.println("Error reading file.");
                                continue;
                            }
                            contentBuilder.append(fileContent);
                        }

                        extraData = contentBuilder.toString();

                        System.out.print("Enter Flags (LWC): ");
                        String flags = scanner.nextLine().trim();
                        if (flags.isEmpty()) flags = "LWC";

                        request = "STORE\t" + remoteName + "\t" + flags;
                        break;

                    // READ
                    case "2":
                        System.out.print("Enter filename: ");
                        String readName = scanner.nextLine().trim();
                        request = "READ\t" + readName + "\t0";
                        break;

                    // REMOVE
                    case "3":
                        System.out.print("Enter filename: ");
                        String remName = scanner.nextLine().trim();
                        request = "REMOVE\t" + remName + "\t0";
                        break;

                    // LIST
                    case "4":
                        request = "LIST\tnull\t0";
                        break;

                    // TOTALS
                    case "5":
                        request = "TOTALS\tnull\t0";
                        break;

                    // EXIT
                    case "6":
                        // Break loop to trigger cleanup
                        running = false;
                        continue;

                    default:
                        System.out.println("Invalid option.");
                        continue;
                }

                // Protocol Transmission
                if (request != null) {
                    out.println(request);

                    // Send Payload if required
                    if (extraData != null) {
                        out.print(extraData);
                        // Send Sentinel to close stream
                        out.println("__END__");
                    }

                    // Receive Response
                    String responseHeader = in.readLine();
                    if (responseHeader == null) {
                        System.out.println("Server disconnected.");
                        break;
                    }

                    // Handle Multi-line Read Response
                    if (choice.equals("2") && responseHeader.equals("OK")) {
                        String line;
                        // Read until Sentinel
                        while ((line = in.readLine()) != null) {
                            if (line.equals("__END__")) break;
                            System.out.println(line);
                        }
                    }
                    // Handle Single-line Response
                    else {
                        System.out.println("[SERVER]: " + responseHeader);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            // Cleanup Resources
            try {
                if (socket != null) socket.close();
                scanner.close();
                System.out.println("Client closed.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Load file content from local disk
    private static String readLocalFile(String path) {
        try {
            File file = new File(path);
            if (!file.exists()) return null;

            StringBuilder content = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }
            return content.toString();
        } catch (IOException e) { return null; }
    }
}