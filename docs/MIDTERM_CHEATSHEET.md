# Midterm cheat sheet: Sentiero Kerbagio

The short version of `MIDTERM_DEFENSE.md`. Read it the morning of the demo.

---

## 1. The 30-second pitch

> "Topic 8, Hiking Trip Planner. The problem: hikers pack from memory and forget small things. My midterm feature: create a hike, get a packing list that fits its difficulty, tick items as you pack, and see the saved detail. Everything is saved in a Room database on the phone, so it works offline and survives closing the app. Built with Compose, Navigation Compose, ViewModel + StateFlow, and Room."

**Out of scope (from the brief):** live navigation, location tracking, emergency guidance, safety guarantees. Never say "it keeps you safe."

---

## 2. Value of each feature (he WILL ask)

| Feature | Value in one line |
|---|---|
| **Create a hike** | Plan a hike in one step: route, date, difficulty. The difficulty already picks the packing list. |
| **Packing checklist** | No packing from memory: start from a list that fits the hike (10 / 12 / 14 items) and see what's left. |
| **Upcoming hikes** | One screen shows the next hike and which one isn't packed yet ("3 of 12 packed"). |
| **Saved on the phone** | Pack over two days, offline. Nothing you ticked is lost, even if the app is killed. |
| **Whole app (final)** | Weather adds the right items. The journal remembers what you forgot, so the next trip starts with those first. |

"Why not a notes app?" → You'd retype the list every time. Here the list fits the hike, and each hike has its own progress.

---

## 2b. The deck, slide by slide (6 slides)

| # | Slide | Say in one line |
|---|---|---|
| 1 | Title · "For people who plan and pack for their own day hikes" | "Sentiero means trail in Italian. It's for people who plan and pack for their own day hikes." |
| 2 | The problem, the value, the midterm goal | "Hikers pack from memory. My feature gives each hike a ready list, tick by tick, saved on the phone." |
| 3 | The feature · Plan a hike, then pack it | Form (route, date, difficulty, Save grey until complete) → 10/12/14 items → every tick saved → "3 of 12". |
| 4 | Upcoming hikes, saved on the phone | Sorted by date, "3 of 12 packed" from one SQL query, Back goes home, still 3/12 after a force-stop. |
| 5 | How it's built | Screens → ViewModel → Repository → DAO → SQLite. One tick, end to end. Events go up, state comes down. |
| 6 | Done since the demo, and what's next | See below. |

**Slide 6, left column: built since the midterm demo.** If he asks you to show one, open a hike and add an item, then delete it and tap Undo.
- **DESIGN.md theme:** my own colors and two bundled fonts (Barlow Condensed, Source Sans 3), not Android Studio's default.
- **Add your own items, delete with Undo:** a new item goes at the end. Undo puts it back in the same place, with the same tick and the same id.
- **"Today", "Tomorrow", "in 5 days" labels** on Upcoming, plus "Date passed" for past hikes.
- **Safer Save:** the name is capped at 80 characters with a counter, a double tap can't save twice (`isSaving`), and a database error shows "Couldn't save. Try again." and keeps what you typed.
- **4 test files:** `TemplatesTest` and `HikeDetailViewModelTest` run on the JVM; `TripDaoTest` and `RoomTripRepositoryTest` run on a device.
- **README and a public GitHub repo.**

**Slide 6, right column: next, week by week.**

| Week | What | Why it matters |
|---|---|---|
| 4 · 24–29 Nov | Weather: Open-Meteo, area search, forecast panel | The first API, with loading, error, offline + Retry |
| 5 · 30 Nov – 6 Dec | Completed tab, Mark as done + journal, delete hike, then weather chips | **The whole brief works** (`p1-complete`). Chips: rain likely → "+ Rain jacket" |
| 6 · 7–13 Dec | Start from last hike, add what you forgot, same-route note; edit hike and journal | The loop: what you forgot comes first next time (`p2-complete`) |
| 7 · 14–20 Dec | Search, evening-before reminder, final checks, release | Final ~20 Dec |

**If he asks "is that realistic?"**
> "It follows a written plan with hours for each task, about 18 hours a week. The order matters: the brief's features are done first, in week 5. If I run late, I drop from the end. The reminder goes first, and nothing new starts after the 17th."

**If he asks "why is the reminder last?"**
1. **It depends on three other features** (2.6, 2.7, 2.14). Mark as done must cancel it, delete hike must cancel it, and edit hike must move it when the date changes. Built last, it's written once, against the finished app.
2. **It's not in the brief.** It's P3, an extra; the brief's four bullets are P1.
3. **It's the most expensive and riskiest task:** ~8 h, plus WorkManager, a notification channel, a runtime permission and battery limits.
4. **It's the first thing dropped.** Nothing depends on it, so if it isn't working by Thu 17 Dec, it's dropped and the app is still complete.
> "The reminder depends on Mark as done, Delete and Edit, since each has to cancel or move it. It's also optional, the most expensive, and the riskiest. So it goes last, and if time runs out it's the first thing I drop, without breaking anything."

**If he asks "what battery limits?"**
- **Doze:** when the phone is still with the screen off, background jobs wait for short maintenance windows.
- **App Standby buckets:** apps you rarely open get fewer chances to run in the background.
- **Phone-maker battery savers:** Samsung's "Put unused apps to sleep" (my Galaxy A13) can delay the reminder or block it.
- **WorkManager isn't an alarm clock:** it guarantees the job runs *eventually*, even after a reboot, but not at an exact minute. Exact alarms need another special permission on Android 12+, which is too much for a packing reminder.
- **Notifications:** Android 13+ needs `POST_NOTIFICATIONS`. I ask only when the user turns the reminder on, I handle "No" ("Notifications are off for Sentiero. [Open settings]"), and I create a notification channel (required since Android 8).
> "WorkManager makes sure the reminder runs even if the app is closed or the phone restarts, but Android delays background work to save battery, so the app says 'around 18:00', not an exact time. A packing reminder a few minutes late is fine, and it's not a safety feature."

---

## 3. The layers (slide 5)

```
Screens (Compose) → ViewModel (StateFlow) → Repository → DAO (Room) → SQLite file sentiero.db
        ↑ state comes down                       events go up (callbacks) ↓
```

| Layer | My files | Its job |
|---|---|---|
| Screens | `UpcomingScreen`, `HikeFormScreen`, `HikeDetailScreen` | Draw the state, send events. No logic, no database. |
| ViewModel | `UpcomingViewModel`, `HikeFormViewModel`, `HikeDetailViewModel` | Hold the screen state as `StateFlow`, survive rotation. |
| Repository | `TripRepository` (interface), `RoomTripRepository` | The only class that talks to the database. |
| DAO | `TripDao`, `ChecklistDao` | The SQL queries. Reads return `Flow`, writes are `suspend`. |
| Database | `SentieroDatabase` (`@Database`, version 1) | Lists the 2 tables, gives the DAOs. |
| Entities | `TripEntity` (`trips`), `ChecklistItemEntity` (`checklist_items`) | One class = one table, one object = one row. |
| Wiring | `AppContainer` | Builds the database and repository once for the whole app. |
| Domain rule | `domain/Templates.kt` → `templateFor(difficulty)` | Difficulty in, starting list out. |
| Navigation | `ui/nav/Routes.kt`, `SentieroNavHost` | Typed routes: `Upcoming`, `HikeForm`, `HikeDetail(id)`. |

---

## 4. One tick, end to end (say it while pointing at slide 5)

Tap → `Modifier.toggleable` gives the new value → callback `onTick(item.id, true)` → `HikeDetailViewModel.onTick` → `viewModelScope.launch { repo.setPacked(...) }` → `ChecklistDao.setPacked`: `UPDATE checklist_items SET is_packed = 1 WHERE id = ?` → Room sees the table changed and the `Flow` emits a new list → `combine` builds the new `UiState` → `StateFlow` → `collectAsStateWithLifecycle()` → the screen recomposes: checkbox ticked, "3 of 12", the bar moves.

---

## 5. Quick answers

**Local database vs API?**
Local (Room) = data the user creates. On the phone, offline, instant. API (Open-Meteo, for the final) = data I don't own, like the forecast. Needs internet and can fail.

**Value of storing locally?**
Works offline (no signal at trailheads), instant, survives the app being killed.

**"Local storage is just temporary"**
*Memory* is temporary: variables, the ViewModel. Room writes a *file* on the phone, so it survives force-stop (slide 4). Its real limit: uninstalling the app or clearing its data removes it, and it doesn't sync between phones. That's fine for a personal list; syncing would need a server, which is out of scope.

**ViewModel vs Room?**
The ViewModel survives rotation, but not the app being killed. Room survives both.

**How does it work with no internet?**
Nothing in the midterm uses the network. There's no INTERNET permission in the manifest at all.

**What displays the list?**
`LazyColumn` + `items(list, key = { it.id })`. It only builds the visible rows, and the key tells Compose which row changed.

**How is the date validated?**
It's picked from the Material 3 `DatePickerDialog`, never typed, so it's always a real date. Save stays disabled until it's set (`canSave`). It's converted with **UTC** because the picker returns midnight UTC. Past dates are **allowed on purpose**, to log a hike you already did; it shows "Date passed".

**Is the checklist hardcoded?**
The *starting template* is in code (`templateFor`). On Save, it's **copied into the database** for that hike, so each hike owns its list.

**Can the user add items?**
Yes, since the midterm: "+ Add item", delete with **Undo**, and a message when the list is empty. Say "since the midterm."

**Why ask for notifications?**
The app doesn't ask now. The optional final reminder ("evening before, ~18:00") needs `POST_NOTIFICATIONS` on Android 13+, so it asks **only when the user turns the reminder on**. If they say no, the app works the same.

**Callbacks?**
Lambdas passed down, like `onTick`, `onSaved`, `onBack` and `onValueChange`. Events go up, state comes down. Composables stay simple, and the NavHost decides where `onSaved` goes. For the database I use `Flow` and `suspend` (coroutines) instead of callbacks.

**Why does Save stay grey?**
`canSave = name not blank && date != null && difficulty != null && !isSaving`. No half-made hikes, and a double tap can't save twice.

**Why save a hike and its items in one transaction?**
`db.withTransaction { insertTrip; insertAll }`: both are saved, or neither is. There's never a hike without its list.

**Why UUID ids instead of auto-increment?**
I need the trip's id *before* inserting, so the items can point to it inside the same transaction.

**Why `setPacked(id, checked)` and not "flip it"?**
I save the new value, so on a fast double tap, the last tap wins.

**Why does Back from the detail go home, not to the form?**
After Save: `navigate(HikeDetail(id)) { popUpTo<HikeForm> { inclusive = true } }` removes the form from the back stack.

**What happens on rotation?**
The ViewModels survive, so typed text stays. "Date dialog open" uses `rememberSaveable`.

**`WhileSubscribed(5_000)`?**
The query stops 5 s after the screen is gone, but keeps running through a rotation, which takes less than 5 s.

**One SQL query for the "3 of 12" on the home screen?**
`LEFT JOIN` trips with items, `SUM(is_packed)` and `COUNT(id)`, `GROUP BY` trip, `ORDER BY date`. It's one query, not one per row.

**Deleting a hike deletes its items?**
The database is ready for it: a foreign key with `onDelete = CASCADE`. There's no "delete hike" button in the app yet, so don't say there is.

**Why not `fallbackToDestructiveMigration`?**
It wipes the data when the schema changes. I export the schema instead (`exportSchema = true`).

**Tests?**
JVM: `TemplatesTest`, `HikeDetailViewModelTest`. On device: `TripDaoTest`, `RoomTripRepositoryTest` (add item, delete + Undo keeps id, position and tick).

---

## 6. Numbers to remember

- **10 / 12 / 14** starting items (Easy / Moderate / Hard). Moderate adds Lunch and Extra layer; Hard also adds Headlamp and Trekking poles.
- **3 screens:** Upcoming, New hike, Hike detail.
- **2 tables:** `trips`, `checklist_items`. Database version **1**, file `sentiero.db`.
- Checked on a **Samsung Galaxy A13, Android 14**: "Jabal Moussa Reserve", Sat 14 Nov, Moderate → tick 3 → force-stop → still **3 / 12**.
