package com.googol;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.List;

import com.googol.Storage.StorageBarrel;

public class TestSearch {
    private static final String HOST = "localhost";
    private static final int PORT = 1099;
    private static final int NUMBER_OF_BARRELS = 3;

    public static void main(String[] args) {
        try {
            // 1) Connect to the RMI registry where StorageBarrels are registered
            Registry registry = LocateRegistry.getRegistry(HOST, PORT);

            // 2) Lookup each StorageBarrel replica
            List<StorageBarrel> barrels = Arrays.asList(
                (StorageBarrel) registry.lookup("StorageBarrel1"),
                (StorageBarrel) registry.lookup("StorageBarrel2"), 
                (StorageBarrel) registry.lookup("StorageBarrel3")
            );

            // 3) Execute search on each barrel and display results
            String query = "Ferrari";
            System.out.println("Search results for '" + query + "':");
            for (StorageBarrel barrel : barrels) {
                System.out.println(" - On " + barrel + ": " + barrel.search(query));
            }

        } catch (Exception e) {
            System.err.println("[TestSearch] Error searching StorageBarrel: " + e.getMessage());
            e.printStackTrace();
        }
    }
}