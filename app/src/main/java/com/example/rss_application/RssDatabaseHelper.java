package com.example.rss_application;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class RssDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "rss_database.db";
    private static final int DATABASE_VERSION = 1;
    private SQLiteDatabase database;

    public RssDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE rss_items (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT, " +
                "link TEXT, " +
                "pubDate TEXT, " +
                "description TEXT)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS rss_items");
        onCreate(db);
    }


    public void open() {
        if (database == null || !database.isOpen()) {
            database = this.getWritableDatabase();
        }
    }


    public void close() {
        if (database != null && database.isOpen()) {
            database.close();
        }
    }


    public void saveRssItem(RssItem item) {
        String INSERT_ITEM = "INSERT INTO rss_items (title, link, pubDate, description) VALUES ('" +
                item.title + "', '" +
                item.link + "', '" +
                item.pubDate + "', '" +
                item.description + "')";
        database.execSQL(INSERT_ITEM);
    }


    public List<RssItem> getRecentRssItems() {
        List<RssItem> items = new ArrayList<>();

        String query = "SELECT * FROM rss_items ORDER BY pubDate DESC LIMIT 5";
        Cursor cursor = database.rawQuery(query, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                    String link = cursor.getString(cursor.getColumnIndexOrThrow("link"));
                    String pubDate = cursor.getString(cursor.getColumnIndexOrThrow("pubDate"));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));

                    items.add(new RssItem(title, link, pubDate, description));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        return items;
    }
}