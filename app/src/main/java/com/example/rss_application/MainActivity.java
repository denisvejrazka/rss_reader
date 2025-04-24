package com.example.rss_application;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RssAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.rss_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Button loadButton = findViewById(R.id.load_button);
        loadButton.setText("Načíst");
        loadButton.setOnClickListener(v -> loadRss());
    }

    private void loadRss() {
        RssRepository repository = new RssRepository(this);
        repository.loadRss("https://servis.idnes.cz/rss.aspx?c=zpravodaj", new RssRepository.Callback() {
            @Override
            public void onSuccess(List<RssItem> items) {
                showRssItems(items);
                Log.d("RSS", "Načteno položek: " + items.size());
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
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