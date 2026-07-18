import type {
  AgentSuggestionResponse,
  ApiKeyResponse,
  AuditLogResponse,
  AutomationRuleView,
  DeploymentEventView,
  ErrorEventView,
  IncidentResponse,
  IncidentTimelineEntryResponse,
  IntegrationView,
  LogEventView,
  OrganizationResponse,
  ProjectResponse,
  RunbookView,
  UserSummary,
} from "@/types";
import { daysAgo, hoursAgo, minutesAgo } from "@/mocks/time";

export const mockUser: UserSummary = {
  id: "user-1",
  email: "ada@acme.dev",
  fullName: "Ada Lovelace",
  role: "OWNER",
  organizationId: "org-1",
};

export const mockOrganization: OrganizationResponse = {
  id: "org-1",
  name: "Acme Corp",
  slug: "acme-corp",
  createdAt: daysAgo(180),
};

export const mockProjects: ProjectResponse[] = [
  {
    id: "proj-checkout",
    organizationId: "org-1",
    name: "checkout-service",
    environment: "production",
    createdAt: daysAgo(120),
  },
  {
    id: "proj-payments",
    organizationId: "org-1",
    name: "payments-api",
    environment: "production",
    createdAt: daysAgo(95),
  },
  {
    id: "proj-notifications",
    organizationId: "org-1",
    name: "notifications-worker",
    environment: "staging",
    createdAt: daysAgo(40),
  },
];

export const mockIncidents: IncidentResponse[] = [
  {
    id: "inc-1",
    projectId: "proj-checkout",
    title: "Checkout throwing NullPointerException after deploy v1.4.0",
    description:
      "Customers report checkout failing intermittently since the 1.4.0 release went out. Error rate on /checkout is up 12x.",
    severity: "CRITICAL",
    status: "AWAITING_APPROVAL",
    service: "checkout",
    openedAt: minutesAgo(25),
    resolvedAt: null,
    createdAt: minutesAgo(25),
    updatedAt: minutesAgo(2),
  },
  {
    id: "inc-2",
    projectId: "proj-checkout",
    title: "Redis connection pool exhausted",
    description: "Checkout service is logging connection pool timeouts against the session cache.",
    severity: "HIGH",
    status: "AWAITING_APPROVAL",
    service: "checkout",
    openedAt: minutesAgo(40),
    resolvedAt: null,
    createdAt: minutesAgo(40),
    updatedAt: minutesAgo(5),
  },
  {
    id: "inc-3",
    projectId: "proj-checkout",
    title: "Payment gateway timeout spike",
    description: "Elevated timeout rate calling the upstream payment gateway from checkout.",
    severity: "HIGH",
    status: "INVESTIGATING",
    service: "payments-gateway",
    openedAt: hoursAgo(2),
    resolvedAt: null,
    createdAt: hoursAgo(2),
    updatedAt: hoursAgo(1),
  },
  {
    id: "inc-4",
    projectId: "proj-checkout",
    title: "Elevated 5xx rate on /cart endpoint",
    description: "5xx rate on /cart climbed from 0.1% to 2.4% over the last hour.",
    severity: "MEDIUM",
    status: "OPEN",
    service: "checkout",
    openedAt: hoursAgo(3),
    resolvedAt: null,
    createdAt: hoursAgo(3),
    updatedAt: hoursAgo(3),
  },
  {
    id: "inc-5",
    projectId: "proj-checkout",
    title: "Slow query warning on orders table",
    description: "p99 query latency on orders table exceeds 800ms during peak traffic.",
    severity: "LOW",
    status: "OPEN",
    service: "checkout",
    openedAt: hoursAgo(5),
    resolvedAt: null,
    createdAt: hoursAgo(5),
    updatedAt: hoursAgo(5),
  },
  {
    id: "inc-6",
    projectId: "proj-checkout",
    title: "Session cookie expiry misconfigured",
    description: "Users were logged out mid-checkout due to an incorrect cookie max-age.",
    severity: "MEDIUM",
    status: "RESOLVED",
    service: "checkout",
    openedAt: daysAgo(1),
    resolvedAt: hoursAgo(20),
    createdAt: daysAgo(1),
    updatedAt: hoursAgo(20),
  },
  {
    id: "inc-7",
    projectId: "proj-checkout",
    title: "Stripe webhook signature mismatch",
    description: "Webhook signature validation started failing for a subset of Stripe events.",
    severity: "HIGH",
    status: "RESOLVED",
    service: "payments-gateway",
    openedAt: daysAgo(2),
    resolvedAt: daysAgo(1),
    createdAt: daysAgo(2),
    updatedAt: daysAgo(1),
  },
  {
    id: "inc-8",
    projectId: "proj-checkout",
    title: "Full outage - database connection pool leak",
    description: "Checkout was fully unavailable for 14 minutes due to a connection pool leak.",
    severity: "CRITICAL",
    status: "CLOSED",
    service: "checkout",
    openedAt: daysAgo(5),
    resolvedAt: daysAgo(4),
    createdAt: daysAgo(5),
    updatedAt: daysAgo(4),
  },
  {
    id: "inc-9",
    projectId: "proj-checkout",
    title: "Minor UI glitch on order confirmation page",
    description: "Order total briefly renders as NaN before hydration completes.",
    severity: "LOW",
    status: "CLOSED",
    service: "checkout-web",
    openedAt: daysAgo(6),
    resolvedAt: daysAgo(6),
    createdAt: daysAgo(6),
    updatedAt: daysAgo(6),
  },
  {
    id: "inc-10",
    projectId: "proj-checkout",
    title: "Increased latency on search endpoint",
    description: "p95 latency on /search doubled after the new relevance model shipped.",
    severity: "MEDIUM",
    status: "OPEN",
    service: "search",
    openedAt: hoursAgo(8),
    resolvedAt: null,
    createdAt: hoursAgo(8),
    updatedAt: hoursAgo(8),
  },
  {
    id: "inc-11",
    projectId: "proj-payments",
    title: "Refund API returning 500 for partial refunds",
    description: "Partial refund requests fail; full refunds are unaffected.",
    severity: "HIGH",
    status: "OPEN",
    service: "refunds-api",
    openedAt: hoursAgo(4),
    resolvedAt: null,
    createdAt: hoursAgo(4),
    updatedAt: hoursAgo(4),
  },
  {
    id: "inc-12",
    projectId: "proj-payments",
    title: "Ledger reconciliation drift detected",
    description: "Nightly reconciliation job flagged a $128.40 drift between ledger and processor.",
    severity: "MEDIUM",
    status: "INVESTIGATING",
    service: "ledger-service",
    openedAt: hoursAgo(10),
    resolvedAt: null,
    createdAt: hoursAgo(10),
    updatedAt: hoursAgo(6),
  },
  {
    id: "inc-13",
    projectId: "proj-payments",
    title: "Currency conversion rate cache stale",
    description: "FX rates cache did not refresh for 6 hours, causing minor overcharges.",
    severity: "LOW",
    status: "RESOLVED",
    service: "fx-service",
    openedAt: daysAgo(3),
    resolvedAt: daysAgo(3),
    createdAt: daysAgo(3),
    updatedAt: daysAgo(3),
  },
  {
    id: "inc-14",
    projectId: "proj-notifications",
    title: "Memory leak suspected in notification worker",
    description: "Worker RSS grows unbounded over ~6 hours before OOMKilled by the scheduler.",
    severity: "HIGH",
    status: "INVESTIGATING",
    service: "notifications-worker",
    openedAt: hoursAgo(1),
    resolvedAt: null,
    createdAt: hoursAgo(1),
    updatedAt: hoursAgo(1),
  },
  {
    id: "inc-15",
    projectId: "proj-notifications",
    title: "SMS delivery provider degraded",
    description: "Upstream SMS provider reporting delayed delivery for APAC region.",
    severity: "MEDIUM",
    status: "OPEN",
    service: "sms-gateway",
    openedAt: hoursAgo(6),
    resolvedAt: null,
    createdAt: hoursAgo(6),
    updatedAt: hoursAgo(6),
  },
];

const timelineByIncident: Record<string, IncidentTimelineEntryResponse[]> = {
  "inc-1": [
    { id: "tl-1-1", actor: "ada@acme.dev", entryType: "CREATED", message: "Incident opened: Checkout throwing NullPointerException after deploy v1.4.0", createdAt: minutesAgo(25) },
    { id: "tl-1-2", actor: "agent", entryType: "AGENT_ACTION", message: "Proposed root cause (confidence 86%): The CheckoutService.charge() method throws NullPointerException because customer is null - introduced by deployment v1.4.0", createdAt: minutesAgo(2) },
  ],
  "inc-2": [
    { id: "tl-2-1", actor: "system", entryType: "CREATED", message: "Incident opened: Redis connection pool exhausted", createdAt: minutesAgo(40) },
    { id: "tl-2-2", actor: "agent", entryType: "AGENT_ACTION", message: "Proposed root cause (confidence 42%): Redis connection pool exhausted, likely due to unclosed connections in the new caching layer", createdAt: minutesAgo(5) },
  ],
  "inc-3": [
    { id: "tl-3-1", actor: "system", entryType: "CREATED", message: "Incident opened: Payment gateway timeout spike", createdAt: hoursAgo(2) },
    { id: "tl-3-2", actor: "ada@acme.dev", entryType: "STATUS_CHANGE", message: "Status changed to INVESTIGATING: paging payments on-call", createdAt: hoursAgo(1) },
  ],
  "inc-4": [
    { id: "tl-4-1", actor: "system", entryType: "CREATED", message: "Incident opened: Elevated 5xx rate on /cart endpoint", createdAt: hoursAgo(3) },
  ],
  "inc-5": [
    { id: "tl-5-1", actor: "system", entryType: "CREATED", message: "Incident opened: Slow query warning on orders table", createdAt: hoursAgo(5) },
  ],
  "inc-6": [
    { id: "tl-6-1", actor: "ada@acme.dev", entryType: "CREATED", message: "Incident opened: Session cookie expiry misconfigured", createdAt: daysAgo(1) },
    { id: "tl-6-2", actor: "ada@acme.dev", entryType: "NOTE", message: "Identified as a config regression from the auth-service 2.1 release", createdAt: hoursAgo(22) },
    { id: "tl-6-3", actor: "ada@acme.dev", entryType: "STATUS_CHANGE", message: "Status changed to RESOLVED: cookie max-age reverted", createdAt: hoursAgo(20) },
  ],
  "inc-7": [
    { id: "tl-7-1", actor: "system", entryType: "CREATED", message: "Incident opened: Stripe webhook signature mismatch", createdAt: daysAgo(2) },
    { id: "tl-7-2", actor: "agent", entryType: "AGENT_ACTION", message: "Proposed root cause (confidence 91%): webhook signing secret was rotated in Stripe but not updated in the payments-gateway config", createdAt: daysAgo(2) },
    { id: "tl-7-3", actor: "ada@acme.dev", entryType: "APPROVAL", message: "Approved the agent's suggestion: rotated secret confirmed in Stripe dashboard audit log", createdAt: hoursAgo(30) },
    { id: "tl-7-4", actor: "ada@acme.dev", entryType: "STATUS_CHANGE", message: "Status changed to RESOLVED", createdAt: daysAgo(1) },
  ],
  "inc-8": [
    { id: "tl-8-1", actor: "system", entryType: "CREATED", message: "Incident opened: Full outage - database connection pool leak", createdAt: daysAgo(5) },
    { id: "tl-8-2", actor: "agent", entryType: "AGENT_ACTION", message: "Proposed root cause (confidence 55%): connection pool leak in the new order-export batch job", createdAt: daysAgo(5) },
    { id: "tl-8-3", actor: "ada@acme.dev", entryType: "APPROVAL", message: "Rejected the agent's suggestion: root cause was actually a missing connection timeout on the reporting replica, not the batch job", createdAt: daysAgo(4) },
    { id: "tl-8-4", actor: "ada@acme.dev", entryType: "STATUS_CHANGE", message: "Status changed to CLOSED", createdAt: daysAgo(4) },
  ],
  "inc-9": [
    { id: "tl-9-1", actor: "system", entryType: "CREATED", message: "Incident opened: Minor UI glitch on order confirmation page", createdAt: daysAgo(6) },
    { id: "tl-9-2", actor: "ada@acme.dev", entryType: "STATUS_CHANGE", message: "Status changed to CLOSED: fixed in checkout-web v2.9.1", createdAt: daysAgo(6) },
  ],
  "inc-10": [
    { id: "tl-10-1", actor: "system", entryType: "CREATED", message: "Incident opened: Increased latency on search endpoint", createdAt: hoursAgo(8) },
  ],
  "inc-11": [
    { id: "tl-11-1", actor: "system", entryType: "CREATED", message: "Incident opened: Refund API returning 500 for partial refunds", createdAt: hoursAgo(4) },
  ],
  "inc-12": [
    { id: "tl-12-1", actor: "system", entryType: "CREATED", message: "Incident opened: Ledger reconciliation drift detected", createdAt: hoursAgo(10) },
    { id: "tl-12-2", actor: "ada@acme.dev", entryType: "STATUS_CHANGE", message: "Status changed to INVESTIGATING: finance team looped in", createdAt: hoursAgo(6) },
  ],
  "inc-13": [
    { id: "tl-13-1", actor: "system", entryType: "CREATED", message: "Incident opened: Currency conversion rate cache stale", createdAt: daysAgo(3) },
    { id: "tl-13-2", actor: "ada@acme.dev", entryType: "STATUS_CHANGE", message: "Status changed to RESOLVED: cache TTL fixed", createdAt: daysAgo(3) },
  ],
  "inc-14": [
    { id: "tl-14-1", actor: "system", entryType: "CREATED", message: "Incident opened: Memory leak suspected in notification worker", createdAt: hoursAgo(1) },
  ],
  "inc-15": [
    { id: "tl-15-1", actor: "system", entryType: "CREATED", message: "Incident opened: SMS delivery provider degraded", createdAt: hoursAgo(6) },
  ],
};

export function getMockTimeline(incidentId: string): IncidentTimelineEntryResponse[] {
  return timelineByIncident[incidentId] ?? [];
}

const suggestionsByIncident: Record<string, AgentSuggestionResponse[]> = {
  "inc-1": [
    {
      id: "sug-1",
      incidentId: "inc-1",
      projectId: "proj-checkout",
      rootCause:
        "The CheckoutService.charge() method throws a NullPointerException because the customer object is null. This started immediately after deployment v1.4.0, which changed how the customer context is hydrated before charge() is called.",
      confidence: 0.86,
      evidence: [
        { type: "error_event", id: "err-101", excerpt: "Cannot invoke CheckoutService.charge() because customer is null" },
        { type: "deployment", id: "dep-201", excerpt: "Deployment of checkout v1.4.0 completed 15 minutes before the first error" },
      ],
      recommendedActions: [
        { description: "Roll back checkout to v1.3.2", risk_level: "MEDIUM" },
        { description: "Add a null-check guard before charge() as a hotfix", risk_level: "LOW" },
      ],
      status: "PENDING_REVIEW",
      reviewedBy: null,
      reviewedAt: null,
      createdAt: minutesAgo(2),
    },
  ],
  "inc-2": [
    {
      id: "sug-2",
      incidentId: "inc-2",
      projectId: "proj-checkout",
      rootCause:
        "Redis connection pool exhausted, most likely due to unclosed connections introduced by the new caching layer added last week.",
      confidence: 0.42,
      evidence: [
        { type: "log_event", id: "log-301", excerpt: "WARN: redis pool timeout after 5000ms - 0 connections available" },
      ],
      recommendedActions: [
        { description: "Temporarily increase Redis pool size", risk_level: "LOW" },
        { description: "Audit the new caching layer for unclosed connections", risk_level: "MEDIUM" },
      ],
      status: "PENDING_REVIEW",
      reviewedBy: null,
      reviewedAt: null,
      createdAt: minutesAgo(5),
    },
  ],
  "inc-7": [
    {
      id: "sug-7",
      incidentId: "inc-7",
      projectId: "proj-checkout",
      rootCause:
        "The webhook signing secret was rotated in the Stripe dashboard but the corresponding config value in payments-gateway was never updated, so signature verification fails for every incoming event.",
      confidence: 0.91,
      evidence: [
        { type: "error_event", id: "err-401", excerpt: "Webhook signature verification failed: no matching signature found" },
        { type: "log_event", id: "log-402", excerpt: "INFO: Stripe secret rotation event received (ignored - no handler)" },
      ],
      recommendedActions: [
        { description: "Update STRIPE_WEBHOOK_SECRET in payments-gateway config", risk_level: "LOW" },
      ],
      status: "APPROVED",
      reviewedBy: "ada@acme.dev",
      reviewedAt: hoursAgo(30),
      createdAt: daysAgo(2),
    },
  ],
  "inc-8": [
    {
      id: "sug-8",
      incidentId: "inc-8",
      projectId: "proj-checkout",
      rootCause:
        "Connection pool leak in the new order-export batch job, which does not release connections on failure.",
      confidence: 0.55,
      evidence: [
        { type: "log_event", id: "log-501", excerpt: "WARN: HikariPool-1 - Connection is not available, request timed out" },
      ],
      recommendedActions: [
        { description: "Add try-with-resources to the order-export batch job", risk_level: "MEDIUM" },
      ],
      status: "REJECTED",
      reviewedBy: "ada@acme.dev",
      reviewedAt: daysAgo(4),
      createdAt: daysAgo(5),
    },
  ],
};

export function getMockSuggestions(incidentId: string): AgentSuggestionResponse[] {
  return suggestionsByIncident[incidentId] ?? [];
}

export function getAllMockSuggestions(): AgentSuggestionResponse[] {
  return Object.values(suggestionsByIncident).flat();
}

export function reviewMockSuggestion(
  suggestionId: string,
  decision: "APPROVED" | "REJECTED",
): AgentSuggestionResponse | null {
  for (const list of Object.values(suggestionsByIncident)) {
    const suggestion = list.find((item) => item.id === suggestionId);
    if (suggestion) {
      suggestion.status = decision;
      suggestion.reviewedBy = mockUser.email;
      suggestion.reviewedAt = new Date().toISOString();
      return suggestion;
    }
  }
  return null;
}

export const mockAuditLogs: AuditLogResponse[] = [
  { id: "audit-1", projectId: "proj-checkout", actor: "agent", action: "AGENT_SUGGESTION_CREATED", entityType: "Incident", entityId: "inc-1", metadata: { suggestionId: "sug-1", confidence: "0.86" }, createdAt: minutesAgo(2) },
  { id: "audit-2", projectId: "proj-checkout", actor: "agent", action: "AGENT_SUGGESTION_CREATED", entityType: "Incident", entityId: "inc-2", metadata: { suggestionId: "sug-2", confidence: "0.42" }, createdAt: minutesAgo(5) },
  { id: "audit-3", projectId: "proj-checkout", actor: "ada@acme.dev", action: "INCIDENT_STATUS_CHANGED", entityType: "Incident", entityId: "inc-3", metadata: { status: "INVESTIGATING" }, createdAt: hoursAgo(1) },
  { id: "audit-4", projectId: "proj-checkout", actor: "ada@acme.dev", action: "INCIDENT_CREATED", entityType: "Incident", entityId: "inc-4", metadata: { severity: "MEDIUM" }, createdAt: hoursAgo(3) },
  { id: "audit-5", projectId: "proj-checkout", actor: "ada@acme.dev", action: "AGENT_SUGGESTION_REVIEWED", entityType: "Incident", entityId: "inc-7", metadata: { suggestionId: "sug-7", decision: "APPROVED" }, createdAt: hoursAgo(30) },
  { id: "audit-6", projectId: "proj-checkout", actor: "ada@acme.dev", action: "INCIDENT_STATUS_CHANGED", entityType: "Incident", entityId: "inc-7", metadata: { status: "RESOLVED" }, createdAt: daysAgo(1) },
  { id: "audit-7", projectId: "proj-checkout", actor: "ada@acme.dev", action: "AGENT_SUGGESTION_REVIEWED", entityType: "Incident", entityId: "inc-8", metadata: { suggestionId: "sug-8", decision: "REJECTED" }, createdAt: daysAgo(4) },
  { id: "audit-8", projectId: "proj-checkout", actor: "ada@acme.dev", action: "INCIDENT_STATUS_CHANGED", entityType: "Incident", entityId: "inc-8", metadata: { status: "CLOSED" }, createdAt: daysAgo(4) },
  { id: "audit-9", projectId: "proj-checkout", actor: "ada@acme.dev", action: "PROJECT_CREATED", entityType: "Project", entityId: "proj-checkout", metadata: { name: "checkout-service" }, createdAt: daysAgo(120) },
  { id: "audit-10", projectId: "proj-checkout", actor: "ada@acme.dev", action: "API_KEY_CREATED", entityType: "ApiKey", entityId: "key-1", metadata: { name: "checkout-prod-key" }, createdAt: daysAgo(110) },
  { id: "audit-11", projectId: "proj-checkout", actor: "ada@acme.dev", action: "API_KEY_REVOKED", entityType: "ApiKey", entityId: "key-2", metadata: { name: "old-ci-key" }, createdAt: daysAgo(30) },
  { id: "audit-12", projectId: "proj-payments", actor: "ada@acme.dev", action: "INCIDENT_CREATED", entityType: "Incident", entityId: "inc-11", metadata: { severity: "HIGH" }, createdAt: hoursAgo(4) },
  { id: "audit-13", projectId: "proj-payments", actor: "ada@acme.dev", action: "INCIDENT_STATUS_CHANGED", entityType: "Incident", entityId: "inc-12", metadata: { status: "INVESTIGATING" }, createdAt: hoursAgo(6) },
  { id: "audit-14", projectId: "proj-notifications", actor: "ada@acme.dev", action: "INCIDENT_CREATED", entityType: "Incident", entityId: "inc-14", metadata: { severity: "HIGH" }, createdAt: hoursAgo(1) },
  { id: "audit-15", projectId: null, actor: "ada@acme.dev", action: "PROJECT_CREATED", entityType: "Project", entityId: "proj-notifications", metadata: { name: "notifications-worker" }, createdAt: daysAgo(40) },
];

export const mockApiKeys: Record<string, ApiKeyResponse[]> = {
  "proj-checkout": [
    { id: "key-1", name: "checkout-prod-key", keyPrefix: "irp_live_8Kp2", lastUsedAt: minutesAgo(4), revokedAt: null, createdAt: daysAgo(110) },
    { id: "key-2", name: "old-ci-key", keyPrefix: "irp_live_Xq91", lastUsedAt: daysAgo(35), revokedAt: daysAgo(30), createdAt: daysAgo(100) },
  ],
  "proj-payments": [
    { id: "key-3", name: "payments-agent-key", keyPrefix: "irp_live_Zt74", lastUsedAt: hoursAgo(1), revokedAt: null, createdAt: daysAgo(90) },
  ],
  "proj-notifications": [
    { id: "key-4", name: "notifications-sdk-key", keyPrefix: "irp_live_Ln30", lastUsedAt: hoursAgo(2), revokedAt: null, createdAt: daysAgo(38) },
  ],
};

export const mockRunbooks: RunbookView[] = [
  { id: "rb-1", projectId: "proj-checkout", title: "Checkout Service Incident Playbook", uploadedBy: "ada@acme.dev", version: 3, chunkCount: 42, createdAt: daysAgo(60) },
  { id: "rb-2", projectId: "proj-checkout", title: "Database Failover Procedure", uploadedBy: "ada@acme.dev", version: 1, chunkCount: 18, createdAt: daysAgo(45) },
  { id: "rb-3", projectId: "proj-checkout", title: "Payment Gateway Troubleshooting Guide", uploadedBy: "ada@acme.dev", version: 2, chunkCount: 27, createdAt: daysAgo(30) },
  { id: "rb-4", projectId: "proj-checkout", title: "On-call Escalation Policy", uploadedBy: "ada@acme.dev", version: 1, chunkCount: 9, createdAt: daysAgo(20) },
];

export const mockAutomations: AutomationRuleView[] = [
  { id: "auto-1", name: "Notify Slack on CRITICAL incidents", condition: "severity = CRITICAL", action: "Post to #incidents-critical", enabled: true, lastTriggeredAt: minutesAgo(25) },
  { id: "auto-2", name: "Auto-approve high-confidence, low-risk suggestions", condition: "confidence >= 90% AND risk = LOW", action: "Auto-approve the suggestion", enabled: false, lastTriggeredAt: null },
  { id: "auto-3", name: "Page on-call for stale approvals", condition: "status = AWAITING_APPROVAL for > 30 minutes", action: "Trigger PagerDuty escalation", enabled: true, lastTriggeredAt: hoursAgo(9) },
];

export const mockIntegrations: IntegrationView[] = [
  { id: "int-slack", name: "Slack", description: "Post incident and AI suggestion updates to a Slack channel.", connected: true, icon: "slack" },
  { id: "int-pagerduty", name: "PagerDuty", description: "Trigger on-call escalations for critical incidents.", connected: false, icon: "pagerduty" },
  { id: "int-github", name: "GitHub", description: "Link incidents to commits, PRs, and deploys.", connected: true, icon: "github" },
  { id: "int-webhook", name: "Generic Webhook", description: "Send incident events to any HTTPS endpoint.", connected: false, icon: "webhook" },
];

const servicesSeed: Record<string, { errors: number; deploys: number }> = {
  checkout: { errors: 3, deploys: 1 },
  "payments-gateway": { errors: 2, deploys: 0 },
  search: { errors: 1, deploys: 0 },
  "refunds-api": { errors: 2, deploys: 0 },
  "ledger-service": { errors: 1, deploys: 0 },
  "notifications-worker": { errors: 2, deploys: 0 },
  "sms-gateway": { errors: 1, deploys: 0 },
};

export function getMockLogsForService(service: string | null): LogEventView[] {
  if (!service) return [];
  return [
    { id: `log-${service}-1`, occurredAt: minutesAgo(3), level: "ERROR", service, message: `${service}: unhandled exception in request pipeline`, traceId: "trace-88a1" },
    { id: `log-${service}-2`, occurredAt: minutesAgo(6), level: "WARN", service, message: `${service}: response latency exceeded 800ms threshold`, traceId: "trace-88a1" },
    { id: `log-${service}-3`, occurredAt: minutesAgo(12), level: "INFO", service, message: `${service}: health check passed`, traceId: null },
  ];
}

export function getMockErrorsForService(service: string | null): ErrorEventView[] {
  if (!service) return [];
  const count = servicesSeed[service]?.errors ?? 1;
  return Array.from({ length: count }, (_, i) => ({
    id: `err-${service}-${i + 1}`,
    occurredAt: minutesAgo(4 + i * 7),
    service,
    exceptionType: i === 0 ? "NullPointerException" : "TimeoutException",
    message:
      i === 0
        ? `Cannot invoke ${service} operation because a required field is null`
        : `Call to upstream dependency timed out after 5000ms`,
    stackHash: `hash-${service}-${i}`,
  }));
}

export function getMockDeploymentsForService(service: string | null): DeploymentEventView[] {
  if (!service) return [];
  const count = servicesSeed[service]?.deploys ?? 0;
  if (count === 0) return [];
  return [
    { id: `dep-${service}-1`, occurredAt: minutesAgo(15), service, version: "1.4.0", status: "SUCCEEDED" },
  ];
}
