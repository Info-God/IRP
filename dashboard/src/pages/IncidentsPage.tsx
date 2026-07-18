import { useState } from "react";
import { Plus } from "lucide-react";
import { PageHeader } from "@/components/layout/PageHeader";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Pagination } from "@/components/ui/Pagination";
import { IncidentTable } from "@/features/incidents/IncidentTable";
import { IncidentFilters } from "@/features/incidents/IncidentFilters";
import { CreateIncidentDialog } from "@/features/incidents/CreateIncidentDialog";
import { useProject } from "@/context/ProjectContext";
import { useIncidents } from "@/hooks/useIncidents";
import type { IncidentStatus, Severity } from "@/types";

export default function IncidentsPage() {
  const { currentProject } = useProject();
  const [status, setStatus] = useState<IncidentStatus | "">("");
  const [severity, setSeverity] = useState<Severity | "">("");
  const [page, setPage] = useState(0);
  const [createOpen, setCreateOpen] = useState(false);

  const { data, isLoading } = useIncidents(currentProject?.id, {
    status: status || undefined,
    severity: severity || undefined,
    page,
    size: 10,
  });

  return (
    <div>
      <PageHeader
        title="Incidents"
        description="All incidents detected or reported for this project"
        action={
          <Button variant="primary" onClick={() => setCreateOpen(true)} disabled={!currentProject}>
            <Plus className="h-4 w-4" /> New incident
          </Button>
        }
      />

      <div className="mb-4">
        <IncidentFilters
          status={status}
          severity={severity}
          onStatusChange={(v) => {
            setStatus(v);
            setPage(0);
          }}
          onSeverityChange={(v) => {
            setSeverity(v);
            setPage(0);
          }}
        />
      </div>

      <Card>
        <IncidentTable incidents={data?.items ?? []} isLoading={isLoading} />
        {data && <Pagination page={data.page} totalPages={data.totalPages} onChange={setPage} />}
      </Card>

      {currentProject && (
        <CreateIncidentDialog open={createOpen} onClose={() => setCreateOpen(false)} projectId={currentProject.id} />
      )}
    </div>
  );
}
