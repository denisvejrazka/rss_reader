package com.example.rss_application;
import android.app.Application;
public class GlobalData extends Application {
    private String toolbarTitle;
    private String loadButtonText;

    @Override
    public void onCreate() {
        super.onCreate();
        toolbarTitle = "Čtečka RSS souborů";
        loadButtonText = "Načíst";
    }

    public String getToolbarTitle() {
        return toolbarTitle;
    }

    public String getLoadButtonText() {
        return loadButtonText;
    }

    public void setToolbarTitle(String toolbarTitle) {
        this.toolbarTitle = toolbarTitle;
    }

    public void setLoadButtonText(String loadButtonText) {
         this.loadButtonText = loadButtonText;
    }
}
