package com.googol.Storage;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

public class StorageBarrelImpl extends UnicastRemoteObject implements StorageBarrel {

    private final Map<String, Set<String>> invertedIndex;
    private final Map<String, String> pageTitles;
    private final Map<String, String> pageSnippets;
    private final Map<String, Set<String>> inboundLinks;
    private final int id;

    public StorageBarrelImpl(int id) throws RemoteException {
        super();
        this.id = id;
        this.invertedIndex = new HashMap<>();
        this.pageTitles = new HashMap<>();
        this.pageSnippets = new HashMap<>();
        this.inboundLinks = new HashMap<>();
    }

    @Override
    public void addToIndex(String word, String url) throws RemoteException {
        invertedIndex.computeIfAbsent(word.toLowerCase(), k -> new HashSet<>()).add(url);
    }

    @Override
    public Set<String> search(String query) throws RemoteException {
        // Normalizar la consulta
        String key = query.toLowerCase().trim();
        System.out.println("[StorageBarrel" + id + "] Searching term: '" + key + "'");
        // Obtener el conjunto de URLs para esa clave
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
        }
        return result != null ? result : Set.of();
    }

    @Override
    public Set<String> buscarPagina(String termo) throws RemoteException {
        return search(termo);
    }

    @Override
    public void armazenarPagina(String url, String content) throws RemoteException {
        System.out.println("[StorageBarrel" + id + "] Storing page: " + url);
        // Partir el contenido en palabras, normalizar a minúsculas y hacer trim
        String[] words = content.split("\\W+");
        for (String w : words) {
            if (w == null || w.isBlank()) continue;
            String key = w.toLowerCase().trim();
            // Añadir la URL al conjunto asociado a esa palabra
            invertedIndex
                .computeIfAbsent(key, k -> new CopyOnWriteArraySet<>())
                .add(url);
        }
        System.out.println("[StorageBarrel" + id + "] Index after storing: " + invertedIndex);
    }

    @Override
    public int getTotalPaginas() throws RemoteException {
        Set<String> allUrls = new HashSet<>();
        for (Set<String> urls : invertedIndex.values()) {
            allUrls.addAll(urls);
        }
        return allUrls.size();
    }

    @Override
    public PageInfo getPageSummary(String url) throws RemoteException {
        String title = pageTitles.getOrDefault(url, "No Title Available");
        String snippet = pageSnippets.getOrDefault(url, "No Snippet Available");
        return new PageInfo(title, snippet);
    }

    @Override
    public void addInboundLink(String targetUrl, String linkingUrl) throws RemoteException {
        inboundLinks.computeIfAbsent(targetUrl, k -> new HashSet<>()).add(linkingUrl);
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
                        e.printStackTrace(); // Log do erro
                        return 0; // Mantém a ordem inalterada em caso de erro
                    }
                })
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }    
}