import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { queryKeys } from "@/hooks/queryKeys";
import { createApiKey, listApiKeys, revokeApiKey } from "@/services/apiKeys";

export function useApiKeys(projectId: string | undefined) {
  return useQuery({
    queryKey: queryKeys.apiKeys(projectId ?? ""),
    queryFn: () => listApiKeys(projectId!),
    enabled: Boolean(projectId),
  });
}

export function useCreateApiKey(projectId: string | undefined) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (name: string) => createApiKey(projectId!, name),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.apiKeys(projectId ?? "") });
    },
  });
}

export function useRevokeApiKey(projectId: string | undefined) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (apiKeyId: string) => revokeApiKey(projectId!, apiKeyId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.apiKeys(projectId ?? "") });
    },
  });
}
