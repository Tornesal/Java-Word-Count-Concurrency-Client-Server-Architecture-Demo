package server;

public class wordCounter {

    public static wordCounts count(String text) {
        if (text == null || text.isEmpty()) {
            return new wordCounts(0, 0, 0);
        }

        // Count lines
        int lineCount = text.split("\n", -1).length;

        // Count words using separators
        String[] tokens = text.split("[ \\t.:;-]+");
        int wordCount = 0;
        for (String t : tokens) {
            if (!t.isEmpty()) {
                wordCount++;
            }
        }

        // Count alphanumeric chars
        int charCount = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                charCount++;
            }
        }

        return new wordCounts(lineCount, wordCount, charCount);
    }
}

