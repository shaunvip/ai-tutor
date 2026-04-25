import base64
import json
import mimetypes
from pathlib import Path
from typing import Any

from app.schemas import AssignmentAnalysisCommand, AssignmentAnalysisResult, PlanStepResult
from app.settings import settings


def analyze_assignment_with_openai(command: AssignmentAnalysisCommand) -> AssignmentAnalysisResult | None:
    if not settings.openai_enabled:
        return None

    image_path = Path(command.asset_path)
    if not image_path.exists():
        return None

    try:
        from openai import OpenAI
    except ImportError:
        return None

    prompt = _assignment_prompt(command)
    client = OpenAI(api_key=settings.openai_api_key)
    try:
        response = client.responses.create(
            model=settings.openai_model,
            input=[
                {
                    "role": "user",
                    "content": [
                        {"type": "input_text", "text": prompt},
                        {"type": "input_image", "image_url": _image_data_url(image_path)},
                    ],
                }
            ],
        )
        payload = _parse_json(response.output_text)
        return _to_assignment_result(payload, command)
    except Exception:
        return None


def _assignment_prompt(command: AssignmentAnalysisCommand) -> str:
    return f"""
Analyze this homework/classwork image for a grade {command.grade_level} student age {command.age}.
Subject hint: {command.subject or "unknown"}.

Return only valid JSON with this exact shape:
{{
  "subject": "math|english|hindi|science|general",
  "taskType": "copying|reading|short_answer_writing|long_answer_writing|math_problem_solving|worksheet|diagram_table_work",
  "estimatedWordCount": 120,
  "questionCount": 5,
  "estimatedTotalMinutes": 25,
  "confidence": 0.75,
  "summary": "short summary",
  "steps": [
    {{"stepOrder": 1, "title": "Read instructions", "plannedStartMinute": 0, "plannedEndMinute": 3}}
  ]
}}

Use effort units, not word count alone. Include reading, writing, thinking, solving, and review time.
For English or Hindi writing/copying work, cap writing effort at maximum 1 minute per handwritten line.
Do not include direct homework answers.
""".strip()


def _image_data_url(image_path: Path) -> str:
    mime_type = mimetypes.guess_type(image_path.name)[0] or "image/jpeg"
    encoded = base64.b64encode(image_path.read_bytes()).decode("ascii")
    return f"data:{mime_type};base64,{encoded}"


def _parse_json(text: str) -> dict[str, Any]:
    stripped = text.strip()
    if stripped.startswith("```"):
        stripped = stripped.strip("`")
        stripped = stripped.removeprefix("json").strip()
    return json.loads(stripped)


def _to_assignment_result(payload: dict[str, Any], command: AssignmentAnalysisCommand) -> AssignmentAnalysisResult:
    steps = [
        PlanStepResult(
            step_order=int(step["stepOrder"]),
            title=str(step["title"]),
            planned_start_minute=int(step["plannedStartMinute"]),
            planned_end_minute=int(step["plannedEndMinute"]),
        )
        for step in payload.get("steps", [])
    ]
    if not steps:
        total_minutes = int(payload.get("estimatedTotalMinutes", 15))
        steps = [
            PlanStepResult(
                step_order=1,
                title="Read instructions",
                planned_start_minute=0,
                planned_end_minute=3,
            ),
            PlanStepResult(
                step_order=2,
                title="Complete work",
                planned_start_minute=3,
                planned_end_minute=max(6, total_minutes - 3),
            ),
            PlanStepResult(
                step_order=3,
                title="Review answers",
                planned_start_minute=max(6, total_minutes - 3),
                planned_end_minute=total_minutes,
            ),
        ]

    return AssignmentAnalysisResult(
        subject=str(payload.get("subject") or command.subject or "general"),
        task_type=str(payload.get("taskType") or "worksheet"),
        estimated_word_count=int(payload.get("estimatedWordCount") or 120),
        question_count=int(payload.get("questionCount") or 5),
        estimated_total_minutes=int(payload.get("estimatedTotalMinutes") or 20),
        confidence=float(payload.get("confidence") or 0.65),
        steps=steps,
        summary=str(payload.get("summary") or "OpenAI image analysis completed"),
    )
