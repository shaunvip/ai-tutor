from fastapi import FastAPI

from app.analysis import analyze_assignment, analyze_progress
from app.schemas import (
    AssignmentAnalysisCommand,
    AssignmentAnalysisResult,
    ProgressAnalysisCommand,
    ProgressAnalysisResult,
)
from app.settings import settings

app = FastAPI(title="AI Tutor Worker", version="0.1.0")


@app.get("/health")
def health() -> dict[str, str]:
    return {
        "status": "ok",
        "openaiEnabled": str(settings.openai_enabled).lower(),
        "openaiModel": settings.openai_model,
    }


@app.post(
    "/internal/analyze-assignment",
    response_model=AssignmentAnalysisResult,
    response_model_by_alias=True,
)
def analyze_assignment_endpoint(command: AssignmentAnalysisCommand) -> AssignmentAnalysisResult:
    return analyze_assignment(command)


@app.post(
    "/internal/analyze-progress",
    response_model=ProgressAnalysisResult,
    response_model_by_alias=True,
)
def analyze_progress_endpoint(command: ProgressAnalysisCommand) -> ProgressAnalysisResult:
    return analyze_progress(command)
