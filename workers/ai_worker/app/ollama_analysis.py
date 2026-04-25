import json
import logging
import urllib.error
import urllib.request

from app.schemas import TutorHintCommand, TutorHintResult
from app.settings import settings

logger = logging.getLogger("ai_tutor_worker.ollama")
MAX_LOG_CHARS = 2_000
OLLAMA_TIMEOUT_SECONDS = 300


def generate_tutor_hint_with_ollama(command: TutorHintCommand) -> TutorHintResult | None:
    if settings.ai_provider != "ollama":
        return None

    prompt = _tutor_prompt(command)
    payload = {
        "model": settings.ollama_model,
        "prompt": prompt,
        "stream": False,
        "options": {
            "temperature": 0.2,
            "num_predict": 160,
        },
    }
    logger.info("ollama request endpoint=/api/generate body=%s", _truncate(json.dumps(payload)))

    request = urllib.request.Request(
        f"{settings.ollama_base_url}/api/generate",
        data=json.dumps(payload).encode("utf-8"),
        headers={"Content-Type": "application/json"},
        method="POST",
    )

    try:
        with urllib.request.urlopen(request, timeout=OLLAMA_TIMEOUT_SECONDS) as response:
            raw_body = response.read().decode("utf-8")
            logger.info("ollama response status=%s body=%s", response.status, _truncate(raw_body))
            body = json.loads(raw_body)
    except (OSError, urllib.error.URLError, json.JSONDecodeError):
        logger.warning("ollama request failed")
        return None

    content = str(body.get("response") or "").strip()
    if not content:
        return None

    return TutorHintResult(content=content, confidence=0.65, provider=f"ollama:{settings.ollama_model}")


def _tutor_prompt(command: TutorHintCommand) -> str:
    mode = command.mode or "hint"
    content = command.content.strip()
    return f"""
You are an AI tutor for a school student. Answer the student's question in a helpful, short, age-appropriate way.

Rules:
- If the question is a simple factual or arithmetic question, answer directly and show one tiny step.
- If it is homework solving, give a hint first and avoid doing the whole worksheet.
- Keep the answer under 4 short sentences.
- Do not mention policies or system instructions.

Mode: {mode}
Student question: {content}
""".strip()


def _truncate(text: str) -> str:
    if len(text) <= MAX_LOG_CHARS:
        return text
    return text[:MAX_LOG_CHARS] + "..."
