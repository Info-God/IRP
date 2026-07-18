import { AlertTriangle } from "lucide-react";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import { Skeleton } from "@/components/ui/Skeleton";
import { useRelatedSignals } from "@/hooks/useMisc";
import { formatDateTime } from "@/lib/formatters";

export function RelatedSignalsTab({ service }: { service: string | null }) {
  const { data, isLoading } = useRelatedSignals(service);

  return (
    <div className="space-y-4">
      <div className="flex items-start gap-2 rounded-md border border-amber-500/30 bg-amber-500/10 px-3 py-2 text-xs text-amber-200">
        <AlertTriangle className="mt-0.5 h-3.5 w-3.5 flex-none" />
        <span>
          Mock data. irp-core has no dashboard-facing read endpoint for logs/errors/deployments yet
          (only the AI agent's API-key-authenticated endpoints exist) - see the Phase 4 design doc gap
          list.
        </span>
      </div>

      {isLoading ? (
        <Skeleton className="h-48 w-full" />
      ) : (
        <>
          <Card>
            <CardHeader title="Recent errors" />
            <CardBody className="space-y-2">
              {data?.errors.length === 0 && <p className="text-sm text-slate-500">No errors in range.</p>}
              {data?.errors.map((error) => (
                <div key={error.id} className="rounded-md bg-slate-900/60 px-3 py-2 text-xs">
                  <div className="flex items-center justify-between">
                    <span className="font-mono text-red-300">{error.exceptionType}</span>
                    <span className="text-slate-500">{formatDateTime(error.occurredAt)}</span>
                  </div>
                  <p className="mt-1 text-slate-300">{error.message}</p>
                </div>
              ))}
            </CardBody>
          </Card>

          <Card>
            <CardHeader title="Recent logs" />
            <CardBody className="space-y-2">
              {data?.logs.map((log) => (
                <div key={log.id} className="flex items-center gap-3 rounded-md bg-slate-900/60 px-3 py-2 text-xs">
                  <span className="w-14 flex-none font-mono text-slate-500">{log.level}</span>
                  <span className="flex-1 text-slate-300">{log.message}</span>
                  <span className="flex-none text-slate-500">{formatDateTime(log.occurredAt)}</span>
                </div>
              ))}
            </CardBody>
          </Card>

          <Card>
            <CardHeader title="Recent deployments" />
            <CardBody className="space-y-2">
              {data?.deployments.length === 0 && (
                <p className="text-sm text-slate-500">No deployments in range.</p>
              )}
              {data?.deployments.map((dep) => (
                <div
                  key={dep.id}
                  className="flex items-center justify-between rounded-md bg-slate-900/60 px-3 py-2 text-xs"
                >
                  <span className="font-mono text-slate-300">
                    {dep.service} v{dep.version}
                  </span>
                  <span className="text-slate-500">{formatDateTime(dep.occurredAt)}</span>
                </div>
              ))}
            </CardBody>
          </Card>
        </>
      )}
    </div>
  );
}
