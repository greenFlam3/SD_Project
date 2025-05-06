package com.googol.Storage;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class StorageBarrelServer {
    private static final int RMI_PORT = 1099;
    private static final int NUMBER_OF_BARRELS = 3;

    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.createRegistry(RMI_PORT);
            for (int i = 1; i <= NUMBER_OF_BARRELS; i++) {
                StorageBarrelImpl barrel = new StorageBarrelImpl(i);
                String name = "StorageBarrel" + i;
                registry.rebind(name, barrel);
                System.out.println("[StorageBarrelServer] " + name + " registrado.");
            }
            System.out.println("[StorageBarrelServer] Todos los barrels están listos. Esperando conexiones...");
            Thread.sleep(Long.MAX_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}