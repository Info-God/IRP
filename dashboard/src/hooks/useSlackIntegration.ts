import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { queryKeys } from "@/hooks/queryKeys";
import {
  connectSlackIntegration,
  disconnectSlackIntegration,
  getSlackIntegration,
  sendTestSlackMessage,
} from "@/services/slackIntegration";

export function useSlackIntegration(projectId: string | undefined) {
  return useQuery({
    queryKey: queryKeys.slackIntegration(projectId ?? ""),
    queryFn: () => getSlackIntegration(projectId!),
    enabled: Boolean(projectId),
  });
}

export function useConnectSlackIntegration(projectId: string | undefined) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (webhookUrl: string) => connectSlackIntegration(projectId!, webhookUrl),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.slackIntegration(projectId ?? "") });
    },
  });
}

export function useDisconnectSlackIntegration(projectId: string | undefined) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => disconnectSlackIntegration(projectId!),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.slackIntegration(projectId ?? "") });
    },
  });
}

export function useSendTestSlackMessage(projectId: string | undefined) {
  return useMutation({
    mutationFn: () => sendTestSlackMessage(projectId!),
  });
}
