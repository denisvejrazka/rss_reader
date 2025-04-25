package com.example.rss_application;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import java.util.List;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RssAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        String toolbarTitle = ((GlobalData) getApplication()).getToolbarTitle();
        toolbar.setTitle(toolbarTitle);

        recyclerView = findViewById(R.id.rss_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Button loadButton = findViewById(R.id.load_button);
        String loadButtonText = ((GlobalData) getApplication()).getLoadButtonText();
        loadButton.setText(loadButtonText);
        loadButton.setOnClickListener(v -> loadRss());
    }

    private void loadRss() {
        if (isOnline()) {
            RssRepository repository = new RssRepository(this);
            repository.loadRss("https://servis.idnes.cz/rss.aspx?c=zpravodaj", new RssRepository.Callback() {
                @Override
                public void onSuccess(List<RssItem> items) {
                    showRssItems(items);
                    RssDatabaseHelper dbHelper = new RssDatabaseHelper(MainActivity.this);
                    dbHelper.open();
                    for (RssItem item : items) {
                        dbHelper.saveRssItem(item);
                    }
                    dbHelper.close();
                }

                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                }
            });
        } else {
            RssDatabaseHelper dbHelper = new RssDatabaseHelper(MainActivity.this);
            dbHelper.open();
            List<RssItem> items = dbHelper.getRecentRssItems();
            showRssItems(items);
            dbHelper.close();
        }
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    private void showRssItems(List<RssItem> items) {
        if (adapter == null) {
            adapter = new RssAdapter(items);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateData(items);
        }
    }
}