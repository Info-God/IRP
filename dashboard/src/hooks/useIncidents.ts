import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { queryKeys } from "@/hooks/queryKeys";
import {
  createIncident,
  getIncident,
  listIncidents,
  updateIncidentStatus,
  type CreateIncidentPayload,
  type ListIncidentsParams,
} from "@/services/incidents";
import type { IncidentStatus } from "@/types";

export function useIncidents(projectId: string | undefined, params: ListIncidentsParams) {
  return useQuery({
    queryKey: queryKeys.incidents(projectId ?? "", params),
    queryFn: () => listIncidents(projectId!, params),
    enabled: Boolean(projectId),
  });
}

export function useIncident(projectId: string | undefined, incidentId: string | undefined) {
  return useQuery({
    queryKey: queryKeys.incident(projectId ?? "", incidentId ?? ""),
    queryFn: () => getIncident(projectId!, incidentId!),
    enabled: Boolean(projectId && incidentId),
  });
}

export function useCreateIncident(projectId: string | undefined) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: CreateIncidentPayload) => createIncident(projectId!, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["incidents", projectId] });
    },
  });
}

export function useUpdateIncidentStatus(projectId: string | undefined, incidentId: string | undefined) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ status, note }: { status: IncidentStatus; note?: string }) =>
      updateIncidentStatus(projectId!, incidentId!, status, note),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.incident(projectId ?? "", incidentId ?? "") });
      queryClient.invalidateQueries({ queryKey: ["incidents", projectId] });
    },
  });
}
