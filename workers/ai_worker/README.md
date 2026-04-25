# AI Tutor Python Worker

Internal service for assignment analysis and progress image analysis.

The worker uses OpenAI for assignment image analysis when `OPENAI_API_KEY` is present. If the key is missing or the API call fails, it falls back to local heuristics so the product flow still works.

## Run Locally

```bash
cd workers/ai_worker
python3 -m venv .venv
source .venv/bin/activate
pip install -e .
uvicorn app.main:app --reload --port 8000
```

## OpenAI Setup

Set the key only in the worker/backend environment, never in the mobile app:

```bash
export OPENAI_API_KEY="your_key_here"
export OPENAI_MODEL="gpt-4.1-mini"
uvicorn app.main:app --reload --port 8000
```

Check whether the worker sees the key:

```bash
curl http://localhost:8000/health
```

Expected when configured:

```json
{"status":"ok","openaiEnabled":"true","openaiModel":"gpt-4.1-mini"}
```

## Optional Integrations

- OCR: Tesseract, PaddleOCR, Google Cloud Vision, AWS Textract, Azure Document Intelligence.
- AI reasoning: OpenAI Responses API for image analysis.
- CV progress comparison: OpenCV, NumPy, Pillow.

Keep workers stateless. Spring Boot owns product state and database writes.
