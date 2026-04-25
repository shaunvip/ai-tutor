from pydantic import BaseModel, ConfigDict, Field, field_validator


def _bounded_int(value: object, default: int, minimum: int, maximum: int) -> int:
    try:
        parsed = int(value) if value not in (None, "") else default
    except (TypeError, ValueError):
        parsed = default
    return max(minimum, min(maximum, parsed))


def _string_or_empty(value: object) -> str:
    return "" if value is None else str(value)


class AssignmentAnalysisCommand(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    assignment_id: str = Field(default="", alias="assignmentId")
    asset_path: str = Field(default="", alias="assetPath")
    subject: str | None = None
    grade_level: int = Field(default=3, alias="gradeLevel")
    age: int = 8

    @field_validator("assignment_id", "asset_path", mode="before")
    @classmethod
    def normalize_required_strings(cls, value: object) -> str:
        return _string_or_empty(value)

    @field_validator("grade_level", mode="before")
    @classmethod
    def normalize_grade_level(cls, value: object) -> int:
        return _bounded_int(value, default=3, minimum=1, maximum=12)

    @field_validator("age", mode="before")
    @classmethod
    def normalize_age(cls, value: object) -> int:
        return _bounded_int(value, default=8, minimum=3, maximum=18)


class PlanStepResult(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    step_order: int = Field(alias="stepOrder")
    title: str
    planned_start_minute: int = Field(alias="plannedStartMinute")
    planned_end_minute: int = Field(alias="plannedEndMinute")


class AssignmentAnalysisResult(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    subject: str
    task_type: str = Field(alias="taskType")
    estimated_word_count: int = Field(alias="estimatedWordCount")
    question_count: int = Field(alias="questionCount")
    estimated_total_minutes: int = Field(alias="estimatedTotalMinutes")
    confidence: float
    steps: list[PlanStepResult]
    summary: str


class ProgressAnalysisCommand(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    session_id: str = Field(default="", alias="sessionId")
    assignment_id: str = Field(default="", alias="assignmentId")
    progress_asset_path: str = Field(default="", alias="progressAssetPath")
    expected_minute: int = Field(default=0, alias="expectedMinute")

    @field_validator("session_id", "assignment_id", "progress_asset_path", mode="before")
    @classmethod
    def normalize_required_strings(cls, value: object) -> str:
        return _string_or_empty(value)

    @field_validator("expected_minute", mode="before")
    @classmethod
    def normalize_expected_minute(cls, value: object) -> int:
        return _bounded_int(value, default=0, minimum=0, maximum=600)


class ProgressAnalysisResult(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    completion_percent: int = Field(alias="completionPercent")
    confidence: float
    behind_minutes: int = Field(alias="behindMinutes")
    summary: str


class FocusAnalysisCommand(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    session_id: str = Field(default="", alias="sessionId")
    focus_asset_path: str = Field(default="", alias="focusAssetPath")

    @field_validator("session_id", "focus_asset_path", mode="before")
    @classmethod
    def normalize_required_strings(cls, value: object) -> str:
        return _string_or_empty(value)


class FocusAnalysisResult(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    looking_away: bool = Field(alias="lookingAway")
    alert: bool
    confidence: float
    reason: str
    alert_message: str = Field(alias="alertMessage")


class TutorHintCommand(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    session_id: str | None = Field(default=None, alias="sessionId")
    mode: str | None = None
    content: str = ""

    @field_validator("session_id", "mode", "content", mode="before")
    @classmethod
    def normalize_strings(cls, value: object) -> str | None:
        if value is None:
            return None
        return str(value)


class TutorHintResult(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    content: str
    confidence: float
    provider: str
