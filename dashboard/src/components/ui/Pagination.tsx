import { ChevronLeft, ChevronRight } from "lucide-react";
import { Button } from "@/components/ui/Button";

export function Pagination({
  page,
  totalPages,
  onChange,
}: {
  page: number;
  totalPages: number;
  onChange: (page: number) => void;
}) {
  if (totalPages <= 1) return null;

  return (
    <div className="flex items-center justify-between border-t border-surface-border px-5 py-3">
      <p className="text-xs text-slate-400">
        Page {page + 1} of {totalPages}
      </p>
      <div className="flex gap-2">
        <Button size="sm" variant="ghost" disabled={page === 0} onClick={() => onChange(page - 1)}>
          <ChevronLeft className="h-3.5 w-3.5" /> Prev
        </Button>
        <Button
          size="sm"
          variant="ghost"
          disabled={page + 1 >= totalPages}
          onClick={() => onChange(page + 1)}
        >
          Next <ChevronRight className="h-3.5 w-3.5" />
        </Button>
      </div>
    </div>
  );
}
