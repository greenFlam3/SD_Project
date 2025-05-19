package com.googol.Client;

import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.List;

import com.googol.Gateway.GatewayImpl;
import com.googol.Storage.StorageBarrel;
import com.googol.Storage.StorageBarrelImpl;

public class RMIServer {
    private static final int RMI_PORT = 1055;
    private static final int NUMBER_OF_BARRELS = 3; // Can be adjusted dynamically

    public static void main(String[] args) {
        try {
            // Create the RMI registry on the specified port
            LocateRegistry.createRegistry(RMI_PORT);
            System.out.println("[Gateway] RMI registry created on port " + RMI_PORT);

            
            List<StorageBarrel> barrels = new ArrayList<>();
            
            // Create and register each StorageBarrel
            for (int i = 1; i <= NUMBER_OF_BARRELS; i++) {
                StorageBarrelImpl barrel = new StorageBarrelImpl(i);
                //registry.rebind("StorageBarrel" + i, barrel);
                barrels.add(barrel);
                System.out.println("[Barrel " + i + "] registered in the Registry.");
            }
            
            // Create the Gateway instance that will look up all replicas
            GatewayImpl gateway = new GatewayImpl(barrels);
            //registry.rebind("GatewayService", gateway);
            System.out.println("[Gateway] GatewayService registered in the Registry.");
            
            System.out.println("RMI Server is running. GatewayService is registered.");
            
            // Keep the server alive indefinitely
            while (true) {
                Thread.sleep(10000);
                // Optional: print global status, e.g.: number of pages indexed in each barrel.
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}