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
    private static final int PORT = 1055;
    private static final String SERVICE_NAME = "GatewayService";

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            // Connect to the Gateway RMI registry
            Registry registry = LocateRegistry.getRegistry(HOST, PORT);
            GatewayService gateway = (GatewayService) registry.lookup(SERVICE_NAME);

            System.out.println("Search Engine RMI Client");

            while (true) {
                System.out.println("\nOptions:");
                System.out.println("1. Index a page (simulation)");
                System.out.println("2. Simple search (one term)");
                System.out.println("3. AND search (multiple terms)");
                System.out.println("4. Show statistics");
                System.out.println("5. Exit");
                System.out.print("Select option: ");

                if (!scanner.hasNextInt()) {
                    System.out.println("Invalid input. Please enter a number.");
                    scanner.next();
                    continue;
                }
                int option = scanner.nextInt();
                scanner.nextLine();

                switch (option) {
                    case 1:
                        System.out.print("Enter URL: ");
                        String url = scanner.nextLine().trim();
                        System.out.print("Enter title: ");
                        String title = scanner.nextLine().trim();
                        System.out.print("Enter page content: ");
                        String text = scanner.nextLine().trim();

                        if (url.isEmpty() || title.isEmpty() || text.isEmpty()) {
                            System.out.println("Error: All fields must be filled.");
                            break;
                        }

                        // Invocar el método remoto para indexar la página
                        gateway.indexPage(url, title, text);
                        System.out.println("Page indexed successfully.");
                        break;

                    case 2:
                        System.out.print("Enter search term: ");
                        String query = scanner.nextLine().trim();
                        if (query.isEmpty()) {
                            System.out.println("Error: Search term cannot be empty.");
                            break;
                        }

                        Set<String> results = gateway.search(query);
                        if (results.isEmpty()) {
                            System.out.println("No pages found for \"" + query + "\".");
                            break;
                        }

                        // Pagination setup
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
                                System.out.println("\nTitle:  " + info.getTitle());
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
                        System.out.print("Enter multiple terms (separated by spaces): ");
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
                        System.out.println("\nAND-search results:");
                        for (String resultUrl : andResults) {
                            PageInfo info = gateway.getPageSummary(resultUrl);
                            System.out.println("\nTitle:  " + info.getTitle());
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
                        System.out.println("Exiting...");
                        return;
                    
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            }

        } catch (NotBoundException e) {
            // Servicio no encontrado en el registry
            System.err.println("[RMIClient] Service '" + SERVICE_NAME + "' not found at " + HOST + ":" + PORT);
            System.err.println("Make sure GatewayServer is running on that port.");
        } catch (RemoteException e) {
            // Error de conexión RMI
            System.err.println("[RMIClient] RMI connection error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            // Otros errores
            System.err.println("[RMIClient] Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}