from pydantic import BaseModel, ConfigDict, Field


class AssignmentAnalysisCommand(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    assignment_id: str = Field(alias="assignmentId")
    asset_path: str = Field(alias="assetPath")
    subject: str | None = None
    grade_level: int = Field(alias="gradeLevel", ge=1, le=12)
    age: int = Field(ge=3, le=18)


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

    session_id: str = Field(alias="sessionId")
    assignment_id: str = Field(alias="assignmentId")
    progress_asset_path: str = Field(alias="progressAssetPath")
    expected_minute: int = Field(default=0, alias="expectedMinute")


class ProgressAnalysisResult(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    completion_percent: int = Field(alias="completionPercent")
    confidence: float
    behind_minutes: int = Field(alias="behindMinutes")
    summary: str
