package com.googol.Downloader;

import com.googol.Queue.URLQueueInterface;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.StringTokenizer;

public class Downloader {
    private static URLQueueInterface urlQueue;

    static {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            urlQueue = (URLQueueInterface) registry.lookup("URLQueue");
        } catch (Exception e) {
            System.err.println("Erro ao conectar à URLQueue: " + e.getMessage());
        }
    }

    public static void processURL(String url) {
        try {
            Document doc = Jsoup.connect(url).get();

            // Extrai palavras e indexa (simplificado para foco na fila de URLs)
            StringTokenizer tokenizer = new StringTokenizer(doc.text());
            while (tokenizer.hasMoreTokens()) {
                System.out.println("Palavra indexada: " + tokenizer.nextToken());
            }

            // Adiciona novos links à fila
            Elements links = doc.select("a[href]");
            links.forEach(link -> {
                try {
                    urlQueue.addURL(link.absUrl("href"));
                } catch (Exception e) {
                    System.err.println("Erro ao adicionar URL: " + e.getMessage());
                }
            });

            System.out.println("Processado: " + url);
        } catch (Exception e) {
            System.err.println("Erro ao acessar URL: " + url);
        }
    }

    public static void main(String[] args) {
        try {
            urlQueue.addURL("https://example.com");

            while (!urlQueue.isEmpty()) {
                String url = urlQueue.getNextURL();
                if (url != null) processURL(url);
            }
        } catch (Exception e) {
            System.err.println("Erro no downloader: " + e.getMessage());
        }
    }
}