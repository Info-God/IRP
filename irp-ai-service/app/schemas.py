from typing import Literal

from pydantic import BaseModel, Field, field_validator


class InvestigationRequest(BaseModel):
    incident_id: str
    project_id: str
    organization_id: str


class Evidence(BaseModel):
    type: Literal["error_event", "log_event", "deployment", "runbook"]
    id: str
    excerpt: str


class RecommendedAction(BaseModel):
    description: str
    risk_level: Literal["LOW", "MEDIUM", "HIGH"]


class InvestigationResult(BaseModel):
    root_cause: str
    confidence: float = Field(ge=0.0, le=1.0)
    evidence: list[Evidence]
    recommended_actions: list[RecommendedAction]

    @field_validator("evidence")
    @classmethod
    def must_have_evidence(cls, value: list[Evidence]) -> list[Evidence]:
        if not value:
            raise ValueError("evidence must not be empty - a confidence score must be grounded")
        return value
