package com.example.myapplication;

import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.example.myapplication.network.Activity3ApiClient;

import java.util.List;

public class Activity3DetailFragment extends Fragment {

    private static final String ARG_ID = "arg_id";

    interface Host {
        void showForm(long projectId);

        void showList(boolean addToBackStack);
    }

    public static Activity3DetailFragment newInstance(long projectId) {
        Activity3DetailFragment f = new Activity3DetailFragment();
        Bundle b = new Bundle();
        b.putLong(ARG_ID, projectId);
        f.setArguments(b);
        return f;
    }

    private Host host;
    private Activity3DbHelper dbHelper;
    private Activity3ApiClient apiClient;
    private long projectId = -1L;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        host = (Host) context;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_activity3_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper = new Activity3DbHelper(requireContext());
        apiClient = new Activity3ApiClient(requireContext());
        if (getArguments() != null) {
            projectId = getArguments().getLong(ARG_ID, -1L);
        }

        MaterialToolbar toolbar = view.findViewById(R.id.toolbarActivity3Detail);
        toolbar.setNavigationOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        MaterialButton edit = view.findViewById(R.id.buttonActivity3Edit);
        edit.setOnClickListener(v -> host.showForm(projectId));

        MaterialButton delete = view.findViewById(R.id.buttonActivity3Delete);
        delete.setOnClickListener(v -> {
            apiClient.deleteRecord(
                    projectId,
                    () -> {
                        dbHelper.deleteProject(projectId);
                        if (isAdded()) {
                            Toast.makeText(requireContext(), R.string.activity3_deleted, Toast.LENGTH_SHORT).show();
                            host.showList(false);
                        }
                    },
                    error -> {
                        if (isAdded()) {
                            Toast.makeText(requireContext(), R.string.activity3_sync_failed, Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        bind(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        View root = getView();
        if (root != null) {
            bind(root);
        }
    }

    private void bind(@NonNull View root) {
        Activity3Summary row = dbHelper.queryProject(projectId);
        if (row == null) {
            Toast.makeText(requireContext(), R.string.activity3_error_required, Toast.LENGTH_SHORT).show();
            host.showList(false);
            return;
        }

        ImageView image = root.findViewById(R.id.imageActivity3Detail);
        image.setImageResource(Activity3Ui.drawableForImageKey(row.imageKey));

        TextView title = root.findViewById(R.id.textActivity3DetailTitle);
        title.setText(row.title);
        TextView meta = root.findViewById(R.id.textActivity3DetailMeta);
        meta.setText(getString(
                R.string.activity3_detail_meta_format,
                row.estimateHours,
                row.dueDateIso,
                Activity3Ui.statusLabel(requireContext(), row.status),
                row.urgent ? getString(R.string.activity3_urgent_yes) : getString(R.string.activity3_urgent_no)));

        LinearLayout container = root.findViewById(R.id.containerActivity3Categories);
        container.removeAllViews();
        List<Activity3CategoryRow> categories = dbHelper.queryCategories(projectId);
        int pad = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
        for (Activity3CategoryRow category : categories) {
            TextView line = new TextView(requireContext());
            line.setPadding(pad, pad, pad, pad);
            line.setText(getString(
                    R.string.activity3_category_line_format,
                    category.categoryName,
                    category.priorityLevel,
                    category.reviewDateIso));
            line.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
            container.addView(line);
        }
    }
}
