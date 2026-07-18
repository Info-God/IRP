# irp-ai-service — Phase 3 AI agent + RAG service

Investigates incidents created in irp-core: pulls recent errors, logs, and deployments for the
incident's service, (eventually) searches uploaded runbooks, and posts a structured root-cause
suggestion back to irp-core for human approval. Never executes actions - see irp-core's
`AgentSuggestionController` for the human-approval gate.

## Run it

```bash
pip install -r requirements.txt
cp .env.example .env   # fill in IRP_CORE_API_KEY (an agent-scoped API key from irp-core) and GROQ_API_KEY
uvicorn app.main:app --reload --port 8000
```

irp-core calls `POST /v1/investigations` (see `app/api/investigations.py`) with the internal
token configured above; this service then calls back into irp-core's `/api/v1/agent/**` endpoints
(the same auth mechanism the Java SDK uses - a project-scoped `X-API-Key`) to read logs/errors/
deployments and post the resulting suggestion.

## What's here vs. what's next

- Tool-calling investigation loop (`app/agent/orchestrator.py`) using Groq's free-tier,
  OpenAI-compatible chat completions API (`llama-3.3-70b-versatile` by default), with
  `search_errors`, `search_logs`, `get_recent_deployments`, `search_runbooks`, and
  `post_investigation_result` as tools.
- Guardrails (`app/guardrails.py`): metadata/message redaction before anything reaches the LLM,
  and confidence capping when the model's answer isn't grounded in retrieved evidence.
- `app/rag/retriever.py` is a stub - always returns no runbook matches. irp-core's schema
  (`runbooks`/`runbook_chunks`, pgvector-backed) is ready, but there's no upload endpoint or
  embedding pipeline yet. Wiring that up is the next piece of Phase 3.
- No direct database access - every read/write goes through irp-core's authenticated REST API,
  preserving the tenancy and audit invariants already enforced there.
- irp-core does not yet call this service automatically when an incident is created (that
  after-commit event listener + HTTP call is still to be wired up on the Java side).
