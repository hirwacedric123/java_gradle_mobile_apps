package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/** Assignment 4 local SQLite DB: parent project records + linked category rows. */
public final class Activity3DbHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "assignment4_registry.db";
    public static final int DB_VERSION = 1;

    public static final String TABLE_PROJECTS = "projects";
    public static final String COL_PROJECT_ID = "_id";
    public static final String COL_PROJECT_TITLE = "title";
    public static final String COL_PROJECT_ESTIMATE_HOURS = "estimate_hours";
    public static final String COL_PROJECT_DUE_DATE = "due_date";
    public static final String COL_PROJECT_IMAGE_KEY = "image_key";
    public static final String COL_PROJECT_STATUS = "status";
    public static final String COL_PROJECT_URGENT = "urgent";

    public static final String TABLE_PROJECT_CATEGORIES = "project_categories";
    public static final String COL_CATEGORY_ID = "_id";
    public static final String COL_CATEGORY_PROJECT_ID = "project_id";
    public static final String COL_CATEGORY_NAME = "category_name";
    public static final String COL_CATEGORY_PRIORITY = "priority_level";
    public static final String COL_CATEGORY_REVIEW_DATE = "review_date";

    private static final String CREATE_PROJECTS =
            "CREATE TABLE " + TABLE_PROJECTS + " ("
                    + COL_PROJECT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_PROJECT_TITLE + " TEXT NOT NULL, "
                    + COL_PROJECT_ESTIMATE_HOURS + " INTEGER NOT NULL, "
                    + COL_PROJECT_DUE_DATE + " TEXT NOT NULL, "
                    + COL_PROJECT_IMAGE_KEY + " TEXT NOT NULL, "
                    + COL_PROJECT_STATUS + " INTEGER NOT NULL, "
                    + COL_PROJECT_URGENT + " INTEGER NOT NULL"
                    + ")";

    private static final String CREATE_CATEGORIES =
            "CREATE TABLE " + TABLE_PROJECT_CATEGORIES + " ("
                    + COL_CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_CATEGORY_PROJECT_ID + " INTEGER NOT NULL, "
                    + COL_CATEGORY_NAME + " TEXT NOT NULL, "
                    + COL_CATEGORY_PRIORITY + " INTEGER NOT NULL, "
                    + COL_CATEGORY_REVIEW_DATE + " TEXT NOT NULL, "
                    + "FOREIGN KEY(" + COL_CATEGORY_PROJECT_ID + ") REFERENCES "
                    + TABLE_PROJECTS + "(" + COL_PROJECT_ID + ") ON DELETE CASCADE"
                    + ")";

    public Activity3DbHelper(@NonNull Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(@NonNull SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(@NonNull SQLiteDatabase db) {
        db.execSQL(CREATE_PROJECTS);
        db.execSQL(CREATE_CATEGORIES);
    }

    @Override
    public void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROJECT_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROJECTS);
        onCreate(db);
    }

    public long insertProjectWithCategory(
            @NonNull String title,
            int estimateHours,
            @NonNull String dueDateIso,
            @NonNull String imageKey,
            int status,
            boolean urgent,
            @NonNull String categoryName,
            int priorityLevel,
            @NonNull String reviewDateIso) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            long projectId = insertProjectInternal(db, title, estimateHours, dueDateIso, imageKey, status, urgent);
            if (projectId < 0) {
                return -1L;
            }
            insertCategoryInternal(db, projectId, categoryName, priorityLevel, reviewDateIso);
            db.setTransactionSuccessful();
            return projectId;
        } finally {
            db.endTransaction();
        }
    }

    public void updateProjectWithCategory(
            long projectId,
            @NonNull String title,
            int estimateHours,
            @NonNull String dueDateIso,
            @NonNull String imageKey,
            int status,
            boolean urgent,
            @NonNull String categoryName,
            int priorityLevel,
            @NonNull String reviewDateIso) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues projectValues = new ContentValues();
            projectValues.put(COL_PROJECT_TITLE, title);
            projectValues.put(COL_PROJECT_ESTIMATE_HOURS, estimateHours);
            projectValues.put(COL_PROJECT_DUE_DATE, dueDateIso);
            projectValues.put(COL_PROJECT_IMAGE_KEY, imageKey);
            projectValues.put(COL_PROJECT_STATUS, status);
            projectValues.put(COL_PROJECT_URGENT, urgent ? 1 : 0);
            db.update(TABLE_PROJECTS, projectValues, COL_PROJECT_ID + "=?", new String[]{String.valueOf(projectId)});

            db.delete(TABLE_PROJECT_CATEGORIES, COL_CATEGORY_PROJECT_ID + "=?", new String[]{String.valueOf(projectId)});
            insertCategoryInternal(db, projectId, categoryName, priorityLevel, reviewDateIso);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void deleteProject(long projectId) {
        getWritableDatabase().delete(TABLE_PROJECTS, COL_PROJECT_ID + "=?", new String[]{String.valueOf(projectId)});
    }

    @NonNull
    public List<Activity3Summary> queryAllProjectSummaries() {
        SQLiteDatabase db = getReadableDatabase();
        List<Activity3Summary> out = new ArrayList<>();
        String sql =
                "SELECT p." + COL_PROJECT_ID + ", p." + COL_PROJECT_TITLE + ", p." + COL_PROJECT_ESTIMATE_HOURS
                        + ", p." + COL_PROJECT_DUE_DATE + ", p." + COL_PROJECT_IMAGE_KEY + ", p."
                        + COL_PROJECT_STATUS + ", p." + COL_PROJECT_URGENT + ", COUNT(c." + COL_CATEGORY_ID + ") cc "
                        + "FROM " + TABLE_PROJECTS + " p LEFT JOIN " + TABLE_PROJECT_CATEGORIES + " c ON c."
                        + COL_CATEGORY_PROJECT_ID + "=p." + COL_PROJECT_ID + " "
                        + "GROUP BY p." + COL_PROJECT_ID + " "
                        + "ORDER BY p." + COL_PROJECT_DUE_DATE + " ASC";
        try (Cursor c = db.rawQuery(sql, null)) {
            while (c.moveToNext()) {
                out.add(new Activity3Summary(
                        c.getLong(0),
                        c.getString(1),
                        c.getInt(2),
                        c.getString(3),
                        c.getString(4),
                        c.getInt(5),
                        c.getInt(6) != 0,
                        c.getInt(7)));
            }
        }
        return out;
    }

    @Nullable
    public Activity3Summary queryProject(long projectId) {
        SQLiteDatabase db = getReadableDatabase();
        String sql =
                "SELECT p." + COL_PROJECT_ID + ", p." + COL_PROJECT_TITLE + ", p." + COL_PROJECT_ESTIMATE_HOURS
                        + ", p." + COL_PROJECT_DUE_DATE + ", p." + COL_PROJECT_IMAGE_KEY + ", p."
                        + COL_PROJECT_STATUS + ", p." + COL_PROJECT_URGENT + ", COUNT(c." + COL_CATEGORY_ID + ") cc "
                        + "FROM " + TABLE_PROJECTS + " p LEFT JOIN " + TABLE_PROJECT_CATEGORIES + " c ON c."
                        + COL_CATEGORY_PROJECT_ID + "=p." + COL_PROJECT_ID + " "
                        + "WHERE p." + COL_PROJECT_ID + "=? GROUP BY p." + COL_PROJECT_ID;
        try (Cursor c = db.rawQuery(sql, new String[]{String.valueOf(projectId)})) {
            if (!c.moveToFirst()) {
                return null;
            }
            return new Activity3Summary(
                    c.getLong(0),
                    c.getString(1),
                    c.getInt(2),
                    c.getString(3),
                    c.getString(4),
                    c.getInt(5),
                    c.getInt(6) != 0,
                    c.getInt(7));
        }
    }

    @Nullable
    public Activity3CategoryRow queryFirstCategory(long projectId) {
        List<Activity3CategoryRow> list = queryCategories(projectId);
        return list.isEmpty() ? null : list.get(0);
    }

    @NonNull
    public List<Activity3CategoryRow> queryCategories(long projectId) {
        SQLiteDatabase db = getReadableDatabase();
        List<Activity3CategoryRow> out = new ArrayList<>();
        try (Cursor c = db.query(
                TABLE_PROJECT_CATEGORIES,
                new String[]{
                        COL_CATEGORY_ID, COL_CATEGORY_PROJECT_ID, COL_CATEGORY_NAME, COL_CATEGORY_PRIORITY, COL_CATEGORY_REVIEW_DATE
                },
                COL_CATEGORY_PROJECT_ID + "=?",
                new String[]{String.valueOf(projectId)},
                null,
                null,
                COL_CATEGORY_PRIORITY + " DESC")) {
            while (c.moveToNext()) {
                out.add(new Activity3CategoryRow(
                        c.getLong(0),
                        c.getLong(1),
                        c.getString(2),
                        c.getInt(3),
                        c.getString(4)));
            }
        }
        return out;
    }

    public void replaceFromServer(@NonNull List<Activity3Summary> summaries, @NonNull List<Activity3CategoryRow> categories) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_PROJECT_CATEGORIES, null, null);
            db.delete(TABLE_PROJECTS, null, null);
            for (Activity3Summary summary : summaries) {
                ContentValues p = new ContentValues();
                p.put(COL_PROJECT_ID, summary.id);
                p.put(COL_PROJECT_TITLE, summary.title);
                p.put(COL_PROJECT_ESTIMATE_HOURS, summary.estimateHours);
                p.put(COL_PROJECT_DUE_DATE, summary.dueDateIso);
                p.put(COL_PROJECT_IMAGE_KEY, summary.imageKey);
                p.put(COL_PROJECT_STATUS, summary.status);
                p.put(COL_PROJECT_URGENT, summary.urgent ? 1 : 0);
                db.insert(TABLE_PROJECTS, null, p);
            }
            for (Activity3CategoryRow category : categories) {
                ContentValues c = new ContentValues();
                c.put(COL_CATEGORY_ID, category.id);
                c.put(COL_CATEGORY_PROJECT_ID, category.projectId);
                c.put(COL_CATEGORY_NAME, category.categoryName);
                c.put(COL_CATEGORY_PRIORITY, category.priorityLevel);
                c.put(COL_CATEGORY_REVIEW_DATE, category.reviewDateIso);
                db.insert(TABLE_PROJECT_CATEGORIES, null, c);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private static long insertProjectInternal(
            @NonNull SQLiteDatabase db,
            @NonNull String title,
            int estimateHours,
            @NonNull String dueDateIso,
            @NonNull String imageKey,
            int status,
            boolean urgent) {
        ContentValues values = new ContentValues();
        values.put(COL_PROJECT_TITLE, title);
        values.put(COL_PROJECT_ESTIMATE_HOURS, estimateHours);
        values.put(COL_PROJECT_DUE_DATE, dueDateIso);
        values.put(COL_PROJECT_IMAGE_KEY, imageKey);
        values.put(COL_PROJECT_STATUS, status);
        values.put(COL_PROJECT_URGENT, urgent ? 1 : 0);
        return db.insert(TABLE_PROJECTS, null, values);
    }

    private static void insertCategoryInternal(
            @NonNull SQLiteDatabase db,
            long projectId,
            @NonNull String categoryName,
            int priorityLevel,
            @NonNull String reviewDateIso) {
        ContentValues values = new ContentValues();
        values.put(COL_CATEGORY_PROJECT_ID, projectId);
        values.put(COL_CATEGORY_NAME, categoryName);
        values.put(COL_CATEGORY_PRIORITY, priorityLevel);
        values.put(COL_CATEGORY_REVIEW_DATE, reviewDateIso);
        db.insert(TABLE_PROJECT_CATEGORIES, null, values);
    }
}
