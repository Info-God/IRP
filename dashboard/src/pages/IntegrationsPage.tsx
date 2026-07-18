import { AlertTriangle, Github, MessageSquare, Radio, Webhook } from "lucide-react";
import { PageHeader } from "@/components/layout/PageHeader";
import { Card, CardBody } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Badge } from "@/components/ui/Badge";
import { Skeleton } from "@/components/ui/Skeleton";
import { useIntegrations } from "@/hooks/useMisc";
import type { IntegrationView } from "@/types";

const iconByType: Record<IntegrationView["icon"], typeof Github> = {
  slack: MessageSquare,
  pagerduty: Radio,
  github: Github,
  webhook: Webhook,
};

export default function IntegrationsPage() {
  const { data: integrations = [], isLoading } = useIntegrations();

  return (
    <div>
      <PageHeader title="Integrations" description="Connect the platform to the tools your team already uses" />

      <div className="mb-4 flex items-start gap-2 rounded-md border border-amber-500/30 bg-amber-500/10 px-3 py-2 text-xs text-amber-200">
        <AlertTriangle className="mt-0.5 h-3.5 w-3.5 flex-none" />
        <span>
          Mock data. No OAuth/webhook configuration storage exists in irp-core yet - this catalog shows
          the intended UX for a future phase.
        </span>
      </div>

      {isLoading ? (
        <Skeleton className="h-48 w-full" />
      ) : (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {integrations.map((integration) => {
            const Icon = iconByType[integration.icon];
            return (
              <Card key={integration.id}>
                <CardBody>
                  <div className="flex items-center justify-between">
                    <div className="rounded-md bg-slate-800 p-2 text-slate-300">
                      <Icon className="h-4 w-4" />
                    </div>
                    <Badge tone={integration.connected ? "green" : "slate"}>
                      {integration.connected ? "Connected" : "Not connected"}
                    </Badge>
                  </div>
                  <p className="mt-3 text-sm font-medium text-slate-100">{integration.name}</p>
                  <p className="mt-1 text-xs text-slate-500">{integration.description}</p>
                  <Button variant="secondary" size="sm" className="mt-3 w-full" disabled title="Coming soon">
                    {integration.connected ? "Manage" : "Connect"}
                  </Button>
                </CardBody>
              </Card>
            );
          })}
        </div>
      )}
    </div>
  );
}
