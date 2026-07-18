import { Badge } from "@/components/ui/Badge";
import type { RiskLevel } from "@/types";

const riskTone = {
  LOW: "green",
  MEDIUM: "amber",
  HIGH: "red",
} as const;

export function RiskBadge({ risk }: { risk: RiskLevel }) {
  return <Badge tone={riskTone[risk]}>{risk} risk</Badge>;
}
