import { AlertTriangle, Plus, Zap } from "lucide-react";
import { PageHeader } from "@/components/layout/PageHeader";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Badge } from "@/components/ui/Badge";
import { Skeleton } from "@/components/ui/Skeleton";
import { EmptyState } from "@/components/ui/EmptyState";
import { useAutomations } from "@/hooks/useMisc";
import { formatRelativeTime } from "@/lib/formatters";

export default function AutomationsPage() {
  const { data: rules = [], isLoading } = useAutomations();

  return (
    <div>
      <PageHeader
        title="Automations"
        description="Rules that react automatically to incidents and AI suggestions"
        action={
          <Button variant="primary" disabled title="Automations have no backend/domain model yet">
            <Plus className="h-4 w-4" /> New rule
          </Button>
        }
      />

      <div className="mb-4 flex items-start gap-2 rounded-md border border-amber-500/30 bg-amber-500/10 px-3 py-2 text-xs text-amber-200">
        <AlertTriangle className="mt-0.5 h-3.5 w-3.5 flex-none" />
        <span>
          Mock data. No automations concept exists in irp-core at all yet - this page shows the
          intended UX for a future phase (e.g. auto-notify Slack on critical incidents, auto-approve
          high-confidence/low-risk suggestions).
        </span>
      </div>

      {isLoading ? (
        <Skeleton className="h-64 w-full" />
      ) : rules.length === 0 ? (
        <EmptyState icon={Zap} title="No automation rules yet" />
      ) : (
        <Card>
          <div className="divide-y divide-surface-border">
            {rules.map((rule) => (
              <div key={rule.id} className="flex items-center justify-between gap-4 px-5 py-4">
                <div className="min-w-0">
                  <div className="flex items-center gap-2">
                    <p className="truncate text-sm font-medium text-slate-100">{rule.name}</p>
                    <Badge tone={rule.enabled ? "green" : "slate"}>{rule.enabled ? "Enabled" : "Disabled"}</Badge>
                  </div>
                  <p className="mt-1 text-xs text-slate-500">
                    When <span className="font-mono text-slate-400">{rule.condition}</span> → {rule.action}
                  </p>
                </div>
                <p className="flex-none text-xs text-slate-500">
                  {rule.lastTriggeredAt ? `Last triggered ${formatRelativeTime(rule.lastTriggeredAt)}` : "Never triggered"}
                </p>
              </div>
            ))}
          </div>
        </Card>
      )}
    </div>
  );
}
