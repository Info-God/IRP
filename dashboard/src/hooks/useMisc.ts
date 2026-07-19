import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { queryKeys } from "@/hooks/queryKeys";
import { getMyOrganization } from "@/services/organizations";
import { createRunbook, listRunbooks, type CreateRunbookPayload } from "@/services/runbooks";
import { listAutomations } from "@/services/automations";
import { listIntegrations } from "@/services/integrations";
import { getRelatedSignals } from "@/services/signals";

export function useOrganization() {
  return useQuery({ queryKey: queryKeys.organization, queryFn: getMyOrganization });
}

export function useRunbooks(projectId: string | undefined) {
  return useQuery({
    queryKey: queryKeys.runbooks(projectId ?? ""),
    queryFn: () => listRunbooks(projectId!),
    enabled: Boolean(projectId),
  });
}

export function useCreateRunbook(projectId: string | undefined) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: CreateRunbookPayload) => createRunbook(projectId!, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.runbooks(projectId ?? "") });
    },
  });
}

export function useAutomations() {
  return useQuery({ queryKey: queryKeys.automations, queryFn: listAutomations });
}

export function useIntegrations() {
  return useQuery({ queryKey: queryKeys.integrations, queryFn: listIntegrations });
}

export function useRelatedSignals(projectId: string | undefined, incidentId: string | undefined) {
  return useQuery({
    queryKey: queryKeys.signals(projectId ?? "", incidentId ?? ""),
    queryFn: () => getRelatedSignals(projectId!, incidentId!),
    enabled: Boolean(projectId && incidentId),
  });
}
