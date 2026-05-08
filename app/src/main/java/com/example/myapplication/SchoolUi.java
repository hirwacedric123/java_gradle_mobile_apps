package com.example.myapplication;

import android.content.Context;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

/** Maps campus keys (stored in SQLite TEXT) to drawables. */
public final class SchoolUi {

    private SchoolUi() {}

    @DrawableRes
    public static int drawableForCampusKey(@NonNull Context context, @NonNull String key) {
        switch (key) {
            case "campus_classic":
                return R.drawable.school_campus_classic;
            case "campus_green":
                return R.drawable.school_campus_green;
            case "campus_modern":
            default:
                return R.drawable.school_campus_modern;
        }
    }
}
