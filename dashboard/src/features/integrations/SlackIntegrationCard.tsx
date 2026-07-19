import { useState, type FormEvent } from "react";
import { CheckCircle2, MessageSquare, Send, Unlink } from "lucide-react";
import { Card, CardBody } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Badge } from "@/components/ui/Badge";
import { Modal } from "@/components/ui/Modal";
import { Skeleton } from "@/components/ui/Skeleton";
import {
  useConnectSlackIntegration,
  useDisconnectSlackIntegration,
  useSendTestSlackMessage,
  useSlackIntegration,
} from "@/hooks/useSlackIntegration";
import { ApiError } from "@/lib/apiClient";
import { formatDateTime } from "@/lib/formatters";

const inputClass =
  "w-full rounded-md border border-surface-border bg-surface px-3 py-2 text-sm text-slate-100 placeholder:text-slate-500 focus:border-indigo-400 focus:outline-none";

function errorMessage(error: unknown, fallback: string): string {
  return error instanceof ApiError ? error.message : fallback;
}

export function SlackIntegrationCard({ projectId }: { projectId: string }) {
  const { data: integration, isLoading } = useSlackIntegration(projectId);
  const connect = useConnectSlackIntegration(projectId);
  const disconnect = useDisconnectSlackIntegration(projectId);
  const sendTest = useSendTestSlackMessage(projectId);

  const [dialogOpen, setDialogOpen] = useState(false);
  const [webhookUrl, setWebhookUrl] = useState("");

  const handleConnect = async (e: FormEvent) => {
    e.preventDefault();
    if (!webhookUrl.trim()) return;
    await connect.mutateAsync(webhookUrl.trim());
    setWebhookUrl("");
    setDialogOpen(false);
  };

  if (isLoading) {
    return (
      <Card>
        <CardBody>
          <Skeleton className="h-32 w-full" />
        </CardBody>
      </Card>
    );
  }

  return (
    <>
      <Card>
        <CardBody>
          <div className="flex items-center justify-between">
            <div className="rounded-md bg-slate-800 p-2 text-slate-300">
              <MessageSquare className="h-4 w-4" />
            </div>
            <Badge tone={integration ? "green" : "slate"}>{integration ? "Connected" : "Not connected"}</Badge>
          </div>
          <p className="mt-3 text-sm font-medium text-slate-100">Slack</p>
          <p className="mt-1 text-xs text-slate-500">
            {integration
              ? "Posts here automatically when an AI suggestion is approved."
              : "Post incident and AI suggestion updates to a Slack channel."}
          </p>

          {integration && (
            <div className="mt-3 space-y-1 rounded-md bg-slate-900/60 px-3 py-2 text-xs">
              <p className="font-mono text-slate-400">{integration.maskedWebhookUrl}</p>
              <p className="text-slate-500">
                Connected by {integration.connectedBy} · {formatDateTime(integration.updatedAt)}
              </p>
            </div>
          )}

          {integration && sendTest.isSuccess && (
            <p className="mt-2 flex items-center gap-1.5 text-xs text-emerald-400">
              <CheckCircle2 className="h-3.5 w-3.5" /> Test message sent
            </p>
          )}
          {sendTest.isError && (
            <p className="mt-2 text-xs text-red-400">{errorMessage(sendTest.error, "Failed to send test message")}</p>
          )}

          <div className="mt-3 flex gap-2">
            {integration ? (
              <>
                <Button
                  variant="secondary"
                  size="sm"
                  className="flex-1"
                  disabled={sendTest.isPending}
                  onClick={() => sendTest.mutate()}
                >
                  <Send className="h-3.5 w-3.5" /> {sendTest.isPending ? "Sending..." : "Send test"}
                </Button>
                <Button
                  variant="ghost"
                  size="sm"
                  title="Disconnect"
                  disabled={disconnect.isPending}
                  onClick={() => disconnect.mutate()}
                >
                  <Unlink className="h-3.5 w-3.5" />
                </Button>
              </>
            ) : (
              <Button variant="secondary" size="sm" className="w-full" onClick={() => setDialogOpen(true)}>
                Connect
              </Button>
            )}
          </div>
        </CardBody>
      </Card>

      <Modal open={dialogOpen} onClose={() => setDialogOpen(false)} title="Connect Slack">
        <form onSubmit={handleConnect} className="space-y-4">
          <div>
            <label className="mb-1 block text-xs font-medium text-slate-400">Incoming webhook URL</label>
            <input
              className={inputClass}
              value={webhookUrl}
              onChange={(e) => setWebhookUrl(e.target.value)}
              placeholder="https://hooks.slack.com/services/T000/B000/XXXXXXXX"
              required
            />
            <p className="mt-1 text-xs text-slate-500">
              Create one at{" "}
              <a
                href="https://api.slack.com/messaging/webhooks"
                target="_blank"
                rel="noreferrer"
                className="text-indigo-400 hover:underline"
              >
                api.slack.com/messaging/webhooks
              </a>
            </p>
          </div>
          {connect.isError && (
            <p className="text-xs text-red-400">{errorMessage(connect.error, "Failed to connect")}</p>
          )}
          <div className="flex justify-end gap-2 pt-2">
            <Button type="button" variant="ghost" onClick={() => setDialogOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" disabled={connect.isPending}>
              {connect.isPending ? "Connecting..." : "Connect"}
            </Button>
          </div>
        </form>
      </Modal>
    </>
  );
}
