import { cn } from "@/lib/cn";

export interface TabItem {
  key: string;
  label: string;
  count?: number;
}

export function Tabs({
  tabs,
  active,
  onChange,
}: {
  tabs: TabItem[];
  active: string;
  onChange: (key: string) => void;
}) {
  return (
    <div className="flex gap-1 border-b border-surface-border px-1">
      {tabs.map((tab) => (
        <button
          key={tab.key}
          onClick={() => onChange(tab.key)}
          className={cn(
            "relative px-3 py-2.5 text-sm font-medium transition-colors",
            active === tab.key ? "text-slate-50" : "text-slate-400 hover:text-slate-200",
          )}
        >
          {tab.label}
          {typeof tab.count === "number" && (
            <span className="ml-1.5 rounded-full bg-slate-800 px-1.5 py-0.5 text-[10px] text-slate-400">
              {tab.count}
            </span>
          )}
          {active === tab.key && (
            <span className="absolute inset-x-0 -bottom-px h-0.5 rounded-full bg-indigo-400" />
          )}
        </button>
      ))}
    </div>
  );
}
