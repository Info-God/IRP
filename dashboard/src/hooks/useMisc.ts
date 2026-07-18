import { useQuery } from "@tanstack/react-query";
import { queryKeys } from "@/hooks/queryKeys";
import { getMyOrganization } from "@/services/organizations";
import { listRunbooks } from "@/services/runbooks";
import { listAutomations } from "@/services/automations";
import { listIntegrations } from "@/services/integrations";
import { getRelatedSignals } from "@/services/signals";

export function useOrganization() {
  return useQuery({ queryKey: queryKeys.organization, queryFn: getMyOrganization });
}

export function useRunbooks() {
  return useQuery({ queryKey: queryKeys.runbooks, queryFn: listRunbooks });
}

export function useAutomations() {
  return useQuery({ queryKey: queryKeys.automations, queryFn: listAutomations });
}

export function useIntegrations() {
  return useQuery({ queryKey: queryKeys.integrations, queryFn: listIntegrations });
}

export function useRelatedSignals(service: string | null) {
  return useQuery({
    queryKey: queryKeys.signals(service),
    queryFn: () => getRelatedSignals(service),
  });
}
