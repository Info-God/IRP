import { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { ArrowLeft } from "lucide-react";
import { Card, CardBody } from "@/components/ui/Card";
import { Skeleton } from "@/components/ui/Skeleton";
import { Tabs } from "@/components/ui/Tabs";
import { SeverityBadge } from "@/features/incidents/SeverityBadge";
import { StatusBadge } from "@/features/incidents/StatusBadge";
import { IncidentTimeline } from "@/features/incident-detail/IncidentTimeline";
import { AgentSuggestionPanel } from "@/features/incident-detail/AgentSuggestionPanel";
import { RelatedSignalsTab } from "@/features/incident-detail/RelatedSignalsTab";
import { useProject } from "@/context/ProjectContext";
import { useIncident, useUpdateIncidentStatus } from "@/hooks/useIncidents";
import { formatDateTime } from "@/lib/formatters";
import type { IncidentStatus } from "@/types";

const statusOptions: IncidentStatus[] = ["OPEN", "INVESTIGATING", "AWAITING_APPROVAL", "RESOLVED", "CLOSED"];

export default function IncidentDetailPage() {
  const { incidentId } = useParams<{ incidentId: string }>();
  const { currentProject } = useProject();
  const navigate = useNavigate();
  const [tab, setTab] = useState("overview");

  const { data, isLoading } = useIncident(currentProject?.id, incidentId);
  const updateStatus = useUpdateIncidentStatus(currentProject?.id, incidentId);

  if (isLoading || !data) {
    return (
      <div className="space-y-4">
        <Skeleton className="h-8 w-64" />
        <Skeleton className="h-40 w-full" />
      </div>
    );
  }

  const { incident, timeline } = data;

  return (
    <div>
      <button
        onClick={() => navigate("/incidents")}
        className="mb-4 flex items-center gap-1.5 text-xs text-slate-400 hover:text-slate-200"
      >
        <ArrowLeft className="h-3.5 w-3.5" /> Back to incidents
      </button>

      <div className="mb-6 flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
        <div className="min-w-0">
          <div className="flex flex-wrap items-center gap-2">
            <SeverityBadge severity={incident.severity} />
            <StatusBadge status={incident.status} />
            {incident.service && (
              <span className="rounded bg-slate-800 px-2 py-0.5 font-mono text-xs text-slate-400">
                {incident.service}
              </span>
            )}
          </div>
          <h2 className="mt-2 text-xl font-semibold text-slate-50">{incident.title}</h2>
          <p className="mt-1 text-xs text-slate-500">
            Opened {formatDateTime(incident.openedAt)}
            {incident.resolvedAt && ` · Resolved ${formatDateTime(incident.resolvedAt)}`}
          </p>
        </div>

        <div className="flex-none">
          <label className="mb-1 block text-xs font-medium text-slate-400">Status</label>
          <select
            value={incident.status}
            disabled={updateStatus.isPending}
            onChange={(e) => updateStatus.mutate({ status: e.target.value as IncidentStatus })}
            className="rounded-md border border-surface-border bg-surface px-3 py-2 text-sm text-slate-200 focus:border-indigo-400 focus:outline-none"
          >
            {statusOptions.map((s) => (
              <option key={s} value={s}>
                {s.replace("_", " ")}
              </option>
            ))}
          </select>
        </div>
      </div>

      <Card className="mb-4">
        <Tabs
          tabs={[
            { key: "overview", label: "Overview" },
            { key: "timeline", label: "Timeline", count: timeline.length },
            { key: "ai", label: "AI Suggestions" },
            { key: "signals", label: "Related Signals" },
          ]}
          active={tab}
          onChange={setTab}
        />
        <CardBody>
          {tab === "overview" && (
            <p className="text-sm leading-relaxed text-slate-300">
              {incident.description ?? "No description provided."}
            </p>
          )}
          {tab === "timeline" && <IncidentTimeline entries={timeline} />}
          {tab === "ai" && currentProject && (
            <AgentSuggestionPanel projectId={currentProject.id} incidentId={incident.id} />
          )}
          {tab === "signals" && <RelatedSignalsTab service={incident.service} />}
        </CardBody>
      </Card>
    </div>
  );
}
