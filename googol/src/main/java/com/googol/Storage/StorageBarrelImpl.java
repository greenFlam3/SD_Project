package com.googol.Storage;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.googol.Util.StopWords;

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
        System.out.println("[DEBUG] Pesquisa recebida: " + query);
        System.out.println("[DEBUG] Índice atual: " + invertedIndex);
    
        Set<String> results = new HashSet<>();
        for (Map.Entry<String, Set<String>> entry : invertedIndex.entrySet()) {
            System.out.println("[DEBUG] Verificando palavra: " + entry.getKey() + " -> URLs: " + entry.getValue());
            if (entry.getKey().equalsIgnoreCase(query)) {
                results.addAll(entry.getValue());
                System.out.println("[DEBUG] Página encontrada: " + entry.getValue());
            }
        }
    
        if (results.isEmpty()) {
            System.out.println("[DEBUG] Nenhum resultado encontrado para: " + query);
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
    public void armazenarPagina(String url, String conteudo) throws RemoteException {
        StopWords.incrementPageCount();
        
        System.out.println("[DEBUG] Armazenando URL: " + url);
        System.out.println("[DEBUG] Conteúdo recebido: " + conteudo);
    
        String[] palavras = conteudo.toLowerCase().split("\\s+");
        for (String palavra : palavras) {
            StopWords.updateWord(palavra);
            if (!StopWords.isStopWord(palavra)) {
                addToIndex(palavra, url);
                System.out.println("[DEBUG] Palavra indexada: " + palavra);
            }
        }
    
        String title = conteudo.split("\\n")[0]; 
        String snippet = conteudo.length() > 150 ? conteudo.substring(0, 150) + "..." : conteudo;
        pageTitles.put(url, title);
        pageSnippets.put(url, snippet);
    
        System.out.println("[DEBUG] Página armazenada com sucesso.");
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