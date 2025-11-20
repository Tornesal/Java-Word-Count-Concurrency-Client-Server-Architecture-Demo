package server;

import java.util.*;

public class cacheHandler {
    // Key: Filename, Value: File Content
    private final Map<String, String> cache;
    private final int maxEntries;

    public cacheHandler(int size) {
        this.maxEntries = size;

        // synchronizedMap ensures thread safety (Consistency)
        // LinkedHashMap(capacity, loadFactor, true) -> 'true' enables Access-Order (LRU)
        this.cache = Collections.synchronizedMap(new LinkedHashMap<String, String>(size, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                // If cache size exceeds maxEntries, remove the oldest accessed item
                return size() > maxEntries;
            }
        });
    }

    public void put(String fileName, String content) {
        cache.put(fileName, content);
        System.out.println("[CACHE] Added/Updated: " + fileName);
    }

    public String get(String fileName) {
        String data = cache.get(fileName);
        if (data != null) {
            System.out.println("[CACHE] Hit: " + fileName);
        } else {
            System.out.println("[CACHE] Miss: " + fileName);
        }
        return data;
    }

    public void remove(String fileName) {
        cache.remove(fileName);
        System.out.println("[CACHE] Removed: " + fileName);
    }

    public boolean contains(String fileName) {
        return cache.containsKey(fileName);
    }
}