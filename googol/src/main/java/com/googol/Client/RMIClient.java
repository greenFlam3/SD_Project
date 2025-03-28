package com.googol.Client;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import java.util.Set;

import com.googol.Gateway.GatewayService;

public class RMIClient {
    public static void main(String[] args) {
        try {
            // Conectar al registro RMI en localhost en el puerto 1099
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);

            // Buscar la referencia remota del Gateway
            GatewayService gateway = (GatewayService) registry.lookup("GatewayService");

            Scanner scanner = new Scanner(System.in);
            System.out.println("Cliente RMI del motor de búsqueda");

            while(true) {
                System.out.println("\nOpciones:");
                System.out.println("1. Indexar una página (simulación)");
                System.out.println("2. Realizar una búsqueda");
                System.out.println("3. Salir");
                System.out.print("Seleccione opción: ");
                int option = scanner.nextInt();
                scanner.nextLine(); // Consumir salto de línea

                if(option == 1) {
                    System.out.print("Introduce URL: ");
                    String url = scanner.nextLine();
                    System.out.print("Introduce título: ");
                    String title = scanner.nextLine();
                    System.out.print("Introduce contenido de la página: ");
                    String text = scanner.nextLine();
                    
                    // Invocar el método remoto para indexar la página
                    gateway.indexPage(url, title, text);
                    System.out.println("Página indexada.");
                } else if(option == 2) {
                    System.out.print("Introduce términos de búsqueda: ");
                    String query = scanner.nextLine();
                    Set<String> results = gateway.search(query);
                    if(results.isEmpty()) {
                        System.out.println("No se encontraron páginas con esos términos.");
                    } else {
                        System.out.println("Resultados encontrados:");
                        for(String res : results) {
                            System.out.println(res);
                        }
                    }
                } else if(option == 3) {
                    System.out.println("Saliendo...");
                    break;
                } else {
                    System.out.println("Opción no válida.");
                }
            }
            scanner.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
