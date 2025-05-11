package com.googol.Web.Shared;

import java.io.Serializable;
import java.util.Objects;

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

    public String getCitation() {
        return snippet;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PageInfo pageInfo = (PageInfo) obj;
        return Objects.equals(title, pageInfo.title) && Objects.equals(snippet, pageInfo.snippet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, snippet);
    }

    @Override
    public String toString() {
        return "PageInfo{" +
               "title='" + title + '\'' +
               ", snippet='" + snippet + '\'' +
               '}';
    }
}