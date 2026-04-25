import json
import logging
from typing import TypeVar

from fastapi import Depends, FastAPI, Header, HTTPException, Request
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from pydantic import BaseModel, ValidationError
from starlette.requests import ClientDisconnect

from app.analysis import analyze_assignment, analyze_focus, analyze_progress, generate_tutor_hint
from app.schemas import (
    AssignmentAnalysisCommand,
    AssignmentAnalysisResult,
    FocusAnalysisCommand,
    FocusAnalysisResult,
    ProgressAnalysisCommand,
    ProgressAnalysisResult,
    TutorHintCommand,
    TutorHintResult,
)
from app.settings import settings

app = FastAPI(title="AI Tutor Worker", version="0.1.0")
logger = logging.getLogger("ai_tutor_worker.validation")
T = TypeVar("T", bound=BaseModel)


def verify_worker_token(x_ai_tutor_worker_token: str | None = Header(default=None)) -> None:
    if x_ai_tutor_worker_token != settings.worker_internal_token:
        raise HTTPException(status_code=401, detail="Invalid worker token")


async def parse_worker_body(request: Request, model_type: type[T]) -> T:
    try:
        raw_body = await request.body()
    except ClientDisconnect as exc:
        logger.warning(
            "Worker client disconnected before body was read path=%s contentLength=%s contentType=%s",
            request.url.path,
            request.headers.get("content-length"),
            request.headers.get("content-type"),
        )
        raise HTTPException(status_code=499, detail="Client disconnected while sending request body") from exc
    body_text = raw_body.decode("utf-8", errors="replace").strip()

    if not body_text:
        logger.warning(
            "Empty worker request body path=%s contentLength=%s contentType=%s",
            request.url.path,
            request.headers.get("content-length"),
            request.headers.get("content-type"),
        )
        raise HTTPException(status_code=400, detail="Request body is required")

    logger.info("worker request body path=%s body=%s", request.url.path, body_text[:2_000])

    try:
        payload = json.loads(body_text)
        if isinstance(payload, str):
            payload = json.loads(payload)
        return model_type.model_validate(payload)
    except json.JSONDecodeError as exc:
        raise HTTPException(status_code=400, detail=f"Invalid JSON body: {exc.msg}") from exc
    except ValidationError as exc:
        raise HTTPException(status_code=422, detail=exc.errors()) from exc


@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError) -> JSONResponse:
    try:
        body = (await request.body()).decode("utf-8", errors="replace")
    except ClientDisconnect:
        body = "<client disconnected before body could be read>"
    logger.warning(
        "Request validation failed path=%s errors=%s body=%s",
        request.url.path,
        exc.errors(),
        body,
    )
    return JSONResponse(status_code=422, content={"detail": exc.errors()})


@app.exception_handler(ClientDisconnect)
async def client_disconnect_handler(request: Request, exc: ClientDisconnect) -> JSONResponse:
    logger.warning(
        "Worker client disconnected path=%s contentLength=%s contentType=%s",
        request.url.path,
        request.headers.get("content-length"),
        request.headers.get("content-type"),
    )
    return JSONResponse(status_code=499, content={"detail": "Client disconnected"})


@app.get("/health")
def health() -> dict[str, str]:
    return {
        "status": "ok",
        "aiProvider": settings.ai_provider,
        "ollamaModel": settings.ollama_model,
        "openaiEnabled": str(settings.openai_enabled).lower(),
        "openaiModel": settings.openai_model,
    }


@app.get("/")
def root() -> dict[str, str]:
    return health()


@app.post(
    "/internal/analyze-assignment",
    response_model=AssignmentAnalysisResult,
    response_model_by_alias=True,
)
async def analyze_assignment_endpoint(
    request: Request,
    _: None = Depends(verify_worker_token),
) -> AssignmentAnalysisResult:
    command = await parse_worker_body(request, AssignmentAnalysisCommand)
    return analyze_assignment(command)


@app.post(
    "/internal/analyze-progress",
    response_model=ProgressAnalysisResult,
    response_model_by_alias=True,
)
async def analyze_progress_endpoint(
    request: Request,
    _: None = Depends(verify_worker_token),
) -> ProgressAnalysisResult:
    command = await parse_worker_body(request, ProgressAnalysisCommand)
    return analyze_progress(command)


@app.post(
    "/internal/analyze-focus",
    response_model=FocusAnalysisResult,
    response_model_by_alias=True,
)
async def analyze_focus_endpoint(
    request: Request,
    _: None = Depends(verify_worker_token),
) -> FocusAnalysisResult:
    command = await parse_worker_body(request, FocusAnalysisCommand)
    return analyze_focus(command)


@app.post(
    "/internal/tutor-hint",
    response_model=TutorHintResult,
    response_model_by_alias=True,
)
async def tutor_hint_endpoint(
    request: Request,
    _: None = Depends(verify_worker_token),
) -> TutorHintResult:
    command = await parse_worker_body(request, TutorHintCommand)
    return generate_tutor_hint(command)
