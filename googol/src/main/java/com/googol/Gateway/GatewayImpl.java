package com.googol.Gateway;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import com.googol.Storage.StorageBarrel;

/**
 * API de Gateway que delega en una lista de StorageBarrel pasada por constructor.
 */
public class GatewayImpl extends UnicastRemoteObject implements GatewayService {
    private static final long serialVersionUID = 1L;
    private final List<StorageBarrel> storageBarrels;

    /**
     * Constructor: recibe directamente la lista de StorageBarrel
     * en lugar de hacer lookup interno.
     */
    public GatewayImpl(List<StorageBarrel> barrels) throws RemoteException {
        super();
        this.storageBarrels = barrels;
    }

    @Override
    public void indexPage(String url, String title, String text) throws RemoteException {
        String combinedContent = title + "\n" + text;
        for (StorageBarrel barrel : storageBarrels) {
            try {
                barrel.armazenarPagina(url, combinedContent);
                System.out.println("[Gateway] Página indexada en " + barrel);
            } catch (RemoteException e) {
                System.err.println("[Gateway] Error al indexar en un barrel: " + e.getMessage());
            }
        }
    }

    @Override
    public Set<String> search(String query) throws RemoteException {
        Set<String> result = new HashSet<>();
        for (StorageBarrel barrel : storageBarrels) {
            try {
                result.addAll(barrel.search(query));
            } catch (RemoteException e) {
                System.err.println("[Gateway] Error al buscar en un barrel: " + e.getMessage());
            }
        }
        return result;
    }
}
