package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class SchoolListFragment extends Fragment {

    private SchoolDbHelper dbHelper;
    private SchoolListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_school_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper = new SchoolDbHelper(requireContext());
        RecyclerView recycler = view.findViewById(R.id.recyclerSchools);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new SchoolListAdapter(getLayoutInflater(), this::openDetail);
        recycler.setAdapter(adapter);

        FloatingActionButton fab = view.findViewById(R.id.fabAddSchool);
        fab.setOnClickListener(v -> openForm(SchoolFormFragment.NEW_SCHOOL_ID));

        refreshList();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshList();
    }

    private void refreshList() {
        List<SchoolSummary> rows = dbHelper.queryAllSchoolSummaries();
        adapter.replaceData(rows);
    }

    private void openDetail(@NonNull SchoolSummary school) {
        FragmentManager fm = requireActivity().getSupportFragmentManager();
        fm.beginTransaction()
                .replace(R.id.fragment1, SchoolDetailFragment.newInstance(school.id))
                .addToBackStack(null)
                .commit();
    }

    private void openForm(long schoolId) {
        FragmentManager fm = requireActivity().getSupportFragmentManager();
        fm.beginTransaction()
                .replace(R.id.fragment1, SchoolFormFragment.newInstance(schoolId))
                .addToBackStack(null)
                .commit();
    }
}
