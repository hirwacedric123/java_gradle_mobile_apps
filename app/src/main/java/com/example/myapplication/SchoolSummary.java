package com.example.myapplication;

/**
 * Row for {@link androidx.recyclerview.widget.RecyclerView} list: {@link SchoolDbHelper#TABLE_SCHOOLS}
 * with aggregated course count from {@link SchoolDbHelper#TABLE_SCHOOL_COURSES}.
 */
public final class SchoolSummary {

    public final long id;
    public final String name;
    public final int foundedYear;
    public final String campusImageKey;
    public final String openedOnIso;
    /** 0 = public, 1 = private. */
    public final int schoolType;
    public final boolean accredited;
    public final int courseCount;

    public SchoolSummary(
            long id,
            String name,
            int foundedYear,
            String campusImageKey,
            String openedOnIso,
            int schoolType,
            boolean accredited,
            int courseCount) {
        this.id = id;
        this.name = name;
        this.foundedYear = foundedYear;
        this.campusImageKey = campusImageKey;
        this.openedOnIso = openedOnIso;
        this.schoolType = schoolType;
        this.accredited = accredited;
        this.courseCount = courseCount;
    }
}
