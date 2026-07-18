import { useQuery } from "@tanstack/react-query";
import { queryKeys } from "@/hooks/queryKeys";
import { listAgentRuns } from "@/services/agentRuns";

export function useAgentRuns(projectId: string | undefined, incidentId: string | undefined) {
  return useQuery({
    queryKey: queryKeys.agentRuns(projectId ?? "", incidentId ?? ""),
    queryFn: () => listAgentRuns(projectId!, incidentId!),
    enabled: Boolean(projectId && incidentId),
  });
}
