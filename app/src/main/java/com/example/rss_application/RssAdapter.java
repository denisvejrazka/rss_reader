package com.example.rss_application;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RssAdapter extends RecyclerView.Adapter<RssAdapter.RssViewHolder> {
    private List<RssItem> rssItems;

    public RssAdapter(List<RssItem> rssItems) {
        this.rssItems = rssItems;
    }

    @Override
    public RssViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rss, parent, false);
        return new RssViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RssViewHolder holder, int position) {
        RssItem item = rssItems.get(position);
        holder.titleTextView.setText(item.title);
        holder.pubDateTextView.setText(item.pubDate);

        holder.itemView.setOnClickListener(v -> {
            android.content.Context context = v.getContext();
            android.content.Intent intent = new android.content.Intent(context, DetailActivity.class);
            intent.putExtra("title", item.title);
            intent.putExtra("description", item.description);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return rssItems.size();
    }

    public void updateData(List<RssItem> newItems) {
        rssItems.clear();
        rssItems.addAll(newItems);
        notifyDataSetChanged();
    }

    public static class RssViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView pubDateTextView;

        public RssViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.rss_title);
            pubDateTextView = itemView.findViewById(R.id.rss_pub_date);
        }
    }
}