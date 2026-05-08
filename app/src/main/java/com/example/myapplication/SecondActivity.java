package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class SecondActivity extends AppCompatActivity {

    private UserEventsDbHelper dbHelper;
    private ArrayAdapter<String> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        dbHelper = new UserEventsDbHelper(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbarSecond);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> NavUtils.navigateUpFromSameTask(this));

        MaterialButton openSchool = findViewById(R.id.buttonOpenSchoolDb);
        openSchool.setOnClickListener(v -> startActivity(new Intent(this, RecyclerViewActivity.class)));

        findViewById(R.id.buttonCreateEventFromSaved)
                .setOnClickListener(v -> startActivity(new Intent(this, CreateEventActivity.class)));

        ListView listView = findViewById(R.id.simpleListView);
        listAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        listView.setAdapter(listAdapter);
        refreshSavedList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshSavedList();
    }

    private void refreshSavedList() {
        ArrayList<String> lines = dbHelper.readSummariesNewestFirst(this);
        if (!lines.isEmpty()) {
            lines.add(getString(R.string.saved_events_demo_separator));
        }
        lines.add(getString(R.string.list_item_1));
        lines.add(getString(R.string.list_item_2));
        lines.add(getString(R.string.list_item_3));
        lines.add(getString(R.string.list_item_4));
        lines.add(getString(R.string.list_item_5));
        listAdapter.clear();
        listAdapter.addAll(lines);
        listAdapter.notifyDataSetChanged();
    }
}
