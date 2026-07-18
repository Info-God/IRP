import { useState } from "react";
import { Bell, LogOut, Search } from "lucide-react";
import { useLocation } from "react-router-dom";
import { ProjectSwitcher } from "@/components/layout/ProjectSwitcher";
import { useAuth } from "@/context/AuthContext";
import { initials } from "@/lib/formatters";

const titleByPath: Record<string, string> = {
  "/dashboard": "Dashboard",
  "/incidents": "Incidents",
  "/copilot": "AI Copilot",
  "/runbooks": "Runbooks / Knowledge Base",
  "/automations": "Automations",
  "/integrations": "Integrations",
  "/audit-logs": "Audit Logs",
  "/settings": "Settings",
};

export function Topbar() {
  const { user, logout } = useAuth();
  const location = useLocation();
  const [menuOpen, setMenuOpen] = useState(false);

  const title =
    titleByPath[location.pathname] ??
    (location.pathname.startsWith("/incidents/") ? "Incident Detail" : "IRP Console");

  return (
    <header className="flex h-14 flex-none items-center justify-between gap-4 border-b border-surface-border bg-surface px-6">
      <div className="flex items-center gap-4">
        <h1 className="text-sm font-semibold text-slate-100">{title}</h1>
        <ProjectSwitcher />
      </div>

      <div className="flex items-center gap-3">
        <button className="hidden items-center gap-2 rounded-md border border-surface-border px-2.5 py-1.5 text-xs text-slate-500 hover:bg-slate-800/60 md:flex">
          <Search className="h-3.5 w-3.5" />
          <span>Search</span>
          <kbd className="rounded border border-surface-border bg-surface px-1 text-[10px]">⌘K</kbd>
        </button>

        <button className="relative rounded-md p-2 text-slate-400 hover:bg-slate-800/60 hover:text-slate-200">
          <Bell className="h-4.5 w-4.5" />
          <span className="absolute right-1.5 top-1.5 h-1.5 w-1.5 rounded-full bg-amber-400" />
        </button>

        <div className="relative">
          <button
            onClick={() => setMenuOpen((prev) => !prev)}
            className="flex h-8 w-8 items-center justify-center rounded-full bg-indigo-500/20 text-xs font-semibold text-indigo-300"
          >
            {user ? initials(user.fullName) : "?"}
          </button>
          {menuOpen && (
            <>
              <div className="fixed inset-0 z-10" onClick={() => setMenuOpen(false)} />
              <div className="absolute right-0 z-20 mt-2 w-52 rounded-md border border-surface-border bg-surface-raised py-1 shadow-xl">
                <div className="border-b border-surface-border px-3 py-2">
                  <p className="truncate text-sm font-medium text-slate-100">{user?.fullName}</p>
                  <p className="truncate text-xs text-slate-500">{user?.email}</p>
                </div>
                <button
                  onClick={logout}
                  className="flex w-full items-center gap-2 px-3 py-2 text-left text-sm text-slate-300 hover:bg-slate-800/60"
                >
                  <LogOut className="h-3.5 w-3.5" /> Log out
                </button>
              </div>
            </>
          )}
        </div>
      </div>
    </header>
  );
}
