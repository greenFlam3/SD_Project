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
        invertedIndex.computeIfAbsent(word, k -> new HashSet<>()).add(url);
    }

    /**
     * Pesquisa uma palavra e retorna os URLs onde ela ocorre.
     */
    @Override
    public Set<String> search(String word) throws RemoteException {
        return invertedIndex.getOrDefault(word, Set.of()); 
        // Si usas Java 8 o anterior, reemplaza Set.of() por Collections.emptySet()
    }

    /**
     * Armazena o conteúdo de uma página (URL) e indexa cada palavra.
     */
    @Override
    public void armazenarPagina(String url, String conteudo) throws RemoteException {
        String[] palavras = conteudo.toLowerCase().split("\\s+");
        for (String palavra : palavras) {
            // Reutilizamos a lógica de addToIndex
            addToIndex(palavra, url);
        }
        System.out.println("[Barrel " + id + "] Página armazenada: " + url);
    }

    /**
     * Pesquisa um termo (funciona como search, mas aqui mostramos logs distintos).
     */
    @Override
    public Set<String> buscarPagina(String termo) throws RemoteException {
        System.out.println("[Barrel " + id + "] Pesquisando termo: " + termo);
        return invertedIndex.getOrDefault(termo.toLowerCase(), Set.of());
    }

    /**
     * Retorna o total de páginas distintas indexadas.
     * Para isso, percorremos os valores do mapa e contamos todos os URLs.
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
