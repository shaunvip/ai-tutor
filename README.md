# AI Tutor

Mobile/tablet-first AI tutor MVP.

The current scaffold follows the latest product decisions:

- Expo React Native app for iOS, Android, and tablets.
- Spring Boot API as the system of record.
- Python FastAPI worker for OCR/AI/CV processing.
- PostgreSQL for local data.
- Existing external Metabase can connect to the app database for analytics.
- No web/PWA app for MVP.
- No external auth provider for MVP.
- No RabbitMQ/managed queue for MVP.
- No Flyway.
- In-app nudges only for MVP.

## Project Layout

```text
backend/             Spring Boot API
workers/ai_worker/   Python internal worker
mobile/              Expo mobile/tablet app
docs/                PRD and architecture notes
docker-compose.yml   Local Postgres
```

## Run Local Infrastructure

```bash
docker compose up -d postgres
```

Connect your existing Metabase to this database:

```text
Host: localhost
Port: 5432
Database: ai_tutor
User: ai_tutor
Password: ai_tutor
```

## Run Python Worker

```bash
cd workers/ai_worker
python3 -m venv .venv
source .venv/bin/activate
pip install -e .
uvicorn app.main:app --reload --port 8000
```

The worker currently uses local heuristics. No API key is required.

## Run Spring Boot API

```bash
cd backend
mvn spring-boot:run
```

The API runs at:

```text
http://localhost:8080
```

Spring Boot creates the local schema using Hibernate `ddl-auto=update`.

If Hibernate reports that it cannot determine the dialect, check that Postgres is running and that `DATABASE_URL` is not exported as an empty value:

```bash
docker compose up -d postgres
unset DATABASE_URL
cd backend
mvn spring-boot:run
```

## Run Mobile App

Expo SDK 54 requires Node 20.19+. Use Node 20 before running the mobile app.

```bash
cd mobile
npm install
EXPO_PUBLIC_API_BASE_URL=http://localhost:8080 npm run start
```

This app is mobile/tablet only for MVP. Use Expo Go, an iOS simulator, an Android emulator, or a native dev build. Do not press `w` in Expo CLI; the web target is intentionally not installed.

For a physical phone, use your machine LAN IP instead of `localhost`:

```bash
EXPO_PUBLIC_API_BASE_URL=http://192.168.x.x:8080 npm run start
```

## Current MVP Capabilities

- Register/login through local Spring session-token auth.
- Capture homework image from the mobile app.
- Create assignment.
- Upload homework image.
- Analyze assignment through Python worker.
- Generate timeline steps.
- Start study session.
- Manually complete steps.
- Capture progress image.
- Record focus nudge events.
- Ask Tutor with hint-first placeholder responses.

## External API Keys

Not required for the scaffold. The Python worker falls back to local heuristics without keys.

Needed later:

- `OPENAI_API_KEY`: real homework understanding, tutor answers, image reasoning.
- `OPENAI_MODEL`: defaults to `gpt-4.1-mini` in local examples.
- Cloud OCR credentials: if using Google Vision, AWS Textract, or Azure Document Intelligence.
- Push credentials: only if alerts are needed while the app is closed/backgrounded.

## PRD

See [docs/ai-tutor-prd.md](/Users/vipul.pandey/projects/ai-tutor/docs/ai-tutor-prd.md).
