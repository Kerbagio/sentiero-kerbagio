# Sentiero Kerbagio — weekly plan

Written 2026-11-01. It is built from the approved design doc (`docs/designs/hiking-trip-planner.md`), `docs/plan.md`, Spec 1 (`docs/specs/01-midterm-slice.md`) and Spec 2 (`docs/specs/02-rest-of-mvp.md`). Nothing is built yet (no commits). Task numbers 2.1-2.17 are Spec 2's child issues, and their hours come from the specs.

## Assumptions
- **10 hours a week.** You didn't say how many hours you have, so this is the default. The budget check below shows what it buys.
- **5 weeks, Mon 2 Nov – Sun 6 Dec,** instead of the default 4, because the design doc assumes the final around Sun 6 Dec. When the real date is announced, I re-plan.
- **Week 1 is the midterm** (Spec 1), due on or before Sun 8 Nov. That date is fixed, so week 1 is planned at what the midterm needs, not at 10 h.

## Budget check

| Scope | Hours | Average over 5 weeks | Fits 10 h/week? |
|---|---|---|---|
| Midterm (Spec 1) | 15.5 h | all in week 1 | No: week 1 runs 5.5 h over (fixed date) |
| **Cut line** = midterm + P1 (2.1-2.7) + release (2.17) | 54.5 h | ~11 h | Only just: about 10% slack instead of 20% |
| + P2, the loop (2.8-2.11) | 70.5 h | ~14 h | No |
| + P3, polish (2.12-2.16) | 87.5 h | ~17.5 h | No |

What ships by ~6 Dec at each pace:
- **10 h/week:** the cut line. That means all four brief bullets, every API state and the journal, but not the loop.
- **~14 h/week:** the cut line plus the loop. The demo sentence ("I forgot my headlamp at Chouf…") works.
- **~18 h/week:** everything, except perhaps the reminder, which is dropped first.

Each week below schedules the cut line as its main list. **Next if you have time** points to the following items in the build queue, so any extra hour goes into the loop first and polish second.

## Build queue (one order for every pace)

Spec 1 (bare) → midterm catch-up → 2.1 → 2.2 → 2.3 → 2.4 → 2.5 → 2.6 → 2.7 → **tag `p1-complete`** → 2.8 → 2.9 → 2.10 → 2.11 → **tag `p2-complete`** → 2.12 → 2.13 → 2.14 → 2.15 → 2.16 → 2.17 (always in the last week)

Anything not built by the deadline is dropped from the end of P3: the reminder (2.16) first, then Completed search, Edit hike, Edit journal, Move back.

## Today, Sun 1 Nov (about 45 min, before week 1)
- ~~Email the professor one line that makes the API official: "For topic 8 I'll use the Open-Meteo forecast and geocoding APIs (free, no key) for weather; I found no free trail-data API." (15 min)~~ Skipped: Open-Meteo is announced at the midterm presentation instead (demo script step 1).
- [x] Create the private GitHub repo and push the planning docs (`docs/`, `DESIGN.md`, `TODOS.md`, this file) as the first commit (30 min)

## Week 1 — Mon 2 – Sun 8 Nov · Midterm: create a hike, tick it, survive a restart (Spec 1)
**Planned: 15.5 h** (5.5 h over the assumption; the date is fixed)
- [x] Project setup: Empty Activity (Compose), minSdk 26, Kotlin serialization + KSP, Room / Navigation / lifecycle dependencies, schema export (1.5 h)
- [x] Theme from DESIGN.md: colors (dynamic color off), bundled fonts + OFL files, type scale, shapes (1.5 h)
- [x] Room: the full v1 entities, converters, DAOs with the aggregate Upcoming query, database (2 h)
- [x] Domain models, `templateFor`, `createHike` in one transaction, AppContainer, SentieroApp (1.5 h)
- [x] ViewModels: Upcoming; HikeForm (validation, 80-char cap, double-tap guard); HikeDetail (absolute ticks) (2 h)
- [x] Screens: Upcoming and the New hike form (1.5 h)
- [x] Screen: Hike detail checklist, with all copy in strings.xml (1.5 h)
- [x] Navigation: typed routes; Save opens the new hike and pops the form (1 h)
- [ ] Tests: TemplatesTest, HikeDetailViewModelTest, TripDaoTest; manual restart and rotation runs (2 h)
- [x] Rehearse the 5-minute demo with Database Inspector open; tag `midterm-demo` and keep its APK (1 h)


**Done when:** on a fresh install, "Jabal Moussa Reserve", Sat 14 Nov, Moderate opens with 12 items. You tick 3, force-stop, reopen, and see the same 3 ticks. The 3 tests are green, `midterm-demo` is tagged, and the rehearsal fits in 5 minutes.

**Top risk:** Navigation Compose isn't in the slides, and the first Room/KSP build errors can eat a day.

**Fallback:** create → tick → restart is the part you can't cut. If you're behind on Thursday, move the bundled fonts and the rotation check to week 2. Never cut the restart test.

**Next if you have time:** 2.1 (week 2). Weeks 2-4 are full, so resting is also a fine use of spare time.

## Week 2 — Mon 9 – Sun 15 Nov · Finish the midterm properly, edit the checklist, build the weather data layer
**Planned: 13.5 h** (the midterm was built bare; 2.7 Delete moved to week 5)
- [x] Midterm catch-up: the DESIGN.md theme (colors, bundled fonts, type, shapes) (1.5 h)
- [ ] Midterm catch-up: TemplatesTest, HikeDetailViewModelTest, TripDaoTest (2 h)
- [ ] Midterm catch-up: copy into strings.xml, 80-char cap + counter, double-tap guard, "Couldn't save" snackbar, relative date labels (1 h)
- [ ] 2.1 Repository: `addItem` (appends inside a transaction), `deleteItem` / `restoreItem` (1 h)
- [ ] 2.1 UI: "+ Add item" field, ✕ with Undo snackbar, zero-items state (2 h)
- [ ] 2.1 Tests: RoomTripRepositoryTest ×2 (1 h)
- [ ] 2.2 Retrofit + kotlinx converter, INTERNET permission, AppContainer wiring (1 h)
- [ ] 2.2 `GeocodingApi`, `ForecastApi`, DTOs with nullable fields (1 h)
- [ ] 2.2 `OpenMeteoWeatherRepository`: mapping, rounding, failure classes, `Sentiero/Weather` log (1.5 h)
- [ ] 2.2 Fixtures (capture Tripoli with curl) + JsonFixturesTest, WeatherRepositoryTest, PlaceTest (1.5 h)


**Done when:** the app uses the DESIGN.md theme and Spec 1's 3 tests pass; adding, deleting and Undo work on the emulator; `./gradlew test` passes the 12 new weather tests.

**Top risk:** the professor hasn't confirmed the API, or the converter or JSON setup fights you.

**Fallback:** fixtures and fake APIs keep every test offline, and the `WeatherRepository` interface isolates a change of provider. If you have no answer by Monday, ask the professor in person.

**Next if you have time:** start 2.3 (week 3).

## Week 3 — Mon 16 – Sun 22 Nov · P1: area search and the forecast panel, every API state
**Planned: 11 h**
- [ ] 2.3 `AreaSearchState`: search on submit, 2-letter minimum, cancel the previous job (1.5 h)
- [ ] 2.3 `AreaSearchField`: Editing / Picked, results, Empty / Error + Retry (1.5 h)
- [ ] 2.3 Form: "Area (optional)", the "No place selected" hint, area saved on create (1 h)
- [ ] 2.3 Tests: AreaSearchStateTest ×4, HikeFormViewModelTest ×1 (1 h)
- [ ] 2.4 `ForecastCardPolicy` + ForecastCardPolicyTest ×6 (1 h)
- [ ] 2.4 HikeDetailViewModel forecast state (key, job, Retry) + HikeDetailViewModelTest ×3 (2 h)
- [ ] 2.4 `ForecastPanel`: every state at one minimum height, values line with UV, attribution (2 h)
- [ ] 2.4 Add-area sheet (1 h)


**Done when:** every weather state can be shown on demand:
- "Faraya" makes you pick (Guinea is listed first), and "zzqx" shows the Empty text.
- In airplane mode the panel shows "Couldn't load the forecast." and "You're offline.", and Retry recovers after you reconnect.
- A hike with an area dated today + 16 shows "Forecast available from …".
- A hike with no area offers Add area.
- A past-dated hike shows no panel.

**Top risk:** async state bugs: out-of-order results, refetching on rotation, a Loading state that never ends.

**Fallback:** AreaSearchStateTest's gated fake reproduces ordering bugs, so fix them in the test first. If the Add-area sheet runs over, it moves to week 4; the No-area text already shows.

**Next if you have time:** 2.5 (week 4), then 2.8 Suggestion chips (P2, 4 h), whose dependencies 2.1 and 2.4 are done.

## Week 4 — Mon 23 – Sun 29 Nov · P1: Completed tab and Mark as done, which completes the brief
**Planned: 12 h**
- [ ] 2.5 `observeCompleted` query + mapping + TripDaoTest ×3 (1 h)
- [ ] 2.5 `Completed` route, bottom bar, `openTab` (1 h)
- [ ] 2.5 CompletedScreen + ViewModel (rows, empty state), `shortDate` + DateLabelsTest (1.5 h)
- [ ] 2.5 Journal mode (read-only) + due rows on Upcoming (1.5 h)
- [ ] 2.6 Repository: `journalSource`, the `saveJournal` transaction, `reopenHike` + tests ×3 (2 h)
- [ ] 2.6 MarkDoneViewModel (draft, `hasChanges`, double-tap guard) + tests ×3 (2 h)
- [ ] 2.6 MarkDoneSheet + the discard gate (2 h)
- [ ] 2.6 Both hosts (pinned button on detail, row button on Upcoming) + Undo snackbar; tag `p1-complete` (1 h)


**Done when:**
- A hike dated today can be marked done from its row and from its detail.
- It moves to Completed with its notes and "Forgot: …", and Undo brings it back.
- Swipe, Back and a scrim tap with typed notes all ask "Discard your notes?".
- `p1-complete` is tagged: all four brief bullets work.

**Top risk:** the sheet (draft, discard gate, two hosts) is the most complex UI in the app.

**Fallback:** ship the detail host first. The Upcoming row button reuses the same sheet and can move into week 5's buffer.

**Next if you have time:** P2 in order: 2.8 Chips (4 h) → 2.9 Start from last hike (6 h) → 2.10 Add something you forgot (4 h) → 2.11 Same-route note (2 h) → tag `p2-complete`.

## Week 5 — Mon 30 Nov – Sun 6 Dec · Release, rehearse, buffer (final assumed ~Sun 6 Dec)
**Planned: 7 h + about 3 h buffer**
- [ ] 2.7 Delete hike: ⋮ menu, confirmation dialog, cascade, `Deleted` state, cascade test (2 h)
- [ ] 2.17 Accessibility: 10-minute TalkBack pass, 200% font, 48dp targets (1 h)
- [ ] 2.17 Rotation at the 3 points, the 3 log tags, greps for catch-alls and the word "trip" (1 h)
- [ ] 2.17 README (what it does, how to run it, tests, API, scope) and a permissions review: INTERNET only, plus POST_NOTIFICATIONS only if 2.16 shipped (1 h)
- [ ] 2.17 Tag `final-demo`, attach the APK to a GitHub release, give the professor repo access (0.5 h)
- [ ] Rehearse the final demo twice, timed, with Spec 2's states-on-demand recipes and defense notes (1.5 h)
- [ ] *(optional)* Draw the architecture and one data flow for the defense (0.5 h)
- [ ] *(optional)* Write a short report, only if the course asks for one (1 h)


**Done when:** the 2.17 checklist is ticked, the demo has been rehearsed twice, `final-demo` is tagged with its APK, and the professor can open the repo.

**Top risk:** slippage from weeks 2-4 eats the buffer, or the final date lands before 6 Dec.

**Fallback:** the buffer goes to P1 leftovers first. Don't start a P3 item in the last 3 days. If the date moves, re-plan; everything up to `p1-complete` is the brief.

**Next if you have time:** finish P2, then P3 in build order (2.12 → 2.16). Anything not built by the deadline is dropped, reminder first.

## Re-plan when
- The final date is announced.
- Any week ends more than 3 h behind.
- The professor rejects Open-Meteo.

Keep ticked tasks ticked, move the rest forward and re-balance the weeks.
