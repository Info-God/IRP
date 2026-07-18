import { delay } from "@/lib/delay";
import { mockAutomations } from "@/mocks/mockData";
import type { AutomationRuleView } from "@/types";

/** No backend/domain model exists yet - see Phase 4 design doc gap list. Mock-only. */
export async function listAutomations(): Promise<AutomationRuleView[]> {
  return delay([...mockAutomations]);
}
