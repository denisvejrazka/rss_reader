package com.example.rss_application;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Parser {
    public static List<RssItem> parse(InputStream inputStream) throws Exception {
        List<RssItem> items = new ArrayList<>();
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(false);
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(inputStream, "UTF_8");

        String title = "", link = "", pubDate = "", description = "";
        boolean insideItem = false;

        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                if (xpp.getName().equalsIgnoreCase("item")) {
                    insideItem = true;
                } else if (insideItem) {
                    switch (xpp.getName()) {
                        case "title":
                            title = xpp.nextText(); break;
                        case "link":
                            link = xpp.nextText(); break;
                        case "pubDate":
                            pubDate = xpp.nextText(); break;
                        case "description":
                            description = xpp.nextText(); break;
                    }
                }
            } else if (eventType == XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("item")) {
                items.add(new RssItem(title, link, pubDate, description));
                insideItem = false;
            }
            eventType = xpp.next();
        }

        return items;
    }
}