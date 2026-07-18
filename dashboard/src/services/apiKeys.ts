import { apiRequest } from "@/lib/apiClient";
import { USE_MOCKS } from "@/lib/config";
import { delay } from "@/lib/delay";
import { mockApiKeys } from "@/mocks/mockData";
import type { ApiKeyCreatedResponse, ApiKeyResponse } from "@/types";

export async function listApiKeys(projectId: string): Promise<ApiKeyResponse[]> {
  if (USE_MOCKS) return delay(mockApiKeys[projectId] ?? []);
  return apiRequest<ApiKeyResponse[]>(`/api/v1/projects/${projectId}/api-keys`);
}

export async function createApiKey(projectId: string, name: string): Promise<ApiKeyCreatedResponse> {
  if (USE_MOCKS) {
    const created: ApiKeyCreatedResponse = {
      id: `key-${Date.now()}`,
      name,
      keyPrefix: `irp_live_${Math.random().toString(36).slice(2, 6)}`,
      plaintextKey: `irp_live_${Math.random().toString(36).slice(2, 26)}`,
      lastUsedAt: null,
      revokedAt: null,
      createdAt: new Date().toISOString(),
    };
    mockApiKeys[projectId] = [...(mockApiKeys[projectId] ?? []), created];
    return delay(created);
  }
  return apiRequest<ApiKeyCreatedResponse>(`/api/v1/projects/${projectId}/api-keys`, {
    method: "POST",
    body: { name },
  });
}

export async function revokeApiKey(projectId: string, apiKeyId: string): Promise<void> {
  if (USE_MOCKS) {
    const key = (mockApiKeys[projectId] ?? []).find((item) => item.id === apiKeyId);
    if (key) key.revokedAt = new Date().toISOString();
    return delay(undefined);
  }
  return apiRequest<void>(`/api/v1/projects/${projectId}/api-keys/${apiKeyId}`, { method: "DELETE" });
}
