package com.googol.Client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import com.googol.Gateway.BarrelStat;
import com.googol.Gateway.GatewayService;
import com.googol.Storage.PageInfo;

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
                System.out.println("2. Simple search (one term)");
                System.out.println("3. AND search (multiple terms)");
                System.out.println("4. Show statistics");
                System.out.println("5. Salir");
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
                            System.out.println("No pages found for \"" + query + "\".");
                            break;
                        }

                        // --- Pagination setup ---
                        List<String> all = new ArrayList<>(results);
                        int pageSize = 10;
                        int totalPages = (all.size() + pageSize - 1) / pageSize;
                        int currentPage = 0;

                        while (true) {
                            int start = currentPage * pageSize;
                            int end = Math.min(start + pageSize, all.size());
                            System.out.printf("\nShowing results %d–%d of %d (Page %d/%d):\n",
                                start+1, end, all.size(), currentPage+1, totalPages);

                            for (int i = start; i < end; i++) {
                                String resultUrl = all.get(i);
                                PageInfo info = gateway.getPageSummary(resultUrl);
                                System.out.println("\nTítulo:  " + info.getTitle());
                                System.out.println("URL:      " + resultUrl);
                                System.out.println("Snippet:  " + info.getSnippet());
                            }

                            // Navigation prompt
                            System.out.print("\nCommands: [n]ext, [p]rev, [e]xit: ");
                            String cmd = scanner.nextLine().trim().toLowerCase();
                            if (cmd.equals("n") && currentPage < totalPages-1) {
                                currentPage++;
                            } else if (cmd.equals("p") && currentPage > 0) {
                                currentPage--;
                            } else if (cmd.equals("e")) {
                                break;
                            } else {
                                System.out.println("Invalid command or no more pages.");
                            }
                        }
                        break;


                    case 3:
                        System.out.print("Introduce varios términos (separados por espacios): ");
                        String multi = scanner.nextLine().trim();
                        if (multi.isEmpty()) {
                            System.out.println("Error: la consulta no puede estar vacía.");
                            break;
                        }
                        // Parse into a Set<String>
                        Set<String> terms = new HashSet<>(Arrays.asList(multi.split("\\s+")));

                        // Call the new RPC
                        Set<String> andResults = gateway.searchMultipleTerms(terms);
                        if (andResults.isEmpty()) {
                            System.out.println("No pages contain *all* of those terms.");
                            break;
                        }
                        System.out.println("Resultados AND‐search:");
                        for (String resultUrl : andResults) {
                            PageInfo info = gateway.getPageSummary(resultUrl);
                            System.out.println("\nTítulo:  " + info.getTitle());
                            System.out.println("URL:      " + resultUrl);
                            System.out.println("Snippet:  " + info.getSnippet());
                        }
                        break;
                    
                    case 4:
                        System.out.println("\nTop 10 search terms:");
                        List<String> top = gateway.getTopSearches();
                        top.forEach(System.out::println);

                        System.out.println("\nBarrel statistics:");
                        List<BarrelStat> bs = gateway.getBarrelStats();
                        for (BarrelStat s : bs) {
                            System.out.printf(
                            "%s — index size: %d pages; avgSearch=%.2fms; avgIndex=%.2fms\n",
                            s.getName(), s.getIndexSize(), s.getAvgSearchMs(), s.getAvgIndexMs()
                            );
                        }
                        break;
                    
                    case 5:
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
