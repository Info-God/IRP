import { useState } from "react";
import { Modal } from "@/components/ui/Modal";
import { Button } from "@/components/ui/Button";
import { useCreateIncident } from "@/hooks/useIncidents";
import type { Severity } from "@/types";

const inputClass =
  "w-full rounded-md border border-surface-border bg-surface px-3 py-2 text-sm text-slate-100 placeholder:text-slate-500 focus:border-indigo-400 focus:outline-none";

export function CreateIncidentDialog({
  open,
  onClose,
  projectId,
}: {
  open: boolean;
  onClose: () => void;
  projectId: string;
}) {
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [severity, setSeverity] = useState<Severity>("MEDIUM");
  const [service, setService] = useState("");

  const createIncident = useCreateIncident(projectId);

  const reset = () => {
    setTitle("");
    setDescription("");
    setSeverity("MEDIUM");
    setService("");
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim()) return;
    await createIncident.mutateAsync({
      title: title.trim(),
      description: description.trim() || undefined,
      severity,
      service: service.trim() || undefined,
    });
    reset();
    onClose();
  };

  return (
    <Modal open={open} onClose={onClose} title="Create incident">
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="mb-1 block text-xs font-medium text-slate-400">Title</label>
          <input
            className={inputClass}
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="e.g. Checkout throwing 500s after deploy"
            required
          />
        </div>
        <div>
          <label className="mb-1 block text-xs font-medium text-slate-400">Description</label>
          <textarea
            className={inputClass}
            rows={3}
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="Optional context for the on-call engineer"
          />
        </div>
        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="mb-1 block text-xs font-medium text-slate-400">Severity</label>
            <select
              className={inputClass}
              value={severity}
              onChange={(e) => setSeverity(e.target.value as Severity)}
            >
              <option value="LOW">Low</option>
              <option value="MEDIUM">Medium</option>
              <option value="HIGH">High</option>
              <option value="CRITICAL">Critical</option>
            </select>
          </div>
          <div>
            <label className="mb-1 block text-xs font-medium text-slate-400">Service</label>
            <input
              className={inputClass}
              value={service}
              onChange={(e) => setService(e.target.value)}
              placeholder="e.g. checkout"
            />
          </div>
        </div>
        <div className="flex justify-end gap-2 pt-2">
          <Button type="button" variant="ghost" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit" variant="primary" disabled={createIncident.isPending}>
            {createIncident.isPending ? "Creating..." : "Create incident"}
          </Button>
        </div>
      </form>
    </Modal>
  );
}
