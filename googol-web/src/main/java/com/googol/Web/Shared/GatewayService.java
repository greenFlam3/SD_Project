package com.googol.Web.Shared;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface GatewayService extends Remote {
    void indexPage(String url, String title, String text) throws RemoteException;
    List<String> smartSearch(String userQuery) throws RemoteException;
    PageInfo getPageSummary(String url) throws RemoteException;
    List<String> getTopSearches() throws RemoteException;
}