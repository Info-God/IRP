import { Bot, Search, Sparkles, Wrench } from "lucide-react";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import { Badge } from "@/components/ui/Badge";
import { Skeleton } from "@/components/ui/Skeleton";
import { EmptyState } from "@/components/ui/EmptyState";
import { useAgentRuns } from "@/hooks/useAgentRuns";
import { formatDateTime } from "@/lib/formatters";
import type { AgentRunStatus, AgentStepResponse } from "@/types";

const statusTone: Record<AgentRunStatus, "amber" | "green" | "red"> = {
  RUNNING: "amber",
  SUCCEEDED: "green",
  FAILED: "red",
};

const toolIcon: Record<string, typeof Search> = {
  search_errors: Search,
  search_logs: Search,
  get_recent_deployments: Search,
  search_runbooks: Search,
  post_investigation_result: Sparkles,
};

function durationLabel(startedAt: string, finishedAt: string | null): string {
  if (!finishedAt) return "in progress";
  const seconds = (new Date(finishedAt).getTime() - new Date(startedAt).getTime()) / 1000;
  return seconds < 1 ? "<1s" : `${seconds.toFixed(1)}s`;
}

function StepRow({ step }: { step: AgentStepResponse }) {
  const Icon = toolIcon[step.toolName] ?? Wrench;
  return (
    <li className="rounded-md border border-surface-border bg-slate-900/40 p-3">
      <div className="flex items-center gap-2">
        <span className="flex h-6 w-6 flex-none items-center justify-center rounded-full bg-indigo-500/15 text-indigo-300">
          <Icon className="h-3.5 w-3.5" />
        </span>
        <span className="font-mono text-xs font-medium text-slate-200">{step.toolName}</span>
        <span className="text-[10px] text-slate-500">step {step.stepIndex + 1}</span>
      </div>
      <div className="mt-2 grid grid-cols-1 gap-2 sm:grid-cols-2">
        <div>
          <p className="mb-1 text-[10px] font-semibold uppercase tracking-wide text-slate-500">Input</p>
          <pre className="max-h-32 overflow-auto rounded bg-slate-950/60 p-2 text-[11px] text-slate-400">
            {JSON.stringify(step.toolInput, null, 2)}
          </pre>
        </div>
        <div>
          <p className="mb-1 text-[10px] font-semibold uppercase tracking-wide text-slate-500">Output</p>
          <pre className="max-h-32 overflow-auto rounded bg-slate-950/60 p-2 text-[11px] text-slate-400">
            {JSON.stringify(step.toolOutput, null, 2)}
          </pre>
        </div>
      </div>
    </li>
  );
}

export function AgentTraceTab({ projectId, incidentId }: { projectId: string; incidentId: string }) {
  const { data: runs = [], isLoading } = useAgentRuns(projectId, incidentId);

  if (isLoading) {
    return (
      <div className="space-y-3">
        <Skeleton className="h-40 w-full" />
      </div>
    );
  }

  if (runs.length === 0) {
    return (
      <EmptyState
        icon={Bot}
        title="No agent runs yet"
        description="Once the AI agent investigates this incident, every tool it calls - and what it found - will be traced here."
      />
    );
  }

  return (
    <div className="space-y-4">
      {runs.map(({ run, steps }) => (
        <Card key={run.id}>
          <CardHeader
            title={
              <span className="flex items-center gap-2">
                <Bot className="h-4 w-4 text-indigo-400" />
                {run.model ?? "Agent run"}
              </span>
            }
            description={`Started ${formatDateTime(run.startedAt)} · ${durationLabel(run.startedAt, run.finishedAt)}${
              run.tokenUsage ? ` · ${run.tokenUsage.total_tokens} tokens` : ""
            }`}
            action={<Badge tone={statusTone[run.status]}>{run.status}</Badge>}
          />
          <CardBody>
            {steps.length === 0 ? (
              <p className="text-sm text-slate-500">No tool calls recorded for this run.</p>
            ) : (
              <ol className="space-y-2">
                {steps.map((step) => (
                  <StepRow key={step.id} step={step} />
                ))}
              </ol>
            )}
          </CardBody>
        </Card>
      ))}
    </div>
  );
}
