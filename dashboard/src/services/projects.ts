import { apiRequest } from "@/lib/apiClient";
import { USE_MOCKS } from "@/lib/config";
import { delay } from "@/lib/delay";
import { mockProjects } from "@/mocks/mockData";
import type { ProjectResponse } from "@/types";

export async function listProjects(): Promise<ProjectResponse[]> {
  if (USE_MOCKS) return delay([...mockProjects]);
  return apiRequest<ProjectResponse[]>("/api/v1/projects");
}

export interface CreateProjectPayload {
  name: string;
  environment: "development" | "staging" | "production";
}

export async function createProject(payload: CreateProjectPayload): Promise<ProjectResponse> {
  if (USE_MOCKS) {
    const project: ProjectResponse = {
      id: `proj-${Date.now()}`,
      organizationId: "org-1",
      name: payload.name,
      environment: payload.environment,
      createdAt: new Date().toISOString(),
    };
    mockProjects.push(project);
    return delay(project);
  }
  return apiRequest<ProjectResponse>("/api/v1/projects", { method: "POST", body: payload });
}
