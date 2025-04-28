package com.example.rss_application;
import android.app.Application;
public class GlobalData extends Application {
    private String toolbarTitle;
    public String loadNewsText;
    private String loadSportText;
    private String loadCultureText;

    @Override
    public void onCreate() {
        super.onCreate();
        toolbarTitle = "Čtečka RSS souborů";
        loadNewsText = "Zprávy";
        loadSportText = "Sport";
        loadCultureText = "Kultura";
    }

    public String getToolbarTitle() {
        return toolbarTitle;
    }

    public String getLoadSportText() {
        return loadSportText;
    }
    public String getLoadCultureText() {
        return loadCultureText;
    }
    public String getLoadNewsText() {
        return loadNewsText;
    }
}
