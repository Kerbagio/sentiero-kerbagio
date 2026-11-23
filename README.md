# Sentiero Kerbagio

A hiking trip planner and trail journal for Android ("sentiero" is Italian for trail).
Android Development final project, topic 8.

**For:** people who plan and pack for their own day hikes.

## What works now

The demonstrated feature: **create a hike, complete its packing items, and see the saved trip detail.**

- **New hike:** route name, a date from the Material 3 calendar, and a difficulty (Easy / Moderate / Hard). Save stays disabled until all three are set.
- **Packing checklist:** each hike starts with 10, 12 or 14 items depending on its difficulty. Every tick is saved immediately, and "Packing · 3 of 12" shows the progress.
- **Upcoming:** hikes sorted by date, each with its packing progress. Tap one to open it.
- **Saved on the phone:** hikes and ticks are stored in a Room (SQLite) database, so they survive closing or force-stopping the app. No internet is needed.
- Since the midterm: you can add your own items, and delete items with Undo.

**Out of scope:** live navigation, location tracking, emergency guidance and safety guarantees.

## How to run it

**You need:** a recent Android Studio (it includes the JDK), and an Android phone or emulator running Android 8.0 (API 26) or newer.

1. Clone the repository:
   ```
   git clone https://github.com/Kerbagio/sentiero-kerbagio.git
   ```
2. In Android Studio: **File → Open**, then pick the `sentiero-kerbagio` folder. Wait for the Gradle sync to finish.
3. Choose a device (a phone with USB debugging, or an emulator) and press **Run ▶**.

From the command line instead:
```
./gradlew installDebug        # Windows: gradlew.bat installDebug
```

**To try the feature:** tap **New hike**, enter a route name, pick a date and a difficulty, then tap **Save hike**. Tick a few items, close the app completely, and open it again: the ticks are still there.

## Tests

```
./gradlew test                  # JVM unit tests: TemplatesTest, HikeDetailViewModelTest
./gradlew connectedAndroidTest  # on a device: TripDaoTest, RoomTripRepositoryTest
```

## How it's built

Kotlin, Jetpack Compose, Material 3, Navigation Compose (type-safe routes), ViewModel + StateFlow, and Room.

```
Screens (Compose) → ViewModel (StateFlow) → Repository → DAO (Room) → sentiero.db on the phone
```

| Folder | What's in it |
|---|---|
| `app/src/main/java/com/anthony/sentiero/ui/` | The three screens, their ViewModels, and navigation |
| `app/src/main/java/com/anthony/sentiero/data/` | The repository, plus the Room database, entities and DAOs in `local/` |
| `app/src/main/java/com/anthony/sentiero/domain/` | Plain models and the starting packing lists (`Templates.kt`) |
| `docs/` | Design document, specs and plan |

## Next steps toward the final

- Weather from Open-Meteo for the hike date, which suggests items to pack
- A Completed tab, and Mark as done with a trail journal
