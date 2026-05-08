package com.example.myapplication;

import android.content.Context;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

/** UI helpers for Assignment 4 image/status mappings. */
public final class Activity3Ui {

    private Activity3Ui() {
    }

    @DrawableRes
    public static int drawableForImageKey(@NonNull String key) {
        switch (key) {
            case "img_camera":
                return android.R.drawable.ic_menu_camera;
            case "img_gallery":
                return android.R.drawable.ic_menu_gallery;
            case "img_agenda":
            default:
                return android.R.drawable.ic_menu_agenda;
        }
    }

    @NonNull
    public static String statusLabel(@NonNull Context context, int status) {
        if (status == 1) {
            return context.getString(R.string.activity3_status_ongoing);
        }
        if (status == 2) {
            return context.getString(R.string.activity3_status_completed);
        }
        return context.getString(R.string.activity3_status_planned);
    }
}
