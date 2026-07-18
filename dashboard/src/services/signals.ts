import { delay } from "@/lib/delay";
import {
  getMockDeploymentsForService,
  getMockErrorsForService,
  getMockLogsForService,
} from "@/mocks/mockData";
import type { DeploymentEventView, ErrorEventView, LogEventView } from "@/types";

/**
 * No JWT-authenticated read endpoint exists for logs/errors/deployments yet -
 * only the API-key-only AgentController has these, for the AI agent itself.
 * Mock-only until the Phase 4 backend gap-fill adds a dashboard-facing
 * equivalent (see design doc: GET /api/v1/projects/{id}/logs|errors|deployments).
 */
export async function getRelatedSignals(service: string | null): Promise<{
  logs: LogEventView[];
  errors: ErrorEventView[];
  deployments: DeploymentEventView[];
}> {
  return delay({
    logs: getMockLogsForService(service),
    errors: getMockErrorsForService(service),
    deployments: getMockDeploymentsForService(service),
  });
}
