import { Bar, BarChart, CartesianGrid, Cell, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import type { AgentSuggestionResponse } from "@/types";

const buckets = [
  { label: "0-20%", min: 0, max: 0.2 },
  { label: "20-40%", min: 0.2, max: 0.4 },
  { label: "40-60%", min: 0.4, max: 0.6 },
  { label: "60-80%", min: 0.6, max: 0.8 },
  { label: "80-100%", min: 0.8, max: 1.01 },
];

function colorFor(min: number): string {
  if (min >= 0.8) return "#34d399";
  if (min >= 0.4) return "#fbbf24";
  return "#f87171";
}

export function ConfidenceHistogram({ suggestions }: { suggestions: AgentSuggestionResponse[] }) {
  const data = buckets.map((bucket) => ({
    label: bucket.label,
    count: suggestions.filter((s) => s.confidence >= bucket.min && s.confidence < bucket.max).length,
    color: colorFor(bucket.min),
  }));

  return (
    <ResponsiveContainer width="100%" height={200}>
      <BarChart data={data} margin={{ top: 8, right: 12, left: -16, bottom: 0 }}>
        <CartesianGrid strokeDasharray="3 3" stroke="#1f2937" vertical={false} />
        <XAxis dataKey="label" stroke="#64748b" fontSize={11} tickLine={false} axisLine={false} />
        <YAxis stroke="#64748b" fontSize={11} tickLine={false} axisLine={false} allowDecimals={false} />
        <Tooltip
          contentStyle={{ background: "#111827", border: "1px solid #1f2937", borderRadius: 8, fontSize: 12 }}
          cursor={{ fill: "rgba(148,163,184,0.08)" }}
        />
        <Bar dataKey="count" name="Suggestions" radius={[4, 4, 0, 0]}>
          {data.map((entry) => (
            <Cell key={entry.label} fill={entry.color} />
          ))}
        </Bar>
      </BarChart>
    </ResponsiveContainer>
  );
}
