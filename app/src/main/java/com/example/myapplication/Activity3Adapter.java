package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public final class Activity3Adapter extends RecyclerView.Adapter<Activity3Adapter.Holder> {

    public interface OnRecordClickListener {
        void onRecordClick(@NonNull Activity3Summary row);
    }

    private final LayoutInflater inflater;
    private final OnRecordClickListener listener;
    private final List<Activity3Summary> data = new ArrayList<>();

    public Activity3Adapter(@NonNull LayoutInflater inflater, @NonNull OnRecordClickListener listener) {
        this.inflater = inflater;
        this.listener = listener;
    }

    public void replaceData(@NonNull List<Activity3Summary> rows) {
        data.clear();
        data.addAll(rows);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(inflater.inflate(R.layout.item_activity3_row, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Activity3Summary row = data.get(position);
        holder.image.setImageResource(Activity3Ui.drawableForImageKey(row.imageKey));
        holder.title.setText(row.title);
        holder.meta.setText(holder.itemView.getContext().getString(
                R.string.activity3_meta_format,
                row.estimateHours,
                row.dueDateIso,
                Activity3Ui.statusLabel(holder.itemView.getContext(), row.status)));
        holder.categoryCount.setText(holder.itemView.getContext().getString(
                R.string.activity3_category_count_format, row.categoryCount));
        holder.urgent.setVisibility(row.urgent ? View.VISIBLE : View.GONE);
        holder.itemView.setOnClickListener(v -> listener.onRecordClick(row));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView title;
        final TextView meta;
        final TextView categoryCount;
        final TextView urgent;

        Holder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imageActivity3);
            title = itemView.findViewById(R.id.textActivity3Title);
            meta = itemView.findViewById(R.id.textActivity3Meta);
            categoryCount = itemView.findViewById(R.id.textActivity3CategoryCount);
            urgent = itemView.findViewById(R.id.textActivity3Urgent);
        }
    }
}
