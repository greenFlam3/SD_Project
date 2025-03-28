package com.googol.Gateway;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;

import com.googol.Storage.StorageBarrel;

public class GatewayImpl extends UnicastRemoteObject implements GatewayService {

    // Para simplificar, utilizamos un único Storage Barrel.
    private StorageBarrel storageBarrel;

    public GatewayImpl(StorageBarrel storageBarrel) throws RemoteException {
        super();
        this.storageBarrel = storageBarrel;
    }

    @Override
    public void indexPage(String url, String title, String text) throws RemoteException {
        // Delegar al Storage Barrel
        String combinedContent = title + " " + text;
        storageBarrel.armazenarPagina(url, combinedContent);
    }

    @Override
    public Set<String> search(String query) throws RemoteException {
        return storageBarrel.search(query);
    }
}
