import { useState } from "react";
import { Modal } from "@/components/ui/Modal";
import { Button } from "@/components/ui/Button";
import { useCreateRunbook } from "@/hooks/useMisc";

const inputClass =
  "w-full rounded-md border border-surface-border bg-surface px-3 py-2 text-sm text-slate-100 placeholder:text-slate-500 focus:border-indigo-400 focus:outline-none";

export function UploadRunbookDialog({
  open,
  onClose,
  projectId,
}: {
  open: boolean;
  onClose: () => void;
  projectId: string;
}) {
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");

  const createRunbook = useCreateRunbook(projectId);

  const reset = () => {
    setTitle("");
    setContent("");
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim() || !content.trim()) return;
    await createRunbook.mutateAsync({ title: title.trim(), content: content.trim() });
    reset();
    onClose();
  };

  return (
    <Modal open={open} onClose={onClose} title="Upload runbook">
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="mb-1 block text-xs font-medium text-slate-400">Title</label>
          <input
            className={inputClass}
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="e.g. Checkout Service Incident Playbook"
            required
          />
        </div>
        <div>
          <label className="mb-1 block text-xs font-medium text-slate-400">Content</label>
          <textarea
            className={inputClass}
            rows={10}
            value={content}
            onChange={(e) => setContent(e.target.value)}
            placeholder="Paste the runbook text. It's split into chunks and embedded so the AI agent can search it during an investigation."
            required
          />
        </div>
        <div className="flex justify-end gap-2 pt-2">
          <Button type="button" variant="ghost" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit" variant="primary" disabled={createRunbook.isPending}>
            {createRunbook.isPending ? "Uploading & embedding..." : "Upload runbook"}
          </Button>
        </div>
      </form>
    </Modal>
  );
}
