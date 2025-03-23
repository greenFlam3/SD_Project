package com.googol.Storage;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StorageBarrelImpl extends UnicastRemoteObject implements StorageBarrel {

    private final Map<String, Set<String>> invertedIndex;
    private final int id; // Identificador do Storage Barrel

    public StorageBarrelImpl(int id) throws RemoteException {
        super();
        this.id = id;
        this.invertedIndex = new HashMap<>();
    }

    /**
     * Adiciona uma palavra (word) ao índice, associando-a ao URL especificado.
     */
    @Override
    public void addToIndex(String word, String url) throws RemoteException {
        invertedIndex.computeIfAbsent(word.toLowerCase(), k -> new HashSet<>()).add(url);
    }

    /**
     * Pesquisa uma palavra e retorna os URLs onde ela ocorre.
     */
    @Override
    public Set<String> search(String word) throws RemoteException {
        return invertedIndex.getOrDefault(word.toLowerCase(), Set.of());
    }

    /**
     * Pesquisa múltiplos termos e retorna apenas os URLs que contêm todos os termos.
     */
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

    /**
     * Pesquisa um termo (chama diretamente o método search()).
     */
    @Override
    public Set<String> buscarPagina(String termo) throws RemoteException {
        System.out.println("[Barrel " + id + "] Searching for term: " + termo);
        return search(termo);
    }

    /**
     * Armazena o conteúdo de uma página (URL) e indexa cada palavra.
     */
    @Override
    public void armazenarPagina(String url, String conteudo) throws RemoteException {
        String[] palavras = conteudo.toLowerCase().split("\\s+");
        for (String palavra : palavras) {
            addToIndex(palavra, url);
        }
        System.out.println("[Barrel " + id + "] Page stored: " + url);
    }

    /**
     * Retorna o total de páginas distintas indexadas.
     */
    @Override
    public int getTotalPaginas() throws RemoteException {
        Set<String> allUrls = new HashSet<>();
        for (Set<String> urls : invertedIndex.values()) {
            allUrls.addAll(urls);
        }
        return allUrls.size();
    }
}