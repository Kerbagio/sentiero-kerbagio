# Midterm defense guide: how Sentiero Kerbagio works

Read this before the demo. It explains every part of the app, which Android tool does each job, and why. The "Likely questions" section at the end has short answers you can say out loud.

Checked on a real phone (Samsung Galaxy A13, Android 14) on 9 Nov 2026: create "Jabal Moussa Reserve", Sat 14 Nov, Moderate → 12 items → tick 3 → force-stop → reopen → same 3 ticked. ✅

---

## 1. The app in one breath

> "Topic 8, Hiking Trip Planner. The midterm feature is: create a trip, complete packing items, and show the saved trip detail screen. I built it with Jetpack Compose for the UI, Navigation Compose for the screens, ViewModels with StateFlow for the state, and Room for local storage. The ticks are saved in a SQLite database, so they survive closing the app."

Out of scope (from the brief): live navigation, location tracking, emergency guidance, safety guarantees.

---

## 2. "For this, I used that": every tool in the app

### Screens and widgets (Jetpack Compose, Material 3)

| What you see | What I used | Where |
|---|---|---|
| Each screen's frame (top bar, FAB slot, padding) | `Scaffold` | all 3 screens |
| Top bar with "Sentiero Kerbagio" above "Upcoming" | `TopAppBar` with a `Column` of two `Text`s as its title | `UpcomingScreen.kt` |
| Back arrow | `IconButton` + `Icon(Icons.AutoMirrored.Filled.ArrowBack)` | form + detail |
| The "+ New hike" button bottom right | `ExtendedFloatingActionButton` | `UpcomingScreen.kt` |
| List of hikes / list of packing items | `LazyColumn` + `items(list, key = { it.id })` | Upcoming + detail |
| Line between hike rows | `HorizontalDivider` | `UpcomingScreen.kt` |
| Progress bar ("3 of 12") | `LinearProgressIndicator(progress = { packed / total })` | Upcoming + detail |
| Tapping a hike row | `Modifier.clickable` | `UpcomingScreen.kt` |
| **Route name** text box | `OutlinedTextField` | `HikeFormScreen.kt` |
| **Date** (the calendar) | Material 3 `DatePickerDialog` + `DatePicker` + `rememberDatePickerState()` | `HikeFormScreen.kt` |
| **Difficulty** Easy / Moderate / Hard | `SingleChoiceSegmentedButtonRow` + `SegmentedButton` | `HikeFormScreen.kt` |
| "Day-hike basics (12)" | `FilterChip` (always selected; it shows the template size) | `HikeFormScreen.kt` |
| **Save hike** button (grey until valid) | `Button(enabled = state.canSave)` | `HikeFormScreen.kt` |
| **Checkbox** rows | `Row` + `Modifier.toggleable(role = Role.Checkbox)` + `Checkbox(onCheckedChange = null)` | `HikeDetailScreen.kt` |
| Form scrolls on small screens | `Modifier.verticalScroll(rememberScrollState())` | `HikeFormScreen.kt` |
| Colors | Material 3 `MaterialTheme` (Android Studio's default theme for now) | `ui/theme/Theme.kt` |
| Drawing behind the status bar | `enableEdgeToEdge()` + Scaffold's `padding` | `MainActivity.kt` |

### State (ViewModel + Kotlin Flow)

| Job | What I used |
|---|---|
| Keep screen data alive during rotation | `ViewModel` (one per screen) |
| Hold the form's typed values | `MutableStateFlow<HikeFormUiState>` (private) exposed as `StateFlow` (public, read-only), changed with `_uiState.update { it.copy(...) }` |
| Turn database data into screen state | `Flow.map { ... }` and `combine(flowA, flowB) { ... }` |
| Make a Flow into something the screen can read now | `.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Loading)` |
| Say "the screen is in exactly one state" | `sealed interface ...UiState { Loading; Empty/NotFound; Content(...) }` |
| Read state in a composable | `collectAsStateWithLifecycle()` + `by` |
| Run database writes without freezing the UI | `viewModelScope.launch { repo.something() }` (coroutines, `suspend` functions) |
| Remember "is the date dialog open" across rotation | `rememberSaveable { mutableStateOf(false) }` |
| Create ViewModels that need the repository | `viewModelFactory { initializer { ... } }` reading the app from `APPLICATION_KEY` |

### Navigation (Navigation Compose 2.10)

| Job | What I used |
|---|---|
| The map of screens | `NavHost(navController, startDestination = Upcoming)` |
| One entry per screen | `composable<Upcoming>`, `composable<HikeForm>`, `composable<HikeDetail>` |
| Routes | **Type-safe routes**: `@Serializable object Upcoming`, `@Serializable data class HikeDetail(val id: String)` |
| Read the hike id on the detail screen | `entry.toRoute<HikeDetail>().id` |
| Go to a screen | `navController.navigate(HikeDetail(id))` |
| Go back | `navController.popBackStack()` |
| After Save, remove the form so Back goes to Upcoming | `navigate(HikeDetail(id)) { popUpTo<HikeForm> { inclusive = true } }` |

### Storage (Room = SQLite)

| Job | What I used |
|---|---|
| A table | `@Entity(tableName = "trips")` on `TripEntity`, `@Entity(tableName = "checklist_items")` on `ChecklistItemEntity` |
| Primary key | `@PrimaryKey val id: String = UUID.randomUUID().toString()` |
| Column names in snake_case | `@ColumnInfo(name = "route_name")` |
| Items belong to a trip | `@ForeignKey(... onDelete = CASCADE)` + `@Index("trip_id")` |
| Save `LocalDate` / `Instant` | `@TypeConverter`s in `Converters.kt` (date → day number, instant → milliseconds) |
| Save the `Difficulty` enum | Room stores enums as TEXT by name automatically ("MODERATE") |
| Read data that updates itself | DAO functions returning `Flow<...>` |
| Write data | DAO `suspend` functions: `@Insert`, `@Query("UPDATE ...")` |
| Save a trip **and** its 12 items together | `db.withTransaction { insertTrip(); insertAll(items) }` |
| Count packed items for every row in one query | `LEFT JOIN` + `SUM(is_packed)` + `COUNT(i.id)` + `GROUP BY`, read into `HikeSummaryRow` with `@Embedded` |
| The database itself | `@Database(entities = [...], version = 1, exportSchema = true)` + `@TypeConverters` |
| Generate Room's code | **KSP** (Kotlin Symbol Processing) runs `room-compiler` at build time |
| Show the schema | `exportSchema = true` writes `app/schemas/.../1.json` |

### Wiring (manual dependency injection)

| Job | What I used |
|---|---|
| Build the database once | `AppContainer` (creates `Room.databaseBuilder(...)` and `TripRepository`) |
| Keep it alive for the whole app | `SentieroApp : Application()` owns the container; registered in the manifest with `android:name=".SentieroApp"` |

---

## 3. The layers (the architecture)

```
UI layer        Screens (Composables)  ──events──▶  ViewModels
                     ▲                                  │
                     └────── StateFlow<UiState> ◀───────┘
                                                        │ calls
Data layer                                     TripRepository
                                                        │ calls
                                         TripDao  /  ChecklistDao
                                                        │ SQL
                                       Room → SQLite file "sentiero.db"
Domain          Models.kt (Hike, PackingItem, Difficulty), Templates.kt  (plain Kotlin, used by all)
Wiring          SentieroApp → AppContainer → (database, repository) → ViewModel factories
```

**Rule:** each layer only talks to the one below it. Screens never touch the repository; ViewModels never touch DAOs. This is the same layering as the course's `TaskEntity → TaskDao → TaskRepository → ViewModel → Screen`.

**Unidirectional data flow:** state goes **down** (ViewModel → screen), events go **up** (screen → ViewModel). The screen never changes data itself; it asks the ViewModel.

---

## 4. File by file (what each one does, in my words)

Path: `app/src/main/java/com/anthony/sentiero/`

### Start-up
- **`SentieroApp.kt`**: The `Application` class. Android creates it first, before any screen, and it lives as long as the app process. It creates the `AppContainer` once.
- **`AppContainer.kt`**: Builds the Room database (`sentiero.db`) and the `TripRepository` once. This is "manual dependency injection": instead of each screen creating its own database, they all get the same one from here.
- **`MainActivity.kt`**: The only Activity. It calls `enableEdgeToEdge()`, then `setContent { SentieroTheme { SentieroNavHost() } }`. Everything else is a composable.

### Domain (plain Kotlin, no Android)
- **`domain/Models.kt`**: The types the UI uses: `Hike`, `HikeSummary` (hike + packed/total counts), `PackingItem`, and the `Difficulty` enum with a `label` ("Moderate").
- **`domain/Templates.kt`**: `templateFor(difficulty)`: 10 base items, plus Lunch and Extra layer for Moderate (12), plus Headlamp and Trekking poles for Hard (14). It's a pure function: difficulty in, list out.

### Data (Room)
- **`data/local/TripEntity.kt`**: The `trips` table. It already has every column the full app needs (area, latitude, notes, completed_at, reminder_on) so I never need a database migration later.
- **`data/local/ChecklistItemEntity.kt`**: The `checklist_items` table: `trip_id` (foreign key with CASCADE), `label`, `is_packed`, `position` (keeps the order), plus 3 columns for later features.
- **`data/local/Converters.kt`**: SQLite only stores numbers and text, so `LocalDate` becomes `toEpochDay()` (a day count) and `Instant` becomes milliseconds.
- **`data/local/TripDao.kt`**: SQL for trips: `observeUpcoming()` (the JOIN query that also counts packed items), `observeTrip(id)`, `insertTrip()`.
- **`data/local/ChecklistDao.kt`**: SQL for items: `observeItems(tripId)` ordered by position, `insertAll()`, `setPacked(itemId, packed)`.
- **`data/local/SentieroDatabase.kt`**: The `@Database` class, version 1, lists both entities and the converters.
- **`data/TripRepository.kt`**: The only door to the data. It converts entities into domain models (entities never leave this file) and creates a hike in one transaction.

### UI
- **`ui/nav/Routes.kt`**: The 3 destinations as `@Serializable` types.
- **`ui/nav/SentieroNavHost.kt`**: The `NavHost`. It creates each screen's ViewModel and decides what each callback does (`onNewHike`, `onOpenHike`, `onSaved`, `onBack`).
- **`ui/upcoming/UpcomingViewModel.kt`** + **`UpcomingScreen.kt`**: The list of hikes. States: `Loading` (draw nothing), `Empty` ("No hikes planned…" + button), `Content` (rows).
- **`ui/form/HikeFormViewModel.kt`** + **`HikeFormScreen.kt`**: The New hike form. The ViewModel holds name/date/difficulty; `canSave` is true only when all three are filled.
- **`ui/detail/HikeDetailViewModel.kt`** + **`HikeDetailScreen.kt`**: The saved hike with its checklist. States: `Loading`, `NotFound`, `Content`.
- **`ui/DateLabels.kt`**: Formats a date as "Sat 14 Nov".
- **`ui/theme/Theme.kt`**: Material 3 theme (default colors for the midterm).

---

## 5. The flows, step by step

### A. Creating a hike
1. Upcoming → tap **New hike** → `onNewHike()` → `navController.navigate(HikeForm())`.
2. Typing the name: `OutlinedTextField(value = state.routeName, onValueChange = viewModel::onRouteNameChange)`. Each letter goes **up** to the ViewModel, which does `_uiState.update { it.copy(routeName = value) }`, and the new state comes **down** and the field redraws. (State hoisting: the text field holds no state of its own.)
3. Tapping **Date** sets `showDatePicker = true`, which shows the `DatePickerDialog`. On **OK**, `pickerState.selectedDateMillis` (milliseconds) is converted with `Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC).toLocalDate()`.
   - **Why UTC?** The Material date picker gives midnight **UTC** of the chosen day. If I converted with Lebanon's time zone it would still be the same day here, but in a zone behind UTC (like the US) it becomes the day before. Using UTC always gives back the exact day the user tapped.
4. Tapping **Moderate**: `viewModel.onDifficultyPicked(MODERATE)`. The chip updates to "Day-hike basics (12)" because it calls `templateFor(difficulty).size`.
5. `canSave` becomes true, so the Save button is enabled.
6. **Save hike**: `viewModel.save(onSaved)` → `viewModelScope.launch { repo.createHike(...) }`.
7. `createHike`: builds a `TripEntity` with a new UUID, builds 12 `ChecklistItemEntity`s with `position = 0..11` using that same trip id, then `db.withTransaction { insertTrip; insertAll }`. **Both are saved or neither is.**
8. It returns the id → `onSaved(id)` → `navigate(HikeDetail(id)) { popUpTo<HikeForm> { inclusive = true } }`. The form is removed from the back stack, so Back from the detail goes to Upcoming, not back to the form.

### B. Ticking an item (the one to explain at the demo)
```
Tap row
 → Modifier.toggleable gives the NEW value (true)
 → HikeDetailViewModel.onTick(itemId, true)
 → viewModelScope.launch { repo.setPacked(itemId, true) }
 → ChecklistDao.setPacked: UPDATE checklist_items SET is_packed = 1 WHERE id = ?
 → Room notices the table changed and re-runs observeItems(tripId), so its Flow emits a new list
 → combine(observeHike, observeItems) builds a new HikeDetailUiState.Content (packed = 3)
 → stateIn → StateFlow
 → collectAsStateWithLifecycle() in HikeDetailScreen
 → recomposition: checkbox ticked, "Packing · 3 of 12", progress bar moves
```
**Key point:** the screen does *not* tick the box itself. It only shows what is in the database. If the write failed, the box would not tick. The database is the single source of truth.

**Also on Upcoming:** `observeUpcoming()` reads `checklist_items` too, so Room re-emits it as well and the row says "3 of 12 packed" without any extra code.

### C. Why the ticks survive force-stop
- `adb shell am force-stop` kills the process. Everything in memory is gone: ViewModels, StateFlows, composables.
- The ticks were written to the SQLite file `/data/data/com.anthony.sentiero/databases/sentiero.db` on the phone's storage.
- On reopen: `SentieroApp` → new `AppContainer` → opens the **same file** → the DAO Flows read the saved rows → same 3 ticks.
- **ViewModel vs Room:** a ViewModel survives **rotation** (configuration change) but **not** the app being killed. Room survives both. That's why the data lives in Room, and the ViewModel only holds the *screen state* built from it.

### D. Rotation
- Rotation destroys and recreates the Activity. The **ViewModels survive** (they are scoped to their navigation entry), so the typed name, date and difficulty stay.
- The "date dialog is open" flag uses `rememberSaveable`, which survives rotation too.

### E. Opening a hike from Upcoming
- Row tap → `onOpenHike(row.id)` → `navigate(HikeDetail(id))`.
- In the NavHost: `entry.toRoute<HikeDetail>().id` → `HikeDetailViewModel.factory(id)` → the ViewModel observes that hike and its items.
- If the id doesn't exist, `observeHike` gives `null` → `NotFound` → "This hike no longer exists." + Back.

---

## 6. What's different from the course examples (and why)

These are good to bring up yourself; they show you understand the choices.

| Course example | My app | Why |
|---|---|---|
| String routes: `composable("tasks")`, `navigate("events")` | **Type-safe routes**: `composable<HikeDetail>`, `navigate(HikeDetail(id))` | The compiler checks the route and its argument. No typo can break navigation, and the id is a real `String` field, not text glued into a URL. |
| `toggleTaskCompletion(task)` saves `!task.isCompleted` | `setPacked(itemId, checked)` saves the checkbox's **new value** | If you tap fast twice, "flip what I had" can read an old value and end up wrong. Saving the absolute value means the last tap always wins. |
| `AppDatabase.getDatabase()` singleton with `@Volatile` + `synchronized` | `AppContainer` created once in `SentieroApp.onCreate()` | Same goal (one database), simpler: the Application is already created exactly once. |
| `object : ViewModelProvider.Factory { override fun create(...) }` with an unchecked cast | `viewModelFactory { initializer { ... } }` | The newer lifecycle helper; same idea, no unchecked cast. |
| Ids `Int` with auto-increment | Ids `String` UUID | I need the trip's id **before** inserting so the 12 items can point to it inside the same transaction. |
| `exportSchema = false` | `exportSchema = true` | The schema JSON is committed, so later changes can be compared and migrated safely. |
| `TasksUiState` sealed interface with `Loading` / `Content` | Same pattern, plus `Empty` and `NotFound` | Every possible screen state is a separate case, and `when` must handle all of them. |
| `stateIn(WhileSubscribed(5000))` | Same | The query stops 5 s after the screen is gone, but survives a rotation (which takes less than 5 s). |

---

## 7. Likely questions and short answers

**Why Jetpack Compose and not XML?**
Compose is the course's UI toolkit and Android's current standard. The UI is a function of state: when the state changes, Compose redraws only what changed (recomposition).

**What is recomposition?**
When a state a composable reads changes, Compose calls that composable again to update the screen. Here, Room emits → StateFlow changes → `collectAsStateWithLifecycle` triggers recomposition.

**Why a ViewModel?**
It survives rotation and keeps the logic out of the composables. The screen only draws state and sends events.

**Why StateFlow and not `mutableStateOf` in the ViewModel?**
Room gives Flows; StateFlow is a Flow that always has a current value, so it fits directly (`map`, `combine`, `stateIn`). In the form I use `MutableStateFlow` like the course's `TasksViewModel`.

**What does `collectAsStateWithLifecycle` do?**
It collects the StateFlow only while the screen is visible (at least STARTED) and turns it into Compose `State`, so the screen recomposes on each new value.

**What does `SharingStarted.WhileSubscribed(5_000)` mean?**
The upstream (the Room query) runs only while someone collects. When the screen leaves, it waits 5 seconds before stopping, so a rotation doesn't restart the query.

**Why a sealed interface for the UI state?**
The screen is always in exactly one state (Loading, Empty, Content…). `when` on a sealed type must cover every case, so I can't forget one.

**Why does `Loading` show nothing?**
Room answers in a few milliseconds. A spinner would just flash, so the screen stays blank for that instant.

**Why Room and not DataStore / SharedPreferences?**
I have structured, related data: trips with many items, counts, ordering. That's a relational database. DataStore is for small settings (the course used it for dark mode).

**What is a DAO?**
Data Access Object: an interface where each function is a SQL query. Room (with KSP) generates the code that runs it.

**What is KSP?**
Kotlin Symbol Processing. At build time it reads Room's annotations (`@Entity`, `@Dao`, `@Query`) and generates the Java/Kotlin code. It also checks my SQL at compile time.

**Why are DAO reads `Flow` but writes `suspend`?**
A read with Flow keeps watching: Room re-runs it when the table changes. A write happens once, and `suspend` lets it run off the main thread without blocking the UI.

**Is the database work on the main thread?**
No. Room runs `suspend` DAO functions and Flow queries on its own background threads. The ViewModel just launches the coroutine.

**What does the transaction in `createHike` protect?**
If the app crashed between inserting the trip and inserting its items, you'd get a hike with no packing list. `withTransaction` makes it all-or-nothing.

**What is the foreign key with CASCADE for?**
Each item must belong to a real trip (`trip_id` → `trips.id`). Deleting a trip will delete its items automatically (Delete hike comes in week 5).

**Why `@Index("trip_id")`?**
Every items query filters by `trip_id`. The index makes that lookup fast, and Room warns without it on a foreign key.

**How does Upcoming know "3 of 12" without a query per hike?**
One SQL query: `LEFT JOIN checklist_items`, `SUM(is_packed)` for packed (true = 1), `COUNT(i.id)` for the total, `GROUP BY t.id`. `@Embedded` puts the trip columns into `TripEntity` and the two counts into `HikeSummaryRow`.

**Why entities AND domain models?**
Entities are the database's shape (columns, annotations). Domain models are what the UI needs. The repository converts between them, so the UI doesn't depend on the database design.

**Why are there columns you don't use yet (notes, completed_at, reminder_on…)?**
They're for the final version (journal, completed tab, reminder). Adding them now means the schema stays at version 1 and I never write a migration.

**What happens if you change the schema later?**
I'd have to bump the version and write a migration. I will never use `fallbackToDestructiveMigration()`, because it deletes users' data.

**How does the date picker work?**
Material 3 `DatePickerDialog` with a `DatePicker` and `rememberDatePickerState`. It returns milliseconds at midnight UTC; I convert with `ZoneOffset.UTC` into a `LocalDate`, which Room stores as a day number.

**Why `LocalDate` and not a `Long` or a `String`?**
A hike is on a **day**, not at a time. `LocalDate` has no time or time zone, so it can't drift. The converter stores it as `toEpochDay()`.

**How does Save stay disabled?**
`canSave` is a computed property: `routeName.isNotBlank() && date != null && difficulty != null`. The button uses `enabled = state.canSave`.

**How is the ViewModel given the repository?**
A factory: `viewModelFactory { initializer { (this[APPLICATION_KEY] as SentieroApp).container.tripRepository } }`. The NavHost calls `viewModel(factory = …)`.

**Is each ViewModel shared between screens?**
No. Inside `composable<…>`, `viewModel()` is scoped to that navigation entry. Leaving the screen with Back clears it.

**Why does Back from the detail skip the form?**
After saving I navigate with `popUpTo<HikeForm> { inclusive = true }`, which removes the form from the back stack.

**Which permissions does the app use?**
None. No INTERNET yet. It's added in week 2 for the weather API, because the brief says "permissions only when justified".

**What's the API for the final?**
Open-Meteo: free, no key, forecast + geocoding for the hike's area, with loading and error states. There's no free trail-data API, so weather is the one data source.

**What's not done yet?**
Be honest: the custom DESIGN.md theme, the automated tests, strings.xml, the 80-character limit and double-tap guard. They were planned for week 2 so the midterm could focus on the core flow. Then: add/delete items, weather, Completed tab, Mark as done with a journal.

---

## 8. Demo script (about 5 minutes)

1. **Scope** (20 s): the one-breath summary from section 1.
2. **Create**: New hike → "Jabal Moussa Reserve" → Date: Sat 14 Nov → Moderate (show the chip says 12) → Save. Lands on detail with 12 items.
3. **Tick 3** items. If you have Android Studio open: **App Inspection → Database Inspector → checklist_items**, with "Keep database connections open" / live updates on, and show `is_packed` change from 0 to 1.
4. **Back**: Upcoming shows "3 of 12 packed".
5. **Force-stop**: `adb shell am force-stop com.anthony.sentiero` (or Android Studio's Stop ■), reopen, open the hike: same 3 ticks.
6. **Explain one tick** with the chain in section 5B.

If something breaks: reinstall the APK from the `midterm-demo` tag.
