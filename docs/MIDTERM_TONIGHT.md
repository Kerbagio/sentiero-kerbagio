# Midterm tonight: the bare version of Spec 1

Read this first. It overrides `docs/specs/01-midterm-slice.md` for the midterm only. Everything removed here comes back in week 2 (see `WEEKLY_PLAN.md`).

## Goal
One demoable flow: **create a hike → tick packing items → saved detail screen**, and the ticks survive `adb shell am force-stop com.anthony.sentiero` plus a reopen.

## Keep (from Spec 1)
- **App name:** full name **Sentiero Kerbagio** ("sentiero" is Italian for trail). Under the launcher icon: `app_name` = "Sentiero" (the full name would be cut off). Inside the app, the Upcoming top bar shows a small overline "Sentiero Kerbagio" (`label` style, `on-surface-variant`) above the "Upcoming" title.
- **Package:** `com.anthony.sentiero`, minSdk 26, Kotlin + Jetpack Compose, Material 3.
- **Room:** `TripEntity` and `ChecklistItemEntity` with **all v1 columns exactly as in Spec 1**, even the unused ones (notes, area, completed_at, reminder_on, forgotten, carried_over, added_after_hike). This avoids a migration later. Also keep `Converters`, `TripDao`, `ChecklistDao` and `SentieroDatabase`, with schema export on.
- **Templates:** `templateFor(difficulty)`, the same items as Spec 1 (Easy 10, Moderate 12, Hard 14).
- **Repository:** `TripRepository` with `createHike` (one transaction), `observeUpcoming`, `observeHike`, `observeItems` and `setPacked`. ViewModels never touch DAOs.
- **Wiring:** `AppContainer` + `SentieroApp` (manual DI), and ViewModel factories.
- **ViewModels and screens:** Upcoming, HikeForm and HikeDetail ViewModels with their 3 screens.
- **Navigation:** typed routes. Save opens the new hike's detail and pops the form.
- **Ticks:** each tick writes the absolute value to Room immediately (`setPacked(itemId, checked)`).

## Removed for tonight
- The custom theme and bundled fonts: use Android Studio's default Material 3 theme.
- All tests: the demo proves it live.
- strings.xml: text inline in Kotlin is fine.
- The 80-character cap and counter, the double-tap guard, and the save-error snackbar.
- The relative date labels ("in 13 days"). Rows show the date and "3 of 12 packed".
- If the aggregate Upcoming SQL query causes trouble, count packed items in Kotlin instead.

## Build order (run the app after every step)
1. Gradle: Room + KSP, Navigation Compose ≥ 2.8, kotlinx-serialization, lifecycle-viewmodel-compose, lifecycle-runtime-compose. Use versions that work together and sync once.
2. Room files, then the Application class + AppContainer.
3. Templates + repository.
4. HikeForm ViewModel + screen, then navigation to a placeholder detail screen.
5. HikeDetail ViewModel + screen (checklist with checkboxes).
6. Upcoming ViewModel + screen (list + FAB), as the start destination.
7. Demo check: create "Jabal Moussa Reserve", Sat 14 Nov 2026, Moderate → 12 items → tick 3 → force-stop → reopen → same 3 ticked.

## Rules while building
- I must be able to explain every file at the demo. Each file gets a 2–3 line comment at the top saying **why this code lives in this layer**, and keep code simple over clever.
- One step at a time; stop after each step so he can run it.
- Don't add features outside this list.
