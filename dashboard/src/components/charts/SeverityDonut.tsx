import { Cell, Legend, Pie, PieChart, ResponsiveContainer, Tooltip } from "recharts";
import type { IncidentResponse, Severity } from "@/types";

const severityColors: Record<Severity, string> = {
  CRITICAL: "#f87171",
  HIGH: "#fb923c",
  MEDIUM: "#fbbf24",
  LOW: "#94a3b8",
};

export function SeverityDonut({ incidents }: { incidents: IncidentResponse[] }) {
  const order: Severity[] = ["CRITICAL", "HIGH", "MEDIUM", "LOW"];
  const data = order
    .map((severity) => ({
      name: severity,
      value: incidents.filter((incident) => incident.severity === severity).length,
    }))
    .filter((entry) => entry.value > 0);

  if (data.length === 0) {
    return <div className="flex h-[220px] items-center justify-center text-sm text-slate-500">No open incidents</div>;
  }

  return (
    <ResponsiveContainer width="100%" height={220}>
      <PieChart>
        <Pie data={data} dataKey="value" nameKey="name" innerRadius={55} outerRadius={80} paddingAngle={3}>
          {data.map((entry) => (
            <Cell key={entry.name} fill={severityColors[entry.name as Severity]} stroke="none" />
          ))}
        </Pie>
        <Tooltip
          contentStyle={{ background: "#111827", border: "1px solid #1f2937", borderRadius: 8, fontSize: 12 }}
        />
        <Legend
          verticalAlign="bottom"
          height={24}
          iconSize={8}
          formatter={(value) => <span className="text-xs text-slate-400">{value}</span>}
        />
      </PieChart>
    </ResponsiveContainer>
  );
}
