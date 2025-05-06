package com.googol.Gateway;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
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

    public GatewayImpl(List<StorageBarrel> barrels) throws RemoteException {
        super();
        this.storageBarrels = new ArrayList<>(barrels);
        for (StorageBarrel b : barrels) {
            barrelMetrics.put(b, new Metrics());
        }
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
            }
        }

        if (failed.size() == storageBarrels.size()) {
            throw new RemoteException("Index failed on all replicas for URL: " + url);
        }
    }

    public Set<String> search(String query) throws RemoteException {
        String key = query.toLowerCase().trim();
        searchCounts
          .computeIfAbsent(key, k -> new AtomicInteger())
          .incrementAndGet();

        Set<String> result = new HashSet<>();
        Set<StorageBarrel> failed = new HashSet<>();

        for (StorageBarrel barrel : storageBarrels) {
            boolean success = false;
            int attempts = 3;

            while (!success && attempts-- > 0) {
                try {
                    // — Start timing search call
                    Metrics m = barrelMetrics.get(barrel);
                    long t0 = System.nanoTime();

                    Set<String> part = barrel.search(key);

                    long duration = System.nanoTime() - t0;
                    m.totalSearchNs.addAndGet(duration);
                    m.searchCalls.incrementAndGet();
                    // — End timing search call

                    result.addAll(part);
                    success = true;
                } catch (RemoteException e) {
                    System.err.println("[Gateway] Search error on " 
                        + barrel + ", retrying... (" + attempts + " left)");
                    try { Thread.sleep(200); } catch (InterruptedException ignored) {}
                }
            }

            if (!success) {
                System.err.println("[Gateway] Permanent search failure on " + barrel);
                failed.add(barrel);
            }
        }

        if (failed.size() == storageBarrels.size()) {
            throw new RemoteException("Search failed on all replicas for query: " + query);
        }
        return result;
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
    public Set<String> searchMultipleTerms(Set<String> terms) throws RemoteException {
        Set<String> result = null;

        for (StorageBarrel barrel : storageBarrels) {
            Set<String> urlsForThisBarrel;
            try {
                urlsForThisBarrel = barrel.searchMultipleTerms(terms);
            } catch (RemoteException e) {
                // if one replica is down, skip it
                System.err.println("[Gateway] Replica failed on AND-search: " + e.getMessage());
                continue;
            }
            if (result == null) {
                // first replica: initialize with its full set
                result = new HashSet<>(urlsForThisBarrel);
            } else {
                // intersect: keep only URLs present in both
                result.retainAll(urlsForThisBarrel);
            }
            // if intersection ever becomes empty, we can stop early
            if (result.isEmpty()) break;
        }

        // if all replicas failed to respond, treat as empty
        return result != null ? result : Set.of();
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