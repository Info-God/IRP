# Internship & Job Intelligence Workflow (self-hosted n8n)

Watches job boards (RSS), company career pages, and your job-alert emails; normalizes every
listing into one shape; drops anything you've already seen; does a free keyword pre-filter;
scores what survives against your profile with a free LLM (Groq); logs every scored job to a
Google Sheet; and pings you on Telegram for anything that scores above your threshold.

```
Schedule (6h) ──┬─→ RSS feeds ────────────┐
                └─→ Career pages ─────────┤
IMAP (job-alert emails) ──────────────────┴─→ Normalize → Merge → Dedupe
    → Keyword/skill pre-filter → AI score (Groq, free) → Log to Sheet
    → score ≥ threshold? → Telegram alert
```

Files in this folder:
- [`workflow.json`](workflow.json) — import this directly into n8n.
- [`.env.example`](.env.example) — env vars the workflow reads at runtime.

This is a starter scaffold, not a finished black box — the two genuinely hand-tuned parts
(career-page CSS selectors, and your resume/skills profile in the AI prompt) only you can fill
in, and are called out below.

---

## 1. Prerequisites

- A running self-hosted n8n instance (Docker or npm install) that you can reach in a browser.
- A Groq account (free) for the AI scoring step: https://console.groq.com/keys
- A Telegram account (free) for alerts.
- A Google account (free) for the results log — Google Sheets.
- Optional: an email account you already use for job alerts (LinkedIn/Indeed/Naukri "jobs for
  you" digests), IMAP-accessible.

---

## 2. Set up the free accounts

**Groq (AI scoring, free tier)**
1. Go to console.groq.com → sign up → **API Keys** → **Create API Key**.
2. Copy it — you'll only see it once.

**Telegram bot (notifications)**
1. In Telegram, message **@BotFather** → `/newbot` → follow the prompts → copy the **bot token**.
2. Send your new bot any message (e.g. "hi") so it has a conversation with you.
3. Visit `https://api.telegram.org/bot<YOUR_TOKEN>/getUpdates` in a browser and read the
   `"chat":{"id": ...}` value — that's your `TELEGRAM_CHAT_ID`.

**Google Sheet (results log)**
1. Create a new Google Sheet, e.g. "Job Intelligence Log".
2. Add a tab named `JobLog` with header row: `title, company, location, url, source,
   match_score, matched_skills, reasoning, date_found`.
3. Copy the sheet ID from its URL (the long string between `/d/` and `/edit`).

**Email IMAP (optional — job-alert emails)**
- Gmail: enable IMAP in Settings, and create an **App Password** (Google Account → Security →
  2-Step Verification → App passwords) rather than your real password.
- Any other provider: note the IMAP host/port from your provider's docs.

---

## 3. Configure n8n environment variables

Add these to wherever n8n's environment is defined (its `docker-compose.yml`, or a `.env` file
it loads), matching [`.env.example`](.env.example):

```bash
GROQ_API_KEY=your_groq_key
TELEGRAM_BOT_TOKEN=your_bot_token
TELEGRAM_CHAT_ID=your_chat_id
```

Restart the n8n container/process so it picks up the new variables — n8n only reads them at
startup.

---

## 4. Import the workflow

1. Open your n8n editor → **Workflows** → **Add workflow** → menu (⋯) → **Import from File**
   (or paste JSON via **Import from URL/Clipboard**).
2. Select [`workflow.json`](workflow.json).
3. You'll land on the canvas with ~18 nodes already wired together. n8n versions differ slightly
   in node parameter shapes, so if any node shows a red warning icon, open it — it's almost
   always a dropdown that needs re-selecting (e.g. HTTP response format), not a broken workflow.

---

## 5. Attach credentials

Two nodes need real n8n credentials attached (open the node → **Credential** dropdown → **Create
New**):

- **Email - Job Alerts (IMAP)** — your email's IMAP host/port/username/app-password.
- **Log All Scored Jobs** (Google Sheets) — sign in with your Google account (OAuth2).
  - In the same node, replace `YOUR_GOOGLE_SHEET_ID` in the **Document** field with your actual
    sheet ID from step 2.

Groq and Telegram deliberately use `{{$env.GROQ_API_KEY}}` / `{{$env.TELEGRAM_BOT_TOKEN}}`
expressions instead of n8n credentials, so step 3's env vars are all they need — nothing to
attach here.

---

## 6. The two parts only you can fill in

**A. Your profile, for AI scoring** — open the **AI Score (Groq - Free)** node, find the
`PROFILE:` sentence inside the system prompt in `jsonBody`, and rewrite it to describe you: real
skills, target roles, experience level, location preference. The AI can only score fit against
what you tell it about yourself.

**B. Career-page selectors** — open **Career Page List** (a Code node). It's currently one
placeholder entry (`Example Corp`). For each company you want watched:
1. Open their careers/jobs page, right-click a job listing → **Inspect**.
2. Find a CSS selector that matches *every* job card (e.g. `.job-listing`, `li.opening`).
3. Within that, find selectors for the title, the link (`<a href>`), and location.
4. Add an entry to the `pages` array with those three selectors as full page-scoped selectors
   (see the existing placeholder for the shape).

This part is inherently manual — there's no universal selector that works across company sites.
If you don't want to bother with this, delete the **Career Page List → Fetch Career Page HTML →
Extract Career Page Jobs → Normalize Career Jobs** chain and rewire **Schedule - Every 6h**
to connect straight to **RSS Feed List** only, and drop input index 1 in **Merge All Sources**
(change its `numberInputs` to 2).

Also worth tuning: the **Keyword/Skill Pre-Filter** node's `mustMatchAny` / `excludeIfAny` /
`preferredLocations` arrays — this is your cheap first-pass filter before the AI call, so keep it
loose enough not to miss real matches.

---

## 7. Add more RSS feeds (optional)

Open **RSS Feed List**. It ships with WeWorkRemotely and RemoteOK, which are known to still
serve public RSS. LinkedIn, Naukri, and Indeed have all discontinued public RSS — cover those
instead via:
- the IMAP email-alert branch (sign up for their email digests, let the workflow parse them), or
- a paid/free-tier job-search API (JSearch on RapidAPI, Adzuna API) added as another HTTP
  Request branch feeding the same normalize-and-merge pattern.

Search "`<board name>` jobs rss feed" to check if a board you care about still has one before
adding it — boards change this without notice.

---

## 8. Test end to end

1. Click the **AI Score (Groq - Free)** node → **Test step** with one manually-entered item, to
   confirm your Groq key and prompt work before running the whole thing.
2. Click **Execute Workflow** (top toolbar) to run the full pipeline once manually.
3. Watch each node's output as it runs — the **Remove Already-Seen Jobs** node will pass
   everything through on the very first run (nothing's "seen" yet), so expect more Telegram
   alerts on run 1 than on later runs.
4. Check your Google Sheet — every scored job should have a row, not just the ones that passed
   the threshold.
5. Check Telegram for alerts on anything scoring ≥ 70 (tune that threshold in the **Shortlist
   Filter** node's condition).

---

## 9. Activate it

Toggle **Active** (top-right of the workflow editor) once steps 4–8 all check out. The Schedule
Trigger will then run every 6 hours (edit the `hoursInterval` in **Schedule - Every 6h** to
change cadence), and the IMAP trigger will fire in near-real-time whenever a new email lands.

---

## Troubleshooting

- **Merge node seems to hang or only shows 2 of 3 branches on a scheduled run** — that's
  expected: the schedule trigger and the email trigger start independent executions, so a
  schedule-triggered run never has email-branch data, and vice versa. n8n handles this fine.
  If you see it behave oddly on your version, split this into two workflows (one
  schedule-triggered for RSS+career pages, one IMAP-triggered for email) — same downstream nodes,
  just duplicated.
- **Groq call fails with 401** — the env var didn't load; confirm `GROQ_API_KEY` is set where
  n8n reads its environment, and that you restarted n8n after adding it.
- **Telegram alert never arrives** — you likely haven't messaged your bot yet (step 2); Telegram
  bots can't message you first.
- **Career page extraction returns empty arrays** — the CSS selectors don't match; re-inspect the
  live page, sites change their markup periodically.
