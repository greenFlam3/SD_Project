package com.googol.Gateway;

import java.io.Serializable;

public class BarrelStat implements Serializable {
    private final String name;
    private final int indexSize;
    private final double avgSearchMs;
    private final double avgIndexMs;

    public BarrelStat(String name, int indexSize, double avgSearchMs, double avgIndexMs) {
        this.name       = name;
        this.indexSize  = indexSize;
        this.avgSearchMs= avgSearchMs;
        this.avgIndexMs = avgIndexMs;
    }
    public String getName()        { return name; }
    public int    getIndexSize()   { return indexSize; }
    public double getAvgSearchMs() { return avgSearchMs; }
    public double getAvgIndexMs()  { return avgIndexMs; }
}