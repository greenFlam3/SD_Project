package com.googol.Storage;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

public interface StorageBarrel extends Remote {
    
    // Adiciona uma palavra ao índice invertido associada a um URL
    void addToIndex(String word, String url) throws RemoteException;

    // Pesquisa uma palavra e retorna os URLs onde ela ocorre
    Set<String> search(String word) throws RemoteException;
}