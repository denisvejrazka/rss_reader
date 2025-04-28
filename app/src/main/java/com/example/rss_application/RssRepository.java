package com.example.rss_application;

import android.os.Handler;
import android.os.Looper;
import android.content.Context;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Network;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.io.BufferedReader;
import java.io.InputStreamReader;


public class RssRepository {

    private final RssDatabaseHelper dbHelper;
    private final Context context;
    public interface Callback {
        void onSuccess(List<RssItem> items);
        void onError(Exception e);
    }

    public RssRepository(Context context) {
        this.context = context;
        dbHelper = new RssDatabaseHelper(context);
    }

    public void loadRss(String urlString, Callback callback) {
        new Thread(() -> {
            try {
                List<RssItem> items;

                if (isNetworkAvailable()) {
                    String response = loadRssFromUrl(urlString);
                    if (response == null || !response.trim().startsWith("<?xml")) {
                        throw new Exception("Response is not valid XML.");
                    }

                    items = parseRssFeed(response);
                    saveRssItemsToDatabase(items);
                } else {
                    items = dbHelper.getRecentRssItems();
                    if (items.isEmpty()) {
                        throw new Exception("No internet connection and no offline data available.");
                    }
                }

                postSuccess(callback, items);
            } catch (Exception e) {
                postError(callback, e);
            }
        }).start();
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = cm.getActiveNetwork();
        if (network != null) {
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
            return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        }
        return false;
    }


    private String loadRssFromUrl(String urlString) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        conn.setRequestProperty("Referer", "https://www.idnes.cz");
        conn.connect();

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
        return response;
    }

    private List<RssItem> parseRssFeed(String rssFeed) throws Exception {
        return Parser.parse(new ByteArrayInputStream(rssFeed.getBytes(StandardCharsets.UTF_8)));
    }


    private void saveRssItemsToDatabase(List<RssItem> items) {
        dbHelper.open();
        for (RssItem item : items) {
            dbHelper.saveRssItem(item);
        }
        dbHelper.close();
    }


    private void postSuccess(Callback callback, List<RssItem> items) {
        new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(items));
    }


    private void postError(Callback callback, Exception e) {
        new Handler(Looper.getMainLooper()).post(() -> callback.onError(e));
    }
}