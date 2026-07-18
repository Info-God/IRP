import { useState } from "react";
import { ChevronDown, Check, Layers } from "lucide-react";
import { useProject } from "@/context/ProjectContext";
import { cn } from "@/lib/cn";

export function ProjectSwitcher() {
  const { projects, currentProject, setCurrentProjectId } = useProject();
  const [open, setOpen] = useState(false);

  if (!currentProject) {
    return <div className="h-9 w-48 animate-pulse rounded-md bg-slate-800" />;
  }

  return (
    <div className="relative">
      <button
        onClick={() => setOpen((prev) => !prev)}
        className="flex items-center gap-2 rounded-md border border-surface-border bg-surface px-3 py-2 text-sm text-slate-200 hover:bg-slate-800/60"
      >
        <Layers className="h-4 w-4 text-slate-400" />
        <span className="max-w-[160px] truncate font-medium">{currentProject.name}</span>
        <span className="hidden text-xs text-slate-500 sm:inline">{currentProject.environment}</span>
        <ChevronDown className="h-3.5 w-3.5 text-slate-500" />
      </button>

      {open && (
        <>
          <div className="fixed inset-0 z-10" onClick={() => setOpen(false)} />
          <div className="absolute left-0 z-20 mt-1 w-64 rounded-md border border-surface-border bg-surface-raised py-1 shadow-xl">
            <p className="px-3 py-1.5 text-[10px] font-semibold uppercase tracking-wide text-slate-500">
              Projects
            </p>
            {projects.map((project) => (
              <button
                key={project.id}
                onClick={() => {
                  setCurrentProjectId(project.id);
                  setOpen(false);
                }}
                className={cn(
                  "flex w-full items-center justify-between px-3 py-2 text-left text-sm hover:bg-slate-800/60",
                  project.id === currentProject.id ? "text-slate-100" : "text-slate-300",
                )}
              >
                <span>
                  {project.name}
                  <span className="ml-1.5 text-xs text-slate-500">{project.environment}</span>
                </span>
                {project.id === currentProject.id && <Check className="h-3.5 w-3.5 text-indigo-400" />}
              </button>
            ))}
          </div>
        </>
      )}
    </div>
  );
}
