import { apiRequest } from "@/lib/apiClient";
import { USE_MOCKS } from "@/lib/config";
import { delay } from "@/lib/delay";
import {
  getMockDeploymentsForService,
  getMockErrorsForService,
  getMockLogsForService,
  mockIncidents,
} from "@/mocks/mockData";
import type { DeploymentEventView, ErrorEventView, LogEventView } from "@/types";

export async function getRelatedSignals(
  projectId: string,
  incidentId: string,
): Promise<{
  logs: LogEventView[];
  errors: ErrorEventView[];
  deployments: DeploymentEventView[];
}> {
  if (USE_MOCKS) {
    const service = mockIncidents.find((incident) => incident.id === incidentId)?.service ?? null;
    return delay({
      logs: getMockLogsForService(service),
      errors: getMockErrorsForService(service),
      deployments: getMockDeploymentsForService(service),
    });
  }
  return apiRequest(`/api/v1/projects/${projectId}/signals`, { query: { incidentId } });
}
