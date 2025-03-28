package com.googol.Downloader;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.googol.Queue.URLQueueInterface;
import com.googol.Storage.StorageBarrel;

public class Downloader {
    private static URLQueueInterface urlQueue;
    private static List<StorageBarrel> storageBarrels = new ArrayList<>();

    static {
        try {
            // Conectar a la URLQueue
            Registry registryQueue = LocateRegistry.getRegistry("localhost", 1088);
            urlQueue = (URLQueueInterface) registryQueue.lookup("URLQueue");

            // Conectar a las StorageBarrels (suponiendo que se registraron con nombres "StorageBarrel1", "StorageBarrel2", "StorageBarrel3")
            Registry registryStorage = LocateRegistry.getRegistry("localhost", 1099);
            int numberOfBarrels = 3; // Número de réplicas que tienes
            for (int i = 1; i <= numberOfBarrels; i++) {
                StorageBarrel barrel = (StorageBarrel) registryStorage.lookup("StorageBarrel" + i);
                storageBarrels.add(barrel);
            }
        } catch (Exception e) {
            System.err.println("Error connecting to remote services: " + e.getMessage());
        }
    }

    public static void processURL(String url) {
        try {
            System.out.println("Processing: " + url);
            Document doc = Jsoup.connect(url).get();

            // Extraer palabras y enviarlas a indexar a cada StorageBarrel
            StringTokenizer tokenizer = new StringTokenizer(doc.text());
            while (tokenizer.hasMoreTokens()) {
                String word = tokenizer.nextToken();
                for (StorageBarrel barrel : storageBarrels) {
                    try {
                        barrel.addToIndex(word, url);
                    } catch (Exception ex) {
                        System.err.println("Error indexing word '" + word + "' in a barrel: " + ex.getMessage());
                    }
                }
            }

            // Procesar enlaces: agregar a la cola y registrar inbound links
            Elements links = doc.select("a[href]");
            links.forEach(link -> {
                try {
                    String targetUrl = link.absUrl("href");
                    urlQueue.addURL(targetUrl);
                    for (StorageBarrel barrel : storageBarrels) {
                        try {
                            barrel.addInboundLink(targetUrl, url);
                        } catch (Exception ex) {
                            System.err.println("Error registering inbound link for '" + targetUrl + "': " + ex.getMessage());
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error adding URL to queue: " + e.getMessage());
                }
            });

            System.out.println("Processed: " + url);
        } catch (Exception e) {
            System.err.println("Error accessing URL: " + url);
        }
    }

    public static void main(String[] args) {
        try {
            while (!urlQueue.isEmpty()) {
                String url = urlQueue.getNextURL();
                System.out.println("Processing: " + url);
                if (url != null) processURL(url);
            }
        } catch (Exception e) {
            System.err.println("Downloader encountered an error: " + e.getMessage());
        }
    }
}