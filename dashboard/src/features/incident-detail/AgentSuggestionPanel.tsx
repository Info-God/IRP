import { CheckCircle2, Sparkles, XCircle } from "lucide-react";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Badge } from "@/components/ui/Badge";
import { ConfidenceMeter } from "@/components/ui/ConfidenceMeter";
import { EmptyState } from "@/components/ui/EmptyState";
import { Skeleton } from "@/components/ui/Skeleton";
import { RiskBadge } from "@/features/agent/RiskBadge";
import { useAgentSuggestions, useReviewSuggestion } from "@/hooks/useAgentSuggestions";
import { formatDateTime } from "@/lib/formatters";

export function AgentSuggestionPanel({ projectId, incidentId }: { projectId: string; incidentId: string }) {
  const { data: suggestions = [], isLoading } = useAgentSuggestions(projectId, incidentId);
  const review = useReviewSuggestion(projectId, incidentId);

  if (isLoading) {
    return (
      <div className="space-y-3">
        <Skeleton className="h-40 w-full" />
      </div>
    );
  }

  if (suggestions.length === 0) {
    return (
      <EmptyState
        icon={Sparkles}
        title="No AI investigation yet"
        description="Once the AI agent investigates this incident, its root-cause analysis and recommended actions will appear here for review."
      />
    );
  }

  return (
    <div className="space-y-4">
      {suggestions.map((suggestion) => (
        <Card key={suggestion.id}>
          <CardHeader
            title={
              <span className="flex items-center gap-2">
                <Sparkles className="h-4 w-4 text-indigo-400" />
                AI-proposed root cause
              </span>
            }
            description={`Proposed ${formatDateTime(suggestion.createdAt)}`}
            action={
              suggestion.status !== "PENDING_REVIEW" ? (
                <Badge tone={suggestion.status === "APPROVED" ? "green" : "red"}>
                  {suggestion.status === "APPROVED" ? "Approved" : "Rejected"}
                  {suggestion.reviewedBy ? ` by ${suggestion.reviewedBy}` : ""}
                </Badge>
              ) : (
                <Badge tone="amber">Awaiting your review</Badge>
              )
            }
          />
          <CardBody className="space-y-4">
            <p className="text-sm leading-relaxed text-slate-200">{suggestion.rootCause}</p>

            <ConfidenceMeter confidence={suggestion.confidence} />

            <div>
              <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-slate-500">Evidence</p>
              <ul className="space-y-1.5">
                {suggestion.evidence.map((item, i) => (
                  <li key={i} className="flex gap-2 rounded-md bg-slate-900/60 px-3 py-2 text-xs">
                    <span className="flex-none rounded bg-slate-800 px-1.5 py-0.5 font-mono text-[10px] uppercase text-slate-400">
                      {item.type.replace("_", " ")}
                    </span>
                    <span className="text-slate-300">{item.excerpt}</span>
                  </li>
                ))}
              </ul>
            </div>

            <div>
              <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-slate-500">
                Recommended actions
              </p>
              <ul className="space-y-1.5">
                {suggestion.recommendedActions.map((action, i) => (
                  <li
                    key={i}
                    className="flex items-center justify-between gap-3 rounded-md bg-slate-900/60 px-3 py-2 text-xs"
                  >
                    <span className="text-slate-300">{action.description}</span>
                    <RiskBadge risk={action.risk_level} />
                  </li>
                ))}
              </ul>
            </div>

            {suggestion.status === "PENDING_REVIEW" && (
              <div className="flex justify-end gap-2 border-t border-surface-border pt-3">
                <Button
                  variant="danger"
                  disabled={review.isPending}
                  onClick={() => review.mutate({ suggestionId: suggestion.id, decision: "REJECTED" })}
                >
                  <XCircle className="h-4 w-4" /> Reject
                </Button>
                <Button
                  variant="success"
                  disabled={review.isPending}
                  onClick={() => review.mutate({ suggestionId: suggestion.id, decision: "APPROVED" })}
                >
                  <CheckCircle2 className="h-4 w-4" /> Approve
                </Button>
              </div>
            )}
          </CardBody>
        </Card>
      ))}
    </div>
  );
}
