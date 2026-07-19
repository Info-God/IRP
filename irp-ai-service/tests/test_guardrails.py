from app.guardrails import cap_confidence_if_ungrounded, redact_metadata, redact_text


def test_redact_metadata_masks_sensitive_keys():
    metadata = {"apiKey": "sk-12345", "user": "ada@acme.dev", "password": "hunter2"}

    result = redact_metadata(metadata)

    assert result["apiKey"] == "***REDACTED***"
    assert result["password"] == "***REDACTED***"
    assert result["user"] == "ada@acme.dev"


def test_redact_metadata_handles_none_and_empty():
    assert redact_metadata(None) is None
    assert redact_metadata({}) == {}


def test_redact_text_masks_inline_secrets():
    text = "Failed request with api_key=sk-abcdef123 and token: xyz789"

    result = redact_text(text)

    assert "sk-abcdef123" not in result
    assert "xyz789" not in result


def test_redact_text_leaves_normal_text_untouched():
    text = "Cannot invoke getDiscount() because coupon is null"

    assert redact_text(text) == text


def test_cap_confidence_if_ungrounded_caps_when_no_evidence():
    assert cap_confidence_if_ungrounded(0.95, 0) == 0.3


def test_cap_confidence_if_ungrounded_leaves_grounded_confidence_alone():
    assert cap_confidence_if_ungrounded(0.86, 2) == 0.86
