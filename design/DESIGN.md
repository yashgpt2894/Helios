# helios Android — design direction (v1, 2026-09-19)

Design target for the Android phone app. This document is input to the build step;
no `android/` source was changed while writing it. Paths are repository-relative.

## 1. Scope, authority, and evidence

Scope: the ten core features in the brief — first-run onboarding with inverter
connection, live dashboard (energy flow + tickers), production/inverter detail with
per-string telemetry, 7-day forecast, insights and savings, battery SoC and charge
strategy, settings (connection / location / theme / white-label brand), shareable
snapshot deep links, shared-snapshot viewer.

Authority for styling: the existing Compose design system at
`android/app/src/main/java/com/helios/core/designsystem/` (color, type, shape,
elevation, motion, haptics, sound) plus `shared-spec/design-tokens.json`. Both are
reused, not replaced. No second design system, no React Native or Expo.

Behaviour authority: `shared-spec/motion-language.md`,
`shared-spec/simulation-formulas.md`, `shared-spec/feature-parity-matrix.md` (referred
to below as FPM), and the PWA reference implementation in `src/`.

Evidence read for this direction: `README.md`;
`.factory/missions/helios-native-apps.md`; `shared-spec/design-tokens.json`;
`shared-spec/motion-language.md`; `shared-spec/screens/dashboard-dark.svg`;
`src/pages/*.tsx`; `src/store/useStore.ts`; `src/services/share.ts`;
`src/services/sunspec.ts`; `android/app/src/main/java/com/helios/core/nav/HeliosNavGraph.kt`;
`android/app/src/main/java/com/helios/feature/dashboard/DashboardScreen.kt`;
`android/app/src/main/java/com/helios/feature/settings/SettingsScreen.kt`;
`android/app/src/main/java/com/helios/feature/landing/LandingScreen.kt`;
`android/app/src/main/java/com/helios/MainActivity.kt`;
`android/app/src/main/AndroidManifest.xml`.

Facts that shaped every direction below:

- The product is a precision instrument, not a marketing surface. Copy is dry and
  unit-bearing ("kW", "kWh", "W/m2"); the visual identity is dark carbon, bone text,
  one solar accent, hairline separators, and Canvas geometry.
- The signature visual is the four-node `EnergyFlow` graph with wattage-modulated dot
  trails (`shared-spec/motion-language.md` section 4). It is the only animation the
  specification calls signature.
- The PWA's Dashboard hero already holds live kW, the flow diagram, and a three-cell
  ledger (`src/pages/Dashboard.tsx` lines 32-87); the approved M0 mockup
  `shared-spec/screens/dashboard-dark.svg` annotates the same order.
- The Android app today is an incomplete port: `HeliosNavGraph.kt` starts at
  `LANDING`, every screen reads telemetry once with
  `remember { TelemetryRepository.readTelemetry() }` (no ticking), `BottomNav` is
  defined but never composed, and Insights/Settings/Production/Battery are stubs
  (`feature/*/…Screen.kt` headers say "M4 stub"). `MainActivity.kt` requests
  `ACCESS_FINE_LOCATION` at launch. `SettingsScreen.kt` has a Share button whose
  `onClick` body is empty. The design below is the target these must be built to.

## 2. Who this is for, and the task that must win

Residential PV + battery owner. Phone in one hand, arm's length, often standing in
the kitchen or next to the inverter, sometimes in bright sun. No account, no cloud,
no third party in the loop.

Principal repeated task: **check live solar and battery state** — "is it producing,
am I storing it, is anything wrong". Two timings must both work:

- glance, 2-5 s, frequently, often resuming from the widget or a notification;
- study, 30-60 s, occasionally, when something looks off.

Secondary tasks: understand today's production and per-string health, understand
savings, change charge strategy, change theme or brand, share a snapshot.

Failure reality that the design must assume: the inverter sits on the home LAN, Wi-Fi
drops, a Modbus TCP server accepts one master, and a timeout is normal. The single
hard rule that follows: **a number is never presented as live without a freshness
statement.**

## 3. Fixed constraints shared by all three directions

- Spacing is a 4 pt grid (`design-tokens.json` `spacing.scale`), screen gutter 20 dp
  (`spacing.safeArea.horizontal`), section rhythm 20 dp.
- Type roles come from `typography.android.scale`: hero 56/300, title-1 34/400,
  title-2 28/400, title-3 22/400, headline 17/600, body 16/400, callout 14/400,
  subheadline 13/400, caption 11/400, caption-2 10/500 with 0.06 tracking.
- Radius md 12, lg 16, xl 22. Elevation level-1 (1 dp) for cards, level-3 (6 dp) for
  sheets and the bottom nav. Hairline separators at `neutral.950@6%`.
- Motion follows `shared-spec/motion-language.md`: spring presets only, staggered
  screen entrance (hero, stats, headers, charts, chrome), the hand-tuned page
  transition (no push-from-right), Reduce Motion and Reduce Transparency fallbacks.
- Android platform behaviour is native, not imitated: `TopAppBar` behaviour,
  `NavigationBar` destinations, `ModalBottomSheet`, `OutlinedTextField`,
  `SegmentedButton`, predictive back, edge-to-edge insets, Material ripple,
  haptics from `HeliosHaptics` (`design-tokens.json` `haptics.mapping`).
- No emoji anywhere in UI copy or icons. Icons come from one family with text labels.
- Non-goals stay out: no accounts, no cloud sync, no analytics or trackers, no iOS or
  Wear OS work in this pass, no store submission work.

## 4. Three directions for the key screen (Dashboard)

The three differ in hierarchy and interaction emphasis. They use the same tokens,
the same colour roles, and the same component set; only the organising idea, the
reading order, and the interaction model change.

### D1 — Live flow console (instrument-first)

Organising idea: the Dashboard is one live instrument; the answer to "is my system
working" is a picture of energy moving between four endpoints right now.

Hierarchy (top to bottom, with type roles):

1. `TopBar` — brand mark, freshness stamp, status pill (headline + caption-2).
2. Hero instrument panel (surface-raised, level-1): four-node `EnergyFlow` with the
   live kW as the hub readout in hero/56, the four node wattages in callout/14, and a
   three-cell ledger beneath a hairline (Today / Self-use / CO2 saved, title-3
   values, caption-2 labels).
3. Rank-1 insight (`InsightHighlight`).
4. Forecast strip, 5 days at the glance layer (caption-2 + micro-bars).
5. Production curve for today (`ProductionChart`, 160 dp).
6. Two-by-two `MetricTile` grid (production today, home usage, lifetime, trees).

Interaction emphasis: glance-first, tap-to-drill. The `EnergyFlow` nodes are the
primary navigation — Solar opens Production, Battery opens Battery, Grid opens the
savings block on Insights, Home opens the consumption block on Insights. Tapping the
freshness stamp or the status pill opens the Connection sheet. There is no primary
call-to-action button on the Dashboard; the screen is self-updating and read-only
except for share, drill-down, and connection recovery.

What it demands of the data layer: the existing 2 s telemetry tick, a freshness
timestamp, four node wattages, and the 48-point day series. Nothing new.

Strengths: fastest correct answer to the principal task; shows the product's actual
model at the glance layer; matches the approved M0 mockup and the signature
animation; stale and fault states can be expressed in place.

Weaknesses: chart and insight sit below the fold; a user who arrives with a decision
("can I run the dryer?") must read the insight card; bright-sun contrast has to be
engineered rather than assumed.

### D2 — Day ledger (answer-first)

Organising idea: the Dashboard answers one question at a time and states one
instruction; the numbers are a supporting ledger.

Hierarchy: `TopBar`; a status sentence in title-2 with a filled tonal action button
("Run heavy loads before 15:00 — Start reminder"); a four-up compact ledger row
(kW, battery %, grid, today kWh) in title-3; a dismissal-aware advisory queue
(rank-1 large, rest as one-line rows); a thin arrow-only flow ribbon; then forecast.
Charts move entirely to Production.

Interaction emphasis: act-and-dismiss. Advisories are snoozable (4 h / today),
the action button either performs or explains the recommendation, and a "why"
disclosure expands the evidence behind it. Monitoring is deliberately suppressed;
the screen wants a decision per visit.

What it demands of the data layer: a recommendation surface with confidence and
provenance, snooze persistence, and a rule that blocks advice when data is stale or
simulated. The current insight engine (`src/services/aiInsights.ts`,
`src/services/forecastInsights.ts`) is template-based and has neither confidence nor
provenance.

Strengths: strong for the decision visit; low visual density; one obvious reading
order and one obvious action, which is good for TalkBack and for large text.

Weaknesses: the glance now costs a sentence and a button before it reaches a number;
it drifts the identity from precision instrument toward coaching app, which the
brand and the approved spec do not support; advice rendered from stale or demo data
would be actively misleading, so it needs suppression rules that the data layer
cannot yet justify.

### D3 — Today timeline (chart-first)

Organising idea: time is the organising axis; the Dashboard is today's energy story
with a live marker on it.

Hierarchy: `TopBar`; a full-bleed, scrub-able 24 h production curve as the hero
(220 dp) with a live marker readout in title-1 using tabular lining figures; battery
and grid as companion traces beneath; the flow diagram demoted to Production;
forecast rendered as the continuation of the same axis (today + 7 days); insights
pinned to the curve as annotations.

Interaction emphasis: scrub to read any past point, compare today against yesterday,
understand shading and thermal effects. No tap is required to see the shape of the
day; a tap adds precision.

What it demands of the data layer: the 48-point series (exists), a yesterday overlay
(does not exist), canvas hit-testing, and per-point accessibility labels.

Strengths: best for the study visit and for diagnosing problems; distinctly not a
generic solar app; reuses the Production screen's chart investment.

Weaknesses: the 2-5 s glance now requires reading a marker on a chart, to the
detriment of the principal task; the largest element on a 412 dp-wide screen becomes
history rather than current state; scrubbing is a precision gesture that fails the
"standing in the sun, one hand" condition; merging 24 h history and 7-day forecast on
one axis raises reading load.

## 5. Evaluation

| Criterion | D1 flow console | D2 day ledger | D3 today timeline |
| --- | --- | --- | --- |
| Glance task (live solar + battery state in 2-5 s) | Pass — state is the first thing drawn | Partial — needs a sentence read before a number | Weak — needs chart reading |
| Study task (why is today odd) | Partial — one tap to the chart | Weak — advisories are not evidence | Pass — the chart is the screen |
| Decision support ("can I run loads now") | Partial — depends on the insight card | Pass — that is the screen's whole purpose | Weak |
| Brand and product model fit | Pass — shows energy moving between four endpoints | Weak — reads as a coaching app | Pass — analysis, but the flow model disappears |
| Native Android plausibility | Pass — Material nav/back/sheets, Canvas instrument | Pass but unusual for a monitoring tool | Pass |
| Accessibility | Pass with a text-equivalent for the canvas | Pass but a live sentence must be announced carefully | Partial — a scrub target is hard without sight |
| Density and content realism | Pass — every element has real data behind it today | Weak — needs confidence and provenance metadata that does not exist | Partial — needs a yesterday series that does not exist |
| Cohesion with the approved spec | Pass — matches `screens/dashboard-dark.svg` and motion section 4 | Weak — contradicts the approved mockup | Weak — moves the signature visual off the home screen |
| Implementation cost on the existing Compose code | Low — the existing `DashboardScreen` is already close | High — new insight surface plus snooze persistence | High — new series, hit-testing, annotations |

Cost is an input to sequencing, not the deciding criterion. The deciding criteria are
the two rows that come from the brief and the parity matrix: the principal repeated
task is a state check, and the product's differentiator is the energy model.

## 6. Selected direction: D1, with three corrections

Selected: **D1 live flow console.**

Reasons:

1. The brief's principal repeated task is checking live solar and battery state. D1
   answers it with one instrument and no prose. D2 inserts a sentence and a button
   before the number; D3 inserts a chart reading.
2. D1 is the only direction that presents the product's actual mental model — energy
   moving between solar, home, battery, and grid — at the glance layer. That model is
   what separates helios from utility-portal clones, and it is what the signature
   animation section of the motion language was written for.
3. The strengths of the rejected directions survive inside D1 without changing its
   hierarchy: D2's "one ranked advisory" is already the `InsightHighlight` directly
   under the hero, and D3's analysis belongs on Production, where the chart is
   already the hero (`feature/production/ProductionScreen.kt`, PWA
   `src/pages/Production.tsx`).
4. Continuity: D1 follows the approved M0 mockup and the existing `DashboardScreen`
   composition, so the build budget goes to the genuinely missing work — first-run
   onboarding, inverter connection and its failure states, and the stub screens —
   instead of re-cutting the key screen.

Corrections the build step must honour, because plain D1 fails the failure case:

- **C1 Freshness is a first-class element.** The hero carries a freshness stamp
  ("Live · 2 s ago"). Past 5 s it reads "Updated 14:32" in secondary text; when the
  link is down it becomes the alert row described in `design/ux-flows.md` FLW-03. No
  live number renders without a freshness statement. A simulated system says
  "Demo system" and never "Live".
- **C2 Connection is one tap from the hero.** Tapping the freshness stamp or the
  status pill opens the Connection sheet (state, last error, attempt count, Retry,
  Edit settings, Use demo system). A user whose system is down is not sent hunting
  through Settings.
- **C3 Insights are suppressed when data cannot support them.** When telemetry is
  stale or the link is down, the insight block is replaced by a quiet "Waiting for
  the inverter" placeholder; the engine is never run against stale or demo data to
  fill space. When the demo system is active, every insight carries a "Demo data"
  qualifier.

Selected-direction details the build must reuse:

- **Type roles.** Hero 56/300 for live kW only; title-2 28/400 for screen titles;
  title-3 22/400 for metric values with tabular lining figures; callout 14/400 for
  node wattages; caption-2 10/500 with 0.06 tracking for uppercase labels and units.
  Units always render as separate static text so a numeric ticker cannot shift
  layout (motion language section 6).
- **Colour roles.** Solar for production figures; Flow for home load and
  self-consumption; Battery hue for SoC; `grid.import` orange for import and
  `grid.export` blue for export; Alert only for faults, connection failures, and
  destructive confirmations. The white-label accent applies through the
  `brand` slot; it never replaces the solar/flow/grid/battery semantic roles.
- **Spacing rhythm.** 20 dp gutter; 16 dp inside cards; 20 dp between sections; the
  hero block (TopBar through ledger) ends within 430 dp of the top inset on a
  412 x 915 dp viewport so the ledger is visible without scrolling.
- **Navigation.** `NavigationBar` with five destinations: Home, Solar, Insights,
  Battery, Settings (FPM section 1 and section 2 `BottomNav`). Navigation is not an
  action; share, retry, and test-connection are. Back follows predictive back; each
  tab keeps its own scroll position; the connection form warns before discarding
  edits.
- **First-run treatment inside D1.** Onboarding is framed as the instrument
  booting: the same type and colour roles, a Canvas demo flow animating on page 1,
  a real connection test on the connect step, and no modal permission wall. The
  instrument appears as soon as a data source exists — real inverter or demo.
- **Imagery.** No photography in the app. The only imagery is Canvas geometry,
  `HeliosMark`, and one weather icon family with text labels. No decorative
  gradients beyond the existing aurora backdrop token, no card stacks, no emoji.

## 7. Token to Compose mapping the build must keep

| Token group | Compose site | Rule |
| --- | --- | --- |
| `color.hues.*` | `core/designsystem/color/HeliosColor.kt` | Extend the existing ramps; do not add a second palette. Light and dark steps already exist for carbon/paper. |
| `color.semantic.*` | `core/ui/theme/HeliosTheme.kt` | Map to `ColorScheme` roles as today. `background.primary` is neutral.50 in both themes (carbon in dark, bone in light). |
| `typography.android.scale` | `core/designsystem/type/HeliosTypography.kt` | One token per style; no ad-hoc `fontSize` in screens. |
| `spacing.scale` | Layout paddings | 4 pt multiples only; gutter 20 dp. |
| `radius` | `core/designsystem/shape/HeliosShape.kt` | xs/sm/md/lg/xl as defined. |
| `elevation.level-*` | `core/designsystem/shape/HeliosElevation.kt` | Cards level-1, sheets and nav level-3. |
| `motion.springPresets` | `core/designsystem/motion/HeliosMotion.kt` | gentle/default/snappy/bouncy; no linear easing except indeterminate loaders and the shimmer. |
| `haptics.mapping` | `core/designsystem/haptics/HeliosHaptics.kt` | tileTap light, primaryCTA medium, shareCopy success, connectionFailure error, battery thresholds 80/50/20. |
| `material.android` | Surfaces | Tonal elevation as specified, hairline inner stroke at 0.5 dp. |

Theme: the app supports Carbon (dark), Paper (light), and Auto. Today
`MainActivity.kt` hard-codes `HeliosTheme(darkTheme = true)` and every screen hard-codes
`isDark = true`; the build step must resolve the theme from
`core/data/repository/ThemeRepository.kt` before first frame, with no flash of the
wrong theme.

## 8. Component contracts

Existing components to keep and complete (each needs the same contract fields —
purpose, props, variants, states, accessibility — in the build step):

| Component | Purpose | Variants and states | Accessibility |
| --- | --- | --- | --- |
| `EnergyFlow` | Four-node live energy model | live, night (no solar dots), stale (trails stop, nodes dimmed), reduced motion (static arrows) | Text equivalent summarising all four paths; each node is a 48 dp tap target with its own label and value |
| `BatteryRing` | SoC hero | charging (glow), discharging (pulse), idle, threshold crossings | Label "Battery 62 percent, charging 1.1 kW"; not colour-only |
| `LiveNumber` | Value ticker | updating, static, stale (no ticker) | Stable accessible name; announce on status change or on a throttle, never every 2 s |
| `MetricTile` | Labelled metric | default, with delta positive/negative, subtle, disabled | Label, value, unit read as one node |
| `StatusPill` | Inverter status | producing, standby, curtailed, night, fault, offline, simulated | Word plus dot; never colour alone |
| `InsightHighlight` / `InsightCard` | Advisory | positive, neutral, attention, critical; with and without action | Severity in words; action is a real button |
| `ForecastStrip` / `ForecastCard` | 7-day forecast | ready, loading skeleton, error, stale | Day, condition text, expected kWh |
| `ProductionChart` | Today's curve | ready, empty before sunrise, loading, scrub | Per-point values via scrub and a summary sentence |
| `TopBar` | Identity + state | 3 slots: mark, freshness, status | Freshness and status are one live region |
| `BottomNav` | Five destinations | selected, unselected, badge for attention | Destination label always present |
| `SectionHeader`, `HeliosMark`, `WeatherIcon` | Structure and identity | as specified | Decorative mark is excluded from semantics |

New components this design requires:

| Component | Purpose | Required states |
| --- | --- | --- |
| `FreshnessStamp` | Age of the displayed reading; opens the Connection sheet | live, aging, stale, offline, demo |
| `ConnectionBanner` | Persistent failure notice on Dashboard and Production | offline with reason, reconnecting, recovered (auto-dismiss after 4 s) |
| `ConnectionSheet` | Inspect and fix the link | idle, testing, success with inverter identity, timeout, refused, wrong unit id, not SunSpec, multiple masters, no network |
| `ChargeModeOption` | Charge strategy selector row | selected, unselected, applying, confirmed, failed with revert |
| `SetupField` | Onboarding and connection text input | default, focused, invalid (message tied to the field), valid |
| `StepProgress` | Onboarding position | 4 steps, current, completed |
| `SnapshotSummaryRow` | Shared viewer metric line | has value, missing optional value |

## 9. Accessibility decisions

- TalkBack: each live figure is one node with label + value + unit. Telemetry
  announcements are throttled to status changes and a 60 s boundary, not to the 2 s
  poll. The freshness and status group is a polite live region.
- Canvas content has a text equivalent. `EnergyFlow` exposes a semantics block such
  as "Solar 4.23 kW to Home. Battery charging 1.11 kW. Grid 0 W." The production
  curve exposes a summary plus per-point values on scrub.
- Touch targets: 48 dp minimum for the share control, status pill, theme options,
  charge modes, and flow nodes. The share control in the PWA is 28 pt (a smell that
  must not be ported).
- Text scaling to 200 percent: the hero number caps at 44 sp and moves to its own
  line; `MetricTile` grid collapses to one column at fontScale 1.6 or above; tab
  labels stay visible; no fixed-height container may clip a value.
- Contrast: measure solar.500 (`#B88A2E`) against neutral.950 (`#070708`) before
  using it for text; use solar.700 and brighter for text on carbon. Status never
  relies on hue; every alert row carries an icon and words.
- Reduce Motion and Reduce Transparency follow motion language sections 11 and 12,
  including the EnergyFlow static-arrow fallback and the instant ticker.
- The app requests location only from the "Use my location" action, explains the
  purpose in the same view, and keeps a fully usable denied path with the default
  location. The launch-time request in `MainActivity.kt` must be removed.
- `strings.xml` already holds status, unit, section, and error strings; the build
  step extends it rather than hard-coding new copy, and keeps the five language
  stubs from M6 consistent.

## 10. Rejected alternatives, risks, and open questions

Rejected: a card-stack Dashboard with one card per metric (scroll cost and no
hierarchy); a gauge-only hero (battery SoC is not the daytime question); an "energy
score" or gamified streak (no data basis, and it would fabricate a statistic); a
photographic hero (the product has no imagery story and it fights the instrument
identity); reusing PWA web layout rules verbatim (they conflict with Material
navigation, back, and inset behaviour).

Risks and open questions for the build step:

1. The hero header row holds brand mark, freshness stamp, share, and status pill. At
   fontScale 1.6 and 412 dp width this is tight; if it wraps, the share control moves
   to an overflow on the hero, and that change needs a re-review.
2. The 5-day glance strip is chosen for legibility at caption-2 on 412 dp; whether
   the full 7 days fits without truncation must be measured on device, not assumed.
3. `EnergyFlow` particle trails must stop (not merely slow) when the reading is stale,
   otherwise the app animates a lie. This is a design requirement on the component.
4. Android App Links for `https://helios.app/share/...` are declared with
   `autoVerify` in `AndroidManifest.xml`, but the repository ships no
   `.well-known/assetlinks.json` (`public/` holds no `.well-known`), so verified link
   handling will fall back to the chooser. Deep-link behaviour must be verified on
   device with the custom scheme `helios://share/...`, and the verified-link gap must
   be recorded, not hidden.
5. Widgets, the Quick Settings tile, and the Wear OS module exist in `android/` and
   in FPM section 6 but are outside this brief's feature list; their design is
   deferred and they must not be presented as designed work.
