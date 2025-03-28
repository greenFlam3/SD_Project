package com.googol.Gateway;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

public interface GatewayService extends Remote {
    void indexPage(String url, String title, String text) throws RemoteException;
    Set<String> search(String query) throws RemoteException;
}
