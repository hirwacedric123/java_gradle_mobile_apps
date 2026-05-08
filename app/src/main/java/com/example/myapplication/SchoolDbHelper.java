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

/**
 * Two linked tables for the “School” domain: school identity and linked courses (FK {@link #COL_COURSE_SCHOOL_ID}).
 */
public final class SchoolDbHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "school_registry.db";
    public static final int DB_VERSION = 1;

    public static final String TABLE_SCHOOLS = "schools";
    public static final String COL_SCHOOL_ID = "_id";
    public static final String COL_SCHOOL_NAME = "name";
    /** Integer year founded. */
    public static final String COL_SCHOOL_FOUNDED_YEAR = "founded_year";
    /** Key into campus drawables (see {@link SchoolUi#drawableForCampusKey(Context, String)}). */
    public static final String COL_SCHOOL_CAMPUS_KEY = "campus_image_key";
    /** ISO-8601 date string (yyyy-MM-dd). */
    public static final String COL_SCHOOL_OPENED_ON = "opened_on";
    /** 0 = public, 1 = private. */
    public static final String COL_SCHOOL_TYPE = "school_type";
    public static final String COL_SCHOOL_ACCREDITED = "accredited";

    public static final String TABLE_SCHOOL_COURSES = "school_courses";
    public static final String COL_COURSE_ID = "_id";
    public static final String COL_COURSE_SCHOOL_ID = "school_id";
    public static final String COL_COURSE_NAME = "course_name";
    public static final String COL_COURSE_CREDITS = "credits";
    public static final String COL_COURSE_TERM_START = "term_start";

    private static final String CREATE_SCHOOLS =
            "CREATE TABLE " + TABLE_SCHOOLS + " ("
                    + COL_SCHOOL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_SCHOOL_NAME + " TEXT NOT NULL, "
                    + COL_SCHOOL_FOUNDED_YEAR + " INTEGER NOT NULL, "
                    + COL_SCHOOL_CAMPUS_KEY + " TEXT NOT NULL, "
                    + COL_SCHOOL_OPENED_ON + " TEXT NOT NULL, "
                    + COL_SCHOOL_TYPE + " INTEGER NOT NULL, "
                    + COL_SCHOOL_ACCREDITED + " INTEGER NOT NULL"
                    + ")";

    private static final String CREATE_COURSES =
            "CREATE TABLE " + TABLE_SCHOOL_COURSES + " ("
                    + COL_COURSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_COURSE_SCHOOL_ID + " INTEGER NOT NULL, "
                    + COL_COURSE_NAME + " TEXT NOT NULL, "
                    + COL_COURSE_CREDITS + " INTEGER NOT NULL, "
                    + COL_COURSE_TERM_START + " TEXT NOT NULL, "
                    + "FOREIGN KEY(" + COL_COURSE_SCHOOL_ID + ") REFERENCES "
                    + TABLE_SCHOOLS + "(" + COL_SCHOOL_ID + ") ON DELETE CASCADE"
                    + ")";

    public SchoolDbHelper(@NonNull Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(@NonNull SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(@NonNull SQLiteDatabase db) {
        db.execSQL(CREATE_SCHOOLS);
        db.execSQL(CREATE_COURSES);
        seedIfEmpty(db);
    }

    @Override
    public void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCHOOL_COURSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCHOOLS);
        onCreate(db);
    }

    private void seedIfEmpty(@NonNull SQLiteDatabase db) {
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_SCHOOLS, null);
        try {
            if (c.moveToFirst() && c.getInt(0) == 0) {
                long id1 = insertSchoolInternal(
                        db,
                        "Riverside Academy",
                        1962,
                        "campus_modern",
                        "1962-09-01",
                        0,
                        true);
                insertCourseInternal(db, id1, "Biology 101", 4, "2026-01-12");

                long id2 = insertSchoolInternal(
                        db,
                        "Hillcrest Institute",
                        1998,
                        "campus_green",
                        "1998-08-20",
                        1,
                        false);
                insertCourseInternal(db, id2, "Mobile Apps", 3, "2026-02-03");
            }
        } finally {
            c.close();
        }
    }

    private static long insertSchoolInternal(
            SQLiteDatabase db,
            String name,
            int foundedYear,
            String campusKey,
            String openedOnIso,
            int schoolType,
            boolean accredited) {
        ContentValues v = new ContentValues();
        v.put(COL_SCHOOL_NAME, name);
        v.put(COL_SCHOOL_FOUNDED_YEAR, foundedYear);
        v.put(COL_SCHOOL_CAMPUS_KEY, campusKey);
        v.put(COL_SCHOOL_OPENED_ON, openedOnIso);
        v.put(COL_SCHOOL_TYPE, schoolType);
        v.put(COL_SCHOOL_ACCREDITED, accredited ? 1 : 0);
        return db.insert(TABLE_SCHOOLS, null, v);
    }

    private static void insertCourseInternal(
            SQLiteDatabase db, long schoolId, String courseName, int credits, String termStartIso) {
        ContentValues v = new ContentValues();
        v.put(COL_COURSE_SCHOOL_ID, schoolId);
        v.put(COL_COURSE_NAME, courseName);
        v.put(COL_COURSE_CREDITS, credits);
        v.put(COL_COURSE_TERM_START, termStartIso);
        db.insert(TABLE_SCHOOL_COURSES, null, v);
    }

    /** Insert school and one linked course in a single transaction. */
    public long insertSchoolWithCourse(
            @NonNull String name,
            int foundedYear,
            @NonNull String campusKey,
            @NonNull String openedOnIso,
            int schoolType,
            boolean accredited,
            @NonNull String courseName,
            int credits,
            @NonNull String termStartIso) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            long schoolId = insertSchoolInternal(db, name, foundedYear, campusKey, openedOnIso, schoolType, accredited);
            if (schoolId < 0) {
                return -1;
            }
            insertCourseInternal(db, schoolId, courseName, credits, termStartIso);
            db.setTransactionSuccessful();
            return schoolId;
        } finally {
            db.endTransaction();
        }
    }

    public void updateSchoolWithCourse(
            long schoolId,
            @NonNull String name,
            int foundedYear,
            @NonNull String campusKey,
            @NonNull String openedOnIso,
            int schoolType,
            boolean accredited,
            @NonNull String courseName,
            int credits,
            @NonNull String termStartIso) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues sv = new ContentValues();
            sv.put(COL_SCHOOL_NAME, name);
            sv.put(COL_SCHOOL_FOUNDED_YEAR, foundedYear);
            sv.put(COL_SCHOOL_CAMPUS_KEY, campusKey);
            sv.put(COL_SCHOOL_OPENED_ON, openedOnIso);
            sv.put(COL_SCHOOL_TYPE, schoolType);
            sv.put(COL_SCHOOL_ACCREDITED, accredited ? 1 : 0);
            db.update(TABLE_SCHOOLS, sv, COL_SCHOOL_ID + "=?", new String[]{String.valueOf(schoolId)});

            db.delete(TABLE_SCHOOL_COURSES, COL_COURSE_SCHOOL_ID + "=?", new String[]{String.valueOf(schoolId)});
            insertCourseInternal(db, schoolId, courseName, credits, termStartIso);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @NonNull
    public List<SchoolSummary> queryAllSchoolSummaries() {
        SQLiteDatabase db = getReadableDatabase();
        List<SchoolSummary> out = new ArrayList<>();
        String sql =
                "SELECT s." + COL_SCHOOL_ID + ", s." + COL_SCHOOL_NAME + ", s." + COL_SCHOOL_FOUNDED_YEAR + ", s."
                        + COL_SCHOOL_CAMPUS_KEY + ", s." + COL_SCHOOL_OPENED_ON + ", s." + COL_SCHOOL_TYPE + ", s."
                        + COL_SCHOOL_ACCREDITED + ", COUNT(c." + COL_COURSE_ID + ") AS cc "
                        + "FROM " + TABLE_SCHOOLS + " s LEFT JOIN " + TABLE_SCHOOL_COURSES + " c ON c."
                        + COL_COURSE_SCHOOL_ID + " = s." + COL_SCHOOL_ID + " "
                        + "GROUP BY s." + COL_SCHOOL_ID + " "
                        + "ORDER BY s." + COL_SCHOOL_NAME + " COLLATE NOCASE";
        try (Cursor c = db.rawQuery(sql, null)) {
            while (c.moveToNext()) {
                out.add(
                        new SchoolSummary(
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
    public SchoolSummary querySchool(long schoolId) {
        SQLiteDatabase db = getReadableDatabase();
        String sql =
                "SELECT s." + COL_SCHOOL_ID + ", s." + COL_SCHOOL_NAME + ", s." + COL_SCHOOL_FOUNDED_YEAR + ", s."
                        + COL_SCHOOL_CAMPUS_KEY + ", s." + COL_SCHOOL_OPENED_ON + ", s." + COL_SCHOOL_TYPE + ", s."
                        + COL_SCHOOL_ACCREDITED + ", COUNT(c." + COL_COURSE_ID + ") AS cc "
                        + "FROM " + TABLE_SCHOOLS + " s LEFT JOIN " + TABLE_SCHOOL_COURSES + " c ON c."
                        + COL_COURSE_SCHOOL_ID + " = s." + COL_SCHOOL_ID + " "
                        + "WHERE s." + COL_SCHOOL_ID + "=? "
                        + "GROUP BY s." + COL_SCHOOL_ID;
        try (Cursor c = db.rawQuery(sql, new String[]{String.valueOf(schoolId)})) {
            if (!c.moveToFirst()) {
                return null;
            }
            return new SchoolSummary(
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

    @NonNull
    public List<CourseRow> queryCoursesForSchool(long schoolId) {
        SQLiteDatabase db = getReadableDatabase();
        List<CourseRow> out = new ArrayList<>();
        try (Cursor c = db.query(
                TABLE_SCHOOL_COURSES,
                new String[]{COL_COURSE_ID, COL_COURSE_SCHOOL_ID, COL_COURSE_NAME, COL_COURSE_CREDITS, COL_COURSE_TERM_START},
                COL_COURSE_SCHOOL_ID + "=?",
                new String[]{String.valueOf(schoolId)},
                null,
                null,
                COL_COURSE_NAME + " COLLATE NOCASE")) {
            while (c.moveToNext()) {
                out.add(
                        new CourseRow(
                                c.getLong(0),
                                c.getLong(1),
                                c.getString(2),
                                c.getInt(3),
                                c.getString(4)));
            }
        }
        return out;
    }

    /** First course row for this school (for edit form), if any. */
    @Nullable
    public CourseRow queryFirstCourse(long schoolId) {
        List<CourseRow> list = queryCoursesForSchool(schoolId);
        return list.isEmpty() ? null : list.get(0);
    }
}
