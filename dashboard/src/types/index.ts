/**
 * Types mirror irp-core's actual DTOs field-for-field (see
 * project/src/main/java/com/irp/core/**\/dto/*.java) so the API client and mock
 * layer can be swapped without touching component code.
 */

export type Severity = "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";

export type IncidentStatus =
  | "OPEN"
  | "INVESTIGATING"
  | "AWAITING_APPROVAL"
  | "RESOLVED"
  | "CLOSED";

export type TimelineEntryType =
  | "CREATED"
  | "STATUS_CHANGE"
  | "NOTE"
  | "AGENT_ACTION"
  | "APPROVAL";

export type SuggestionStatus = "PENDING_REVIEW" | "APPROVED" | "REJECTED";

export type RiskLevel = "LOW" | "MEDIUM" | "HIGH";

export interface PageResponse<T> {
  items: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface UserSummary {
  id: string;
  email: string;
  fullName: string;
  role: "OWNER" | "ADMIN" | "MEMBER";
  organizationId: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresInSeconds: number;
  user: UserSummary;
}

export interface OrganizationResponse {
  id: string;
  name: string;
  slug: string;
  createdAt: string;
}

export interface ProjectResponse {
  id: string;
  organizationId: string;
  name: string;
  environment: "development" | "staging" | "production";
  createdAt: string;
}

export interface IncidentResponse {
  id: string;
  projectId: string;
  title: string;
  description: string | null;
  severity: Severity;
  status: IncidentStatus;
  service: string | null;
  openedAt: string;
  resolvedAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface IncidentTimelineEntryResponse {
  id: string;
  actor: string;
  entryType: TimelineEntryType;
  message: string;
  createdAt: string;
}

export interface IncidentDetailResponse {
  incident: IncidentResponse;
  timeline: IncidentTimelineEntryResponse[];
}

export interface EvidenceItem {
  type: "error_event" | "log_event" | "deployment" | "runbook";
  id: string;
  excerpt: string;
}

export interface RecommendedActionItem {
  description: string;
  risk_level: RiskLevel;
}

export interface AgentSuggestionResponse {
  id: string;
  incidentId: string;
  projectId: string;
  rootCause: string;
  confidence: number;
  evidence: EvidenceItem[];
  recommendedActions: RecommendedActionItem[];
  status: SuggestionStatus;
  reviewedBy: string | null;
  reviewedAt: string | null;
  createdAt: string;
}

export type AgentRunStatus = "RUNNING" | "SUCCEEDED" | "FAILED";

export interface AgentRunResponse {
  id: string;
  incidentId: string;
  projectId: string;
  status: AgentRunStatus;
  model: string | null;
  tokenUsage: Record<string, number> | null;
  startedAt: string;
  finishedAt: string | null;
}

export interface AgentStepResponse {
  id: string;
  stepIndex: number;
  toolName: string;
  toolInput: unknown;
  toolOutput: unknown;
  createdAt: string;
}

export interface AgentRunDetailResponse {
  run: AgentRunResponse;
  steps: AgentStepResponse[];
}

export interface AuditLogResponse {
  id: string;
  projectId: string | null;
  actor: string;
  action: string;
  entityType: string;
  entityId: string;
  metadata: Record<string, unknown>;
  createdAt: string;
}

export interface ApiKeyResponse {
  id: string;
  name: string;
  keyPrefix: string;
  lastUsedAt: string | null;
  revokedAt: string | null;
  createdAt: string;
}

export interface ApiKeyCreatedResponse extends ApiKeyResponse {
  plaintextKey: string;
}

// --- Types with no backend endpoint yet - shaped for the UI, sourced from mocks only ---

export type LogLevel = "TRACE" | "DEBUG" | "INFO" | "WARN" | "ERROR";

export interface LogEventView {
  id: string;
  occurredAt: string;
  level: LogLevel;
  service: string;
  message: string;
  traceId: string | null;
}

export interface ErrorEventView {
  id: string;
  occurredAt: string;
  service: string;
  exceptionType: string;
  message: string;
  stackHash: string;
}

export type DeploymentStatus = "STARTED" | "SUCCEEDED" | "FAILED" | "ROLLED_BACK";

export interface DeploymentEventView {
  id: string;
  occurredAt: string;
  service: string;
  version: string;
  status: DeploymentStatus;
}

export interface RunbookView {
  id: string;
  projectId: string;
  title: string;
  uploadedBy: string;
  version: number;
  chunkCount: number;
  createdAt: string;
}

export interface AutomationRuleView {
  id: string;
  name: string;
  condition: string;
  action: string;
  enabled: boolean;
  lastTriggeredAt: string | null;
}

export interface IntegrationView {
  id: string;
  name: string;
  description: string;
  connected: boolean;
  icon: "slack" | "pagerduty" | "github" | "webhook";
}
