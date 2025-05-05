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
            // 1) Conectar al RMI Registry donde están los StorageBarrel
            Registry registry = LocateRegistry.getRegistry(HOST, PORT);

            // 2) Intentar obtener cada réplica de StorageBarrel
            List<StorageBarrel> barrels = Arrays.asList(
                (StorageBarrel) registry.lookup("StorageBarrel1"),
                (StorageBarrel) registry.lookup("StorageBarrel2"), 
                (StorageBarrel) registry.lookup("StorageBarrel3")
            );

            // 3) Ejecutar search en cada barrel y mostrar resultados
            String query = "Ferrari";
            System.out.println("Resultados de búsqueda para '" + query + "':");
            for (StorageBarrel barrel : barrels) {
                System.out.println(" - En " + barrel + ": " + barrel.search(query));
            }

        } catch (Exception e) {
            System.err.println("[TestSearch] Error al buscar en StorageBarrel: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
