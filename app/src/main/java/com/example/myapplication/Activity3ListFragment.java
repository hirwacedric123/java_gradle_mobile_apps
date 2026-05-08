package com.example.myapplication;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class Activity3ListFragment extends Fragment {

    interface Host {
        void showForm(long projectId);

        void showDetail(long projectId);
    }

    private Host host;
    private Activity3DbHelper dbHelper;
    private Activity3Adapter adapter;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        host = (Host) context;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_activity3_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper = new Activity3DbHelper(requireContext());

        MaterialToolbar toolbar = view.findViewById(R.id.toolbarActivity3List);
        toolbar.setNavigationOnClickListener(v -> requireActivity().finish());

        RecyclerView recycler = view.findViewById(R.id.recyclerActivity3);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new Activity3Adapter(getLayoutInflater(), row -> host.showDetail(row.id));
        recycler.setAdapter(adapter);

        FloatingActionButton fab = view.findViewById(R.id.fabAddActivity3);
        fab.setOnClickListener(v -> host.showForm(Activity3FormFragment.NEW_ID));

        refresh();
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        if (adapter != null) {
            adapter.replaceData(dbHelper.queryAllProjectSummaries());
        }
    }
}
