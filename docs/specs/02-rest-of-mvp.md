# Spec 2: Rest of the MVP — weather, journal, the loop, polish (Epic)

Status: FINAL (approved 2026-11-01; P3 order confirmed: reminder dropped first) · Due: the final (date not announced; the plan assumes ~Sun 2026-12-06) · Tiers: P1 cut line, P2 loop, P3 polish (CEO-1)
Builds on: Spec 1 (`docs/specs/01-midterm-slice.md`, FINAL). Spec 1 must be built and tagged `midterm-demo` first.
Sources: `docs/plan.md` (wins where documents differ), `docs/designs/hiking-trip-planner.md` (rules 1-19), `DESIGN.md`, `docs/design-preview/` (visual source of truth, DR-4A). Spec answers of 2026-11-01: a shared `MarkDoneViewModel`; "offline" read from the exception; editing difficulty keeps the list.

## Context

The final is graded on four things: all four MVP bullets of the brief work, every API state can be shown on demand, the loop holds up in a live demo, and every layer can be explained ("why does this value live here?"). Spec 1 delivers create → tick → restart. This epic delivers the rest in the CEO-1 tiers:
- **P1 is the cut line.** It completes the brief and must ship no matter what.
- **P2 is the loop.** It makes the app worth defending: "It remembers what I forgot."
- **P3 is polish.** It is dropped in a fixed order if time runs short.

Who cares:
- **Hikers:** packing learns from the last hike, and old notes come back when they matter.
- **The grader:** sees the brief's bullets, visible API states and the course stack.
- **Me:** a schedule with a safe fallback line.

## Current State

Verified 2026-11-01: the repository has no commits and no code (`git log`: "does not have any commits yet"). This spec assumes Spec 1's files exist as specified there. Schema v1 already has every column used below (ENG-R1, ENG-R2), so this epic needs **no migration**.

Live API checks, 2026-11-01:
- Geocoding no-match bodies have no `results` key:
  - "zzqx" → `{"generationtime_ms":0.17…}`
  - "qxzvq" → `{"generationtime_ms":0.26…}`
  - "Mchati" → no results. The preview's sample area is not findable, so don't use it in the demo.
- Multi-country names, where the user must pick:
  - "Tripoli" → Libya first, then Lebanon, Greece, US, Italy.
  - "Faraya" → Guinea first, then Lebanon (Mount Lebanon, 1,313 m), then Farayatomou (Guinea).
- Single matches:
  - "Jabal Moussa" → one mountain result: Mount Lebanon, 34.06028 / 35.7725, 1,574 m.
  - "Bsharri" → one result: North Lebanon, 1,442 m.
- The forecast endpoint was checked against the docs only, because automated fetches are blocked.

Rule coverage before and after this epic:

| Rule / decision | Spec 1 | This epic | Tier |
|---|---|---|---|
| 1 Upcoming order | ✅ | 2.5 due rows | P0/P1 |
| 2 Completed order | — | 2.5 | P1 |
| 3 "Last hike" | — | 2.9 | P2 |
| 4 Mark as done from the hike date | — | 2.5, 2.6 | P1 |
| 5 Sheet: notes, flags, Undo / add forgotten / Edit journal | — | 2.6 / 2.10 / 2.13 | P1 / P2 / P3 |
| 6 Journal mode / Move back | — | 2.5 / 2.12 | P1 / P3 |
| 7 Delete / Edit | — | 2.7 / 2.14 | P1 / P3 |
| 8 Position order | ✅ | 2.1 | P0 |
| 9 Copy from last hike | — | 2.9 | P2 |
| 10 Append | — | 2.1, 2.8 | P1, P2 |
| 11 Item delete + Undo | — | 2.1 | P1 |
| 12 List-source chips | template only | 2.9 | P2 |
| 13 Completed search | — | 2.15 | P3 |
| 14 Same-route note | — | 2.11 | P2 |
| 15-17 Area search, its states, Save never waits | — | 2.3 | P1 |
| 18 Forecast card | — | 2.4 | P1 |
| 19 Suggestions | — | 2.8 | P2 |
| CEO-3/5 Reminder | — | 2.16 | P3 |

## Decisions made in this spec (not settled in the plan)

1. **P3 order.** Two approved notes disagree.
   - CEO-3 calls the reminder "the last MVP slice", dropped first if late. CEO-1's P3 list drops Completed search first and lists the reminder last.
   - This spec follows CEO-3 for the reminder and CEO-1's order for the rest.
   - **Build:** Move back → Edit journal → Edit hike → Completed search → Reminder.
   - **If late, drop from the end:** Reminder, then search, Edit hike, Edit journal, Move back.
2. **Forecast numbers are rounded once**, half up to whole numbers, when the response is mapped. The card shows the same numbers the rules compare, so "5°" always means the ≤ 5 °C row can fire. Rule 19's thresholds don't change.
3. **Dates are English everywhere** (`Locale.ENGLISH`, because the app is not translated). They show the year when it isn't the current year ("Sat 25 Oct 2025"). This replaces Spec 1's formatter; Spec 1's examples don't change.
4. **"No forecast for this date yet." keeps Retry.** The plan allowed "hidden or harmless", and keeping it means one error layout.
5. **Due rows follow the approved preview.** From the hike date, the Upcoming row shows a "Mark as done" text button where the progress line was. On the hike date it reads "[Mark as done] when you're back" (rule 4).
6. **Mark as done also turns the reminder off** (`reminder_on = 0`, job cancelled). By then it is always too late, and Undo does not turn it back on.
7. **New copy** for snackbars, the delete dialog, the area hint and Edit hike. The full table is under Copy. The delete button is a plain text button, because DESIGN.md keeps red for real errors.
8. **Search and same-route matching run in Kotlin** with `ignoreCase = true` on the observed Completed list. SQL `LIKE` and `lower()` only fold ASCII case, and the list is tens of rows.
9. **Area label.** "<name>, <region>" (for example "Bsharri, North Lebanon"). When the region is missing or equals the name, use "<name>, <country>" (for example "Tripoli, Libya").
10. **The reminder notification opens the app on Upcoming.** There is no deep link into the hike.
11. **One sheet for both modes.** Title "How did it go?", button "Save to journal". Edit journal saves without a snackbar; Move back shows "Moved back to Upcoming."
12. **Test seams.** Reported up front under implementation rule 3.
    - Repositories become interfaces with `Room…` and `OpenMeteo…` implementations. Spec 1's fake-repository ViewModel test already needs this.
    - `unitTests.isReturnDefaultValues = true`, so `android.util.Log` in repositories doesn't break JVM tests.

## Proposed Change

Final architecture (one direction, as in the week 6/8 slides):
```
 Upcoming ─┐ Completed ─┐  HikeForm ──────┐   HikeDetail (upcoming | journal) ─┐   MarkDoneSheet (hosted by Upcoming and HikeDetail)
 UpcomingVM  CompletedVM   HikeFormVM      HikeDetailVM                        MarkDoneVM(hikeId)
     │           │          ├ AreaSearchState  ├ AreaSearchState (Add area)        │
     ▼           ▼          ▼                  ▼                                   ▼
 TripRepository (interface) ── RoomTripRepository ── TripDao / ChecklistDao ── Room (sentiero.db v1)
                                   └── ReminderScheduler ── WorkManager ── ReminderWorker ── NotificationManager   (2.16)
 WeatherRepository (interface) ── OpenMeteoWeatherRepository ── GeocodingApi / ForecastApi (Retrofit, one OkHttpClient)
 Pure Kotlin, JVM-tested: Templates · SuggestionRules · ForecastCardPolicy · ReminderTime · JournalRules · DateLabels · Place
```

### Shared contracts

**Dependencies and manifest**
- **2.2:** `com.squareup.retrofit2:retrofit` and `com.squareup.retrofit2:converter-kotlinx-serialization` (same version, latest stable; the official converter since Retrofit 2.11). Add `<uses-permission android:name="android.permission.INTERNET" />`.
- **2.16:** `androidx.work:work-runtime-ktx` and `<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />`.
- No other new dependencies.

**Repository interfaces** (decision 12). If Spec 1 shipped `class TripRepository`, extract the interface with Android Studio's Refactor ▸ Extract ▸ Interface and rename the class `RoomTripRepository`.
```kotlin
interface TripRepository {
    fun observeUpcoming(): Flow<List<HikeSummary>>                                   // Spec 1
    fun observeCompleted(): Flow<List<CompletedSummary>>                             // 2.5
    fun observeHike(id: String): Flow<Hike?>                                         // Spec 1, every field mapped
    fun observeItems(tripId: String): Flow<List<PackingItem>>                        // Spec 1, every field mapped
    suspend fun journalSource(id: String): JournalSource?                            // 2.6
    suspend fun createHike(routeName: String, date: LocalDate, difficulty: Difficulty,
                           area: Area? = null, source: ListSource = ListSource.Template): CreateResult  // 2.3 area, 2.9 source
    suspend fun updateHike(id: String, routeName: String, date: LocalDate, difficulty: Difficulty, area: Area?) // 2.14
    suspend fun setArea(id: String, area: Area)                                      // 2.4
    suspend fun setPacked(itemId: String, packed: Boolean)                           // Spec 1
    suspend fun addItem(tripId: String, label: String): Boolean                      // 2.1 (false = blank, nothing written)
    suspend fun deleteItem(itemId: String): PackingItem?                             // 2.1 (snapshot for Undo)
    suspend fun restoreItem(tripId: String, item: PackingItem)                       // 2.1 (same id and position)
    suspend fun saveJournal(id: String, draft: JournalDraft, markDone: Boolean)      // 2.6, 2.10, 2.13
    suspend fun reopenHike(id: String)                                               // 2.6 Undo, 2.12 Move back
    suspend fun deleteHike(id: String)                                               // 2.7
    suspend fun setReminder(id: String, on: Boolean): Boolean                        // 2.16 (false = too late, nothing written)
    suspend fun reminderContent(id: String): ReminderContent?                        // 2.16 (null = gone, done or off)
}
interface WeatherRepository {
    suspend fun searchPlaces(query: String): PlaceSearchResult                       // 2.2
    suspend fun dailyForecast(area: Area, date: LocalDate): ForecastResult           // 2.2
}
```
Spec 1's `createHike` returned the id. From 2.9 it returns `CreateResult`, and the form reads `.id`.

**Domain types** (`domain/`, no Android imports; Spec 1 types gain defaulted fields, so Spec 1 code still compiles)
```kotlin
data class Area(val label: String, val latitude: Double, val longitude: Double)
data class Hike(val id: String, val routeName: String, val date: LocalDate, val difficulty: Difficulty,
                val area: Area? = null, val notes: String = "", val completedAt: Instant? = null, val reminderOn: Boolean = false)
data class PackingItem(val id: String, val label: String, val isPacked: Boolean, val position: Int = 0,
                       val forgotten: Boolean = false, val carriedOver: Boolean = false, val addedAfterHike: Boolean = false)
data class CompletedSummary(val hike: Hike, val totalCount: Int, val forgottenCount: Int, val firstForgotten: String?)
sealed interface ListSource { data object Template : ListSource; data class LastHike(val tripId: String) : ListSource }
data class CreateResult(val id: String, val fellBackToTemplate: Boolean)
data class JournalSource(val notes: String, val items: List<PackingItem>)
data class JournalDraft(val notes: String, val forgottenIds: Set<String>, val removedIds: Set<String>, val added: List<NewItem>)
data class NewItem(val id: String, val label: String, val forgotten: Boolean)   // id is made in the sheet when the row is added
data class ReminderContent(val routeName: String, val itemsLeft: Int)
```
Mapping: `area` is non-null only when `area_label`, `latitude` and `longitude` are all non-null. The three columns are always written together.

**DAO additions** (Room binds every parameter; converters apply to parameters too)
```kotlin
data class CompletedSummaryRow(@Embedded val trip: TripEntity, val totalCount: Int, val forgottenCount: Int, val firstForgotten: String?)
data class ReminderContentRow(val routeName: String, val itemsLeft: Int)

// TripDao
@Query("""
    SELECT t.*, COUNT(i.id) AS totalCount, COALESCE(SUM(i.forgotten), 0) AS forgottenCount,
           (SELECT f.label FROM checklist_items f WHERE f.trip_id = t.id AND f.forgotten = 1
            ORDER BY f.position ASC, f.id ASC LIMIT 1) AS firstForgotten
    FROM trips t LEFT JOIN checklist_items i ON i.trip_id = t.id
    WHERE t.completed_at IS NOT NULL
    GROUP BY t.id
    ORDER BY t.date DESC, t.completed_at DESC
""") fun observeCompleted(): Flow<List<CompletedSummaryRow>>                            // rules 2, 3; ENG-R14 (one query)
@Query("SELECT * FROM trips WHERE id = :id") suspend fun getTrip(id: String): TripEntity?
@Query("UPDATE trips SET area_label = :label, latitude = :lat, longitude = :lon WHERE id = :id")
suspend fun setArea(id: String, label: String?, lat: Double?, lon: Double?)
@Query("""UPDATE trips SET route_name = :routeName, date = :date, difficulty = :difficulty,
          area_label = :label, latitude = :lat, longitude = :lon WHERE id = :id""")
suspend fun updateDetails(id: String, routeName: String, date: LocalDate, difficulty: Difficulty, label: String?, lat: Double?, lon: Double?)
@Query("UPDATE trips SET notes = :notes WHERE id = :id") suspend fun setNotes(id: String, notes: String)
@Query("UPDATE trips SET completed_at = :at WHERE id = :id") suspend fun setCompletedAt(id: String, at: Instant?)
@Query("UPDATE trips SET reminder_on = :on WHERE id = :id") suspend fun setReminderOn(id: String, on: Boolean)
@Query("DELETE FROM trips WHERE id = :id") suspend fun deleteTrip(id: String)                // items go by ON DELETE CASCADE
@Query("""SELECT t.route_name AS routeName,
          (SELECT COUNT(*) FROM checklist_items i WHERE i.trip_id = t.id AND i.is_packed = 0) AS itemsLeft
          FROM trips t WHERE t.id = :id AND t.completed_at IS NULL AND t.reminder_on = 1""")
suspend fun reminderContent(id: String): ReminderContentRow?

// ChecklistDao
@Query("SELECT * FROM checklist_items WHERE trip_id = :tripId ORDER BY position ASC, id ASC")
suspend fun getItems(tripId: String): List<ChecklistItemEntity>
@Query("SELECT * FROM checklist_items WHERE id = :itemId") suspend fun getItem(itemId: String): ChecklistItemEntity?
@Query("SELECT COALESCE(MAX(position), -1) + 1 FROM checklist_items WHERE trip_id = :tripId") suspend fun nextPosition(tripId: String): Int
@Insert suspend fun insert(item: ChecklistItemEntity)
@Query("DELETE FROM checklist_items WHERE id = :itemId") suspend fun delete(itemId: String)
@Query("UPDATE checklist_items SET forgotten = 0 WHERE trip_id = :tripId") suspend fun clearForgotten(tripId: String)
@Query("UPDATE checklist_items SET forgotten = 1 WHERE trip_id = :tripId AND id IN (:ids)")
suspend fun markForgotten(tripId: String, ids: List<String>)
@Query("DELETE FROM checklist_items WHERE trip_id = :tripId AND added_after_hike = 1 AND id IN (:ids)")
suspend fun deleteAddedAfterHike(tripId: String, ids: List<String>)                         // ENG-R10: never deletes template items
```

**Network** (`data/remote/`, kotlinx.serialization as in the week 7 workshop)
```kotlin
interface GeocodingApi {   // base URL https://geocoding-api.open-meteo.com/
    @GET("v1/search")
    suspend fun search(@Query("name") name: String, @Query("count") count: Int = 5,
                       @Query("language") language: String = "en", @Query("format") format: String = "json"): GeocodingResponseDto
}
interface ForecastApi {    // base URL https://api.open-meteo.com/
    @GET("v1/forecast")
    suspend fun daily(@Query("latitude") latitude: Double, @Query("longitude") longitude: Double,
                      @Query("daily", encoded = true) daily: String = DAILY_FIELDS,
                      @Query("timezone") timezone: String = "auto",                      // R3-4: the hike's local day
                      @Query("start_date") startDate: String, @Query("end_date") endDate: String): ForecastResponseDto
}
const val DAILY_FIELDS = "temperature_2m_max,temperature_2m_min,precipitation_probability_max,wind_speed_10m_max,uv_index_max"

@Serializable data class GeocodingResponseDto(val results: List<PlaceDto>? = null)       // no key when nothing matches (verified)
@Serializable data class PlaceDto(val name: String, val latitude: Double, val longitude: Double,
                                  val admin1: String? = null, val country: String? = null)
@Serializable data class ForecastResponseDto(val daily: DailyDto? = null)
@Serializable data class DailyDto(
    val time: List<String> = emptyList(),
    @SerialName("temperature_2m_max") val temperatureMax: List<Double?> = emptyList(),
    @SerialName("temperature_2m_min") val temperatureMin: List<Double?> = emptyList(),
    @SerialName("precipitation_probability_max") val precipitationProbabilityMax: List<Double?> = emptyList(),
    @SerialName("wind_speed_10m_max") val windSpeedMax: List<Double?> = emptyList(),
    @SerialName("uv_index_max") val uvIndexMax: List<Double?> = emptyList(),
)
```
- **Wiring:** `AppContainer` builds one `OkHttpClient()` (default 10 s timeouts) and one `Json { ignoreUnknownKeys = true }`. It builds two `Retrofit`s with `json.asConverterFactory("application/json".toMediaType())` and exposes `weatherRepository = OpenMeteoWeatherRepository(geocodingApi, forecastApi)`.
- **Forecast request:** start date and end date are both the hike date (`LocalDate.toString()`).

**Mapping and failure classes** (`OpenMeteoWeatherRepository`; decision 2; answer 2; CEO T1/T2)
```kotlin
data class Place(val name: String, val region: String?, val country: String?, val latitude: Double, val longitude: Double) {
    val subtitle: String get() = listOfNotNull(region, country).joinToString(", ")
    fun toArea() = Area(areaLabel(name, region, country), latitude, longitude)
}
fun areaLabel(name: String, region: String?, country: String?) = when {          // decision 9
    region != null && !region.equals(name, ignoreCase = true) -> "$name, $region"
    country != null -> "$name, $country"
    else -> name
}
data class DailyForecast(val date: LocalDate, val minC: Int?, val maxC: Int?, val rainPct: Int?, val windKmh: Int?, val uv: Int?)
enum class WeatherFailure { OFFLINE, OTHER }
sealed interface PlaceSearchResult { data class Found(val places: List<Place>) : PlaceSearchResult
    data object NoMatch : PlaceSearchResult; data class Failed(val failure: WeatherFailure) : PlaceSearchResult }
sealed interface ForecastResult { data class Day(val forecast: DailyForecast) : ForecastResult
    data object NoRowForDate : ForecastResult; data class Failed(val failure: WeatherFailure) : ForecastResult }
```
- **`searchPlaces`:**
  - `results.orEmpty()` mapped (admin1 → region), first 5, in API order. Empty → `NoMatch`.
- **`dailyForecast`:**
  - Index = `daily.time.indexOf(date.toString())`. A missing `daily` or index −1 → `NoRowForDate`.
  - Each value is `list.getOrNull(index)?.roundToInt()`, so null stays null.
- **Catch by name, in this order** (never `catch (e: Exception)`; `CancellationException` passes through):
  1. `UnknownHostException`, `ConnectException` → `Failed(OFFLINE)`.
  2. Other `IOException` (including `SocketTimeoutException`) → `Failed(OTHER)`.
  3. `HttpException` → `Failed(OTHER)`, except code 400 on the forecast → `NoRowForDate`.
  4. `kotlinx.serialization.SerializationException` → `Failed(OTHER)`.
- **Logging (T11):** one line per request on tag `Sentiero/Weather`, with the parameters, the outcome class and the exception's simple name.

**Pure rules** (`domain/`, each with a JVM test)
```kotlin
// ForecastCardPolicy.kt — rule 18, checked in this order before any request
sealed interface ForecastCard { data object Hidden : ForecastCard; data object NoArea : ForecastCard
    data class TooFar(val availableFrom: LocalDate) : ForecastCard; data class Request(val area: Area, val date: LocalDate) : ForecastCard }
fun forecastCard(hike: Hike, today: LocalDate): ForecastCard = when {
    hike.completedAt != null -> ForecastCard.Hidden                                  // 18.1
    hike.date.isBefore(today) -> ForecastCard.Hidden                                 // 18.2
    hike.area == null -> ForecastCard.NoArea                                         // 18.3 (before 18.4, R3-15)
    hike.date.isAfter(today.plusDays(15)) -> ForecastCard.TooFar(hike.date.minusDays(15))   // 18.4
    else -> ForecastCard.Request(hike.area, hike.date)                               // 18.5
}

// DateLabels.kt — decision 3 (replaces Spec 1's formatter)
fun shortDate(date: LocalDate, today: LocalDate): String =
    date.format(DateTimeFormatter.ofPattern(if (date.year == today.year) "EEE d MMM" else "EEE d MMM yyyy", Locale.ENGLISH))
```
`SuggestionRules.kt` (2.8), `JournalRules.kt` (2.11, 2.15) and `ReminderTime.kt` (2.16) are specified in their children.

**Routes and navigation** (ENG-R4)
```kotlin
@Serializable object Upcoming
@Serializable object Completed                                                       // 2.5
@Serializable data class HikeForm(val editId: String? = null)                        // editId used from 2.14
@Serializable data class HikeDetail(val id: String, val fromLastHike: Boolean = false, val fellBackToBasics: Boolean = false) // flags: 2.9

fun NavController.openTab(tab: Any) = navigate(tab) { popUpTo<Upcoming> { saveState = true }; launchSingleTop = true; restoreState = true }
```
- **Tab bar:** Upcoming and Completed each put `SentieroBottomBar` in their own `Scaffold(bottomBar = …)`, so there are no nested Scaffolds.
  - `NavigationBar` with icons `hiking` and `menu_book` and labels "Upcoming" and "Completed".
  - Selected = `currentBackStackEntry.destination.hasRoute<…>()`.
- **Detail and form** have no tab bar. The FAB is on Upcoming only.

**Copy** (all in `res/values/strings.xml`; DR-1A vocabulary, so no user-visible "trip")

| Where | Text |
|---|---|
| Tabs | Upcoming · Completed |
| Due row (2.5/2.6) | [Mark as done] · on the hike date: [Mark as done] when you're back |
| Completed empty | No completed hikes yet. When a hike is over, open it and tap Mark as done: your notes and anything you forgot land here. · [Go to Upcoming] |
| Completed row | No notes · Forgot: %1$s · Forgot: %1$s +%2$d more |
| Search (2.15) | Search route or notes · No hikes match "%1$s". · [Clear] |
| Detail menu | More options · Edit hike · Delete · Move back to Upcoming |
| Delete dialog | Delete %1$s? · Its packing list and notes will be deleted too. · [Cancel] [Delete] |
| Checklist | + Add item · Add item (icon) · Delete %1$s (icon) · Nothing to pack yet. Add the first thing you'll carry. · %1$s deleted. [Undo] |
| Tags | Forgot last time (upcoming, carried over) · Forgot (journal) |
| Errors | Couldn't save. Try again. |
| Mark as done | Moved to Completed. [Undo] · Moved back to Upcoming. |
| Sheet | How did it go? · Notes · Anything you forgot? · These go to the top of your next hike's list. · + Add something you forgot · Remove %1$s (icon) · [Save to journal] |
| Sheet dismiss | Discard your notes? · [Keep editing] [Discard] |
| Journal | Notes · [Edit journal] |
| Area field | Area (optional) · Pick the village nearest the trailhead. Mountain weather can differ from the coast. · No place selected. The hike saves without a forecast. · Type at least 2 letters. · Search places (icon) · Clear area (icon) |
| Area states | No places found for '%1$s'. Try the nearest village or town. · Couldn't search right now. [Retry] |
| Add area sheet | Add area |
| Forecast | Forecast for %1$s · %2$s · Getting the forecast for %1$s… · %1$s to %2$s · Rain %3$s · Wind %4$s · UV %5$s · Forecast from Open-Meteo. Always check conditions before you go. |
| Forecast states | Add an area to see the forecast. [Add area] · Forecast available from %1$s. · Couldn't load the forecast. · You're offline. · No forecast for this date yet. · [Retry] |
| Chips | + %1$s · Add %1$s to the list (TalkBack) |
| Form (2.9/2.14) | Packing list starts from · Last hike: %1$s (%2$d items) (plurals) · Last hike was deleted; started from basics. · Edit hike · Save changes · Changing difficulty doesn't change your list. |
| Same route | You hiked this on %1$s. · You hiked this on %1$s: "%2$s" |
| Reminder | Remind me the evening before · We'll remind you %1$s, around 18:00 · Notifications are off for Sentiero. [Open settings] · Too late for a reminder |
| Notification | Channel "Hike reminders" ("The evening-before packing reminder.") · Title "Tomorrow: %1$s" · "%d item left to pack." / "%d items left to pack." (plurals) · "Everything is packed." when 0 |

Value formatting (pure helpers, nulls shown as "–"):
- `formatTemp(12)` = "12°"
- `formatPercent(70)` = "70%"
- `formatKmh(25)` = "25 km/h"
- UV is shown as the bare number.
- Any null → "–". Example: "– to 19° · Rain – · Wind 25 km/h · UV 4".

## Child Issues

| # | Title | Tier | Effort | Status | Dependencies |
|---|---|---|---|---|---|
| 2.1 | Checklist editing: add, delete + Undo, zero items | P1 | 4 h | todo | Spec 1 |
| 2.2 | Weather data layer: Retrofit, DTOs, failure classes | P1 | 5 h | todo | Spec 1 |
| 2.3 | Area search + area on the new-hike form | P1 | 5 h | todo | 2.2 |
| 2.4 | Forecast panel with every state + Add area | P1 | 6 h | todo | 2.3 |
| 2.5 | Completed tab, journal mode, due rows | P1 | 5 h | todo | Spec 1 |
| 2.6 | Mark as done: sheet, transaction, Undo, two hosts | P1 | 7 h | todo | 2.5 |
| 2.7 | Delete hike | P1 | 2 h | todo | Spec 1 |
| 2.8 | Suggestion chips | P2 | 4 h | todo | 2.1, 2.4 |
| 2.9 | Start from last hike + memory tag + arrival | P2 | 6 h | todo | 2.6 |
| 2.10 | Add something you forgot | P2 | 4 h | todo | 2.6 |
| 2.11 | Same-route note | P2 | 2 h | todo | 2.5 |
| 2.12 | Move back to Upcoming | P3 (dropped 5th) | 1 h | todo | 2.6 |
| 2.13 | Edit journal | P3 (dropped 4th) | 2 h | todo | 2.10 |
| 2.14 | Edit hike | P3 (dropped 3rd) | 4 h | todo | 2.3 |
| 2.15 | Completed search | P3 (dropped 2nd) | 2 h | todo | 2.5 |
| 2.16 | Evening-before reminder | P3 (dropped 1st) | 8 h | todo | 2.6, 2.7, 2.14 |
| 2.17 | Final hardening, demo rehearsal, release | all | 5 h | todo | everything built |

## Dependency Graph
```
Spec 1 ─┬─▶ 2.1 Checklist editing ───────────────────────────────┐
        ├─▶ 2.2 Weather data ─▶ 2.3 Area search ─▶ 2.4 Forecast ─┴─▶ 2.8 Chips
        │                            └──────────────────────────────▶ 2.14 Edit hike ─┐
        ├─▶ 2.5 Completed + journal mode ─┬─▶ 2.6 Mark as done ─┬─▶ 2.9 From last hike
        │                                 │                     ├─▶ 2.10 Add forgotten ─▶ 2.13 Edit journal
        │                                 │                     └─▶ 2.12 Move back
        │                                 ├─▶ 2.11 Same-route note
        │                                 └─▶ 2.15 Completed search
        └─▶ 2.7 Delete hike ───────────────────────────────────────────────────────────┴─▶ 2.16 Reminder (needs 2.6, 2.7, 2.14 paths)
All ─▶ 2.17 Final hardening
```

## Sequencing Rationale

- **P1 before everything.** P1 is the cut line: with Spec 1 it covers all four brief bullets, including the API with visible states and the separate Upcoming and Completed screens.
  - Tag `p1-complete` when its children pass, so the brief is safe even if P2 and P3 slip.
- **Inside P1, network comes early (CEO-1 "risk-first").**
  - 2.1 is a small warm-up right after the midterm and unblocks the chips.
  - 2.2 → 2.3 → 2.4 is the riskiest brief bullet (new library, two APIs, the professor's API approval), so it goes first.
  - 2.5 → 2.6 (journal core) is pure Room work you already know.
  - 2.7 is small.
  - Reordering the weather after the journal pushes the riskiest work into the weeks with the least slack.
- **P2 follows CEO-1's listing (chips, last hike, add forgotten, same-route note).** Every P2 child needs P1 data: the forecast for the chips, and completed hikes with flags for the copy. Build 2.9 and 2.10 back to back: the demo sentence (a Headlamp that was never on an Easy list) needs both.
- **P3 is built in reverse drop order** (decision 1), so whatever is left unbuilt when time runs out is exactly what the drop order says to cut.
  - The reminder is last because it is the most expensive P3 item (~8 h) with the most platform risk (permission, WorkManager, Doze).
  - It also needs every path that changes a trip to exist first: delete (2.7), Mark as done (2.6) and date edit (2.14). ENG-R6 makes TripRepository the only place that calls the scheduler.
- **2.17 runs last but is not optional.** It is the demo rehearsal and the release.

---

### 2.1 Checklist editing (P1 · rules 8, 10, 11 · ENG-R8 · DR-2B, DR-6A, DR-6B)
**Build**
- **`addItem(tripId, label)`:**
  - `label.trim()`. Blank → returns `false` and writes nothing.
  - Otherwise, inside `db.withTransaction`, inserts one unpacked row at `nextPosition(tripId)` with every flag false. The transaction serializes two fast adds.
  - It and the sheet (2.6/2.10) share one private `insertAppended(tripId, id, label, forgotten, addedAfterHike)`. This is the single append path (ENG-R8).
- **`deleteItem(itemId)`** returns the deleted row as a `PackingItem` with every field, or `null` if it is already gone.
- **`restoreItem(tripId, item)`** re-inserts the same id, position and flags (rule 11 Undo). A `SQLiteConstraintException` (the hike was deleted meanwhile) is caught, logged on `Sentiero/Db`, and ignored.
- **Detail, upcoming mode — add field.** After the last row: a single-line `OutlinedTextField` with placeholder "+ Add item".
  - `ImeAction.Done` and a trailing `add` icon button ("Add item") both submit.
  - Submitting adds the item, clears the field and keeps focus.
  - Cap 60 characters; the counter appears from 48 (DR-6A).
- **Delete.** Each row ends with a 48dp ✕ (`close`, "Delete <label>").
  - Tapping it deletes and shows "<label> deleted." with Undo, `SnackbarDuration.Long`.
  - Undo calls `restoreItem`.
- **Zero items (DR-2B).** The "Packing · n of m" header and the progress line are hidden. "Nothing to pack yet. Add the first thing you'll carry." (`body`, `on-surface-variant`) sits above the add field.
- **Write failure.** "Couldn't save. Try again." snackbar; the typed text stays.

**Accept**
1. On a 12-item hike, "Headlamp" + Done adds row 13, unpacked; the header reads "Packing · 0 of 13".
2. Submitting "   " adds nothing.
3. ✕ on "Snacks" removes it. Undo puts it back in the same place with the same packed state.
4. Deleting every item shows the zero-items text. Adding one item shows "Packing · 0 of 1".
5. The add field stops at 60 characters.

**Tests:** `RoomTripRepositoryTest` (instrumented) +2:
- `addItem` appends at max + 1, trims, and ignores blank.
- Delete → restore gives back the same id, position and `isPacked`.

**Effort:** repository 1 h · UI 2 h · tests 1 h.

### 2.2 Weather data layer (P1 · rules 15-18 data · CEO T1, T2, T11 · ENG-R13 · answer 2)
**Build**
- Dependencies, INTERNET permission, `GeocodingApi`, `ForecastApi`, DTOs, `OpenMeteoWeatherRepository` and `AppContainer` wiring, exactly as in Shared contracts.
- **`app/build.gradle.kts`:** `testOptions { unitTests.isReturnDefaultValues = true }` (decision 12).
- **Fixtures** in `app/src/test/resources/`:
  - `geocoding_no_match.json` = `{"generationtime_ms":0.17237663}`. This is the live body of 2026-11-01.
  - `geocoding_tripoli.json`: capture it once from your laptop with `curl 'https://geocoding-api.open-meteo.com/v1/search?name=Tripoli&count=5&language=en&format=json'`.
  - `forecast_ok.json`: hand-written in the documented shape for 2026-11-14 with values 19.3 / 12.1 / 70 / 25.2 / 4.15.
  - `forecast_nulls.json`: the same file with `precipitation_probability_max` and `uv_index_max` set to `[null]`.
- **Fakes** in `app/src/test/.../fakes/`:
  - `FakeGeocodingApi` and `FakeForecastApi` each take a `suspend` lambda, so a test can return a fixture, throw, or wait on a `CompletableDeferred`.

**Accept**
1. `./gradlew test` passes `JsonFixturesTest`, `WeatherRepositoryTest` and `PlaceTest`.
2. `grep -rn "catch (e: Exception)" app/src/main` prints nothing.
3. With airplane mode on, a forecast request logs one `Sentiero/Weather` line ending in `Failed(OFFLINE) UnknownHostException`.

**Tests**
- `JsonFixturesTest` +4: each fixture decodes with the production `Json`.
- `WeatherRepositoryTest` +6:
  - no `results` → `NoMatch`
  - Tripoli → 5 places in API order, Libya first
  - `UnknownHostException` and `ConnectException` → `OFFLINE`
  - timeout, HTTP 500 and `SerializationException` → `OTHER`
  - forecast HTTP 400 and a missing date row → `NoRowForDate`
  - nulls stay null; 12.5 → 13 and −2.5 → −2
- `PlaceTest` +2: label rules (decision 9); subtitle.

**Effort:** dependencies + DI 1 h · APIs + DTOs 1 h · repository 1.5 h · fixtures + tests 1.5 h.

### 2.3 Area search + area on the new-hike form (P1 · rules 15, 16, 17 · ENG-R7 · CEO T5 · DR-2D)
**Build**
- **`ui/components/AreaSearchState.kt`.** A plain state holder owned by a ViewModel, built with `(weather, viewModelScope, initial: Area?)`. It lives in the ViewModel so results survive rotation and requests die with the screen.
```kotlin
sealed interface SearchPhase { data object Idle : SearchPhase; data object Loading : SearchPhase
    data class Results(val places: List<Place>) : SearchPhase; data class Empty(val query: String) : SearchPhase; data object Error : SearchPhase }
sealed interface AreaFieldUi { data class Editing(val text: String, val phase: SearchPhase, val tooShort: Boolean) : AreaFieldUi
    data class Picked(val area: Area) : AreaFieldUi }
// onTextChange(text): cancel the running job; Editing(text.take(60), Idle, tooShort = false). Results always match the text.
// submit(): q = text.trim(); q.length < 2 → tooShort = true, no request; else remember q, cancel the old job (T5), Loading, launch search.
// retry(): same as submit with the remembered query.   pick(place): cancel job; Picked(place.toArea()).   clear(): Editing("", Idle, false).
// pickedArea: Area? = (ui as? Picked)?.area
```
- **`ui/components/AreaSearchField.kt`** is stateless: `(ui, onTextChange, onSubmit, onRetry, onPick, onClear, unpickedHint: String?)`.
  - **Editing:**
    - Outlined field labelled "Area (optional)", `ImeAction.Search`, and a trailing `search` icon button ("Search places").
    - Supporting text, first match wins: "Type at least 2 letters." when `tooShort`; the rule 15 helper when the text is blank; otherwise `unpickedHint ?: helper`.
    - Under the field:
      - Loading: a thin `LinearProgressIndicator`.
      - Results: up to 5 `ListItem` rows with headline = name, supporting = subtitle, hairline dividers, whole row tappable.
      - Empty: the rule 16 empty copy.
      - Error: the rule 16 error copy with a [Retry] text button.
  - **Picked:** a read-only field showing the label with a trailing ✕ ("Clear area"). To change the area, clear it and search again.
- **Form.** Row order: Route name → Date → Difficulty → Area → list chips → Save (DR Pass 1).
  - `HikeFormViewModel` owns `areaSearch` and passes `unpickedHint` = "No place selected. The hike saves without a forecast." (DR-2D).
  - Save calls `createHike(…, area = areaSearch.pickedArea)`. The factory now also reads `weatherRepository`.
- **Save never waits on the network (rule 17).** A running search is cancelled when the form leaves the back stack.

**Accept**
1. Submitting "B" shows "Type at least 2 letters." and logs no request.
2. "Faraya" lists Guinea first and Lebanon second. Nothing is picked until a row is tapped.
3. "zzqx" shows "No places found for 'zzqx'. Try the nearest village or town." Backup query: "qxzvq" (also verified empty).
4. In airplane mode: "Couldn't search right now." with Retry. After reconnecting, Retry shows results.
5. Typing a name without picking shows the DR-2D hint, and Save creates the hike with no area.
6. Picking "Bsharri" shows "Bsharri, North Lebanon" read-only with ✕. ✕ returns to an empty field with the helper.
7. Rotation with results on screen keeps the text and the results.
8. Tapping Save while a search is loading opens the new hike at once, without an area.

**Tests**
- `AreaSearchStateTest` +4:
  - too short → no request
  - a newer submit cancels the older one: query A held on a gate, B answered, A released → Results(B) (T5)
  - Error → Retry → Results
  - editing the text clears Results
- `HikeFormViewModelTest` +1: unpicked text saves with `area = null`.

**Effort:** state holder 1.5 h · field 1.5 h · form 1 h · tests 1 h.

### 2.4 Forecast panel with every state + Add area (P1 · rule 18 · DR-2C · answer 2 · R3-4, R3-5, R3-6)
**Build**
- **`domain/ForecastCardPolicy.kt`** as in Shared contracts.
- **`HikeDetailViewModel` gets `forecast: ForecastPanelUi`.** It is recomputed on every hike emission with `LocalDate.now(clock)`:
```kotlin
sealed interface ForecastPanelUi { data object Hidden : ForecastPanelUi; data object NoArea : ForecastPanelUi
    data class TooFar(val from: LocalDate) : ForecastPanelUi; data class Loading(val place: String) : ForecastPanelUi
    data class Content(val place: String, val date: LocalDate, val daily: DailyForecast) : ForecastPanelUi
    data class Error(val noRow: Boolean, val offline: Boolean) : ForecastPanelUi }
// Hidden / NoArea / TooFar → cancel the job, forget the key.   Request(area, date) → fetch only if it differs from the last key,
// so rotation and ticks never refetch. Fetch: cancel old job; Loading(area.label); Day → Content; NoRowForDate → Error(noRow = true);
// Failed(f) → Error(noRow = false, offline = f == OFFLINE). retryForecast() works only from Error (ignored while Loading).
```
- **`ui/detail/ForecastPanel.kt`** follows DESIGN.md `forecast-panel`: `surface-container`, `md` corners, `md` padding. Every visible state gets `Modifier.heightIn(min = 148.dp)` (one height, still no fixed heights; DR-6B).
  - NoArea: copy + [Add area].
  - TooFar: "Forecast available from <shortDate(from)>."
  - Loading: thin progress bar + "Getting the forecast for <place>…".
  - Content:
    - title "Forecast for <place> · <shortDate>" in `label`
    - the values line in `body` with `tnum`; UV is shown (R3-6)
    - chips (2.8)
    - attribution in `body-small` `on-surface-variant`
  - Error:
    - "Couldn't load the forecast.", or "No forecast for this date yet." when `noRow`
    - "You're offline." under it when `offline`
    - [Retry] (decision 4)
- **Order on the detail screen (DR-1B):** title → date line → (2.11 note) → forecast panel → packing. Journal mode is always `Hidden`.
- **Add area sheet.** A `ModalBottomSheet` titled "Add area" containing `AreaSearchField` with `unpickedHint = null`, backed by a second `AreaSearchState` owned by the detail ViewModel.
  - Tapping a result calls `setArea(id, place.toArea())` and closes the sheet. The hike emits, and the panel goes Loading → Content.
  - Dismissing writes nothing.

**Accept**
1. "Jabal Moussa Reserve" with area "Jabal Moussa, Mount Lebanon", dated within 15 days: "Getting the forecast for Jabal Moussa, Mount Lebanon…", then Content with the values line, UV and attribution.
2. No area: "Add an area to see the forecast." Picking a place in the sheet shows Loading, then Content, without leaving the screen.
3. With an area, dated today + 16: "Forecast available from <today + 1>.", and no `Sentiero/Weather` line is logged.
4. A past-dated upcoming hike shows no panel.
5. Airplane mode: "Couldn't load the forecast." + "You're offline." + Retry. After reconnecting, Retry shows Content.
6. Rotating while Content shows keeps it, with exactly one Weather log line.
7. Ticking items never logs a new Weather line.

**Tests**
- `ForecastCardPolicyTest` +6:
  - completed → Hidden
  - past → Hidden
  - no area → NoArea, even when dated today + 30
  - today → Request
  - today + 15 → Request
  - today + 16 → TooFar(date − 15)
- `HikeDetailViewModelTest` +3 (fake repositories, fixed `Clock`):
  - offline → Error(offline = true) → Retry → Content
  - NoRowForDate → Error(noRow = true)
  - journal mode → Hidden with zero forecast calls

**Effort:** policy 1 h · ViewModel 1.5 h · panel 2 h · Add-area sheet 1 h · tests 0.5 h.

### 2.5 Completed tab, journal mode, due rows (P1 · rules 1, 2, 4, 6 · DR-1A, DR-2A, DR-7A · decision 3)
**Build**
- **Data and navigation.** `Completed` route, `openTab`, `SentieroBottomBar`, `TripDao.observeCompleted()` and the mapping to `CompletedSummary`.
- **`CompletedViewModel(repo, clock)`.** States `Loading` (renders nothing), `Empty` and `Content(rows)`. A row has:
  - `routeName`
  - `dateLabel` "Sun 25 Oct · Easy"
  - `notes` (trimmed, null if blank)
  - `forgotLabel` (null, "Forgot: Headlamp", or "Forgot: Headlamp +2 more")
- **`CompletedScreen`** follows DR-7A:
  - route in `title`, wrapping to 2 lines then "…"
  - the date line in `body-small`
  - notes in `body`, `on-surface-variant`, `maxLines = 2`, ellipsis; "No notes" in muted italic when null
  - the memory tag with `forgotLabel` (DESIGN.md `memory-tag`)
  - Empty: the copy + [Go to Upcoming] (`openTab(Upcoming)`)
- **Upcoming due rows** (`date <= today`, decision 5).
  - The row's second line keeps "Sat 31 Oct · Date passed" or "Sat 14 Nov · Today".
  - A third line replaces the progress line and the count. It holds the "Mark as done" text button (wired in 2.6) and, on the hike date only, " when you're back" in `body-small`.
- **Journal mode** of `HikeDetailScreen` (`completedAt != null`):
  - title, then "Sat 14 Nov · Moderate", then "Notes" (`title-small`) and the notes (`body`) or "No notes", then the read-only checklist.
  - Checkboxes take `onCheckedChange = null`.
  - Items flagged forgotten carry the memory tag "Forgot". TalkBack reads "Headlamp, not packed, forgot".
  - No forecast, add field, ✕, reminder or pinned button.
- **`shortDate`** (decision 3) replaces every date format in the app.

**Accept**
1. With no completed hikes, Completed shows the empty copy, and [Go to Upcoming] switches tabs.
2. Completed lists Sat 31 Oct before Sun 25 Oct. Two hikes on the same date show the later `completedAt` first.
3. A completed row shows the route, "Sun 25 Oct · Easy", two lines of notes ending in "…", and "Forgot: Headlamp +2 more". A hike without notes shows "No notes".
4. A completed hike opens in journal mode: no forecast, no add field, no ✕, and the checkboxes can't be toggled.
5. The tab bar appears only on Upcoming and Completed. Back from Completed returns to Upcoming.
6. A hike dated 2025-10-25 shows "Sat 25 Oct 2025". With the phone set to French, labels stay English.

**Tests**
- `TripDaoTest` (instrumented) +3:
  - Completed order, including the tie-breaker
  - `forgottenCount` and `firstForgotten`
  - Upcoming excludes completed hikes
- `DateLabelsTest` +2: same year / other year; English with a French default locale.

**Effort:** DAO + repository 1 h · navigation + tab bar 1 h · Completed screen + ViewModel 1.5 h · journal mode 1 h · due rows 0.5 h.

### 2.6 Mark as done: sheet, transaction, Undo, two hosts (P1 · rules 4, 5 · CEO T2, T3 · DR-1B, DR-1C, DR-2E · answer 1)
**Build**
- **`journalSource(id)`** reads the notes and the items in position order once.
- **`saveJournal(id, draft, markDone)`** is one `db.withTransaction` (T3):
  1. `setNotes(id, draft.notes)`
  2. `clearForgotten(id)`
  3. `markForgotten(id, forgottenIds)` if not empty
  4. `deleteAddedAfterHike(id, removedIds)` if not empty
  5. `insertAppended(…, addedAfterHike = true)` for each `added`, in order
  6. if `markDone`: `setCompletedAt(id, Instant.now(clock))` and `setReminderOn(id, false)` (decision 6)
  - After the commit, if `markDone`, call the scheduler's `cancel(id)` (2.16).
  - `RoomTripRepository` now takes the `clock`.
- **`reopenHike(id)`** = `setCompletedAt(id, null)`. It keeps notes, flags and added items (rule 5).
- **`MarkDoneViewModel(hikeId, repo)`** (answer 1):
  - **Scope and lifetime.** Created with `viewModel(key = "markDone-$hikeId", factory = MarkDoneViewModel.factory(hikeId))` inside the host destination, so it lives in that back-stack entry and survives rotation.
  - **Opening.** `start()` loads from Room only if nothing is loaded yet. `reset()` (on close) clears that, so the next open starts from saved data (rule 5).
```kotlin
data class SheetRow(val id: String, val label: String, val forgotten: Boolean, val removable: Boolean, val isNew: Boolean)
data class MarkDoneUi(val loaded: Boolean = false, val notes: String = "", val rows: List<SheetRow> = emptyList(), val addText: String = "",
                      val isSaving: Boolean = false, val saveFailed: Boolean = false, val hasChanges: Boolean = false)
// onNotesChange(text.take(2_000)); onToggleForgot(id); save(markDone): returns at once if isSaving (double tap);
// success → event Saved(markDone); SQLiteException → saveFailed = true, draft kept (T2). hasChanges compares with the loaded snapshot.
```
- **`ui/detail/MarkDoneSheet.kt`.** A `ModalBottomSheet` (`skipPartiallyExpanded = true`) styled with DESIGN.md `bottom-sheet`:
  - Title "How did it go?".
  - Notes field "Notes", `minLines = 3`, cap 2,000 with the counter from 1,600.
  - "Anything you forgot?" (`title-small`) with one toggleable row per item: checkbox = forgot, the whole row is one TalkBack element. With zero items, the section is hidden until 2.10.
  - [Save to journal]: filled, full width, with a spinner while saving.
  - On failure, "Couldn't save. Try again." in `error` above the button.
- **Dismiss gate (DR-2E).** Every dismissal path (swipe, Back, scrim tap) goes through `rememberModalBottomSheetState(confirmValueChange = …)`.
  - With `hasChanges`, it returns `false` and opens "Discard your notes?": [Keep editing] / [Discard]. Discard writes nothing and closes.
  - Without changes, the sheet closes directly.
  - After `Saved` or Discard, the host closes the sheet by setting `sheetFor = null`, which removes it from composition. That skips the gate, then `reset()` runs.
- **Hosts.** Each keeps `sheetFor: String?` in `rememberSaveable`, so an open sheet survives rotation.
  - **`UpcomingScreen`:** the due row's "Mark as done" button.
  - **`HikeDetailScreen`:** in upcoming mode with `date <= today`, a pinned full-width "Mark as done" `Button` in `Scaffold(bottomBar = …)`. It is the screen's only filled action. There is no button before the hike date (rule 4).
- **After `Saved(markDone = true)`.** The host shows "Moved to Completed." with Undo (`SnackbarDuration.Long`). Undo calls `repo.reopenHike(id)` through the host's ViewModel.
  - On the detail screen you stay, and it switches to journal mode (DR-1C).
  - On Upcoming the row leaves.

**Accept**
1. A hike dated tomorrow has no pinned button and no row button. Dated today, it has both.
2. On the detail screen: type notes, tick "Sun hat", Save. The same screen turns into journal mode and shows the snackbar. Completed shows the hike with "Forgot: Sun hat".
3. Undo returns it to Upcoming. Mark as done again opens the sheet prefilled with those notes and the Sun hat flag.
4. From the Upcoming row: Save removes the row and shows the snackbar on Upcoming. Undo brings the row back.
5. With typed notes, swipe down, Back and a scrim tap each show "Discard your notes?". Keep editing keeps the text. Discard closes, and the hike is still upcoming with nothing written. Without changes, there is no dialog.
6. Rotating with the sheet open keeps the notes and the ticks.
7. Double-tapping [Save to journal] completes the hike once (one snackbar).

**Tests**
- `RoomTripRepositoryTest` +3:
  - `saveJournal(markDone = true)` writes notes, flags and `completedAt`, and sets `reminder_on = 0`.
  - All-or-nothing: a draft whose new item reuses an existing item id throws, and nothing changes.
  - `reopenHike` keeps notes and flags.
- `MarkDoneViewModelTest` +3:
  - opens from the saved notes and flags
  - `hasChanges` is false until an edit
  - a save failure keeps the draft and sets `saveFailed`

**Effort:** repository 1.5 h · ViewModel 1.5 h · sheet 2 h · two hosts + snackbar 1 h · tests 1 h.

### 2.7 Delete hike (P1 · rule 7)
**Build**
- **Menu.** A ⋮ `IconButton` (`more_vert`, "More options") in the detail top app bar, in both modes, opens a `DropdownMenu`. Delete is always present; 2.12 and 2.14 add their items later.
- **Confirmation.** Delete opens an `AlertDialog`: "Delete <route>?" / "Its packing list and notes will be deleted too." / [Cancel] [Delete], both plain text buttons.
- **`deleteHike(id)`** = `deleteTrip(id)`; items go by cascade. 2.16 adds the reminder `cancel(id)`.
- **After deleting.** Once `deleteHike` succeeds, the ViewModel sets a `deleted` flag. The next `null` hike then maps to `HikeDetailUiState.Deleted` instead of `NotFound`. `Deleted` renders nothing and calls `popBackStack()` once, so "This hike no longer exists." never flashes; that message stays for hikes missing on open.

**Accept**
1. Delete from upcoming mode and from journal mode returns to the list without the hike.
2. Database Inspector shows none of its `checklist_items` rows.
3. Cancel changes nothing.

**Tests:** `TripDaoTest` +1: deleting a trip removes its items.

**Effort:** 2 h.

### 2.8 Suggestion chips (P2 · rule 19 · ENG-R11 · decision 2)
**Build**
- **`domain/SuggestionRules.kt`.** The table below is the contract:
```kotlin
private class Row(val item: String, val fires: (DailyForecast) -> Boolean, val suppressedBy: List<String>)
private val TABLE = listOf(
    Row("Rain jacket", { (it.rainPct ?: -1) >= 50 }, listOf("rain jacket", "raincoat", "rain coat", "poncho", "waterproof jacket")),
    Row("Dry bag", { (it.rainPct ?: -1) >= 50 }, listOf("dry bag", "waterproof bag")),
    Row("Warm layer", { it.minC != null && it.minC <= 5 }, listOf("warm layer", "fleece", "down jacket")),
    Row("Gloves", { it.minC != null && it.minC <= 5 }, listOf("gloves")),
    Row("Extra water (1 L)", { (it.maxC ?: Int.MIN_VALUE) >= 28 }, listOf("extra water")),
    Row("Sunscreen", { (it.uv ?: -1) >= 6 }, listOf("sunscreen", "sun cream")),
    Row("Windproof jacket", { (it.windKmh ?: -1) >= 40 },
        listOf("windproof", "windbreaker", "rain jacket", "raincoat", "rain coat", "waterproof jacket")),   // ENG-R11, poncho excepted
)
fun suggest(daily: DailyForecast, labels: List<String>): List<String> {
    val have = labels.map { it.trim().lowercase() }
    return TABLE.filter { it.fires(daily) && it.suppressedBy.none { s -> have.any { h -> h.contains(s) } } }.map { it.item }.take(3)
}
```
- **Chips.** Content shows `suggest(daily, currentItemLabels)`, recomputed whenever items change. Each is an `AssistChip` with a leading `add` icon, the item label, a neutral outline and `sm` corners, and never the blaze. TalkBack reads "Add <item> to the list".
- **Tap** calls `addItem(id, item)`. The new item suppresses its own chip, so the chip disappears.

**Accept**
1. Rain 70% and no rain gear on the list → "+ Rain jacket" and "+ Dry bag". Tapping Rain jacket appends "Rain jacket" unpacked at the end, and its chip disappears.
2. A list with "Raincoat" gets no Rain jacket chip and, at wind ≥ 40 km/h, no Windproof jacket chip. A list with only "Poncho" still gets Windproof jacket.
3. Never more than 3 chips, in table order.

**Tests:** `SuggestionRulesTest` +7:
- rain 50 fires both rain rows; 49 fires none
- min 5 fires Warm layer and Gloves; 6 doesn't
- max 28, UV 6 and wind 40 fire; 27, 5 and 39 don't
- null values never fire
- Raincoat and Poncho suppression
- the cap of 3, in table order
- case-insensitive trimmed matching ("  RAIN JACKET ")

**Effort:** rules 1 h · chips 1 h · tests 1.5 h · wiring 0.5 h.

### 2.9 Start from last hike + memory tag + arrival (P2 · rules 3, 9, 12 · CEO-6 · CEO T6 · DESIGN.md Motion)
**Build**
- **Last-hike chip data.** `HikeFormViewModel` (new mode) reads `observeCompleted().map { it.firstOrNull() }` (rule 3) into `lastHike: (id, routeName, totalCount)?`.
- **"Packing list starts from" chips** in a `FlowRow`:
  - "Last hike: <route> (<n> items)" (plurals; the route ellipsized to 1 line) appears only when `lastHike != null` (rule 12).
  - "Day-hike basics (N)" (Spec 1).
  - Exactly one is selected. "Last hike" is preselected at first load when it exists (CEO-6).
- **`createHike(…, source)`.** Inside one transaction:
  - `ListSource.LastHike(id)`: if that trip still exists, copy its forgotten items first in old order with `carriedOver = true`, then the rest in old order with `carriedOver = false` (rule 9).
  - Every copy starts unpacked, with `forgotten = false` and `addedAfterHike = false`, at positions 0..n−1. `carriedOver` is never copied itself.
  - If the source is gone, use `templateFor(difficulty)` and return `fellBackToTemplate = true` (T6).
  - A source with 0 items gives an empty list (DR-2B state).
- **Navigation.** Save goes to `HikeDetail(id, fromLastHike = source is LastHike && !fellBack, fellBackToBasics = fellBack)` with `popUpTo<HikeForm> { inclusive = true }`.
  - With `fellBackToBasics`, the detail screen shows "Last hike was deleted; started from basics." once.
  - A `rememberSaveable` flag stops it from repeating on rotation.
- **Memory tag (upcoming mode).** Rows with `carriedOver` show the tag "Forgot last time": `tertiary-container`, an 8dp `blaze` dot, `xs` corners.
  - TalkBack reads "Headlamp, not packed, forgot last time" (DR-6B).
- **Arrival moment.** When `fromLastHike` is set and a `rememberSaveable` "played" flag is still false, carried-over rows animate their background from `tertiaryContainer` to transparent with `tween(900, easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f))`, then set the flag.
  - Compose follows the system animator scale, so with "Remove animations" on, the tint doesn't show.

**Accept**
1. With no completed hike, only "Day-hike basics (N)" shows, and it is selected.
2. Chouf Cedars Loop (Easy, 10 items) is completed with Sun hat flagged. New hike then shows "Last hike: Chouf Cedars Loop (10 items)", selected.
3. Saving lands on a list with Sun hat first, tagged "Forgot last time" and unpacked, followed by the other 9 in old order, all unpacked. The tint fades in about 0.9 s.
4. Choosing "Day-hike basics" instead gives the difficulty template.
5. Deleting Chouf while the form is open, then saving, gives the template and "Last hike was deleted; started from basics."
6. With "Remove animations" on, no tint shows.

**Tests**
- `RoomTripRepositoryTest` +2: copy order and flags (rule 9); deleted source → template + `fellBackToTemplate`.
- `HikeFormViewModelTest` +1: "Last hike" is preselected when a completed hike exists.

**Effort:** repository 1.5 h · form chips 1.5 h · memory tag 1 h · arrival 1 h · tests 1 h.

### 2.10 Add something you forgot (P2 · rule 5 · ENG-R9, ENG-R10 · DR-3A · R3-30, R3-32)
**Build**
- **Sheet layout.** Under "Anything you forgot?" sits the helper "These go to the top of your next hike's list." (`body-small`, `on-surface-variant`; DR-3A). After the rows comes a field with placeholder "+ Add something you forgot": single line, `ImeAction.Done`, cap 60. With zero items, the section now shows the header, the helper and the field.
- **`onAddForgotten()`.** Trims; a blank entry is ignored. Then:
  - If the label equals an existing row's label (`trim()`, `ignoreCase = true`), that row's `forgotten` becomes true and nothing is added (ENG-R9).
  - Otherwise it appends `SheetRow(id = UUID, label, forgotten = true, removable = true, isNew = true)`.
  - The field clears either way.
- **Removable rows.** `removable = isNew || item.addedAfterHike`. Those rows show ✕ ("Remove <label>").
  - Removing a new row drops it from the draft.
  - Removing a persisted row adds its id to `removedIds`, which deletes only rows with `added_after_hike = 1` (ENG-R10).
- **Save** inserts new rows appended in the order they were added (R3-30), with `isPacked = false`, `forgotten` = the row's tick, and `addedAfterHike = true`.

**Accept**
1. Typing "Headlamp" adds a ticked row with ✕. After Save, the journal shows Headlamp last, tagged "Forgot".
2. Typing " sun HAT " ticks the existing Sun hat and adds no copy.
3. ✕ on a new row removes it before Save, and nothing is written.
4. **Loop:** complete Chouf with Headlamp added. New hike from "Last hike" shows Headlamp first, tagged "Forgot last time", unpacked.

**Tests**
- `MarkDoneViewModelTest` +2: a duplicate label flags the existing row; ✕ shows only on new or added-after-hike rows.
- `RoomTripRepositoryTest` +1: added rows are appended at the end with `addedAfterHike = 1`.

**Effort:** sheet field + rows 2 h · ViewModel 1 h · tests 1 h.

### 2.11 Same-route note (P2 · rule 14 · CEO-7 · DR-7B · R3-10)
**Build**
- **`domain/JournalRules.kt`:**
```kotlin
fun sameRouteMatch(hike: Hike, completed: List<CompletedSummary>): Hike? =        // completed is already in Completed order
    completed.map { it.hike }.firstOrNull { it.id != hike.id && it.routeName.trim().equals(hike.routeName.trim(), ignoreCase = true) }
fun noteExcerpt(notes: String): String? {                                          // null → "You hiked this on <date>."
    val flat = notes.trim().replace(Regex("\\s+"), " ")
    return when { flat.isEmpty() -> null; flat.length <= 140 -> flat; else -> flat.take(140).trimEnd() + "…" }
}
```
- **ViewModel.** `HikeDetailViewModel` combines the hike with `observeCompleted()` in upcoming mode only.
- **Note row.** Between the date line and the forecast panel, a tappable `Row` styled as the memory tag:
  - Without notes: "You hiked this on Sun 25 Oct."
  - With notes: 'You hiked this on Sun 25 Oct: "<excerpt>"'.
  - Tapping it navigates to `HikeDetail(match.id)` (journal mode). TalkBack: role Button, the text plus "Open journal entry".

**Accept**
1. Upcoming "jabal moussa reserve " with a completed "Jabal Moussa Reserve" that has notes shows the note with an excerpt. Tapping it opens that journal entry, and Back returns.
2. Without notes, it shows "You hiked this on Sun 25 Oct."
3. "Jabal Moussa" doesn't match "Jabal Moussa Reserve" (CEO-7).
4. With two matches, the most recent in Completed order wins.

**Tests:** `JournalRulesTest` +4:
- the match is case-insensitive, trimmed, and excludes itself
- the most recent match wins
- blank notes → null
- longer than 140 → 140 characters + "…"

**Effort:** 2 h.

### 2.12 Move back to Upcoming (P3, dropped 5th · rule 6)
**Build:** ⋮ menu item "Move back to Upcoming", in journal mode only. It calls `reopenHike(id)` and shows "Moved back to Upcoming.". You stay on the screen, which switches to upcoming mode.

**Accept**
1. Moving back keeps the notes and flags.
2. The hike reappears in Upcoming, sorted by its date.

**Tests:** covered by 2.6's `reopenHike` test, plus a manual check.

**Effort:** 1 h.

### 2.13 Edit journal (P3, dropped 4th · rules 5, 6 · ENG-R10)
**Build**
- **Entry.** Journal mode shows an [Edit journal] text button under the notes. It opens the same sheet with the same title and button (decision 11).
- **Save** calls `saveJournal(markDone = false)`: `completedAt` is unchanged, with no Undo and no snackbar.
- **Removal.** Rows added after the hike (persisted `addedAfterHike`) show ✕ and can be removed. Template and copied items can't.

**Accept**
1. Editing notes keeps the hike's place in Completed.
2. Removing a sheet-added "Headlamp" deletes it. Template items never show ✕ in the sheet.

**Tests:** `RoomTripRepositoryTest` +1: Edit-journal save keeps `completedAt` and deletes only added-after-hike rows.

**Effort:** 2 h.

### 2.14 Edit hike (P3, dropped 3rd · rule 7 · R3-2 · answer 3)
**Build**
- **Entry.** ⋮ menu item "Edit hike" (upcoming mode) → `HikeForm(editId = id)`.
- **Loading.** `HikeFormViewModel(editId)` loads `observeHike(editId).first()` once and prefills route, date, difficulty and `AreaSearchState(initial = hike.area)` (Picked). A null result pops back.
- **Form.**
  - Title "Edit hike"; button "Save changes".
  - No "Packing list starts from" chips.
  - Under Difficulty: "Changing difficulty doesn't change your list." (`body-small`).
  - The area is kept unless cleared or re-picked. Clearing it and saving removes it (R3-2).
- **Save** calls `updateHike(…)`, which never touches items (answer 3), then `popBackStack()` back to the detail.
  - A date change re-runs the forecast policy (new key) and, from 2.16, reschedules the reminder.
- **Back** discards the unsaved form without a dialog, same as New hike.

**Accept**
1. Changing Easy → Hard leaves the list exactly as it was.
2. Clearing the area and saving shows "Add an area to see the forecast."
3. Moving the date to tomorrow refetches the forecast for the new date.

**Tests**
- `HikeFormViewModelTest` +1: edit mode prefills and passes the stored area unchanged.
- `RoomTripRepositoryTest` +1: `updateHike` changes the fields and leaves the items untouched.

**Effort:** ViewModel + form mode 2.5 h · navigation + menu 0.5 h · tests 1 h.

### 2.15 Completed search (P3, dropped 2nd · rule 13 · DR-2A · R3-11, R3-12)
**Build**
- **`JournalRules.matchesQuery(summary, query)`:** `query.trim()` is blank, or the route or notes `contains(q, ignoreCase = true)` (decision 8).
- **`CompletedViewModel`** keeps `query` (survives rotation) and combines it with the list.
- **Search field.** It sits at the top of the list and shows only when there are at least 3 completed hikes. If the count drops below 3, it hides and the query clears.
  - Outlined, with a leading `search` icon, placeholder "Search route or notes", and a trailing ✕ when not empty. Cap 60.
  - Filtering happens as you type; it is local and instant.
- **No match:** 'No hikes match "<query>".' + [Clear].

**Accept**
1. With 3 completed hikes, typing "mud" keeps only hikes whose notes contain "Mud" or "mud".
2. "zzz" shows the no-match text, and Clear restores the list.
3. With 2 completed hikes, there is no search field.

**Tests:** `JournalRulesTest` +1: route or notes match, case-insensitive; blank query → all.

**Effort:** 2 h.

### 2.16 Evening-before reminder (P3, dropped 1st · CEO-3, CEO-5 · ENG-R6 · DR-2F · CEO T10)
**Build**
- **`domain/ReminderTime.kt`:**
```kotlin
fun reminderAt(hikeDate: LocalDate, now: ZonedDateTime): ZonedDateTime? =
    hikeDate.minusDays(1).atTime(18, 0).atZone(now.zone).takeIf { it.isAfter(now) }     // null → too late
sealed interface ReminderRow { data object TooLate : ReminderRow; data object Off : ReminderRow
    data class On(val fireAt: ZonedDateTime) : ReminderRow; data object Blocked : ReminderRow }
fun reminderRow(reminderOn: Boolean, fireAt: ZonedDateTime?, notificationsEnabled: Boolean, deniedThisVisit: Boolean) = when {
    fireAt == null -> ReminderRow.TooLate
    !notificationsEnabled && (reminderOn || deniedThisVisit) -> ReminderRow.Blocked
    reminderOn -> ReminderRow.On(fireAt)
    else -> ReminderRow.Off
}
```
- **Scheduler.** `reminder/ReminderScheduler.kt` holds `interface ReminderScheduler { fun schedule(hikeId: String, fireAt: ZonedDateTime); fun cancel(hikeId: String) }` and `WorkManagerReminderScheduler(workManager, clock)`:
  - one-time work with `setInitialDelay(Duration.between(now, fireAt))`
  - input `hikeId`, tag `reminder`
  - `enqueueUniqueWork("reminder-$hikeId", ExistingWorkPolicy.REPLACE, …)`
  - `cancel` = `cancelUniqueWork("reminder-$hikeId")`
- **TripRepository owns every reminder side effect (ENG-R6).** The constructor takes the scheduler.
  - `setReminder(id, true)`: if `reminderAt` is null, return `false` and write nothing. Otherwise set `reminder_on = 1` and schedule.
  - `setReminder(id, false)`: set `reminder_on = 0` and cancel.
  - `updateHike`: if `reminderOn`, reschedule, or turn it off and cancel when too late.
  - `deleteHike`: cancel.
  - `saveJournal(markDone)`: cancel (decision 6).
- **`reminder/ReminderWorker.kt`** (`CoroutineWorker`, default `WorkerFactory`; it reads the container from `applicationContext`):
  - `reminderContent(id)` null → success, log "skip … gone, done or off".
  - Notifications off → success, log "skip … notifications off". This is checked on API 33+ with `POST_NOTIFICATIONS` not granted, and on any API when `areNotificationsEnabled()` is false (T10).
  - Otherwise post on channel `hike_reminders`, created in `SentieroApp.onCreate` with importance DEFAULT:
    - small icon `hiking`
    - title "Tomorrow: <route>"
    - text from the plurals, or "Everything is packed." when 0
    - notification id `hikeId.hashCode()`
    - the tap opens `MainActivity` (decision 10) with `FLAG_IMMUTABLE`
  - Log tag `Sentiero/Reminder`.
- **Row** (upcoming mode only, after the checklist).
  - **Detail screen.**
    - Holds a permission launcher (`ActivityResultContracts.RequestPermission`).
    - Re-reads `areNotificationsEnabled()` in `LifecycleResumeEffect`.
    - Toggle on:
      - notifications enabled → `setReminder(true)`
      - API 33+ → launch the permission request
      - otherwise → `onNotificationsDenied()`
  - **ViewModel.** Combines the hike, `reminderAt(date, now)`, `notificationsEnabled` and `deniedThisVisit` through `reminderRow`.
  - **Row states (DR-2F):**
    - **Off:** "Remind me the evening before", with a switch.
    - **On:** "We'll remind you <shortDate(fireAt)>, around 18:00".
    - **Blocked:** "Notifications are off for Sentiero." + [Open settings], which opens `Settings.ACTION_APP_NOTIFICATION_SETTINGS` with `EXTRA_APP_PACKAGE`. The switch shows the stored value.
    - **TooLate:** disabled switch, "Too late for a reminder".

**Accept**
1. API 33+ emulator: toggling on shows the permission prompt. Allow → "We'll remind you Fri 13 Nov, around 18:00", and Background Task Inspector shows one job "reminder-<id>".
2. Deny → "Notifications are off for Sentiero." + Open settings. The switch stays off.
3. Set the emulator clock to 17:55 (automatic time off), create a hike dated tomorrow and turn the reminder on. Around 18:00, "Tomorrow: <route>" arrives with the number of unpacked items.
4. Editing the date reschedules (still one job). Delete and Mark as done leave no job, and no notification arrives.
5. With the clock at 19:00, a hike dated tomorrow (or any earlier date) shows "Too late for a reminder" with the switch disabled.
6. Revoking the permission after scheduling: no crash at 18:00, and one `Sentiero/Reminder` skip line.
7. API 26-32 emulator: no prompt. The toggle works directly.

**Tests**
- `ReminderTimeTest` +3: 18:00 the day before; 18:00 exactly → null; later → null.
- `ReminderRowTest` +2: TooLate wins; Blocked when denied.
- `RoomTripRepositoryTest` +3, with a fake scheduler that records calls:
  - on schedules; off cancels; too late writes nothing
  - a date edit reschedules; too late → `reminder_on = 0` + cancel
  - delete and Mark as done cancel

**Effort:** rules + scheduler 2 h · worker + channel + notification 2 h · row + permission flow 2 h · tests 1.5 h · device checks 0.5 h.

### 2.17 Final hardening, demo rehearsal, release (all · D1, D8, D9, T11, R3-20, R3-25, R3-26)
**Build / do**
- **Vocabulary (D1).** `grep -in "trip" app/src/main/res/values/strings.xml` prints nothing user-visible.
- **Accessibility (D8).**
  - A 10-minute TalkBack pass over every screen.
  - 200% font size with no clipped text.
  - Every target ≥ 48dp.
  - Focus order follows the visual order.
- **Rotation (D9)** at three points: the form while typing, the open sheet, and the forecast Content.
- **Logs (T11).** `Sentiero/Weather`, `Sentiero/Db` and `Sentiero/Reminder` lines are present for one failure each.
- **No catch-all.** `grep -rn "catch (e: Exception)" app/src/main` prints nothing.
- **Release.**
  - `README.md`: what the app does, how to run it, how to run the tests, the API used, and the scope boundary.
  - Tag `final-demo`. Attach the debug APK to a GitHub release of that tag.
  - Give the professor access to the private repo as a collaborator, unless the course says otherwise (R3-20).
- **Rehearse** the final demo script below, timed, including the Q&A defense notes.

**Accept**
1. All of the above checked off in the PR or commit description, with the commands and their results.

**Effort:** 5 h.

---

## Acceptance Criteria (epic)
1. Every child's **Accept** list passes on an emulator.
2. Spec 1's criteria 1-12 still pass (no degradation).
3. Each test in the Testing Plan states its card: `protects=…; fails_when=…; no_claim=…`.
4. The exported schema `1.json` under `app/schemas/` is unchanged (`git diff --exit-code app/schemas`). No migration was needed.

## Definition of Done
1. **The brief's four MVP bullets work on the emulator:**
   - create with date, route name, difficulty and a packing checklist
   - Upcoming and Completed on separate tabs
   - trips and checklist progress survive a force-stop
   - Open-Meteo with Loading, Empty, Error and Retry visible
2. **Every API state can be shown on demand with the recipes below:** Loading, Content, Empty, search Error, forecast Error, offline, Too far ahead, No area, and past date.
3. **The loop demo works end to end:** Chouf done with Headlamp added → new hike from Last hike → Headlamp first with "Forgot last time" → a forecast chip appends an item.
4. **Tests are green:**
   - `./gradlew test`: 52 new JVM tests plus Spec 1's 5.
   - `./gradlew connectedAndroidTest`: 17 new instrumented tests plus Spec 1's 2.
5. **The greps are clean:** no catch-all, no user-visible "trip".
6. **Quality passes are done:** TalkBack, 200% font, and rotation at three points.
7. **Tags exist:** `p1-complete` (the cut line), then `final-demo` with its APK. The professor has repo access.
8. **I can answer** "why does this value live here?" for every class added here (Implementation rules, rule 6).

## Testing Plan

| Layer | What | Count |
|---|---|---|
| Unit (JVM) | `SuggestionRulesTest` 7 · `ForecastCardPolicyTest` 6 · `WeatherRepositoryTest` 6 · `MarkDoneViewModelTest` 5 · `JournalRulesTest` 5 · `JsonFixturesTest` 4 · `AreaSearchStateTest` 4 · `HikeFormViewModelTest` 3 · `HikeDetailViewModelTest` 3 · `ReminderTimeTest` 3 · `PlaceTest` 2 · `DateLabelsTest` 2 · `ReminderRowTest` 2 | +52 |
| Instrumented (in-memory Room) | `RoomTripRepositoryTest` 13 (2.1 ×2, 2.6 ×3, 2.9 ×2, 2.10, 2.13, 2.14, 2.16 ×3) · `TripDaoTest` 4 (2.5 ×3, 2.7) | +17 |
| Manual E2E | full loop · states on demand · TalkBack 10 min · 200% font · rotation ×3 · reminder on API 33+ and API 26-32 | 6 scripts |

Cards:
- **SuggestionRulesTest:** protects=rule 19's table drives the chips exactly; fails_when=a threshold, a synonym or the cap of 3 changes; no_claim=real forecast accuracy (hand-made `DailyForecast` values).
- **ForecastCardPolicyTest:** protects=rule 18's local order, with no request outside 18.5; fails_when=a check moves or a boundary is off by one; no_claim=none beyond protects (fixed `today`).
- **WeatherRepositoryTest:** protects=no-match → Empty, the failure classes and rounding; fails_when=`results` becomes non-null, a catch is widened, or rounding changes; no_claim=the live API shape after 2026-11-01 (fixtures and fake APIs stand in for HTTP).
- **JsonFixturesTest:** protects=the production `Json` decodes real and edge bodies; fails_when=a DTO field turns non-null or `ignoreUnknownKeys` is dropped; no_claim=the forecast fixture is hand-made from the docs.
- **AreaSearchStateTest:** protects=only the latest query lands, and the 2-letter minimum holds; fails_when=the old job isn't cancelled; no_claim=real network latency (gated fake).
- **HikeFormViewModelTest:** protects=unpicked text saves no area, Last hike is preselected, and edit keeps the area; fails_when=typed text is saved as an area or the default flips; no_claim=Compose rendering (fake repositories).
- **HikeDetailViewModelTest:** protects=offline Error → Retry recovery, no-row copy, and no request in journal mode; fails_when=errors are swallowed or journal mode fetches; no_claim=the real network stack (fake weather repository).
- **MarkDoneViewModelTest:** protects=the sheet opens from saved data (rule 5), the dismiss gate, dedupe, removable rows and a failed save keeping the draft; fails_when=the draft leaks across opens or duplicates are added; no_claim=Room atomicity (covered by `RoomTripRepositoryTest`).
- **JournalRulesTest:** protects=same-route matching, the excerpt and search matching; fails_when=case or trim handling changes, or the wrong match wins; no_claim=none beyond protects.
- **PlaceTest / DateLabelsTest:** protects=the area labels and the English dates with the year rule; fails_when=a label or format changes; no_claim=none beyond protects.
- **ReminderTimeTest / ReminderRowTest:** protects=18:00 the day before, the too-late cut-off and the row states; fails_when=the time or the precedence changes; no_claim=WorkManager firing time (Doze may delay it, CEO-5).
- **RoomTripRepositoryTest:** protects=append and Undo, the all-or-nothing journal save, the copy order and fallback, edits leaving items alone, and every reminder side effect; fails_when=a write path skips a step or the scheduler; no_claim=a real WorkManager (fake scheduler) or real process death (in-memory database).
- **TripDaoTest:** protects=the Completed order and counts, Upcoming excluding completed hikes, and the cascade; fails_when=the SQL changes; no_claim=performance at scale.

## Final demo script (~8 minutes), states on demand, defense notes

**Prepare the day before.**
- Re-run the empty queries "zzqx" and "qxzvq" (R3-14).
- Create a completed "Chouf Cedars Loop" (Easy). In its sheet, add Headlamp as forgotten and write notes.
  - Mark it done **before** creating Jabal Moussa (R3-13: the loop copies at creation, so hikes planned earlier never receive later flags).
- Pick a hike date within 15 days and a mountain area. In December, "Jabal Moussa" (1,574 m) or "Faraya" (1,313 m) are likely to trigger Warm layer and Gloves (min ≤ 5 °C).
- If no chip fires on the day, show `SuggestionRulesTest` instead. Never fake the forecast.

**Script.**
1. **Scope (30 s).** Topic 8; the brief is the contract; the one API is Open-Meteo weather because no free trail API exists; navigation, tracking and safety guarantees are out of scope.
2. **Completed tab.** Chouf with its notes and "Forgot: Headlamp".
3. **New hike** "Jabal Moussa Reserve", Moderate, date within 15 days.
   - Area: type "Faraya" to show that you must pick (Guinea comes first), then clear it and pick "Jabal Moussa".
   - "Last hike: Chouf Cedars Loop" is preselected → Save.
4. **Detail.** Headlamp is first with the blaze tag and the arrival tint. The forecast loads, and a chip appends an item.
   - Tell the demo sentence: "I forgot my headlamp at Chouf…"
5. **States on demand:**
   - **Airplane mode → forecast Error.** "Couldn't load the forecast." + "You're offline." Reconnect, then Retry.
   - **Too far.** A hike with an area dated today + 16 → "Forecast available from …" (R3-15).
   - **No area.** A hike without an area → "Add an area to see the forecast."
   - **Past date.** A past-dated hike → no panel, Mark as done visible.
   - **Empty.** Search "zzqx".
   - **Loading.** Emulator Extended controls ▸ Cellular ▸ network type EDGE (R3-25).
6. **Mark Jabal Moussa as done** from the Upcoming row → Undo → done again from the detail screen. Explain the all-or-nothing transaction.
7. **Data flow of one forecast:**
   - `HikeDetailViewModel` gets the hike from Room's `Flow` and runs `forecastCard()` locally.
   - It calls `WeatherRepository.dailyForecast`; Retrofit decodes the DTO, the repository maps it to `DailyForecast`, and failures become `OFFLINE` or `OTHER`.
   - That becomes `ForecastPanelUi`, flows through the `StateFlow` and `collectAsStateWithLifecycle`, and ends as the panel.

**Defense notes (likely questions):**
- **Why decide the card locally?** Rule 18: no wasted calls, and every state can be reproduced.
- **Why no separate remote data source as in week 7?** Two GET calls: the Retrofit interface is the data source, and a pass-through class would add nothing. `WeatherRepository` maps and classifies.
- **Why does the reminder live in TripRepository?** It is the one owner of "a trip changed → the reminder follows" (ENG-R6).
- **Why a separate MarkDoneViewModel?** Two screens host the sheet, its draft must survive rotation, and Save is all-or-nothing.
- **Why round before the rules?** The numbers on the card are the numbers the rules use.
- **Known limitation (R3-13).** Forgotten items reach only hikes created from the last hike after it was marked done.

## Implementation rules

1. Acceptance criteria are the `protects / fails_when / no_claim` profile of each test: the observable it protects, the wrong result it rejects, and what green does not prove. Never "make the tests pass".
2. Real code and real tests land in the same change; a placeholder, a stub assertion or a test deferred to a follow-up is not done.
3. Any change to a test, validator, CI step, timeout, tolerance, threshold, snapshot or suppression pragma is reported on its own line in your summary with its justification, before any other result. (This spec already reports two: decision 12.)
4. A reported command names the command and its observed result (exit status, counts, the failing line); a command you did not run is not reported, and stderr is never silenced in a command you cite.
5. Your report is a claim until the parent re-executes it: say what you verified, what you reused, and what you could not check.
6. Course rule: I must be able to explain every file in my own words before the demo. Code I can't explain doesn't count.

## Rollback Plan

- **Commits.** One or more small commits per child on `main`; `main` builds at every commit.
- **Tags.** `p1-complete` after 2.7, `p2-complete` after 2.11, `final-demo` at the end. If a later child breaks the app close to the deadline, check out the last tag and rebuild its APK.
- **Database.** Schema v1 is unchanged, so no data rollback is ever needed. Any schema change would need a real `Migration` and a test (ENG-R2), and never a destructive fallback.
- **Reminder.** If 2.16 ships and must be removed, keep one line in `SentieroApp.onCreate`: `WorkManager.getInstance(this).cancelAllWorkByTag("reminder")`.

## Effort Estimate

| Tier | Children | Effort |
|---|---|---|
| P1 cut line | 2.1 4 h · 2.2 5 h · 2.3 5 h · 2.4 6 h · 2.5 5 h · 2.6 7 h · 2.7 2 h | 34 h |
| P2 loop | 2.8 4 h · 2.9 6 h · 2.10 4 h · 2.11 2 h | 16 h |
| P3 polish | 2.12 1 h · 2.13 2 h · 2.14 4 h · 2.15 2 h · 2.16 8 h | 17 h |
| Release | 2.17 5 h | 5 h |
| **Total** | | **~72 h** (P1 alone ~34 h) |

Per-component splits are in each child.
- **Fallback if time runs short:** P1 + 2.17 ≈ 39 h still completes the brief.
- **Each P3 drop saves:** reminder 8 h → search 2 h → Edit hike 4 h → Edit journal 2 h → Move back 1 h.

## Files Reference

| File | Change |
|---|---|
| `gradle/libs.versions.toml`, `app/build.gradle.kts` | Retrofit + kotlinx converter (2.2), WorkManager (2.16), `unitTests.isReturnDefaultValues` (2.2) |
| `app/src/main/AndroidManifest.xml` | INTERNET (2.2), POST_NOTIFICATIONS (2.16) |
| `.../SentieroApp.kt` | notification channel (2.16) |
| `.../AppContainer.kt` | OkHttpClient, Json, 2 Retrofits, `weatherRepository` (2.2); clock into the repository (2.6); scheduler (2.16) |
| `.../data/TripRepository.kt` | interface (decision 12); `RoomTripRepository.kt` with every write path (2.1-2.16) |
| `.../data/WeatherRepository.kt`, `OpenMeteoWeatherRepository.kt` | interface + mapping + failure classes (2.2) |
| `.../data/remote/OpenMeteoApi.kt`, `Dtos.kt` | `GeocodingApi`, `ForecastApi`, DTOs (2.2) |
| `.../data/local/TripDao.kt`, `ChecklistDao.kt` | queries from Shared contracts (2.1, 2.4-2.7, 2.14, 2.16) |
| `.../domain/Models.kt` | `Area`, extended `Hike` and `PackingItem`, `CompletedSummary`, `ListSource`, `CreateResult`, journal types |
| `.../domain/Place.kt`, `ForecastCardPolicy.kt`, `SuggestionRules.kt`, `JournalRules.kt`, `DateLabels.kt`, `ReminderTime.kt` | pure rules |
| `.../reminder/ReminderScheduler.kt`, `ReminderWorker.kt`, `Notifications.kt` | 2.16 |
| `.../ui/nav/Routes.kt`, `SentieroNavHost.kt`, `SentieroBottomBar.kt` | `Completed`, route flags, tabs (2.5, 2.9) |
| `.../ui/components/AreaSearchState.kt`, `AreaSearchField.kt`, `MemoryTag.kt` | 2.3, 2.9 |
| `.../ui/upcoming/UpcomingScreen.kt`, `UpcomingViewModel.kt` | due rows, sheet host, Undo (2.5, 2.6) |
| `.../ui/completed/CompletedScreen.kt`, `CompletedViewModel.kt` | 2.5, 2.15 |
| `.../ui/form/HikeFormScreen.kt`, `HikeFormViewModel.kt` | area (2.3), list chips (2.9), edit mode (2.14) |
| `.../ui/detail/HikeDetailScreen.kt`, `HikeDetailViewModel.kt` | add/delete (2.1), forecast (2.4), journal mode (2.5), host (2.6), menu (2.7), chips (2.8), tag + arrival (2.9), note (2.11), reminder row (2.16) |
| `.../ui/detail/ForecastPanel.kt`, `ForecastFormat.kt`, `MarkDoneSheet.kt`, `MarkDoneViewModel.kt`, `ReminderRow.kt` | 2.4, 2.6, 2.10, 2.16 |
| `app/src/main/res/values/strings.xml` (+ `plurals`), `res/drawable/` (`hiking`, `menu_book`, `add`, `close`, `more_vert`, `search`, `notifications`) | copy table; DR-5A icons |
| `app/src/test/.../*Test.kt`, `fakes/`, `resources/*.json` | JVM tests and fixtures |
| `app/src/androidTest/.../RoomTripRepositoryTest.kt`, `TripDaoTest.kt` | instrumented tests |
| `README.md` | 2.17 |

## Out of Scope

- **Deferred or rejected earlier:**
  - photos (CEO-4)
  - exact-time alarms (CEO-5)
  - route-name suggestions (CEO-7)
  - a tablet layout, Arabic UI translation and RTL mirroring (DR-6A, DR-6C)
  - a "Start from basics" button on an empty list (DR-2B)
  - keeping unsaved sheet drafts (DR-2E)
- **Brief and premise limits:** an offline weather cache (approach B), trail-data APIs, device location, live navigation, emergency guidance, safety guarantees.
- **Not chosen in this spec:**
  - `ACCESS_NETWORK_STATE` or ConnectivityManager checks (answer 2)
  - rebuilding the list when difficulty is edited (answer 3)
  - deep links from the notification (decision 10)
  - giving forgotten items to hikes that were already planned (R3-13)
- **Testing tools not chosen:** Robolectric and MockWebServer (D13, D14).

## Related

- `docs/plan.md`:
  - CEO-1..7, especially the tiers and the P3 order
  - DR-1A..7B
  - ENG-R1..R14
  - tasks T1-T7, T10, T11, D1-D10, E4-E10
- `docs/designs/hiking-trip-planner.md`: rules 1-19; reviewer concerns R3-2..R3-32 cited above.
- `DESIGN.md` and `docs/design-preview/` (the due-row and sheet layout).
- Spec 1: `docs/specs/01-midterm-slice.md`.
