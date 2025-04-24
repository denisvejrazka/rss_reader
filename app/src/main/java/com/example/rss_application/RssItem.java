package com.example.rss_application;

public class RssItem {
    public String title;
    public String link;
    public String pubDate;
    public String description;

    public RssItem(String title, String link, String pubDate, String description) {
        this.title = title;
        this.link = link;
        this.pubDate = pubDate;
        this.description = description;
    }
}
