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

    // Načítání RSS kanálu a jeho uložení do databáze
    public void loadRss(String urlString, Callback callback) {
        new Thread(() -> {
            try {
                List<RssItem> items;

                if (isNetworkAvailable()) {
                    // Pokud je internetové připojení, načti data online
                    String response = loadRssFromUrl(urlString);
                    if (response == null || !response.trim().startsWith("<?xml")) {
                        throw new Exception("Response is not valid XML.");
                    }

                    items = parseRssFeed(response);
                    saveRssItemsToDatabase(items);
                } else {
                    // Pokud není připojení k internetu, načti data z databáze
                    items = dbHelper.getRecentRssItems(); // Načte poslední položky z databáze
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

    // Kontrola připojení k síti
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