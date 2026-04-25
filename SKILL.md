---
name: ai-tutor
description: Use when working on the AI Tutor codebase, PRD, or local setup. Covers the Spring Boot backend, Python FastAPI AI worker, Expo React Native mobile app, PostgreSQL data model, local image storage, Ollama/OpenAI tutor flows, focus/progress capture, study history, and common debugging commands.
---

# AI Tutor

## Orientation

Treat this repository as a mobile/tablet-first AI tutor MVP.

Core modules:

- `backend/`: Spring Boot API, product state, auth, assignments, sessions, tutor threads, storage, and worker calls.
- `workers/ai_worker/`: FastAPI worker for AI/CV/OCR-style analysis and local Ollama integration.
- `mobile/`: Expo React Native app for Android/iOS/tablets.
- `docs/ai-tutor-prd.md`: product requirements and technical architecture.

Primary product decisions:

- Use Spring Boot + Python workers + PostgreSQL.
- Use Expo React Native for mobile/tablet; ignore web for MVP.
- Use local auth, not Clerk/Auth0/Firebase/Cognito/Supabase Auth.
- Do not add Flyway, RabbitMQ, parent dashboard, team dashboard, or a Metabase Docker dependency.
- Metabase exists outside this repo and reads database views/queries.
- Default local AI provider is Ollama with `gemma3:latest`; OpenAI is optional.

## Implementation Workflow

1. Read the local code before changing behavior.
2. Keep changes scoped to the requested feature or bug.
3. Update `docs/ai-tutor-prd.md` when product behavior changes.
4. Update `README.md`, `backend/README.md`, `mobile/README.md`, or `ai-setup.md` only when setup/run behavior changes.
5. Preserve existing user edits and avoid unrelated refactors.

Use these boundaries:

- Spring owns business rules, persistence, session state, tutor policy, and image metadata.
- Python workers are mostly stateless and return structured analysis.
- Mobile owns camera capture, local nudges, voice alerts, and user interaction state.
- Local image files are stored under `/Users/vipul.pandey/images/{studentId}/{category}` by default.

## Local Commands

Backend validation:

```bash
cd /Users/vipul.pandey/projects/ai-tutor/backend
mvn -q -Dmaven.repo.local=/tmp/ai-tutor-m2 -DskipTests package
```

Python worker validation:

```bash
cd /Users/vipul.pandey/projects/ai-tutor
workers/ai_worker/.venv/bin/python -m compileall workers/ai_worker/app
```

Mobile validation:

```bash
cd /Users/vipul.pandey/projects/ai-tutor/mobile
./node_modules/.bin/tsc --noEmit --pretty false
```

Run Python worker:

```bash
cd /Users/vipul.pandey/projects/ai-tutor/workers/ai_worker
.venv/bin/uvicorn app.main:app --reload --port 8000
```

Run mobile on LAN:

```bash
cd /Users/vipul.pandey/projects/ai-tutor/mobile
source ~/.nvm/nvm.sh
nvm use 20.20.2
REACT_NATIVE_PACKAGER_HOSTNAME=192.168.1.9 EXPO_PUBLIC_API_BASE_URL=http://192.168.1.9:8080 EXPO_NO_DEPENDENCY_VALIDATION=1 ./node_modules/.bin/expo start --lan --port 8082 --clear
```

## Backend Notes

Use Spring Boot APIs for:

- Student auth and current student identity.
- Assignment creation, image upload, analysis, and plan generation.
- Study session start/pause/resume/end.
- Manual checkpoint completion.
- Progress captures and focus checks.
- Tutor threads and messages.
- Student study history: assignments, scanned images, extracted questions, tutor questions, asked-at times, finished-at time, and actual duration.

Worker calls should:

- Use JSON request bodies.
- Include `X-AI-Tutor-Worker-Token`.
- Log request body and worker response with truncation.
- Use HTTP/1.1 for Uvicorn compatibility.
- Allow slow local model responses with a five-minute timeout.

## Python Worker Notes

Defaults should work without environment variables:

- `AI_PROVIDER=ollama`
- `OLLAMA_BASE_URL=http://localhost:11434`
- `OLLAMA_MODEL=gemma3:latest`
- `PYTHON_WORKER_INTERNAL_TOKEN=dev-worker-token`

Keep worker endpoints internal:

- `/internal/analyze-assignment`
- `/internal/analyze-progress`
- `/internal/analyze-focus`
- `/internal/tutor-hint`

For malformed or empty bodies, return controlled `400/422` responses. For client disconnects, return/log controlled `499` instead of allowing ASGI tracebacks.

## Mobile Notes

Use Expo React Native and TypeScript.

Important behavior:

- Capture homework/classwork images.
- Show timeline/checkpoints.
- Allow manual checkpoint completion.
- Auto-capture progress images at 3 or 4 captures per minute during active study.
- Use front camera for focus checks.
- Show popup plus voice alert when the student is looking away or behind plan.
- Let the student tap a help/hand icon to ask tutor questions.
- Show a student-facing study record/history view.

When testing on a physical phone, `EXPO_PUBLIC_API_BASE_URL` must use the machine LAN IP, not `localhost`.

## Debugging Signals

- `ReadableStream is not defined`: Node is too old; use Node 20.
- Expo Go SDK mismatch: align project SDK with installed Expo Go or install matching Expo Go.
- `Unable to resolve react-native-web`: web path was triggered; MVP is mobile/tablet.
- `Unsupported upgrade request` in Uvicorn: likely HTTP upgrade/H2C probe; Spring worker client should force HTTP/1.1.
- `Worker client disconnected before body was read`: inspect Java worker client transport, timeout, and request body logging.
- `422 body Field required` from Python: inspect JSON serialization and `Content-Type`.
- `fetch failed` in Expo CLI: often dependency validation/network call; use offline/validation flags only when appropriate.
- `curl http://localhost:11434/api/tags` should show `gemma3:latest` before expecting local tutor responses.

## Validation Rule

After backend changes, run the Maven package command.

After worker changes, run Python compileall.

After mobile changes, run TypeScript compile.

If a command cannot be run, report that explicitly with the reason.
