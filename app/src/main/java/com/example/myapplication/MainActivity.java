package com.example.myapplication;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.tabs.TabLayout;

import java.util.Optional;

public class MainActivity extends AppCompatActivity {

    private static final int MENU_BOTTOM_SAVED = 100;
    private static final int MENU_BOTTOM_SCHOOL = 101;
    private static final int MENU_BOTTOM_ABOUT = 102;
    private static final int MENU_BOTTOM_ASSIGNMENT4 = 103;

    /** Which main tab panel (0 Events, 1 Calendar, 2 Hosting) is visible; used for animated tab switches. */
    private int selectedPanelIndex = -1;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private Runnable pendingTabSwitchRunnable;

    private TabLayout tabLayoutMain;
    private final TextView[] bottomNavItems = new TextView[5];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        setupSearchAndCreate(toolbar);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayoutMain = tabLayout;
        setupTabs(tabLayout);
        initTabContent();
        setupCalendarTab();
        setupHostingTab();
        findViewById(R.id.contentEvents).post(this::runPopularCardsEntrance);
        setupBottomNav();
        setupLandingInteractions();
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                applyBottomNavHintForMainTab(tab.getPosition());
                switchTabWithLoading(tab.getPosition());
                if (tab.getPosition() == 1) {
                    applyCalendarTabStyle(tab, true);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab.getPosition() == 1) {
                    applyCalendarTabStyle(tab, false);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // no-op
            }
        });

        MaterialCardView cardToday = findViewById(R.id.cardToday);
        cardToday.setOnClickListener(v -> {
            v.animate().cancel();
            v.animate()
                    .scaleX(0.96f)
                    .scaleY(0.96f)
                    .setDuration(90)
                    .withEndAction(() -> v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(140)
                            .setInterpolator(new DecelerateInterpolator())
                            .withEndAction(() -> startActivity(
                                    new Intent(MainActivity.this, SecondActivity.class)))
                            .start())
                    .start();
        });

        TextView popularMenu = findViewById(R.id.textPopularMenu);
        popularMenu.setOnClickListener(this::showPopularOverflowMenu);

        CircularProgressIndicator loadingSpinner = findViewById(R.id.loadingSpinner);
        loadingSpinner.setIndeterminate(true);
    }

    private void initTabContent() {
        View[] panels = {
                findViewById(R.id.contentEvents),
                findViewById(R.id.contentCalendar),
                findViewById(R.id.contentHosting)
        };
        for (int i = 0; i < panels.length; i++) {
            panels[i].setVisibility(i == 0 ? View.VISIBLE : View.GONE);
            panels[i].setAlpha(1f);
            panels[i].setTranslationY(0f);
            panels[i].setTranslationX(0f);
        }
        selectedPanelIndex = 0;
    }

    /**
     * Shows a full-screen loading panel with spinner, then slides panels (clear “page load” feel).
     */
    private void switchTabWithLoading(int position) {
        if (selectedPanelIndex >= 0 && position == selectedPanelIndex) {
            return;
        }
        View overlay = findViewById(R.id.loadingOverlay);
        TextView message = findViewById(R.id.loadingMessage);
        CircularProgressIndicator spinner = findViewById(R.id.loadingSpinner);
        message.setText(R.string.tab_loading_message);
        spinner.setVisibility(View.VISIBLE);
        spinner.setIndeterminate(true);
        overlay.setVisibility(View.VISIBLE);
        overlay.setAlpha(0f);
        overlay.bringToFront();
        overlay.animate().alpha(1f).setDuration(200).start();

        if (pendingTabSwitchRunnable != null) {
            mainHandler.removeCallbacks(pendingTabSwitchRunnable);
        }
        pendingTabSwitchRunnable = () -> {
            pendingTabSwitchRunnable = null;
            showTabContentAnimated(position, () -> overlay.animate()
                    .alpha(0f)
                    .setDuration(260)
                    .withEndAction(() -> {
                        overlay.setVisibility(View.GONE);
                        overlay.setAlpha(0f);
                    })
                    .start());
        };
        mainHandler.postDelayed(pendingTabSwitchRunnable, 320);
    }

    private float loadingSlidePx(float dp) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private void showTabContentAnimated(int position, Runnable onComplete) {
        View[] panels = {
                findViewById(R.id.contentEvents),
                findViewById(R.id.contentCalendar),
                findViewById(R.id.contentHosting)
        };
        if (selectedPanelIndex < 0) {
            for (int i = 0; i < panels.length; i++) {
                panels[i].setVisibility(i == position ? View.VISIBLE : View.GONE);
                panels[i].setAlpha(1f);
                panels[i].setTranslationY(0f);
                panels[i].setTranslationX(0f);
            }
            selectedPanelIndex = position;
            Optional.ofNullable(onComplete).ifPresent(Runnable::run);
            return;
        }
        if (position == selectedPanelIndex) {
            Optional.ofNullable(onComplete).ifPresent(Runnable::run);
            return;
        }
        View prev = panels[selectedPanelIndex];
        View next = panels[position];
        final boolean forward = position > selectedPanelIndex;
        selectedPanelIndex = position;
        float exitX = forward ? -loadingSlidePx(56f) : loadingSlidePx(56f);
        float enterFromX = forward ? loadingSlidePx(64f) : -loadingSlidePx(64f);

        prev.animate().cancel();
        next.animate().cancel();

        prev.animate()
                .alpha(0f)
                .translationX(exitX)
                .setDuration(220)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    prev.setVisibility(View.GONE);
                    prev.setAlpha(1f);
                    prev.setTranslationX(0f);
                    prev.setTranslationY(0f);
                    next.setAlpha(0f);
                    next.setTranslationX(enterFromX);
                    next.setTranslationY(0f);
                    next.setVisibility(View.VISIBLE);
                    next.animate()
                            .alpha(1f)
                            .translationX(0f)
                            .setDuration(340)
                            .setInterpolator(new DecelerateInterpolator())
                            .withEndAction(() -> Optional.ofNullable(onComplete).ifPresent(Runnable::run))
                            .start();
                })
                .start();
    }

    private void runPopularCardsEntrance() {
        int[] ids = {R.id.cardPopularNeon, R.id.cardPopularPier, R.id.cardPopularMarket};
        float shift = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 32f, getResources().getDisplayMetrics());
        long step = 70L;
        for (int i = 0; i < ids.length; i++) {
            final long delayMs = step * i;
            View card = findViewById(ids[i]);
            if (card == null) {
                continue;
            }
            card.setAlpha(0f);
            card.setTranslationX(shift);
            card.setScaleX(0.94f);
            card.setScaleY(0.94f);
            card.postDelayed(() -> card.animate()
                    .alpha(1f)
                    .translationX(0f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(340)
                    .setInterpolator(new OvershootInterpolator(0.85f))
                    .start(), delayMs);
        }
    }

    private void setupLandingInteractions() {
        MaterialCardView cardTomorrow = findViewById(R.id.cardTomorrow);
        MaterialCardView cardWeekend = findViewById(R.id.cardWeekend);
        cardTomorrow.setOnClickListener(
                v -> {
                    bounceView(v);
                    Toast.makeText(this, R.string.toast_filter_tomorrow, Toast.LENGTH_SHORT).show();
                });
        cardWeekend.setOnClickListener(
                v -> {
                    bounceView(v);
                    Toast.makeText(this, R.string.toast_filter_weekend, Toast.LENGTH_SHORT).show();
                });

        wirePopularEventCard(R.id.cardPopularNeon, R.string.popular_neon_ships);
        wirePopularEventCard(R.id.cardPopularPier, R.string.popular_street_fair);
        wirePopularEventCard(R.id.cardPopularMarket, R.string.popular_night_market);
    }

    private void wirePopularEventCard(int cardId, int eventTitleRes) {
        MaterialCardView card = findViewById(cardId);
        card.setOnClickListener(
                v -> {
                    bounceView(v);
                    String title = getString(eventTitleRes);
                    Toast.makeText(this, getString(R.string.popular_event_open_format, title), Toast.LENGTH_SHORT).show();
                });
    }

    private void setupBottomNav() {
        bottomNavItems[0] = findViewById(R.id.bottomNavFeed);
        bottomNavItems[1] = findViewById(R.id.bottomNavWatch);
        bottomNavItems[2] = findViewById(R.id.bottomNavShop);
        bottomNavItems[3] = findViewById(R.id.bottomNavAlerts);
        bottomNavItems[4] = findViewById(R.id.bottomNavMenu);

        TypedValue selectable = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, selectable, true);
        int rippleResId = selectable.resourceId;

        for (TextView item : bottomNavItems) {
            item.setBackgroundResource(rippleResId);
        }

        bottomNavItems[0].setOnClickListener(v -> onBottomNavTap(0, this::scrollEventsFeedToTop));
        bottomNavItems[1].setOnClickListener(
                v -> onBottomNavTap(1, () -> startActivity(new Intent(MainActivity.this, WatchActivity.class))));
        bottomNavItems[2].setOnClickListener(
                v -> onBottomNavTap(2, () -> startActivity(new Intent(MainActivity.this, ShopActivity.class))));
        bottomNavItems[3].setOnClickListener(
                v -> onBottomNavTap(3, () -> startActivity(new Intent(MainActivity.this, AlertsActivity.class))));
        bottomNavItems[4].setOnClickListener(this::onBottomNavMenuClicked);

        setBottomNavSelected(4);
    }

    private void onBottomNavTap(int index, Runnable action) {
        bounceView(bottomNavItems[index]);
        setBottomNavSelected(index);
        action.run();
    }

    private void selectMainTabAndToast(int tabPosition, int toastMessageRes) {
        if (tabLayoutMain != null) {
            TabLayout.Tab tab = tabLayoutMain.getTabAt(tabPosition);
            if (tab != null) {
                tab.select();
            }
        }
        Toast.makeText(this, toastMessageRes, Toast.LENGTH_SHORT).show();
    }

    private void scrollEventsFeedToTop() {
        if (tabLayoutMain != null) {
            TabLayout.Tab tab = tabLayoutMain.getTabAt(0);
            if (tab != null) {
                tab.select();
            }
        }
        Toast.makeText(this, R.string.toast_bottom_feed, Toast.LENGTH_SHORT).show();
        View eventsScroll = findViewById(R.id.contentEvents);
        eventsScroll.postDelayed(
                () -> {
                    if (eventsScroll instanceof NestedScrollView) {
                        ((NestedScrollView) eventsScroll).smoothScrollTo(0, 0);
                    }
                },
                480);
    }

    private void onBottomNavMenuClicked(View anchor) {
        bounceView(anchor);
        setBottomNavSelected(4);
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, MENU_BOTTOM_SAVED, 0, R.string.bottom_nav_menu_saved);
        popup.getMenu().add(0, MENU_BOTTOM_SCHOOL, 1, R.string.bottom_nav_menu_school);
        popup.getMenu().add(0, MENU_BOTTOM_ASSIGNMENT4, 2, R.string.activity3_menu_open_registry);
        popup.getMenu().add(0, MENU_BOTTOM_ABOUT, 3, R.string.bottom_nav_menu_about);
        popup.setOnMenuItemClickListener(
                item -> {
                    int id = item.getItemId();
                    if (id == MENU_BOTTOM_SAVED) {
                        startActivity(new Intent(MainActivity.this, SecondActivity.class));
                        return true;
                    }
                    if (id == MENU_BOTTOM_SCHOOL) {
                        startActivity(new Intent(MainActivity.this, RecyclerViewActivity.class));
                        return true;
                    }
                    if (id == MENU_BOTTOM_ASSIGNMENT4) {
                        startActivity(new Intent(MainActivity.this, Activity3.class));
                        return true;
                    }
                    if (id == MENU_BOTTOM_ABOUT) {
                        Toast.makeText(MainActivity.this, R.string.toast_about_demo, Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    return false;
                });
        popup.show();
    }

    private void setBottomNavSelected(int index) {
        int selectedColor = ContextCompat.getColor(this, R.color.tab_indicator);
        int normalColor = ContextCompat.getColor(this, R.color.bottom_icon);
        for (int i = 0; i < bottomNavItems.length; i++) {
            applyBottomNavItemColors(bottomNavItems[i], i == index ? selectedColor : normalColor);
        }
    }

    private static void applyBottomNavItemColors(TextView item, int color) {
        item.setTextColor(color);
        Drawable[] rel = item.getCompoundDrawablesRelative();
        Drawable top = rel[1];
        if (top != null) {
            Drawable tinted = DrawableCompat.wrap(top.mutate());
            DrawableCompat.setTint(tinted, color);
            item.setCompoundDrawablesRelativeWithIntrinsicBounds(null, tinted, null, null);
        }
    }

    /** Highlights Alerts when Calendar opens; dims bottom nav on Hosting. Leaves Feed/Watch/Shop/Menu as-is on Events tab. */
    private void applyBottomNavHintForMainTab(int tabPosition) {
        if (tabPosition == 1) {
            setBottomNavSelected(3);
        } else if (tabPosition == 2) {
            clearBottomNavHighlight();
        }
    }

    private void clearBottomNavHighlight() {
        int muted = ContextCompat.getColor(this, R.color.bottom_icon);
        for (TextView item : bottomNavItems) {
            if (item != null) {
                applyBottomNavItemColors(item, muted);
            }
        }
    }

    private void bounceView(View v) {
        v.animate().cancel();
        v.animate()
                .scaleX(0.88f)
                .scaleY(0.88f)
                .setDuration(80)
                .withEndAction(() -> v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(160)
                        .setInterpolator(new OvershootInterpolator(1.1f))
                        .start())
                .start();
    }

    private void pulseView(View v) {
        v.animate().cancel();
        v.animate()
                .scaleX(1.03f)
                .scaleY(1.03f)
                .setDuration(100)
                .withEndAction(() -> v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(160)
                        .setInterpolator(new DecelerateInterpolator())
                        .start())
                .start();
    }

    private void showLoadingOverlay(Runnable afterHidden) {
        View overlay = findViewById(R.id.loadingOverlay);
        TextView message = findViewById(R.id.loadingMessage);
        CircularProgressIndicator spinner = findViewById(R.id.loadingSpinner);
        message.setText(R.string.search_loading_message);
        spinner.setIndeterminate(true);
        overlay.animate().cancel();
        overlay.bringToFront();
        overlay.setVisibility(View.VISIBLE);
        overlay.setAlpha(0f);
        spinner.setVisibility(View.VISIBLE);
        overlay.animate()
                .alpha(1f)
                .setDuration(200)
                .start();
        mainHandler.postDelayed(
                () ->
                        overlay.animate()
                                .alpha(0f)
                                .setDuration(240)
                                .withEndAction(
                                        () -> {
                                            overlay.setVisibility(View.GONE);
                                            overlay.setAlpha(0f);
                                            Optional.ofNullable(afterHidden).ifPresent(Runnable::run);
                                        })
                                .start(),
                700);
    }

    private void setupCalendarTab() {
        CalendarView calendarView = findViewById(R.id.calendarView);
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            pulseView(view);
            String msg = getString(R.string.calendar_date_toast, month + 1, dayOfMonth, year);
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });
    }

    private void setupHostingTab() {
        Spinner spinner = findViewById(R.id.spinnerHostingVisibility);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.hosting_visibility_options,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        final boolean[] first = {true};
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (first[0]) {
                    first[0] = false;
                    return;
                }
                bounceView(spinner);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // no-op
            }
        });
    }

    /**
     * Adds {@link SearchView} and Create control directly to the {@link Toolbar} so they always
     * appear on phones (menu action views are often dropped when horizontal space is tight).
     */
    private void setupSearchAndCreate(Toolbar toolbar) {
        SearchView searchView = new SearchView(toolbar.getContext());
        searchView.setId(View.generateViewId());
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setIconifiedByDefault(false);
        searchView.setSubmitButtonEnabled(false);
        styleSearchView(searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                showLoadingOverlay(
                        () ->
                                Toast.makeText(
                                                MainActivity.this,
                                                query == null || query.trim().isEmpty()
                                                        ? MainActivity.this.getString(R.string.search_cleared)
                                                        : MainActivity.this.getString(
                                                                R.string.search_toast_format, query.trim()),
                                                Toast.LENGTH_SHORT)
                                        .show());
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        LinearLayout row = new LinearLayout(toolbar.getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams searchLp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        row.addView(searchView, searchLp);

        TextView createBtn = new TextView(toolbar.getContext());
        createBtn.setText(R.string.menu_create);
        createBtn.setTextColor(ContextCompat.getColor(this, R.color.white));
        createBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
        int padH = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 10f, getResources().getDisplayMetrics());
        int padV = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 8f, getResources().getDisplayMetrics());
        createBtn.setPadding(padH, padV, padH, padV);
        TypedValue selectable = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, selectable, true);
        createBtn.setBackgroundResource(selectable.resourceId);
        createBtn.setOnClickListener(this::onToolbarCreateClicked);

        LinearLayout.LayoutParams createLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        createLp.gravity = Gravity.CENTER_VERTICAL;
        row.addView(createBtn, createLp);

        TextView schoolBtn = new TextView(toolbar.getContext());
        schoolBtn.setText(R.string.school_toolbar_short);
        schoolBtn.setTextColor(ContextCompat.getColor(this, R.color.white));
        schoolBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
        schoolBtn.setPadding(padH, padV, padH, padV);
        schoolBtn.setBackgroundResource(selectable.resourceId);
        schoolBtn.setContentDescription(getString(R.string.school_menu_open_registry));
        schoolBtn.setOnClickListener(this::onToolbarSchoolClicked);

        LinearLayout.LayoutParams schoolLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        schoolLp.gravity = Gravity.CENTER_VERTICAL;
        row.addView(schoolBtn, schoolLp);

        TextView a4Btn = new TextView(toolbar.getContext());
        a4Btn.setText(R.string.activity3_toolbar_short);
        a4Btn.setTextColor(ContextCompat.getColor(this, R.color.white));
        a4Btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
        a4Btn.setPadding(padH, padV, padH, padV);
        a4Btn.setBackgroundResource(selectable.resourceId);
        a4Btn.setContentDescription(getString(R.string.activity3_menu_open_registry));
        a4Btn.setOnClickListener(this::onToolbarAssignment4Clicked);

        LinearLayout.LayoutParams a4Lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        a4Lp.gravity = Gravity.CENTER_VERTICAL;
        row.addView(a4Btn, a4Lp);

        Toolbar.LayoutParams rowLp = new Toolbar.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        rowLp.gravity = Gravity.CENTER_VERTICAL;
        toolbar.addView(row, rowLp);
    }

    private void onToolbarCreateClicked(View v) {
        bounceView(v);
        startActivity(new Intent(this, CreateEventActivity.class));
    }

    private void onToolbarSchoolClicked(View v) {
        bounceView(v);
        startActivity(new Intent(this, RecyclerViewActivity.class));
    }

    private void onToolbarAssignment4Clicked(View v) {
        bounceView(v);
        startActivity(new Intent(this, Activity3.class));
    }

    private void showPopularOverflowMenu(View anchor) {
        bounceView(anchor);
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0, R.string.popular_menu_sort);
        popup.getMenu().add(0, 2, 1, R.string.popular_menu_share);
        popup.setOnMenuItemClickListener(item -> {
            Toast.makeText(this, item.getTitle(), Toast.LENGTH_SHORT).show();
            return true;
        });
        popup.show();
    }

    private void styleSearchView(SearchView searchView) {
        View plate = searchView.findViewById(androidx.appcompat.R.id.search_plate);
        if (plate != null) {
            plate.setBackground(null);
        }
        View editFrame = searchView.findViewById(androidx.appcompat.R.id.search_edit_frame);
        if (editFrame != null) {
            editFrame.setBackground(null);
        }
        int padH = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 8f, getResources().getDisplayMetrics());
        int padV = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 4f, getResources().getDisplayMetrics());
        searchView.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_toolbar_search_rounded));
        searchView.setPadding(padH, padV, padH, padV);

        int white = ContextCompat.getColor(this, R.color.white);
        int hint = Color.argb(200, 255, 255, 255);
        AutoCompleteTextView searchEdit = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        if (searchEdit != null) {
            searchEdit.setTextColor(white);
            searchEdit.setHintTextColor(hint);
            searchEdit.setBackground(null);
        }
        android.widget.ImageView close =
                searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        if (close != null) {
            close.setColorFilter(white);
        }
        android.widget.ImageView searchButton =
                searchView.findViewById(androidx.appcompat.R.id.search_mag_icon);
        if (searchButton != null) {
            searchButton.setColorFilter(white);
        }
    }

    private void setupTabs(TabLayout tabLayout) {
        tabLayout.removeAllTabs();
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_events));
        TabLayout.Tab calendarTab = tabLayout.newTab();
        calendarTab.setCustomView(buildCalendarTabView());
        tabLayout.addTab(calendarTab);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_hosting));
        tabLayout.selectTab(tabLayout.getTabAt(0));
        applyCalendarTabStyle(tabLayout.getTabAt(1), false);
    }

    private void applyCalendarTabStyle(TabLayout.Tab tab, boolean selected) {
        if (tab == null || tab.getCustomView() == null) {
            return;
        }
        View custom = tab.getCustomView();
        if (!(custom instanceof LinearLayout)) {
            return;
        }
        LinearLayout row = (LinearLayout) custom;
        if (row.getChildCount() < 2) {
            return;
        }
        TextView prefix = (TextView) row.getChildAt(0);
        TextView suffix = (TextView) row.getChildAt(1);
        int primary = ContextCompat.getColor(this, R.color.text_primary);
        int muted = ContextCompat.getColor(this, R.color.text_secondary);
        prefix.setTextColor(selected ? primary : muted);
        suffix.setTextColor(Color.RED);
    }

    private View buildCalendarTabView() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        TextView prefix = new TextView(this);
        prefix.setText(R.string.tab_calendar);
        prefix.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        prefix.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        TextView suffix = new TextView(this);
        suffix.setText(R.string.tab_calendar_suffix);
        suffix.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        suffix.setTextColor(Color.RED);
        row.addView(prefix);
        row.addView(suffix);
        row.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        row.setContentDescription(getString(R.string.tab_calendar) + getString(R.string.tab_calendar_suffix));
        return row;
    }
}
