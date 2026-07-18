import type { IncidentStatus, Severity } from "@/types";

const statuses: IncidentStatus[] = ["OPEN", "INVESTIGATING", "AWAITING_APPROVAL", "RESOLVED", "CLOSED"];
const severities: Severity[] = ["CRITICAL", "HIGH", "MEDIUM", "LOW"];

const selectClass =
  "rounded-md border border-surface-border bg-surface px-3 py-2 text-sm text-slate-200 focus:border-indigo-400 focus:outline-none";

export function IncidentFilters({
  status,
  severity,
  onStatusChange,
  onSeverityChange,
}: {
  status: IncidentStatus | "";
  severity: Severity | "";
  onStatusChange: (value: IncidentStatus | "") => void;
  onSeverityChange: (value: Severity | "") => void;
}) {
  return (
    <div className="flex items-center gap-2">
      <select
        value={status}
        onChange={(e) => onStatusChange(e.target.value as IncidentStatus | "")}
        className={selectClass}
      >
        <option value="">All statuses</option>
        {statuses.map((s) => (
          <option key={s} value={s}>
            {s.replace("_", " ")}
          </option>
        ))}
      </select>
      <select
        value={severity}
        onChange={(e) => onSeverityChange(e.target.value as Severity | "")}
        className={selectClass}
      >
        <option value="">All severities</option>
        {severities.map((s) => (
          <option key={s} value={s}>
            {s}
          </option>
        ))}
      </select>
    </div>
  );
}
