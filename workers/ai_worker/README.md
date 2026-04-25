# AI Tutor Python Worker

Internal FastAPI service for assignment analysis, progress analysis, focus analysis, and tutor hints.

Spring Boot calls this worker through internal JSON APIs. The worker is mostly stateless; Spring owns database writes and product state.

Current local default:

```text
AI_PROVIDER=ollama
OLLAMA_BASE_URL=http://localhost:11434
OLLAMA_MODEL=gemma3:latest
PYTHON_WORKER_INTERNAL_TOKEN=dev-worker-token
```

No OpenAI API key is required for the MVP path.

## 1. Prerequisites

- Python 3.11+.
- Ollama running locally on port `11434`.
- `gemma3:latest` pulled in Ollama.

Check Python:

```bash
python3 --version
```

Check Ollama:

```bash
curl http://localhost:11434/api/tags
```

Expected model entry:

```text
gemma3:latest
```

If missing:

```bash
ollama pull gemma3
```

If Ollama is not running:

```bash
ollama serve
```

If `ollama serve` says port `11434` is already in use, Ollama is already running.

## 2. First-Time Worker Setup

From the repo root:

```bash
cd /Users/vipul.pandey/projects/ai-tutor/workers/ai_worker
python3 -m venv .venv
source .venv/bin/activate
python -m pip install --upgrade pip
python -m pip install -e .
```

Optional CV/OCR dependencies can be installed later:

```bash
python -m pip install -e ".[cv,ocr]"
```

## 3. Run Worker

Use this command for normal local development:

```bash
cd /Users/vipul.pandey/projects/ai-tutor/workers/ai_worker
.venv/bin/uvicorn app.main:app --reload --host 127.0.0.1 --port 8000
```

The worker runs at:

```text
http://localhost:8000
```

## 4. Verify Worker Health

```bash
curl http://localhost:8000/health
```

Expected:

```json
{
  "status": "ok",
  "aiProvider": "ollama",
  "ollamaModel": "gemma3:latest",
  "openaiEnabled": "false",
  "openaiModel": "gpt-4.1-mini"
}
```

## 5. Verify Tutor Hint Endpoint

The internal endpoints require the worker token.

```bash
curl -i -X POST http://localhost:8000/internal/tutor-hint \
  -H 'Content-Type: application/json' \
  -H 'X-AI-Tutor-Worker-Token: dev-worker-token' \
  --data '{"sessionId":"test-session","mode":"hint","content":"What is 2+2?"}'
```

Expected response shape:

```json
{
  "content": "2 + 2 = 4...",
  "confidence": 0.65,
  "provider": "ollama:gemma3:latest"
}
```

## 6. Spring Boot Connection

Spring Boot defaults already point to the local worker:

```text
PYTHON_WORKER_BASE_URL=http://localhost:8000
PYTHON_WORKER_INTERNAL_TOKEN=dev-worker-token
```

So this is enough in another terminal:

```bash
cd /Users/vipul.pandey/projects/ai-tutor/backend
mvn spring-boot:run
```

To override explicitly:

```bash
PYTHON_WORKER_BASE_URL=http://localhost:8000 \
PYTHON_WORKER_INTERNAL_TOKEN=dev-worker-token \
mvn spring-boot:run
```

Spring worker calls use:

- HTTP/1.1, because Uvicorn does not support Spring/JDK cleartext HTTP upgrade attempts.
- Five-minute timeout, because local Ollama can be slow.
- `X-AI-Tutor-Worker-Token` header.
- JSON request/response bodies.

## 7. Internal Endpoints

```text
GET  /health
GET  /
POST /internal/analyze-assignment
POST /internal/analyze-progress
POST /internal/analyze-focus
POST /internal/tutor-hint
```

All `/internal/**` endpoints require:

```text
X-AI-Tutor-Worker-Token: dev-worker-token
```

## 8. Optional OpenAI Setup

Set the key only in the worker/backend environment, never in the mobile app:

```bash
export AI_PROVIDER=openai
export OPENAI_API_KEY="your_key_here"
export OPENAI_MODEL="gpt-4.1-mini"
.venv/bin/uvicorn app.main:app --reload --host 127.0.0.1 --port 8000
```

Check whether the worker sees the key:

```bash
curl http://localhost:8000/health
```

Expected when configured:

```json
{
  "status": "ok",
  "aiProvider": "openai",
  "openaiEnabled": "true",
  "openaiModel": "gpt-4.1-mini"
}
```

## 9. Troubleshooting

### `curl: Failed to connect to localhost port 8000`

Worker is not running. Start it:

```bash
cd /Users/vipul.pandey/projects/ai-tutor/workers/ai_worker
.venv/bin/uvicorn app.main:app --reload --host 127.0.0.1 --port 8000
```

### `401 Invalid worker token`

Spring and Python token values differ. Use:

```text
PYTHON_WORKER_INTERNAL_TOKEN=dev-worker-token
```

### `Unsupported upgrade request`

Usually a wrong client or HTTP upgrade probe hit the worker port. Spring should call the worker through its HTTP/1.1 `RestClient`.

### `Worker client disconnected before body was read`

Usually Spring/backend was stopped, restarted, or using stale code while Python was reading the request. Restart both Spring and the worker after transport changes.

### `Ollama request failed`

Check Ollama:

```bash
curl http://localhost:11434/api/tags
```

If models are empty:

```bash
ollama pull gemma3
```

## 10. Optional Integrations

- OCR: Tesseract, PaddleOCR, Google Cloud Vision, AWS Textract, Azure Document Intelligence.
- AI reasoning: OpenAI Responses API for image analysis.
- CV progress comparison: OpenCV, NumPy, Pillow.

Keep workers stateless. Spring Boot owns product state and database writes.
