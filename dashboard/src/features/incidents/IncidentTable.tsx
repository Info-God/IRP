import { useNavigate } from "react-router-dom";
import { Siren } from "lucide-react";
import { SeverityBadge } from "@/features/incidents/SeverityBadge";
import { StatusBadge } from "@/features/incidents/StatusBadge";
import { EmptyState } from "@/components/ui/EmptyState";
import { Skeleton } from "@/components/ui/Skeleton";
import { formatRelativeTime } from "@/lib/formatters";
import type { IncidentResponse } from "@/types";

export function IncidentTable({
  incidents,
  isLoading,
}: {
  incidents: IncidentResponse[];
  isLoading: boolean;
}) {
  const navigate = useNavigate();

  if (isLoading) {
    return (
      <div className="divide-y divide-surface-border">
        {Array.from({ length: 6 }).map((_, i) => (
          <div key={i} className="flex items-center gap-4 px-5 py-3.5">
            <Skeleton className="h-4 w-1/3" />
            <Skeleton className="h-5 w-16" />
            <Skeleton className="h-5 w-24" />
            <Skeleton className="h-4 w-20" />
          </div>
        ))}
      </div>
    );
  }

  if (incidents.length === 0) {
    return (
      <EmptyState
        icon={Siren}
        title="No incidents match these filters"
        description="Try clearing the status or severity filter, or create a new incident."
      />
    );
  }

  return (
    <div className="overflow-x-auto">
      <table className="w-full text-left text-sm">
        <thead>
          <tr className="border-b border-surface-border text-xs uppercase tracking-wide text-slate-500">
            <th className="px-5 py-3 font-medium">Title</th>
            <th className="px-3 py-3 font-medium">Severity</th>
            <th className="px-3 py-3 font-medium">Status</th>
            <th className="px-3 py-3 font-medium">Service</th>
            <th className="px-5 py-3 text-right font-medium">Opened</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-surface-border">
          {incidents.map((incident) => (
            <tr
              key={incident.id}
              onClick={() => navigate(`/incidents/${incident.id}`)}
              className="cursor-pointer transition-colors hover:bg-slate-800/40"
            >
              <td className="max-w-md truncate px-5 py-3.5 font-medium text-slate-100">{incident.title}</td>
              <td className="px-3 py-3.5">
                <SeverityBadge severity={incident.severity} />
              </td>
              <td className="px-3 py-3.5">
                <StatusBadge status={incident.status} />
              </td>
              <td className="px-3 py-3.5 font-mono text-xs text-slate-400">{incident.service ?? "-"}</td>
              <td className="px-5 py-3.5 text-right text-xs text-slate-400">
                {formatRelativeTime(incident.openedAt)}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
