import { useState } from "react";
import { BookOpen, FileText, Plus } from "lucide-react";
import { PageHeader } from "@/components/layout/PageHeader";
import { Card, CardBody } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Skeleton } from "@/components/ui/Skeleton";
import { EmptyState } from "@/components/ui/EmptyState";
import { UploadRunbookDialog } from "@/features/runbooks/UploadRunbookDialog";
import { useRunbooks } from "@/hooks/useMisc";
import { useProject } from "@/context/ProjectContext";
import { formatDateTime } from "@/lib/formatters";

export default function RunbooksPage() {
  const { currentProject } = useProject();
  const { data: runbooks = [], isLoading } = useRunbooks(currentProject?.id);
  const [uploadOpen, setUploadOpen] = useState(false);

  return (
    <div>
      <PageHeader
        title="Runbooks / Knowledge Base"
        description="Documents the AI agent searches for remediation guidance during an investigation"
        action={
          <Button variant="primary" onClick={() => setUploadOpen(true)} disabled={!currentProject}>
            <Plus className="h-4 w-4" /> Upload runbook
          </Button>
        }
      />

      {isLoading ? (
        <Skeleton className="h-64 w-full" />
      ) : runbooks.length === 0 ? (
        <EmptyState icon={BookOpen} title="No runbooks yet" description="Upload one to give the AI agent remediation guidance to search during an investigation." />
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

      {currentProject && (
        <UploadRunbookDialog open={uploadOpen} onClose={() => setUploadOpen(false)} projectId={currentProject.id} />
      )}
    </div>
  );
}
