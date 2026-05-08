package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

import java.util.ArrayList;

/** Persistent storage for events created from {@link CreateEventActivity}. */
public final class UserEventsDbHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "user_events.db";
    private static final int VERSION = 1;

    public static final String TABLE_EVENTS = "user_created_events";
    public static final String COL_ID = "_id";
    public static final String COL_TITLE = "title";
    public static final String COL_LOCATION = "location";
    public static final String COL_DATE = "event_date";
    public static final String COL_TIME = "event_time";
    public static final String COL_NOTES = "notes";
    public static final String COL_CATEGORY = "category";
    public static final String COL_PUBLIC = "is_public";
    public static final String COL_CREATED_AT = "created_at_ms";

    private static final String CREATE_TABLE =
            "CREATE TABLE "
                    + TABLE_EVENTS
                    + " ("
                    + COL_ID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_TITLE
                    + " TEXT NOT NULL, "
                    + COL_LOCATION
                    + " TEXT NOT NULL, "
                    + COL_DATE
                    + " TEXT NOT NULL, "
                    + COL_TIME
                    + " TEXT NOT NULL, "
                    + COL_NOTES
                    + " TEXT NOT NULL DEFAULT '', "
                    + COL_CATEGORY
                    + " TEXT NOT NULL, "
                    + COL_PUBLIC
                    + " INTEGER NOT NULL DEFAULT 1, "
                    + COL_CREATED_AT
                    + " INTEGER NOT NULL"
                    + ")";

    public UserEventsDbHelper(@NonNull Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(@NonNull SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        onCreate(db);
    }

    public long insertEvent(
            @NonNull String title,
            @NonNull String location,
            @NonNull String dateIso,
            @NonNull String timeText,
            @NonNull String notes,
            @NonNull String category,
            boolean isPublic) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COL_TITLE, title);
        v.put(COL_LOCATION, location);
        v.put(COL_DATE, dateIso);
        v.put(COL_TIME, timeText);
        v.put(COL_NOTES, notes);
        v.put(COL_CATEGORY, category);
        v.put(COL_PUBLIC, isPublic ? 1 : 0);
        v.put(COL_CREATED_AT, System.currentTimeMillis());
        return db.insert(TABLE_EVENTS, null, v);
    }

    /** One line per row for {@link android.widget.ListView} display (newest first). */
    @NonNull
    public ArrayList<String> readSummariesNewestFirst(@NonNull Context context) {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<String> out = new ArrayList<>();
        try (Cursor c =
                db.query(
                        TABLE_EVENTS,
                        new String[]{COL_TITLE, COL_LOCATION, COL_DATE, COL_TIME, COL_CATEGORY},
                        null,
                        null,
                        null,
                        null,
                        COL_CREATED_AT + " DESC")) {
            while (c.moveToNext()) {
                String title = c.getString(0);
                String loc = c.getString(1);
                String date = c.getString(2);
                String time = c.getString(3);
                String cat = c.getString(4);
                String line =
                        context.getString(R.string.event_saved_line_format, title, date, time, loc, cat);
                out.add(line);
            }
        }
        return out;
    }
}
