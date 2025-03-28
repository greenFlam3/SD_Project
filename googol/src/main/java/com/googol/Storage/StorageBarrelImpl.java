package com.googol.Storage;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//NEW
import java.util.LinkedHashSet;
import java.util.stream.Collectors;
import com.googol.Util.StopWords; // Se asume que ya existe esta clase de utilidades

public class StorageBarrelImpl extends UnicastRemoteObject implements StorageBarrel {

    private final Map<String, Set<String>> invertedIndex;
    private final Map<String, String> pageTitles;
    private final Map<String, String> pageSnippets;
    private final Map<String, Set<String>> inboundLinks; // NUEVO: Registra los enlaces entrantes
    private final int id; // Identificador do Storage Barrel

    public StorageBarrelImpl(int id) throws RemoteException {
        super();
        this.id = id;
        this.invertedIndex = new HashMap<>();
        this.pageTitles = new HashMap<>();
        this.pageSnippets = new HashMap<>();
        this.inboundLinks = new HashMap<>();

    }

    @Override
    // Se normaliza la palabra a minúsculas
    public void addToIndex(String word, String url) throws RemoteException {
        invertedIndex.computeIfAbsent(word.toLowerCase(), k -> new HashSet<>()).add(url);
    }

    @Override
    // Devuelve el conjunto de URLs asociadas a la palabra (o conjunto vacío si no existe)
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
    // Incrementar el contador de páginas indexadas
    StopWords.incrementPageCount();
    
    // Separamos el contenido en palabras (convertido a minúsculas)
    String[] palavras = conteudo.toLowerCase().split("\\s+");
    for (String palavra : palavras) {
        // Actualizamos el contador de esta palabra
        StopWords.updateWord(palavra);
        // Solo indexamos la palabra si no se considera stop word dinámicamente
        if (!StopWords.isStopWord(palavra)) {
            addToIndex(palavra, url);
        }
    }

    // Extraemos título y snippet
    String title = conteudo.split("\\n")[0]; // Primera línea como título
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
    
    // NUEVO: Registra un enlace entrante (linkingUrl enlaza a targetUrl)
    @Override
    public void addInboundLink(String targetUrl, String linkingUrl) throws RemoteException {
        inboundLinks.computeIfAbsent(targetUrl, k -> new HashSet<>()).add(linkingUrl);
    }
    
    // NUEVO: Devuelve la lista de URLs que enlazan a la URL dada
    @Override
    public Set<String> getInboundLinks(String url) throws RemoteException {
        return inboundLinks.getOrDefault(url, Set.of());
    }
    
    // NUEVO: Búsqueda que retorna los URLs para una palabra, ordenados por importancia
    // (número de enlaces entrantes en orden descendente)
    @Override
public Set<String> searchOrderedByRelevance(String word) throws RemoteException {
    Set<String> urls = search(word);
    // Ordenamos usando el tamaño de inboundLinks y capturamos RemoteException en la lambda
    return urls.stream()
            .sorted((u1, u2) -> {
                try {
                    return Integer.compare(
                        getInboundLinks(u2).size(),
                        getInboundLinks(u1).size());
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            })
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
