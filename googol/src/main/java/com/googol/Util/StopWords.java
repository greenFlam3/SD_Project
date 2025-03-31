package com.googol.Util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class StopWords {

    // Contador global de palavras (chave: palavra, valor: número de ocorrências)
    private static final ConcurrentHashMap<String, AtomicInteger> wordCounts = new ConcurrentHashMap<>();
    // Contador total de páginas indexadas
    private static final AtomicInteger totalPages = new AtomicInteger(0);
    // Porcentagem de corte (ex: 80% das páginas)
    private static final double THRESHOLD_PERCENT = 0.8;

    /**
     * Chamar este método a cada vez que uma página é indexada.
     */
    public static void incrementPageCount() {
        totalPages.incrementAndGet();
    }

    /**
     * Atualiza o contador para uma palavra específica.
     */
    public static void updateWord(String word) {
        word = word.toLowerCase();
        wordCounts.computeIfAbsent(word, k -> new AtomicInteger(0)).incrementAndGet();
    }

    /**
     * Determina se uma palavra é uma stop word com base na frequência no corpus.
     */
    public static boolean isStopWord(String word) {
        word = word.toLowerCase();
        int count = wordCounts.getOrDefault(word, new AtomicInteger(0)).get();
        int pages = totalPages.get();

        // Se o número de páginas indexadas for maior que 0 e a palavra aparece em um grande percentual delas, é considerada stop word
        return pages > 0 && ((double) count / pages) >= THRESHOLD_PERCENT;
    }
}