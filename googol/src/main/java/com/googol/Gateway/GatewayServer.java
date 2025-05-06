package com.googol.Gateway;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

import com.googol.Storage.StorageBarrel;

/**
 * Servidor independiente para Gateway: 
 * 1) Recupera réplicas por RMI lookup (puerto 1099). 
 * 2) Construye el GatewayImpl con esa lista.
 * 3) Registra el servicio en el registry (puerto 1055).
 */
public class GatewayServer {
    private static final int STORAGE_RMI_PORT = 1099;
    private static final int GATEWAY_RMI_PORT = 1055;
    private static final int NUMBER_OF_BARRELS = 3;

    public static void main(String[] args) {
        try {
            // 1) Conectar al Registry donde están los StorageBarrels
            Registry storageRegistry = LocateRegistry.getRegistry("localhost", STORAGE_RMI_PORT);

            // 2) Hacer lookup de cada réplica
            List<StorageBarrel> barrels = new ArrayList<>();
            for (int i = 1; i <= NUMBER_OF_BARRELS; i++) {
                try {
                    StorageBarrel barrel = (StorageBarrel) storageRegistry.lookup("StorageBarrel" + i);
                    barrels.add(barrel);
                    System.out.println("[GatewayServer] Conectado a StorageBarrel" + i);
                } catch (Exception ex) {
                    System.err.println("[GatewayServer] Error al conectar con StorageBarrel" + i + ": " + ex.getMessage());
                }
            }

            // 3) Crear instancia de GatewayImpl pasándole la lista
            GatewayImpl gateway = new GatewayImpl(barrels);

            // 4) Registrar el servicio GatewayService en otro Registry
            Registry gatewayRegistry = LocateRegistry.createRegistry(GATEWAY_RMI_PORT);
            gatewayRegistry.rebind("GatewayService", gateway);
            System.out.println("[GatewayServer] GatewayService registrado en el Registry (puerto " + GATEWAY_RMI_PORT + ").");

        } catch (RemoteException e) {
            System.err.println("[GatewayServer] Error al inicializar el servicio: " + e.getMessage());
            e.printStackTrace();
        }
    }
}