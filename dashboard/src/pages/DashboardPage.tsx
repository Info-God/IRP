import { useNavigate } from "react-router-dom";
import { AlertTriangle, Clock, Siren, Sparkles } from "lucide-react";
import { PageHeader } from "@/components/layout/PageHeader";
import { StatCard } from "@/components/ui/StatCard";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import { Skeleton } from "@/components/ui/Skeleton";
import { IncidentTrendChart } from "@/components/charts/IncidentTrendChart";
import { SeverityDonut } from "@/components/charts/SeverityDonut";
import { SeverityBadge } from "@/features/incidents/SeverityBadge";
import { StatusBadge } from "@/features/incidents/StatusBadge";
import { useProject } from "@/context/ProjectContext";
import { useIncidents } from "@/hooks/useIncidents";
import { useAllAgentSuggestions } from "@/hooks/useAgentSuggestions";
import { formatRelativeTime } from "@/lib/formatters";

export default function DashboardPage() {
  const { currentProject } = useProject();
  const navigate = useNavigate();

  const { data: allIncidents, isLoading: incidentsLoading } = useIncidents(currentProject?.id, {
    page: 0,
    size: 100,
  });
  const { data: projectSuggestions = [] } = useAllAgentSuggestions(currentProject?.id);

  const incidents = allIncidents?.items ?? [];
  const openIncidents = incidents.filter((i) => i.status !== "RESOLVED" && i.status !== "CLOSED");
  const awaitingApproval = incidents.filter((i) => i.status === "AWAITING_APPROVAL");
  const approved = projectSuggestions.filter((s) => s.status === "APPROVED");
  const avgApprovedConfidence =
    approved.length > 0 ? approved.reduce((sum, s) => sum + s.confidence, 0) / approved.length : null;
  const pendingSuggestions = projectSuggestions.filter((s) => s.status === "PENDING_REVIEW");

  return (
    <div>
      <PageHeader
        title="Dashboard"
        description={currentProject ? `Overview for ${currentProject.name}` : "Select a project to get started"}
      />

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard label="Open incidents" value={incidentsLoading ? "-" : openIncidents.length} icon={Siren} />
        <StatCard
          label="Awaiting approval"
          value={incidentsLoading ? "-" : awaitingApproval.length}
          icon={Sparkles}
          tone="warning"
        />
        <StatCard
          label="Avg. approved confidence"
          value={avgApprovedConfidence !== null ? `${Math.round(avgApprovedConfidence * 100)}%` : "-"}
          icon={Clock}
          tone="success"
        />
        <StatCard
          label="Critical open"
          value={incidentsLoading ? "-" : openIncidents.filter((i) => i.severity === "CRITICAL").length}
          icon={AlertTriangle}
          tone="danger"
        />
      </div>

      <div className="mt-4 grid grid-cols-1 gap-4 lg:grid-cols-3">
        <Card className="lg:col-span-2">
          <CardHeader title="Incidents opened (last 14 days)" />
          <CardBody>
            {incidentsLoading ? <Skeleton className="h-[220px] w-full" /> : <IncidentTrendChart incidents={incidents} />}
          </CardBody>
        </Card>
        <Card>
          <CardHeader title="Open incidents by severity" />
          <CardBody>
            {incidentsLoading ? (
              <Skeleton className="h-[220px] w-full" />
            ) : (
              <SeverityDonut incidents={openIncidents} />
            )}
          </CardBody>
        </Card>
      </div>

      <div className="mt-4 grid grid-cols-1 gap-4 lg:grid-cols-2">
        <Card>
          <CardHeader title="Recent incidents" action={<a href="/incidents" className="text-xs text-indigo-400 hover:underline">View all</a>} />
          <div className="divide-y divide-surface-border">
            {incidents.slice(0, 5).map((incident) => (
              <button
                key={incident.id}
                onClick={() => navigate(`/incidents/${incident.id}`)}
                className="flex w-full items-center justify-between gap-3 px-5 py-3 text-left hover:bg-slate-800/40"
              >
                <div className="min-w-0">
                  <p className="truncate text-sm font-medium text-slate-100">{incident.title}</p>
                  <p className="mt-0.5 text-xs text-slate-500">{formatRelativeTime(incident.openedAt)}</p>
                </div>
                <div className="flex flex-none items-center gap-2">
                  <SeverityBadge severity={incident.severity} />
                  <StatusBadge status={incident.status} />
                </div>
              </button>
            ))}
            {incidents.length === 0 && !incidentsLoading && (
              <p className="px-5 py-6 text-sm text-slate-500">No incidents for this project yet.</p>
            )}
          </div>
        </Card>

        <Card>
          <CardHeader
            title="Recent AI activity"
            description="Suggestions awaiting your review"
            action={<a href="/copilot" className="text-xs text-indigo-400 hover:underline">Open AI Copilot</a>}
          />
          <div className="divide-y divide-surface-border">
            {pendingSuggestions.slice(0, 5).map((s) => (
              <button
                key={s.id}
                onClick={() => navigate(`/incidents/${s.incidentId}`)}
                className="flex w-full items-start gap-3 px-5 py-3 text-left hover:bg-slate-800/40"
              >
                <Sparkles className="mt-0.5 h-4 w-4 flex-none text-indigo-400" />
                <div className="min-w-0">
                  <p className="line-clamp-2 text-sm text-slate-200">{s.rootCause}</p>
                  <p className="mt-1 text-xs text-slate-500">{Math.round(s.confidence * 100)}% confidence</p>
                </div>
              </button>
            ))}
            {pendingSuggestions.length === 0 && (
              <p className="px-5 py-6 text-sm text-slate-500">Nothing pending review right now.</p>
            )}
          </div>
        </Card>
      </div>
    </div>
  );
}
