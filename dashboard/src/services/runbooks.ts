import { apiRequest } from "@/lib/apiClient";
import { USE_MOCKS } from "@/lib/config";
import { delay } from "@/lib/delay";
import { mockRunbooks } from "@/mocks/mockData";
import type { RunbookView } from "@/types";

export async function listRunbooks(projectId: string): Promise<RunbookView[]> {
  if (USE_MOCKS) {
    return delay(mockRunbooks.filter((runbook) => runbook.projectId === projectId));
  }
  return apiRequest<RunbookView[]>(`/api/v1/projects/${projectId}/runbooks`);
}

export interface CreateRunbookPayload {
  title: string;
  content: string;
}

export async function createRunbook(projectId: string, payload: CreateRunbookPayload): Promise<RunbookView> {
  if (USE_MOCKS) {
    const now = new Date().toISOString();
    const runbook: RunbookView = {
      id: `rb-${Date.now()}`,
      projectId,
      title: payload.title,
      uploadedBy: "you@acme.dev",
      version: 1,
      chunkCount: Math.max(1, Math.ceil(payload.content.split(/\s+/).length / 180)),
      createdAt: now,
    };
    mockRunbooks.unshift(runbook);
    return delay(runbook);
  }
  return apiRequest<RunbookView>(`/api/v1/projects/${projectId}/runbooks`, {
    method: "POST",
    body: payload,
  });
}
