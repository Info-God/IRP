import { useQuery } from "@tanstack/react-query";
import { queryKeys } from "@/hooks/queryKeys";
import { listAuditLogs } from "@/services/auditLogs";

export function useAuditLogs(projectId: string | null, page: number) {
  return useQuery({
    queryKey: queryKeys.auditLogs(projectId, page),
    queryFn: () => listAuditLogs(projectId, page),
  });
}
