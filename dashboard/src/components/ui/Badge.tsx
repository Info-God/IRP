import type { ReactNode } from "react";
import { cn } from "@/lib/cn";

type BadgeTone = "slate" | "blue" | "violet" | "amber" | "green" | "red" | "orange";

const toneClasses: Record<BadgeTone, string> = {
  slate: "bg-slate-500/15 text-slate-300 ring-slate-500/30",
  blue: "bg-blue-500/15 text-blue-300 ring-blue-500/30",
  violet: "bg-violet-500/15 text-violet-300 ring-violet-500/30",
  amber: "bg-amber-500/15 text-amber-300 ring-amber-500/30",
  green: "bg-emerald-500/15 text-emerald-300 ring-emerald-500/30",
  red: "bg-red-500/15 text-red-300 ring-red-500/30",
  orange: "bg-orange-500/15 text-orange-300 ring-orange-500/30",
};

export function Badge({ tone = "slate", children }: { tone?: BadgeTone; children: ReactNode }) {
  return (
    <span
      className={cn(
        "inline-flex items-center gap-1 rounded-full px-2.5 py-0.5 text-xs font-medium ring-1 ring-inset",
        toneClasses[tone],
      )}
    >
      {children}
    </span>
  );
}
