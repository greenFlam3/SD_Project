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

    @Override
    public void armazenarPagina(String url, String conteudo) throws RemoteException {
        String[] palavras = conteudo.toLowerCase().split("\\s+");
        for (String palavra : palavras) {
            invertedIndex.computeIfAbsent(palavra, k -> new HashSet<>()).add(url);
        }
        System.out.println("[Barrel " + id + "] Página armazenada: " + url);
    }

    @Override
    public Set<String> buscarPagina(String termo) throws RemoteException {
        System.out.println("[Barrel " + id + "] Pesquisando termo: " + termo);
        return invertedIndex.getOrDefault(termo.toLowerCase(), Set.of());
    }

    @Override
    public int getTotalPaginas() throws RemoteException {
        return invertedIndex.size();
    }
}
