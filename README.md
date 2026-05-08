# Mobile Apps Year 3

Android + Flask coursework project that combines:
- a native Android app (`app/`)
- a Flask + SQLite backend (`server/`)

The Android app includes multiple screens (navigation, lists, forms, RecyclerView, local DB) and an Assignment 4 flow that can sync with the Flask API.

## Tech Stack

- Android: Java, XML layouts, Gradle (Kotlin DSL), Volley
- Backend: Python 3, Flask, SQLite

## Repository Structure

```text
mobile_apps_year3/
├── app/                     # Android application module
├── server/                  # Flask backend + templates + static assets
├── gradle/                  # Gradle wrapper files
├── build.gradle.kts         # Root Gradle build
├── settings.gradle.kts      # Project settings
└── README.md
```

## Prerequisites

### Android
- Android Studio (latest stable recommended)
- Android SDK with API 36
- JDK 11

### Backend
- Python 3.10+ (3.11 recommended)
- `pip`

## Android App Setup

1. Open the project in Android Studio.
2. Let Gradle sync complete.
3. Build or run the app on an emulator/device.

Main module:
- Namespace: `com.example.myapplication`
- Compile SDK: `36`
- Min SDK: `24`

## Backend Setup (Flask)

From project root:

```bash
cd server
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
python app.py
```

The backend starts at:
- `http://127.0.0.1:5000`

On first run, `assignment4.db` is created automatically from `schema.sql`.

## Backend Routes

### Web pages
- `GET /` - records list
- `GET /records/new` - create form
- `POST /records/new` - create submit
- `GET /records/<id>` - record details
- `GET /records/<id>/edit` - edit form
- `POST /records/<id>/edit` - edit submit
- `POST /records/<id>/delete` - delete

### JSON API
- `GET /api/records`
- `GET /api/records/<id>`
- `POST /api/records`
- `PUT /api/records/<id>`
- `DELETE /api/records/<id>`

## Notes

- Keep local-only files out of git (`server/venv/`, `*.db`, `.gradle/`, local IDE files).
- `AndroidManifest.xml` includes `INTERNET` permission for API access.

## Author

Cedric (Year 3 Mobile Apps coursework)
