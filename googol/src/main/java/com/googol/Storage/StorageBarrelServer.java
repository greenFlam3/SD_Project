package com.googol.Storage;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import com.googol.Gateway.GatewayImpl;

public class StorageBarrelServer {
    public static void main(String[] args) {
        try {
            // Crear o usar el Registry en el puerto 1099
            Registry registry = LocateRegistry.createRegistry(1099);
            
            // Número de réplicas que queremos (puede ser 2, 3, o más)
            int numberOfBarrels = 3;
            
            // Crear y registrar cada StorageBarrelImpl
            for (int i = 1; i <= numberOfBarrels; i++) {
                StorageBarrelImpl barrel = new StorageBarrelImpl(i);
                String bindingName = "StorageBarrel" + i;
                registry.rebind(bindingName, barrel);
                System.out.println("[Barrel " + i + "] registrado como " + bindingName);
            }
            
            // Opcional: si queremos que la Gateway se inicie en este mismo proceso,
            // podemos crearla y registrarla.
            GatewayImpl gateway = new GatewayImpl();  // Nota: usa el constructor que hace lookup
            registry.rebind("GatewayService", gateway);
            System.out.println("GatewayService registrado en el Registry.");

            // Mantener el servidor activo
            while (true) {
                Thread.sleep(10000);
                // Opcional: imprimir estado o estadísticas de las réplicas.
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}