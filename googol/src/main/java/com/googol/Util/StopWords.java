package com.googol.Util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class StopWords {

    // Contador global de palabras (clave: palabra, valor: número de ocurrencias)
    private static final ConcurrentHashMap<String, AtomicInteger> wordCounts = new ConcurrentHashMap<>();
    // Cantidad total de páginas indexadas (se debe actualizar cada vez que se indexa una página)
    private static final AtomicInteger totalPages = new AtomicInteger(0);
    // Umbral de porcentaje (por ejemplo, 80% de las páginas)
    private static final double THRESHOLD_PERCENT = 0.8;

    /**
     * Se debe llamar a este método cada vez que se indexa una página.
     */
    public static void incrementPageCount() {
        totalPages.incrementAndGet();
    }

    /**
     * Actualiza el contador para una palabra dada.
     */
    public static void updateWord(String word) {
        word = word.toLowerCase();
        wordCounts.computeIfAbsent(word, k -> new AtomicInteger(0)).incrementAndGet();
    }

    /**
     * Determina si una palabra es stop word basado en la frecuencia relativa en el corpus.
     */
    public static boolean isStopWord(String word) {
        word = word.toLowerCase();
        int count = wordCounts.getOrDefault(word, new AtomicInteger(0)).get();
        int pages = totalPages.get();
        // Si se ha indexado al menos una página y la palabra aparece en un porcentaje alto de ellas, se considera stop word
        if (pages > 0 && ((double) count / pages) >= THRESHOLD_PERCENT) {
            return true;
        }
        return false;
    }
}