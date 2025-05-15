package com.googol.Storage;

import java.io.Serializable;

public class BarrelStat implements Serializable {
    private static final long serialVersionUID = 1L;

    private String barrelId;
    private int indexedPages;
    private double averageSearchMs;
    private double averageIndexMs;

    public BarrelStat(String barrelId, int indexedPages, double averageSearchMs, double averageIndexMs) {
        this.barrelId = barrelId;
        this.indexedPages = indexedPages;
        this.averageSearchMs = averageSearchMs;
        this.averageIndexMs = averageIndexMs;
    }

    public String getName() {
        return barrelId;
    }

    public int getIndexSize() {
        return indexedPages;
    }

    public double getAvgSearchMs() {
        return averageSearchMs;
    }

    public double getAvgIndexMs() {
        return averageIndexMs;
    }
}