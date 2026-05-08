package com.example.myapplication;

/** One row in Activity 3 linked categories table. */
public final class Activity3CategoryRow {

    public final long id;
    public final long projectId;
    public final String categoryName;
    public final int priorityLevel;
    public final String reviewDateIso;

    public Activity3CategoryRow(
            long id, long projectId, String categoryName, int priorityLevel, String reviewDateIso) {
        this.id = id;
        this.projectId = projectId;
        this.categoryName = categoryName;
        this.priorityLevel = priorityLevel;
        this.reviewDateIso = reviewDateIso;
    }
}
