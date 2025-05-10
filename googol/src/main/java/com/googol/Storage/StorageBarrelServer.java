package com.googol.Storage;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class StorageBarrelServer {
    private static final int RMI_PORT = 1099;
    private static final int NUMBER_OF_BARRELS = 3;

    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.createRegistry(RMI_PORT);
            List<StorageBarrelImpl> barrels = new ArrayList<>();

            for (int i = 1; i <= NUMBER_OF_BARRELS; i++) {
                StorageBarrelImpl barrel = new StorageBarrelImpl(i);
                String name = "StorageBarrel" + i;
                registry.rebind(name, barrel);
                barrels.add(barrel); // Save for debug use
                System.out.println("[StorageBarrelServer] " + name + " registered.");
            }

            System.out.println("[StorageBarrelServer] All barrels are ready. Awaiting connections...");

            // Schedule debug print after 10 seconds
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    System.out.println("=== DEBUG: Checking stored pages in all barrels ===");
                    for (StorageBarrelImpl barrel : barrels) {
                        System.out.println(">>> Barrel " + barrel.getId());
                        barrel.debugPrintStoredPages();
                    }
                }
            }, 10000); // 10 seconds

            Thread.sleep(Long.MAX_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}