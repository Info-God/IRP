import { cn } from "@/lib/cn";
import { formatPercent } from "@/lib/formatters";

export function ConfidenceMeter({ confidence }: { confidence: number }) {
  const tone =
    confidence >= 0.8 ? "bg-emerald-500" : confidence >= 0.5 ? "bg-amber-500" : "bg-red-500";
  const label = confidence >= 0.8 ? "High" : confidence >= 0.5 ? "Medium" : "Low";

  return (
    <div className="w-full max-w-[220px]">
      <div className="mb-1 flex items-center justify-between text-xs">
        <span className="text-slate-400">Confidence</span>
        <span className="font-medium text-slate-200">
          {formatPercent(confidence)} <span className="text-slate-500">· {label}</span>
        </span>
      </div>
      <div className="h-1.5 w-full overflow-hidden rounded-full bg-slate-800">
        <div
          className={cn("h-full rounded-full transition-all", tone)}
          style={{ width: `${Math.round(confidence * 100)}%` }}
        />
      </div>
    </div>
  );
}
