---
name: Sentiero Kerbagio
description: Trail-marker utility. Limestone ground, cedar ink, and one painted blaze that marks only what the app remembered for you.
colors:
  primary: "#24473A"
  on-primary: "#FFFFFF"
  primary-container: "#CFE3D8"
  on-primary-container: "#0B2A1F"
  inverse-primary: "#9DCFB6"
  secondary: "#55635B"
  on-secondary: "#FFFFFF"
  secondary-container: "#D8E2DB"
  on-secondary-container: "#131E18"
  tertiary: "#B5400B"
  on-tertiary: "#FFFFFF"
  tertiary-container: "#FFDBCC"
  on-tertiary-container: "#5A1B00"
  blaze: "#E8590C"
  background: "#F4F4F0"
  on-background: "#1A231F"
  surface: "#F4F4F0"
  on-surface: "#1A231F"
  surface-variant: "#DCE3DD"
  on-surface-variant: "#4F5A54"
  surface-dim: "#D5D6D1"
  surface-bright: "#F4F4F0"
  surface-container-lowest: "#FFFFFF"
  surface-container-low: "#EFEFEA"
  surface-container: "#E9EAE4"
  surface-container-high: "#E3E4DE"
  surface-container-highest: "#DDDED8"
  inverse-surface: "#2E3531"
  inverse-on-surface: "#EEF1EC"
  outline: "#747E78"
  outline-variant: "#C6CBC5"
  error: "#BA1A1A"
  on-error: "#FFFFFF"
  error-container: "#FFDAD6"
  on-error-container: "#410002"
  scrim: "#000000"
  primary-dark: "#9DCFB6"
  on-primary-dark: "#003825"
  primary-container-dark: "#1F4E3D"
  on-primary-container-dark: "#B9EBD1"
  inverse-primary-dark: "#24473A"
  secondary-dark: "#BCCABF"
  on-secondary-dark: "#27332C"
  secondary-container-dark: "#3D4A42"
  on-secondary-container-dark: "#D8E6DB"
  tertiary-dark: "#FFB693"
  on-tertiary-dark: "#561F00"
  tertiary-container-dark: "#7A3000"
  on-tertiary-container-dark: "#FFDBCC"
  blaze-dark: "#FF8A4C"
  background-dark: "#111714"
  on-background-dark: "#E2E6E1"
  surface-dark: "#111714"
  on-surface-dark: "#E2E6E1"
  surface-variant-dark: "#3F4943"
  on-surface-variant-dark: "#B9C3BC"
  surface-dim-dark: "#111714"
  surface-bright-dark: "#363D39"
  surface-container-lowest-dark: "#0C110F"
  surface-container-low-dark: "#171E1A"
  surface-container-dark: "#1B2320"
  surface-container-high-dark: "#252D29"
  surface-container-highest-dark: "#303834"
  inverse-surface-dark: "#E2E6E1"
  inverse-on-surface-dark: "#2E3531"
  outline-dark: "#87918B"
  outline-variant-dark: "#3F4943"
  error-dark: "#FFB4AB"
  on-error-dark: "#690005"
  error-container-dark: "#93000A"
  on-error-container-dark: "#FFDAD6"
typography:
  headline:
    fontFamily: Barlow Condensed
    fontWeight: 600
    fontSize: 28sp
    lineHeight: 34sp
  title:
    fontFamily: Barlow Condensed
    fontWeight: 600
    fontSize: 22sp
    lineHeight: 28sp
  title-small:
    fontFamily: Source Sans 3
    fontWeight: 600
    fontSize: 16sp
    lineHeight: 22sp
  body:
    fontFamily: Source Sans 3
    fontWeight: 400
    fontSize: 16sp
    lineHeight: 24sp
  body-small:
    fontFamily: Source Sans 3
    fontWeight: 400
    fontSize: 14sp
    lineHeight: 20sp
  label-large:
    fontFamily: Source Sans 3
    fontWeight: 600
    fontSize: 14sp
    lineHeight: 20sp
  label:
    fontFamily: Source Sans 3
    fontWeight: 600
    fontSize: 12sp
    lineHeight: 16sp
    letterSpacing: 0.02em
  numeric:
    fontFamily: Source Sans 3
    fontFeature: tnum
rounded:
  xs: 4dp
  sm: 6dp
  md: 8dp
  lg: 12dp
  xl: 20dp
spacing:
  xs: 4dp
  sm: 8dp
  md: 16dp
  lg: 24dp
  xl: 32dp
  2xl: 48dp
  touch-min: 48dp
  row-min: 56dp
components:
  button-primary:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.on-primary}"
    rounded: "{rounded.md}"
  fab:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.on-primary}"
    rounded: "{rounded.lg}"
  input:
    borderColor: "{colors.outline}"
    focusBorderColor: "{colors.primary}"
    rounded: "{rounded.sm}"
  list-row:
    dividerColor: "{colors.outline-variant}"
    minHeight: "{spacing.row-min}"
  forecast-panel:
    backgroundColor: "{colors.surface-container}"
    rounded: "{rounded.md}"
  suggestion-chip:
    borderColor: "{colors.outline}"
    textColor: "{colors.on-surface}"
    rounded: "{rounded.sm}"
  memory-tag:
    backgroundColor: "{colors.tertiary-container}"
    textColor: "{colors.on-tertiary-container}"
    dotColor: "{colors.blaze}"
    rounded: "{rounded.xs}"
  bottom-sheet:
    backgroundColor: "{colors.surface-container-low}"
    rounded: "{rounded.xl}"
  snackbar:
    backgroundColor: "{colors.inverse-surface}"
    textColor: "{colors.inverse-on-surface}"
    actionColor: "{colors.inverse-primary}"
  nav-bar:
    backgroundColor: "{colors.surface-container}"
    indicatorColor: "{colors.secondary-container}"
---

# Sentiero Kerbagio

## Overview

**Creative North Star:** Trail-marker utility. The app works like a field notebook: quiet neutrals for everything you do, and one painted blaze, like the waymarks painted on Lebanese limestone, for everything the app remembered for you.
**Memorable thing:** "It remembers what I forgot." Every choice below serves that sentence.
**Product context:** Android app (Kotlin, Jetpack Compose, Material 3) for hikers who plan day hikes, pack from a checklist and keep a trail journal. Category peers: hiking planners and packing-list apps, which usually lean on forest green, photos and maps. Course final project, graded on a demo and on explaining every layer.
**Mode per surface:**
- Upcoming list, trip form, checklist: Operate.
- Journal entries and notes: Read.
- Forecast panel: Operate (glanceable numbers).
**Reference sites:** none; the direction comes from design knowledge (research declined).
**Key characteristics:**
- Condensed, sign-like route names over calm body text.
- Rows and hairlines instead of a stack of cards.
- The only saturated color is the blaze, and it appears only where the app remembered something.
- Daylight-readable: light limestone by default, full dark scheme that follows the system.

## Colors

**Strategy:** Restrained. Neutrals and cedar ink do the work; color is rare and always means something.
**Light or dark:** light by default, because packing happens at home and the app gets checked in daylight at the trailhead. The dark scheme follows the system setting and keeps the same meanings.
**Dynamic color is off.** Material You would replace the palette with the wallpaper's colors and erase the blaze's meaning, so `dynamicColor = false`.

What each role means here:
- `primary` (deep cedar): the main action on a screen (Save, FAB, filled checkboxes, progress lines). One filled primary action per screen.
- `secondary-container`: selected states that are not actions (selected chip, selected difficulty segment, nav indicator).
- `tertiary` family = **the blaze**. Reserved for memory: the "Forgot last time" tag, the "You hiked this on ..." note, and the arrival moment when a list starts from the last hike. Nothing else uses it: not weather suggestions, not warnings, not decoration.
- `blaze` / `blaze-dark` is not a Material role. It is the solid fill for the small dot inside the memory tag and the arrival tint. In Compose it is one extra color picked by dark or light theme.
- `error` stays Material red and is only for real errors (failed save, invalid input). Error messages always carry text or an icon, so they never rely on red alone, and they never look like the blaze tag.
- Neutrals derive from cedar: every gray carries a little green so the screen reads as one material.
- Dark surfaces step up through the `surface-container-*` levels for hierarchy instead of inverting light-mode grays.

Contrast (WCAG AA, checked): body text 14.6:1 light / 14.4:1 dark; muted text 6.5:1 / 10.0:1; blaze text 5.1:1 / 10.7:1; text on the memory tag 10.2:1 / 7.2:1; outlines 3.8:1 / 5.6:1 against the surface.

## Typography

Two families, both SIL Open Font License, bundled in `res/font` so they render offline and on emulators without Google Play services:
- **Barlow Condensed** (Jeremy Tribby; a low-contrast grotesk inspired by public road signage): `headline` for screen titles and the trip detail title, `title` for route names in lists and app bars. Its narrow width fits long Lebanese route names ("Qadisha Valley: Qannoubine trail") on one line.
- **Source Sans 3** (Adobe): everything you read, type or tick. `title-small` for section heads, `body` for checklist labels, inputs and notes, `body-small` for dates and secondary lines, `label-large` for buttons, `label` for tags and chips.
- **Numbers** use Source Sans 3 with tabular figures (`fontFeatureSettings = "tnum"`), so "6 of 12", temperatures and dates don't jitter.
- Sentence case everywhere. No all-caps titles, no tracked-caps kickers.
- Neither family has Arabic letters; Arabic route names fall back to the system font (Noto) automatically. Test one Arabic name in the form.

Files to bundle (static TTFs from the Google Fonts family downloads; keep each family's `OFL.txt` in the repo): `barlow_condensed_medium.ttf` (500), `barlow_condensed_semibold.ttf` (600), `source_sans_3_regular.ttf` (400), `source_sans_3_medium.ttf` (500), `source_sans_3_semibold.ttf` (600).

Material 3 mapping (Compose `Typography`):

| M3 style | Token |
|---|---|
| displaySmall, headlineLarge, headlineMedium, headlineSmall | `headline` (headlineSmall at 24sp/30sp) |
| titleLarge | `title` |
| titleMedium, titleSmall | `title-small` (titleSmall at 14sp/20sp) |
| bodyLarge | `body` |
| bodyMedium, bodySmall | `body-small` (bodySmall at 12sp/16sp) |
| labelLarge | `label-large` |
| labelMedium, labelSmall | `label` (labelSmall at 11sp/16sp) |

## Layout

- Phone portrait first, single column. Content keeps its state on rotation; no tablet layout in this project.
- Screen margins `md`. Vertical rhythm on the 8dp grid: `sm` inside a row, `md` between groups, `lg` before a new section.
- Lists are full-width rows separated by `outline-variant` hairlines, at least `row-min` tall (72dp when two lines). Packing progress is a 3dp line under the row's text, not a ring or a badge.
- One tonal panel per screen at most (the forecast). Never a card inside a card.
- Every touch target is at least `touch-min`, including the ✕ delete icon and checkboxes.

## Elevation & Depth

Depth comes from Material 3 tonal surfaces (`surface-container-*` levels), not shadows. Only the FAB and the bottom sheet use elevation, with Material's default offset shadows. No glows, no zero-offset halos.

## Shapes

Squared-off, like signage. Compose `Shapes`: extraSmall = `xs` (tags, checkboxes), small = `sm` (chips, text fields), medium = `md` (forecast panel, buttons), large = `lg` (FAB), extraLarge = `xl` (bottom-sheet top corners). Buttons override Material's default stadium shape to `md`. Inner radius = outer radius − gap when nesting.

## Components

- **Primary button / FAB:** filled `primary`. Pressed and focus use Material's ripple and state layers; disabled uses Material's 38% content opacity.
- **List row (trip):** route name in `title`, difficulty as an outlined `label` tag, date in `body-small`, 3dp progress line. The whole row is the tap target.
- **Checklist row:** Checkbox, label in `body`, ✕ delete at the end. Packed labels switch to `on-surface-variant`; no strike-through, so packed items stay easy to read.
- **Memory tag:** `tertiary-container` background, `on-tertiary-container` text, a 8dp `blaze` dot, text "Forgot last time". TalkBack reads it as part of the item: "Headlamp, not packed, forgot last time".
- **Forecast panel:** `surface-container`, `md` corners, place label in `label`, values in `body` with tabular figures, suggestion chips outlined and neutral, attribution line in `body-small` `on-surface-variant`.
- **Suggestion chip:** outlined, `sm` corners, leading "+", neutral colors. It never uses the blaze.
- **Text field:** outlined, `sm` corners, 2dp `primary` border on focus, helper text in `body-small`.
- **Bottom sheet (Mark as done):** `surface-container-low`, `xl` top corners, drag handle, title in `title`.
- **Snackbar:** `inverse-surface` with the Undo action in `inverse-primary`.
- **Navigation bar:** two destinations, `surface-container`, `secondary-container` indicator.

## Do's and Don'ts

- Do keep the blaze for memory only; if a new feature wants attention, use weight, an icon or a neutral container.
- Do show one filled primary action per screen.
- Do use tabular figures for every number that can change.
- Do design the empty, loading and error state of each list and panel with the same tokens.
- Do keep `dynamicColor = false`.
- Don't put trips or checklist items in cards; use rows and hairlines.
- Don't use green as a "nature" decoration; cedar is ink, not a theme color splash.
- Don't add photos, maps, mountain illustrations or emoji to fill space.
- Don't use the blaze or orange for warnings or errors.
- Don't round everything; keep the shape scale.

## Motion

- **Approach:** minimal-functional.
- **Easing:** enter(ease-out, Material EmphasizedDecelerate) exit(ease-in, EmphasizedAccelerate) move(ease-in-out, Standard).
- **Duration:** micro(50-100ms) short(150-250ms) medium(250-400ms) long(400-700ms).
- **The one authored moment:** when a new trip starts from the last hike, items forgotten last time arrive first with a `tertiary-container` row tint that fades out over about 900ms, leaving only the memory tag. Skipped when the system "Remove animations" setting is on.

## Decisions Log
| Date | Decision | Rationale |
|------|----------|-----------|
| 2026-11-01 | Initial design system created | Created from the approved design doc and the product review |
| 2026-11-01 | Blaze color reserved for memory | Serves "It remembers what I forgot"; the loop is visible at a glance |
| 2026-11-01 | Barlow Condensed + Source Sans 3, bundled | Signage-like titles fit long route names; offline rendering; both OFL |
| 2026-11-01 | Rows instead of cards; dynamic color off | Calmer, denser lists; color meaning survives on every phone |
