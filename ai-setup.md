# AI Setup

## Recommended MVP Setup

Use Ollama with one local vision-language model first.

```text
Mobile app
 -> Spring Boot API
 -> Python worker
 -> Ollama on localhost:11434
```

This avoids an OpenAI API key for MVP and keeps infra small.

## Install Ollama

```bash
brew install ollama
ollama pull gemma3
```

Confirm Ollama is running:

```bash
curl http://localhost:11434/api/tags
```

## Model Choice

Use `gemma3` first because it can handle:

- Homework/classwork image analysis.
- Focus image classification.
- Tutor hint responses.
- JSON-style responses when prompted carefully.

This is simpler than running separate OCR, face landmark, and tutor models.

## Python Worker Environment

The worker defaults are already local Ollama:

```text
AI_PROVIDER=ollama
OLLAMA_BASE_URL=http://localhost:11434
OLLAMA_MODEL=gemma3:latest
PYTHON_WORKER_INTERNAL_TOKEN=dev-worker-token
```

Run the worker:

```bash
cd workers/ai_worker
.venv/bin/uvicorn app.main:app --reload --port 8000
```

OpenAI remains optional later:

```bash
AI_PROVIDER=openai
OPENAI_API_KEY=...
OPENAI_MODEL=gpt-4.1-mini
```

## Current Tradeoff

Ollama + `gemma3` is the lowest-infra local MVP path, but it may be slower and less accurate than specialized tools.

Later, if needed:

- MediaPipe Face Landmarker for faster looking-away/focus detection.
- Tesseract or PaddleOCR for stronger OCR.
- OpenAI or another hosted model for higher-quality reasoning.

For now, keep one local model and validate the product flow first.
