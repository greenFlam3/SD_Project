package com.googol.Queue;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface URLQueueInterface extends Remote {
    void addURL(String url) throws RemoteException;
    String getNextURL() throws RemoteException;
    boolean isEmpty() throws RemoteException;
    boolean containsURL(String url) throws RemoteException;  // Verifica se um URL já está na fila
    int getQueueSize() throws RemoteException;  // Retorna o tamanho da fila
}