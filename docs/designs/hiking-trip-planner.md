# Design: Sentiero Kerbagio, Hiking Trip Planner and Trail Journal

Written 2026-11-01
Branch: main
Repo: hiking-planner (local, not yet pushed)
Status: APPROVED
Mode: Builder (course final project, Android Development, USJ INCI)

## Problem Statement

Course topic brief #8. Hikers want to prepare a simple trip, organize a packing checklist, and record completed outings. Two pains from the brief: trip preparation misses small but important items, and notes from previous routes are hard to find later.

Build context: solo, Kotlin + Jetpack Compose, Android Studio. The midterm demo is on or before Sun 2026-11-08. The final due date has not been announced; the plan assumes about 5 weeks (around 2026-12-06).

## What Makes This Cool

The loop. Packing apps (PackPoint, Packr) build lists from the weather. Hiking journals store notes and photos. They are separate products. The brief's two pains are really one memory problem, so this app closes the loop:

- The forecast for the hike date suggests packing items ("Rain 70% → + Rain jacket"). The API changes what you pack. It is not a decorative card.
- When you mark a hike done, you write notes and flag what you forgot, including things that were never on the list. The next trip can start from that list, with forgotten items placed first.
- Notes come back when they matter: a searchable journal, and a "You hiked this on ..." note on any upcoming trip that repeats a route you already did.

Demo sentence: "I forgot my headlamp at Chouf. When I planned Jabal Moussa, the headlamp was already at the top of my list, and the 70% rain forecast suggested a rain jacket."

## Constraints

- Brief scope boundary: no live navigation, no emergency guidance, no location tracking, no safety guarantees. Weather suggestions are advisory and always labelled "Forecast from Open-Meteo. Always check conditions before you go."
- Grading model (from the course slides): the midterm needs a working demo, a defense of the problem and scope, and an explanation of the state and data flow. Every module's assessment question is "Why does this value live here?" Code the student cannot explain does not count.
- Course stack only: Compose, ViewModel + StateFlow + sealed UiState, Repository, Room (Entity, DAO with Flow + suspend writes), DataStore for preferences, Retrofit for network, WorkManager + notification channels for background work. No Hilt, no multi-module.
- Navigation Compose is required by the brief but is not in the uploaded slides. The student learns it independently.
- Open-Meteo: free for non-commercial use, no API key, forecast horizon 16 days (today through today + 15), includes a geocoding API.
- Permissions only when justified: INTERNET is a normal permission with no prompt. POST_NOTIFICATIONS (Android 13+) is requested only if the optional reminder is built, and only when the user turns a reminder on.
- Solo project in a course designed for pairs. The "show each member's contribution" item becomes "show your own commit history".

## Premises (agreed)

1. The brief is the contract. MVP = create trip (date, route name, difficulty, packing checklist); Upcoming and Completed on separate screens; trips and checklist progress persisted in Room; one API with visible Loading / Empty / Error / Retry states.
2. The grade is won in the explanation, not the feature count. Course stack only; every line must be defensible in its layer.
3. The one API is weather (Open-Meteo), not trail data. No free, reliable trail API exists. The user types an area name, picks the right match, and geocoding gives its coordinates. No location permission.
4. "Trail Journal" = notes written when the user marks a trip completed. Completion is an explicit user action, never automatic when the date passes. Photos are a stretch goal (Photo Picker, no permission needed).
5. The midterm is one vertical slice that survives an app restart: create trip → tick packing items → saved detail screen. No API and no Completed screen before the midterm.

## Approaches Considered

- **A: By the slides.** One module, Room + ViewModel + Retrofit, weather card on the detail screen. Kept as the foundation and the fallback: if time runs out, the app degrades to A and still meets the brief.
- **B: Production architecture.** Multi-module, Hilt, offline weather cache refreshed by WorkManager. Ruled out: not taught, hard to defend in Q&A, setup cost too high solo with a 7-day midterm.
- **C: The loop.** Chosen. See below.

## Recommended Approach: C, the loop, built A-first

Layers (one direction, as in the slides): Compose screen → screen ViewModel exposing `StateFlow<UiState>` → `TripRepository` (Room) and `WeatherRepository` (Retrofit) → data sources. Manual dependency injection through an `AppContainer` created in the `Application` class.

Data model sketch (to be finalized in the engineering review):
- `trips`: id, routeName, date (calendar date, not a timestamp), difficulty (Easy / Moderate / Hard), areaLabel, latitude, longitude (all three optional, set together), completedAt (null = not done), notes, sourceTripId (optional: the completed trip this list was copied from).
- `checklist_items`: id, tripId (foreign key, cascade delete), label, isPacked, position, forgotten (set on this trip's own mark-as-done sheet), carriedOver (true when the item was forgotten on the source trip; drives the "forgot last time" tag).

### Behavior rules

Trips
1. Upcoming = trips with no completedAt, sorted by hike date ascending (past-dated trips therefore sit on top).
2. Completed = trips with completedAt, sorted by hike date descending, then completedAt descending as the tie-breaker. This screen is the journal.
3. "Last hike" = the first trip in the Completed order.
4. Completion is offered only from the hike date onward. On the hike date the Upcoming card reads "Today · Mark as done when you're back"; after it, "Date passed · Mark as done". Before the hike date the detail screen shows no Mark as done button. A trip created with a past date (logging an old hike) can be completed right away.
5. The mark-as-done sheet always opens from the trip's saved notes and forgotten flags, whether opened by Mark as done or by Edit journal (a first completion therefore starts empty). It has a notes field, the trip's items to tick as "forgot this", and an "Add something you forgot" field that adds a new item to this trip with forgotten = true and isPacked = false. Saving from Mark as done writes the notes, flags and added items, sets completedAt, and shows a snackbar whose Undo is an immediate Move back to Upcoming (rule 6): it clears completedAt and keeps the notes, flags and added items. Saving from Edit journal updates the notes, flags and added items only, keeps completedAt unchanged, and shows no Undo.
6. A completed trip opens in journal mode: the same detail screen with a read-only checklist (ticks and "forgot" flags visible), the notes, no forecast card, no add-item, no Mark as done. "Edit journal" reopens the sheet prefilled. The overflow menu has "Move back to Upcoming", which clears completedAt and keeps notes and flags.
7. Every trip's overflow menu has Delete (confirmation dialog; its items are deleted with it). Upcoming trips also have Edit, which reuses the new-trip form for route name, date, difficulty and area. Delete and Edit are the exits for a cancelled or rescheduled hike, so nothing has to be marked done falsely.

Checklist
8. Items are shown in position order. Ticking never moves an item.
9. A new trip started from the template gets the template items in template order. A new trip started from the last hike gets that hike's forgotten items first (old order, carriedOver = true), then its other items (old order). Every copied item starts unpacked with forgotten = false. carriedOver stays on that trip as history. It is never copied itself: on the next copy it is set only for items forgotten on the trip being copied. The new trip stores sourceTripId.
10. "+ Add item" and accepted suggestions are appended at the end.
11. Each item can be deleted (✕ on the row, with an Undo snackbar). There is no rename: delete and add again.
12. The new-trip form shows a "Last hike: <route> (<n> items)" chip only when at least one completed trip exists. Otherwise only the template chip shows, preselected.

Notes, for "hard to find later"
13. Completed has a search field that filters by route name or notes text (case-insensitive contains).
14. When an upcoming trip's route name matches a completed trip's route name (case-insensitive, trimmed), its detail screen shows "You hiked this on <date>: <first 140 characters of notes>", linking to that journal entry. With several matches, the most recent in Completed order wins.

Area and weather
15. Area lookup is a free-text search against the Open-Meteo geocoding API, up to 5 results. Each result shows name, region and country, and the user must tap one; no tap means no area. Helper text: "Pick the village nearest the trailhead. Mountain weather can differ from the coast." The trip stores the chosen label with its latitude and longitude.
16. Search states: Loading; Results; Empty, which is the API's empty state ("No places found for '<query>'. Try the nearest village or town."); Error ("Couldn't search right now." + Retry).
17. Saving a trip never waits on the network. No area, a failed search, or no pick all save normally.
18. The forecast card state is decided locally, in this order, before any request is made:
    1. Completed trip: no card.
    2. Hike date before today: no card, no request, no suggestions.
    3. No area: "Add an area to see the forecast." with an Add area button that opens the same search and saves the pick to this trip.
    4. Hike date after today + 15: "Forecast available from <hike date minus 15 days>."
    5. Otherwise request that single day. Loading, then Content ("Forecast for <area label> · <date>": min to max temperature, rain chance, max wind, suggestions, attribution line) or Error with Retry. A response with no row for the date is shown as Error with "No forecast for this date yet."
19. Suggestion rules, using the daily values for the hike date. This table is the contract the unit tests check:

| Condition | Suggested item | Suppressed if an item label contains |
|---|---|---|
| precipitation_probability_max ≥ 50 % | Rain jacket | rain jacket, raincoat, rain coat, poncho, waterproof jacket |
| precipitation_probability_max ≥ 50 % | Dry bag | dry bag, waterproof bag |
| temperature_2m_min ≤ 5 °C | Warm layer | warm layer, fleece, down jacket |
| temperature_2m_min ≤ 5 °C | Gloves | gloves |
| temperature_2m_max ≥ 28 °C | Extra water (1 L) | extra water |
| uv_index_max ≥ 6 | Sunscreen | sunscreen, sun cream |
| wind_speed_10m_max ≥ 40 km/h | Windproof jacket | windproof, windbreaker, rain jacket |

   Matching is case-insensitive on trimmed labels. At most 3 chips show, in table order, after suppression. Tapping a chip appends an unpacked item with that exact label; the chip then disappears because the item exists.

Build order:
1. Midterm slice (by 2026-11-08): project setup, Room trips + items, new-trip form with the checklist template, trip detail with tick, untick, add and delete item, Upcoming list, restart-proof.
2. Upcoming vs Completed split, mark-as-done sheet (notes, forgot flags, add forgotten item), journal mode, Undo and Move back, Delete and Edit, Completed search.
3. Weather: area search in the form and from the No area card, forecast card with all states.
4. Forecast-driven packing suggestions (pure Kotlin rules from the table, unit-tested).
5. "Start from last hike" with carried-over items first, and the same-route "You hiked this" note.
6. Optional: evening-before reminder with WorkManager + notification channel.
7. Stretch: photos on completed trips through the Photo Picker.

Wireframe: docs/wireframe-core-flow.png
Visual source of truth (design review, DR-4A): `DESIGN.md` + `docs/design-preview/`. The wireframe documents flow only; its cards, emoji and grayscale styling are superseded. Later decisions are recorded in `docs/plan.md`, which wins where it differs from this doc.

## Open Questions

1. Final due date: not announced. Re-plan when it is.
2. Does "one agreed API" require explicit professor approval? Treated as yes; see The Assignment.
3. Checklist templates: one "day hike" template, or one per difficulty level? Decide in the product review.
4. Reminder notification and photos: in, optional, or cut? Decide in the product review.

## Success Criteria

Midterm (on or before 2026-11-08):
- On an emulator: create a trip with name, date and difficulty, starting from the template checklist; tick 3 items; open the detail screen; force-stop the app; reopen; the trip and the same 3 ticks are still there.
- Explain out loud the path of one tick: checkbox event → ViewModel → repository → DAO update → Room Flow emits → StateFlow → recomposition.

Final:
- All four MVP bullets from the brief work on a device or emulator.
- Every weather and search state can be shown on demand: Empty (search for "zzqx"), Error (airplane mode, then Retry), Too far ahead (a date more than 15 days away), No area (a trip saved without an area), past date (no card), Content.
- Marking a trip done moves it to Completed with its notes; a new trip started from it shows the forgotten items first, tagged "forgot last time".
- Automated tests pass: DAO queries, at least one ViewModel, and the suggestion rules table.
- The student can answer "why does this value live here?" for every class.

## Distribution Plan

A course deliverable, not a store release. Source in a private GitHub repository with a commit history that shows solo authorship, a debug APK, and a live demo on an emulator or device. Optional CI: a GitHub Actions workflow that runs `./gradlew test assembleDebug` on push.

## Next Steps

1. Create the Android Studio project (Empty Activity, Compose, Material 3), commit, push to GitHub.
2. Room: `TripEntity`, `ChecklistItemEntity`, DAOs, `AppDatabase`, `TripRepository`, `AppContainer`.
3. New-trip form and a hard-coded day-hike template.
4. Trip detail: tick, untick, add and delete item.
5. Upcoming list and Navigation Compose between the three screens.
6. Restart test, then a rehearsal of the data-flow explanation.

## Review notes

Disposition: COMPLETED

Stop: PASS

No blocking findings remain. The 32 open minor finding(s) below are recorded here, not fixed.

### R3-1 — clarity (minor)

**Problem**

> Rule 12 says the template chip is preselected when it is the only chip, but not which chip is preselected when the 'Last hike' chip also shows (the wireframe draws 'Last hike: Chouf Cedars (11)' selected). That default decides whether the demo's forgotten-items-first list appears without an extra tap.

**Remedy**

> State the default when both chips show (for example 'Last hike' preselected) and that exactly one chip is always selected.

### R3-2 — completeness (minor)

**Problem**

> Rule 15 does not say what the area field shows when a name was typed but no result picked: the trip saves without an area while the field may still display the typed text, so the user believes an area is set. For rule 7's Edit it also does not say that an untouched stored area is kept, or how an area is removed.

**Remedy**

> State that unpicked text is flagged in the form (for example 'No place selected') and dropped on Save, that Edit keeps the stored area unless a new result is picked, and how an area is cleared (for example a clear button on the field).

### R3-3 — clarity (minor)

**Problem**

> Rule 15 does not say whether the area search runs on submit (keyboard search action or a button) or as the user types; as-you-type needs a debounce, a minimum query length, and discarding of out-of-order responses so Results always match the current text.

**Remedy**

> Choose the trigger; if search runs as the user types, state the debounce delay, the minimum length and that a newer query cancels the older request.

### R3-4 — feasibility (minor)

**Problem**

> Rule 18 compares the hike date with the device's local today, but Open-Meteo aggregates daily values over the day of the request's timezone parameter, which defaults to GMT. Without timezone=auto (or Asia/Beirut), the daily min, max and rain chance cover a day shifted by two to three hours, and for two to three hours after local midnight in Lebanon the 'today + 15' window runs one day past the API's, so a hike at the far edge is requested outside the API's range and shows as Error.

**Remedy**

> Specify the forecast request: the stored coordinates, the daily fields from rule 19, start_date = end_date = hike date, and timezone=auto.

### R3-5 — completeness (minor)

**Problem**

> Rule 19's table and rule 18.5's Content assume every daily value for the hike date is present; neither says what a missing (null) value does, and depending on the JSON converter a null in a non-null Kotlin field either fails parsing or crashes later.

**Remedy**

> State that a missing value never triggers its rule and shows as a dash on the card, model those response fields as nullable, and add the missing-value case to the unit-test table.

### R3-6 — clarity (minor)

**Problem**

> Rule 18.5's Content shows temperature, rain chance and wind but not the UV index (the wireframe's card likewise shows only temperature, rain and wind), while rule 19 suggests Sunscreen from uv_index_max, so a Sunscreen chip can appear with no visible reason on the card, weakening the 'the API changes what you pack' explanation.

**Remedy**

> Show the UV max on the Content card (for example 'UV 7') or drop the UV rule.

### R3-7 — consistency (minor)

**Problem**

> Rule 19's Windproof jacket row is suppressed by 'rain jacket' but not by the Rain jacket row's synonyms 'raincoat', 'rain coat' and 'waterproof jacket', so on a windy, rainy day a list with 'Raincoat' can still get a Windproof jacket chip while a list with 'Rain jacket' cannot.

**Remedy**

> Add the rain-jacket synonyms (poncho excepted) to the Windproof jacket row, or drop 'rain jacket' from it, and cover both cases in the rules tests.

### R3-8 — scope (minor)

**Problem**

> sourceTripId is stored (data model, rule 9) but no rule reads it: the 'forgot last time' tag comes from carriedOver and the same-route note from route names. It also goes stale when its trip is deleted (rule 7) or moved back to Upcoming (rule 6, or rule 5's Undo), and if declared as a Room foreign key with the default NO_ACTION, deleting a source trip fails with a constraint error. Under the course's 'Why does this value live here?' question, a field nothing reads is hard to defend.

**Remedy**

> Drop sourceTripId, or name the behavior that reads it and declare it with onDelete = SET_NULL.

### R3-9 — consistency (minor)

**Problem**

> What Makes This Cool promises the 'You hiked this' note on any upcoming trip that repeats a route, but rule 14 matches only identical free-text route names (case-insensitive, trimmed), so 'Jabal Moussa' never matches 'Jabal Moussa Reserve' and the note silently does not appear.

**Remedy**

> Suggest completed route names in the route-name field so a repeat reuses the exact name, or narrow the claim to trips with the same route name.

### R3-10 — completeness (minor)

**Problem**

> Rule 14's note quotes the first 140 characters of the matching trip's notes, but nothing requires notes, so a match with empty notes would read 'You hiked this on <date>: ' with nothing after it.

**Remedy**

> When the matching trip has no notes, show 'You hiked this on <date>' alone, still linked to the journal entry.

### R3-11 — completeness (minor)

**Problem**

> Rule 13 does not say what Completed shows when the search matches nothing.

**Remedy**

> Add no-match copy (for example: No hikes match <query>.) with a way to clear the search.

### R3-12 — clarity (minor)

**Problem**

> The wireframe draws Completed's search field below the entries; built as the last list item, it is reachable only after scrolling past the whole journal, which works against the 'hard to find later' pain it is meant to answer.

**Remedy**

> Put the search field at the top of Completed (or pin it) and say so in rule 13.

### R3-13 — clarity (minor)

**Problem**

> Forgotten items reach only trips created from the last hike after it was marked done, because rule 9 copies at creation. Trips already planned, like the wireframe's Jabal Moussa and Qadisha while Baatara is still pending, never receive items flagged later, nor flags changed afterwards through Edit journal, and the design does not state this limit of the loop.

**Remedy**

> State the limitation in the rules and the defense notes, and order the demo so Chouf is marked done before Jabal Moussa is created.

### R3-14 — feasibility (minor)

**Problem**

> The Empty recipe assumes the query 'zzqx' returns no places, but Open-Meteo's geocoding search fuzzy-matches queries of three or more characters, so the recipe is unverified and could show Results during grading.

**Remedy**

> Run the query before the demo and keep a backup nonsense query that is confirmed to return no results.

### R3-15 — clarity (minor)

**Problem**

> The Too far ahead recipe does not say the trip needs an area; rule 18 checks No area before Too far ahead, so a far-dated trip saved without an area shows No area instead.

**Remedy**

> Write the recipe as a trip with an area dated more than 15 days ahead.

### R3-16 — clarity (minor)

**Problem**

> Rule 18.5 gives copy only for the no-row case ('No forecast for this date yet.'); the general forecast Error has no copy, so whether server or request errors are worded as a connection problem, with a Retry that cannot help, is still undecided.

**Remedy**

> Give the forecast Error neutral copy like the search's (for example 'Couldn't load the forecast.'), or distinguish offline failures from other errors.

### R3-17 — clarity (minor)

**Problem**

> The midterm demo script is out of order: the criterion reads 'tick 3 items; open the detail screen; force-stop the app' and Premise 5 reads 'create trip, tick packing items, saved detail screen', but ticking exists only on the detail screen. Where Save navigates (the new trip's detail screen or Upcoming) is unstated.

**Remedy**

> Rewrite the script in executable order (create trip, land on or open its detail screen, tick 3 items, force-stop, reopen, open the trip, see the same 3 ticks) and state where Save navigates.

### R3-18 — completeness (minor)

**Problem**

> If Open Question 4 keeps the evening-before reminder, its behavior is unspecified: firing time, where the on/off setting lives (per trip in Room or global in DataStore), what happens when POST_NOTIFICATIONS is denied, the notification text, and cancelling or rescheduling the work when the trip is completed, deleted or re-dated (all possible through rules 5 and 7).

**Remedy**

> Write these behaviors before building step 6, or cut the reminder in the product review.

### R3-19 — feasibility (minor)

**Problem**

> Stretch photos rely on the Photo Picker alone; by default access to a picked photo's URI lasts only until the app stops or the device restarts, so storing the URI alone in Room would show broken photos after a restart.

**Remedy**

> If photos are built, take a persistable URI permission on the picked image or copy it into app-private storage, and say which.

### R3-20 — completeness (minor)

**Problem**

> The Distribution Plan relies on a private GitHub repository whose history must show solo authorship but does not say how the professor gets access (for example a collaborator invite) or where the debug APK is delivered.

**Remedy**

> Add the access step and the APK delivery location to the distribution plan.

### R3-21 — clarity (minor)

**Problem**

> Approach A is 'the foundation and the fallback' that 'still meets the brief', but A's description omits the Completed screen and mark-as-done the brief requires, and the build order marks no fallback cut line. Step 2 bundles the split, the mark-as-done sheet, journal mode, Undo and Move back, Delete and Edit, and search, so what is dropped first when time runs short is unclear.

**Remedy**

> State the fallback as a cut line in the build order (for example steps 1-3, with step 2 reduced to the split, the mark-as-done sheet and Delete) and the order in which the remaining step-2 items are dropped.

### R3-22 — feasibility (minor)

**Problem**

> Room DAO tests normally run as instrumented tests on an emulator (./gradlew connectedAndroidTest) unless Robolectric is added, so the optional CI command './gradlew test assembleDebug' would not run the DAO tests the final criteria require; the design does not say where each kind of test runs.

**Remedy**

> State that DAO tests are instrumented (or add Robolectric) and that the optional CI covers only the JVM unit tests (ViewModel and the suggestion rules table).

### R3-23 — completeness (minor)

**Problem**

> Only the Upcoming empty state is defined ('No trips planned. Plan your next hike.', in the wireframe caption); the Completed screen's empty state, before any trip is marked done, is undefined in the rules and the wireframe.

**Remedy**

> Add Completed empty-state copy, for example pointing the user to Mark as done.

### R3-24 — feasibility (minor)

**Problem**

> The schema grows after the midterm (completedAt, notes, forgotten, areaLabel, latitude, longitude, sourceTripId and carriedOver are first used in build steps 2-5), but the design does not say whether the midterm database already has these columns or how later Room version bumps are handled; changing the schema without bumping the version makes Room fail at startup, and a destructive migration would wipe the trips created for the midterm.

**Remedy**

> Either create the full sketched schema in the midterm slice, or plan version bumps with migrations (or accept destructive migration and say so).

### R3-25 — completeness (minor)

**Problem**

> The on-demand criterion covers Empty, Error, Too far ahead, No area, past date and Content but gives no recipe for Loading (it is not even listed), which on a normal connection can flash too briefly for a grader to see, although a visible Loading state is in Premise 1's contract.

**Remedy**

> Add a Loading recipe that needs no production code change, for example the emulator's network speed and latency throttling.

### R3-26 — completeness (minor)

**Problem**

> The grading model lists three midterm items (a working demo, a defense of the problem and scope, an explanation of the state and data flow), but the midterm success criteria and Next Steps 6 prepare only the demo and the data-flow explanation.

**Remedy**

> Add a midterm criterion or step to rehearse the problem and scope defense (the brief as contract, weather instead of trail data, what is out of scope).

### R3-27 — clarity (minor)

**Problem**

> Decisions are routed to the product review (Open Questions 3-4) and the engineering review (the data model sketch), and two specs are planned, but Next Steps goes straight to coding and schedules none of these relative to the midterm.

**Remedy**

> State when each review and a spec run happens (for example a spec for the midterm slice now; the CEO and eng reviews after the midterm demo, before build step 2).

### R3-28 — completeness (minor)

**Problem**

> Rule 5 now says what each Save writes and that the sheet always reopens from the saved notes and flags, which implies that closing it without Save (back gesture, swipe down or a tap outside the sheet) discards the typed notes, flag changes and added items and leaves the trip as it was, but the rule never says so, and nothing protects a long journal entry from an accidental dismiss.

**Remedy**

> State that closing the sheet without Save writes nothing and leaves the trip unchanged (a dismissed Mark as done sheet does not complete the trip), and choose whether unsaved notes trigger a discard confirmation or are dropped silently.

### R3-29 — completeness (minor)

**Problem**

> Rule 5 lets both Mark as done and Edit journal add items through the sheet, but nothing removes one: the sheet only toggles 'forgot this', and rule 6 makes the journal checklist read-only, so rule 11's delete is unavailable on a completed trip. A mistyped or wrong added item stays in the journal and is copied first, tagged 'forgot last time', into the next trip started from that hike; unticking 'forgot this' in Edit journal only turns it into an ordinary unpacked item that is still copied, and removing it takes Move back to Upcoming, a delete and Mark as done again, which re-stamps completedAt.

**Remedy**

> Let rows added in the sheet be removed before Save, and state how an added item is removed after Save (for example Edit journal can delete items that were added through the sheet), or name Move back, rule 11's delete and Mark as done again as the intended path.

### R3-30 — clarity (minor)

**Problem**

> Rule 8 shows items in position order and rule 10 names what is appended at the end ('+ Add item' and accepted suggestions), but items added through the sheet's 'Add something you forgot' field get no stated position, although it decides where they appear in the journal checklist and, through rule 9's old order, where they sit among the forgotten items copied first into the next trip.

**Remedy**

> State that items added in the sheet are appended at the end in the order they were added, for example by naming them in rule 10.

### R3-31 — clarity (minor)

**Problem**

> Rule 5 says saving from Mark as done shows an Undo snackbar but not where the user is afterwards. Mark as done is offered on the Upcoming card (rule 4; the wireframe draws a 'Mark as done' button on Baatara's card) and on the detail screen, and the user could stay where they were (the detail screen switching to journal mode) or be taken to Completed, as the wireframe caption 'Save moves it here (Undo snackbar)' can be read. That decides which screen shows the snackbar and what the user sees when Undo returns the trip to Upcoming.

**Remedy**

> State which screen the user is on after Save from each entry point (for example: they stay where they were, the detail screen switching to journal mode) and that the Undo snackbar shows there.

### R3-32 — completeness (minor)

**Problem**

> The 'Add something you forgot' field accepts any label, so a user who overlooks an item already on a long list (the wireframe's trips have 11 and 14 items) and types it again creates a duplicate, and the next trip started from that hike lists it twice: first, tagged 'forgot last time', and again in its old place. The wireframe caption describes the field as adding 'a forgotten item that was never on the list'.

**Remedy**

> When the typed label matches an existing item of the trip (case-insensitive on trimmed labels, as in rule 19), flag that item as forgotten instead of adding a copy.

### Prior finding evidence

**R2-3 → R3-1 (persisting)**

> Rule 12 is unchanged ('Otherwise only the template chip shows, preselected') and names no default when both chips show; the wireframe still draws 'Last hike: Chouf Cedars (11)' selected.

**R2-4 → R3-2 (persisting)**

> Rule 15 is unchanged ('the user must tap one; no tap means no area') and still says nothing about unpicked text, keeping a stored area on Edit, or clearing an area.

**R2-5 → R3-3 (persisting)**

> Rule 15 is unchanged and still names no search trigger, debounce, minimum length or stale-response handling.

**R2-6 → R3-4 (persisting)**

> Rule 18.5 still says only 'Otherwise request that single day', with no request parameters and no timezone.

**R2-7 → R3-5 (persisting)**

> Rules 18.5 and 19 are unchanged and still do not say what a missing daily value does or that the response fields are nullable.

**R2-8 → R3-6 (persisting)**

> Rule 18.5's Content still lists 'min to max temperature, rain chance, max wind' without UV while rule 19 keeps the uv_index_max Sunscreen row; the wireframe's forecast card also shows no UV.

**R2-9 → R3-7 (persisting)**

> Rule 19's Windproof jacket row still reads 'windproof, windbreaker, rain jacket', without 'raincoat', 'rain coat' or 'waterproof jacket'.

**R2-10 → R3-8 (persisting)**

> The data model still lists sourceTripId and rule 9 still ends 'The new trip stores sourceTripId.', while no rule reads it and no onDelete is stated.

**R2-11 → R3-9 (persisting)**

> What Makes This Cool still promises the note 'on any upcoming trip that repeats a route you already did' while rule 14 still requires identical route names.

**R2-12 → R3-10 (persisting)**

> Rule 14 still renders 'You hiked this on <date>: <first 140 characters of notes>' with no empty-notes case, and rule 5 still does not require notes.

**R2-13 → R3-11 (persisting)**

> Rule 13 is unchanged and gives no copy for a search with no matches.

**R2-14 → R3-12 (persisting)**

> Rule 13 is unchanged and does not place the search field, and the wireframe still draws 'Search notes and routes' below the Completed entries.

**R2-15 → R3-13 (persisting)**

> Rule 9 still copies forgotten items only at creation, and no rule or defense note states that earlier-planned trips miss later flags; the wireframe still shows Jabal Moussa created from Chouf while Baatara is pending, and rule 5's Edit journal now also changes flags after a copy.

**R2-16 → R3-14 (persisting)**

> The final on-demand criterion still uses a zzqx search for Empty, with no confirmed result and no backup query.

**R2-17 → R3-15 (persisting)**

> The criterion still reads 'Too far ahead (a date more than 15 days away)' with no area requirement, and rule 18 still checks No area (18.3) before Too far ahead (18.4).

**R2-18 → R3-16 (persisting)**

> Rule 18.5 is unchanged: copy exists only for the no-row case, and the wireframe caption says only 'Error + Retry'.

**R2-19 → R3-17 (persisting)**

> The midterm criterion still reads 'tick 3 items; open the detail screen; force-stop the app', Premise 5 is unchanged, and neither the rules nor the wireframe (whose new-trip caption covers only when Save is enabled and past dates) state where Save navigates.

**R2-20 → R3-18 (persisting)**

> Open Question 4 still leaves the reminder open, and build step 6 still reads only 'Optional: evening-before reminder with WorkManager + notification channel.'

**R2-21 → R3-19 (persisting)**

> Premise 4 ('Photo Picker, no permission needed') and build step 7 are unchanged, with no persistable URI permission or app-private copy.

**R2-22 → R3-20 (persisting)**

> The Distribution Plan is unchanged: no professor access step for the private repository and no APK delivery location.

**R2-23 → R3-21 (persisting)**

> Approach A still reads 'Kept as the foundation and the fallback: if time runs out, the app degrades to A and still meets the brief', and build step 2 still bundles every journal feature with no cut line.

**R2-24 → R3-22 (persisting)**

> The optional CI is still './gradlew test assembleDebug' while the final criteria still require DAO query tests; where DAO tests run is still unstated.

**R2-25 → R3-23 (persisting)**

> The Upcoming empty copy exists only in the wireframe caption, and neither the rules nor the wireframe's Completed caption define Completed's empty state.

**R2-26 → R3-24 (persisting)**

> The data model sketch and the build order are unchanged, with no statement on midterm columns, version bumps or migrations.

**R2-27 → R3-25 (persisting)**

> The on-demand criterion still lists Empty, Error, Too far ahead, No area, past date and Content; Loading appears only in the wireframe's card-state caption, with no recipe.

**R2-28 → R3-26 (persisting)**

> The midterm criteria and Next Steps 6 ('Restart test, then a rehearsal of the data-flow explanation') still omit the problem and scope defense.

**R2-29 → R3-27 (persisting)**

> Open Questions 3-4 still defer to the product review and the data model to the engineering review, and Next Steps 1-6 still schedule neither review nor the two a spec runs.
