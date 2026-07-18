import { apiRequest } from "@/lib/apiClient";
import { USE_MOCKS } from "@/lib/config";
import { delay } from "@/lib/delay";
import { paginate } from "@/lib/paginate";
import { getMockTimeline, mockIncidents } from "@/mocks/mockData";
import type {
  IncidentDetailResponse,
  IncidentResponse,
  IncidentStatus,
  PageResponse,
  Severity,
} from "@/types";

export interface ListIncidentsParams {
  status?: IncidentStatus;
  severity?: Severity;
  page?: number;
  size?: number;
}

export async function listIncidents(
  projectId: string,
  params: ListIncidentsParams = {},
): Promise<PageResponse<IncidentResponse>> {
  const { status, severity, page = 0, size = 20 } = params;

  if (USE_MOCKS) {
    const filtered = mockIncidents.filter(
      (incident) =>
        incident.projectId === projectId &&
        (!status || incident.status === status) &&
        (!severity || incident.severity === severity),
    );
    const sorted = [...filtered].sort((a, b) => (a.openedAt < b.openedAt ? 1 : -1));
    return delay(paginate(sorted, page, size));
  }

  return apiRequest<PageResponse<IncidentResponse>>(`/api/v1/projects/${projectId}/incidents`, {
    query: { status, severity, page, size },
  });
}

export async function getIncident(projectId: string, incidentId: string): Promise<IncidentDetailResponse> {
  if (USE_MOCKS) {
    const incident = mockIncidents.find((item) => item.id === incidentId && item.projectId === projectId);
    if (!incident) throw new Error("Incident not found");
    return delay({ incident, timeline: getMockTimeline(incidentId) });
  }
  return apiRequest<IncidentDetailResponse>(`/api/v1/projects/${projectId}/incidents/${incidentId}`);
}

export interface CreateIncidentPayload {
  title: string;
  description?: string;
  severity: Severity;
  service?: string;
}

export async function createIncident(
  projectId: string,
  payload: CreateIncidentPayload,
): Promise<IncidentResponse> {
  if (USE_MOCKS) {
    const now = new Date().toISOString();
    const incident: IncidentResponse = {
      id: `inc-${Date.now()}`,
      projectId,
      title: payload.title,
      description: payload.description ?? null,
      severity: payload.severity,
      status: "OPEN",
      service: payload.service ?? null,
      openedAt: now,
      resolvedAt: null,
      createdAt: now,
      updatedAt: now,
    };
    mockIncidents.unshift(incident);
    return delay(incident);
  }
  return apiRequest<IncidentResponse>(`/api/v1/projects/${projectId}/incidents`, {
    method: "POST",
    body: payload,
  });
}

export async function updateIncidentStatus(
  projectId: string,
  incidentId: string,
  status: IncidentStatus,
  note?: string,
): Promise<IncidentResponse> {
  if (USE_MOCKS) {
    const incident = mockIncidents.find((item) => item.id === incidentId && item.projectId === projectId);
    if (!incident) throw new Error("Incident not found");
    incident.status = status;
    incident.updatedAt = new Date().toISOString();
    if (status === "RESOLVED" || status === "CLOSED") incident.resolvedAt = incident.updatedAt;
    return delay(incident);
  }
  return apiRequest<IncidentResponse>(`/api/v1/projects/${projectId}/incidents/${incidentId}/status`, {
    method: "PATCH",
    body: { status, note },
  });
}
