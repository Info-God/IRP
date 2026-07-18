import { apiRequest } from "@/lib/apiClient";
import { USE_MOCKS } from "@/lib/config";
import { delay } from "@/lib/delay";
import { paginate } from "@/lib/paginate";
import { mockAuditLogs } from "@/mocks/mockData";
import type { AuditLogResponse, PageResponse } from "@/types";

export async function listAuditLogs(
  projectId: string | null,
  page = 0,
  size = 20,
): Promise<PageResponse<AuditLogResponse>> {
  if (USE_MOCKS) {
    const filtered = projectId ? mockAuditLogs.filter((log) => log.projectId === projectId) : mockAuditLogs;
    const sorted = [...filtered].sort((a, b) => (a.createdAt < b.createdAt ? 1 : -1));
    return delay(paginate(sorted, page, size));
  }
  return apiRequest<PageResponse<AuditLogResponse>>("/api/v1/audit-logs", {
    query: { projectId: projectId ?? undefined, page, size },
  });
}
