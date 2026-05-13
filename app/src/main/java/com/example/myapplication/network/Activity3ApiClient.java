package com.example.myapplication.network;

import android.content.Context;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapplication.Activity3CategoryRow;
import com.example.myapplication.Activity3Summary;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class Activity3ApiClient {

    public interface SyncCallback {
        void onSuccess(@NonNull List<Activity3Summary> summaries, @NonNull List<Activity3CategoryRow> categories);

        void onError(@NonNull Exception e);
    }

    /**
     * Emulator: use http://10.0.2.2:5000/api to reach the host machine.
     * Real phone: use your PC LAN IP shown by Flask (e.g. http://192.168.1.128:5000/api).
     */
    private static final String BASE_URL = "http://192.168.1.128:5000/api";
    private final RequestQueue queue;

    public Activity3ApiClient(@NonNull Context context) {
        this.queue = Volley.newRequestQueue(context.getApplicationContext());
    }

    public void createRecord(
            @NonNull String title,
            int estimateHours,
            @NonNull String dueDate,
            @NonNull String imageKey,
            int status,
            boolean urgent,
            @NonNull String categoryName,
            int priorityLevel,
            @NonNull String reviewDate,
            @NonNull Runnable onOk,
            @NonNull Response.ErrorListener onError) {
        JSONObject body = new JSONObject();
        try {
            body.put("title", title);
            body.put("estimate_hours", estimateHours);
            body.put("due_date", dueDate);
            body.put("image_key", imageKey);
            body.put("status", status);
            body.put("urgent", urgent);
            body.put("category_name", categoryName);
            body.put("priority_level", priorityLevel);
            body.put("review_date", reviewDate);
        } catch (JSONException e) {
            onError.onErrorResponse(new com.android.volley.VolleyError(e));
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                BASE_URL + "/records",
                body,
                response -> onOk.run(),
                onError);
        queue.add(request);
    }

    public void fetchAll(@NonNull SyncCallback callback) {
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                BASE_URL + "/records",
                null,
                response -> {
                    try {
                        Parsed parsed = parse(response);
                        callback.onSuccess(parsed.summaries, parsed.categories);
                    } catch (Exception e) {
                        callback.onError(e);
                    }
                },
                error -> callback.onError(error));
        queue.add(request);
    }

    public void deleteRecord(
            long recordId,
            @NonNull Runnable onOk,
            @NonNull Response.ErrorListener onError) {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.DELETE,
                BASE_URL + "/records/" + recordId,
                null,
                response -> onOk.run(),
                onError);
        queue.add(request);
    }

    private static Parsed parse(@NonNull JSONArray response) throws JSONException {
        List<Activity3Summary> summaries = new ArrayList<>();
        List<Activity3CategoryRow> categories = new ArrayList<>();
        for (int i = 0; i < response.length(); i++) {
            JSONObject item = response.getJSONObject(i);
            long id = item.getLong("id");
            JSONArray cats = item.optJSONArray("categories");
            summaries.add(new Activity3Summary(
                    id,
                    item.getString("title"),
                    item.getInt("estimate_hours"),
                    item.getString("due_date"),
                    item.getString("image_key"),
                    item.getInt("status"),
                    item.getBoolean("urgent"),
                    cats == null ? 0 : cats.length()));

            if (cats != null) {
                for (int c = 0; c < cats.length(); c++) {
                    JSONObject cat = cats.getJSONObject(c);
                    categories.add(new Activity3CategoryRow(
                            cat.getLong("id"),
                            id,
                            cat.getString("category_name"),
                            cat.getInt("priority_level"),
                            cat.getString("review_date")));
                }
            }
        }
        return new Parsed(summaries, categories);
    }

    private static final class Parsed {
        final List<Activity3Summary> summaries;
        final List<Activity3CategoryRow> categories;

        Parsed(List<Activity3Summary> summaries, List<Activity3CategoryRow> categories) {
            this.summaries = summaries;
            this.categories = categories;
        }
    }
}
