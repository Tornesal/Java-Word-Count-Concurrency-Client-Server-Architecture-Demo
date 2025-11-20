package server;

public class wordCounter {

    // Operations

    public static wordCounts count(String text) {
        if (text == null || text.isEmpty()) {
            return new wordCounts(0, 0, 0);
        }

        // 1. Count Lines
        // Grammar: <line> -> <words> '\n'
        // We use -1 to include trailing empty lines if necessary, though usually split is sufficient
        String[] lines = text.split("\n");
        int lineCount = lines.length;

        // 2. Count Words
        // Grammar: <words> -> <word> | <words> <separator> <word>
        // Separators defined as: blank, dot, :, ;, -
        // We split by any combination of these specific characters
        String[] tokens = text.split("[ .:;-]+");

        int wordCount = 0;
        for (String t : tokens) {
            // Only count if the token actually contains data (handles leading/trailing separators)
            if (!t.trim().isEmpty()) {
                wordCount++;
            }
        }

        // 3. Count Characters
        // Grammar: "Characters will be represented as the number of characters that are valid within the specified grammar"
        // Valid units are: A-Z, a-z, 0-9
        int charCount = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (isGrammarUnit(c)) {
                charCount++;
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