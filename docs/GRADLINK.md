# GradLink — product brief

> **The problem in one line:** a student graduates, goes home with a qualification,
> and stalls — because every entry-level job wants experience they can't get, the
> job market is an unreadable maze, and if they'd rather build something, the money
> and the people who could help are scattered across a dozen offices they've never
> heard of.

GradLink is one platform for the two doors a graduate can walk through:

1. **Get hired** — convert "no experience" into a first, verifiable work record.
2. **Build your own** — turn an idea into a registered, funded, mentored business.

---

## 1. Why this is real (evidence, not vibes)

| Fact | Figure | Source |
| --- | --- | --- |
| Graduate unemployment | **12.2%** (Q2 2025), up from 5.8% in 2008 | [Stats SA QLFS](https://www.statssa.gov.za/?p=18668) |
| Young graduates (under 35) unemployed | **~24%** | [News24 on Stats SA](https://www.news24.com/business/economy/unemployment-worsens-24-of-young-university-graduates-now-jobless-20250513-0667) |
| Unemployed youth with **no previous work experience** | **58.7%** of 4.8 million | [Stats SA](https://www.statssa.gov.za/?p=18398) |
| Youth not in employment, education or training (NEET) | **45.1%** of ages 15–34 | [Stats SA QLFS](https://www.statssa.gov.za/publications/P0211/P02111stQuarter2025.pdf) |
| National unemployment | **32.7%** (Q1 2026) | [TechCentral on Stats SA](https://techcentral.co.za/a-degree-is-no-longer-enough/283358/) |

That 58.7% *is* the experience paradox: you cannot get the job without experience,
and you cannot get experience without the job. GradLink's whole job is to break
that loop on the left while opening the right-hand door for the graduates who'd
rather employ themselves.

### The two doors, and what sits in front of each

**Door 1 — employment.** The state already funds the loop-breaker and most
graduates don't know it exists:

* **YES (Youth Employment Service)** — 12-month paid placements with private
  employers, **~R5,241/month** from March 2026, ages 18–34, **no experience
  required**, applied for through sayouth.mobi (zero-rated). One placement per
  person. ([programme details](https://gocareers.co.za/apply/yes-programme))
* Learnerships and internships — same function, sector-specific (NQF-linked).
* What's missing is **navigation**: which programme, which document, which
  deadline, and what to do in week 2 when you're placed but useless at Excel.

**Door 2 — entrepreneurship.** Funding exists and is wildly under-used:

* **NYDA Grant Programme** — non-repayable **R1,000 – R200,000** (up to
  R250,000 for co-operatives; R300,000 for agriculture/technology). Ages 18–35,
  100% youth-owned, turnover under R750,000, **Business Management Training is a
  compulsory prerequisite**. Four tiers from survivalist to expansion.
  ([NYDA](https://www.nyda.gov.za/Portals/0/WebSitesCreative_MyContentManager/1092/NYDA_Grant_Programme.pdf),
  [guide](https://smesouthafrica.co.za/sme-guides/guide-to-nyda-funding/))
* SEFA loans, bank youth accounts, incubators, and pitch competitions — each with
  different paperwork.
* What's missing is **translation**: "which of these can I actually get, this
  month, with what I have?" plus **people** — a co-founder, a mentor who has done
  it, and peers who won't let you quit.

Sources for the Perplexity links you shared: that page is JavaScript-rendered, so
its text can't be extracted server-side. Everything above is from the primary
sources it would have cited (Stats SA, NYDA, YES), which is what judges will ask
for anyway.

---

## 2. What GradLink does

### Pillar A — "Get my first yes"

| Feature | What it removes |
| --- | --- |
| **No-experience job & programme feed** | Filter to roles and programmes that explicitly require 0 years |
| **Experience builder** | Turns coursework, side projects, volunteering, and campus roles into CV-ready bullets |
| **CV + cover letter builder** | Structured templates per sector; exports PDF |
| **Application tracker** | Kanban: Saved → Applied → Interview → Offer, with deadline reminders |
| **Interview simulator** | AI asks role-specific questions, scores answers, suggests improvements |
| **First-90-days guide** | What to do in weeks 1–12 of a placement so it converts to a job |

### Pillar B — "Build my own"

| Feature | What it removes |
| --- | --- |
| **Funding matcher** | Answers "what can I get with my age, stage, turnover and sector?" (NYDA tier, SEFA, youth funds) |
| **Readiness checklist** | CIPC registration, B-BBEE affidavit, bank account, tax number — tracked as steps, not prose |
| **Co-founder matching** | Skills-based matching ("I build, you sell") with profile verification |
| **Mentor marketplace** | Book 30 minutes with an operator in your sector; mentors pledge hours |
| **Idea validation sprint** | 48-hour guided process: problem interviews, 10 customer calls, go/no-go |
| **Toolkit library** | Business plan, pitch deck, pricing calculator, invoicing, first-client scripts |

### Pillar C — the connective tissue

* **Path maps** — "I have a BCom Accounting, what are the 6 realistic next moves?"
* **Community + meetups** — graduates in the same position, by city and sector.
* **Progress score** — a single number that goes up when you apply, register,
  pitch, or finish a module, so sitting at home has a visible cost.
* **Guides in 3 languages** — English, isiZulu, Afrikaans (extendable).

---

## 3. User journeys (what the demo shows)

**Thabo — BCom, 14 months at home, Bloemfontein**
1. Opens GradLink → "No experience? Start here."
2. Sees 6 YES placements, 3 internships, all filtered to 0 years required.
3. CV builder turns his tutor job + a resstock project into two bullets.
4. Applies via the tracker; gets a reminder at the deadline.
5. Interview simulator drills the 5 questions that come up in retail banking.
6. Gets placed → "First 90 days" checklist converts the 12-month contract into a
   permanent role.

**Naledi — Diploma in IT, wants to freelance**
1. Idea validation sprint narrows "an app" to "stock-taking for spaza shops".
2. Readiness checklist: CIPC → bank → tax → first invoice template.
3. Funding matcher: NYDA Threshold 2 (R10k–R50k) + prerequisite training booked.
4. Co-founder matching finds a partner who can sell; mentor books a call.
5. Pitch deck review clinic before the NYDA panel.

---

## 4. Data model (what already exists in this repo)

The app is search-first because that is how graduates actually use it: **one
search box, two answer types.** The existing schema maps cleanly:

```
Entry
 ├─ kind:      job | programme | funding | mentor | cofounder | guide | community
 ├─ category:  employment | funding | mentorship | community | upskilling
 ├─ tags:      ["no experience", "graduate", "18-34", "stipend", "remote"]
 ├─ price:     stipend or grant ceiling (nullable)
 ├─ location:  city / province / remote
 ├─ url:       official application page
 └─ deadline:  drives reminders (new field)
```

Search ranking already handles: typo tolerance (`nyda` / `nyad`), synonyms
(`funding → grant, capital`), facet filtering by category, and sorting by
closeness/deadline. See `docs/ARCHITECTURE.md §3`.

### New backend endpoints to add (roadmap)

| Endpoint | Purpose |
| --- | --- |
| `GET /api/v1/funding/match?age&stage&turnover&sector` | Rule-based eligibility matcher over NYDA/SEFA tiers |
| `POST /api/v1/cv/build` | Deterministic CV assembly (never AI-invented experience) |
| `POST /api/v1/interview/start` | Question set per role, scored answers |
| `GET /api/v1/tracker` | Application states with reminders |
| `POST /api/v1/cofounder/search` | Skills-vector matching |

---

## 5. Hackathon pitch (60 seconds)

> "58.7% of unemployed young South Africans have never had a job — and a degree
> no longer protects you, with graduate unemployment more than doubling since
> 2008. At the same time, R200,000 NYDA grants go unclaimed because the
> paperwork is invisible and the prerequisite training looks like a maze.
>
> GradLink is one platform with two doors: get your first verified work
> experience, or fund and build your own business. We match you to the
> programmes that explicitly need zero experience, turn your coursework into a
> real CV, and if you'd rather build — we tell you exactly which grant you
> qualify for today, what document is missing, who your co-founder could be,
> and which mentor will take your call.
>
> Everything runs on the phone you already have, offline-first."

### Judging criteria mapping

| Criterion | Evidence |
| --- | --- |
| Problem relevance | Stats SA: 12.2% graduate unemployment; 58.7% with no experience; 45.1% NEET |
| Solution clarity | Two doors, one search box, explicit next action per item |
| Technical execution | Green CI, installable APK, 47 backend tests, offline-first Room cache |
| Business viability | Free to graduate; revenue from employer listing fees, mentor sessions, and funded-programme placements |
| Impact/scale | 4.8m unemployed youth; national footprint, no office needed |

---

## 6. Roadmap (post-hackathon)

1. **Content moat** — ingest learnerships, YES placements and bursaries daily;
   every listing carries a source URL and a "verified on" date.
2. **Outcome tracking** — did the user get placed? That number is the pitch.
3. **Employer side** — post a 0-experience role, screen candidates by readiness score.
4. **Funder dashboard** — which applicants are eligible, which documents are missing.
5. **Offline-first distribution** — zero-rated data partnership; SMS fallback.

---

## 7. Naming

**GradLink** — graduate + link (connection to work, funding, and each other).
The repo keeps the `searchhh` package id for build stability; the product surface
(strings, docs, APK label) is GradLink.
