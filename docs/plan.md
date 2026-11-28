# Sentiero Kerbagio working plan

Baseline (approved, source of truth for problem, premises and behavior rules 1-19): `docs/designs/hiking-trip-planner.md`.
This file records what each plan review changes on top of that baseline. Where they differ, this file wins.

## Product review, 2026-11-01

Mode: HOLD SCOPE (explicit user instruction: "pick Hold Scope mode"). Review depth: implementation-ready (default).

### Pre-review system audit
- Repo: `main`, no commits yet; only `docs/` exists (design doc, wireframe). No stashes, no TODO/FIXME markers, no TODOS.md.
- Binding decisions (the decision log): approach C built A-first; five premises; midterm on or before 2026-11-08, final assumed ~2026-12-06. This review honors all three.
- Prior learnings applied: "weather-states-decided-locally" (8/10, 2026-11-01).
- Landscape: reused from the first design session the same day (PackPoint, Packr, Wanderpack: weather-aware packing lists; HikerFeed, Trailfold: hiking journals). Layer 3 holds: the planner and the journal are separate products elsewhere; joining them is this app's point.
- UI scope: yes (5 screens + sheet). Section 11 applies.

### 0A. Premise challenge
- Real problem: a solo student must pass a midterm demo in at most 7 days and ship a defensible final MVP around 5 weeks later, alongside other courses. The product problem (forgotten items, lost notes) is the vehicle for the grade.
- The plan attacks the product pain directly: carried-over forgotten items and forecast suggestions change the next packing list; journal search covers lost notes.
- Do-nothing cost: a failed midterm, which cannot be recovered later.
- The risk is not the idea. It is the amount of approved behavior: 19 rules, 5 screens plus a sheet, 2 APIs, and an optional reminder, built solo while learning Navigation Compose. The approved build order also puts the riskiest brief requirement (network) third, behind pure-Room journal polish.

### 0B. Existing code leverage
| Sub-problem | Reuse |
|---|---|
| Room entity, DAO, repository, ViewModel `stateIn` | Week 6 "My Tasks" workshop (TaskEntity, TaskDao with `observeAll(): Flow`, `@Upsert`, `TaskRepository`, `TaskViewModel`) maps one-to-one to trips and checklist items |
| Retrofit service, DTO → domain mapping, Loading/Content/Empty/Error + Retry | Week 7 "Campus Events feed" workshop (`CampusApi`, `EventsRemoteDataSource`, `EventsRepository`, failure exercise) |
| Sealed UiState + `collectAsStateWithLifecycle` | Week 8 MVVM workshop (`TasksUiState`) |
| WorkManager + notification channel | Week 9 slides (no workshop code) |
| Navigation Compose | None in the course material: new learning |

REALITY: premise-table finding unverified=1 (Navigation Compose is not in the uploaded slides; whether it was taught in class is unknown) docs/designs/hiking-trip-planner.md:30
REALITY: already-done pass no commits on main; nothing built yet (git log: "does not have any commits yet") docs/plan.md:9
REALITY: surface-check pass Open-Meteo forecast params `daily=temperature_2m_max,temperature_2m_min,precipitation_probability_max,wind_speed_10m_max,uv_index_max`, `start_date`/`end_date` (ISO 8601), `timezone` confirmed against an Open-Meteo OpenAPI mirror and client libraries [static evidence; no live probe, the workspace cannot reach the API] docs/designs/hiking-trip-planner.md:91
REALITY: binding-decisions pass three user decisions from 2026-11-01 honored; none overruled docs/plan.md:9
REALITY: numbers pass 16-day horizon [measured: Open-Meteo README]; thresholds, 3 chips, 5 results, 140 chars [estimated: product choices, unit-test contract] docs/designs/hiking-trip-planner.md:89
REALITY: deferred-asks pass no linked issue; the course brief is the requirement source docs/designs/hiking-trip-planner.md:37
REALITY: pr-count pass no ship-policy; solo work commits to main, no PR queue docs/plan.md:1

### 0C. Dream state
```
  CURRENT STATE                THIS PLAN                          12-MONTH IDEAL
  Approved design doc,   --->  Midterm slice (Nov 8), then   ---> Portfolio app on GitHub (maybe
  wireframe, no code           brief-complete MVP + the loop      Play internal testing) that
                               (~Dec 6), defended layer by        friends use for Lebanese trails;
                               layer                              photos, shared lists, exports
```
The plan moves toward the ideal: Room schema, repository boundary and the loop are the parts later features hang on.

### 0G. HOLD SCOPE checks
1. Complexity: about 35 files and more than 2 new classes [estimated]. The count is natural for 5 screens on a layered stack. Fewer moving parts where it is free: one detail screen and ViewModel for upcoming and journal mode (already planned); one area-search composable reused by the form and the No-area card (already planned); one repository call for Undo and Move back.
2. Minimum changes for the goal: the midterm feature in the brief is "create a trip, complete packing items, display the saved trip detail". Add item and delete item are not required by it. Everything below the brief-complete line can slip without failing the brief.
3. Invariants kept: brief scope boundary, the five premises, rules 1-19 except where a decision below changes delivery timing.

## Decision ledger

| ID and owner | Contract and evidence | Current | Proposed | Status | Exact approval and scope |
|---|---|---|---|---|---|
| CEO-1 delivery (user) | Design doc build order steps 1-7; R3-21 asks for a cut line | Order 1-7 as written; midterm includes add/delete item; no cut line | A) Risk-first tiers with a cut line (see payload) | approved | User chose "Risk-first tiers (recommended)" in D1, 2026-11-01. Scope: delivery order and cut line only; rules 1-19 unchanged |
| CEO-2 templates (user) | Open Question 3 | Undecided: one or per difficulty | A) Base template + difficulty extras | approved | User chose "Base + difficulty extras (recommended)" in D2. Scope: starting list for template trips; items as in the D2 preview |
| CEO-3 reminder (user) | Open Question 4; brief: "optional notification reminder"; R3-18 | Optional step 6, unspecified | A) Keep, as the last MVP slice, with a written spec | approved | User chose "Keep, last MVP slice (recommended)" in D3. Scope: the D3 preview spec; dropped first if late |
| CEO-4 photos (user) | Open Question 4; premise 4 "stretch"; R3-19 | Stretch step 7 | A) Defer to TODOS.md | approved (deferred) | User chose "Defer to TODOS.md (recommended)" in D4. Photos leave this project's scope |

## currentDecision (CEO-1)
Commitment comparison:
Commitment | Source/approval or pending | Current | A | B | C
Midterm contents | design doc step 1 (approved) | create + template + tick + add + delete + list | create + template + tick + minimal list (add/delete move to week 2) | same as Current | same as Current
Order after midterm | pending | journal, weather, suggestions, last-hike copy, reminder, photos | weather, journal core, loop, polish, reminder | journal core, weather, loop, polish, reminder | unchanged
Fallback cut line | R3-21 pending | none | "brief-complete" = midterm + weather + journal core | same line, journal before weather | none
Behavior rules 1-19 | approved | unchanged | unchanged | unchanged | unchanged

Question: D1 — CEO-1: In what order do you build, and where is the line that must be done no matter what?
Header: Delivery
A) Risk-first tiers (recommended)
Midterm = brief's exact feature + minimal list. Then weather, journal core, loop, polish. Effort S (planning only), risk low. ✅ The riskiest brief bullet (network) lands while there is time to fix it ✅ A written cut line tells you what to drop under pressure ❌ Add/delete item wait until the week after the midterm
B) Design-doc order + cut line
Keep the approved order, add the same cut line. Effort S, risk medium. ✅ No change to the approved sequence ✅ Still gives a fallback line ❌ Network risk surfaces in week 3, with less time to recover
C) Keep as is
No cut line, approved order. Effort S, risk high. ✅ Nothing to relearn ✅ Midterm still includes add/delete ❌ Under time pressure nothing says what to drop first

## currentDecision (CEO-2)
Commitment comparison:
Commitment | Source/approval or pending | Current | A | B | C
Template source | Open Question 3 | undecided | base list + difficulty extras | one day-hike list | three full lists
Difficulty field effect | brief field | label only | changes the starting list | label only | changes the starting list

Question: D2 — CEO-2: What does a new trip's packing list start from?
Header: Templates
A) Base + difficulty extras (recommended)
One 10-item base list; Moderate and Hard add a few items. Effort S, risk low. ✅ Difficulty finally does something instead of being a label ✅ One pure function, easy to unit test ❌ You must pick sensible extras (draft in the plan)
B) One day-hike template
Same 10 items for every trip. Effort S, risk low. ✅ Least code ✅ Nothing to justify ❌ Difficulty is decorative, a weak answer in Q&A
C) Three full templates
Separate lists per difficulty. Effort M, risk low. ✅ Most tailored ❌ Three lists to maintain, mostly duplicated

## currentDecision (CEO-3)
Question: D3 — CEO-3: Keep the evening-before reminder?
Header: Reminder
A) Keep, last MVP slice (recommended)
Per-trip toggle, fires 18:00 the evening before. Effort M, risk medium. ✅ Covers week 9 (WorkManager, channels) ✅ Only feature that justifies a runtime permission ❌ Must cancel or reschedule on edit, delete and complete
B) Defer to TODOS.md
Not built in this project. Effort S (zero implementation). ✅ Saves about 2 days ✅ No permission flow ❌ Week 9 content and the brief's optional item are unused

## currentDecision (CEO-4)
Question: D4 — CEO-4: Photos on completed hikes?
Header: Photos
A) Defer to TODOS.md (recommended)
Not built now; logged with the storage note. Effort S (zero implementation). ✅ Not in the brief's MVP ✅ Avoids the picked-photo access trap (R3-19) ❌ Journal is text-only
B) Keep as stretch
Photo Picker, copy into app storage. Effort M, risk medium. ✅ Richer journal ❌ File handling and cleanup on delete add work

## Answered decisions (Step 0)

### CEO-1, approved: delivery tiers
- **P0 Midterm (by 2026-11-08):** create trip (route name, date, difficulty) with the template list; tick and untick; a minimal trip list as the entry point; trip detail; survives force-stop.
- **P1 Brief-complete (the cut line; must ship no matter what):** add and delete item (rules 10-11); area search and forecast card with all states (rules 15-18); Upcoming/Completed split (rules 1-2); Mark as done with notes and forgot flags plus its Undo (rules 4-5); Delete trip (rule 7).
- **P2 The loop:** suggestion chips (rule 19); start from last hike with carried-over items first (rules 9, 12); "Add something you forgot" (rule 5); same-route note (rule 14).
- **P3 Polish, dropped in this order if late:** Completed search (rule 13), Edit trip (rule 7), Edit journal and journal mode editing (rules 5-6), Move back to Upcoming (rule 6), reminder (CEO-3).
- Note: P3 order means search is the first thing dropped. Delete trip (P1) still gives cancelled hikes an exit if Edit trip is dropped.

### CEO-2, approved: templates
`templateFor(difficulty)`: base 10 items (Water (2 L), Snacks, Trail shoes, Sun hat, Sunglasses, First-aid kit, Phone + power bank, ID + cash, Small backpack, Trash bag). Moderate adds Lunch and Extra layer (12). Hard adds Lunch, Extra layer, Headlamp and Trekking poles (14). Order: base first, then extras as listed.

### CEO-3, approved: reminder (last P3 slice)
Per-trip toggle "Remind me the evening before" on the upcoming detail screen. Android 13+ asks POST_NOTIFICATIONS only when toggled on; denied keeps the toggle off with a one-line reason. Fires at 18:00 local time the day before the hike date: "Tomorrow: <route>. <n> items left to pack." When 18:00 the day before has passed, the toggle is disabled. Editing the date reschedules; Delete and Mark as done cancel. One unique WorkManager job per trip.

### CEO-4, approved: photos deferred
Logged in TODOS.md. Not built in this project.

## NOT in scope
- Photos on completed hikes: deferred by CEO-4 (TODOS.md).
- Live navigation, location tracking, emergency guidance, safety guarantees: brief scope boundary.
- Trail-data APIs: premise 3. *Changed 28 Nov:* the professor suggested a landmark map with trails, so Spec 2 child 2.18 adds it with OpenStreetMap trail data captured once and bundled (no live trail API calls, no device location). See Spec 2 decision 13.

## Current scope (before Section 1)
Mode: HOLD SCOPE (explicit user instruction). Accepted: CEO-1 tiers and cut line, CEO-2 templates, CEO-3 reminder as last P3 slice. Deferred: CEO-4 photos (TODOS.md). Rejected: none. Pending: CEO-5, CEO-6, CEO-7 below.

## Section 1: Architecture Review

```
MainActivity
  └─ SentieroNavHost (Navigation Compose)
       ├─ TripsScreen(Upcoming) ──┐
       ├─ TripsScreen(Completed) ─┴─ TripListsViewModel ─────┐
       ├─ TripFormScreen (new / edit) ── TripFormViewModel ──┤
       │     └─ AreaSearch (shared composable + state)       │
       └─ TripDetailScreen (upcoming / journal mode)         │
             ├─ ForecastCard, SuggestionChips                │
             ├─ MarkDoneSheet          TripDetailViewModel ──┤
             └─ ReminderToggle                               ▼
                         AppContainer (manual DI, created in SentieroApp)
                           ├─ TripRepository ── TripDao, ChecklistDao ── AppDatabase (Room)
                           ├─ WeatherRepository ── ForecastApi  (api.open-meteo.com)
                           │                    └─ GeocodingApi (geocoding-api.open-meteo.com)
                           ├─ ReminderScheduler ── WorkManager ── ReminderWorker ── NotificationManager
                           └─ today() provider
       Pure Kotlin (no Android): templateFor(), suggest(), forecastCardState(), reminderTime()
```

State machines:
```
TRIP:  UPCOMING(before date) --date arrives--> UPCOMING(due) --Mark as done--> COMPLETED
         ^  Edit date, any time                                  |  Undo / Move back
         |_______________________________________________________|  (back to UPCOMING, due)
       any state --Delete--> gone (items cascade; reminder cancelled)
       Blocked: Mark as done before the hike date (button hidden, rule 4); Edit on COMPLETED (rule 7)

AREA SEARCH:  Idle --submit--> Loading --> Results --pick--> Picked
                                       --> Empty | Error --Retry--> Loading
              Picked --user edits text--> Idle (pick cleared)          [copy: design review, R3-2]

FORECAST CARD: decided locally first (rule 18.1-18.4); only 18.5 requests:
              Loading --> Content | Error --Retry--> Loading;  key = (lat, lon, date)

REMINDER:     Off --toggle on--> [API 33+: ask permission] --granted--> Scheduled
                                                         --denied---> Off + reason
              Scheduled --edit date--> rescheduled (or Off if too late)
              Scheduled --delete / Mark as done--> cancelled
```

Findings:
- **WARNING A1 (owner: engineering review):** "today" drives rules 4, 18, the Upcoming labels and the reminder. If each class calls `LocalDate.now()`, rule tests cannot pin a date and demo recipes depend on the real calendar. Remedy direction: one today provider in AppContainer; pure functions take `today` as a parameter.
- **WARNING A2 (owner: engineering review):** reminder side effects must run on every path that changes a trip: delete, Mark as done, date edit. Spread across ViewModels, one path will be missed. Remedy direction: one place owns "trip changed → reminder updated".
- **OK:** Open-Meteo is a single point of failure for weather only; rule 17 keeps the rest of the app working offline.
- **OK:** no auth boundary; data is local; both endpoints are read-only GETs.
- Scaling: single user, local. 1,000 trips / 15,000 items is fine for Room + LazyColumn. Nothing breaks first at this scale.
- Production failures: geocoding down → rule 16 Error + Retry; forecast down → rule 18.5 Error + Retry; WorkManager delayed by Doze → see CEO-5; Room schema change without migration → crash on launch (R3-24, owner: engineering review).
- Rollback: reinstall the last good debug APK (see Section 9 task: tag the midterm build). Local data survives unless a destructive migration runs.
- Artifact value test: `docs/plan.md`, the design doc and the coming specs are read by the student while building and by the next reviews; they retire when the app ships.

## Section 2: Error & Rescue Map

```
METHOD/CODEPATH                    | WHAT CAN GO WRONG                         | EXCEPTION CLASS
-----------------------------------|-------------------------------------------|------------------------------
WeatherRepository.searchPlaces(q)  | no network / timeout                      | IOException (SocketTimeoutException)
                                   | HTTP 4xx/5xx                              | retrofit2.HttpException
                                   | malformed JSON                            | SerializationException (converter-specific)
                                   | no match: body has NO "results" key       | parse failure if `results` is non-null  ← verified live
                                   | screen closed mid-request                 | CancellationException (must propagate)
WeatherRepository.forecast(...)    | no network / timeout                      | IOException
                                   | date outside the API window (TZ edge)     | HttpException 400 [static docs; unverified live]
                                   | daily value is null                       | NPE / parse failure if fields non-null
TripRepository writes (Room)       | constraint (item for a deleted trip)      | SQLiteConstraintException
                                   | disk full                                 | SQLiteFullException
MarkDone save (several writes)     | process dies between writes               | (partial state, no exception)
ReminderWorker.doWork              | trip deleted / completed since scheduling | (no row)
                                   | permission revoked after scheduling       | SecurityException on notify (API 33+)
Notification tap PendingIntent     | missing FLAG_IMMUTABLE                    | IllegalArgumentException on API 31+

EXCEPTION / CASE                   | RESCUED? | RESCUE ACTION                               | USER SEES
-----------------------------------|----------|---------------------------------------------|---------------------------------------
IOException (search)               | Y        | Error state + Retry (rule 16)               | "Couldn't search right now." + Retry
HttpException / parse (search)     | Y        | same as above                               | same
No "results" key                   | N ← GAP  | `results` nullable, null/empty → Empty      | must be "No places found for ..." (rule 16)
IOException (forecast)             | Y        | Error + Retry (rule 18.5)                   | copy undefined (R3-16, design review)
HttpException 400 (forecast)       | Y        | Error "No forecast for this date yet."      | that text, Retry hidden or harmless
Null daily value                   | N ← GAP  | nullable fields; dash; rule not triggered   | "–" for that value (R3-5)
CancellationException              | N ← GAP  | never caught by a catch-all; rethrow        | nothing (correct)
SQLite write failures              | N ← GAP  | catch in ViewModel, keep form/sheet state   | "Couldn't save. Try again." snackbar
Partial MarkDone                   | N ← GAP  | one Room @Transaction for the sheet save    | all-or-nothing
Worker: trip gone / completed      | Y        | return success, post nothing                | nothing (correct)
Worker: permission revoked         | N ← GAP  | check permission before notify; skip + log  | nothing (user turned it off)
PendingIntent flags                | N ← GAP  | FLAG_IMMUTABLE                              | otherwise crash on tap
```
- **CRITICAL GAP E1:** if the geocoding DTO declares `results` as non-null, every "no match" search (live: `{"generationtime_ms":0.17}` for "zzqx") becomes Error instead of the brief-required Empty state. Covered by approved rule 16; the remedy is determined (nullable `results`, null → Empty). Owner: the engineering review and the specs. No new approval needed.
- GAPs E2-E6 above have determined remedies inside approved rules (no silent failure, rule 5's save, CEO-3's reminder). Owner: the engineering review.
- Catch-all smell: a ViewModel `catch (e: Exception)` swallows `CancellationException` and breaks coroutine cancellation. Catch IOException, HttpException and the converter's parse exception by name.

## Section 3: Security & Threat Model
| Threat | Likelihood | Impact | Mitigated? |
|---|---|---|---|
| Cleartext HTTP to the APIs | Low | Low | Yes if base URLs are `https://` (Android blocks cleartext by default) |
| SQL injection through search text | Low | Low | Yes: Room binds `:query`; `%` and `_` in user text act as wildcards (harmless) |
| Very long route name / notes / labels breaking layout | Med | Low | No → cap lengths (route 80, label 60, notes 2,000) and ellipsize; owner: design review + specs |
| Arabic / RTL route names | Med | Low | Compose handles bidi; geocoding `language=en` returns English names; verify with an Arabic query (design review) |
| Secrets in repo | None | — | No API key exists |
| Dependency risk | Low | Low | Retrofit, OkHttp, Room, Navigation, WorkManager, Lifecycle, a JSON converter: all Google/Square maintained |
| Personal data leaving the device | Low | Low | Only the typed area query and a village's coordinates + date go to Open-Meteo; no device location |
No High-severity findings.

## Section 4: Data Flow & Interaction Edge Cases

```
CREATE TRIP: INPUT form ─▶ VALIDATE (name non-blank, date, difficulty) ─▶ TRANSFORM (trim; templateFor or copy last hike)
             ─▶ PERSIST (trip + items, one transaction) ─▶ OUTPUT (navigate; destination R3-17)
  nil/empty: Save disabled · too long: cap (Section 3) · double Save: TWO TRIPS ← GAP · DB error: E-row above
  stale: last hike deleted between form open and Save → copy finds no source ← GAP (fall back to template, say so)

TICK ITEM:   Checkbox(newValue) ─▶ VM.setPacked(id, newValue) ─▶ DAO UPDATE ─▶ Flow emits ─▶ recomposition

AREA SEARCH: submit(q) ─▶ Loading ─▶ GET /v1/search ─▶ Results | Empty | Error
  stale: submit "Bsh", then "Bsharri" quickly; responses return out of order → old results shown ← GAP

FORECAST:    rule 18 local decision ─▶ (18.5 only) GET /v1/forecast ─▶ Loading ─▶ Content | Error
  rotation: VM survives, no refetch · leave screen: viewModelScope cancels · Retry during Loading: ignored
```

Async ordering, tick item. Invariant: after taps stop, the stored `isPacked` equals what the checkbox last showed.
```
 time | tap A                   | tap B                   | Room (single writer)
  t0  | UI shows false          |                         | false
  t1  | write(true) queued      |                         |
  t2  |                         | UI STILL false (no emit)| 
  t3  |                         | write(true) queued      |
  t4  |                         |                         | true, then true → UI true (user meant false)
```
Room runs writes on a serial executor, so writes land in launch order; the risk is only the sub-50 ms window before the Flow re-emits. Accept it with absolute writes (`setPacked(id, value)`, never `!current`), and test two rapid events in a ViewModel test. Area search ordering: the ViewModel cancels the previous search job before starting a new one, so only the latest query can produce Results.

| Interaction | Edge case | Handled? | How |
|---|---|---|---|
| Save trip | double tap | **No ← GAP** | disable Save while saving; VM ignores a second event |
| Save trip | rotation mid-form | Yes | form state in VM / rememberSaveable |
| Start from last hike | source deleted meanwhile | **No ← GAP** | fall back to the template and show "Last hike was deleted; started from basics" |
| Start from last hike | source has 0 items | **No ← GAP** | empty checklist state (design review) |
| Delete item + Undo | another item added meanwhile | Yes | re-insert with old id and position; order by position, then id |
| Mark as done | dismiss sheet without Save | Unclear (R3-28) | design review |
| Mark as done | "Add something you forgot" duplicates a label | **No (R3-32)** | flag the existing item instead; owner: engineering review |
| Area search | out-of-order responses | **No ← GAP** | cancel previous job (above) |
| Forecast | airplane mode, then back online | Yes | Error + Retry |
| Reminder | toggled on for a trip tomorrow after 18:00 | Yes | toggle disabled (CEO-3) |
| Reminder | trip deleted with job pending | Yes | cancel on delete (A2); worker also skips a missing trip |

## Section 5: Code Quality Review
- One function per behavior, reused: Undo and Move back both call one `reopenTrip(id)`; "+ Add item", suggestion chips and "Add something you forgot" all call one `addItem(tripId, label, forgotten)` that appends at max position + 1 (also fixes R3-30's missing position rule).
- Rule 18 and rule 19 are branch-heavy: implement as pure, table-driven functions (`forecastCardState(...)`, `suggest(daily, labels)`), not `if` chains inside composables.
- New trip and Edit trip share one screen and ViewModel with a mode argument.
- **WARNING Q1 (owner: design review):** the UI mixes "trip" and "hike" ("Upcoming trips", "New trip", "Last hike", "Completed hikes"). Pick one word for the UI. Code uses `Trip` throughout.
- Over-engineering avoided: no use-case layer, no Hilt, no modules (premise 2).

## Section 6: Test Review
```
NEW THING                          | TYPE              | HAPPY                       | FAILURE / EDGE
-----------------------------------|-------------------|-----------------------------|---------------------------------------------
templateFor(difficulty)            | Unit (JVM)        | Easy 10, Moderate 12, Hard 14, order | —
suggest(daily, labels)             | Unit (JVM)        | each table row fires        | thresholds at boundary (49/50, 5/5.1); null value never fires; synonym suppression; max 3
forecastCardState(trip, today)     | Unit (JVM)        | 18.5 → request              | completed, past date, no area, today+15 vs +16
reminderTime(date, now)            | Unit (JVM)        | 18:00 day before            | now after 18:00 day before → null (too late)
Trip/Checklist DAO queries         | Instrumented      | Upcoming/Completed order, progress counts | cascade delete; carried-over copy order
TripDetailViewModel                | Unit (JVM, fake repo) | tick writes absolute value | two rapid ticks; Mark as done Undo
WeatherRepository mapping          | Unit (JVM, fake API) | DTO → domain            | no "results" key → Empty; IOException → Error; HTTP 400 → no-forecast
Restart test (midterm)             | Manual on emulator| create, tick 3, force-stop, reopen | —
Full loop (final)                  | Manual on emulator| Chouf done → Jabal Moussa from last hike → headlamp first | airplane mode on detail
```
- 2am-Friday test: the full-loop manual script plus the restart test.
- Hostile QA test: double-tap Save; search "Tripoli" and check that Libya is not picked silently; date = today + 15 and today + 16.
- Flakiness: any test reading the real date is flaky → inject today (A1). Never call Open-Meteo from tests; use a fake API.
- Pyramid: many JVM unit tests (pure functions, VM), a few instrumented DAO tests, two manual scripts. Not inverted.
- Test-method choices (fake API vs MockWebServer, Robolectric vs instrumented DAO tests) are open for the engineering review.

## Section 7: Performance Review
- **WARNING P1 (owner: engineering review):** Upcoming cards show "6 of 11 packed". Counting items per trip in a loop is N+1. One query returning trips with packed/total counts avoids it.
- Index `checklist_items.tripId` (Room warns on an unindexed foreign key).
- Forecast is fetched when the detail screen opens; small payload, no cache needed. Geocoding runs on submit only.
- Slowest paths: forecast and geocoding requests (network bound, roughly 0.3-2 s [estimated]); everything else is local.

## Section 8: Observability & Debuggability Review
- Logcat tags per layer (`Sentiero/Weather`: request params and outcome; `Sentiero/Reminder`: scheduled or cancelled, trip id, fire time; `Sentiero/Db`: write failures).
- The "dashboards" for a local app are Android Studio's App Inspection: Database Inspector (live Room tables) and Background Task Inspector (WorkManager jobs).
- **Demo use:** during the midterm Q&A, tick an item with the Database Inspector open and show the `is_packed` cell change. That shows the state and data flow live instead of only describing it.
- Runbook: forecast shows Error → Logcat `Sentiero/Weather`; reminder did not fire → Background Task Inspector.

## Section 9: Deployment & Rollout Review
- Room migrations: R3-24 is open for the engineering review (full schema from day one vs migrations vs destructive fallback).
- Keep `main` runnable at every commit; tag the midterm demo commit (`midterm-demo`) and keep its debug APK as the rollback build.
- Pre-demo smoke test = the midterm script (R3-17, owner: specs) run on a fresh emulator install.
- Environment parity: test the reminder on an API 33+ emulator (permission prompt) and one below 33 (no prompt).
- No feature flags needed: the tiers are the rollout plan, and P2/P3 work only merges when it runs.

## Section 10: Long-Term Trajectory Review
- Debt: manual DI (fine at this size); possible destructive migrations (eng decision); text-only journal (TODOS.md).
- Path dependency: the Room schema is the only hard-to-change part. Reversibility 4/5.
- Knowledge: design doc + this plan + specs give a new reader the "why" for each layer. The 1-year question passes if the specs stay current.

## Section 11: Design & UX Review

```
 [Upcoming] --FAB--> [New trip form] --Save--> [Trip detail (upcoming)] --Mark as done (from date)--> [Sheet]
     ^  \--card------------------------------------^        |  \-- Add area --> [Area search]          |
     |                                                       |                                         v
     |                                            [Trip detail (journal)] <------------ Save + Undo snackbar
 [Completed] --card------------------------------------------^   --Edit journal--> [Sheet]
```

| Feature | Loading | Empty | Error | Success | Partial |
|---|---|---|---|---|---|
| Upcoming list | brief | defined | n/a | list | — |
| Completed list | brief | **undefined (R3-23)** | n/a | list | search no-match **undefined (R3-11)** |
| Trip form | — | — | Save disabled; no inline hints defined | **destination undefined (R3-17)** | area typed, not picked (R3-2) |
| Area search | defined | defined | defined | defined | — |
| Forecast card | defined | (no-row → Error) | generic copy **undefined (R3-16)** | defined | null value → dash (R3-5) |
| Checklist | — | **undefined: zero items** | write failure (Section 2) | defined | — |
| Mark-as-done sheet | — | first completion empty | save failure (Section 2) | where you land **undefined (R3-31)** | dismiss (R3-28) |
| Reminder toggle | permission prompt | — | denied defined | scheduled confirmation copy undefined | too late defined |

- Emotional arc: plan (anticipation) → pack (progress bar fills) → hike day ("Today") → write notes (reflection) → next plan ("forgot last time" is the delight moment).
- Generic-look risk: a stock Material list-of-cards app. the design-system session is next in your pipeline and sets the identity.
- Accessibility: 48 dp targets (your Material slides); TalkBack labels like "Headlamp, not packed, forgot last time"; content descriptions on ✕ icons; font scaling.
- Recommendation: run the design review (already in your pipeline) for the undefined states above.

## Pending decisions (Sections 1-11)

| ID and owner | Contract and evidence | Current | Proposed | Status | Exact approval and scope |
|---|---|---|---|---|---|
| CEO-5 reminder timing (user) | CEO-3 says "fires 18:00"; WorkManager (week 9) may delay work under Doze; exact alarms need a special permission | "Fires at 18:00" | A) WorkManager, promise "around 18:00" | approved | User chose "Around 18:00 (recommended)" in D5. Scope: reminder timing wording and mechanism; CEO-3 otherwise unchanged |
| CEO-6 default chip (user) | R3-1; rule 12 | Template preselected only when it is alone | A) "Last hike" preselected whenever it exists | approved | User chose "Last hike (recommended)" in D6. Scope: rule 12 default only; exactly one chip always selected |
| CEO-7 same-route note (user) | R3-9; rule 14 exact match vs "any repeat" claim | Claim says any repeat; rule needs identical names | A) Narrow the claim to the same route name | approved | User chose "Same route name only (recommended)" in D7. Scope: rule 14 unchanged; the "any repeat" claim in What Makes This Cool narrows to "the same route name" |

## currentDecision (CEO-5)
Question: D5 — CEO-5: How exact must the reminder time be?
Header: Reminder time
A) Around 18:00 (recommended)
WorkManager one-time job with an initial delay; Android may delay it a little. Effort S, risk low. ✅ Week 9 tool, no extra permission ✅ Survives reboot ❌ Can arrive minutes late under battery saving
B) Exactly 18:00
AlarmManager exact alarm + SCHEDULE_EXACT_ALARM special permission. Effort M, risk medium. ✅ Precise time ❌ A second permission that is hard to justify for a packing nudge ❌ Not the week 9 tool

## currentDecision (CEO-6)
Question: D6 — CEO-6: When both chips show on the new-trip form, which starts selected?
Header: Default list
A) Last hike (recommended)
The loop is the default; one tap switches to basics. Effort S, risk low. ✅ The demo path needs no extra tap ✅ Forgotten items reach the next trip by default ❌ A Hard hike after an Easy one starts without Headlamp unless you switch
B) Template for the difficulty
Basics by default; one tap for last hike. Effort S, risk low. ✅ Always matches the chosen difficulty ❌ Users who never tap the chip never see the loop

## currentDecision (CEO-7)
Question: D7 — CEO-7: When does "You hiked this on ..." appear?
Header: Same route
A) Same route name only (recommended)
Exact match (case-insensitive, trimmed); the doc stops promising "any repeat". Effort S, risk low. ✅ Honest and tiny ✅ No new UI ❌ "Jabal Moussa" vs "Jabal Moussa Reserve" won't match
B) Suggest past route names
The route field offers names of completed hikes as you type. Effort M, risk medium. ✅ Repeats match reliably ❌ Dropdown UI work in P2

## TODO choices
HOLD SCOPE: no new TODO proposals. The only deferral is photos (CEO-4), already in TODOS.md.

Approval readiness: PASS — CEO-1 (D1 "Risk-first tiers (recommended)"), CEO-2 (D2 "Base + difficulty extras (recommended)"), CEO-3 (D3 "Keep, last MVP slice (recommended)"), CEO-4 (D4 "Defer to TODOS.md (recommended)", deferral), CEO-5 (D5 "Around 18:00 (recommended)"), CEO-6 (D6 "Last hike (recommended)"), CEO-7 (D7 "Same route name only (recommended)"). Each applied only within its stated scope.

## Review facts
- Mode: HOLD SCOPE. Status: clean (0 unresolved choices in this review's ledger; 3 critical gaps found, all turned into determined tasks inside approved rules).
- Routed to the engineering review (pending, not CEO decisions): A1 single today provider; A2 one owner for trip-change side effects (reminder); P1 N+1 progress counts + `tripId` index; R3-24 schema/migrations; test-method choices (fake API vs MockWebServer, Robolectric vs instrumented DAO tests); R3-3 search trigger; R3-4 `timezone=auto`; R3-7 synonym table; R3-8 `sourceTripId`; R3-22 where tests run; R3-29/R3-30/R3-32 sheet-added items.
- Routed to the design review: Completed empty and no-match states (R3-23, R3-11); search position (R3-12); checklist zero-items state; Save destination (R3-17); where Mark as done lands (R3-31); sheet dismiss (R3-28); area field states (R3-2); UV on card (R3-6); empty-notes note (R3-10); forecast error copy (R3-16); trip/hike vocabulary (Q1); input length caps and RTL names (Section 3); reminder confirmation copy.

## Reality rows
REALITY: premise-table finding unverified=1 (Navigation Compose not in uploaded slides; class coverage unknown) docs/designs/hiking-trip-planner.md:30
REALITY: already-done pass no commits on main yet docs/plan.md:9
REALITY: surface-check pass geocoding `/v1/search` live-checked (no-match body has no "results" key; "Tripoli" returns Libya first); forecast params static-checked only (endpoint blocks automated fetch) docs/plan.md:186
REALITY: binding-decisions pass all 2026-11-01 user decisions honored docs/plan.md:9
REALITY: numbers pass 16-day horizon [measured: Open-Meteo README]; thresholds, caps and counts [estimated: product choices] docs/designs/hiking-trip-planner.md:89
REALITY: deferred-asks pass no linked issue docs/designs/hiking-trip-planner.md:37
REALITY: pr-count pass solo, commits to main, no PR queue docs/plan.md:1

## NOT in scope (final)
- Photos on completed hikes: deferred (D4, "Defer to TODOS.md (recommended)"); not in the brief's MVP; storage trap. In TODOS.md.
- Exact-time reminders: rejected (D5 chose "around 18:00"); needs a second special permission.
- Route-name suggestions for the same-route note: rejected (D7 chose exact names only).
- Live navigation, location tracking, emergency guidance, safety guarantees, trail-data APIs: brief scope boundary and premise 3. *Changed 28 Nov:* bundled OSM trail lines on a landmark map are in scope (Spec 2, 2.18); live trail-data API calls and device location stay out.

## What already exists
- Week 6 "My Tasks" (Room entity, DAO, repository, ViewModel `stateIn`): reused as the shape of trips and checklist items.
- Week 7 "Campus Events feed" (Retrofit, DTO mapping, Loading/Content/Empty/Error + Retry, failure exercise): reused for geocoding and forecast.
- Week 8 MVVM workshop (sealed UiState, `collectAsStateWithLifecycle`): reused for every screen.
- Week 9 slides (WorkManager, notification channels): reused for the reminder; no workshop code to copy.
- Nothing exists for Navigation Compose in the course material.

## Dream state delta
After this plan ships, the app is a complete course MVP with the loop: about 80% of the 12-month ideal's core (planning, packing, journal, memory between hikes). Missing for the ideal: photos, sharing lists with friends, export, and polish for daily real-world use.

## Failure Modes Registry
```
CODEPATH                      | FAILURE MODE                         | RESCUED?      | TEST?   | USER SEES?                     | LOGGED?
------------------------------|--------------------------------------|---------------|---------|--------------------------------|--------
searchPlaces                  | no network / HTTP error / bad JSON   | Y             | planned | "Couldn't search right now."   | planned
searchPlaces                  | no "results" key (no match)          | planned (T1)  | planned | Empty copy (was: wrong Error)  | planned
forecast                      | no network                           | Y             | planned | Error + Retry                  | planned
forecast                      | HTTP 400 out of window               | Y             | planned | "No forecast for this date yet." | planned
forecast                      | null daily value                     | planned (T1)  | planned | "–"                            | n/a
any ViewModel catch           | CancellationException swallowed      | planned (T2)  | planned | nothing (correct after fix)    | n/a
Room writes                   | constraint / disk full               | planned (T2)  | no      | "Couldn't save. Try again."    | planned
Mark-as-done save             | partial write on process death       | planned (T3)  | planned | all-or-nothing                 | n/a
Save trip                     | double tap → two trips               | planned (T4)  | planned | one trip                       | n/a
Area search                   | out-of-order responses               | planned (T5)  | planned | latest results only            | n/a
Start from last hike          | source deleted meanwhile             | planned (T6)  | planned | basics + one-line notice       | n/a
Tick item                     | fast double tap                      | accepted (T7) | planned | correct within ~50 ms window   | n/a
ReminderWorker                | trip gone / done                     | Y             | planned | nothing (correct)              | planned
ReminderWorker                | permission revoked                   | planned (T10) | no      | nothing (user's choice)        | planned
Notification tap              | PendingIntent without FLAG_IMMUTABLE | planned (T10) | no      | crash on API 31+ before fix    | n/a
```
Critical gaps found in review (RESCUED N, TEST N, silent): 3 (CancellationException swallow, partial Mark-as-done save, wrong Empty/Error mapping). All three now map to determined tasks T1-T3; 0 remain open.

## Diagrams produced
System architecture, state machines (trip, area search, forecast card, reminder), data flows with shadow paths, tick-item async schedule, error/rescue map, user flow. Deployment/rollback is a two-step text plan (Section 9) for a sideloaded debug APK; no diagram adds value.

## Stale Diagram Audit
Only diagram in touched files: `docs/wireframe-core-flow.png`. Still accurate except two pending items for the design review: the Completed search box position (R3-12) and the trip/hike vocabulary (Q1). The "any repeat" wording is not in the wireframe.

## Implementation Tasks

- [ ] **T1 (P1, human: ~1h / CC: ~5min)** — Weather data — Make `results` and every daily value nullable; map null/empty results to Empty and null values to "–" without firing rules
  - Surfaced by: Section 2 — E1 (live: no-match body is `{"generationtime_ms":...}`) and R3-5
  - Files: to be determined (weather DTOs, WeatherRepository)
  - Verify: unit test with the live no-match body → Empty; a fixture with `uv_index_max: [null]` → no Sunscreen chip
- [ ] **T2 (P1, human: ~1h / CC: ~5min)** — ViewModels — Catch IOException, HttpException and the converter's parse exception by name; never `catch (e: Exception)`; Room write failures show "Couldn't save. Try again." and keep input
  - Surfaced by: Section 2 — catch-all smell, SQLite write failures
  - Files: to be determined (all ViewModels)
  - Verify: grep for `catch (e: Exception)` returns nothing; ViewModel test cancels its scope mid-request without a crash
- [ ] **T3 (P1, human: ~1h / CC: ~5min)** — Mark as done — Save notes, flags, added items and completedAt in one Room transaction
  - Surfaced by: Section 2 — partial MarkDone save
  - Files: to be determined (TripDao / TripRepository)
  - Verify: instrumented DAO test asserts all-or-nothing
- [ ] **T4 (P1, human: ~30min / CC: ~5min)** — Trip form — Disable Save while saving; ignore a second submit
  - Surfaced by: Section 4 — double tap creates two trips
  - Files: to be determined (TripFormViewModel, TripFormScreen)
  - Verify: ViewModel test sends Save twice → exactly one trip
- [ ] **T5 (P2, human: ~1h / CC: ~5min)** — Area search — Cancel the previous search job before starting a new one
  - Surfaced by: Section 4 — out-of-order responses
  - Files: to be determined (area search state holder)
  - Verify: test with a fake API that pauses query A, releases B then A → only B's results shown
- [ ] **T6 (P2, human: ~30min / CC: ~5min)** — Start from last hike — Fall back to the difficulty template with a one-line notice when the source trip is gone
  - Surfaced by: Section 4 — stale source
  - Files: to be determined (TripRepository create path)
  - Verify: repository test deletes the source before Save → template items + notice flag
- [ ] **T7 (P1, human: ~30min / CC: ~5min)** — Checklist — Write absolute values (`setPacked(id, value)`), never `!current`
  - Surfaced by: Section 4 — tick-item async schedule
  - Files: to be determined (TripDetailViewModel, ChecklistDao)
  - Verify: ViewModel test with two rapid events ends in the last shown value
- [ ] **T8 (P1, human: ~15min / CC: ~2min)** — Release — Tag the midterm demo commit `midterm-demo` and keep its debug APK
  - Surfaced by: Section 9 — rollback build
  - Files: none (git tag, APK archive)
  - Verify: `git tag --list midterm-demo` prints the tag; the APK installs on a clean emulator
- [ ] **T9 (P1, human: ~1h / CC: n/a)** — Midterm rehearsal — Practice the scope defense (brief as contract, weather instead of trail data, what is out of scope) and the data-flow explanation with Database Inspector open
  - Surfaced by: Section 8 — demo use; reviewer concern R3-26
  - Files: none
  - Verify: timed run-through under 5 minutes, including one live tick shown in Database Inspector
- [ ] **T10 (P2, human: ~30min / CC: ~5min)** — Reminder — Check notification permission in the worker before posting; use FLAG_IMMUTABLE on the tap PendingIntent; Logcat tag `Sentiero/Reminder`
  - Surfaced by: Section 2 — revoked permission, PendingIntent crash; Section 8
  - Files: to be determined (ReminderWorker)
  - Verify: API 33+ emulator: revoke permission after scheduling → no crash, log line present
- [ ] **T11 (P2, human: ~30min / CC: ~5min)** — Logging — Logcat tags `Sentiero/Weather` and `Sentiero/Db` for requests, outcomes and write failures
  - Surfaced by: Section 8 — debuggability
  - Files: to be determined (repositories)
  - Verify: airplane-mode forecast produces one Weather log line with the cause

Effort ratios assumed: features ~30x, tests ~50x, process tasks not compressible.

## Completion Summary
```
  +====================================================================+
  |            MEGA PLAN REVIEW — COMPLETION SUMMARY                   |
  +====================================================================+
  | Mode selected        | HOLD SCOPE                                  |
  | System Audit         | Greenfield: no commits; docs only; design   |
  |                      | doc approved; 3 binding user decisions      |
  | Step 0               | Tiers + cut line, templates, reminder kept, |
  |                      | photos deferred                             |
  | Section 1  (Arch)    | 2 issues found (routed to eng)              |
  | Section 2  (Errors)  | 15 error paths mapped, 7 GAPS               |
  | Section 3  (Security)| 2 issues found, 0 High severity             |
  | Section 4  (Data/UX) | 11 edge cases mapped, 5 unhandled           |
  | Section 5  (Quality) | 1 issue found                               |
  | Section 6  (Tests)   | Diagram produced, 2 gaps (test methods)     |
  | Section 7  (Perf)    | 1 issue found                               |
  | Section 8  (Observ)  | 2 gaps found                                |
  | Section 9  (Deploy)  | 2 risks flagged                             |
  | Section 10 (Future)  | Reversibility: 4/5, debt items: 3           |
  | Section 11 (Design)  | 9 issues (routed to design review)          |
  +--------------------------------------------------------------------+
  | NOT in scope         | written (4 items)                           |
  | What already exists  | written                                     |
  | Dream state delta    | written                                     |
  | Error/rescue registry| 12 rows, 3 CRITICAL GAPS (all now tasks)    |
  | Failure modes        | 15 total, 0 CRITICAL GAPS open              |
  | TODOS.md updates     | 1 item (photos, from Step 0)                |
  | Scope proposals      | 0 proposed, 0 accepted (HOLD SCOPE)         |
  | CEO plan             | skipped by mode                             |
  |                      | no background tasks for native fallback)    |
  | Lake Score           | N/A (no coverage-scored questions)          |
  | Diagrams produced    | 7 (architecture, 4 state machines, data     |
  |                      | flow, async schedule, user flow)            |
  | Stale diagrams found | 0 (2 pending items for design review)       |
  | Unresolved decisions | 0                                           |
  | Reality rows         | 7/7 rows (PLAN_REALITY below), 1 unverified |
  +====================================================================+
```

### Unresolved Decisions
None. Items routed to the engineering review and the design review are listed under Review facts with their owners.

PLAN_REALITY: phase=ceo rows=7/7 missing=none unverified=1 verdict=complete

## Design review, 2026-11-01

Target : the plan's trip form, Upcoming list, Completed list, trip detail, and loading/error states. Calibrated against `DESIGN.md` (approved today) and the approved design preview (`docs/design-preview/`). The first design session wireframe (`docs/wireframe-core-flow.png`) predates DESIGN.md.
Mockups: this review uses the approved HTML preview as visual evidence.

### Step 0
- **Initial design completeness: 5/10.** The behavior rules are unusually complete (19 rules, state machines, a suggestion table) and DESIGN.md exists, but the plan never says what several screens show when they are empty, failing or half-filled, where you land after Save and Mark as done, which word the UI uses (trip or hike), and the wireframe contradicts DESIGN.md (cards, emoji).
- **A 10 for this plan:** every screen has a named first/second/third, every list and panel has its loading/empty/error/partial state written as visible copy, every text field has limits and long/Arabic behavior, TalkBack and font scaling are specified, and every plan reference uses DESIGN.md tokens.
- **DESIGN.md:** present; all decisions calibrate against it.
- **Existing leverage:** DESIGN.md components (list-row, memory-tag, forecast-panel, suggestion-chip, bottom-sheet, snackbar, nav-bar); approved preview screens (Upcoming, Trip detail, Mark-as-done sheet).

### Design review decisions (pending until answered)

| ID | Issue | Decision (user answer) | Status |
|---|---|---|---|
| DR-1A | Mixed trip/hike wording | "'Hike' + tabs Upcoming/Completed (recommended)": UI says hike everywhere (New hike, Last hike, Mark as done); bottom tabs "Upcoming" and "Completed"; code keeps `Trip` | approved |
| DR-1B | Detail screen order | "Memory, forecast, packing; Done pinned (recommended)" | approved |
| DR-1C | Landing after Save / Mark as done | "Stay with the hike (recommended)" | approved |
| DR-2A | Completed empty + search | "Teach empty + search on top (recommended)" | approved |
| DR-2B | Checklist with zero items | "Prompt + Add field (recommended)" | approved |
| DR-2C | Forecast loading / error | "Labeled loading + neutral error (recommended)" | approved |
| DR-2D | Area field honesty | "Honest field + clear button (recommended)" | approved |
| DR-2E | Sheet dismiss | "Confirm only if changed (recommended)" | approved |
| DR-2F | Reminder row copy | "State-by-state copy (recommended)" | approved |
| DR-3A | Loop payoff invisible at "Anything you forgot?" | "One helper line (recommended)" | approved |
| DR-4A | Wireframe shows cards + emoji (hard rejection 7) | "Preview wins; sketch = flow only (recommended)" | approved |
| DR-5A | No component / icon map | "Stock M3 + named icons (recommended)" | approved |
| DR-6A | No text limits; long and Arabic names | "Caps + wrap rules (recommended)" | approved |
| DR-6B | No accessibility spec | "Checkable a11y spec (recommended)" | approved |
| DR-6C | Rotation undecided | "Support it, same layout (recommended)" | approved |
| DR-7A | Completed row content undefined | "Route, date, 2 lines of notes, forgot tag (recommended)" | approved |
| DR-7B | "You hiked this" with empty notes | "Date only, still linked (recommended)" | approved |

### Pass 1: Information Architecture — 5/10 → 9/10
Navigation (DR-1A, DR-1C):
```
 Bottom bar:  [Upcoming]  [Completed]           FAB "New hike" on Upcoming only
   Upcoming ──row──▶ Hike detail (upcoming mode) ──⋮──▶ Edit hike / Delete
      │                   │ from hike date: pinned [Mark as done] ─▶ Sheet ─Save─▶ same screen, journal mode + Undo
      └─FAB─▶ New hike ─Save─▶ Hike detail of the new hike   (form removed from back stack; Back → Upcoming)
   Upcoming row "Mark as done" ─▶ Sheet ─Save─▶ row leaves Upcoming + Undo snackbar on Upcoming
   Completed ──row──▶ Hike detail (journal mode) ──"Edit journal"─▶ Sheet
```
Screen hierarchy (first / second / third):
- **Upcoming:** (1) the soonest hike row (past-dated "date passed" rows sit on top with "Mark as done"), (2) its packing progress, (3) the FAB. Row = route name `title` · difficulty tag + date `body-small` · 3dp progress line · "6 of 12 packed".
- **Hike detail, upcoming mode (DR-1B):** title (route, `headline`) + "Sat 14 Nov · Moderate" → "You hiked this on …" note (only when a same-name completed hike exists) → forecast panel → "Packing · 6 of 12" + progress line + checklist + "+ Add item" → reminder row. From the hike date: a pinned full-width "Mark as done" button at the bottom, the screen's only filled action. Before the date: no button.
- **Hike detail, journal mode:** title + date/difficulty → notes (`body`) → "Edit journal" text button → read-only checklist with "Forgot" tags. No forecast, no add, no reminder, no pinned button.
- **New / Edit hike form:** Route name → Date → Difficulty → Area (optional) → "Packing list starts from" chips (new only) → Save (filled, bottom).
- **Completed:** (1) search (only with ≥ 3 hikes), (2) newest hike row, (3) its notes preview.
- Copy changes from DR-1A: "Upcoming trips" → "Upcoming"; "New trip" → "New hike"; "Save trip" → "Save hike"; "Completed hikes" → "Completed"; "No trips planned. Plan your next hike." → "No hikes planned. Plan your next one." + button "New hike".

### Pass 2: Interaction State Coverage — 4/10 → 9/10
```
 FEATURE            | LOADING                     | EMPTY                                   | ERROR                                   | SUCCESS                   | PARTIAL
 -------------------|-----------------------------|-----------------------------------------|-----------------------------------------|---------------------------|-------------------------------
 Upcoming list      | nothing (Room is instant)   | "No hikes planned. Plan your next one." | n/a (local)                             | rows                      | —
                    |                             | + "New hike" button                     |                                         |                           |
 Completed list     | nothing                     | "No completed hikes yet. When a hike is | n/a                                     | rows                      | search no match: 'No hikes match
                    |                             | over, open it and tap Mark as done: your|                                         |                           | "<query>".' + Clear (DR-2A)
                    |                             | notes and anything you forgot land      |                                         |                           |
                    |                             | here." + text button "Go to Upcoming"   |                                         |                           |
 New/Edit form      | Save shows a small spinner, | —                                       | "Couldn't save. Try again." snackbar,   | → hike detail (DR-1C)     | Area typed, not picked:
                    | disabled while saving       |                                         | input kept (CEO T2)                     |                           | "No place selected. The hike saves
                    |                             |                                         |                                         |                           | without a forecast." (DR-2D)
 Area search        | thin progress bar under     | "No places found for '<q>'. Try the     | "Couldn't search right now." + Retry    | up to 5 results: name,    | picked: field shows label + ✕
                    | the field                   | nearest village or town." (rule 16)     | (rule 16)                               | region, country           |
 Forecast panel     | thin progress bar +         | (no row → error copy below)             | "Couldn't load the forecast." + Retry;  | rule 18.5 content         | null value → "–" (CEO T1)
                    | "Getting the forecast for   |                                         | offline adds "You're offline."; no row: |                           |
                    | <place>…" (DR-2C)           |                                         | "No forecast for this date yet."        |                           |
                    |  — panel keeps one height in every state                                                                                                           |
 Checklist          | —                           | progress hidden; "Nothing to pack yet.  | write fails → "Couldn't save. Try       | rows                      | —
                    |                             | Add the first thing you'll carry." above| again." snackbar                        |                           |
                    |                             | Add item (DR-2B)                        |                                         |                           |
 Mark-as-done sheet | Save button spinner         | first completion opens empty (rule 5)   | "Couldn't save. Try again." in sheet    | stay + Undo (DR-1C)       | dismiss with edits → "Discard your
                    |                             |                                         |                                         |                           | notes?" Keep editing / Discard (DR-2E)
 Reminder row       | permission dialog           | Off: "Remind me the evening before"     | Denied: "Notifications are off for      | On: "We'll remind you Fri | Too late: disabled, "Too late for
                    |                             |                                         | Sentiero." + "Open settings"           | 13 Nov, around 18:00"     | a reminder" (DR-2F)
```

### Pass 3: User Journey & Emotional Arc — 6/10 → 9/10
```
STEP | USER DOES                           | USER FEELS        | PLAN SPECIFIES
1    | Opens the app the first time        | curious           | Upcoming empty state + "New hike" button
2    | Creates a hike, lands on its list   | organized         | difficulty template; lands on detail (DR-1C)
3    | Packs over a few evenings           | calm progress     | progress line; ticks persist; "6 of 12"
4    | Sees "Rain 70%", taps "+ Rain jacket"| looked after     | suggestion chips + advisory attribution line
5    | Evening before: reminder            | prepared          | DR-2F copy; "5 items left to pack"
6    | After the hike: Mark as done        | reflective        | notes + forgot flags + helper line "These go to the top of your next hike's list." (DR-3A)
7    | Plans the next hike                 | "it remembered!"  | Last hike preselected; forgotten items first with the blaze arrival moment
```
5-second: limestone, cedar ink, one condensed title — calm and specific. 5-minute: the packing loop and the forecast chips. 5-year: a searchable journal of every hike with what you learned.

### Pass 4: Generic-Look Risk — 6/10 → 9/10
Classifier: OPERATE (app UI). Hard rejections: #7 (stacked cards) and emoji came only from the first design session wireframe; resolved by DR-4A. Litmus: brand in first screen YES (condensed title, limestone, cedar); one visual anchor YES (forecast panel, blaze tag); scannable by headings YES; one job per section YES; cards necessary YES (only the forecast panel, as a tonal panel); motion improves hierarchy YES (one arrival moment); premium without shadows YES.
**Visual source of truth (DR-4A):** `DESIGN.md` + `docs/design-preview/`. `docs/wireframe-core-flow.png` documents flow only; its cards, emoji and grayscale styling are superseded.

### Pass 5: Design System Alignment — 6/10 → 9/10
Component map (DR-5A), styled only with DESIGN.md tokens:
| Control | Material 3 component | Tokens |
|---|---|---|
| Hike rows (Upcoming, Completed) | `ListItem` / custom row + `HorizontalDivider` | `{components.list-row}`, `{typography.title}` route |
| Difficulty (form) | `SingleChoiceSegmentedButtonRow` | selected `{colors.secondary-container}` |
| Date (form) | `DatePickerDialog` (past dates allowed) | `{rounded.xl}` |
| "Packing list starts from" | `FilterChip` (single select) | selected `{colors.secondary-container}` |
| Area search results | `ListItem` rows under the field (no dropdown) | `{typography.body}`, region/country `{typography.body-small}` |
| Forecast panel | `Surface` tonal panel | `{components.forecast-panel}` |
| Suggestion chips | `AssistChip` (outlined, leading "+") | `{components.suggestion-chip}`; never the blaze |
| Memory tag / "You hiked this" note | custom `Row` | `{components.memory-tag}` |
| Checklist row | `Checkbox` + label + `IconButton(close)` | `{typography.body}` |
| Mark-as-done sheet | `ModalBottomSheet` | `{components.bottom-sheet}` |
| Undo / errors | `Snackbar` | `{components.snackbar}` |
| Tabs | `NavigationBar` (Upcoming, Completed) | `{components.nav-bar}` |
| New hike | `ExtendedFloatingActionButton` | `{components.fab}` |
| Save / Mark as done | `Button` | `{components.button-primary}`, shape `{rounded.md}` |
Icons (Material Symbols Outlined, imported as vector drawables with Android Studio's Vector Asset tool): `hiking` (Upcoming tab), `menu_book` (Completed tab), `add`, `close`, `more_vert`, `arrow_back`, `event`, `search`, `notifications`.

### Pass 6: Responsive & Accessibility — 3/10 → 9/10
- **Text (DR-6A):** route name 80, item label 60, notes 2,000, area search 60 characters; a counter appears in the last 20% of each limit. Lists: route wraps to 2 lines, then "…"; the detail title wraps fully. Arabic text is typed and shown right-to-left inside the English layout; no layout mirroring (the app is not translated).
- **Accessibility (DR-6B):** each checklist row is one TalkBack element ("Headlamp, not packed, forgot last time"); ✕ reads "Delete Headlamp"; progress reads "Packed 6 of 12"; no fixed heights, checked at 200% font size; every target ≥ 48dp; the arrival animation is skipped when system animations are off; focus order follows the visual order. Check: 10-minute TalkBack pass on the emulator.
- **Rotation (DR-6C):** allowed; the same single column scrolls in landscape; typed form text, an open sheet and the forecast state survive rotation. No tablet layout.

### Pass 7: Unresolved Design Decisions — 2 resolved, 0 deferred
- **Completed row (DR-7A):** route (`title`) · "Sun 25 Oct · Easy" (`body-small`) · first 2 lines of notes (`body`, `{colors.on-surface-variant}`, "…") · memory tag "Forgot: Headlamp" (+N more) when any; no notes → "No notes" in muted italic.
- **Same-route note (DR-7B):** "You hiked this on Sun 25 Oct." when the old hike has no notes; with notes: 'You hiked this on Sun 25 Oct: "Start earlier, parking full by 10…"'. Both open that journal entry.
- Resolved by earlier approval: UV on the forecast card (R3-6) is shown in the preview approved at design consultation ("UV 4").

## Design review outputs

### NOT in scope (design)
- Tablet / two-pane layouts: DR-6C keeps one column.
- Arabic UI translation and RTL layout mirroring: DR-6A supports Arabic text only.
- "Start from basics" button on an empty checklist: not chosen in DR-2B.
- Keeping unsaved sheet drafts: not chosen in DR-2E.
- Photos in journal rows: deferred by CEO-4.

### What already exists
DESIGN.md tokens and components; the approved preview screens (Upcoming, Hike detail, Mark-as-done sheet); Material 3 components from the week 4 slides; the first design session wireframe for flow.

### TODOS.md updates
No new design TODOs: every finding was decided inside the current scope.

## Implementation Tasks (design review)
Synthesized from this review's findings. Each task derives from a specific finding above.

- [ ] **D1 (P1, human: ~1h / CC: ~5min)** — Copy — Apply the "hike" vocabulary and tab names everywhere (DR-1A)
  - Surfaced by: Pass 1 — mixed trip/hike wording
  - Files: to be determined (strings.xml)
  - Verify: grep strings.xml for "trip" in user-visible strings returns nothing
- [ ] **D2 (P1, human: ~2h / CC: ~10min)** — Navigation — Save → new hike's detail with form popped; Mark as done stays in place with Undo (DR-1C)
  - Surfaced by: Pass 1 — undefined landing
  - Files: to be determined (NavHost, form and detail screens)
  - Verify: create a hike → detail opens; Back → Upcoming (not the form)
- [ ] **D3 (P1, human: ~2h / CC: ~10min)** — Hike detail — Order and pinned Mark as done from the hike date (DR-1B)
  - Surfaced by: Pass 1 — detail order
  - Files: to be determined (HikeDetailScreen)
  - Verify: a hike dated tomorrow → no button; a hike dated today → pinned button
- [ ] **D4 (P1, human: ~2h / CC: ~10min)** — States — Empty, loading, error and partial states from the Pass 2 table (DR-2A..2F)
  - Surfaced by: Pass 2 — state coverage
  - Files: to be determined
  - Verify: run each recipe: empty lists, airplane mode, zero items, unpicked area, sheet dismiss, reminder denied
- [ ] **D5 (P2, human: ~15min / CC: ~2min)** — Sheet — Helper line under "Anything you forgot?" (DR-3A)
  - Surfaced by: Pass 3 — loop payoff
  - Files: to be determined (MarkDoneSheet)
  - Verify: visible in the sheet
- [ ] **D6 (P1, human: ~1h / CC: ~10min)** — Components — Build controls from the DR-5A map; import the 9 icons as vector drawables
  - Surfaced by: Pass 5 — component map
  - Files: to be determined (res/drawable, screens)
  - Verify: no emoji or text glyphs as icons in the UI
- [ ] **D7 (P1, human: ~1h / CC: ~10min)** — Inputs — Length caps, counters and wrap rules (DR-6A)
  - Surfaced by: Pass 6 — text limits
  - Files: to be determined (form, sheet, add-item field)
  - Verify: paste 300 characters → capped at 80; Arabic name displays correctly
- [ ] **D8 (P1, human: ~1h / CC: ~10min)** — Accessibility — Merged row semantics, content descriptions, 200% font check (DR-6B)
  - Surfaced by: Pass 6 — accessibility
  - Files: to be determined (checklist row, progress)
  - Verify: TalkBack reads "Headlamp, not packed, forgot last time"; 200% font shows no clipped text
- [ ] **D9 (P2, human: ~30min / CC: ~5min)** — Rotation — Verify form text, open sheet and forecast survive rotation (DR-6C)
  - Surfaced by: Pass 6 — rotation
  - Files: none (manual check)
  - Verify: rotate at each of the three points; nothing resets
- [ ] **D10 (P2, human: ~1h / CC: ~5min)** — Completed — Row content and the same-route note copy (DR-7A, DR-7B)
  - Surfaced by: Pass 7
  - Files: to be determined (CompletedScreen, HikeDetailScreen)
  - Verify: a hike with and without notes shows the right row and note

## Design Review Completion Summary
```
  +====================================================================+
  |         DESIGN PLAN REVIEW — COMPLETION SUMMARY                    |
  +====================================================================+
  | System Audit         | DESIGN.md present (approved today); UI scope |
  |                      | = 5 screens + sheet; no code yet             |
  | Step 0               | 5/10 initial; all 7 passes                   |
  | Pass 1  (Info Arch)  | 5/10 → 9/10 after fixes                      |
  | Pass 2  (States)     | 4/10 → 9/10 after fixes                      |
  | Pass 3  (Journey)    | 6/10 → 9/10 after fixes                      |
  | Pass 4  (Generic look)    | 6/10 → 9/10 after fixes                      |
  | Pass 5  (Design Sys) | 6/10 → 9/10 after fixes                      |
  | Pass 6  (Responsive) | 3/10 → 9/10 after fixes                      |
  | Pass 7  (Decisions)  | 2 resolved, 0 deferred                       |
  +--------------------------------------------------------------------+
  | NOT in scope         | written (5 items)                            |
  | What already exists  | written                                      |
  | TODOS.md updates     | 0 items proposed                             |
  | Approved Mockups     | 0 generated (no image key); approved HTML    |
  |                      | preview used as evidence                     |
  | Decisions made       | 16 added to plan                             |
  | Decisions deferred   | 0                                            |
  | Overall design score | 3/10 → 9/10 (lowest pass)                    |
  +====================================================================+
```
Every pass stops at 9, not 10: the last point is the built screens matching this spec, which only /design-review on the running app can confirm.

### Unresolved Decisions
None.

## Engineering review, 2026-11-01

Target (fixed): this plan (`docs/plan.md`) with its baseline `docs/designs/hiking-trip-planner.md`, `DESIGN.md` and `TODOS.md`. Report file: this file. No code exists yet (no commits), so every path below is proposed.

### Scope Challenge A
- **Reuse:** week 6 Room "My Tasks" (entity, DAO with `Flow` reads and suspend writes, repository, ViewModel `stateIn`); week 7 Retrofit workshop (`@Serializable` DTOs with `@SerialName`, DTO → domain mapping in the repository, failure classification: no connection, 4xx, 5xx, serialization mismatch); week 8 sealed `UiState`; week 9 WorkManager + notification channels. Kotlin serialization is already the course's JSON path (week 7 PDF, slide 7), and type-safe Navigation Compose (stable since 2.8.0) uses the same plugin [Layer 1].
- **Minimum changes:** the CEO tiers (CEO-1) already order the work and name the drop order; no further cuts proposed.
- **Complexity (estimated):** ~35 new Kotlin files; new classes over 2 (5 ViewModels, 2 repositories, 2 DAOs, database, AppContainer, ReminderScheduler + Worker, 4 pure rule files). Gate B applies.
- **Search check:** type-safe routes `@Serializable object/data class` + `composable<T>` + `toRoute()`; note `SavedStateHandle.toRoute` depends on Android `Bundle` (official Navigation 2.8 article), which matters for JVM ViewModel tests.
- **TODOS:** photos (deferred) blocks nothing.
- **Distribution:** debug APK + private GitHub repo; optional CI `./gradlew test` (JVM tests only, see Section 3).

Proposed arrangement (Original):
```
app/src/main/java/.../sentiero/
  SentieroApp.kt, MainActivity.kt, AppContainer.kt
  data/local/   SentieroDatabase, TripEntity, ChecklistItemEntity, TripDao, ChecklistDao, Converters
  data/remote/  OpenMeteoApi (ForecastApi, GeocodingApi), ForecastDto, GeocodingDto
  data/         TripRepository, WeatherRepository
  domain/       Models (Hike, PackingItem, DailyForecast, Place), Templates, SuggestionRules,
                ForecastCardPolicy, ReminderTime              <- pure Kotlin, JVM-tested
  reminder/     ReminderScheduler, ReminderWorker
  ui/theme/     Color, Type, Shape, Theme
  ui/nav/       Routes, SentieroNavHost
  ui/upcoming/  UpcomingScreen, UpcomingViewModel
  ui/completed/ CompletedScreen, CompletedViewModel
  ui/form/      HikeFormScreen, HikeFormViewModel
  ui/detail/    HikeDetailScreen, HikeDetailViewModel, MarkDoneSheet, ForecastPanel
  ui/components/ AreaSearchField (+ AreaSearchState), HikeRow, MemoryTag
```
Smaller alternative: one `HikeListsViewModel` + one list screen with a mode for Upcoming/Completed, and one DAO for both tables (~31 files).

### Scope Challenge B
- Feature cuts: none proposed (CEO-1 governs delivery order and drops).
- Structure question ENG-S: Original arrangement vs Smaller arrangement. Pending remedies not decided here: all ENG-R rows below.

Scope record: feature answers: none proposed; structure: A "Original arrangement (recommended)" (D1, 2026-11-01); accepted scope: CEO-1 tiers unchanged + the Original arrangement above (including domain models mapped in repositories); pending remedies: ENG-R1..R14.

### Scope Challenge C
No issues found beyond the section findings below. Scope Challenge result: scope accepted as-is.

## Eng Section 1: Architecture

Data boundaries (one direction, as in the week 6/8 slides):
```
 Compose screen ──event──▶ ScreenViewModel ──suspend/Flow──▶ TripRepository ──▶ TripDao/ChecklistDao ──▶ Room
      ▲                       │  StateFlow<UiState>                │  @Transaction writes            (source of truth)
      └────── state ──────────┘                                    └──▶ ReminderScheduler ──▶ WorkManager ──▶ ReminderWorker
                                      └──suspend──▶ WeatherRepository ──▶ GeocodingApi / ForecastApi (Retrofit)
                                                       maps DTO → Place / DailyForecast; classifies failures
 Pure Kotlin, no Android imports: Templates · SuggestionRules · ForecastCardPolicy · ReminderTime
```
Design-twice (new boundary: the repositories):
- Shape 1 (chosen): `TripRepository` (local data + reminder side effects) and `WeatherRepository` (remote, two base URLs, DTO mapping, failure classes). Depth: each hides a lot (transactions, copy ordering, timezone, error classes) behind a few functions.
- Shape 2: one `HikeRepository` facade over Room, Retrofit and WorkManager. Rejected: it mixes local and network failure modes in one class and becomes a pass-through for both.
Red-flag screen: information leakage (DTOs and entities) is prevented by mapping at both repositories (D1 arrangement); split ownership of reminder state is ENG-R6; "two ways to add an item" is ENG-R8; no shallow modules, pass-throughs, temporal splits, importable internals or hand-synced lists found.

Findings:
- [INFORMATIONAL] (confidence: 9/10) docs/designs/hiking-trip-planner.md:54 — `sourceTripId` is stored but no rule reads it (rules 9, 14 use `carriedOver` and route names). Finalizing the schema without it (R3-8).
- [INFORMATIONAL] (confidence: 8/10) docs/plan.md CEO-3 — a per-hike reminder needs a stored flag; the schema sketch has none.
- [INFORMATIONAL] (confidence: 9/10) docs/plan.md Review facts — schema timing and migrations undecided (R3-24); destructive migration would wipe midterm demo data.
- [INFORMATIONAL] (confidence: 8/10) `LocalDate` needs API 26 or core-library desugaring; minSdk unset.
- [INFORMATIONAL] (confidence: 8/10) Navigation 2.8 `SavedStateHandle.toRoute` depends on Android `Bundle`, so a ViewModel that reads it is not a plain JVM test (Navigation 2.8 article).
- [INFORMATIONAL] (confidence: 8/10) docs/plan.md Section 1 A1 — "today" read in several places; tests and demo recipes need one controllable source.
- [INFORMATIONAL] (confidence: 8/10) docs/plan.md Section 1 A2 — reminder side effects on delete / Mark as done / date edit can be missed if spread across ViewModels.
- [INFORMATIONAL] (confidence: 7/10) docs/designs/hiking-trip-planner.md:80 — area search trigger undefined (R3-3).
- Determined (no new approval needed): forecast request = stored coordinates, `daily=temperature_2m_max,temperature_2m_min,precipitation_probability_max,wind_speed_10m_max,uv_index_max`, `start_date=end_date=<hike date>`, `timezone=auto` — required for rule 18.5's single local day (R3-4). JSON = kotlinx.serialization (`Json { ignoreUnknownKeys = true }`), the course's week 7 path.

#### Pending choices (Section 1)
| ID | Choice | Current | Proposed (recommended) | State |
|---|---|---|---|---|
| ENG-R1 | Schema ID type (field set finalized: `sourceTripId` dropped, `reminderOn` added) | unspecified | String UUID ids, as the week 6 slide | approved: D2 "String UUID (recommended)"; schema below is accepted |
| ENG-R2 | Schema version policy | unspecified | full schema in v1 now; `exportSchema = true`; any later change gets a real Migration; never destructive | approved: D3 "Full schema in v1 now (recommended)" |
| ENG-R3 | minSdk | unspecified | 26 (java.time without desugaring) | approved: D4 "minSdk 26 (recommended)"; target/compile = latest stable from the template |
| ENG-R4 | Navigation style | unspecified | type-safe routes; id read in NavHost and passed to the ViewModel factory | approved: D5 "Typed routes, id into VM factory (recommended)" |
| ENG-R5 | Source of "today" | `LocalDate.now()` wherever needed | `java.time.Clock` in AppContainer; pure functions take `today` | approved: D6 "Inject a Clock (recommended)" |
| ENG-R6 | Reminder side-effect owner | unspecified | TripRepository only; worker re-checks Room before posting | approved: D7 "Repository owns it (recommended)" |
| ENG-R7 | Area search trigger | unspecified | on submit (keyboard search / icon), min 2 characters | approved: D8 "On submit (recommended)" |

Proposed v1 schema (ENG-R1 varies only the id type):
```
trips:           id TEXT PK | route_name TEXT NOT NULL | date INTEGER NOT NULL (epochDay) | difficulty TEXT NOT NULL (EASY|MODERATE|HARD)
                 | area_label TEXT NULL | latitude REAL NULL | longitude REAL NULL | notes TEXT NOT NULL DEFAULT ''
                 | completed_at INTEGER NULL (epoch millis) | reminder_on INTEGER NOT NULL DEFAULT 0
checklist_items: id TEXT PK | trip_id TEXT NOT NULL FK→trips.id ON DELETE CASCADE (indexed) | label TEXT NOT NULL
                 | is_packed INTEGER NOT NULL DEFAULT 0 | position INTEGER NOT NULL | forgotten INTEGER NOT NULL DEFAULT 0
                 | carried_over INTEGER NOT NULL DEFAULT 0 | added_after_hike INTEGER NOT NULL DEFAULT 0   <- ENG-R10 (D11)
```

Section 1 dispositions: ENG-R1..R7 accepted (D2-D8). Determined items carried: forecast request parameters with `timezone=auto`; kotlinx.serialization.

Accepted Section 1 contracts:
```kotlin
// Routes (ENG-R4)
@Serializable object Upcoming
@Serializable object Completed
@Serializable data class HikeForm(val editId: String? = null)
@Serializable data class HikeDetail(val id: String)

// NavHost passes ids into factories; ViewModels never call SavedStateHandle.toRoute()
composable<HikeDetail> { entry ->
    val id = entry.toRoute<HikeDetail>().id
    val vm: HikeDetailViewModel = viewModel(factory = HikeDetailViewModel.factory(id))
}

// AppContainer (manual DI, created once in SentieroApp)
class AppContainer(context: Context) {
    val clock: Clock = Clock.systemDefaultZone()                         // ENG-R5
    val db = Room.databaseBuilder(context, SentieroDatabase::class.java, "sentiero.db").build() // ENG-R2: no destructive fallback
    val reminderScheduler = ReminderScheduler(WorkManager.getInstance(context), clock)
    val tripRepository = TripRepository(db, db.tripDao(), db.checklistDao(), reminderScheduler, clock) // ENG-R6
    val weatherRepository = WeatherRepository(forecastApi, geocodingApi)  // two Retrofit instances, two base URLs
}
```

## Eng Section 2: Code Quality

Findings:
- [INFORMATIONAL] (confidence: 8/10) docs/designs/hiking-trip-planner.md:71 — three add paths ("+ Add item", suggestion chip, "Add something you forgot") with position defined for only two (R3-30); three copies of insert logic would drift.
- [INFORMATIONAL] (confidence: 7/10) docs/designs/hiking-trip-planner.md:64 — "Add something you forgot" can duplicate an existing label (R3-32); the copy rule then lists it twice on the next hike.
- [INFORMATIONAL] (confidence: 7/10) docs/designs/hiking-trip-planner.md:64-65 — an item added in the sheet by mistake cannot be removed: journal mode is read-only and the sheet only toggles flags (R3-29).
- [INFORMATIONAL] (confidence: 9/10) docs/designs/hiking-trip-planner.md:99 — the Windproof jacket row suppresses on "rain jacket" but not its synonyms "raincoat", "rain coat", "waterproof jacket" from the row above (R3-7).

#### Pending choices (Section 2)
| ID | Choice | Current | Proposed (recommended) | State |
|---|---|---|---|---|
| ENG-R8 | One add-item path | three paths, position for two | one `TripRepository.addItem(tripId, label, forgotten = false)` appending at max position + 1, used by all three | approved: D9 "One addItem, appends (recommended)" |
| ENG-R9 | Duplicate label in "Add something you forgot" | adds a duplicate | flag the existing item (case-insensitive, trimmed) instead | approved: D10 "Flag the existing item (recommended)" |
| ENG-R10 | Removing a mistaken sheet item | impossible | rows added in the open sheet show ✕ until Save; after Save, "Edit journal" can delete only items added through the sheet (`addedAfterHike` column) | approved: D11 "✕ on items added after the hike (recommended)"; `added_after_hike INTEGER NOT NULL DEFAULT 0` joins schema v1 |
| ENG-R11 | Windproof suppression list | windproof, windbreaker, rain jacket | add raincoat, rain coat, waterproof jacket (not poncho) | approved: D12 "Add the synonyms (recommended)" |

Section 2 dispositions: ENG-R8..R11 accepted (D9-D12). Rule 19's Windproof row now reads: windproof, windbreaker, rain jacket, raincoat, rain coat, waterproof jacket.

## Eng Section 3: Tests

Framework: none detected (no project yet). Proposed: JUnit 4 + kotlinx-coroutines-test for JVM tests (`app/src/test`), AndroidX Test + Room in-memory for database tests per ENG-R12. No regression rule applies: there is no existing behavior.

```
CODE PATHS (planned)                                        USER FLOWS
[+] domain/Templates.templateFor(difficulty)                [+] Midterm slice
  └── [GAP] Easy 10 / Moderate 12 / Hard 14, order            ├── [GAP] [→E2E manual] create → land on detail → tick 3 → force-stop → reopen → same ticks
[+] domain/SuggestionRules.suggest(daily, labels)             └── [GAP] rotation keeps typed form text, open sheet, forecast
  ├── [GAP] each row fires at threshold (50 %, 5 °C, 28 °C, 6, 40 km/h)
  ├── [GAP] boundary below threshold does not fire           [+] The loop
  ├── [GAP] null value never fires                             ├── [GAP] [→E2E manual] Chouf done (forgot Headlamp) → new hike from last hike → Headlamp first, tagged
  ├── [GAP] synonym suppression incl. Raincoat / Poncho        └── [GAP] suggestion chip tap → item appended, chip disappears
  └── [GAP] max 3 chips, table order
[+] domain/ForecastCardPolicy.state(trip, today)            [+] Error states
  └── [GAP] completed / past / no area / today+15 / today+16   ├── [GAP] airplane mode → "Couldn't load the forecast." + "You're offline." → Retry recovers
[+] domain/ReminderTime.at(date, now)                          ├── [GAP] search "zzqx" → Empty copy
  └── [GAP] 18:00 day before; null when past                   └── [GAP] HTTP 400 path → "No forecast for this date yet."
[+] data/local DAOs (Room)
  ├── [GAP] Upcoming order (date asc), Completed order (date desc, completedAt desc)
  ├── [GAP] progress counts per hike (see ENG-R14)
  ├── [GAP] cascade delete removes items
  └── [GAP] markDone transaction is all-or-nothing (CEO T3)
[+] data/TripRepository
  ├── [GAP] createFromLastHike: forgotten first, carriedOver set, flags reset (rule 9)
  ├── [GAP] source deleted → template + notice (CEO T6)
  ├── [GAP] addItem appends at max+1, trims, ignores blank (ENG-R8)
  ├── [GAP] forgot-field duplicate flags existing item (ENG-R9)
  └── [GAP] reminder scheduled / cancelled on date edit, delete, markDone (ENG-R6, fake scheduler)
[+] data/WeatherRepository + DTOs
  ├── [GAP] no "results" key → Empty (live fixture `{"generationtime_ms":0.17}`) (CEO T1)
  ├── [GAP] Tripoli fixture → 5 places, Lebanon not auto-picked
  ├── [GAP] null daily values parse (CEO T1)
  └── [GAP] IOException / HttpException / parse → error classes (CEO T2)
[+] ViewModels (fake repositories, fixed Clock)
  ├── [GAP] HikeDetailViewModel: two rapid ticks end on last value (CEO T7); Mark as done + Undo
  ├── [GAP] HikeFormViewModel: double Save → one hike (CEO T4); unpicked area saves no area (DR-2D)
  └── [GAP] AreaSearchState: newer submit cancels older (CEO T5)

COVERAGE: 0/32 planned paths tested (greenfield)  |  E2E-worthy: 2 manual scripts
```
Value cards (one per critical path; full list in the Test Plan Artifact):
- Value: protects=rule 19 table drives chips exactly; fails_when=a threshold, synonym or the 3-chip cap changes; why_new=no tests exist; seam=none; no_claim=real forecast accuracy (fixture values)
- Value: protects=no-match search shows Empty, not Error; fails_when=`results` becomes non-null; why_new=no tests exist; seam=none; no_claim=live API shape stays the same (fixture captured 2026-11-01)
- Value: protects=last-hike copy puts forgotten items first and resets flags; fails_when=copy order or flag reset changes; why_new=no tests exist; seam=none; no_claim=UI rendering of the tag (DAO/repository level)
- Value: protects=no ghost reminder after delete/done/date edit; fails_when=a path stops calling the scheduler; why_new=no tests exist; seam=fake ReminderScheduler interface (also used by production wiring); no_claim=WorkManager timing on a real device

#### Pending choices (Section 3)
| ID | Choice | Current | Proposed (recommended) | State |
|---|---|---|---|---|
| ENG-R12 | Where DAO tests run | unspecified (R3-22) | instrumented `androidTest` with in-memory Room on the emulator; CI runs JVM tests only | approved: D13 "Instrumented, in-memory Room (recommended)" |
| ENG-R13 | How network code is tested | unspecified | fake API interfaces for repository logic + JSON fixture decode tests with the real `Json` config; no MockWebServer | approved: D14 "Fake API + JSON fixtures (recommended)" |

Section 3 dispositions: ENG-R12, ENG-R13 accepted (D13, D14). Test layout: `app/src/test` (JUnit 4, kotlinx-coroutines-test, fakes, JSON fixtures in `src/test/resources`) runs with `./gradlew test`; `app/src/androidTest` (in-memory Room DAO tests) runs with `./gradlew connectedAndroidTest` on the emulator. Optional CI runs `./gradlew test assembleDebug` only.

## Eng Section 4: Performance

Findings:
- [INFORMATIONAL] (confidence: 8/10) docs/plan.md Section 1 (DR-1 Upcoming row "6 of 12 packed") — counting items per hike with one query per row is N+1. Scale: tens of hikes, about 10-20 items each [estimated]; harmless now, but it is the pattern a grader checks.
- [OK] `checklist_items.trip_id` index is in schema v1 (ENG-R1), so per-hike item queries and cascade deletes use it.
- [OK] Forecast fetched once per (lat, lon, date) while the detail screen is open; no cache needed. Geocoding only on submit (ENG-R7).
- [OK] Lists are `LazyColumn`; notes previews limited to 2 lines (DR-7A); no whole-table loads into memory beyond the visible lists.
- Slowest paths: forecast and geocoding requests (network bound) [estimated 0.3-2 s]; local queries are sub-10 ms at this scale [estimated].

#### Pending choices (Section 4)
| ID | Choice | Current | Proposed (recommended) | State |
|---|---|---|---|---|
| ENG-R14 | How lists get packed/total counts | unspecified | one aggregate query per list returning `HikeSummary` (trip columns + `packedCount` + `totalCount` via LEFT JOIN + SUM/COUNT) | approved: D15 "One aggregate query (recommended)" |

Section 4 dispositions: ENG-R14 accepted (D15).

## Eng decision ledger
TODOS.md: no new proposals (no deferrals in Sections 1-4).
Approval readiness: PASS — ENG-S (D1 "Original arrangement (recommended)"), ENG-R1 (D2), ENG-R2 (D3), ENG-R3 (D4), ENG-R4 (D5), ENG-R5 (D6), ENG-R6 (D7), ENG-R7 (D8), ENG-R8 (D9), ENG-R9 (D10), ENG-R10 (D11), ENG-R11 (D12), ENG-R12 (D13), ENG-R13 (D14), ENG-R14 (D15). Determined items (forecast request parameters with `timezone=auto`; kotlinx.serialization) follow approved rule 18.5 and premise 2.

## Eng review outputs

### NOT in scope (eng)
- Robolectric: not chosen (D13); DAO tests run on the emulator.
- MockWebServer: not chosen (D14); parsing is covered by fixture decode tests.
- Destructive migrations: rejected (D3).
- Exact alarms / SCHEDULE_EXACT_ALARM: rejected earlier (CEO-5).
- Hilt, multi-module: rejected in office hours (approach B).

### What already exists
Course workshop code reused as patterns, not copied files: week 6 Room task stack, week 7 Retrofit + `@Serializable` DTO mapping and failure classes, week 8 sealed UiState. Nothing exists in this repo yet.

### Reminder flow (diagram)
```
 toggle ON ─▶ HikeDetailVM ─▶ TripRepository.setReminder(id, true)
                                 ├─ UPDATE trips SET reminder_on = 1
                                 └─ ReminderScheduler.schedule(id, date)      unique work "reminder-<id>", REPLACE,
                                                                               initial delay = ReminderTime.at(date, now) [CEO-5: around 18:00]
 edit date ─▶ TripRepository.updateHike ─▶ reschedule (or cancel + reminder_on = 0 if too late)
 delete / markDone ─▶ TripRepository ─▶ ReminderScheduler.cancel(id)
 ReminderWorker.doWork(id): read trip → missing | completed | reminder_on = 0 → success, no post
                            permission missing (API 33+) → success, log "Sentiero/Reminder", no post
                            else post on channel "hike_reminders": "Tomorrow: <route>. <n> items left to pack."
```

### Failure modes (eng)
| Path | Realistic failure | Test / handling | User sees |
|---|---|---|---|
| Room schema | column added after the midterm without migration | ENG-R2: full v1 now; any change needs Migration + test | normal start |
| Create hike | double Save | CEO T4 + VM test | one hike |
| Last-hike copy | source deleted meanwhile | CEO T6 + repository test | basics + notice |
| Geocoding | no-match body has no "results" | CEO T1 + fixture test | Empty copy |
| Forecast | offline / 400 / null values | CEO T1, T2 + repository tests | Error / no-forecast / "–" |
| Ticks | fast double tap | CEO T7 + VM test | last shown value |
| Reminder | hike deleted after scheduling | ENG-R6 cancel + worker re-check | no notification (correct) |
| Search | out-of-order responses | CEO T5 + controlled fake test | latest results |
Critical gaps (no test, no handling, silent): 0.

### Worktree parallelization strategy
Sequential implementation, no parallelization opportunity: one developer, one app module, and every later tier builds on the midterm's Room layer.

## Implementation Tasks (eng review)
Synthesized from this review's findings. Each task derives from a specific finding above.

- [ ] **E1 (P1, human: ~1h / CC: ~10min)** — Project setup — New Compose project (minSdk 26), Kotlin serialization plugin, KSP for Room, Navigation Compose 2.8+, Retrofit + kotlinx converter, WorkManager, lifecycle-viewmodel-compose
  - Surfaced by: Section 1 — ENG-R3, ENG-R4
  - Files: build.gradle.kts (app, project), libs.versions.toml
  - Verify: `./gradlew assembleDebug` succeeds; app launches on an API 26+ emulator
- [ ] **E2 (P1, human: ~2h / CC: ~10min)** — Database — Entities, DAOs, converters (LocalDate ↔ epochDay, Instant ↔ millis, Difficulty ↔ TEXT), full schema v1 with `exportSchema = true`
  - Surfaced by: Section 1 — ENG-R1, ENG-R2
  - Files: data/local/*, app/schemas/
  - Verify: schemas/…/1.json committed; DAO instrumented tests pass
- [ ] **E3 (P1, human: ~1h / CC: ~5min)** — DI — AppContainer with Clock, database, repositories, scheduler; ViewModel factories take ids
  - Surfaced by: Section 1 — ENG-R4, ENG-R5
  - Files: AppContainer.kt, SentieroApp.kt, ui/**/…ViewModel.kt
  - Verify: ViewModel unit tests construct VMs with fakes and a fixed Clock
- [ ] **E4 (P1, human: ~1h / CC: ~10min)** — Navigation — Typed routes and NavHost; Save pops the form and opens the new hike (DR-1C)
  - Surfaced by: Section 1 — ENG-R4
  - Files: ui/nav/*
  - Verify: create → detail; Back → Upcoming
- [ ] **E5 (P2, human: ~2h / CC: ~10min)** — Reminder — Repository-owned scheduling, worker re-check, channel, FLAG_IMMUTABLE (CEO T10)
  - Surfaced by: Section 1 — ENG-R6
  - Files: reminder/*, data/TripRepository.kt
  - Verify: repository tests with a fake scheduler; Background Task Inspector shows one job per hike
- [ ] **E6 (P2, human: ~1h / CC: ~5min)** — Area search — Submit-only trigger, 2-character minimum, cancel previous job
  - Surfaced by: Section 1 — ENG-R7
  - Files: ui/components/AreaSearchField.kt
  - Verify: submit "B" → helper text; submit twice fast → only the latest results
- [ ] **E7 (P1, human: ~1h / CC: ~5min)** — Checklist writes — One `addItem` path, duplicate-label flagging, `added_after_hike` and ✕ rules
  - Surfaced by: Section 2 — ENG-R8, R9, R10
  - Files: data/TripRepository.kt, ui/detail/MarkDoneSheet.kt
  - Verify: repository tests for append, trim, blank, duplicate; sheet ✕ only on sheet-added rows
- [ ] **E8 (P1, human: ~1h / CC: ~10min)** — Suggestion rules — Table-driven `suggest()` with the corrected windproof row
  - Surfaced by: Section 2 — ENG-R11
  - Files: domain/SuggestionRules.kt, src/test/…/SuggestionRulesTest.kt
  - Verify: `./gradlew test` covers every row, boundaries, nulls, synonyms, cap of 3
- [ ] **E9 (P1, human: ~2h / CC: ~15min)** — Tests — JVM tests (rules, repositories with fakes, ViewModels with fixed Clock, JSON fixtures) and instrumented DAO tests
  - Surfaced by: Section 3 — ENG-R12, ENG-R13
  - Files: app/src/test/**, app/src/androidTest/**, src/test/resources/*.json
  - Verify: `./gradlew test` and `./gradlew connectedAndroidTest` green
- [ ] **E10 (P2, human: ~30min / CC: ~5min)** — Lists — Aggregate `HikeSummary` queries for Upcoming and Completed
  - Surfaced by: Section 4 — ENG-R14
  - Files: data/local/TripDao.kt
  - Verify: DAO test: 2 hikes with 3/5 and 0/0 items → counts (3,5) and (0,0)

## Eng Review Completion Summary
- Step 0: Scope Challenge — scope accepted as-is (Original arrangement, D1)
- Reality rows: 7/7 applicable rows emitted (PLAN_REALITY line below), 1 unverified premise (Navigation Compose class coverage)
- Architecture Review: 8 issues found (all resolved: D2-D8 + 1 determined)
- Code Quality Review: 4 issues found (D9-D12)
- Test Review: diagram produced, 32 planned paths, 2 method choices (D13-D14); test plan artifact written
- Performance Review: 1 issue found (D15)
- NOT in scope: written
- What already exists: written
- TODOS.md updates: 0 items proposed
- Failure modes: 0 critical gaps flagged
- Unresolved decisions: 0 in this review
- Parallelization: 1 lane, 0 parallel / 1 sequential
- Lake Score: 8/9 (answers scored for completeness: D3, D6, D7, D9, D10, D11, D12, D14, D15; D14 picked the 9/10 option over MockWebServer's 10/10)

REALITY: premise-table finding unverified=1 (Navigation Compose not in uploaded slides) docs/designs/hiking-trip-planner.md:30
REALITY: already-done pass no commits on main docs/plan.md:9
REALITY: surface-check pass geocoding live-checked; forecast params static-checked; Navigation 2.8 type-safe APIs checked against the official page docs/plan.md:811
REALITY: binding-decisions pass all first design session, CEO and design decisions honored docs/plan.md:9
REALITY: numbers pass file counts and latencies [estimated]; thresholds from approved rule 19 docs/plan.md:811
REALITY: deferred-asks pass no linked issue docs/designs/hiking-trip-planner.md:37
REALITY: pr-count pass solo, commits to main docs/plan.md:1

## Review summary

| Review | Trigger | Why | Runs | Status | Findings |
|--------|---------|-----|------|--------|----------|
| CEO Review | `the product review` | Scope & strategy | 1 | CLEAR | mode: HOLD_SCOPE, 0 critical gaps |
| Eng Review | `the engineering review` | Architecture & tests (required) | 1 | CLEAR | 13 issues, 0 critical gaps |
| Design Review | `the design review` | UI/UX gaps | 1 | CLEAR | score: 3/10 → 9/10, 16 decisions |

- **VERDICT:** CEO + ENG + DESIGN CLEARED — ready to implement.

NO UNRESOLVED DECISIONS
