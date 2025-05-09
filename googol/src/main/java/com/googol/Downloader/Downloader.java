package com.googol.Downloader;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.googol.Queue.URLQueueInterface;
import com.googol.Storage.StorageBarrel;

public class Downloader {
    private static URLQueueInterface urlQueue;
    private static List<StorageBarrel> storageBarrels = new ArrayList<>();

    static {
        try {
            // connect to queue
            Registry rq = LocateRegistry.getRegistry("localhost", 1088);
            urlQueue = (URLQueueInterface) rq.lookup("URLQueue");

            // connect to barrels
            Registry rs = LocateRegistry.getRegistry("localhost", 1099);
            for (int i = 1; i <= 3; i++) {
                storageBarrels.add(
                  (StorageBarrel) rs.lookup("StorageBarrel" + i)
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Downloader initialization failed: " + e.getMessage(), e);
        }
    }

    // single‐URL processing logic unchanged
    public static void processURL(String url) {
        try {
            System.out.println("Processing URL: " + url);
            Document doc = Jsoup.connect(url).get();
            String text = doc.text();

            // ——— Index the page in each barrel —————————
            for (StorageBarrel barrel : storageBarrels) {
                String pageTitle = doc.title();
                String body      = doc.body().text();
                // join title + body with a newline so StorageBarrelImpl.split picks it up:
                barrel.armazenarPagina(url, pageTitle + "\n" + body);
                System.out.println("[Downloader] indexed in " + barrel + ": " + url);
            }

            // ——— Crawl outlinks and enqueue them ——————
            // Select all <a href="..."> links in the document
            Elements links = doc.select("a[href]");
            for (Element link : links) {
                String target = link.absUrl("href");
                if (target.isBlank()) continue;
                try {
                    // Add new URL to the queue for later processing
                    urlQueue.addURL(target);
                    for (StorageBarrel barrel : storageBarrels) {
                        try {
                            barrel.addInboundLink(target, url);  // link from current page to target
                        } catch (RemoteException e) {
                            System.err.println("[Downloader] Failed to register inbound link: " + e.getMessage());
                        }
                    }
                    System.out.println("[Downloader] enqueued: " + target);
                } catch (RemoteException e) {
                    System.err.println("[Downloader] failed to enqueue " + target + ": " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("[Downloader] Error processing URL " + url + ": " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        final int THREAD_COUNT = 5;
        ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(THREAD_COUNT);
    
        try {
            while (true) {
                // 1) Try to dequeue the next URL
                String url = urlQueue.getNextURL();
                if (url != null) {
                    // 2) Got one—submit it for processing
                    pool.execute(() -> processURL(url));
                } else {
                    // 3) No URL right now. Are we really done?
                    boolean queueEmpty     = urlQueue.isEmpty();           // remote queue
                    boolean noActiveTasks  = pool.getActiveCount() == 0;   // local workers
    
                    if (queueEmpty && noActiveTasks) {
                        // nothing left anywhere
                        break;
                    }
                    // otherwise, wait a bit for new URLs to arrive
                    Thread.sleep(500);
                }
            }
        } catch (Exception e) {
            System.err.println("[Downloader] main loop error: " + e.getMessage());
        } finally {
            pool.shutdown();
            try {
                if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                    pool.shutdownNow();
                }
            } catch (InterruptedException ie) {
                pool.shutdownNow();
                Thread.currentThread().interrupt();
            }
            System.out.println("[Downloader] all tasks finished.");
        }
    }
}    