package com.example.myapplication;

/** Recycler row projection joining Assignment 4 parent table with linked category row count. */
public final class Activity3Summary {

    public final long id;
    public final String title;
    public final int estimateHours;
    public final String dueDateIso;
    public final String imageKey;
    /** 0 = planned, 1 = ongoing, 2 = completed. */
    public final int status;
    public final boolean urgent;
    public final int categoryCount;

    public Activity3Summary(
            long id,
            String title,
            int estimateHours,
            String dueDateIso,
            String imageKey,
            int status,
            boolean urgent,
            int categoryCount) {
        this.id = id;
        this.title = title;
        this.estimateHours = estimateHours;
        this.dueDateIso = dueDateIso;
        this.imageKey = imageKey;
        this.status = status;
        this.urgent = urgent;
        this.categoryCount = categoryCount;
    }
}
