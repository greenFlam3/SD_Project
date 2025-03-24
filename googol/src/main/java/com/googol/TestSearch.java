package com.googol;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import com.googol.Storage.StorageBarrel;

public class TestSearch {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            StorageBarrel barrel = (StorageBarrel) registry.lookup("StorageBarrel");

            System.out.println("Search results for 'Ferrari': " + barrel.search("example"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}