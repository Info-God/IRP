import { apiRequest } from "@/lib/apiClient";
import { USE_MOCKS } from "@/lib/config";
import { delay } from "@/lib/delay";
import { getAllMockSuggestions, getMockSuggestions, reviewMockSuggestion } from "@/mocks/mockData";
import type { AgentSuggestionResponse } from "@/types";

export async function listSuggestions(
  projectId: string,
  incidentId: string,
): Promise<AgentSuggestionResponse[]> {
  if (USE_MOCKS) return delay(getMockSuggestions(incidentId));
  return apiRequest<AgentSuggestionResponse[]>(
    `/api/v1/projects/${projectId}/incidents/${incidentId}/agent-suggestions`,
  );
}

export async function reviewSuggestion(
  projectId: string,
  incidentId: string,
  suggestionId: string,
  decision: "APPROVED" | "REJECTED",
  note?: string,
): Promise<AgentSuggestionResponse> {
  if (USE_MOCKS) {
    const updated = reviewMockSuggestion(suggestionId, decision);
    if (!updated) throw new Error("Suggestion not found");
    return delay(updated);
  }
  return apiRequest<AgentSuggestionResponse>(
    `/api/v1/projects/${projectId}/incidents/${incidentId}/agent-suggestions/${suggestionId}`,
    { method: "PATCH", body: { decision, note } },
  );
}

/** Every suggestion for a project, across all its incidents - backs the AI Copilot queue. */
export async function listAllSuggestions(projectId: string): Promise<AgentSuggestionResponse[]> {
  if (USE_MOCKS) return delay(getAllMockSuggestions().filter((s) => s.projectId === projectId));
  return apiRequest<AgentSuggestionResponse[]>(`/api/v1/projects/${projectId}/agent-suggestions`);
}
