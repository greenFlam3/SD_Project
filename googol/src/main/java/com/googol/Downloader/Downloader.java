package com.googol.Downloader;

import com.googol.Queue.URLQueueInterface;
import com.googol.Storage.StorageBarrel;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.StringTokenizer;

public class Downloader {
    private static URLQueueInterface urlQueue;
    private static StorageBarrel storageBarrel;

    static {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1088);
            urlQueue = (URLQueueInterface) registry.lookup("URLQueue");
            storageBarrel = (StorageBarrel) LocateRegistry.getRegistry("localhost", 1099).lookup("StorageBarrel");
        } catch (Exception e) {
            System.err.println("Error connecting to remote services: " + e.getMessage());
        }
    }

    public static void processURL(String url) {
        try {
            System.out.println("Processing: " + url);
            Document doc = Jsoup.connect(url).get();

            // Extract words and index them in StorageBarrel
            StringTokenizer tokenizer = new StringTokenizer(doc.text());
            while (tokenizer.hasMoreTokens()) {
                String word = tokenizer.nextToken();
                storageBarrel.addToIndex(word, url);
            }

            // Extract and add new links to the queue
            Elements links = doc.select("a[href]");
            links.forEach(link -> {
                try {
                    urlQueue.addURL(link.absUrl("href"));
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