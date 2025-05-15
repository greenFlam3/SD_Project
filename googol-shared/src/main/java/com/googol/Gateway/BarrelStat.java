package com.googol.Gateway;

import java.io.Serializable;

public class BarrelStat implements Serializable {
    private static final long serialVersionUID = 1L;
    private String barrelId;
    private int indexedPages;
    private double averageResponseTime;

    public BarrelStat(String barrelId, int indexedPages, double averageResponseTime) {
        this.barrelId = barrelId;
        this.indexedPages = indexedPages;
        this.averageResponseTime = averageResponseTime;
    }

    public String getBarrelId() {
        return barrelId;
    }

    public int getIndexedPages() {
        return indexedPages;
    }

    public double getAverageResponseTime() {
        return averageResponseTime;
    }

    @Override
    public String toString() {
        return "BarrelStat{" +
               "barrelId='" + barrelId + '\'' +
               ", indexedPages=" + indexedPages +
               ", averageResponseTime=" + averageResponseTime +
               '}';
    }
}
