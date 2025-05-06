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
 * Servidor RMI para la cola de URLs, con creación o reutilización del Registry.
 */
public class URLQueueServer extends UnicastRemoteObject implements URLQueueInterface {
    private static final long serialVersionUID = 1L;
    private static final int RMI_PORT = 1088;  // Puerto destinado para URLQueue

    private final Queue<String> queue = new ConcurrentLinkedQueue<>();
    private final Set<String> processedURLs = ConcurrentHashMap.newKeySet();

    public URLQueueServer() throws RemoteException {
        super();
    }

    @Override
    public void addURL(String url) throws RemoteException {
        System.out.println("Adicionando URL: " + url);
        if (!processedURLs.contains(url) && !queue.contains(url)) {
            queue.offer(url);
            System.out.println("URL adicionada: " + url);
        } else {
            System.out.println("URL ya está en la fila o fue procesada: " + url);
        }
    }

    @Override
    public String getNextURL() throws RemoteException {
        System.out.println("Obteniendo próxima URL...");
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
                // Intentar crear un nuevo RMI Registry en el puerto 1088
                registry = LocateRegistry.createRegistry(RMI_PORT);
                System.out.println("[URLQueueServer] RMI Registry creado en el puerto " + RMI_PORT);
            } catch (ExportException e) {
                // Si ya existe, reutilizar el Registry existente
                registry = LocateRegistry.getRegistry(RMI_PORT);
                System.out.println("[URLQueueServer] Usando Registry existente en el puerto " + RMI_PORT);
            }

            // Registrar este servicio URLQueue en el Registry
            URLQueueServer server = new URLQueueServer();
            registry.rebind("URLQueue", server);
            System.out.println("[URLQueueServer] URLQueueServer listo y registrado como 'URLQueue'.");

            // Mantener el servidor activo indefinidamente
            Thread.sleep(Long.MAX_VALUE);

        } catch (Exception e) {
            System.err.println("[URLQueueServer] Error al inicializar: " + e.getMessage());
            e.printStackTrace();
        }
    }
}