import { apiRequest } from "@/lib/apiClient";
import { USE_MOCKS } from "@/lib/config";
import { delay } from "@/lib/delay";
import { getMockAgentRuns } from "@/mocks/mockData";
import type { AgentRunDetailResponse } from "@/types";

export async function listAgentRuns(projectId: string, incidentId: string): Promise<AgentRunDetailResponse[]> {
  if (USE_MOCKS) return delay(getMockAgentRuns(incidentId));
  return apiRequest<AgentRunDetailResponse[]>(
    `/api/v1/projects/${projectId}/incidents/${incidentId}/agent-runs`,
  );
}
