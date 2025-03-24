package com.googol.Queue;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class URLQueueServer extends UnicastRemoteObject implements URLQueueInterface {
    private final Queue<String> queue = new ConcurrentLinkedQueue<>();
    private final Set<String> processedURLs = ConcurrentHashMap.newKeySet();  // URLs já processados

    public URLQueueServer() throws RemoteException {
        super();
    }

    @Override
    public void addURL(String url) throws RemoteException {
        System.out.println("Adding URL: " + url);
        if (!processedURLs.contains(url) && !queue.contains(url)) {  // Evita URLs duplicados
            queue.offer(url);
            System.out.println("URL added: " + url);
        } else {
            System.out.println("URL already in queue or processed: " + url);
        }
    }

    @Override
    public String getNextURL() throws RemoteException {
        System.out.println("Getting next URL...");
        String url = queue.poll();
        if (url != null) {
            processedURLs.add(url);  // Marca URL como processado
        }
        return url;
    }

    @Override
    public boolean isEmpty() throws RemoteException {
        return queue.isEmpty();
    }

    @Override
    public boolean containsURL(String url) throws RemoteException {
        return queue.contains(url);
    }

    @Override
    public int getQueueSize() throws RemoteException {
        return queue.size();
    }

    public static void main(String[] args) {
        try {
            URLQueueServer server = new URLQueueServer();
            Registry registry = LocateRegistry.createRegistry(1088);
            registry.rebind("URLQueue", server);
            System.out.println("URLQueueServer is ready...");
        } catch (Exception e) {
            System.err.println("Error starting server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}