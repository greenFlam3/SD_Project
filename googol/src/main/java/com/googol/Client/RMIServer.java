package com.googol.Client;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

import com.googol.Gateway.GatewayImpl;
import com.googol.Storage.StorageBarrel;
import com.googol.Storage.StorageBarrelImpl;

public class RMIServer {
    private static final int RMI_PORT = 1055;
    private static final int NUMBER_OF_BARRELS = 3; // Pode ser ajustado dinamicamente

    public static void main(String[] args) {
        try {
            // Criar o registro RMI na porta especificada
            Registry registry = LocateRegistry.createRegistry(RMI_PORT);
            
            List<StorageBarrel> barrels = new ArrayList<>();
            
            // Criar e registrar cada StorageBarrel
            for (int i = 1; i <= NUMBER_OF_BARRELS; i++) {
                StorageBarrelImpl barrel = new StorageBarrelImpl(i);
                registry.rebind("StorageBarrel" + i, barrel);
                barrels.add(barrel);
                System.out.println("[Barrel " + i + "] registrado no Registry.");
            }
            
            // Criar a instância do Gateway que buscará todas as réplicas
            //GatewayImpl gateway = new GatewayImpl(); // GatewayImpl buscará "StorageBarrel1", "StorageBarrel2", etc.
            //registry.rebind("GatewayService", gateway);
            
            System.out.println("Servidor RMI em funcionamento. GatewayService está registrado.");
            
            // Mantém o servidor ativo
            while (true) {
                Thread.sleep(10000);
                // Opcional: imprimir o estado global, ex.: quantidade de páginas indexadas em cada barrel.
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
