import { delay } from "@/lib/delay";
import { mockRunbooks } from "@/mocks/mockData";
import type { RunbookView } from "@/types";

/** No backend endpoint exists yet - see Phase 4 design doc gap list. Mock-only. */
export async function listRunbooks(): Promise<RunbookView[]> {
  return delay([...mockRunbooks]);
}
