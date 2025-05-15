package com.googol.Storage;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;
import com.googol.Gateway.PageInfo;

public interface StorageBarrel extends Remote {

    // Adiciona uma palavra ao índice invertido associada a um URL
    void addToIndex(String word, String url) throws RemoteException;

    // Pesquisa uma palavra e retorna os URLs onde ela ocorre
    Set<String> search(String word) throws RemoteException;

    // Pesquisa múltiplos termos e retorna URLs que contenham todos eles
    Set<String> searchMultipleTerms(Set<String> terms) throws RemoteException;

    // Armazena o conteúdo de uma página (separa em palavras e as indexa)
    void armazenarPagina(String url, String conteudo) throws RemoteException;

    // Pesquisa um termo (semelhante a search, mas pode diferir na lógica)
    Set<String> buscarPagina(String termo) throws RemoteException;

    // Retorna o total de páginas distintas indexadas
    int getTotalPaginas() throws RemoteException;

    // Retorna um resumo da página com título e um trecho de texto
    PageInfo getPageSummary(String url) throws RemoteException;

    // NEW: Registra un enlace entrante: linkingUrl enlaza a targetUrl
    void addInboundLink(String targetUrl, String linkingUrl) throws RemoteException;
    
    // NEW: Devuelve la lista de URLs que enlazan a la URL dada (para consulta de enlaces entrantes)
    Set<String> getInboundLinks(String url) throws RemoteException;
    
    // NEW: Busca una palabra y retorna los URLs ordenados por importancia (cantidad de enlaces entrantes)
    Set<String> searchOrderedByRelevance(String word) throws RemoteException;
}