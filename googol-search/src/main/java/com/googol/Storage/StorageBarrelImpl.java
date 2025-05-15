package com.googol.Storage;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

import com.googol.Util.StopWords;
import com.googol.Gateway.PageInfo;

public class StorageBarrelImpl extends UnicastRemoteObject implements StorageBarrel {

    private static final long serialVersionUID = 1L;
    private final ConcurrentHashMap<String, Set<String>> invertedIndex;
    private final Map<String, String> pageTitles;
    private final Map<String, String> pageSnippets;
    private final Map<String, Set<String>> inboundLinks;
    private final int id;

    public StorageBarrelImpl(int id) throws RemoteException {
        super();
        this.id = id;
        this.invertedIndex = new ConcurrentHashMap<>();
        this.pageTitles = new HashMap<>();
        this.pageSnippets = new HashMap<>();
        this.inboundLinks = new HashMap<>();
    }

    public int getId() {
        return this.id;
    }

    @Override
    public void addToIndex(String word, String url) throws RemoteException {
        invertedIndex
            .computeIfAbsent(word.toLowerCase(), k -> new HashSet<>())
            .add(url);
    }

    @Override
    public Set<String> search(String query) throws RemoteException {
        String key = query.toLowerCase().trim();
        StopWords.updateSearchTerm(key);
        System.out.println("[StorageBarrel" + id + "] Searching term: '" + key + "'");
        Set<String> results = invertedIndex.getOrDefault(key, new CopyOnWriteArraySet<>());
        if (results.isEmpty()) {
            System.out.println("[StorageBarrel" + id + "] No results for: '" + key + "'");
        } else {
            System.out.println("[StorageBarrel" + id + "] Results: " + results);
        }
        return results;
    }

    @Override
    public Set<String> searchMultipleTerms(Set<String> terms) throws RemoteException {
        Set<String> result = null;
        for (String term : terms) {
            Set<String> urls = invertedIndex.get(term.toLowerCase());
            if (urls == null) {
                return Set.of();
            }
            if (result == null) {
                result = new HashSet<>(urls);
            } else {
                result.retainAll(urls);
            }
            if (result.isEmpty()) {
                break;
            }
        }
        return result != null ? result : Set.of();
    }

    @Override
    public Set<String> buscarPagina(String termo) throws RemoteException {
        return search(termo);
    }

    @Override
    public void armazenarPagina(String url, String content) throws RemoteException {
        // Update total page count for stop-word statistics
        StopWords.incrementPageCount();

        System.out.println("[StorageBarrel" + id + "] Storing page: " + url);
        String[] words = content.split("\\W+");
        for (String w : words) {
            if (w == null || w.isBlank()) continue;
            String key = w.toLowerCase().trim();

            // Update word frequency and skip if it's a stop-word
            StopWords.updateWord(key);
            if (StopWords.isStopWord(key)) {
                System.out.println("[StorageBarrel" + id + "] Skipping stop word: '" + key + "'");
                continue;
            }

            // Index the word
            invertedIndex
                .computeIfAbsent(key, k -> new CopyOnWriteArraySet<>())
                .add(url);
            System.out.println("[StorageBarrel" + id + "] Indexed word: '" + key + "'");
        }

        // Store page title and snippet
        String title = content.split("\\n")[0];
        String snippet = content.length() > 150
            ? content.substring(0, 150) + "..."
            : content;
        pageTitles.put(url, title);
        pageSnippets.put(url, snippet);
        System.out.println("[StorageBarrel" + id + "] Page stored with title and snippet.");
    }

    @Override
    public int getTotalPaginas() throws RemoteException {
        synchronized (invertedIndex) {
        Set<String> allUrls = new HashSet<>();
        for (Set<String> urls : invertedIndex.values()) {
            allUrls.addAll(urls);
        }
        return allUrls.size();
        }
    }

    @Override
    public PageInfo getPageSummary(String url) throws RemoteException {
        String title = pageTitles.getOrDefault(url, "No Title Available");
        String snippet = pageSnippets.getOrDefault(url, "No Snippet Available");
        return new PageInfo(title, snippet);
    }

    @Override
    public void addInboundLink(String targetUrl, String linkingUrl) throws RemoteException {
        inboundLinks
            .computeIfAbsent(targetUrl, k -> new HashSet<>())
            .add(linkingUrl);
    }

    @Override
    public Set<String> getInboundLinks(String url) throws RemoteException {
        return inboundLinks.getOrDefault(url, Set.of());
    }

    @Override
    public Set<String> searchOrderedByRelevance(String word) throws RemoteException {
        Set<String> urls = invertedIndex.getOrDefault(word.toLowerCase(), Set.of());
        return urls.stream()
            .sorted((u1, u2) -> {
                try {
                    return Integer.compare(
                        getInboundLinks(u2).size(),
                        getInboundLinks(u1).size());
                } catch (RemoteException e) {
                    e.printStackTrace();
                    return 0;
                }
            })
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    // Debug method to print stored pages and their titles/snippets
    public void debugPrintStoredPages() {
        System.out.println("=== DEBUG: Stored Pages ===");

        if (pageTitles.isEmpty()) {
            System.out.println("No pages stored.");
            return;
        }

        for (String url : pageTitles.keySet()) {
            System.out.println("URL: " + url);
            System.out.println("Title: " + pageTitles.get(url));
            System.out.println("Snippet: " + pageSnippets.getOrDefault(url, "No Snippet Available"));
            System.out.println("========================================");
        }
    }
}