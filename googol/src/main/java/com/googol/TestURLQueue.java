package com.googol;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import com.googol.Queue.URLQueueInterface;

public class TestURLQueue {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1088);
            URLQueueInterface queue = (URLQueueInterface) registry.lookup("URLQueue");

            queue.addURL("https://pt.wikipedia.org/wiki/Ferrari"); // URL de teste

            System.out.println("URL added to queue!");
            System.out.println("Tamaño ahora: " + queue.getQueueSize());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}