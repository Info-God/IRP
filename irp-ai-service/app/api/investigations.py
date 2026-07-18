import logging

from fastapi import APIRouter, BackgroundTasks, Header, HTTPException, status

from app.agent.orchestrator import run_investigation
from app.config import settings
from app.core_client import IrpCoreClient
from app.schemas import InvestigationRequest

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/v1/investigations", tags=["investigations"])


@router.post("", status_code=status.HTTP_202_ACCEPTED)
def create_investigation(
    request: InvestigationRequest,
    background_tasks: BackgroundTasks,
    x_internal_token: str = Header(default=""),
) -> dict[str, str]:
    if x_internal_token != settings.internal_token:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="invalid internal token")

    background_tasks.add_task(_investigate_and_report, request.incident_id)
    return {"status": "accepted"}


def _investigate_and_report(incident_id: str) -> None:
    core_client = IrpCoreClient(settings)
    try:
        outcome = run_investigation(settings, core_client, incident_id)
        if outcome.result is not None:
            core_client.post_suggestion(incident_id, {
                "rootCause": outcome.result.root_cause,
                "confidence": outcome.result.confidence,
                "evidence": [e.model_dump() for e in outcome.result.evidence],
                "recommendedActions": [a.model_dump() for a in outcome.result.recommended_actions],
            })
            logger.info("Posted suggestion for incident %s (confidence=%.2f)",
                        incident_id, outcome.result.confidence)
        else:
            logger.warning("Investigation of incident %s produced no result: %s", incident_id, outcome.error)
    except Exception:
        logger.exception("Investigation of incident %s failed unexpectedly", incident_id)
    finally:
        core_client.close()
