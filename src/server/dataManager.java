package server;

import java.io.*;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicLong;

public class dataManager {

    // Standardize where we keep the data to avoid cluttering the root
    private final String STORAGE_DIR = "server_data";
    // Binary file for stats ensures we don't mix it up with user text files
    private final File TOTALS_FILE = new File(STORAGE_DIR + File.separator + "totals.dat");

    // Atomic counters prevent race conditions when multiple clients update stats at once
    private AtomicLong globalLines = new AtomicLong(0);
    private AtomicLong globalWords = new AtomicLong(0);
    private AtomicLong globalChars = new AtomicLong(0);

    public dataManager() {
        // Ensure the data layer has a physical place to live
        File dir = new File(STORAGE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Restore previous state (persistence requirement)
        loadTotals();
    }

    // File Operations

    public synchronized void saveFile(String filename, String content) throws IOException {
        // Writes raw bytes to the storage directory
        Path path = Paths.get(STORAGE_DIR, filename);
        Files.write(path, content.getBytes());
        System.out.println("[DATA] Persisted: " + filename);
    }

    public synchronized String readFile(String filename) throws IOException {
        // Used when a file is requested but fell out of the Cache
        Path path = Paths.get(STORAGE_DIR, filename);
        if (!Files.exists(path)) return null;

        return new String(Files.readAllBytes(path));
    }

    public synchronized boolean deleteFile(String filename) {
        // Removes the physical reference from the disk
        File f = new File(STORAGE_DIR, filename);
        boolean deleted = f.delete();

        if (deleted) System.out.println("[DATA] Deleted: " + filename);
        return deleted;
    }

    public String getFileList() {
        File folder = new File(STORAGE_DIR);

        // Filter out the system file (totals.dat) so clients only see their text files
        File[] files = folder.listFiles((dir, name) -> !name.equals("totals.dat"));

        if (files == null || files.length == 0) return "EMPTY";

        // Formatting list for protocol transmission
        StringBuilder sb = new StringBuilder();
        for (File f : files) sb.append(f.getName()).append(",");

        // Clean up trailing comma for cleaner output
        return sb.substring(0, sb.length() - 1);
    }

    // Global Statistics

    // Needed to subtract totals when updating a file
    public void subtractTotals(int l, int w, int c) {
        globalLines.addAndGet(-l);
        globalWords.addAndGet(-w);
        globalChars.addAndGet(-c);
        saveTotals();
    }

    public void updateTotals(int l, int w, int c) {
        // Add new file stats to the running system total
        globalLines.addAndGet(l);
        globalWords.addAndGet(w);
        globalChars.addAndGet(c);

        // Commit to disk immediately so we don't lose data on a crash
        saveTotals();
    }

    public String getTotals() {
        // Format specifically for the client protocol response
        return "Lines=" + globalLines.get() +
                ", Words=" + globalWords.get() +
                ", Chars=" + globalChars.get();
    }

    // Internal Persistence

    private synchronized void saveTotals() {
        // Use DataOutputStream for reliable binary storage of Longs
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(TOTALS_FILE))) {
            dos.writeLong(globalLines.get());
            dos.writeLong(globalWords.get());
            dos.writeLong(globalChars.get());
        } catch (IOException e) {
            System.out.println("[DATA] Failed to save totals: " + e.getMessage());
        }
    }

    private void loadTotals() {
        // Only attempt load if we actually have a previous run saved
        if (!TOTALS_FILE.exists()) return;

        try (DataInputStream dis = new DataInputStream(new FileInputStream(TOTALS_FILE))) {
            globalLines.set(dis.readLong());
            globalWords.set(dis.readLong());
            globalChars.set(dis.readLong());
            System.out.println("[DATA] Totals restored from previous run.");
        } catch (IOException e) {
            System.out.println("[DATA] Failed to load totals.");
        }
    }
}