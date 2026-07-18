import { NavLink } from "react-router-dom";
import {
  LayoutDashboard,
  Siren,
  Sparkles,
  BookOpen,
  Zap,
  Plug,
  ScrollText,
  Settings,
  ChevronsLeft,
  ChevronsRight,
  ShieldCheck,
} from "lucide-react";
import { cn } from "@/lib/cn";

const navItems = [
  { to: "/dashboard", label: "Dashboard", icon: LayoutDashboard },
  { to: "/incidents", label: "Incidents", icon: Siren },
  { to: "/copilot", label: "AI Copilot", icon: Sparkles },
  { to: "/runbooks", label: "Runbooks", icon: BookOpen },
  { to: "/automations", label: "Automations", icon: Zap },
  { to: "/integrations", label: "Integrations", icon: Plug },
  { to: "/audit-logs", label: "Audit Logs", icon: ScrollText },
];

export function Sidebar({
  collapsed,
  onToggle,
  mobileOpen,
  onCloseMobile,
}: {
  collapsed: boolean;
  onToggle: () => void;
  mobileOpen: boolean;
  onCloseMobile: () => void;
}) {
  return (
    <>
      {/* Backdrop: mobile only, closes the drawer on click. Never rendered at md+, where
          the sidebar is always inline rather than an overlay. */}
      {mobileOpen && (
        <div className="fixed inset-0 z-30 bg-black/50 md:hidden" onClick={onCloseMobile} />
      )}
      <aside
        className={cn(
          "fixed inset-y-0 left-0 z-40 h-full w-60 flex-none flex-col border-r border-surface-border",
          "bg-surface-raised md:relative md:z-auto md:flex",
          mobileOpen ? "flex" : "hidden",
          collapsed ? "md:w-16" : "md:w-60",
        )}
      >
        <div className="flex h-14 items-center gap-2 border-b border-surface-border px-4">
          <div className="flex h-7 w-7 flex-none items-center justify-center rounded-md bg-indigo-500">
            <ShieldCheck className="h-4 w-4 text-white" />
          </div>
          {!collapsed && <span className="truncate text-sm font-semibold text-slate-100">IRP Console</span>}
        </div>

        <nav className="flex-1 space-y-1 overflow-y-auto p-2">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              onClick={onCloseMobile}
              className={({ isActive }) =>
                cn(
                  "flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-colors",
                  isActive
                    ? "bg-indigo-500/15 text-indigo-300"
                    : "text-slate-400 hover:bg-slate-800/60 hover:text-slate-200",
                )
              }
              title={collapsed ? item.label : undefined}
            >
              <item.icon className="h-4.5 w-4.5 flex-none" />
              {!collapsed && <span className="truncate">{item.label}</span>}
            </NavLink>
          ))}
        </nav>

        <div className="space-y-1 border-t border-surface-border p-2">
          <NavLink
            to="/settings"
            onClick={onCloseMobile}
            className={({ isActive }) =>
              cn(
                "flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-colors",
                isActive
                  ? "bg-indigo-500/15 text-indigo-300"
                  : "text-slate-400 hover:bg-slate-800/60 hover:text-slate-200",
              )
            }
            title={collapsed ? "Settings" : undefined}
          >
            <Settings className="h-4.5 w-4.5 flex-none" />
            {!collapsed && <span>Settings</span>}
          </NavLink>
          <button
            onClick={onToggle}
            className="hidden w-full items-center gap-3 rounded-md px-3 py-2 text-sm text-slate-500 hover:bg-slate-800/60 hover:text-slate-300 md:flex"
          >
            {collapsed ? <ChevronsRight className="h-4.5 w-4.5" /> : <ChevronsLeft className="h-4.5 w-4.5" />}
            {!collapsed && <span>Collapse</span>}
          </button>
        </div>
      </aside>
    </>
  );
}
