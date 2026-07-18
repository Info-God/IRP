import { AlertTriangle, BookOpen, FileText, Plus } from "lucide-react";
import { PageHeader } from "@/components/layout/PageHeader";
import { Card, CardBody } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Skeleton } from "@/components/ui/Skeleton";
import { EmptyState } from "@/components/ui/EmptyState";
import { useRunbooks } from "@/hooks/useMisc";
import { formatDateTime } from "@/lib/formatters";

export default function RunbooksPage() {
  const { data: runbooks = [], isLoading } = useRunbooks();

  return (
    <div>
      <PageHeader
        title="Runbooks / Knowledge Base"
        description="Documents the AI agent searches for remediation guidance during an investigation"
        action={
          <Button variant="primary" disabled title="Upload requires the runbook ingestion backend (Phase 3 follow-up) - not built yet">
            <Plus className="h-4 w-4" /> Upload runbook
          </Button>
        }
      />

      <div className="mb-4 flex items-start gap-2 rounded-md border border-amber-500/30 bg-amber-500/10 px-3 py-2 text-xs text-amber-200">
        <AlertTriangle className="mt-0.5 h-3.5 w-3.5 flex-none" />
        <span>
          Mock data. No upload/list endpoint exists in irp-core yet - the <code>runbooks</code> /{" "}
          <code>runbook_chunks</code> tables (pgvector-backed) were laid down in the Phase 3 migration
          but nothing writes to them yet. This page shows the intended UX.
        </span>
      </div>

      {isLoading ? (
        <Skeleton className="h-64 w-full" />
      ) : runbooks.length === 0 ? (
        <EmptyState icon={BookOpen} title="No runbooks yet" />
      ) : (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {runbooks.map((runbook) => (
            <Card key={runbook.id}>
              <CardBody>
                <div className="flex items-start gap-3">
                  <div className="rounded-md bg-indigo-500/15 p-2 text-indigo-300">
                    <FileText className="h-4 w-4" />
                  </div>
                  <div className="min-w-0">
                    <p className="truncate text-sm font-medium text-slate-100">{runbook.title}</p>
                    <p className="mt-0.5 text-xs text-slate-500">v{runbook.version} · {runbook.chunkCount} chunks</p>
                  </div>
                </div>
                <p className="mt-3 text-xs text-slate-500">
                  Uploaded by {runbook.uploadedBy} · {formatDateTime(runbook.createdAt)}
                </p>
              </CardBody>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
