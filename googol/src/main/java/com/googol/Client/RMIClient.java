package com.googol.Client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Scanner;

import com.googol.Gateway.BarrelStat;
import com.googol.Gateway.GatewayService;
import com.googol.Queue.URLQueueInterface;
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
                System.out.println("2. Search");
                System.out.println("3. Show statistics");
                System.out.println("4. Exit");
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

                        gateway.indexPage(url, title, text);
                        System.out.println("Page indexed successfully.");

                        // 2) Also enqueue it for the downloader
                        try {
                            Registry queueReg = LocateRegistry.getRegistry(HOST, 1088);
                            URLQueueInterface queue = (URLQueueInterface) queueReg.lookup("URLQueue");
                            queue.addURL(url);
                            System.out.println("URL queued for crawling.");
                            } catch (NotBoundException | RemoteException e) {
                                System.err.println("Failed to enqueue URL: " + e.getMessage());
                            }
                        break;

                        case 2:
                            System.out.print("Enter search query: ");
                            String queryInput = scanner.nextLine().trim();
                            if (queryInput.isEmpty()) {
                                System.out.println("Query cannot be empty.");
                                break;
                            }
                        
                            List<String> results = gateway.smartSearch(queryInput);
                            if (results.isEmpty()) {
                                System.out.println("No pages found.");
                                break;
                            }
                        
                            System.out.println("\nSearch Results (ranked by relevance):");
                            for (String resultUrl : results) {
                                PageInfo info = gateway.getPageSummary(resultUrl);
                                System.out.println("\nTitle:   " + info.getTitle());
                                System.out.println("URL:     " + resultUrl);
                                System.out.println("Snippet: " + info.getSnippet());
                            }
                            break;                    

                    case 3:
                        System.out.println("\nTop 10 search terms:");
                        List<String> top = gateway.getTopSearches();
                        top.forEach(System.out::println);

                        System.out.println("\nBarrel statistics:");
                        List<BarrelStat> stats = gateway.getBarrelStats();
                        for (BarrelStat s : stats) {
                            System.out.printf(
                                "%s — index size: %d pages; avgSearch=%.2fms; avgIndex=%.2fms\n",
                                s.getName(), s.getIndexSize(), s.getAvgSearchMs(), s.getAvgIndexMs()
                            );
                        }
                        break;

                    case 4:
                        System.out.println("Exiting...");
                        return;

                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            }

        } catch (NotBoundException e) {
            System.err.println(
                "[RMIClient] Service '" + SERVICE_NAME + "' not found at " + HOST + ":" + PORT
            );
            System.err.println("Make sure GatewayServer is running on that port.");
        } catch (RemoteException e) {
            System.err.println("[RMIClient] RMI connection error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("[RMIClient] Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}