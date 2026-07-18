import type { PageResponse } from "@/types";

export function paginate<T>(items: T[], page: number, size: number): PageResponse<T> {
  const start = page * size;
  return {
    items: items.slice(start, start + size),
    page,
    size,
    totalElements: items.length,
    totalPages: Math.max(1, Math.ceil(items.length / size)),
  };
}
