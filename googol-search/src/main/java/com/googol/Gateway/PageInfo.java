package com.googol.Gateway;

import java.io.Serializable;

public class PageInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String title;
    private final String snippet;

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

    public String getCitation() {
        return snippet;
    }
}
