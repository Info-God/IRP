import re
from typing import Any

_SENSITIVE_KEY_PATTERN = re.compile(r"(password|secret|token|api[_-]?key|authorization)", re.IGNORECASE)
_SENSITIVE_INLINE_PATTERN = re.compile(r"(?i)(api[_-]?key|token|password)\s*[:=]\s*\S+")
_REDACTED = "***REDACTED***"


def redact_metadata(metadata: dict[str, Any] | None) -> dict[str, Any] | None:
    """Mirrors the Java SDK's SensitiveDataMasker: redact values whose key looks like
    a credential before any log/error metadata is sent to the LLM."""
    if not metadata:
        return metadata
    return {
        key: (_REDACTED if _SENSITIVE_KEY_PATTERN.search(key) else value)
        for key, value in metadata.items()
    }


def redact_text(text: str) -> str:
    """Catches secrets embedded in free-text log/error messages, not just metadata keys."""
    return _SENSITIVE_INLINE_PATTERN.sub(r"\1=***REDACTED***", text)


def cap_confidence_if_ungrounded(confidence: float, evidence_count: int) -> float:
    """Grounded-confidence guardrail: a claim with no supporting evidence can't be
    reported as confident, even if the model says so."""
    if evidence_count == 0:
        return min(confidence, 0.3)
    return confidence
