# AI Tutor API

Spring Boot API for the AI Tutor MVP.

## Responsibilities

- Local auth/session tokens.
- Student profile.
- Assignment lifecycle.
- Homework image upload.
- Timeline planning.
- Study sessions.
- Manual progress.
- Progress image analysis orchestration.
- Focus event storage.
- Hint-first tutor thread storage.

## Run

```bash
docker compose up -d postgres
cd backend
mvn spring-boot:run
```

## Important Environment Variables

```text
DATABASE_URL=jdbc:postgresql://localhost:5432/ai_tutor
DATABASE_USERNAME=ai_tutor
DATABASE_PASSWORD=ai_tutor
PYTHON_WORKER_BASE_URL=http://localhost:8000
PYTHON_WORKER_INTERNAL_TOKEN=dev-worker-token
LOCAL_STORAGE_ROOT=/Users/vipul.pandey/images
```

Captured homework and progress images are saved under `LOCAL_STORAGE_ROOT`, grouped by student id:

```text
{studentId}/assignments
{studentId}/progress
{studentId}/focus
```

No external auth provider, queue, or Flyway is used.

If Hibernate reports that it cannot determine the dialect, Postgres is usually not reachable or `DATABASE_URL` is empty/mis-set. For local defaults:

```bash
docker compose up -d postgres
unset DATABASE_URL
mvn spring-boot:run
```

This module includes project-local Maven settings under `.mvn/` so dependency resolution uses Maven Central instead of any user-level mirror configured for another workspace.
