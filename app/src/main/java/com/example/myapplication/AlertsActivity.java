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

public class AlertsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alerts);
        BottomNavHelper.bind(this, BottomNavHelper.TAB_ALERTS);

        MaterialToolbar toolbar = findViewById(R.id.toolbarAlerts);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> NavUtils.navigateUpFromSameTask(this));

        String[] titles = getResources().getStringArray(R.array.alerts_item_titles);
        String[] subtitles = getResources().getStringArray(R.array.alerts_item_subtitles);
        String[] times = getResources().getStringArray(R.array.alerts_item_times);

        RecyclerView recyclerView = findViewById(R.id.recyclerAlerts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new AlertsAdapter(titles, subtitles, times));
    }

    private final class AlertsAdapter extends RecyclerView.Adapter<AlertsAdapter.Vh> {

        private final String[] titles;
        private final String[] subtitles;
        private final String[] times;
        private final boolean[] unread;

        AlertsAdapter(String[] titles, String[] subtitles, String[] times) {
            this.titles = titles;
            this.subtitles = subtitles;
            this.times = times;
            this.unread = new boolean[titles.length];
            for (int i = 0; i < this.unread.length; i++) {
                this.unread[i] = i < 2;
            }
        }

        @NonNull
        @Override
        public Vh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View row =
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alert_row, parent, false);
            return new Vh(row);
        }

        @Override
        public void onBindViewHolder(@NonNull Vh holder, int position) {
            holder.title.setText(titles[position]);
            holder.subtitle.setText(subtitles[position]);
            holder.time.setText(times[position]);
            holder.dotUnread.setVisibility(unread[position] ? View.VISIBLE : View.INVISIBLE);
            holder.itemView.setOnClickListener(
                    v -> {
                        Toast.makeText(
                                        AlertsActivity.this,
                                        getString(R.string.alert_open_format, titles[position], subtitles[position]),
                                        Toast.LENGTH_SHORT)
                                .show();
                        if (unread[position]) {
                            unread[position] = false;
                            notifyItemChanged(position);
                        }
                    });
        }

        @Override
        public int getItemCount() {
            return titles.length;
        }

        final class Vh extends RecyclerView.ViewHolder {
            final View dotUnread;
            final TextView time;
            final TextView title;
            final TextView subtitle;

            Vh(@NonNull View itemView) {
                super(itemView);
                dotUnread = itemView.findViewById(R.id.dotUnread);
                time = itemView.findViewById(R.id.textAlertTime);
                title = itemView.findViewById(R.id.textAlertTitle);
                subtitle = itemView.findViewById(R.id.textAlertSubtitle);
            }
        }
    }
}
