import { useState } from "react";
import { ScrollText } from "lucide-react";
import { PageHeader } from "@/components/layout/PageHeader";
import { Card } from "@/components/ui/Card";
import { Pagination } from "@/components/ui/Pagination";
import { EmptyState } from "@/components/ui/EmptyState";
import { Skeleton } from "@/components/ui/Skeleton";
import { useProject } from "@/context/ProjectContext";
import { useAuditLogs } from "@/hooks/useAuditLogs";
import { formatDateTime } from "@/lib/formatters";

export default function AuditLogsPage() {
  const { currentProject } = useProject();
  const [page, setPage] = useState(0);
  const { data, isLoading } = useAuditLogs(currentProject?.id ?? null, page);

  return (
    <div>
      <PageHeader
        title="Audit Logs"
        description="Append-only record of every mutating action in this project - never edited or deleted"
      />

      <Card>
        {isLoading ? (
          <div className="space-y-3 p-5">
            {Array.from({ length: 6 }).map((_, i) => (
              <Skeleton key={i} className="h-6 w-full" />
            ))}
          </div>
        ) : (data?.items.length ?? 0) === 0 ? (
          <EmptyState icon={ScrollText} title="No audit log entries yet" />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead>
                <tr className="border-b border-surface-border text-xs uppercase tracking-wide text-slate-500">
                  <th className="px-5 py-3 font-medium">Actor</th>
                  <th className="px-3 py-3 font-medium">Action</th>
                  <th className="px-3 py-3 font-medium">Entity</th>
                  <th className="px-3 py-3 font-medium">Metadata</th>
                  <th className="px-5 py-3 text-right font-medium">When</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-surface-border">
                {data?.items.map((log) => (
                  <tr key={log.id} className="hover:bg-slate-800/30">
                    <td className="px-5 py-3 font-medium text-slate-200">{log.actor}</td>
                    <td className="px-3 py-3">
                      <span className="rounded bg-slate-800 px-2 py-0.5 font-mono text-xs text-slate-300">
                        {log.action}
                      </span>
                    </td>
                    <td className="px-3 py-3 text-xs text-slate-400">
                      {log.entityType} <span className="text-slate-600">#{log.entityId.slice(0, 8)}</span>
                    </td>
                    <td className="px-3 py-3 font-mono text-xs text-slate-500">
                      {Object.entries(log.metadata)
                        .map(([k, v]) => `${k}=${v}`)
                        .join(", ") || "-"}
                    </td>
                    <td className="px-5 py-3 text-right text-xs text-slate-400">{formatDateTime(log.createdAt)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        {data && <Pagination page={data.page} totalPages={data.totalPages} onChange={setPage} />}
      </Card>
    </div>
  );
}
