package com.example.myapplication;

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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class SchoolDetailFragment extends Fragment {

    private static final String ARG_SCHOOL_ID = "arg_school_id";

    private long schoolId = -1L;
    private SchoolDbHelper dbHelper;

    public static SchoolDetailFragment newInstance(long schoolId) {
        SchoolDetailFragment f = new SchoolDetailFragment();
        Bundle b = new Bundle();
        b.putLong(ARG_SCHOOL_ID, schoolId);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_school_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper = new SchoolDbHelper(requireContext());
        if (getArguments() != null) {
            schoolId = getArguments().getLong(ARG_SCHOOL_ID, -1L);
        }

        MaterialToolbar toolbar = view.findViewById(R.id.toolbarSchoolDetail);
        toolbar.setNavigationOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        MaterialButton edit = view.findViewById(R.id.buttonEditSchool);
        edit.setOnClickListener(
                v ->
                        requireActivity()
                                .getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragment1, SchoolFormFragment.newInstance(schoolId))
                                .addToBackStack(null)
                                .commit());

        MaterialButton delete = view.findViewById(R.id.buttonDeleteSchool);
        delete.setOnClickListener(
                v ->
                        new MaterialAlertDialogBuilder(requireContext())
                                .setTitle(R.string.school_delete_confirm_title)
                                .setMessage(R.string.school_delete_confirm_message)
                                .setNegativeButton(R.string.school_delete_confirm_no, (d, which) -> d.dismiss())
                                .setPositiveButton(
                                        R.string.school_delete_confirm_yes,
                                        (d, which) -> {
                                            dbHelper.deleteSchool(schoolId);
                                            Toast.makeText(requireContext(), R.string.school_deleted, Toast.LENGTH_SHORT).show();
                                            requireActivity().getSupportFragmentManager().popBackStack();
                                        })
                                .show());

        bind(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        View v = getView();
        if (v != null) {
            bind(v);
        }
    }

    private void bind(@NonNull View root) {
        SchoolSummary school = dbHelper.querySchool(schoolId);
        if (school == null) {
            Toast.makeText(requireContext(), R.string.school_error_required, Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
            return;
        }

        ImageView image = root.findViewById(R.id.imageDetailCampus);
        image.setImageResource(SchoolUi.drawableForCampusKey(requireContext(), school.campusImageKey));

        TextView name = root.findViewById(R.id.textDetailName);
        name.setText(school.name);

        String typeLabel =
                getString(school.schoolType == 0 ? R.string.school_type_label_public : R.string.school_type_label_private);
        TextView meta = root.findViewById(R.id.textDetailMeta);
        meta.setText(getString(R.string.school_meta_format, school.foundedYear, school.openedOnIso, typeLabel));

        TextView acc = root.findViewById(R.id.textDetailAccredited);
        acc.setText(getString(school.accredited ? R.string.school_accredited_yes : R.string.school_accredited_no));

        LinearLayout container = root.findViewById(R.id.containerCourses);
        container.removeAllViews();

        List<CourseRow> courses = dbHelper.queryCoursesForSchool(schoolId);
        int pad = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, getResources().getDisplayMetrics());
        if (courses.isEmpty()) {
            TextView empty = new TextView(requireContext());
            empty.setPadding(pad, pad / 2, pad, pad / 2);
            empty.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium);
            empty.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            empty.setText(getString(R.string.school_courses_count_format, 0));
            container.addView(empty);
        } else {
            for (CourseRow c : courses) {
                TextView line = new TextView(requireContext());
                line.setPadding(pad, pad / 2, pad, pad / 2);
                line.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium);
                line.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
                line.setText(getString(R.string.school_course_line_format, c.courseName, c.credits, c.termStartIso));
                container.addView(line);
            }
        }
    }
}
