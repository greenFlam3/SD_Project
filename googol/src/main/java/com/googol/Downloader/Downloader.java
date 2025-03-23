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
            System.err.println("Error connecting to URLQueue: " + e.getMessage());
        }
    }

    public static void processURL(String url) {
        try {
            Document doc = Jsoup.connect(url).get();

            // Extract words and index them (simplified for now)
            StringTokenizer tokenizer = new StringTokenizer(doc.text());
            while (tokenizer.hasMoreTokens()) {
                System.out.println("Indexed word: " + tokenizer.nextToken());
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
            urlQueue.addURL("https://example.com");

            while (!urlQueue.isEmpty()) {
                String url = urlQueue.getNextURL();
                if (url != null) processURL(url);
            }
        } catch (Exception e) {
            System.err.println("Downloader encountered an error: " + e.getMessage());
        }
    }
}