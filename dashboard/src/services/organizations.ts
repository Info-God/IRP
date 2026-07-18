import { apiRequest } from "@/lib/apiClient";
import { USE_MOCKS } from "@/lib/config";
import { delay } from "@/lib/delay";
import { mockOrganization } from "@/mocks/mockData";
import type { OrganizationResponse } from "@/types";

export async function getMyOrganization(): Promise<OrganizationResponse> {
  if (USE_MOCKS) return delay(mockOrganization);
  return apiRequest<OrganizationResponse>("/api/v1/organizations/me");
}
