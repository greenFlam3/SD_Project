package com.googol.Queue;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class URLQueueServer implements URLQueueInterface {
    private final Queue<String> queue = new ConcurrentLinkedQueue<>();

    public URLQueueServer() throws RemoteException {
        super();
    }

    @Override
    public void addURL(String url) throws RemoteException {
        queue.offer(url);
        System.out.println("URL adicionada: " + url);
    }

    @Override
    public String getNextURL() throws RemoteException {
        return queue.poll();
    }

    @Override
    public boolean isEmpty() throws RemoteException {
        return queue.isEmpty();
    }

    public static void main(String[] args) {
        try {
            URLQueueServer server = new URLQueueServer();
            URLQueueInterface stub = (URLQueueInterface) UnicastRemoteObject.exportObject(server, 0);
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("URLQueue", stub);
            System.out.println("URLQueueServer pronto...");
        } catch (Exception e) {
            System.err.println("Erro ao iniciar o servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
