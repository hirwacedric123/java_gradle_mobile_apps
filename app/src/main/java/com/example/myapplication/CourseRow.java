package com.example.myapplication;

/** One row in {@link SchoolDbHelper#TABLE_SCHOOL_COURSES}. */
public final class CourseRow {

    public final long id;
    public final long schoolId;
    public final String courseName;
    public final int credits;
    public final String termStartIso;

    public CourseRow(long id, long schoolId, String courseName, int credits, String termStartIso) {
        this.id = id;
        this.schoolId = schoolId;
        this.courseName = courseName;
        this.credits = credits;
        this.termStartIso = termStartIso;
    }
}
