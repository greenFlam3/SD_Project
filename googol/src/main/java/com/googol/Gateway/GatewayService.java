package com.googol.Gateway;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

import com.googol.Storage.PageInfo;

public interface GatewayService extends Remote {
    void indexPage(String url, String title, String text) throws RemoteException;
    Set<String> search(String query) throws RemoteException;
    PageInfo getPageSummary(String url) throws RemoteException;
    Set<String> searchMultipleTerms(Set<String> terms) throws RemoteException;
    List<String> getTopSearches() throws RemoteException;
    List<BarrelStat> getBarrelStats() throws RemoteException;
}