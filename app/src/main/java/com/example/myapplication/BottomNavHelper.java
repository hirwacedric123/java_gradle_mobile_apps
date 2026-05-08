package com.example.myapplication;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

/** Shared bottom navigation behavior for all non-main activities. */
public final class BottomNavHelper {

    public static final int TAB_FEED = 0;
    public static final int TAB_WATCH = 1;
    public static final int TAB_SHOP = 2;
    public static final int TAB_ALERTS = 3;
    public static final int TAB_MENU = 4;

    private static final int MENU_SAVED = 100;
    private static final int MENU_SCHOOL = 101;
    private static final int MENU_ASSIGNMENT4 = 102;
    private static final int MENU_ABOUT = 103;

    private BottomNavHelper() {
    }

    public static void bind(@NonNull AppCompatActivity activity, int selectedTab) {
        TextView feed = activity.findViewById(R.id.bottomNavFeed);
        TextView watch = activity.findViewById(R.id.bottomNavWatch);
        TextView shop = activity.findViewById(R.id.bottomNavShop);
        TextView alerts = activity.findViewById(R.id.bottomNavAlerts);
        TextView menu = activity.findViewById(R.id.bottomNavMenu);
        if (feed == null || watch == null || shop == null || alerts == null || menu == null) {
            return;
        }

        feed.setOnClickListener(v -> open(activity, MainActivity.class));
        watch.setOnClickListener(v -> open(activity, WatchActivity.class));
        shop.setOnClickListener(v -> open(activity, ShopActivity.class));
        alerts.setOnClickListener(v -> open(activity, AlertsActivity.class));
        menu.setOnClickListener(v -> showMenu(activity, v));

        applySelectedState(activity, selectedTab, feed, watch, shop, alerts, menu);
    }

    private static void open(@NonNull AppCompatActivity activity, @NonNull Class<?> target) {
        if (activity.getClass().equals(target)) {
            return;
        }
        Intent i = new Intent(activity, target);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activity.startActivity(i);
    }

    private static void showMenu(@NonNull AppCompatActivity activity, @NonNull View anchor) {
        PopupMenu popup = new PopupMenu(activity, anchor);
        popup.getMenu().add(0, MENU_SAVED, 0, R.string.bottom_nav_menu_saved);
        popup.getMenu().add(0, MENU_SCHOOL, 1, R.string.bottom_nav_menu_school);
        popup.getMenu().add(0, MENU_ASSIGNMENT4, 2, R.string.activity3_menu_open_registry);
        popup.getMenu().add(0, MENU_ABOUT, 3, R.string.bottom_nav_menu_about);
        popup.setOnMenuItemClickListener(item -> onMenuItem(activity, item));
        popup.show();
    }

    private static boolean onMenuItem(@NonNull AppCompatActivity activity, @NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == MENU_SAVED) {
            open(activity, SecondActivity.class);
            return true;
        }
        if (id == MENU_SCHOOL) {
            open(activity, RecyclerViewActivity.class);
            return true;
        }
        if (id == MENU_ASSIGNMENT4) {
            open(activity, Activity3.class);
            return true;
        }
        if (id == MENU_ABOUT) {
            Toast.makeText(activity, R.string.toast_about_demo, Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    private static void applySelectedState(
            @NonNull AppCompatActivity activity,
            int selectedTab,
            @NonNull TextView feed,
            @NonNull TextView watch,
            @NonNull TextView shop,
            @NonNull TextView alerts,
            @NonNull TextView menu) {
        int selected = ContextCompat.getColor(activity, R.color.tab_indicator);
        int normal = ContextCompat.getColor(activity, R.color.bottom_icon);
        TextView[] items = {feed, watch, shop, alerts, menu};
        for (int i = 0; i < items.length; i++) {
            tintItem(items[i], i == selectedTab ? selected : normal);
        }
    }

    private static void tintItem(@NonNull TextView view, int color) {
        view.setTextColor(color);
        Drawable[] rel = view.getCompoundDrawablesRelative();
        Drawable top = rel[1];
        if (top != null) {
            Drawable tinted = DrawableCompat.wrap(top.mutate());
            DrawableCompat.setTint(tinted, color);
            view.setCompoundDrawablesRelativeWithIntrinsicBounds(null, tinted, null, null);
        }
    }
}
