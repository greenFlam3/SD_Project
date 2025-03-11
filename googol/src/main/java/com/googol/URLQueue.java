package com.googol;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class URLQueue {
    private static final Queue<String> queue = new ConcurrentLinkedQueue<>();

    // Adiciona um URL à fila
    public static void addURL(String url) {
        queue.offer(url);
    }

    // Obtém e remove o próximo URL da fila
    public static String getNextURL() {
        return queue.poll();
    }

    // Verifica se a fila está vazia
    public static boolean isEmpty() {
        return queue.isEmpty();
    }

    public static void main(String[] args) {
        addURL("https://example.com");
        addURL("https://another-site.com");

        while (!isEmpty()) {
            System.out.println("Processando: " + getNextURL());
        }
    }
}
