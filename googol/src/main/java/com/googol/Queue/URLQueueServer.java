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
        System.out.println("Adicionando URL: " + url);
        if (!processedURLs.contains(url) && !queue.contains(url)) {  // Evita URLs duplicados
            queue.offer(url);
            System.out.println("URL adicionada: " + url);
        } else {
            System.out.println("URL já está na fila ou já foi processada: " + url);
        }
    }

    @Override
    public String getNextURL() throws RemoteException {
        System.out.println("Obtendo próxima URL...");
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
            // Criar ou usar o Registry no porto 1088
            Registry registry = LocateRegistry.createRegistry(1088);
            
            // Criar e registrar o URLQueueServer
            URLQueueServer server = new URLQueueServer();
            registry.rebind("URLQueue", server);
            System.out.println("URLQueueServer is ready...");
            
            // Manter o servidor ativo
            while (true) {
                Thread.sleep(10000);
            }
        } catch (Exception e) {
            System.err.println("Error starting server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}