package com.googol;

import java.util.*;

public class IndexStorageBarrel {
    private static final Map<String, Set<String>> invertedIndex = new HashMap<>();

    // Adiciona uma palavra ao índice
    public static void addToIndex(String word, String url) {
        invertedIndex.computeIfAbsent(word.toLowerCase(), k -> new HashSet<>()).add(url);
    }

    // Obtém páginas onde uma palavra ocorre
    public static Set<String> search(String word) {
        return invertedIndex.getOrDefault(word.toLowerCase(), Collections.emptySet());
    }

    public static void main(String[] args) {
        addToIndex("java", "https://example.com/java");
        addToIndex("programming", "https://example.com/code");

        System.out.println("Páginas sobre Java: " + search("java"));
    }
}
