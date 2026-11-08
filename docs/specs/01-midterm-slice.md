# Spec 1: Midterm slice — create a hike, tick its packing list, survive a restart

Status: FINAL (approved 2026-11-01) · Due: on or before Sun 2026-11-08 · Tier: P0 (CEO-1)
Sources: `docs/plan.md` (all approved decisions), `docs/designs/hiking-trip-planner.md` (rules 1-19), `DESIGN.md`.

## Context

The midterm grades a working demo, a defense of the problem and scope, and an explanation of the state and data flow ("why does this value live here?"). The brief's midterm feature is: create a trip, complete packing items, and display the saved trip detail screen. This spec builds exactly that, on the final architecture, so nothing built now gets thrown away later.

## Current State

Verified 2026-11-01: the repository has no commits and no code. It contains only `docs/`, `DESIGN.md` and `TODOS.md`. Patterns to follow come from the course: week 6 `TaskEntity` / `TaskDao` / `TaskRepository` / `TaskViewModel` (Room, `Flow` reads, suspend writes, `stateIn(WhileSubscribed(5_000))`) and week 8 sealed `UiState`.

## Proposed Change

```
 [Upcoming] ──FAB "New hike"──▶ [New hike form] ──Save──▶ [Hike detail]   (form popped; Back → Upcoming)
     ▲  └──────────────row tap───────────────────────────────▲
 UpcomingVM ◀─Flow─ TripRepository ◀─Flow─ TripDao / ChecklistDao ◀── Room (sentiero.db, schema v1)
 HikeFormVM ──createHike()──▶ TripRepository ──@Transaction insert trip + template items
 HikeDetailVM ──setPacked(id, value)──▶ TripRepository ──▶ UPDATE checklist_items
```

### Implementation Details

**Project setup** (Android Studio, "Empty Activity" Compose template)
- **App name:** full name **Sentiero Kerbagio** ("sentiero" is Italian for trail). Under the launcher icon: `app_name` = "Sentiero" (the full name would be cut off). Inside the app, the Upcoming top bar shows a small overline "Sentiero Kerbagio" (`label` style, `on-surface-variant`) above the "Upcoming" title.
- Package `com.anthony.sentiero` (rename if you prefer; keep it consistent). minSdk **26**, target/compile SDK = the template's latest stable (ENG-R3).
- Plugins: Android application, Kotlin Android, Kotlin Compose compiler (from the template), **Kotlin serialization** (for typed routes), **KSP** (for the Room compiler).
- Dependencies (version catalog, latest stable): `androidx.room:room-runtime`, `room-ktx`, `room-compiler` (ksp); `androidx.navigation:navigation-compose` **≥ 2.8.0**; `org.jetbrains.kotlinx:kotlinx-serialization-json`; `androidx.lifecycle:lifecycle-viewmodel-compose`, `lifecycle-runtime-compose`. Tests: `junit:junit`, `org.jetbrains.kotlinx:kotlinx-coroutines-test`, `androidx.test.ext:junit`, `androidx.test:runner`, `androidx.room:room-testing`.
- Room schema export: `ksp { arg("room.schemaLocation", "$projectDir/schemas") }`; commit `app/schemas/`.
- No Retrofit, WorkManager or INTERNET permission yet (P1+).

**Schema v1** (ENG-R1, ENG-R2: every column exists now; unused ones keep defaults until later tiers)
```kotlin
enum class Difficulty { EASY, MODERATE, HARD }   // Room stores enums as TEXT by name

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "route_name") val routeName: String,
    val date: LocalDate,                                   // epochDay via Converters
    val difficulty: Difficulty,
    @ColumnInfo(name = "area_label") val areaLabel: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @ColumnInfo(defaultValue = "") val notes: String = "",
    @ColumnInfo(name = "completed_at") val completedAt: Instant? = null,   // epoch millis
    @ColumnInfo(name = "reminder_on", defaultValue = "0") val reminderOn: Boolean = false,
)

@Entity(
    tableName = "checklist_items",
    foreignKeys = [ForeignKey(entity = TripEntity::class, parentColumns = ["id"],
        childColumns = ["trip_id"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("trip_id")],
)
data class ChecklistItemEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "trip_id") val tripId: String,
    val label: String,
    @ColumnInfo(name = "is_packed", defaultValue = "0") val isPacked: Boolean = false,
    val position: Int,
    @ColumnInfo(defaultValue = "0") val forgotten: Boolean = false,
    @ColumnInfo(name = "carried_over", defaultValue = "0") val carriedOver: Boolean = false,
    @ColumnInfo(name = "added_after_hike", defaultValue = "0") val addedAfterHike: Boolean = false,
)

class Converters {
    @TypeConverter fun fromLocalDate(d: LocalDate?): Long? = d?.toEpochDay()
    @TypeConverter fun toLocalDate(v: Long?): LocalDate? = v?.let(LocalDate::ofEpochDay)
    @TypeConverter fun fromInstant(i: Instant?): Long? = i?.toEpochMilli()
    @TypeConverter fun toInstant(v: Long?): Instant? = v?.let(Instant::ofEpochMilli)
}

@Database(entities = [TripEntity::class, ChecklistItemEntity::class], version = 1, exportSchema = true)
@TypeConverters(Converters::class)
abstract class SentieroDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
    abstract fun checklistDao(): ChecklistDao
}
```
No `fallbackToDestructiveMigration()`, ever (ENG-R2).

**DAOs**
```kotlin
data class HikeSummaryRow(@Embedded val trip: TripEntity, val packedCount: Int, val totalCount: Int)

@Dao interface TripDao {
    @Query("""
        SELECT t.*, COALESCE(SUM(i.is_packed), 0) AS packedCount, COUNT(i.id) AS totalCount
        FROM trips t LEFT JOIN checklist_items i ON i.trip_id = t.id
        WHERE t.completed_at IS NULL
        GROUP BY t.id
        ORDER BY t.date ASC, t.route_name ASC
    """)
    fun observeUpcoming(): Flow<List<HikeSummaryRow>>           // ENG-R14: one query, no N+1

    @Query("SELECT * FROM trips WHERE id = :id")
    fun observeTrip(id: String): Flow<TripEntity?>

    @Insert suspend fun insertTrip(trip: TripEntity)
}

@Dao interface ChecklistDao {
    @Query("SELECT * FROM checklist_items WHERE trip_id = :tripId ORDER BY position ASC, id ASC")
    fun observeItems(tripId: String): Flow<List<ChecklistItemEntity>>

    @Insert suspend fun insertAll(items: List<ChecklistItemEntity>)

    @Query("UPDATE checklist_items SET is_packed = :packed WHERE id = :itemId")
    suspend fun setPacked(itemId: String, packed: Boolean)      // absolute value (CEO T7)
}
```

**Domain + rules** (`domain/`, no Android imports)
```kotlin
data class Hike(val id: String, val routeName: String, val date: LocalDate, val difficulty: Difficulty)
data class HikeSummary(val hike: Hike, val packedCount: Int, val totalCount: Int)
data class PackingItem(val id: String, val label: String, val isPacked: Boolean)

private val BASE = listOf("Water (2 L)", "Snacks", "Trail shoes", "Sun hat", "Sunglasses",
    "First-aid kit", "Phone + power bank", "ID + cash", "Small backpack", "Trash bag")
fun templateFor(d: Difficulty): List<String> = BASE + when (d) {          // CEO-2
    Difficulty.EASY -> emptyList()
    Difficulty.MODERATE -> listOf("Lunch", "Extra layer")
    Difficulty.HARD -> listOf("Lunch", "Extra layer", "Headlamp", "Trekking poles")
}
```

**Repository** (`data/TripRepository.kt`)
```kotlin
class TripRepository(private val db: SentieroDatabase, private val tripDao: TripDao, private val checklistDao: ChecklistDao) {
    fun observeUpcoming(): Flow<List<HikeSummary>>            // maps HikeSummaryRow → HikeSummary
    fun observeHike(id: String): Flow<Hike?>
    fun observeItems(tripId: String): Flow<List<PackingItem>>
    suspend fun createHike(routeName: String, date: LocalDate, difficulty: Difficulty): String {
        val trip = TripEntity(routeName = routeName.trim(), date = date, difficulty = difficulty)
        val items = templateFor(difficulty).mapIndexed { i, label -> ChecklistItemEntity(tripId = trip.id, label = label, position = i) }
        db.withTransaction { tripDao.insertTrip(trip); checklistDao.insertAll(items) }   // all-or-nothing
        return trip.id
    }
    suspend fun setPacked(itemId: String, packed: Boolean) = checklistDao.setPacked(itemId, packed)
}
```
Entities never leave the repository (D1 arrangement: mapping at the boundary).

**AppContainer + Application** (ENG-R5)
```kotlin
class AppContainer(context: Context) {
    val clock: Clock = Clock.systemDefaultZone()
    private val db = Room.databaseBuilder(context, SentieroDatabase::class.java, "sentiero.db").build()
    val tripRepository = TripRepository(db, db.tripDao(), db.checklistDao())
}
class SentieroApp : Application() { lateinit var container: AppContainer; override fun onCreate() { super.onCreate(); container = AppContainer(this) } }
```
Register `android:name=".SentieroApp"` in the manifest. Each ViewModel has a `companion fun factory(...)` built with `viewModelFactory { initializer { … } }`, reading the container from `APPLICATION_KEY`.

**Navigation** (ENG-R4)
```kotlin
@Serializable object Upcoming
@Serializable data class HikeForm(val editId: String? = null)   // editId unused until P3
@Serializable data class HikeDetail(val id: String)
```
`NavHost(startDestination = Upcoming)`. HikeDetail reads `entry.toRoute<HikeDetail>().id` in the NavHost and passes it to `HikeDetailViewModel.factory(id)`. After Save: `navController.navigate(HikeDetail(id)) { popUpTo<HikeForm> { inclusive = true } }` (DR-1C). No bottom bar yet (Completed arrives in P1).

**ViewModels**
- `UpcomingViewModel(repo, clock)`: `uiState: StateFlow<UpcomingUiState>`; `sealed interface UpcomingUiState { data object Loading; data object Empty; data class Content(val rows: List<UpcomingRow>) }`. `UpcomingRow(id, routeName, difficulty, dateLabel, relativeLabel, packed, total)`. `relativeLabel`: date == today → "Today"; today+1 → "Tomorrow"; future → "in N days"; past → "Date passed". `dateLabel` format `EEE d MMM` (e.g. "Sat 14 Nov"). Loading renders nothing (DR Pass 2).
- `HikeFormViewModel(repo)`: holds `routeName`, `date: LocalDate?`, `difficulty: Difficulty?`, `isSaving`; `canSave = routeName.isNotBlank() && date != null && difficulty != null && !isSaving`; route input capped at 80 chars, counter shown from 64 (DR-6A). `save()`: returns immediately if `!canSave` (double-tap guard, CEO T4); sets `isSaving`; calls `createHike`; on success emits a one-shot `savedHikeId`; on `SQLiteException` shows "Couldn't save. Try again." and keeps input (CEO T2). Never `catch (e: Exception)`.
- `HikeDetailViewModel(hikeId, repo)`: `combine(observeHike, observeItems)` → `sealed interface HikeDetailUiState { Loading; NotFound; Content(title, dateLabel, difficulty, packed, total, items) }`. `onTick(itemId, checked)` → `repo.setPacked(itemId, checked)` with the Checkbox's new value (absolute; CEO T7).

**Screens** (DESIGN.md tokens; copy per DR-1A)
- Upcoming: TopAppBar with the overline "Sentiero Kerbagio" (`label`, `on-surface-variant`) above the title "Upcoming" (`headline`); `LazyColumn` of rows (no cards): route `title` (2 lines max then …), difficulty outlined tag + "Sat 14 Nov · in 13 days" `body-small`, 3dp progress line, "6 of 12 packed". Empty: "No hikes planned. Plan your next one." + button "New hike". `ExtendedFloatingActionButton` "New hike".
- New hike: "Route name" outlined field; "Date" field opening `DatePickerDialog` (past dates allowed; convert `selectedDateMillis` with `ZoneOffset.UTC`, never the device zone); "Difficulty" `SingleChoiceSegmentedButtonRow` (Easy / Moderate / Hard, none preselected); "Packing list starts from" one preselected `FilterChip` "Day-hike basics (N)" where N = template size for the chosen difficulty; filled "Save hike" button, disabled until valid, small spinner while saving.
- Hike detail: title (`headline`, wraps fully), "Sat 14 Nov · Moderate", "Packing · 3 of 12" + progress line, checklist rows `Checkbox` + label (packed labels in `on-surface-variant`, no strike-through). Each row is one TalkBack element: "Headlamp, not packed" (DR-6B). No forecast, add, delete, reminder or Mark as done yet. NotFound: "This hike no longer exists." + Back.
- All copy in `res/values/strings.xml`.

**Theme** (answer 1: theme at midterm)
- `ui/theme/Color.kt`: light and dark `ColorScheme`s with every DESIGN.md color role; `Blaze = Color(0xFFE8590C)`, `BlazeDark = Color(0xFFFF8A4C)` for later use.
- `ui/theme/Type.kt`: `FontFamily`s from `res/font/barlow_condensed_medium.ttf`, `barlow_condensed_semibold.ttf`, `source_sans_3_regular.ttf`, `source_sans_3_medium.ttf`, `source_sans_3_semibold.ttf`; `Typography` mapped per the DESIGN.md table. Tabular figures via `fontFeatureSettings = "tnum"` on number styles.
- `ui/theme/Shape.kt`: extraSmall 4dp, small 6dp, medium 8dp, large 12dp, extraLarge 20dp; buttons use `MaterialTheme.shapes.medium`.
- `ui/theme/Theme.kt`: `SentieroTheme(darkTheme = isSystemInDarkTheme())`, `dynamicColor` not used.
- Font files come from the Google Fonts family downloads (static TTFs); put both `OFL.txt` files in `licenses/`.

## Acceptance Criteria

1. Fresh install shows "Upcoming" with "No hikes planned. Plan your next one." and a "New hike" button.
2. "Save hike" stays disabled until the route name is non-blank, a date is picked and a difficulty is chosen; the route field stops at 80 characters.
3. Saving "Jabal Moussa Reserve", Sat 14 Nov 2026, Moderate opens its detail with 12 unpacked items in this order: Water (2 L), Snacks, Trail shoes, Sun hat, Sunglasses, First-aid kit, Phone + power bank, ID + cash, Small backpack, Trash bag, Lunch, Extra layer.
4. Back from that detail returns to Upcoming (not the form); on 2026-11-01 its row reads "Jabal Moussa Reserve", "Moderate", "Sat 14 Nov · in 13 days", "0 of 12 packed".
5. Ticking 3 items shows "Packing · 3 of 12" and the Upcoming row "3 of 12 packed".
6. `adb shell am force-stop com.anthony.sentiero`, then reopening, shows the same 3 ticks on the same items.
7. Double-tapping "Save hike" creates exactly one hike.
8. Rotating the device while filling the form keeps the typed name, picked date and chosen difficulty.
9. A hike dated Sat 31 Oct 2026 saves and appears first in Upcoming with "Date passed".
10. Light theme shows surface #F4F4F0 and route names in Barlow Condensed; switching the system to dark mode switches the app; wallpaper colors never change the palette.
11. `./gradlew test` passes `TemplatesTest` and `HikeDetailViewModelTest`; `./gradlew connectedAndroidTest` passes `TripDaoTest`.
12. `app/schemas/com.anthony.sentiero.data.local.SentieroDatabase/1.json` is committed and lists both tables with every column of the approved v1 schema.
13. The demo commit is tagged `midterm-demo` and its debug APK is kept.
14. Each test in the Testing Plan states its card: `protects=…; fails_when=…; no_claim=…`.
15. No degradation of existing functionality (none exists yet).

## Testing Plan

| Layer | What | Count |
|---|---|---|
| Unit (JVM) | `TemplatesTest`: Easy 10, Moderate 12, Hard 14; base order; extras order | +3 |
| Unit (JVM) | `HikeDetailViewModelTest` (fake repository): tick writes `setPacked(id, true)`; ticks `true` then `false` fast end on `false` | +2 |
| Instrumented | `TripDaoTest` (in-memory Room): createHike inserts trip + items in position order; `observeUpcoming` counts 0 of 12 → 3 of 12 after three `setPacked` | +2 |
| Manual E2E | Restart script (criteria 3-6) and rotation (criterion 8) on the emulator | +2 |

Cards:
- TemplatesTest — protects=difficulty decides the starting list exactly (CEO-2); fails_when=an item, the count or the order changes; no_claim=none beyond protects
- HikeDetailViewModelTest — protects=absolute writes, last tap wins (CEO T7); fails_when=the ViewModel computes `!current`; no_claim=Room write ordering (fake repository)
- TripDaoTest — protects=ticks persist in Room and the aggregate counts follow; fails_when=`setPacked` or the JOIN query breaks; no_claim=surviving a real process restart (in-memory database; the manual restart test covers it)

## Demo script (midterm, ~5 minutes)

1. Scope in one breath: "Topic 8. The brief is the contract. The one API will be weather from Open-Meteo, because there is no free trail API. Out of scope: navigation, tracking, safety guarantees."
2. Create "Jabal Moussa Reserve", Sat 14 Nov, Moderate → lands on detail with 12 items.
3. Tick 3 items with Android Studio's Database Inspector open on `checklist_items`: the `is_packed` cells change live.
4. Force-stop, reopen, open the hike: same 3 ticks.
5. Explain one tick: `Checkbox(onCheckedChange)` → `HikeDetailViewModel.onTick` → `TripRepository.setPacked` → `ChecklistDao.setPacked` UPDATE → Room's `Flow` emits → `combine` → `StateFlow` → `collectAsStateWithLifecycle` → recomposition.

## Implementation rules

1. Acceptance criteria are the `protects / fails_when / no_claim` profile of each test: the observable it protects, the wrong result it rejects, and what green does not prove. Never "make the tests pass".
2. Real code and real tests land in the same change; a placeholder, a stub assertion or a test deferred to a follow-up is not done.
3. Any change to a test, validator, CI step, timeout, tolerance, threshold, snapshot or suppression pragma is reported on its own line in your summary with its justification, before any other result.
4. A reported command names the command and its observed result (exit status, counts, the failing line); a command you did not run is not reported, and stderr is never silenced in a command you cite.
5. Your report is a claim until the parent re-executes it: say what you verified, what you reused, and what you could not check.
6. Course rule: I must be able to explain every file in my own words before the demo. Code I can't explain doesn't count.

## Rollback Plan

Every step is a small commit on `main`. If the demo build breaks, reinstall the APK from the `midterm-demo` tag. Schema v1 is final, so no database rollback is needed; never add a destructive migration.

## Effort Estimate

| Component | Effort |
|---|---|
| Project setup, plugins, dependencies, schema export | ~1.5 h |
| Theme: colors, fonts, type, shapes | ~1.5 h |
| Entities, converters, DAOs, database | ~2 h |
| Templates, domain models, repository, AppContainer | ~1.5 h |
| Three ViewModels | ~2 h |
| Three screens + strings | ~3 h |
| Navigation | ~1 h |
| Tests (3 automated + 2 manual scripts) | ~2 h |
| Demo rehearsal + tag + APK | ~1 h |
| **Total** | **~15.5 h** over 7 days |

## Files Reference

| File | Change |
|---|---|
| `settings.gradle.kts`, `build.gradle.kts`, `gradle/libs.versions.toml` | plugins and dependencies |
| `app/build.gradle.kts` | minSdk 26, serialization + KSP plugins, schema location |
| `app/src/main/AndroidManifest.xml` | `android:name=".SentieroApp"` |
| `app/src/main/java/com/anthony/sentiero/SentieroApp.kt` | Application + container |
| `.../AppContainer.kt` | Clock, database, TripRepository |
| `.../MainActivity.kt` | `SentieroTheme { SentieroNavHost() }` |
| `.../data/local/TripEntity.kt`, `ChecklistItemEntity.kt`, `Converters.kt`, `TripDao.kt`, `ChecklistDao.kt`, `SentieroDatabase.kt` | schema v1 + DAOs |
| `.../data/TripRepository.kt` | create, observe, setPacked |
| `.../domain/Models.kt`, `Templates.kt` | domain types, `templateFor` |
| `.../ui/nav/Routes.kt`, `SentieroNavHost.kt` | typed routes |
| `.../ui/upcoming/UpcomingScreen.kt`, `UpcomingViewModel.kt` | list |
| `.../ui/form/HikeFormScreen.kt`, `HikeFormViewModel.kt` | new hike |
| `.../ui/detail/HikeDetailScreen.kt`, `HikeDetailViewModel.kt` | checklist |
| `.../ui/theme/Color.kt`, `Type.kt`, `Shape.kt`, `Theme.kt` | DESIGN.md theme |
| `app/src/main/res/font/*.ttf`, `res/values/strings.xml`, `licenses/OFL-*.txt` | fonts, copy, licenses |
| `app/src/test/.../TemplatesTest.kt`, `HikeDetailViewModelTest.kt` | JVM tests |
| `app/src/androidTest/.../TripDaoTest.kt` | instrumented DAO test |
| `app/schemas/.../1.json` | exported schema (generated, committed) |

## Out of Scope

Weather (area search, forecast, suggestions), the Completed screen and bottom bar, Mark as done and the journal, add/delete item, Edit/Delete hike, "Start from last hike", the reminder, photos. All are in Spec 2.

## Related

- `docs/plan.md`: CEO-1 (tiers), CEO-2 (templates), DR-1A..7B (design), ENG-R1..R14 (engineering), tasks T2, T4, T7, T8, T9, D1-D3, E1-E4, E9
- `docs/designs/hiking-trip-planner.md`: rules 1, 8, 12; premise 5
- `DESIGN.md`
- Spec 2: rest of the MVP (`docs/specs/02-rest-of-mvp.md`)
