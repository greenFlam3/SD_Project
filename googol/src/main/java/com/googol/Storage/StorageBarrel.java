package com.googol.Storage;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

public interface StorageBarrel extends Remote {

    // Adiciona uma palavra ao índice invertido associada a um URL
    void addToIndex(String word, String url) throws RemoteException;

    // Pesquisa uma palavra e retorna os URLs onde ela ocorre
    Set<String> search(String word) throws RemoteException;

    // Armazena o conteúdo de uma página (separa em palavras e as indexa)
    void armazenarPagina(String url, String conteudo) throws RemoteException;

    // Pesquisa um termo (semelhante a search, mas pode diferir na lógica)
    Set<String> buscarPagina(String termo) throws RemoteException;

    // Retorna o total de páginas distintas indexadas
    int getTotalPaginas() throws RemoteException;
}
