package com.googol.Storage;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StorageBarrelImpl extends UnicastRemoteObject implements StorageBarrel {

    private final Map<String, Set<String>> invertedIndex;
    private final Map<String, String> pageTitles;
    private final Map<String, String> pageSnippets;
    private final int id; // Identificador do Storage Barrel

    public StorageBarrelImpl(int id) throws RemoteException {
        super();
        this.id = id;
        this.invertedIndex = new HashMap<>();
        this.pageTitles = new HashMap<>();
        this.pageSnippets = new HashMap<>();
    }

    @Override
    public void addToIndex(String word, String url) throws RemoteException {
        invertedIndex.computeIfAbsent(word.toLowerCase(), k -> new HashSet<>()).add(url);
    }

    @Override
    public Set<String> search(String word) throws RemoteException {
        return invertedIndex.getOrDefault(word.toLowerCase(), Set.of());
    }

    @Override
    public Set<String> searchMultipleTerms(Set<String> terms) throws RemoteException {
        Set<String> result = null;

        for (String term : terms) {
            Set<String> urls = invertedIndex.get(term.toLowerCase());

            if (urls == null) {
                return Set.of(); // Se um termo não for encontrado, retorna conjunto vazio.
            }
            if (result == null) {
                result = new HashSet<>(urls); // Primeiro conjunto encontrado
            } else {
                result.retainAll(urls); // Mantém apenas URLs que aparecem em todos os termos
            }
        }
        return result != null ? result : Set.of();
    }

    @Override
    public Set<String> buscarPagina(String termo) throws RemoteException {
        System.out.println("[Barrel " + id + "] Searching for term: " + termo);
        return search(termo);
    }

    @Override
    public void armazenarPagina(String url, String conteudo) throws RemoteException {
        String[] palavras = conteudo.toLowerCase().split("\\s+");
        for (String palavra : palavras) {
            addToIndex(palavra, url);
        }

        // Extraindo título e snippet
        String title = conteudo.split("\\n")[0]; // Primeira linha como título
        String snippet = conteudo.length() > 150 ? conteudo.substring(0, 150) + "..." : conteudo;
        pageTitles.put(url, title);
        pageSnippets.put(url, snippet);

        System.out.println("[Barrel " + id + "] Page stored: " + url);
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
}