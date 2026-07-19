import { ApiError, apiRequest } from "@/lib/apiClient";
import { USE_MOCKS } from "@/lib/config";
import { delay } from "@/lib/delay";
import type { SlackIntegrationResponse } from "@/types";

let mockIntegration: SlackIntegrationResponse | null = {
  id: "slack-1",
  maskedWebhookUrl: "https://hooks.slack.com/services/••••7f2a",
  connectedBy: "ada@acme.dev",
  createdAt: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString(),
  updatedAt: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString(),
};

export async function getSlackIntegration(projectId: string): Promise<SlackIntegrationResponse | null> {
  if (USE_MOCKS) return delay(mockIntegration);
  try {
    return await apiRequest<SlackIntegrationResponse>(`/api/v1/projects/${projectId}/integrations/slack`);
  } catch (error) {
    if (error instanceof ApiError && error.status === 404) return null;
    throw error;
  }
}

export async function connectSlackIntegration(projectId: string, webhookUrl: string): Promise<SlackIntegrationResponse> {
  if (USE_MOCKS) {
    const now = new Date().toISOString();
    mockIntegration = {
      id: "slack-1",
      maskedWebhookUrl: `https://hooks.slack.com/services/••••${webhookUrl.slice(-4)}`,
      connectedBy: "you@acme.dev",
      createdAt: mockIntegration?.createdAt ?? now,
      updatedAt: now,
    };
    return delay(mockIntegration);
  }
  return apiRequest<SlackIntegrationResponse>(`/api/v1/projects/${projectId}/integrations/slack`, {
    method: "PUT",
    body: { webhookUrl },
  });
}

export async function disconnectSlackIntegration(projectId: string): Promise<void> {
  if (USE_MOCKS) {
    mockIntegration = null;
    return delay(undefined);
  }
  return apiRequest<void>(`/api/v1/projects/${projectId}/integrations/slack`, { method: "DELETE" });
}

export async function sendTestSlackMessage(projectId: string): Promise<void> {
  if (USE_MOCKS) return delay(undefined);
  return apiRequest<void>(`/api/v1/projects/${projectId}/integrations/slack/test`, { method: "POST" });
}
