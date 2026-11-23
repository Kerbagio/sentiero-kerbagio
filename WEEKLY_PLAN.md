# Sentiero Kerbagio — weekly plan

Written 2026-11-01. It is built from the approved design doc (`docs/designs/hiking-trip-planner.md`), `docs/plan.md`, Spec 1 (`docs/specs/01-midterm-slice.md`) and Spec 2 (`docs/specs/02-rest-of-mvp.md`). Nothing is built yet (no commits). Task numbers 2.1-2.17 are Spec 2's child issues, and their hours come from the specs.

## Re-plan, Mon 23 Nov
The real dates: **the midterm presentation is Tue 24 Nov** (the slice is built, and the deck and README are ready), and **the final is around Sun 20 Dec**. Weeks 1-2 are kept below as history. Weeks 3-5 are replaced by weeks 4-7.

- **About 9-10 hours a week, without rushing.** What's left of P1 + release is ~33 h over 4 weeks, so P1 fits with a little room.
- **The cut line is P1 + release.** P2 (the loop) starts only if P1 is finished early. P3 is not planned.
- **The last week stays calm:** finishing Mark as done, delete hike, hardening and rehearsal. No new screens after Sun 13 Dec.

## Original assumptions (1 Nov, kept for history)
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
- [x] Tests: TemplatesTest, HikeDetailViewModelTest, TripDaoTest; manual restart and rotation runs (2 h) *(done in the week 2 catch-up)*
- [x] Rehearse the 5-minute demo with Database Inspector open; tag `midterm-demo` and keep its APK (1 h)


**Done when:** on a fresh install, "Jabal Moussa Reserve", Sat 14 Nov, Moderate opens with 12 items. You tick 3, force-stop, reopen, and see the same 3 ticks. The 3 tests are green, `midterm-demo` is tagged, and the rehearsal fits in 5 minutes.

**Top risk:** Navigation Compose isn't in the slides, and the first Room/KSP build errors can eat a day.

**Fallback:** create → tick → restart is the part you can't cut. If you're behind on Thursday, move the bundled fonts and the rotation check to week 2. Never cut the restart test.

**Next if you have time:** 2.1 (week 2). Weeks 2-4 are full, so resting is also a fine use of spare time.

## Week 2 — Mon 9 – Sun 15 Nov · Finish the midterm properly, edit the checklist, build the weather data layer
**Planned: 13.5 h** (the midterm was built bare; 2.7 Delete moved to week 5)
- [x] Midterm catch-up: the DESIGN.md theme (colors, bundled fonts, type, shapes) (1.5 h)
- [x] Midterm catch-up: TemplatesTest, HikeDetailViewModelTest, TripDaoTest (2 h)
- [x] Midterm catch-up: copy into strings.xml, 80-char cap + counter, double-tap guard, "Couldn't save" snackbar, relative date labels (1 h)
- 2.1 Checklist editing: built on 14 Nov, waiting for its Accept check → moved to week 4
- 2.2 Weather data layer: not started → moved to week 4


**Done when:** the app uses the DESIGN.md theme and Spec 1's 3 tests pass; adding, deleting and Undo work on the emulator; `./gradlew test` passes the 12 new weather tests.

**Top risk:** the professor hasn't confirmed the API, or the converter or JSON setup fights you.

**Fallback:** fixtures and fake APIs keep every test offline, and the `WeatherRepository` interface isolates a change of provider. If you have no answer by Monday, ask the professor in person.

**Next if you have time:** start 2.3 (week 3).

## Week 3 — Mon 16 – Sun 22 Nov
No planned work was done this week. 2.1 had already been built on 14 Nov; 2.2-2.4 move to weeks 4-6.

## Week 4 — Mon 23 – Sun 29 Nov · Midterm presentation, then the weather data layer
**Planned: ~6.5 h** (light: the presentation is on Tuesday)
- [x] Deck cut to 6 slides for the brief, README written, repo pushed and made public (Mon 23 Nov)
- [ ] **Tue 24 Nov: present the midterm** (deck: `presentation/Sentiero-Midterm.pptx`, notes: `docs/MIDTERM_CHEATSHEET.md`)
- [ ] 2.1 Accept check on the phone: add an item, delete it, Undo, zero-items text; then tick it here (0.5 h)
- [ ] 2.2 Retrofit + kotlinx converter, INTERNET permission, AppContainer wiring (1 h)
- [ ] 2.2 `GeocodingApi`, `ForecastApi`, DTOs with nullable fields (1 h)
- [ ] 2.2 `OpenMeteoWeatherRepository`: mapping, rounding, failure classes, `Sentiero/Weather` log (1.5 h)
- [ ] 2.2 Fixtures (capture Tripoli with curl) + JsonFixturesTest, WeatherRepositoryTest, PlaceTest (1.5 h)

**Done when:** the midterm is presented, 2.1 is ticked, and `./gradlew test` passes the new weather tests.

**Top risk:** the converter or JSON setup fights you.

**Fallback:** fixtures and fake APIs keep every test offline. If 2.2 runs over, finish it on Monday of week 5; nothing else needs it before 2.3.

## Week 5 — Mon 30 Nov – Sun 6 Dec · Area search and the forecast panel, every API state
**Planned: ~10 h**
- [ ] 2.3 `AreaSearchState`: search on submit, 2-letter minimum, cancel the previous job (1.5 h)
- [ ] 2.3 `AreaSearchField`: Editing / Picked, results, Empty / Error + Retry (1.5 h)
- [ ] 2.3 Form: "Area (optional)", the "No place selected" hint, area saved on create (1 h)
- [ ] 2.3 Tests: AreaSearchStateTest ×4, HikeFormViewModelTest ×1 (1 h)
- [ ] 2.4 `ForecastCardPolicy` + ForecastCardPolicyTest ×6 (1 h)
- [ ] 2.4 HikeDetailViewModel forecast state (key, job, Retry) + HikeDetailViewModelTest ×3 (2 h)
- [ ] 2.4 `ForecastPanel`: every state at one minimum height, values line with UV, attribution (2 h)

**Done when:**
- "Faraya" makes you pick (Guinea is listed first), and "zzqx" shows the Empty text.
- In airplane mode the panel shows "Couldn't load the forecast." and "You're offline.", and Retry recovers after you reconnect.
- A hike with an area dated today + 16 shows "Forecast available from …".
- A past-dated hike shows no panel.

**Top risk:** async state bugs: out-of-order results, refetching on rotation, a Loading state that never ends.

**Fallback:** AreaSearchStateTest's gated fake reproduces ordering bugs, so fix them in the test first. If the panel runs over, it moves to Monday of week 6.

## Week 6 — Mon 7 – Sun 13 Dec · Completed tab and the journal behind Mark as done
**Planned: ~10 h**
- [ ] 2.4 Add-area sheet (1 h)
- [ ] 2.5 `observeCompleted` query + mapping + TripDaoTest ×3 (1 h)
- [ ] 2.5 `Completed` route, bottom bar, `openTab` (1 h)
- [ ] 2.5 CompletedScreen + ViewModel (rows, empty state), `shortDate` + DateLabelsTest (1.5 h)
- [ ] 2.5 Journal mode (read-only) + due rows on Upcoming (1.5 h)
- [ ] 2.6 Repository: `journalSource`, the `saveJournal` transaction, `reopenHike` + tests ×3 (2 h)
- [ ] 2.6 MarkDoneViewModel (draft, `hasChanges`, double-tap guard) + tests ×3 (2 h)

**Done when:** a hike with no area offers Add area, the Completed tab and journal mode work, and the journal repository and ViewModel tests pass.

**Top risk:** the journal transaction and its Undo are the trickiest data code in the app.

**Fallback:** the tests come first. If the week runs over, the due rows on Upcoming move to Monday of week 7.

## Week 7 — Mon 14 – Sun 20 Dec · Finish Mark as done, harden, rehearse (final ~Sun 20 Dec)
**Planned: ~10 h, no new screens after Wednesday**
- [ ] 2.6 MarkDoneSheet + the discard gate (2 h)
- [ ] 2.6 Both hosts (pinned button on detail, row button on Upcoming) + Undo snackbar; tag `p1-complete` (1 h)
- [ ] 2.7 Delete hike: ⋮ menu, confirmation dialog, cascade, `Deleted` state, cascade test (2 h)
- [ ] 2.17 Accessibility: 10-minute TalkBack pass, 200% font, 48dp targets (1 h)
- [ ] 2.17 Rotation at the 3 points, the 3 log tags, greps for catch-alls and the word "trip" (1 h)
- [ ] 2.17 Update the README (weather, Completed, journal) and a permissions review: INTERNET only (0.5 h)
- [ ] 2.17 Tag `final-demo`, attach the APK to a GitHub release (0.5 h)
- [ ] Rehearse the final demo twice, timed, with Spec 2's states-on-demand recipes and defense notes (1.5 h)

**Done when:** all four brief bullets work (`p1-complete`), the 2.17 checklist is ticked, the demo has been rehearsed twice, and `final-demo` is tagged with its APK.

**Top risk:** weeks 4-6 slip and squeeze the rehearsal.

**Fallback:** if you're behind on Wed 16 Dec, cut in this order: first the Upcoming row host of Mark as done (the detail host is enough), then 2.7 Delete hike. Never cut the rehearsal or the README update. Start nothing new after Thu 17 Dec.

**Next if you have time (any week, only after `p1-complete`):** P2 in order: 2.8 Chips (4 h) → 2.9 Start from last hike (6 h) → 2.10 Add something you forgot (4 h) → 2.11 Same-route note (2 h).

## Re-plan when
- The final date changes.
- Any week ends more than 3 h behind.
- The professor rejects Open-Meteo.

Keep ticked tasks ticked, move the rest forward and re-balance the weeks.
