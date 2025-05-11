package com.googol.Web.Model;

public class SearchResult {
    private String title;
    private String url;
    private String citation;

    // Constructor
    public SearchResult(String title, String url, String citation) {
        this.title = title;
        this.url = url;
        this.citation = citation;
    }

    // Empty constructor (needed for frameworks like Spring/Thymeleaf)
    public SearchResult() {}

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCitation() {
        return citation;
    }

    public void setCitation(String citation) {
        this.citation = citation;
    }
}