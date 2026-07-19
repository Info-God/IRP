import { useNavigate } from "react-router-dom";
import { CheckCircle2, Sparkles, XCircle } from "lucide-react";
import { PageHeader } from "@/components/layout/PageHeader";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import { StatCard } from "@/components/ui/StatCard";
import { Button } from "@/components/ui/Button";
import { EmptyState } from "@/components/ui/EmptyState";
import { Skeleton } from "@/components/ui/Skeleton";
import { ConfidenceMeter } from "@/components/ui/ConfidenceMeter";
import { ConfidenceHistogram } from "@/components/charts/ConfidenceHistogram";
import { useProject } from "@/context/ProjectContext";
import { useAllAgentSuggestions, useReviewSuggestionAny } from "@/hooks/useAgentSuggestions";

export default function AiCopilotPage() {
  const { currentProject } = useProject();
  const navigate = useNavigate();
  const { data: suggestions = [], isLoading } = useAllAgentSuggestions(currentProject?.id);
  const review = useReviewSuggestionAny();

  const pending = suggestions.filter((s) => s.status === "PENDING_REVIEW");
  const reviewed = suggestions.filter((s) => s.status !== "PENDING_REVIEW");
  const approvedCount = reviewed.filter((s) => s.status === "APPROVED").length;
  const approvalRate = reviewed.length > 0 ? Math.round((approvedCount / reviewed.length) * 100) : null;

  return (
    <div>
      <PageHeader
        title="AI Copilot"
        description="Every root-cause suggestion the agent has proposed for this project, across all incidents"
      />

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        <StatCard label="Pending review" value={pending.length} icon={Sparkles} tone="warning" />
        <StatCard label="Reviewed" value={reviewed.length} icon={CheckCircle2} />
        <StatCard label="Approval rate" value={approvalRate !== null ? `${approvalRate}%` : "-"} icon={CheckCircle2} tone="success" />
      </div>

      <Card className="mt-4">
        <CardHeader title="Confidence distribution" description="Across all suggestions for this project" />
        <CardBody>
          {isLoading ? <Skeleton className="h-[200px] w-full" /> : <ConfidenceHistogram suggestions={suggestions} />}
        </CardBody>
      </Card>

      <Card className="mt-4">
        <CardHeader title="Pending review" description="Approve or reject directly from the queue" />
        {isLoading ? (
          <CardBody>
            <Skeleton className="h-24 w-full" />
          </CardBody>
        ) : pending.length === 0 ? (
          <EmptyState icon={Sparkles} title="Nothing pending review" description="New suggestions will show up here as the AI agent investigates incidents." />
        ) : (
          <div className="divide-y divide-surface-border">
            {pending.map((suggestion) => (
              <div key={suggestion.id} className="px-5 py-4">
                <div className="flex items-start justify-between gap-4">
                  <button
                    onClick={() => navigate(`/incidents/${suggestion.incidentId}`)}
                    className="min-w-0 flex-1 text-left"
                  >
                    <p className="text-sm text-slate-100">{suggestion.rootCause}</p>
                    <p className="mt-1 text-xs text-slate-500">
                      Incident #{suggestion.incidentId} · {suggestion.evidence.length} evidence item(s)
                    </p>
                  </button>
                  <div className="w-40 flex-none">
                    <ConfidenceMeter confidence={suggestion.confidence} />
                  </div>
                </div>
                <div className="mt-3 flex justify-end gap-2">
                  <Button
                    variant="danger"
                    size="sm"
                    disabled={review.isPending}
                    onClick={() =>
                      review.mutate({
                        projectId: suggestion.projectId,
                        incidentId: suggestion.incidentId,
                        suggestionId: suggestion.id,
                        decision: "REJECTED",
                      })
                    }
                  >
                    <XCircle className="h-3.5 w-3.5" /> Reject
                  </Button>
                  <Button
                    variant="success"
                    size="sm"
                    disabled={review.isPending}
                    onClick={() =>
                      review.mutate({
                        projectId: suggestion.projectId,
                        incidentId: suggestion.incidentId,
                        suggestionId: suggestion.id,
                        decision: "APPROVED",
                      })
                    }
                  >
                    <CheckCircle2 className="h-3.5 w-3.5" /> Approve
                  </Button>
                </div>
              </div>
            ))}
          </div>
        )}
      </Card>
    </div>
  );
}
