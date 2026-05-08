package com.example.myapplication;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class SchoolFormFragment extends Fragment {

    public static final long NEW_SCHOOL_ID = -1L;
    private static final String ARG_SCHOOL_ID = "arg_school_id";

    private SchoolDbHelper dbHelper;
    private long editingSchoolId = NEW_SCHOOL_ID;
    private String[] campusKeys;

    private TextInputEditText editName;
    private TextInputEditText editYear;
    private TextInputEditText editOpenedOn;
    private TextInputEditText editCourseName;
    private TextInputEditText editCredits;
    private TextInputEditText editTermStart;
    private Spinner spinnerCampus;
    private ImageView imagePreview;
    private RadioGroup radioType;
    private RadioButton radioPublic;
    private RadioButton radioPrivate;
    private CheckBox checkAccredited;

    public static SchoolFormFragment newInstance(long schoolId) {
        SchoolFormFragment f = new SchoolFormFragment();
        Bundle b = new Bundle();
        b.putLong(ARG_SCHOOL_ID, schoolId);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_school_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper = new SchoolDbHelper(requireContext());
        campusKeys = getResources().getStringArray(R.array.school_campus_keys);

        Bundle args = getArguments();
        if (args != null) {
            editingSchoolId = args.getLong(ARG_SCHOOL_ID, NEW_SCHOOL_ID);
        }

        MaterialToolbar toolbar = view.findViewById(R.id.toolbarSchoolForm);
        toolbar.setTitle(
                editingSchoolId == NEW_SCHOOL_ID ? getString(R.string.school_form_add_title) : getString(R.string.school_form_edit_title));
        toolbar.setNavigationOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        editName = view.findViewById(R.id.editSchoolName);
        editYear = view.findViewById(R.id.editFoundedYear);
        editOpenedOn = view.findViewById(R.id.editOpenedOn);
        editCourseName = view.findViewById(R.id.editCourseName);
        editCredits = view.findViewById(R.id.editCredits);
        editTermStart = view.findViewById(R.id.editTermStart);
        spinnerCampus = view.findViewById(R.id.spinnerCampus);
        imagePreview = view.findViewById(R.id.imageCampusPreview);
        radioType = view.findViewById(R.id.radioSchoolType);
        radioPublic = view.findViewById(R.id.radioPublic);
        radioPrivate = view.findViewById(R.id.radioPrivate);
        checkAccredited = view.findViewById(R.id.checkAccredited);

        String[] campusLabels =
                new String[]{
                        getString(R.string.school_campus_modern_label),
                        getString(R.string.school_campus_classic_label),
                        getString(R.string.school_campus_green_label)
                };
        ArrayAdapter<String> spinAdapter =
                new ArrayAdapter<>(requireContext(), R.layout.item_school_spinner, campusLabels);
        spinAdapter.setDropDownViewResource(R.layout.item_school_spinner_dropdown);
        spinnerCampus.setAdapter(spinAdapter);

        final boolean[] spinnerGate = {false};
        spinnerCampus.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                        if (!spinnerGate[0]) {
                            return;
                        }
                        updateCampusImage(position);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // no-op
                    }
                });

        wireDatePicker(editOpenedOn);
        wireDatePicker(editTermStart);

        MaterialButton save = view.findViewById(R.id.buttonSave);
        save.setOnClickListener(v -> attemptSave());

        view.findViewById(R.id.buttonCancel)
                .setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        if (editingSchoolId == NEW_SCHOOL_ID) {
            String today = formatUtcMillis(MaterialDatePicker.todayInUtcMilliseconds());
            editOpenedOn.setText(today);
            editTermStart.setText(today);
            spinnerCampus.setSelection(0);
            spinnerGate[0] = true;
            updateCampusImage(0);
        } else {
            loadExisting(spinnerGate);
            spinnerGate[0] = true;
        }
    }

    private void loadExisting(@NonNull boolean[] spinnerGate) {
        SchoolSummary school = dbHelper.querySchool(editingSchoolId);
        if (school == null) {
            Toast.makeText(requireContext(), R.string.school_error_required, Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
            return;
        }
        editName.setText(school.name);
        editYear.setText(String.valueOf(school.foundedYear));
        editOpenedOn.setText(school.openedOnIso);
        if (school.schoolType == 0) {
            radioPublic.setChecked(true);
        } else {
            radioPrivate.setChecked(true);
        }
        checkAccredited.setChecked(school.accredited);

        int campusIndex = 0;
        for (int i = 0; i < campusKeys.length; i++) {
            if (campusKeys[i].equals(school.campusImageKey)) {
                campusIndex = i;
                break;
            }
        }
        spinnerCampus.setSelection(campusIndex, false);
        updateCampusImage(campusIndex);
        spinnerGate[0] = false;

        CourseRow course = dbHelper.queryFirstCourse(editingSchoolId);
        if (course != null) {
            editCourseName.setText(course.courseName);
            editCredits.setText(String.valueOf(course.credits));
            editTermStart.setText(course.termStartIso);
        }
    }

    private void updateCampusImage(int campusPosition) {
        String key = campusKeys[Math.max(0, Math.min(campusPosition, campusKeys.length - 1))];
        imagePreview.setImageResource(SchoolUi.drawableForCampusKey(requireContext(), key));
    }

    private void wireDatePicker(@NonNull TextInputEditText field) {
        field.setOnClickListener(
                v -> {
                    Long current = parseIsoToUtcMillis(field.getText() != null ? field.getText().toString() : null);
                    MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
                    if (current != null) {
                        builder.setSelection(current);
                    }
                    MaterialDatePicker<Long> picker = builder.build();
                    picker.addOnPositiveButtonClickListener(selection -> field.setText(formatUtcMillis(selection)));
                    picker.show(getParentFragmentManager(), "pick_" + field.getId());
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
        String name = textOf(editName);
        String yearStr = textOf(editYear);
        String opened = textOf(editOpenedOn);
        String courseName = textOf(editCourseName);
        String creditsStr = textOf(editCredits);
        String term = textOf(editTermStart);

        if (TextUtils.isEmpty(name)
                || TextUtils.isEmpty(yearStr)
                || TextUtils.isEmpty(opened)
                || TextUtils.isEmpty(courseName)
                || TextUtils.isEmpty(creditsStr)
                || TextUtils.isEmpty(term)) {
            Toast.makeText(requireContext(), R.string.school_error_required, Toast.LENGTH_SHORT).show();
            return;
        }

        int year;
        try {
            year = Integer.parseInt(yearStr.trim());
            if (year < 1800 || year > 2100) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), R.string.school_error_year, Toast.LENGTH_SHORT).show();
            return;
        }

        int credits;
        try {
            credits = Integer.parseInt(creditsStr.trim());
            if (credits < 1 || credits > 12) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), R.string.school_error_credits, Toast.LENGTH_SHORT).show();
            return;
        }

        int type = radioPrivate.isChecked() ? 1 : 0;
        boolean accredited = checkAccredited.isChecked();
        int pos = spinnerCampus.getSelectedItemPosition();
        String campusKey = campusKeys[Math.max(0, Math.min(pos, campusKeys.length - 1))];

        if (editingSchoolId == NEW_SCHOOL_ID) {
            dbHelper.insertSchoolWithCourse(name, year, campusKey, opened, type, accredited, courseName, credits, term);
        } else {
            dbHelper.updateSchoolWithCourse(
                    editingSchoolId, name, year, campusKey, opened, type, accredited, courseName, credits, term);
        }

        Toast.makeText(requireContext(), R.string.school_saved, Toast.LENGTH_SHORT).show();
        FragmentManager fm = requireActivity().getSupportFragmentManager();
        fm.popBackStack();
    }

    private static String textOf(@Nullable TextInputEditText e) {
        return e == null || e.getText() == null ? "" : e.getText().toString().trim();
    }
}
