import { delay } from "@/lib/delay";
import { mockIntegrations } from "@/mocks/mockData";
import type { IntegrationView } from "@/types";

/** No backend/domain model exists yet - see Phase 4 design doc gap list. Mock-only. */
export async function listIntegrations(): Promise<IntegrationView[]> {
  return delay([...mockIntegrations]);
}
