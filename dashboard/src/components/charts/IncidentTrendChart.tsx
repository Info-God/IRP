import { Area, AreaChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import type { IncidentResponse } from "@/types";

function bucketByDay(incidents: IncidentResponse[]): { date: string; count: number }[] {
  const buckets = new Map<string, number>();
  const now = new Date();

  for (let i = 13; i >= 0; i--) {
    const d = new Date(now);
    d.setDate(d.getDate() - i);
    const key = d.toLocaleDateString(undefined, { month: "short", day: "numeric" });
    buckets.set(key, 0);
  }

  for (const incident of incidents) {
    const key = new Date(incident.openedAt).toLocaleDateString(undefined, {
      month: "short",
      day: "numeric",
    });
    if (buckets.has(key)) buckets.set(key, (buckets.get(key) ?? 0) + 1);
  }

  return Array.from(buckets.entries()).map(([date, count]) => ({ date, count }));
}

export function IncidentTrendChart({ incidents }: { incidents: IncidentResponse[] }) {
  const data = bucketByDay(incidents);

  return (
    <ResponsiveContainer width="100%" height={220}>
      <AreaChart data={data} margin={{ top: 8, right: 12, left: -16, bottom: 0 }}>
        <defs>
          <linearGradient id="incidentTrendGradient" x1="0" y1="0" x2="0" y2="1">
            <stop offset="5%" stopColor="#6366f1" stopOpacity={0.4} />
            <stop offset="95%" stopColor="#6366f1" stopOpacity={0} />
          </linearGradient>
        </defs>
        <CartesianGrid strokeDasharray="3 3" stroke="#1f2937" vertical={false} />
        <XAxis
          dataKey="date"
          stroke="#64748b"
          fontSize={11}
          tickLine={false}
          axisLine={false}
          interval="preserveStartEnd"
        />
        <YAxis stroke="#64748b" fontSize={11} tickLine={false} axisLine={false} allowDecimals={false} />
        <Tooltip
          contentStyle={{
            background: "#111827",
            border: "1px solid #1f2937",
            borderRadius: 8,
            fontSize: 12,
          }}
          labelStyle={{ color: "#e2e8f0" }}
        />
        <Area
          type="monotone"
          dataKey="count"
          name="Incidents opened"
          stroke="#818cf8"
          strokeWidth={2}
          fill="url(#incidentTrendGradient)"
        />
      </AreaChart>
    </ResponsiveContainer>
  );
}
