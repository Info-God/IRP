import { CheckCircle2, FileEdit, MessageSquare, PlusCircle, Sparkles } from "lucide-react";
import { cn } from "@/lib/cn";
import { formatDateTime } from "@/lib/formatters";
import type { IncidentTimelineEntryResponse, TimelineEntryType } from "@/types";

const iconByType: Record<TimelineEntryType, typeof PlusCircle> = {
  CREATED: PlusCircle,
  STATUS_CHANGE: FileEdit,
  NOTE: MessageSquare,
  AGENT_ACTION: Sparkles,
  APPROVAL: CheckCircle2,
};

const colorByType: Record<TimelineEntryType, string> = {
  CREATED: "bg-blue-500/15 text-blue-300",
  STATUS_CHANGE: "bg-slate-500/15 text-slate-300",
  NOTE: "bg-slate-500/15 text-slate-300",
  AGENT_ACTION: "bg-indigo-500/15 text-indigo-300",
  APPROVAL: "bg-emerald-500/15 text-emerald-300",
};

export function IncidentTimeline({ entries }: { entries: IncidentTimelineEntryResponse[] }) {
  if (entries.length === 0) {
    return <p className="px-1 py-6 text-sm text-slate-500">No timeline entries yet.</p>;
  }

  return (
    <ol className="space-y-0">
      {entries.map((entry, index) => {
        const Icon = iconByType[entry.entryType];
        const isLast = index === entries.length - 1;
        return (
          <li key={entry.id} className="relative flex gap-3 pb-6 last:pb-0">
            {!isLast && <span className="absolute left-[15px] top-8 h-full w-px bg-surface-border" />}
            <span className={cn("relative z-10 flex h-8 w-8 flex-none items-center justify-center rounded-full", colorByType[entry.entryType])}>
              <Icon className="h-4 w-4" />
            </span>
            <div className="min-w-0 flex-1 pt-1">
              <div className="flex flex-wrap items-baseline gap-x-2">
                <span className="text-sm font-medium text-slate-100">{entry.actor}</span>
                <span className="text-xs text-slate-500">{formatDateTime(entry.createdAt)}</span>
              </div>
              <p className="mt-0.5 text-sm text-slate-300">{entry.message}</p>
            </div>
          </li>
        );
      })}
    </ol>
  );
}
