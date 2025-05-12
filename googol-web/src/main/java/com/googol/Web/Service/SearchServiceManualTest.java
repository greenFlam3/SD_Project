package com.googol.Web.Service;

import java.rmi.RemoteException;
import java.util.List;

import com.googol.Web.Model.SearchResult;

public class SearchServiceManualTest {

    public static void main(String[] args) {
        try {
            SearchService service = new SearchService();

            // Test search
            System.out.println("Searching for: 'encyclopedia'");
            List<SearchResult> results = service.search("encyclopedia");

            for (SearchResult result : results) {
                System.out.println("Title: " + result.getTitle());
                System.out.println("URL: " + result.getUrl());
                System.out.println("Snippet: " + result.getCitation());
                System.out.println("----");
            }

            // Test index (optional)
            System.out.println("Indexing a test URL...");
            //service.indexUrl("https://example.com");

            System.out.println("Manual test completed successfully.");

        } catch (RemoteException e) {
            System.err.println("RemoteException: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}