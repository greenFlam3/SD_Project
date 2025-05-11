package com.googol.Web.Controller;

import java.rmi.RemoteException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.googol.Web.Model.SearchResult;
import com.googol.Web.Service.SearchService;

@Controller
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @PostMapping("/search")
    public String search(@RequestParam("query") String query, Model model) {
        try {
            List<SearchResult> results = searchService.search(query);
            model.addAttribute("results", results);
        } catch (RemoteException e) {
            // Optionally: log error, add a user-friendly message
            model.addAttribute("error", "Failed to fetch results. Please try again.");
        }
        return "results";
    }

    @PostMapping("/index")
    public String indexUrl(@RequestParam("url") String url) {
        try {
            searchService.indexUrl(url);
        } catch (RemoteException e) {
            // Optionally handle/log the error
            System.err.println("Failed to index URL: " + e.getMessage());
        }
        return "redirect:/";
    }
}