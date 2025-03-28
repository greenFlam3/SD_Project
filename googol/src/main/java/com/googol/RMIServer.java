import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIServer {
    public static void main(String[] args) {
        try {
            // Crear la instancia del Storage Barrel
            StorageBarrelImpl storageBarrel = new StorageBarrelImpl(1);
            
            // Crear la instancia del Gateway, usando el Storage Barrel
            GatewayImpl gateway = new GatewayImpl(storageBarrel);

            // Crear el registro RMI en el puerto 1099
            Registry registry = LocateRegistry.createRegistry(1099);

            // Registrar el Gateway en el registro
            registry.rebind("GatewayService", gateway);

            System.out.println("Servidor RMI en funcionamiento. GatewayService está registrado.");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
