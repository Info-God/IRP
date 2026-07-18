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

/**
 * irp-core has no project-wide "list all suggestions" endpoint yet - only
 * per-incident listing exists (see Phase 4 design doc gap list). This
 * aggregates across mock incidents for the AI Copilot page; swap for a real
 * `GET /api/v1/projects/{id}/agent-suggestions?status=` once it's built.
 */
export async function listAllSuggestions(): Promise<AgentSuggestionResponse[]> {
  if (USE_MOCKS) return delay(getAllMockSuggestions());
  throw new Error("listAllSuggestions has no backend endpoint yet - see Phase 4 design doc gap list");
}
