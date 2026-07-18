import { Badge } from "@/components/ui/Badge";
import type { IncidentStatus } from "@/types";

const statusTone = {
  OPEN: "blue",
  INVESTIGATING: "violet",
  AWAITING_APPROVAL: "amber",
  RESOLVED: "green",
  CLOSED: "slate",
} as const;

const statusLabel: Record<IncidentStatus, string> = {
  OPEN: "Open",
  INVESTIGATING: "Investigating",
  AWAITING_APPROVAL: "Awaiting approval",
  RESOLVED: "Resolved",
  CLOSED: "Closed",
};

export function StatusBadge({ status }: { status: IncidentStatus }) {
  return <Badge tone={statusTone[status]}>{statusLabel[status]}</Badge>;
}
