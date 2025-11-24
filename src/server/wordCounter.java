package server;

public class wordCounter {

    // Operations

    public static wordCounts count(String text) {
        if (text == null || text.isEmpty()) {
            return new wordCounts(0, 0, 0);
        }

        int lineCount = 0;
        int wordCount = 0;
        int charCount = 0;

        // 1. Split by logical lines first (handles \n)
        String[] lines = text.split("\n");
        lineCount = lines.length;

        for (String line : lines) {
            // 2. Count Words per line
            // Separators: space, dot, colon, semi-colon, dash
            // We trim first to avoid empty tokens from leading spaces
            String[] tokens = line.trim().split("[ .:;-]+");

            for (String t : tokens) {
                if (!t.isEmpty()) {
                    wordCount++;
                }
            }

            // 3. Count Characters per line
            // Valid units: A-Z, a-z, 0-9
            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                if (isGrammarUnit(c)) {
                    charCount++;
                }
            }
        }

        return new wordCounts(lineCount, wordCount, charCount);
    }

    // Helper to enforce specific grammar rules for <unit>
    private static boolean isGrammarUnit(char c) {
        return (c >= 'A' && c <= 'Z') ||
                (c >= 'a' && c <= 'z') ||
                (c >= '0' && c <= '9');
    }
}