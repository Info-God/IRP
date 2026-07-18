import { useState } from "react";
import { Copy, KeyRound, Plus, Trash2 } from "lucide-react";
import { PageHeader } from "@/components/layout/PageHeader";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Badge } from "@/components/ui/Badge";
import { Tabs } from "@/components/ui/Tabs";
import { Skeleton } from "@/components/ui/Skeleton";
import { Modal } from "@/components/ui/Modal";
import { useAuth } from "@/context/AuthContext";
import { useProject } from "@/context/ProjectContext";
import { useOrganization } from "@/hooks/useMisc";
import { useApiKeys, useCreateApiKey, useRevokeApiKey } from "@/hooks/useApiKeys";
import { formatDateTime, formatRelativeTime, initials } from "@/lib/formatters";

const inputClass =
  "w-full rounded-md border border-surface-border bg-surface px-3 py-2 text-sm text-slate-100 placeholder:text-slate-500 focus:border-indigo-400 focus:outline-none";

export default function SettingsPage() {
  const [tab, setTab] = useState("organization");

  return (
    <div>
      <PageHeader title="Settings" description="Organization, projects, API keys, and your profile" />
      <Card>
        <Tabs
          tabs={[
            { key: "organization", label: "Organization" },
            { key: "projects", label: "Projects" },
            { key: "api-keys", label: "API Keys" },
            { key: "profile", label: "Profile" },
          ]}
          active={tab}
          onChange={setTab}
        />
        <CardBody>
          {tab === "organization" && <OrganizationTab />}
          {tab === "projects" && <ProjectsTab />}
          {tab === "api-keys" && <ApiKeysTab />}
          {tab === "profile" && <ProfileTab />}
        </CardBody>
      </Card>
    </div>
  );
}

function OrganizationTab() {
  const { data: org, isLoading } = useOrganization();

  if (isLoading || !org) return <Skeleton className="h-24 w-full" />;

  return (
    <div className="max-w-md space-y-4">
      <p className="text-xs text-amber-300">
        Read-only for now - irp-core has no organization update endpoint yet.
      </p>
      <Field label="Name" value={org.name} />
      <Field label="Slug" value={org.slug} />
      <Field label="Created" value={formatDateTime(org.createdAt)} />
    </div>
  );
}

function ProjectsTab() {
  const { projects, isLoading } = useProject();

  if (isLoading) return <Skeleton className="h-24 w-full" />;

  return (
    <div className="space-y-3">
      <p className="text-xs text-amber-300">
        Project creation exists on the backend; update/delete endpoints do not yet.
      </p>
      <div className="divide-y divide-surface-border rounded-md border border-surface-border">
        {projects.map((project) => (
          <div key={project.id} className="flex items-center justify-between px-4 py-3">
            <div>
              <p className="text-sm font-medium text-slate-100">{project.name}</p>
              <p className="text-xs text-slate-500">Created {formatDateTime(project.createdAt)}</p>
            </div>
            <Badge tone={project.environment === "production" ? "green" : "slate"}>{project.environment}</Badge>
          </div>
        ))}
      </div>
    </div>
  );
}

function ApiKeysTab() {
  const { currentProject } = useProject();
  const { data: keys = [], isLoading } = useApiKeys(currentProject?.id);
  const createKey = useCreateApiKey(currentProject?.id);
  const revokeKey = useRevokeApiKey(currentProject?.id);

  const [name, setName] = useState("");
  const [plaintextKey, setPlaintextKey] = useState<string | null>(null);

  const handleCreate = async () => {
    if (!name.trim()) return;
    const created = await createKey.mutateAsync(name.trim());
    setPlaintextKey(created.plaintextKey);
    setName("");
  };

  return (
    <div>
      <div className="mb-4 flex items-end gap-2">
        <div className="flex-1">
          <label className="mb-1 block text-xs font-medium text-slate-400">New key name</label>
          <input
            className={inputClass}
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="e.g. ai-agent-key"
          />
        </div>
        <Button variant="primary" onClick={handleCreate} disabled={createKey.isPending || !currentProject}>
          <Plus className="h-4 w-4" /> Create key
        </Button>
      </div>

      {isLoading ? (
        <Skeleton className="h-24 w-full" />
      ) : (
        <div className="divide-y divide-surface-border rounded-md border border-surface-border">
          {keys.map((key) => (
            <div key={key.id} className="flex items-center justify-between px-4 py-3">
              <div className="flex items-center gap-3">
                <div className="rounded-md bg-slate-800 p-2 text-slate-300">
                  <KeyRound className="h-3.5 w-3.5" />
                </div>
                <div>
                  <p className="text-sm font-medium text-slate-100">{key.name}</p>
                  <p className="font-mono text-xs text-slate-500">
                    {key.keyPrefix}••••••••
                    {key.lastUsedAt && ` · last used ${formatRelativeTime(key.lastUsedAt)}`}
                  </p>
                </div>
              </div>
              {key.revokedAt ? (
                <Badge tone="red">Revoked</Badge>
              ) : (
                <Button
                  variant="danger"
                  size="sm"
                  disabled={revokeKey.isPending}
                  onClick={() => revokeKey.mutate(key.id)}
                >
                  <Trash2 className="h-3.5 w-3.5" /> Revoke
                </Button>
              )}
            </div>
          ))}
          {keys.length === 0 && <p className="px-4 py-6 text-sm text-slate-500">No API keys yet.</p>}
        </div>
      )}

      <Modal open={plaintextKey !== null} onClose={() => setPlaintextKey(null)} title="API key created">
        <p className="mb-3 text-sm text-slate-300">
          Copy this key now - it will not be shown again.
        </p>
        <div className="flex items-center gap-2 rounded-md border border-surface-border bg-surface px-3 py-2">
          <code className="flex-1 truncate text-xs text-emerald-300">{plaintextKey}</code>
          <button
            onClick={() => plaintextKey && navigator.clipboard.writeText(plaintextKey)}
            className="rounded-md p-1.5 text-slate-400 hover:bg-slate-800 hover:text-slate-200"
          >
            <Copy className="h-3.5 w-3.5" />
          </button>
        </div>
      </Modal>
    </div>
  );
}

function ProfileTab() {
  const { user } = useAuth();
  if (!user) return null;

  return (
    <div className="flex items-center gap-4">
      <div className="flex h-14 w-14 items-center justify-center rounded-full bg-indigo-500/20 text-lg font-semibold text-indigo-300">
        {initials(user.fullName)}
      </div>
      <div>
        <p className="text-sm font-medium text-slate-100">{user.fullName}</p>
        <p className="text-xs text-slate-500">{user.email}</p>
        <Badge tone="slate">{user.role}</Badge>
      </div>
    </div>
  );
}

function Field({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <p className="text-xs font-medium text-slate-400">{label}</p>
      <p className="mt-0.5 text-sm text-slate-100">{value}</p>
    </div>
  );
}
