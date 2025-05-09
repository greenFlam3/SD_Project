package com.googol.Gateway;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import com.googol.Storage.PageInfo;
import com.googol.Storage.StorageBarrel;

public class GatewayImpl extends UnicastRemoteObject implements GatewayService {
    private static final long serialVersionUID = 1L;

    private final ConcurrentHashMap<String, AtomicInteger> searchCounts = new ConcurrentHashMap<>();

    private static class Metrics {
        AtomicLong totalSearchNs = new AtomicLong();
        AtomicInteger searchCalls = new AtomicInteger();
        AtomicLong totalIndexNs  = new AtomicLong();
        AtomicInteger indexCalls  = new AtomicInteger();
    }
    private final Map<StorageBarrel, Metrics> barrelMetrics = new ConcurrentHashMap<>();

    private final List<StorageBarrel> storageBarrels;

    private final List<RetryEntry> retryQueue = new ArrayList<>();

    private static class RetryEntry {
        StorageBarrel barrel;
        String url;
        String content;

        RetryEntry(StorageBarrel barrel, String url, String content) {
            this.barrel = barrel;
            this.url = url;
            this.content = content;
        }
    }

    public GatewayImpl(List<StorageBarrel> barrels) throws RemoteException {
        super();
        this.storageBarrels = new ArrayList<>(barrels);
        for (StorageBarrel b : barrels) {
            barrelMetrics.put(b, new Metrics());
        }

        Thread retryThread = new Thread(() -> {
            while (true) {
                synchronized (retryQueue) {
                    retryQueue.removeIf(entry -> {
                        try {
                            entry.barrel.armazenarPagina(entry.url, entry.content);
                            System.out.println("[Retry] Success on " + entry.barrel + " for URL: " + entry.url);
                            return true;
                        } catch (RemoteException e) {
                            System.err.println("[Retry] Still failing on " + entry.barrel + " for " + entry.url);
                            return false;
                        }
                    });
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ignored) {}
            }
            });
            retryThread.setDaemon(true);
            retryThread.start();
    }

    @Override
    public void indexPage(String url, String title, String text) throws RemoteException {
        String combined = title + "\n" + text;
        Set<StorageBarrel> failed = new HashSet<>();

        for (StorageBarrel barrel : storageBarrels) {
            boolean ack = false;
            int attempts = 3;

            while (!ack && attempts-- > 0) {
                try {
                    // — Start timing index call
                    Metrics m = barrelMetrics.get(barrel);
                    long t0 = System.nanoTime();

                    barrel.armazenarPagina(url, combined);

                    long duration = System.nanoTime() - t0;
                    m.totalIndexNs.addAndGet(duration);
                    m.indexCalls.incrementAndGet();
                    // — End timing index call

                    System.out.println("[Gateway] ACK from " + barrel);
                    ack = true;
                } catch (RemoteException e) {
                    System.err.println("[Gateway] No ACK from " + barrel 
                        + ", retrying... (" + attempts + " left)");
                    try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                }
            }

            if (!ack) {
                System.err.println("[Gateway] Permanent failure on " + barrel);
                failed.add(barrel);
                synchronized (retryQueue) {
                    retryQueue.add(new RetryEntry(barrel, url, combined));
                }
            }            
        }

        if (failed.size() == storageBarrels.size()) {
            throw new RemoteException("Index failed on all replicas for URL: " + url);
        }
    }

    @Override
    public List<String> smartSearch(String userQuery) throws RemoteException {
        // 1) Split & normalize the user’s input
        Set<String> terms = Arrays.stream(userQuery.toLowerCase().split("\\s+"))
                                .filter(s -> !s.isBlank())
                                .collect(Collectors.toSet());
        System.out.println("[Gateway] Terms to search: " + terms);
        if (terms.isEmpty()) return List.of();

        // 2) Randomize barrel order for load‐balancing
        List<StorageBarrel> shuffledBarrels = new ArrayList<>(storageBarrels);
        Collections.shuffle(shuffledBarrels);

        // 3) Collect union across all barrels
        Set<String> combined = new HashSet<>();
        for (StorageBarrel barrel : shuffledBarrels) {
            System.out.println("[Gateway] Searching in barrel: " + barrel);
            Set<String> barrelResult;
            try {
                if (terms.size() == 1) {
                    // single‐term → simple search
                    barrelResult = barrel.search(terms.iterator().next());
                } else {
                    // multi‐term → AND within that barrel
                    barrelResult = barrel.searchMultipleTerms(terms);
                }
                System.out.println("[Gateway] Barrel result: " + barrelResult);
            } catch (RemoteException e) {
                System.err.println("[Gateway] Search failed on barrel " + barrel + ": " + e.getMessage());
                continue;
            }

            combined.addAll(barrelResult);
            System.out.println("[Gateway] Combined result so far: " + combined);
        }

        if (combined.isEmpty()) {
            return List.of();
        }

        return combined.stream()
            .sorted((u1, u2) -> {
                int in1 = 0, in2 = 0;
                for (StorageBarrel barrel : shuffledBarrels) {
                    try {
                        in1 += barrel.getInboundLinks(u1).size();
                        in2 += barrel.getInboundLinks(u2).size();
                    } catch (RemoteException ignored) { }
                }
                return Integer.compare(in2, in1);
            })
            .toList();
    }

    @Override
    public PageInfo getPageSummary(String url) throws RemoteException {
        for (StorageBarrel barrel : storageBarrels) {
            try {
                return barrel.getPageSummary(url);
            } catch (RemoteException e) {
                System.err.println("[Gateway] Summary error on " + barrel + ": " + e.getMessage());
            }
        }
        throw new RemoteException("All barrels failed to provide page summary for " + url);
    }

    @Override
    public List<String> getTopSearches() throws RemoteException {
        return searchCounts.entrySet().stream()
          .sorted((a, b) -> b.getValue().get() - a.getValue().get())
          .limit(10)
          .map(e -> e.getKey() + ": " + e.getValue().get())
          .collect(Collectors.toList());
    }

    @Override
    public List<BarrelStat> getBarrelStats() throws RemoteException {
        List<BarrelStat> stats = new ArrayList<>();
        for (Map.Entry<StorageBarrel, Metrics> entry : barrelMetrics.entrySet()) {
            StorageBarrel barrel = entry.getKey();
            Metrics m          = entry.getValue();

            // RPC call to get number of indexed pages
            int size = barrel.getTotalPaginas();

            double avgSearchMs = m.searchCalls.get() == 0
                ? 0
                : (m.totalSearchNs.get()  / 1_000_000.0) / m.searchCalls.get();
            double avgIndexMs = m.indexCalls.get() == 0
                ? 0
                : (m.totalIndexNs.get()   / 1_000_000.0) / m.indexCalls.get();

            stats.add(new BarrelStat(
                barrel.toString(),
                size,
                avgSearchMs,
                avgIndexMs
            ));
        }
        return stats;
    }
}