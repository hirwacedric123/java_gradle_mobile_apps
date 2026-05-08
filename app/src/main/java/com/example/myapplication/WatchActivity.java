package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

public class WatchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch);
        BottomNavHelper.bind(this, BottomNavHelper.TAB_WATCH);

        MaterialToolbar toolbar = findViewById(R.id.toolbarWatch);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> NavUtils.navigateUpFromSameTask(this));

        String[] titles = getResources().getStringArray(R.array.watch_clip_titles);
        String[] subtitles = getResources().getStringArray(R.array.watch_clip_subtitles);
        String[] lengths = getResources().getStringArray(R.array.watch_clip_lengths);

        RecyclerView recyclerView = findViewById(R.id.recyclerWatch);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new WatchAdapter(titles, subtitles, lengths));
    }

    private final class WatchAdapter extends RecyclerView.Adapter<WatchAdapter.Vh> {

        private final String[] titles;
        private final String[] subtitles;
        private final String[] lengths;

        WatchAdapter(String[] titles, String[] subtitles, String[] lengths) {
            this.titles = titles;
            this.subtitles = subtitles;
            this.lengths = lengths;
        }

        @NonNull
        @Override
        public Vh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View row =
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.item_watch_row, parent, false);
            return new Vh(row);
        }

        @Override
        public void onBindViewHolder(@NonNull Vh holder, int position) {
            holder.title.setText(titles[position]);
            holder.subtitle.setText(subtitles[position]);
            holder.length.setText(lengths[position]);
            holder.itemView.setOnClickListener(
                    v ->
                            Toast.makeText(
                                            WatchActivity.this,
                                            getString(R.string.watch_play_format, titles[position]),
                                            Toast.LENGTH_SHORT)
                                    .show());
        }

        @Override
        public int getItemCount() {
            return titles.length;
        }

        final class Vh extends RecyclerView.ViewHolder {
            final TextView title;
            final TextView subtitle;
            final TextView length;

            Vh(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.textWatchTitle);
                subtitle = itemView.findViewById(R.id.textWatchSubtitle);
                length = itemView.findViewById(R.id.textWatchLength);
            }
        }
    }
}
