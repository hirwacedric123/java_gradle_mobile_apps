import os
import sqlite3
from flask import Flask, jsonify, redirect, render_template, request, url_for, g


BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DB_PATH = os.path.join(BASE_DIR, "assignment4.db")

app = Flask(__name__)


def get_db():
    if "db" not in g:
        g.db = sqlite3.connect(DB_PATH)
        g.db.row_factory = sqlite3.Row
        g.db.execute("PRAGMA foreign_keys = ON")
    return g.db


@app.teardown_appcontext
def close_db(_error):
    db = g.pop("db", None)
    if db is not None:
        db.close()


def init_db():
    db = get_db()
    with open(os.path.join(BASE_DIR, "schema.sql"), "r", encoding="utf-8") as f:
        db.executescript(f.read())
    db.commit()


def status_text(status):
    return {0: "Planned", 1: "Ongoing", 2: "Completed"}.get(status, "Planned")


def urgent_flag(payload):
    """Normalize urgent from JSON booleans, numeric flags, or HTML forms (1/0, on/off)."""
    if payload is None:
        return False
    v = payload.get("urgent") if hasattr(payload, "get") else payload
    if isinstance(v, bool):
        return v
    if isinstance(v, (int, float)):
        return v != 0
    if v is None:
        return False
    s = str(v).strip().lower()
    return s in ("1", "true", "yes", "on")


def fetch_all_records():
    db = get_db()
    projects = db.execute("SELECT * FROM projects ORDER BY due_date ASC").fetchall()
    out = []
    for p in projects:
        categories = db.execute(
            """
            SELECT id, category_name, priority_level, review_date
            FROM project_categories
            WHERE project_id = ?
            ORDER BY priority_level DESC
            """,
            (p["id"],),
        ).fetchall()
        out.append(
            {
                "id": p["id"],
                "title": p["title"],
                "estimate_hours": p["estimate_hours"],
                "due_date": p["due_date"],
                "image_key": p["image_key"],
                "status": p["status"],
                "status_text": status_text(p["status"]),
                "urgent": bool(p["urgent"]),
                "categories": [
                    {
                        "id": c["id"],
                        "category_name": c["category_name"],
                        "priority_level": c["priority_level"],
                        "review_date": c["review_date"],
                    }
                    for c in categories
                ],
            }
        )
    return out


def create_record(payload):
    db = get_db()
    cur = db.execute(
        """
        INSERT INTO projects(title, estimate_hours, due_date, image_key, status, urgent)
        VALUES (?, ?, ?, ?, ?, ?)
        """,
        (
            payload["title"],
            int(payload["estimate_hours"]),
            payload["due_date"],
            payload["image_key"],
            int(payload["status"]),
            1 if urgent_flag(payload) else 0,
        ),
    )
    project_id = cur.lastrowid
    db.execute(
        """
        INSERT INTO project_categories(project_id, category_name, priority_level, review_date)
        VALUES (?, ?, ?, ?)
        """,
        (
            project_id,
            payload["category_name"],
            int(payload["priority_level"]),
            payload["review_date"],
        ),
    )
    db.commit()
    return project_id


@app.get("/")
def home():
    records = fetch_all_records()
    return render_template("list.html", records=records)


@app.get("/records/new")
def new_record_form():
    return render_template("form.html", mode="create", record=None)


@app.post("/records/new")
def create_record_form():
    create_record(request.form)
    return redirect(url_for("home"))


@app.get("/records/<int:record_id>")
def detail(record_id):
    records = fetch_all_records()
    record = next((r for r in records if r["id"] == record_id), None)
    if record is None:
        return redirect(url_for("home"))
    return render_template("detail.html", record=record)


@app.get("/records/<int:record_id>/edit")
def edit_record_form(record_id):
    records = fetch_all_records()
    record = next((r for r in records if r["id"] == record_id), None)
    if record is None:
        return redirect(url_for("home"))
    category = record["categories"][0] if record["categories"] else None
    return render_template("form.html", mode="edit", record=record, category=category)


@app.post("/records/<int:record_id>/edit")
def edit_record_submit(record_id):
    db = get_db()
    db.execute(
        """
        UPDATE projects
        SET title=?, estimate_hours=?, due_date=?, image_key=?, status=?, urgent=?
        WHERE id=?
        """,
        (
            request.form["title"],
            int(request.form["estimate_hours"]),
            request.form["due_date"],
            request.form["image_key"],
            int(request.form["status"]),
            1 if urgent_flag(request.form) else 0,
            record_id,
        ),
    )
    db.execute("DELETE FROM project_categories WHERE project_id = ?", (record_id,))
    db.execute(
        """
        INSERT INTO project_categories(project_id, category_name, priority_level, review_date)
        VALUES (?, ?, ?, ?)
        """,
        (
            record_id,
            request.form["category_name"],
            int(request.form["priority_level"]),
            request.form["review_date"],
        ),
    )
    db.commit()
    return redirect(url_for("detail", record_id=record_id))


@app.post("/records/<int:record_id>/delete")
def delete_record(record_id):
    db = get_db()
    db.execute("DELETE FROM projects WHERE id = ?", (record_id,))
    db.commit()
    return redirect(url_for("home"))


@app.get("/api/records")
def api_list():
    return jsonify(fetch_all_records())


@app.get("/api/records/<int:record_id>")
def api_detail(record_id):
    records = fetch_all_records()
    record = next((r for r in records if r["id"] == record_id), None)
    if record is None:
        return jsonify({"error": "not found"}), 404
    return jsonify(record)


@app.post("/api/records")
def api_create():
    payload = request.get_json(force=True)
    project_id = create_record(payload)
    return jsonify({"id": project_id}), 201


@app.put("/api/records/<int:record_id>")
def api_update(record_id):
    payload = request.get_json(force=True)
    db = get_db()
    db.execute(
        """
        UPDATE projects
        SET title=?, estimate_hours=?, due_date=?, image_key=?, status=?, urgent=?
        WHERE id=?
        """,
        (
            payload["title"],
            int(payload["estimate_hours"]),
            payload["due_date"],
            payload["image_key"],
            int(payload["status"]),
            1 if urgent_flag(payload) else 0,
            record_id,
        ),
    )
    db.execute("DELETE FROM project_categories WHERE project_id = ?", (record_id,))
    db.execute(
        """
        INSERT INTO project_categories(project_id, category_name, priority_level, review_date)
        VALUES (?, ?, ?, ?)
        """,
        (
            record_id,
            payload["category_name"],
            int(payload["priority_level"]),
            payload["review_date"],
        ),
    )
    db.commit()
    return jsonify({"updated": True})


@app.delete("/api/records/<int:record_id>")
def api_delete(record_id):
    db = get_db()
    db.execute("DELETE FROM projects WHERE id = ?", (record_id,))
    db.commit()
    return jsonify({"deleted": True})


if __name__ == "__main__":
    if not os.path.exists(DB_PATH):
        with app.app_context():
            init_db()
    app.run(host="0.0.0.0", port=5000, debug=True)
