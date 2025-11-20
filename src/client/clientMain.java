package client;

import java.net.*;
import java.io.*;
import java.util.Scanner;

public class clientMain {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("--- Word Count Client ---");

        while (true) {
            System.out.println("\nSelect Option:");
            System.out.println("1. Store/Update File");
            System.out.println("2. Read File");
            System.out.println("3. Remove File");
            System.out.println("4. List Files");
            System.out.println("5. Get System Totals");
            System.out.println("6. Exit");
            System.out.print("> ");

            String choice = scanner.nextLine();

            if (choice.equals("6")) {
                System.out.println("Exiting.");
                break;
            }

            try {
                // Connect to server for each request (stateless connection pattern)
                Socket socket = new Socket("localhost", 5050);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String request = null;
                String extraData = null;

                // Logic to build the request based on user input
                switch (choice) {
                    case "1": // STORE
                        System.out.print("Enter filename on Server: ");
                        String remoteName = scanner.nextLine();

                        System.out.print("Enter local file path to upload (or type 'manual'): ");
                        String localPath = scanner.nextLine();

                        if (localPath.equalsIgnoreCase("manual")) {
                            System.out.print("Type content: ");
                            extraData = scanner.nextLine();
                        } else {
                            // Read local file
                            extraData = readLocalFile(localPath);
                            if (extraData == null) {
                                System.out.println("Error reading local file.");
                                socket.close();
                                continue;
                            }
                        }

                        System.out.print("Enter Flags (e.g. LWC): ");
                        String flags = scanner.nextLine();
                        if (flags.isEmpty()) flags = "LWC"; // Default

                        // Protocol: STORE \t filename \t flags
                        request = "STORE\t" + remoteName + "\t" + flags;
                        break;

                    case "2": // READ
                        System.out.print("Enter filename to read: ");
                        String readName = scanner.nextLine();
                        request = "READ\t" + readName + "\t0";
                        break;

                    case "3": // REMOVE
                        System.out.print("Enter filename to remove: ");
                        String remName = scanner.nextLine();
                        request = "REMOVE\t" + remName + "\t0";
                        break;

                    case "4": // LIST
                        request = "LIST\tnull\t0";
                        break;

                    case "5": // TOTALS
                        request = "TOTALS\tnull\t0";
                        break;

                    default:
                        System.out.println("Invalid option.");
                        socket.close();
                        continue;
                }

                // Send Request
                if (request != null) {
                    out.println(request);
                    // If we have file content (for STORE), send it now
                    if (extraData != null) {
                        out.println(extraData);
                    }
                }

                // Receive Response
                String response = in.readLine();
                System.out.println("[SERVER]: " + response);

                socket.close();

            } catch (IOException e) {
                System.out.println("Connection Error: " + e.getMessage());
            }
        }
        scanner.close();
    }

    // Helper to read a text file from your own computer to send to the server
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
        } catch (IOException e) {
            return null;
        }
    }
}