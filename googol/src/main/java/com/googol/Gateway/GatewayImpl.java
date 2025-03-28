package com.googol.Gateway;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.googol.Storage.StorageBarrel;

public class GatewayImpl extends UnicastRemoteObject implements GatewayService {

    private static final int NUMBER_OF_BARRELS = 3;
    private static final int RMI_PORT = 1055;
    private List<StorageBarrel> storageBarrels;

    public GatewayImpl() throws RemoteException {
        super();
        storageBarrels = new ArrayList<>();
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", RMI_PORT);
            for (int i = 1; i <= NUMBER_OF_BARRELS; i++) {
                try {
                    StorageBarrel barrel = (StorageBarrel) registry.lookup("StorageBarrel" + i);
                    storageBarrels.add(barrel);
                    System.out.println("[Gateway] Conectado ao " + "StorageBarrel" + i);
                } catch (Exception e) {
                    System.err.println("[Gateway] Erro ao conectar com StorageBarrel" + i + ": " + e.getMessage());
                }
            }
        } catch(Exception e) {
            System.err.println("[Gateway] Falha ao conectar-se ao RMI Registry: " + e.getMessage());
        }
    }

    @Override
    public void indexPage(String url, String title, String text) throws RemoteException {
        String combinedContent = title + "\n" + text;
        for (StorageBarrel barrel : storageBarrels) {
            try {
                barrel.armazenarPagina(url, combinedContent);
                System.out.println("[Gateway] Página indexada em um StorageBarrel.");
            } catch (RemoteException e) {
                System.err.println("[Gateway] Erro ao indexar em um barrel: " + e.getMessage());
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
                System.err.println("[Gateway] Erro ao buscar em um barrel: " + e.getMessage());
            }
        }
        return result;
    }
}