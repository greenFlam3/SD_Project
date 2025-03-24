package com.googol.Storage;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class StorageBarrelServer {
    public static void main(String[] args) {
        try {
            int port = 1099; //Integer.parseInt(args[0]); // Porta passada como argumento
            int id = 44; //Integer.parseInt(args[1]);   // Identificador único do Storage Barrel

            StorageBarrel barrel = new StorageBarrelImpl(id);
            Registry registry = LocateRegistry.createRegistry(port);

            registry.rebind("StorageBarrel", barrel);
            System.out.println("[Barrel " + id + "] Ativo na porta " + port);

            // Exibir status a cada 10 segundos
            while (true) {
                Thread.sleep(10000);
                System.out.println("[Barrel " + id + "] Páginas indexadas: " + barrel.getTotalPaginas());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}