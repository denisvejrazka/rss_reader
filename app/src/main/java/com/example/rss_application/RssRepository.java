package com.example.rss_application;

import android.os.Handler;
import android.os.Looper;
import android.content.Context;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class RssRepository {

    private final RssDatabaseHelper dbHelper;

    public interface Callback {
        void onSuccess(List<RssItem> items);
        void onError(Exception e);
    }

    public RssRepository(Context context) {
        dbHelper = new RssDatabaseHelper(context);
    }

    // Načítání RSS kanálu a jeho uložení do databáze
    public void loadRss(String urlString, Callback callback) {
        new Thread(() -> {
            try {
                String response = loadRssFromUrl(urlString);
                if (response == null || !response.trim().startsWith("<?xml")) {
                    throw new Exception("Response is not valid XML.");
                }

                List<RssItem> items = parseRssFeed(response);
                saveRssItemsToDatabase(items);
                postSuccess(callback, items);
            } catch (Exception e) {
                postError(callback, e);
            }
        }).start();
    }

    // Otevření připojení k URL a načítání odpovědi jako text
    private String loadRssFromUrl(String urlString) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        conn.setRequestProperty("Referer", "https://www.idnes.cz");
        conn.connect();

        // Handle redirects if needed
        if (conn.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP || conn.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM) {
            conn = (HttpURLConnection) new URL(conn.getHeaderField("Location")).openConnection();
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder responseBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            responseBuilder.append(line).append("\n");
        }

        String response = responseBuilder.toString();
        Log.d("RSS_RAW", response);
        return response;
    }

    // Parsování RSS feedu
    private List<RssItem> parseRssFeed(String rssFeed) throws Exception {
        return Parser.parse(new ByteArrayInputStream(rssFeed.getBytes(StandardCharsets.UTF_8)));
    }

    // Uložení položek do databáze
    private void saveRssItemsToDatabase(List<RssItem> items) {
        dbHelper.open();
        for (RssItem item : items) {
            dbHelper.saveRssItem(item);
        }
        dbHelper.close();
    }

    // Odeslání úspěšného výsledku na hlavní vlákno
    private void postSuccess(Callback callback, List<RssItem> items) {
        new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(items));
    }

    // Odeslání chyby na hlavní vlákno
    private void postError(Callback callback, Exception e) {
        new Handler(Looper.getMainLooper()).post(() -> callback.onError(e));
    }
}