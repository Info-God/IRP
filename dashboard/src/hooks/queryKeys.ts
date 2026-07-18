import type { ListIncidentsParams } from "@/services/incidents";

export const queryKeys = {
  projects: ["projects"] as const,
  organization: ["organization"] as const,
  incidents: (projectId: string, params: ListIncidentsParams) =>
    ["incidents", projectId, params] as const,
  incident: (projectId: string, incidentId: string) => ["incident", projectId, incidentId] as const,
  suggestions: (projectId: string, incidentId: string) =>
    ["agent-suggestions", projectId, incidentId] as const,
  allSuggestions: ["agent-suggestions", "all"] as const,
  auditLogs: (projectId: string | null, page: number) => ["audit-logs", projectId, page] as const,
  apiKeys: (projectId: string) => ["api-keys", projectId] as const,
  runbooks: ["runbooks"] as const,
  automations: ["automations"] as const,
  integrations: ["integrations"] as const,
  signals: (service: string | null) => ["signals", service] as const,
};
