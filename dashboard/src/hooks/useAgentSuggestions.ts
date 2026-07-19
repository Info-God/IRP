import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { queryKeys } from "@/hooks/queryKeys";
import { listAllSuggestions, listSuggestions, reviewSuggestion } from "@/services/agentSuggestions";

export function useAgentSuggestions(projectId: string | undefined, incidentId: string | undefined) {
  return useQuery({
    queryKey: queryKeys.suggestions(projectId ?? "", incidentId ?? ""),
    queryFn: () => listSuggestions(projectId!, incidentId!),
    enabled: Boolean(projectId && incidentId),
  });
}

export function useAllAgentSuggestions(projectId: string | undefined) {
  return useQuery({
    queryKey: queryKeys.allSuggestions(projectId ?? ""),
    queryFn: () => listAllSuggestions(projectId!),
    enabled: Boolean(projectId),
  });
}

export function useReviewSuggestion(projectId: string | undefined, incidentId: string | undefined) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({
      suggestionId,
      decision,
      note,
    }: {
      suggestionId: string;
      decision: "APPROVED" | "REJECTED";
      note?: string;
    }) => reviewSuggestion(projectId!, incidentId!, suggestionId, decision, note),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.suggestions(projectId ?? "", incidentId ?? "") });
      queryClient.invalidateQueries({ queryKey: queryKeys.incident(projectId ?? "", incidentId ?? "") });
      queryClient.invalidateQueries({ queryKey: ["incidents", projectId] });
      queryClient.invalidateQueries({ queryKey: queryKeys.allSuggestions(projectId ?? "") });
    },
  });
}

/** Same mutation, but takes projectId/incidentId per-call instead of per-hook -
 * needed by the AI Copilot queue, where each row belongs to a different incident. */
export function useReviewSuggestionAny() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({
      projectId,
      incidentId,
      suggestionId,
      decision,
      note,
    }: {
      projectId: string;
      incidentId: string;
      suggestionId: string;
      decision: "APPROVED" | "REJECTED";
      note?: string;
    }) => reviewSuggestion(projectId, incidentId, suggestionId, decision, note),
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({ queryKey: queryKeys.suggestions(variables.projectId, variables.incidentId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.incident(variables.projectId, variables.incidentId) });
      queryClient.invalidateQueries({ queryKey: ["incidents", variables.projectId] });
      queryClient.invalidateQueries({ queryKey: queryKeys.allSuggestions(variables.projectId) });
    },
  });
}
