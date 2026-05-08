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

public final class SchoolListAdapter extends RecyclerView.Adapter<SchoolListAdapter.Holder> {

    public interface OnSchoolClickListener {
        void onSchoolClick(@NonNull SchoolSummary school);
    }

    private final LayoutInflater inflater;
    private final OnSchoolClickListener listener;
    private final List<SchoolSummary> data = new ArrayList<>();

    public SchoolListAdapter(@NonNull LayoutInflater inflater, @NonNull OnSchoolClickListener listener) {
        this.inflater = inflater;
        this.listener = listener;
    }

    public void replaceData(@NonNull List<SchoolSummary> items) {
        data.clear();
        data.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = inflater.inflate(R.layout.item_school_row, parent, false);
        return new Holder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        SchoolSummary s = data.get(position);
        holder.imageCampus.setImageResource(SchoolUi.drawableForCampusKey(holder.itemView.getContext(), s.campusImageKey));
        holder.textName.setText(s.name);
        String typeLabel =
                holder.itemView
                        .getContext()
                        .getString(s.schoolType == 0 ? R.string.school_type_label_public : R.string.school_type_label_private);
        holder.textMeta.setText(
                holder.itemView
                        .getContext()
                        .getString(R.string.school_meta_format, s.foundedYear, s.openedOnIso, typeLabel));
        holder.textCourseCount.setText(
                holder.itemView.getContext().getString(R.string.school_courses_count_format, s.courseCount));
        holder.itemView.setOnClickListener(v -> listener.onSchoolClick(s));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ImageView imageCampus;
        final TextView textName;
        final TextView textMeta;
        final TextView textCourseCount;

        Holder(@NonNull View itemView) {
            super(itemView);
            imageCampus = itemView.findViewById(R.id.imageCampus);
            textName = itemView.findViewById(R.id.textSchoolName);
            textMeta = itemView.findViewById(R.id.textSchoolMeta);
            textCourseCount = itemView.findViewById(R.id.textCourseCount);
        }
    }
}
