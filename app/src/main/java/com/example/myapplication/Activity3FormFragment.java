package com.example.myapplication;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.example.myapplication.network.Activity3ApiClient;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Activity3FormFragment extends Fragment {

    public static final long NEW_ID = -1L;
    private static final String ARG_ID = "arg_id";

    interface Host {
        void showList(boolean addToBackStack);
    }

    public static Activity3FormFragment newInstance(long projectId) {
        Activity3FormFragment fragment = new Activity3FormFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_ID, projectId);
        fragment.setArguments(args);
        return fragment;
    }

    private Host host;
    private Activity3DbHelper dbHelper;
    private long editingId = NEW_ID;
    private String[] imageKeys;
    private Activity3ApiClient apiClient;

    private TextInputEditText editTitle;
    private TextInputEditText editHours;
    private TextInputEditText editDueDate;
    private TextInputEditText editCategoryName;
    private TextInputEditText editPriority;
    private TextInputEditText editReviewDate;
    private Spinner spinnerImage;
    private ImageView imagePreview;
    private MaterialButtonToggleGroup toggleStatus;
    private CheckBox checkUrgent;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        host = (Host) context;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_activity3_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper = new Activity3DbHelper(requireContext());
        apiClient = new Activity3ApiClient(requireContext());
        imageKeys = getResources().getStringArray(R.array.activity3_image_keys);
        if (getArguments() != null) {
            editingId = getArguments().getLong(ARG_ID, NEW_ID);
        }

        MaterialToolbar toolbar = view.findViewById(R.id.toolbarActivity3Form);
        toolbar.setTitle(editingId == NEW_ID
                ? getString(R.string.activity3_form_add_title)
                : getString(R.string.activity3_form_edit_title));
        toolbar.setNavigationOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        editTitle = view.findViewById(R.id.editActivity3Title);
        editHours = view.findViewById(R.id.editActivity3Hours);
        editDueDate = view.findViewById(R.id.editActivity3DueDate);
        editCategoryName = view.findViewById(R.id.editActivity3CategoryName);
        editPriority = view.findViewById(R.id.editActivity3Priority);
        editReviewDate = view.findViewById(R.id.editActivity3ReviewDate);
        spinnerImage = view.findViewById(R.id.spinnerActivity3Image);
        imagePreview = view.findViewById(R.id.imageActivity3Preview);
        toggleStatus = view.findViewById(R.id.toggleActivity3Status);
        checkUrgent = view.findViewById(R.id.checkActivity3Urgent);

        String[] imageLabels = getResources().getStringArray(R.array.activity3_image_labels);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.item_school_spinner, imageLabels);
        adapter.setDropDownViewResource(R.layout.item_school_spinner_dropdown);
        spinnerImage.setAdapter(adapter);
        spinnerImage.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View v, int position, long id) {
                updateImage(position);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        wireDatePicker(editDueDate);
        wireDatePicker(editReviewDate);
        TextInputLayout tilDue = view.findViewById(R.id.tilActivity3DueDate);
        tilDue.setEndIconOnClickListener(v -> showDatePicker(editDueDate));
        TextInputLayout tilReview = view.findViewById(R.id.tilActivity3ReviewDate);
        tilReview.setEndIconOnClickListener(v -> showDatePicker(editReviewDate));

        MaterialButton save = view.findViewById(R.id.buttonActivity3Save);
        save.setOnClickListener(v -> attemptSave());
        view.findViewById(R.id.buttonActivity3Cancel)
                .setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        if (editingId == NEW_ID) {
            String today = formatUtcMillis(MaterialDatePicker.todayInUtcMilliseconds());
            editDueDate.setText(today);
            editReviewDate.setText(today);
            spinnerImage.setSelection(0);
            updateImage(0);
        } else {
            loadExisting();
        }
    }

    private void loadExisting() {
        Activity3Summary row = dbHelper.queryProject(editingId);
        if (row == null) {
            Toast.makeText(requireContext(), R.string.activity3_error_required, Toast.LENGTH_SHORT).show();
            host.showList(false);
            return;
        }
        editTitle.setText(row.title);
        editHours.setText(String.valueOf(row.estimateHours));
        editDueDate.setText(row.dueDateIso);
        checkUrgent.setChecked(row.urgent);
        if (row.status == 1) {
            toggleStatus.check(R.id.toggleActivity3Ongoing);
        } else if (row.status == 2) {
            toggleStatus.check(R.id.toggleActivity3Completed);
        } else {
            toggleStatus.check(R.id.toggleActivity3Planned);
        }

        int idx = 0;
        for (int i = 0; i < imageKeys.length; i++) {
            if (imageKeys[i].equals(row.imageKey)) {
                idx = i;
                break;
            }
        }
        spinnerImage.setSelection(idx);
        updateImage(idx);

        Activity3CategoryRow category = dbHelper.queryFirstCategory(editingId);
        if (category != null) {
            editCategoryName.setText(category.categoryName);
            editPriority.setText(String.valueOf(category.priorityLevel));
            editReviewDate.setText(category.reviewDateIso);
        }
    }

    private void updateImage(int position) {
        String key = imageKeys[Math.max(0, Math.min(position, imageKeys.length - 1))];
        imagePreview.setImageResource(Activity3Ui.drawableForImageKey(key));
    }

    private void wireDatePicker(@NonNull TextInputEditText field) {
        field.setOnClickListener(v -> showDatePicker(field));
    }

    private void showDatePicker(@NonNull TextInputEditText field) {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker().build();
        picker.addOnPositiveButtonClickListener(selection -> field.setText(formatUtcMillis(selection)));
        picker.show(getParentFragmentManager(), "pick_" + field.getId());
    }

    private int readStatus() {
        int checkedId = toggleStatus.getCheckedButtonId();
        if (checkedId == R.id.toggleActivity3Ongoing) {
            return 1;
        }
        if (checkedId == R.id.toggleActivity3Completed) {
            return 2;
        }
        return 0;
    }

    private void attemptSave() {
        String title = textOf(editTitle);
        String hoursStr = textOf(editHours);
        String dueDate = textOf(editDueDate);
        String categoryName = textOf(editCategoryName);
        String priorityStr = textOf(editPriority);
        String reviewDate = textOf(editReviewDate);
        if (TextUtils.isEmpty(title)
                || TextUtils.isEmpty(hoursStr)
                || TextUtils.isEmpty(dueDate)
                || TextUtils.isEmpty(categoryName)
                || TextUtils.isEmpty(priorityStr)
                || TextUtils.isEmpty(reviewDate)) {
            Toast.makeText(requireContext(), R.string.activity3_error_required, Toast.LENGTH_SHORT).show();
            return;
        }

        int hours;
        int priority;
        try {
            hours = Integer.parseInt(hoursStr);
            priority = Integer.parseInt(priorityStr);
            if (hours < 1 || hours > 100 || priority < 1 || priority > 5) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            Toast.makeText(requireContext(), R.string.activity3_error_numbers, Toast.LENGTH_SHORT).show();
            return;
        }

        int status = readStatus();
        boolean urgent = checkUrgent.isChecked();
        int imagePos = spinnerImage.getSelectedItemPosition();
        String imageKey = imageKeys[Math.max(0, Math.min(imagePos, imageKeys.length - 1))];

        if (editingId == NEW_ID) {
            dbHelper.insertProjectWithCategory(title, hours, dueDate, imageKey, status, urgent, categoryName, priority, reviewDate);
        } else {
            dbHelper.updateProjectWithCategory(editingId, title, hours, dueDate, imageKey, status, urgent, categoryName, priority, reviewDate);
        }

        Toast.makeText(requireContext(), R.string.activity3_saved, Toast.LENGTH_SHORT).show();
        apiClient.createRecord(
                title,
                hours,
                dueDate,
                imageKey,
                status,
                urgent,
                categoryName,
                priority,
                reviewDate,
                () -> apiClient.fetchAll(new Activity3ApiClient.SyncCallback() {
                    @Override
                    public void onSuccess(
                            @NonNull java.util.List<Activity3Summary> summaries,
                            @NonNull java.util.List<Activity3CategoryRow> categories) {
                        dbHelper.replaceFromServer(summaries, categories);
                        if (isAdded()) {
                            Toast.makeText(requireContext(), R.string.activity3_sync_ok, Toast.LENGTH_SHORT).show();
                            host.showList(false);
                        }
                    }

                    @Override
                    public void onError(@NonNull Exception e) {
                        if (isAdded()) {
                            Toast.makeText(requireContext(), R.string.activity3_sync_failed, Toast.LENGTH_SHORT).show();
                            host.showList(false);
                        }
                    }
                }),
                error -> {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), R.string.activity3_sync_failed, Toast.LENGTH_SHORT).show();
                        host.showList(false);
                    }
                });
    }

    private static String textOf(@Nullable TextInputEditText e) {
        return e == null || e.getText() == null ? "" : e.getText().toString().trim();
    }

    private static String formatUtcMillis(long millis) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
        return fmt.format(new Date(millis));
    }
}
