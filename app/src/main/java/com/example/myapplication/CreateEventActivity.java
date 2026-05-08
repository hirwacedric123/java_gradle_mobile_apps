package com.example.myapplication;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class CreateEventActivity extends AppCompatActivity {

    private UserEventsDbHelper dbHelper;
    private TextInputEditText editTitle;
    private TextInputEditText editLocation;
    private TextInputEditText editDate;
    private TextInputEditText editTime;
    private TextInputEditText editNotes;
    private Spinner spinnerCategory;
    private SwitchMaterial switchPublic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        dbHelper = new UserEventsDbHelper(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbarCreateEvent);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> NavUtils.navigateUpFromSameTask(this));

        editTitle = findViewById(R.id.editEventTitle);
        editLocation = findViewById(R.id.editEventLocation);
        editDate = findViewById(R.id.editEventDate);
        editTime = findViewById(R.id.editEventTime);
        editNotes = findViewById(R.id.editEventNotes);
        spinnerCategory = findViewById(R.id.spinnerEventCategory);
        switchPublic = findViewById(R.id.switchEventPublic);

        editDate.setText(formatUtcMillis(MaterialDatePicker.todayInUtcMilliseconds()));
        wireDatePicker(editDate);

        ArrayAdapter<CharSequence> catAdapter =
                ArrayAdapter.createFromResource(this, R.array.event_categories, android.R.layout.simple_spinner_item);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(catAdapter);

        MaterialButton save = findViewById(R.id.buttonSaveEvent);
        save.setOnClickListener(v -> attemptSave());
        findViewById(R.id.buttonCancelEvent).setOnClickListener(v -> NavUtils.navigateUpFromSameTask(this));
    }

    private void wireDatePicker(TextInputEditText field) {
        field.setOnClickListener(
                v -> {
                    Long current = parseIsoToUtcMillis(field.getText() != null ? field.getText().toString() : null);
                    MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
                    if (current != null) {
                        builder.setSelection(current);
                    }
                    MaterialDatePicker<Long> picker = builder.build();
                    picker.addOnPositiveButtonClickListener(selection -> field.setText(formatUtcMillis(selection)));
                    picker.show(getSupportFragmentManager(), "pick_create_event_date");
                });
    }

    @Nullable
    private static Long parseIsoToUtcMillis(@Nullable String iso) {
        if (TextUtils.isEmpty(iso)) {
            return null;
        }
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            fmt.setLenient(false);
            fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date d = fmt.parse(iso.trim());
            return d != null ? d.getTime() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static String formatUtcMillis(long millis) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
        return fmt.format(new Date(millis));
    }

    private void attemptSave() {
        String title = textOf(editTitle);
        String location = textOf(editLocation);
        String date = textOf(editDate);
        String time = textOf(editTime);
        String notes = textOf(editNotes);
        if (TextUtils.isEmpty(title)
                || TextUtils.isEmpty(location)
                || TextUtils.isEmpty(date)
                || TextUtils.isEmpty(time)) {
            Toast.makeText(this, R.string.create_event_error_required, Toast.LENGTH_SHORT).show();
            return;
        }
        Object sel = spinnerCategory.getSelectedItem();
        String category =
                sel != null
                        ? sel.toString()
                        : getResources().getStringArray(R.array.event_categories)[0];
        long rowId =
                dbHelper.insertEvent(
                        title,
                        location,
                        date,
                        time,
                        notes != null ? notes : "",
                        category,
                        switchPublic.isChecked());
        if (rowId < 0) {
            Toast.makeText(this, R.string.create_event_error_required, Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, R.string.create_event_saved, Toast.LENGTH_SHORT).show();
        finish();
    }

    private static String textOf(TextInputEditText field) {
        if (field == null || field.getText() == null) {
            return "";
        }
        return field.getText().toString().trim();
    }
}
