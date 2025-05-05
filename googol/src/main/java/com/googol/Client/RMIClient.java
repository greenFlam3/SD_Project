package com.googol.Client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import java.util.Set;

import com.googol.Gateway.GatewayService;

public class RMIClient {
    private static final String HOST = "localhost";
    // Modificación: ajustar al puerto donde realmente corre GatewayService (1055)
    private static final int PORT = 1055;
    private static final String SERVICE_NAME = "GatewayService";

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            // 1) Conectar al RMI Registry del Gateway
            Registry registry = LocateRegistry.getRegistry(HOST, PORT);
            GatewayService gateway = (GatewayService) registry.lookup(SERVICE_NAME);

            System.out.println("Cliente RMI del motor de búsqueda");

            while (true) {
                System.out.println("\nOpciones:");
                System.out.println("1. Indexar una página (simulación)");
                System.out.println("2. Realizar una búsqueda");
                System.out.println("3. Salir");
                System.out.print("Seleccione opción: ");

                if (!scanner.hasNextInt()) {
                    System.out.println("Entrada no válida. Intente de nuevo.");
                    scanner.next();
                    continue;
                }
                int option = scanner.nextInt();
                scanner.nextLine();

                switch (option) {
                    case 1:
                        System.out.print("Introduce URL: ");
                        String url = scanner.nextLine().trim();
                        System.out.print("Introduce título: ");
                        String title = scanner.nextLine().trim();
                        System.out.print("Introduce contenido de la página: ");
                        String text = scanner.nextLine().trim();

                        if (url.isEmpty() || title.isEmpty() || text.isEmpty()) {
                            System.out.println("Error: Todos los campos deben estar llenos.");
                            break;
                        }

                        // Invocar el método remoto para indexar la página
                        gateway.indexPage(url, title, text);
                        System.out.println("Página indexada.");
                        break;

                    case 2:
                        System.out.print("Introduce términos de búsqueda: ");
                        String query = scanner.nextLine().trim();

                        if (query.isEmpty()) {
                            System.out.println("Error: La consulta no puede estar vacía.");
                            break;
                        }

                        Set<String> results = gateway.search(query);
                        if (results.isEmpty()) {
                            System.out.println("No se encontraron páginas con esos términos.");
                        } else {
                            System.out.println("Resultados encontrados:");
                            results.forEach(System.out::println);
                        }
                        break;

                    case 3:
                        System.out.println("Saliendo...");
                        return;

                    default:
                        System.out.println("Opción no válida.");
                }
            }

        } catch (NotBoundException e) {
            // Servicio no encontrado en el registry
            System.err.println("[RMIClient] Servicio '" + SERVICE_NAME + "' no encontrado en " + HOST + ":" + PORT);
            System.err.println("Asegúrate de haber arrancado GatewayServer en ese puerto.");
        } catch (RemoteException e) {
            // Error de conexión RMI
            System.err.println("[RMIClient] Error de conexión RMI: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            // Otros errores
            System.err.println("[RMIClient] Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
