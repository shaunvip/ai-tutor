import math
from pathlib import Path

from PIL import Image

from app.openai_analysis import analyze_assignment_with_openai
from app.schemas import (
    AssignmentAnalysisCommand,
    AssignmentAnalysisResult,
    PlanStepResult,
    ProgressAnalysisCommand,
    ProgressAnalysisResult,
)


def analyze_assignment(command: AssignmentAnalysisCommand) -> AssignmentAnalysisResult:
    openai_result = analyze_assignment_with_openai(command)
    if openai_result is not None:
        return openai_result

    image_stats = _image_stats(command.asset_path)
    subject = command.subject or "general"
    task_type = _task_type_from_subject(subject)
    question_count = _estimate_questions(command.grade_level, image_stats["area"])
    estimated_words = _estimate_words(question_count, command.grade_level, task_type)
    total_minutes = _estimate_minutes(
        estimated_words=estimated_words,
        question_count=question_count,
        grade_level=command.grade_level,
        task_type=task_type,
        subject=subject,
    )

    return AssignmentAnalysisResult(
        subject=subject,
        task_type=task_type,
        estimated_word_count=estimated_words,
        question_count=question_count,
        estimated_total_minutes=total_minutes,
        confidence=0.45 if image_stats["readable"] else 0.25,
        steps=_build_plan_steps(total_minutes, question_count),
        summary=(
            "Local heuristic analysis. Add OCR/OpenAI later for stronger task extraction "
            "and question-level planning."
        ),
    )


def analyze_progress(command: ProgressAnalysisCommand) -> ProgressAnalysisResult:
    image_stats = _image_stats(command.progress_asset_path)
    completion_percent = 35 if image_stats["readable"] else 0
    expected_percent = min(100, max(0, command.expected_minute * 5))
    behind_minutes = max(0, (expected_percent - completion_percent) // 5)

    return ProgressAnalysisResult(
        completion_percent=completion_percent,
        confidence=0.35 if image_stats["readable"] else 0.15,
        behind_minutes=behind_minutes,
        summary=(
            "Local heuristic progress estimate. Replace with OCR/OpenCV comparison "
            "against the assignment image as the next worker iteration."
        ),
    )


def _image_stats(path: str) -> dict[str, int | bool]:
    image_path = Path(path)
    if not image_path.exists():
        return {"readable": False, "width": 0, "height": 0, "area": 0}

    try:
        with Image.open(image_path) as image:
            width, height = image.size
            return {"readable": True, "width": width, "height": height, "area": width * height}
    except Exception:
        return {"readable": False, "width": 0, "height": 0, "area": 0}


def _task_type_from_subject(subject: str) -> str:
    normalized = subject.lower()
    if "math" in normalized or "maths" in normalized:
        return "math_problem_solving"
    if _is_language_subject(normalized):
        return "short_answer_writing"
    if "science" in normalized:
        return "worksheet"
    return "worksheet"


def _estimate_questions(grade_level: int, image_area: int) -> int:
    area_hint = 2 if image_area > 1_500_000 else 0
    return max(3, min(10, 2 + grade_level // 2 + area_hint))


def _estimate_words(question_count: int, grade_level: int, task_type: str) -> int:
    if task_type == "math_problem_solving":
        return question_count * max(8, grade_level * 2)
    return question_count * max(18, grade_level * 6)


def _estimate_minutes(
    estimated_words: int,
    question_count: int,
    grade_level: int,
    task_type: str,
    subject: str,
) -> int:
    writing_minutes = _estimate_writing_minutes(
        estimated_words=estimated_words,
        grade_level=grade_level,
        task_type=task_type,
        subject=subject,
    )
    thinking_minutes = question_count * (2.2 if task_type == "math_problem_solving" else 1.2)
    review_minutes = max(3, question_count // 2)
    return max(10, round(writing_minutes + thinking_minutes + review_minutes))


def _estimate_writing_minutes(
    estimated_words: int,
    grade_level: int,
    task_type: str,
    subject: str,
) -> float:
    if _is_language_subject(subject) and task_type in {
        "copying",
        "short_answer_writing",
        "long_answer_writing",
        "worksheet",
    }:
        estimated_lines = _estimate_handwritten_lines(estimated_words, grade_level)
        return estimated_lines * 1.0

    writing_words_per_minute = max(5, min(16, 5 + grade_level))
    return estimated_words / writing_words_per_minute


def _estimate_handwritten_lines(estimated_words: int, grade_level: int) -> int:
    words_per_line = max(5, min(9, 5 + grade_level // 2))
    return max(1, math.ceil(estimated_words / words_per_line))


def _is_language_subject(subject: str) -> bool:
    normalized = subject.lower()
    return any(keyword in normalized for keyword in ("english", "hindi", "language"))


def _build_plan_steps(total_minutes: int, question_count: int) -> list[PlanStepResult]:
    read_end = min(3, max(2, total_minutes // 8))
    work_end = max(read_end + 5, total_minutes - 3)
    midpoint = read_end + max(3, (work_end - read_end) // 2)

    return [
        PlanStepResult(
            step_order=1,
            title="Read instructions",
            planned_start_minute=0,
            planned_end_minute=read_end,
        ),
        PlanStepResult(
            step_order=2,
            title=f"Finish first {max(1, question_count // 2)} questions",
            planned_start_minute=read_end,
            planned_end_minute=midpoint,
        ),
        PlanStepResult(
            step_order=3,
            title="Finish remaining work",
            planned_start_minute=midpoint,
            planned_end_minute=work_end,
        ),
        PlanStepResult(
            step_order=4,
            title="Review answers",
            planned_start_minute=work_end,
            planned_end_minute=total_minutes,
        ),
    ]
