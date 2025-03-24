package com.googol.Storage;

import java.io.Serializable;

public class PageInfo implements Serializable {
    private String title;
    private String snippet;

    public PageInfo(String title, String snippet) {
        this.title = title;
        this.snippet = snippet;
    }

    public String getTitle() {
        return title;
    }

    public String getSnippet() {
        return snippet;
    }

    @Override
    public String toString() {
        return "Title: " + title + "\\nSnippet: " + snippet;
    }
}