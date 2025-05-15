package com.googol.Queue;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * RMI server for the URL queue, with creation or reuse of the Registry.
 */
public class URLQueueServer extends UnicastRemoteObject implements URLQueueInterface {
    private static final long serialVersionUID = 1L;
    private static final int RMI_PORT = 1088;  // Port designated for URLQueue

    private final Queue<String> queue = new ConcurrentLinkedQueue<>();
    private final Set<String> processedURLs = ConcurrentHashMap.newKeySet();

    public URLQueueServer() throws RemoteException {
        super();
    }

    @Override
    public void addURL(String url) throws RemoteException {
        System.out.println("Adding URL: " + url);
        if (!processedURLs.contains(url) && !queue.contains(url)) {
            queue.offer(url);
            System.out.println("URL added: " + url);
        } else {
            System.out.println("URL already in the queue or processed: " + url);
        }
    }

    @Override
    public String getNextURL() throws RemoteException {
        System.out.println("Retrieving next URL...");
        String url = queue.poll();
        if (url != null) {
            processedURLs.add(url);
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
            Registry registry;
            try {
                // Attempt to create a new RMI Registry on port 1088
                registry = LocateRegistry.createRegistry(RMI_PORT);
                System.out.println("[URLQueueServer] RMI Registry created on port " + RMI_PORT);
            } catch (ExportException e) {
                // If one already exists, reuse the existing registry
                registry = LocateRegistry.getRegistry(RMI_PORT);
                System.out.println("[URLQueueServer] Using existing registry on port " + RMI_PORT);
            }

            // Register this URLQueue service in the registry
            URLQueueServer server = new URLQueueServer();
            registry.rebind("URLQueue", server);
            System.out.println("[URLQueueServer] URLQueueServer ready and registered as 'URLQueue'.");

            // Keep the server alive indefinitely
            Thread.sleep(Long.MAX_VALUE);

        } catch (Exception e) {
            System.err.println("[URLQueueServer] Initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}