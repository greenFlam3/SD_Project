package com.googol.Web.Service;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.googol.Web.Model.SearchResult;
import com.googol.Web.Shared.GatewayService;
import com.googol.Web.Shared.PageInfo;


@Service
public class SearchService {

    private GatewayService gateway;

    public SearchService() throws Exception {
        gateway = (GatewayService) Naming.lookup("rmi://localhost:1099/GatewayService");
    }

    public List<SearchResult> search(String query) throws RemoteException {
        List<String> urls = gateway.smartSearch(query);
        List<SearchResult> results = new ArrayList<>();

        for (String url : urls) {
            PageInfo info = gateway.getPageSummary(url);
            SearchResult result = new SearchResult(info.getTitle(), url, info.getCitation());
            results.add(result);
        }

        return results;
    }

    public void indexUrl(String url) throws RemoteException {
        String dummyTitle = "Submitted by user";
        String dummyText = "";  // or fetch the page content if needed
        gateway.indexPage(url, dummyTitle, dummyText);
    }

}