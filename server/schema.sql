DROP TABLE IF EXISTS project_categories;
DROP TABLE IF EXISTS projects;

CREATE TABLE projects (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    estimate_hours INTEGER NOT NULL,
    due_date TEXT NOT NULL,
    image_key TEXT NOT NULL,
    status INTEGER NOT NULL,
    urgent INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE project_categories (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    project_id INTEGER NOT NULL,
    category_name TEXT NOT NULL,
    priority_level INTEGER NOT NULL,
    review_date TEXT NOT NULL,
    FOREIGN KEY(project_id) REFERENCES projects(id) ON DELETE CASCADE
);
