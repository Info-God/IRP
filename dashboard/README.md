# IRP Console — Phase 4 React Dashboard

Frontend for the Agentic AI Incident Response Platform. Runs fully on mock data out of the
box — no backend required to demo it.

## Run it

```bash
npm install
npm run dev
```

Open **http://localhost:5173**. It redirects straight to `/login` (no landing page); any
email/password signs in in mock mode. Demo credentials are pre-filled.

## Connecting to the real backend

By default `VITE_USE_MOCKS=true`, so every page reads from `src/mocks/mockData.ts` through
the service layer in `src/services/`. To point at the real irp-core backend instead:

```bash
cp .env.example .env
# edit .env: VITE_USE_MOCKS=false, VITE_API_BASE_URL=http://localhost:8080
npm run dev
```

Each file in `src/services/` has a real `apiRequest(...)` call ready to go — the mock/real
switch is a single `if (USE_MOCKS)` branch per function, so no component code changes when
you flip the flag. See the "What is still mock" list below for which services have no real
endpoint to switch to yet.

## Project structure

```
src/
  types/           DTO types mirroring irp-core's Java DTOs field-for-field
  lib/              apiClient (fetch wrapper), config, formatters, cn, delay, paginate
  services/         one file per resource - real fetch + mock implementation, switched by USE_MOCKS
  mocks/            mockData.ts (the in-memory "database"), time.ts helpers
  context/          AuthContext (JWT + user), ProjectContext (project switcher)
  hooks/            TanStack Query hooks per resource, queryKeys.ts
  components/
    ui/              Badge, Button, Card, Modal, Tabs, Skeleton, StatCard, ConfidenceMeter, Pagination
    layout/          AppShell, Sidebar, Topbar, ProjectSwitcher, PageHeader
    charts/          IncidentTrendChart, SeverityDonut, ConfidenceHistogram (Recharts)
  features/
    incidents/        IncidentTable, IncidentFilters, CreateIncidentDialog, SeverityBadge, StatusBadge
    incident-detail/  IncidentTimeline, AgentSuggestionPanel, RelatedSignalsTab
    agent/             RiskBadge
  pages/             one component per route
  App.tsx            routes
  main.tsx           providers (QueryClient, Router, Auth, Project)
```

## Commands

| Command | What it does |
|---|---|
| `npm run dev` | Start the dev server at http://localhost:5173 |
| `npm run build` | Type-check and production build to `dist/` |
| `npm run typecheck` | Type-check only |
| `npm run preview` | Serve the production build locally |
