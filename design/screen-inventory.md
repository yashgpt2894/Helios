# helios Android — screen inventory (v1)

Stable IDs for every route, screen, state, and visible action on the Android phone build, traced to `shared-spec/feature-parity-matrix.md` (cited as FPM). Companion documents: `design/DESIGN.md` (direction and rules), `design/ux-flows.md` (flows FLW-01 to FLW-06).

Statuses describe the current `android/` implementation as read for this step. They are evidence for the build step, not this step's work: this step changed no Android source.

## 1. How to read this inventory

| ID family | Meaning | Example |
| --- | --- | --- |
| REQ- | Requirement from the product brief | REQ-03 live dashboard with energy flow and tickers |
| RTE- | Route or modal surface | RTE-07 Dashboard; RTE-18 connection sheet (modal, not addressable) |
| SCR- | Screen or surface | SCR-13 Settings location, also opened in pick mode |
| STS- | State of a screen | STS-026 stale reading |
| ACT- | Visible action with trigger, feedback, state change, persistence, failure response, and accessibility semantics | ACT-031 share snapshot |
| CMP- | Reusable component | CMP-18 FreshnessStamp |
| SVC- | Service, type, or persistence concern | SVC-19 ConnectionRepository |
| FLW- | User flow | FLW-03 recovery |

Convention for the Failure response column: "not applicable" marks an action that is entirely local and cannot fail (navigation, expansion, a local selection). Non-applicability is stated rather than filled with an invented error path, and no state is listed for a screen where it cannot occur.

Two further conventions matter for the build step:

- A control that appears on screen must be listed here as an action with a real outcome. A control with no outcome is removed rather than listed. The one deliberate exception is documented where it appears: the metric tiles on SCR-07 are read-only by decision, not by omission.
- Accessibility semantics are part of the action definition, not a separate pass. Each row states the role and label the build must expose.

## 2. Requirements

| ID | Requirement | FPM trace | Screens | Flows |
| --- | --- | --- | --- | --- |
| REQ-01 | First-run onboarding with a usable demo path | FPM section 1 (/ Landing becomes the 4-screen onboarding plus a first-run gate) | SCR-01..SCR-06 | FLW-01 |
| REQ-02 | Inverter connection: configure, test, persist, recover | FPM section 3 (connection, updateConnection), section 4 (SunspecService.readTelemetry), section 5 (ConnectionConfig) | SCR-04, SCR-05, SCR-12, SCR-18 | FLW-01, FLW-03 |
| REQ-03 | Live dashboard with energy flow and tickers | FPM section 1 (DashboardScreen), section 2 (EnergyFlow, LiveNumber, MetricTile, StatusPill) | SCR-07 | FLW-02 |
| REQ-04 | Production and inverter detail with per-string telemetry | FPM section 1 (ProductionScreen), section 2 (ProductionChart, WeekChart, MetricTile) | SCR-08 | FLW-02 |
| REQ-05 | 7-day production forecast | FPM section 4 (weather.ts fetchForecast, mapWeatherCode), section 2 (ForecastCard, ForecastStrip) | SCR-09, SCR-07 | FLW-02, FLW-03 |
| REQ-06 | Insights and savings | FPM section 4 (aiInsights generateInsights, computeSavings, forecastInsights), section 2 (InsightCard, InsightHighlight) | SCR-09, SCR-07 | FLW-02 |
| REQ-07 | Battery state of charge and charge strategy | FPM section 1 (BatteryScreen, charge mode selector), section 2 (BatteryRing) | SCR-10, SCR-19 | FLW-06 |
| REQ-08 | Settings: connection, location, theme, white-label brand | FPM section 3 (theme, brand, connection, location), section 4 (lib/theme.ts, brand.ts) | SCR-11..SCR-16 | FLW-06 |
| REQ-09 | Shareable snapshot deep links | FPM section 4 (share.ts encode/build/buildUrl), section 5 (SnapshotPayload v1), section 6 (App Links plus custom scheme) | SCR-07, SCR-16, SCR-17 | FLW-05 |
| REQ-10 | Shared-snapshot viewer | FPM section 1 (/share/:payload SharedView and SharedScreen), section 4 (decodeSnapshot, resolveBrand) | SCR-17 | FLW-05 |
| REQ-11 | Honest state: freshness, stale, offline, and classified failures | FPM section 3 (status), section 4 (SunspecService); brief recovery requirement | SCR-07, SCR-08, SCR-18 | FLW-02, FLW-03 |
| REQ-12 | Local-first data with no accounts, no cloud sync, and no analytics | Brief non-goals; mission constraints in .factory/missions/helios-native-apps.md | SCR-03, SCR-11, SCR-16 | FLW-01, FLW-04 |

## 3. Routes

| Route ID | Surface name | Presentation | Addressable |
| --- | --- | --- | --- |
| RTE-01 | Start gate (route resolution) | Full screen, non-interactive | Internal only: resolved on launch, never targeted by a deep link |
| RTE-02 | Onboarding 1 — Welcome | Full screen, pager page 1 of 4 | Yes |
| RTE-03 | Onboarding 2 — Local-first promise | Full screen, pager page 2 of 4 | Yes |
| RTE-04 | Onboarding 3 — Connect the inverter | Full screen, pager page 3 of 4, scrollable with the keyboard open | Yes |
| RTE-05 | Onboarding 4 — Connection test result | Full screen or bottom sheet; in this design a full screen page so the failure options fit at large text | Yes |
| RTE-06 | Onboarding 5 — Location for the forecast | Full screen, pager page 4 of 4, skippable | Yes |
| RTE-07 | Dashboard (Home) | Tab destination 1 of 5, scrollable | Yes |
| RTE-08 | Production (Solar) | Tab destination 2 of 5, scrollable | Yes |
| RTE-09 | Insights and savings | Tab destination 3 of 5, scrollable | Yes |
| RTE-10 | Battery | Tab destination 4 of 5, scrollable | Yes |
| RTE-11 | Settings | Tab destination 5 of 5, scrollable | Yes |
| RTE-12 | Settings — Connection | Secondary screen with a top app bar and a back affordance | Yes |
| RTE-13 | Settings — Location (and place picker) | Secondary screen; also opened in pick mode from SCR-06 and SCR-09 | Yes |
| RTE-14 | Settings — Appearance | Secondary screen | Yes |
| RTE-15 | Settings — Brand (white label) | Secondary screen | Yes |
| RTE-16 | Settings — About and diagnostics | Secondary screen | Yes |
| RTE-17 | Shared snapshot viewer | Full screen, no tabs, leaf destination | Yes, by deep link |
| RTE-18 (modal, not addressable) | Connection sheet | ModalBottomSheet, reachable from SCR-07, SCR-08, SCR-10, and any banner | No, modal or dialog |
| RTE-19 (dialog, not addressable) | Charge-mode confirmation dialog | Material 3 AlertDialog | No, modal or dialog |

Addressable means a deep link or an in-app route can target it. RTE-01 is internal only, and the two modals (RTE-18, RTE-19) are opened by their parent screen rather than by a link.

Deep links that must resolve (FPM section 6): `https://helios.app/share/{payload}` and `helios://share/{payload}` to RTE-17; `helios://dashboard` to RTE-07; `helios://battery` to RTE-10. The manifest also declares `https://helios.app/` and a bare `helios://` to the former landing destination, which this design replaces with the start gate (RTE-01) and onboarding (RTE-02). Verified App Links cannot work yet: no `.well-known/assetlinks.json` ships in the repository, so the custom scheme is the testable path and the gap is recorded in DESIGN.md and ux-flows.md.

## 4. Screens, states, and actions

### SCR-01 — Start gate (route resolution)

- Route: RTE-01  
- Surface: Full screen, non-interactive  
- Purpose: Resolve first run versus returning use, apply the saved theme, and paint last-known telemetry before the first poll.  
- Android status now: Missing. HeliosNavGraph.kt line 36 starts at LANDING; theme is hard-coded in MainActivity.kt line 53.  
- FPM trace: FPM section 1 (route / becomes onboarding plus a first-run gate); FPM section 3 (theme)

**States**

| State ID | State | Applies when | Treatment |
| --- | --- | --- | --- |
| STS-001 | resolving-first-run | Cold start with no first_run_complete flag | Brand mark on the saved theme, no spinner beyond 600 ms |
| STS-002 | resolving-returning | Cold start with the flag set | Brand mark, then SCR-07 with last-known values and their age |

**Visible actions**: none. The gate is deliberately non-interactive.

**Notes**

- Not addressable. No visible action by design: a gate that asks the user something is a gate that has failed.

### SCR-02 — Onboarding 1 — Welcome

- Route: RTE-02  
- Surface: Full screen, pager page 1 of 4  
- Purpose: State what the app does in one sentence and offer the demo path immediately.  
- Android status now: Missing. Onboarding does not exist. `LandingScreen.kt` is a 44-line welcome stub with no actions, `HeliosNavGraph.kt` line 36 starts on it, and the marketing landing page lives only in the PWA (`src/pages/Landing.tsx`).  
- FPM trace: FPM section 1 (/ Landing becomes the 4-screen onboarding); FPM section 6 (Onboarding: 4-screen pager)

**States**

| State ID | State | Applies when | Treatment |
| --- | --- | --- | --- |
| STS-003 | default | Always | Brand mark, one sentence, step indicator 1 of 4, two actions |

**Visible actions**

| Action ID | Control | Trigger | Feedback | State change | Persistence | Failure response | Accessibility semantics | FPM trace |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| ACT-001 | Set up my system | Tap | Page advances with the gentle spring | Pager index 1 to 2 | none | not applicable | Button, label 'Set up my system', reached first in focus order | FPM section 1 |
| ACT-002 | Explore demo first | Tap | Demo chip appears in TopBar; live values animate in | Source = demo; navigates to SCR-07; first_run_complete set | DataStore: source = demo, first_run_complete = true | not applicable | Button, label 'Explore demo first, uses simulated data' | FPM section 3 (simulated connection status) |
| ACT-003 | System back | Back gesture or button | App closes | Pager index 0 stays | none | not applicable | Platform back | FPM section 6 (predictive back) |

### SCR-03 — Onboarding 2 — Local-first promise

- Route: RTE-03  
- Surface: Full screen, pager page 2 of 4  
- Purpose: Say plainly what stays on the device, what leaves it, and that there is no account.  
- Android status now: Missing.  
- FPM trace: FPM section 1 (onboarding); brief non-goals (no accounts, no cloud sync)

**States**

| State ID | State | Applies when | Treatment |
| --- | --- | --- | --- |
| STS-004 | default | Always | Three plain statements plus the continue action |

**Visible actions**

| Action ID | Control | Trigger | Feedback | State change | Persistence | Failure response | Accessibility semantics | FPM trace |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| ACT-004 | Continue | Tap | Page advances | Pager index 2 to 3 | none | not applicable | Button, label 'Continue' | FPM section 1 |
| ACT-005 | Back | Tap or back gesture | Page returns | Pager index 2 to 1 | none | not applicable | Platform back plus an in-app back control with label | FPM section 6 |

**Notes**

- The exact wording of what leaves the device belongs to the build step; the design requires that the coordinate send to Open-Meteo is named.

### SCR-04 — Onboarding 3 — Connect the inverter

- Route: RTE-04  
- Surface: Full screen, pager page 3 of 4, scrollable with the keyboard open  
- Purpose: Collect the minimum SunSpec Modbus parameters and prove the link before saving anything.  
- Android status now: Missing. No ConnectionRepository or DAO exists; ConnectionConfig is an unused Room entity (core/domain/model/ConnectionConfig.kt, referenced nowhere).  
- FPM trace: FPM section 4 (sunspec.ts readTelemetry); FPM section 3 (connection, updateConnection); FPM section 5 (ConnectionConfig)

**States**

| State ID | State | Applies when | Treatment |
| --- | --- | --- | --- |
| STS-005 | default | Always on entry | Fields prefilled with a typical LAN default (192.168.1.42:502, unit 1, poll 2 s) |
| STS-006 | invalid-input | A field fails local validation | Error text tied to the field, valid values preserved |
| STS-007 | testing | Test connection is running | Elapsed seconds, up to 6 s, controls disabled with the reason stated |

**Visible actions**

| Action ID | Control | Trigger | Feedback | State change | Persistence | Failure response | Accessibility semantics | FPM trace |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| ACT-006 | Protocol selector | Tap | Selected segment moves with the snappy spring | Protocol = sunspec-modbus-tcp | draft only, on device | not applicable | Segmented button; RS-485 option announced as disabled with the reason | FPM section 5 (protocol field) |
| ACT-007 | Host field | Type | Value echoed; whitespace trimmed on blur | host = value | onboarding draft | Invalid shape marks the field and blocks Test connection | Text field, label 'Inverter host or IP address', URL keyboard | FPM section 3 (connection.host) |
| ACT-008 | Port field | Type | Digits only | port = 1..65535 | onboarding draft | Out of range marks the field | Text field, label 'Modbus TCP port', numeric keyboard | FPM section 3 (connection.port) |
| ACT-009 | Unit id field | Type | Digits only | unitId = 1..247 | onboarding draft | Out of range marks the field | Text field, label 'Modbus unit identifier' | FPM section 3 (connection.unitId) |
| ACT-010 | Poll interval selector | Tap | Selected value shown as text | pollIntervalMs in {1000,2000,5000,10000} | onboarding draft | not applicable | Selector announced as 'Poll interval, 2 seconds' | FPM section 3 (connection.pollIntervalMs) |
| ACT-011 | Keyboard submit | imeAction Done | Same as Test connection | see Test connection | none | see Test connection | imeAction labelled 'Test connection' | FPM section 6 (keyboard submission) |
| ACT-012 | Test connection | Tap | STS-007 while probing, haptic medium on press | Writes the config only on success; navigates to SCR-05 | ConnectionConfig written after a successful probe | Each class F1-F6 shows its own message on SCR-05; nothing is persisted | Primary button, label 'Test connection', busy state announced | FPM section 4 (SunspecService.readTelemetry) |
| ACT-013 | Use demo system | Tap | Demo chip appears | Source = demo; SCR-07; first_run_complete set | source = demo | not applicable | Secondary button, label 'Use demo system, no inverter needed' | FPM section 3 (connection status simulated) |

**Notes**

- No device discovery (mDNS or scan) in v1: the design does not promise a scan it cannot deliver. Manual entry plus the demo path is the honest scope.

### SCR-05 — Onboarding 4 — Connection test result

- Route: RTE-05  
- Surface: Full screen or bottom sheet; in this design a full screen page so the failure options fit at large text  
- Purpose: Prove what was found on the device, or name the failure class and offer the way out.  
- Android status now: Missing.  
- FPM trace: FPM section 4 (SunspecService); FPM section 5 (SolarTelemetry identity fields)

**States**

| State ID | State | Applies when | Treatment |
| --- | --- | --- | --- |
| STS-008 | success | Probe succeeded | Inverter identity: manufacturer, model, firmware, string count, first reading |
| STS-009 | timeout | TCP connect exceeded 2 s | F3-class message with attempt count and the values used |
| STS-010 | refused | TCP connect rejected | Port-refused message and the port that was tried |
| STS-011 | wrong-unit-id | Modbus exception 2 | Unit id message |
| STS-012 | not-sunspec | No SunS magic word | Protocol message |
| STS-013 | socket-busy | Connect then reset, or empty reads | Multiple-master advisory |
| STS-014 | no-network | No active transport | Different-network message |

**Visible actions**

| Action ID | Control | Trigger | Feedback | State change | Persistence | Failure response | Accessibility semantics | FPM trace |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| ACT-014 | Go to dashboard | Tap, success state only | Dashboard entrance choreography | first_run_complete = true; SCR-07 | ConnectionConfig and first_run_complete | not applicable | Primary button | FPM section 1 (dashboard route) |
| ACT-015 | Try again | Tap, failure states | Busy state, then a new result | Re-runs the probe | none | A repeated failure updates the message with the new attempt count | Primary button on failure states | FPM section 4 |
| ACT-016 | Edit details | Tap, failure states | Returns to SCR-04 with values intact | SCR-04 focused on the field named by the failure class | draft kept | not applicable | Button | FPM section 3 (updateConnection) |
| ACT-017 | Use demo system | Tap, failure states | Demo chip | Source = demo; SCR-07 | source = demo | not applicable | Button | FPM section 3 |
| ACT-018 | Copy failure detail | Tap, failure states | Snackbar 'Diagnostic copied' | Clipboard holds class, host, port, unit id, attempt count, timestamp | none | If the clipboard is unavailable the text is shown for selection | Button described as 'Copy diagnostic detail' | FPM section 4 |

**Notes**

- Success must show data read from the device, not a guess: identity fields come from the probe, which is what makes it proof.

### SCR-06 — Onboarding 5 — Location for the forecast

- Route: RTE-06  
- Surface: Full screen, pager page 4 of 4, skippable  
- Purpose: Place the forecast, with the permission requested at the point of use and a usable denied path.  
- Android status now: Missing. MainActivity.kt lines 36-48 request ACCESS_FINE_LOCATION at launch, before any user action.  
- FPM trace: FPM section 3 (location, useMyLocation); FPM section 4 (weather.reverseGeocode)

**States**

| State ID | State | Applies when | Treatment |
| --- | --- | --- | --- |
| STS-015 | default-location | No location chosen | Default coordinates shown with the source named as default |
| STS-016 | granted | Permission granted | Resolved place name and coordinates |
| STS-017 | denied | Permission denied | Note that the default location is in use, with a settings action; no repeat prompt |
| STS-018 | geocode-failed | Coordinates obtained, lookup failed | Coordinates kept and shown; forecast still requested |
| STS-019 | resolving | Lookup in flight | Busy label 'detecting...' with controls disabled and the reason stated |

**Visible actions**

| Action ID | Control | Trigger | Feedback | State change | Persistence | Failure response | Accessibility semantics | FPM trace |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| ACT-019 | Use my location | Tap | System permission dialog, then busy label | Location from FusedLocationProvider, reverse-geocoded | Location record with source = device | Denial or failure keeps the default and shows which one is in use | Button, label 'Use my location' | FPM section 3 (useMyLocation) |
| ACT-020 | Choose a place by name | Tap | Opens SCR-13 in pick mode | Location from the picked result | Location record with source = manual | No results keeps the query and shows an empty state | Button | FPM section 4 (GeocodingService) |
| ACT-021 | Not now | Tap | Proceeds to the dashboard | first_run_complete = true | first_run_complete | not applicable | Button, label 'Not now, use the default location' | FPM section 3 (location source default) |
| ACT-022 | Retry lookup | Tap, geocode-failed state | Busy label | Re-runs reverse geocode | same coordinates | Repeats the failure note without a modal | Button | FPM section 4 |
| ACT-023 | Open system settings | Tap, denied state | Leaves the app | none | none | not applicable | Button described as opening Android settings | FPM section 6 (permission handling) |

**Notes**

- The forecast is optional enrichment; a denied permission never blocks the dashboard. This is the fix for the launch-time request in MainActivity.kt.

### SCR-07 — Dashboard (Home)

- Route: RTE-07  
- Surface: Tab destination 1 of 5, scrollable  
- Purpose: Answer the principal repeated task in one glance and be the drill-down hub for the other four destinations.  
- Android status now: Partial. DashboardScreen.kt (126 lines) builds a similar order but reads telemetry once, has no ticker, no share, no freshness, no banner, no scaffold, and no navigation.  
- FPM trace: FPM section 1 (DashboardScreen); FPM section 2 (EnergyFlow, LiveNumber, MetricTile, StatusPill, ShareButton, TopBar, BottomNav, InsightHighlight, ForecastStrip, ProductionChart)

**States**

| State ID | State | Applies when | Treatment |
| --- | --- | --- | --- |
| STS-020 | loading | Cold start with no cached read | Skeletons in the hero, ledger, and chart shapes |
| STS-021 | live-producing | Link up, status PRODUCING | Ticker running, trails animating, stamp 'Live' |
| STS-022 | night | Status NIGHT | No solar dots; copy states that the system is asleep, not broken |
| STS-023 | standby | Status STANDBY | Trails idle; copy names standby |
| STS-024 | curtailed | Status CURTAILED | Curtailment explained in one line, solar accent, no alert colour |
| STS-025 | fault | Status FAULT | Alert banner with the reason class and the recovery actions |
| STS-026 | stale | Last read older than 15 s | Values dimmed, absolute timestamp, banner, insights replaced |
| STS-027 | offline | Classified link failure | Last-known values labelled as such, trails stopped, retry offered |
| STS-028 | demo | Source is the simulated system | Demo chip, no 'Live' wording, insights qualified as demo data |
| STS-029 | forecast-loading | Forecast request in flight, nothing cached | Forecast strip shows a skeleton of five columns |
| STS-030 | forecast-error | Forecast request failed | Inline forecast error with retry; telemetry unaffected |
| STS-031 | insights-suppressed | Source stale, offline, or demo | Quiet placeholder 'Waiting for the inverter' instead of advisories |

**Visible actions**

| Action ID | Control | Trigger | Feedback | State change | Persistence | Failure response | Accessibility semantics | FPM trace |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| ACT-024 | Pull to refresh | Drag down past the top inset | Refresh indicator, then live values | Immediate poll and forecast refresh when older than 1 h | none | Failure surfaces through the banner, not a toast | Scroll container exposes 'Refresh' as the swipe action label | FPM section 3 (tick) |
| ACT-025 | Freshness stamp | Tap | Connection sheet rises | SCR-18 opens | none | not applicable | Button, label 'Updated 12 seconds ago. Open connection details' | FPM section 2 (StatusPill), section 3 (connection) |
| ACT-026 | Status pill | Tap | Same sheet | SCR-18 opens | none | not applicable | Button, label 'Producing. Open connection details' | FPM section 2 (StatusPill) |
| ACT-027 | Solar node | Tap the flow node | Shared-element morph | SCR-08 opens | tab state kept | not applicable | Button, label 'Solar, 4.23 kilowatts. Open production detail' | FPM section 2 (EnergyFlow) |
| ACT-028 | Battery node | Tap | Shared-element morph into the ring | SCR-10 opens | tab state kept | not applicable | Button, label 'Battery 62 percent, charging 1.11 kilowatts. Open battery detail' | FPM section 2 (EnergyFlow, BatteryRing) |
| ACT-029 | Grid node | Tap | Navigation | SCR-09 opens at the savings block | none | not applicable | Button, label 'Grid exporting 0.9 kilowatts. Open savings' | FPM section 2 (EnergyFlow) |
| ACT-030 | Home node | Tap | Navigation | SCR-09 opens at the consumption insight | none | not applicable | Button, label 'Home using 3.1 kilowatts. Open consumption insights' | FPM section 2 (EnergyFlow) |
| ACT-031 | Share snapshot | Tap the share control | System share sheet; success haptic; snackbar 'Snapshot link shared' | SnapshotPayload v1 built from the current reading | none (stateless link) | No share target: copy to clipboard and say so; clipboard failure: show the URL for manual copy | Icon button with a 48 dp target and label 'Share snapshot' | FPM section 2 (ShareButton), section 4 (share.ts) |
| ACT-032 | Featured insight action | Tap the action on the hero advisory | Navigation with the target marked | Action is only shown when it can be honoured; it navigates (for example 'Show peak window' opens SCR-08 with the peak band marked) | none | Hidden, never dead, when the source is demo, stale, or offline | Button whose label states the destination | FPM section 2 (InsightHighlight); PWA action button is a dead control at src/components/InsightCard.tsx lines 57-62 and must not be ported as-is |
| ACT-033 | Forecast day | Tap a day in the strip | Navigation | SCR-09 opens with that day's card focused | none | not applicable | Button, label 'Friday, 35 kilowatt hours expected' | FPM section 2 (ForecastStrip) |
| ACT-034 | Chart scrub | Drag across the production curve | Readout follows the finger with per-point values | Selection index only | none | not applicable | Drag exposes each point's time and value through TalkBack; a summary sentence precedes the chart | FPM section 2 (ProductionChart) |
| ACT-035 | Connection banner | Tap the banner body | Connection sheet rises | SCR-18 opens | none | not applicable | Live-region friendly alert row, tappable as one target | FPM section 3 (connection status) |
| ACT-036 | Connection banner retry | Tap 'Retry now' in the banner | Attempt counter updates in place | Immediate probe attempt | none | Banner updates with the new class and attempt count | Button inside the alert row | FPM section 3 (updateConnection status) |
| ACT-037 | Waiting-for-inverter action | Tap 'Check connection' in the suppressed-insights placeholder | Connection sheet rises | SCR-18 opens | none | not applicable | Button | FPM section 3 |

**Notes**

- Metric tiles are deliberately non-interactive in v1: the four drill-down paths already exist on the flow nodes, and adding four more targets to the glance layer costs calm without adding a task. This is a decision, not a missing handler.
- The chart range switch does not exist here; the week chart lives on SCR-08.

### SCR-08 — Production (Solar)

- Route: RTE-08  
- Surface: Tab destination 2 of 5, scrollable  
- Purpose: Prove array health: generating now, per-string telemetry, inverter grid, week comparison.  
- Android status now: Stub. ProductionScreen.kt is labelled an M4 stub.  
- FPM trace: FPM section 1 (ProductionScreen); FPM section 2 (ProductionChart, WeekChart, MetricTile); FPM section 4 (buildWeekSeries)

**States**

| State ID | State | Applies when | Treatment |
| --- | --- | --- | --- |
| STS-032 | live | Link up with production | Live kW, string cards with utilization bars |
| STS-033 | partial-string-data | A string returns 0 W or no voltage | That string marked 'no data' rather than drawn as a healthy zero |
| STS-034 | stale | Last read older than 15 s | Dimmed values, banner, absolute timestamp |
| STS-035 | offline | Link failure | Last-known values labelled, banner with retry |
| STS-036 | week-loading | Week series being built | Skeleton bars in the week chart shape |
| STS-037 | night | No production yet today | Flat curve with honest copy; not an error state |
| STS-038 | string-imbalance | Spread above 15 percent | Attention advisory with the measured spread and an inspection hint |

**Visible actions**

| Action ID | Control | Trigger | Feedback | State change | Persistence | Failure response | Accessibility semantics | FPM trace |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| ACT-038 | String card | Tap | Card expands with the gentle spring | Expanded state per string id | session only | not applicable | Expandable button, label 'String A, Roof South-East, 2.33 kilowatts, 78 percent utilization' | FPM section 2 (MetricTile), section 1 (per-string cards) |
| ACT-039 | Week chart bar | Tap a bar | Readout for produced and consumed that day | Selected day index | none | not applicable | Each bar exposes day, produced, consumed | FPM section 2 (WeekChart) |
| ACT-040 | Today chart scrub | Drag across the curve | Per-point readout | Selection index | none | not applicable | Per-point values available to TalkBack | FPM section 2 (ProductionChart) |
| ACT-041 | Stale banner actions | Tap retry or the sheet | See SCR-18 | See SCR-18 | See SCR-18 | See SCR-18 | Shared component, same semantics as SCR-07 | FPM section 3 |

**Notes**

- Inverter telemetry (DC voltage, AC frequency, heatsink, cabinet) is a read-only grid, not a set of actions.

### SCR-09 — Insights and savings

- Route: RTE-09  
- Surface: Tab destination 3 of 5, scrollable  
- Purpose: Explain today, place the 7-day forecast, and state savings with their basis.  
- Android status now: Stub. InsightsScreen.kt is labelled an M4 stub.  
- FPM trace: FPM section 1 (InsightsScreen); FPM section 2 (ForecastCard, InsightCard, InsightHighlight, MetricTile); FPM section 4 (InsightsEngine, computeSavings, generateForecastInsights)

**States**

| State ID | State | Applies when | Treatment |
| --- | --- | --- | --- |
| STS-039 | forecast-ready | Forecast loaded | Seven day rows with weather, expected kWh, and a bar |
| STS-040 | forecast-loading | Request in flight | Skeleton rows matching the row height |
| STS-041 | forecast-error | Request failed | Inline error with retry; insights from live telemetry still shown |
| STS-042 | no-advisories | Engine returned no items | Explanatory line stating the system is operating cleanly |
| STS-043 | savings-unavailable | No telemetry to price | Savings tiles show 'no data' rather than a zero amount |
| STS-044 | demo-qualifier | Source is the demo system | Every advisory carries a 'Demo data' qualifier |
| STS-045 | stale-or-offline | Last read older than 15 s or the link is down | Advisories suppressed, waiting placeholder, forecast still usable |

**Visible actions**

| Action ID | Control | Trigger | Feedback | State change | Persistence | Failure response | Accessibility semantics | FPM trace |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| ACT-042 | Use my location | Tap the location action in the section header | Permission dialog at the tap, then busy label | Location updated and the forecast refetched | Location record with source = device | Denied keeps the current location and explains it | Button, label 'Use my location for the forecast' | FPM section 3 (useMyLocation) |
| ACT-043 | Forecast retry | Tap in the forecast-error state | Skeleton rows return | Refetch | none | Error text updates with the reason class | Button | FPM section 4 (fetchForecast) |
| ACT-044 | Advisory action | Tap the action label on an insight card | Navigation to the surface that can honour it | Real destination state, for example SCR-10 with the Time-of-use mode focused for 'Schedule pre-charge' | none (a mode change needs the confirmation on SCR-19) | Hidden when the source cannot support it; never a no-op | Button whose label states the destination | FPM section 4 (InsightsEngine actionLabel) |
| ACT-045 | Advisory overflow | Long press an advisory | Menu with Copy text and Dismiss for today | Dismissal recorded for the current date | preference keyed by date (local) | If the preference write fails the advisory stays visible and no confirmation is shown | Menu button with label 'More actions for this advisory' | FPM section 2 (InsightCard) |
| ACT-046 | Copy advisory text | Tap in the overflow menu | Snackbar 'Copied' | Clipboard | none | Fallback selection of the text | Menu item | FPM section 2 (InsightCard) |
| ACT-047 | Savings disclosure | Tap 'How is this calculated?' | Expands in place | expanded = true for that block | session only | not applicable | Expandable button with the expanded state described | FPM section 4 (computeSavings rates) |
| ACT-048 | Show all advisories | Tap when more than five exist | List expands | expanded = true | session only | not applicable | Button | FPM section 2 (InsightCard list) |
| ACT-049 | Savings tile | Tap | Navigation to the savings disclosure | expanded block focused | none | not applicable | Button with a value plus unit label | FPM section 2 (MetricTile) |

**Notes**

- Savings rates are constants in the PWA (0.32 per kWh import, 0.08 export, aiInsights.ts lines 4-5). The design requires them to be visible and stated, not implied.

### SCR-10 — Battery

- Route: RTE-10  
- Surface: Tab destination 4 of 5, scrollable  
- Purpose: Show state of charge as a physical state, plus backup readiness and the charge strategy.  
- Android status now: Stub. BatteryScreen.kt is labelled an M4 stub; charge strategy has no implementation anywhere in android/.  
- FPM trace: FPM section 1 (BatteryScreen); FPM section 2 (BatteryRing, MetricTile); FPM section 3 (battery state)

**States**

| State ID | State | Applies when | Treatment |
| --- | --- | --- | --- |
| STS-046 | charging | batteryPowerW above 30 W | Ring glow with the pulse from motion language section 5 |
| STS-047 | discharging | batteryPowerW below -30 W | Ring opacity pulse |
| STS-048 | idle | Within the dead band | Static ring, no glow |
| STS-049 | low-soc | SoC at or below 20 percent | Attention styling with the heavy threshold haptic on entry only |
| STS-050 | full | SoC at 100 percent | Full state with export context when relevant |
| STS-051 | mode-applying | A mode change is in flight | Selected row shows applying, other rows disabled with the reason stated |
| STS-052 | mode-failed | The inverter rejected the change | Selection reverts to the reported mode with the failure named |
| STS-053 | readiness-unavailable | No home load figure to divide by | Readiness shows 'no data' rather than a fabricated hours figure |

**Visible actions**

| Action ID | Control | Trigger | Feedback | State change | Persistence | Failure response | Accessibility semantics | FPM trace |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| ACT-050 | Charge mode row | Tap a mode | Selection highlights, then the confirmation dialog | SCR-19 opens with the chosen mode | on confirmation only | If applying fails, the selection reverts and the failure is named | Radio-style selectable row with the description read out | FPM section 1 (charge mode selector), section 3 |
| ACT-051 | Confirm mode change | Tap Confirm in SCR-19 | Applying state, success haptic on confirmation | Mode written to the inverter configuration and to the local preference | local preference plus device state | STS-052 revert with the reason | Dialog confirm button, labelled with the mode | FPM section 1 (charge strategy) |
| ACT-052 | Cancel mode change | Tap Cancel or back | Dialog closes | No change | none | not applicable | Dialog cancel button | FPM section 1 |
| ACT-053 | Readiness disclosure | Tap 'How is this calculated?' | Expands in place | expanded = true | session only | not applicable | Expandable button | FPM section 2 (MetricTile) |
| ACT-054 | Stale banner actions | Tap retry or the sheet | See SCR-18 | See SCR-18 | See SCR-18 | See SCR-18 | Shared component | FPM section 3 |

**Notes**

- Ring thresholds fire on crossing only (80, 50, 20 percent), never on every poll, so the phone does not buzz inside a moving car.

### SCR-11 — Settings

- Route: RTE-11  
- Surface: Tab destination 5 of 5, scrollable  
- Purpose: One entry point per settings concern, with current values visible before entry.  
- Android status now: Stub. SettingsScreen.kt (118 lines) has a theme row, a brand row with four hard-coded ids, and a share button whose onClick body is empty (dead control).  
- FPM trace: FPM section 1 (SettingsScreen); FPM section 3 (theme, brand, connection, location)

**States**

| State ID | State | Applies when | Treatment |
| --- | --- | --- | --- |
| STS-054 | default | Always | Rows: connection, location, appearance, brand, about; each with its current value |
| STS-055 | demo-source | Source is the demo system | Connection row states 'Demo system' and offers setup |
| STS-056 | disconnected | Link down | Connection row states the failure class as its summary value |

**Visible actions**

| Action ID | Control | Trigger | Feedback | State change | Persistence | Failure response | Accessibility semantics | FPM trace |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| ACT-055 | Connection row | Tap | Navigation with the shared-element header | SCR-12 opens | none | not applicable | Row button, label 'Connection, SunSpec Modbus at 192.168.1.42' | FPM section 3 (connection) |
| ACT-056 | Location row | Tap | Navigation | SCR-13 opens | none | not applicable | Row button with the current place name | FPM section 3 (location) |
| ACT-057 | Appearance row | Tap | Navigation | SCR-14 opens | none | not applicable | Row button with the current theme name | FPM section 3 (theme, setTheme) |
| ACT-058 | Brand row | Tap | Navigation | SCR-15 opens | none | not applicable | Row button with the current brand name | FPM section 3 (brand), section 4 (brand.ts) |
| ACT-059 | About row | Tap | Navigation | SCR-16 opens | none | not applicable | Row button | FPM section 1 (settings surface) |
| ACT-060 | Data residency disclosure | Tap the info action on the row | Inline expansion | expanded = true | session only | not applicable | Expandable button, label 'About local-only data' | FPM section 1 (app preferences) |

**Notes**

- The PWA settings rows for Notifications, App lock, and About are not ported as-is: Notifications and App lock have no implementation in the brief's scope, and a visible control that does nothing is unacceptable. App lock is recorded as deferred in DESIGN.md and needs a real implementation (BiometricPrompt plus a lock state machine) if the owner wants it.

### SCR-12 — Settings — Connection

- Route: RTE-12  
- Surface: Secondary screen with a top app bar and a back affordance  
- Purpose: Inspect, test, and persist the inverter link without overwriting a working configuration by accident.  
- Android status now: Missing (no ConnectionRepository, no DAO).  
- FPM trace: FPM section 3 (connection, updateConnection); FPM section 6 (Data persistence: Room plus DataStore)

**States**

| State ID | State | Applies when | Treatment |
| --- | --- | --- | --- |
| STS-057 | saved | Matches the persisted config | Fields at saved values, Save disabled |
| STS-058 | dirty | At least one field differs | Save enabled, discard guard armed |
| STS-059 | validating | Test in flight | Busy state with elapsed seconds |
| STS-060 | validation-error | Local validation failed | Field errors, Save disabled |
| STS-061 | test-passed | Probe succeeded | Success row with inverter identity, then persist |
| STS-062 | test-failed | Probe failed by class F1 to F7 | Error row with the class, the saved working config kept and stated |
| STS-063 | save-failed | Persistence write failed | Error naming the storage failure; the in-memory config is not treated as saved |
| STS-064 | log-visible | Connection log expanded | Last 20 attempts with timestamps and classes |

**Visible actions**

| Action ID | Control | Trigger | Feedback | State change | Persistence | Failure response | Accessibility semantics | FPM trace |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| ACT-061 | Protocol selector | Tap | Segment moves | protocol field in the draft | on save | not applicable | Segmented button | FPM section 5 (ConnectionConfig) |
| ACT-062 | Host, port, unit id, poll fields | Type or select | Dirty marker appears | Draft fields | on save | Field-level validation | Labelled fields with numeric keyboards where appropriate | FPM section 3 (updateConnection) |
| ACT-063 | Save and test | Tap | Busy then result row with the gentle spring | On success: persisted config becomes the active source and polling restarts | ConnectionConfig written to Room; failure leaves the previous config active | STS-062 or STS-063 with the reason; nothing is overwritten | Primary button, busy state announced | FPM section 3, section 6 |
| ACT-064 | Discard changes | Tap, or accept the leave guard | Fields return to saved values | Draft cleared | none | not applicable | Button | FPM section 3 |
| ACT-065 | Use demo system | Tap | Source switch with the demo chip | Source = demo | source = demo | not applicable | Button | FPM section 3 (status simulated) |
| ACT-066 | Connection log | Tap the log action | Expands a list of attempts | expanded = true | log persisted locally, last 20 entries | not applicable | Expandable button; each entry read as time plus class | FPM section 3 |

**Notes**

- A stored configuration plus a changed form plus a failed test must resolve to the working configuration. The state names which config is live at all times.

### SCR-13 — Settings — Location (and place picker)

- Route: RTE-13  
- Surface: Secondary screen; also opened in pick mode from SCR-06 and SCR-09  
- Purpose: Set the forecast location three ways: device, place name, or explicit coordinates.  
- Android status now: Missing. LocationRepository.kt exists but is referenced nowhere.  
- FPM trace: FPM section 3 (location, useMyLocation); FPM section 4 (reverseGeocode, fetchForecast)

**States**

| State ID | State | Applies when | Treatment |
| --- | --- | --- | --- |
| STS-065 | default-location | Source is default | Default place shown with the source named |
| STS-066 | resolved | Source is device or manual | Place name and coordinates shown |
| STS-067 | resolving | Lookup in flight | Busy label |
| STS-068 | denied | Permission denied | Explanation and a settings action; no repeat prompt |
| STS-069 | search-empty | Query returned nothing | Empty state that keeps the query |
| STS-070 | search-error | Geocoder failed | Error with retry |

**Visible actions**

| Action ID | Control | Trigger | Feedback | State change | Persistence | Failure response | Accessibility semantics | FPM trace |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| ACT-067 | Use my location | Tap | Permission dialog at the tap, then busy | Location from the device, reverse-geocoded | Location record, source = device | Denied or failed keeps the previous location and says so | Button | FPM section 3 (useMyLocation) |
| ACT-068 | Place search field | Type two or more characters | Results list appears | Query state | none | Empty and error states as above | Search field with the results count announced | FPM section 4 (reverseGeocode, geocoder) |
| ACT-069 | Place result | Tap a result | Selection confirmed, forecast refetched | Location = result; forecast reload | Location record, source = manual | Forecast failure is reported in the forecast section, not as a location error | List item with name and region | FPM section 3 (location), section 4 (fetchForecast) |
| ACT-070 | Open system settings | Tap in the denied state | Leaves the app | none | none | not applicable | Button | FPM section 6 (permission handling) |
| ACT-071 | Retry lookup | Tap in the search-error state | Busy label | Re-run | none | Failure note repeats without a modal | Button | FPM section 4 |

### SCR-14 — Settings — Appearance

- Route: RTE-14  
- Surface: Secondary screen  
- Purpose: Choose Carbon, Paper, or Auto, and see the result immediately.  
- Android status now: Partial. ThemeRepository.kt persists the mode, but HeliosTheme is hard-coded to dark in MainActivity.kt line 53 and every screen hard-codes isDark = true.  
- FPM trace: FPM section 3 (theme, setTheme, cycleTheme); FPM section 4 (lib/theme.ts)

**States**

| State ID | State | Applies when | Treatment |
| --- | --- | --- | --- |
| STS-071 | dark | Theme = Carbon | Dark palette applied, option marked selected |
| STS-072 | light | Theme = Paper | Light palette applied |
| STS-073 | auto | Theme = Auto | Follows the system setting and reacts to a system change without a restart |
| STS-074 | large-text | System font scale at or above 1.6 | Options stack, preview text wraps, no truncation |

**Visible actions**

| Action ID | Control | Trigger | Feedback | State change | Persistence | Failure response | Accessibility semantics | FPM trace |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| ACT-072 | Carbon option | Tap | Colours change immediately with the normal duration cross-fade | theme = dark | DataStore | not applicable | Selectable option with a check mark and 'selected' state described | FPM section 3 (theme) |
| ACT-073 | Paper option | Tap | Colours change immediately | theme = light | DataStore | not applicable | Selectable option, selected state described | FPM section 3 (theme) |
| ACT-074 | Auto option | Tap | Follows the system immediately | theme = auto | DataStore | not applicable | Selectable option, described as following the system setting | FPM section 3 (theme) |
| ACT-075 | Text size note | Tap | Explains that Android controls text size | none; the app follows the system setting | none | not applicable | Read-only note, not a control | FPM section 6 (Dynamic Type equivalent) |

**Notes**

- Android dynamic colour stays off so the helios palette is not replaced by wallpaper colours (HeliosTheme already takes dynamicColor = false).

### SCR-15 — Settings — Brand (white label)

- Route: RTE-15  
- Surface: Secondary screen  
- Purpose: Switch between helios and the white-label brands, with a preview of what changes.  
- Android status now: Partial. BrandRepository.kt resolves brands; the Settings stub lists four ids ('helios', 'solaris', 'volt', 'aether') that differ from the PWA registry ('helios', 'voltcraft', 'sunworks', 'meridian'). This mismatch must be resolved in favour of the PWA registry.  
- FPM trace: FPM section 3 (brand, setBrandFromSearch); FPM section 4 (brand.ts resolveBrand, applyBrandAccent); FPM section 5 (Brand)

**States**

| State ID | State | Applies when | Treatment |
| --- | --- | --- | --- |
| STS-075 | helios-selected | Brand = helios | Default accent and mark |
| STS-076 | white-label-selected | Brand is a white label | Accent, mark, and footer text change |
| STS-077 | from-snapshot | Brand came from an opened snapshot payload | Brand shown with the source named and a reset action |

**Visible actions**

| Action ID | Control | Trigger | Feedback | State change | Persistence | Failure response | Accessibility semantics | FPM trace |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| ACT-076 | Brand option | Tap | Immediate accent, mark, and copy change with the gentle spring | brand = id | DataStore | not applicable | Selectable option with the brand name and a selected state | FPM section 4 (applyBrandAccent) |
| ACT-077 | Reset to helios | Tap, white-label state | Accent and copy return | brand = helios | DataStore | not applicable | Button | FPM section 4 (resolveBrand) |
| ACT-078 | What changes disclosure | Tap | Inline list of what the brand affects and what it does not | expanded = true | session only | not applicable | Expandable button | FPM section 5 (Brand fields) |

**Notes**

- The brand affects accent, mark, and copy only. It never changes the semantic colours (solar, flow, grid, battery, alert), which is what keeps a white-label build honest about the same physics.

### SCR-16 — Settings — About and diagnostics

- Route: RTE-16  
- Surface: Secondary screen  
- Purpose: State the build, the data policy, and the recovery actions that live nowhere else.  
- Android status now: Missing.  
- FPM trace: FPM section 1 (settings surface); FPM section 5 (SolarTelemetry identity); mission constraints (no secrets, no telemetry)

**States**

| State ID | State | Applies when | Treatment |
| --- | --- | --- | --- |
| STS-078 | default | Always | Version, source, protocol, privacy summary, licences, and the actions below |

**Visible actions**

| Action ID | Control | Trigger | Feedback | State change | Persistence | Failure response | Accessibility semantics | FPM trace |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| ACT-079 | Share a snapshot for support | Tap | Same share flow as the dashboard | SnapshotPayload v1 built | none | Copy fallback then manual selection | Button | FPM section 4 (share.ts) |
| ACT-080 | Run setup again | Tap | Confirmation dialog naming what will be replaced | Returns to SCR-02 with the connection kept until a new one succeeds | first_run_complete is only cleared after the confirmation | not applicable | Button with a destructive-styled confirmation | FPM section 1 (onboarding) |
| ACT-081 | Copy diagnostics | Tap | Snackbar 'Diagnostics copied' | Clipboard holds app version, source, last failure class, device model, Android version; nothing else | none | Fallback selection | Button described as copying local diagnostics only | FPM section 5; brief constraint: no trackers, no analytics |
| ACT-082 | Privacy details | Tap | Inline expansion naming every outbound request | expanded = true | session only | not applicable | Expandable button | FPM section 4 (weather.ts, Open-Meteo) |
| ACT-083 | Licences | Tap | Opens the licence list | Navigation or expansion | none | not applicable | Button | FPM section 2 (component provenance) |

### SCR-17 — Shared snapshot viewer

- Route: RTE-17  
- Surface: Full screen, no tabs, leaf destination  
- Purpose: Render a shared snapshot from a deep link, with no account and no network requirement for decoding.  
- Android status now: Partial. SharedScreen.kt decodes and lists five rows, but drops the timestamp formatting, the CO2 note, the white-label footer rule, the invalid-link copy, and both call-to-action buttons.  
- FPM trace: FPM section 1 (/share/:payload SharedView / SharedScreen); FPM section 4 (decodeSnapshot, resolveBrand); FPM section 5 (SnapshotPayload v1)

**States**

| State ID | State | Applies when | Treatment |
| --- | --- | --- | --- |
| STS-079 | valid | Payload decoded with v = 1 | Brand, timestamp, live kW, today, battery, self-use, lifetime, optional forecast |
| STS-080 | invalid-or-truncated | Decode failed or v is not 1 | 'Link expired or invalid' with one action; no stack trace, no blank screen |
| STS-081 | minimal-payload | No forecast array and no brand | Forecast block absent, layout closes up; default brand used |
| STS-082 | white-label-payload | Payload carries br | Brand name and accent applied; footer reads 'Powered by helios' |
| STS-083 | cold-start-by-link | App launched from the link | Viewer shown without onboarding; back exits the app |

**Visible actions**

| Action ID | Control | Trigger | Feedback | State change | Persistence | Failure response | Accessibility semantics | FPM trace |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| ACT-084 | Try helios | Tap | Existing installs open the dashboard; fresh installs start setup | SCR-07 or SCR-02 | none | not applicable | Primary button with the brand name in the label | FPM section 1 (shared viewer to app) |
| ACT-085 | Copy link | Tap | Snackbar 'Link copied' | Clipboard holds the URL | none | Show the URL for manual selection | Button | FPM section 4 (buildShareUrl) |
| ACT-086 | Open in browser | Tap | Leaves the app for the web viewer | none | none | If no browser handles the URL, the button is hidden | Button describing that it opens the web version | FPM section 4 (buildShareUrl) |
| ACT-087 | Back | Tap or gesture | Returns to the previous destination, or exits when cold-started | Navigation | none | not applicable | Platform back | FPM section 6 (deep links) |

**Notes**

- Decoding is offline by design: a snapshot is a self-contained value, and the deep link is the one path where the app must work with no network at all.
- Payload parity is a compatibility requirement, not a nicety: Android-produced payloads must open in the PWA viewer and vice versa (build-step test).

### SCR-18 — Connection sheet

- Route: RTE-18 (modal, not addressable)  
- Surface: ModalBottomSheet, reachable from SCR-07, SCR-08, SCR-10, and any banner  
- Purpose: One place to see why the link is down and to fix it, without leaving the screen being read.  
- Android status now: Missing.  
- FPM trace: FPM section 3 (connection, updateConnection, tick); FPM section 4 (SunspecService)

**States**

| State ID | State | Applies when | Treatment |
| --- | --- | --- | --- |
| STS-084 | idle | Sheet open, link healthy | Source, host, last good read, no error |
| STS-085 | reconnecting | Retry in flight | Attempt counter and elapsed time |
| STS-086 | recovered | A retry succeeded | Success row for 4 s, then the sheet stays open until dismissed |

**Visible actions**

| Action ID | Control | Trigger | Feedback | State change | Persistence | Failure response | Accessibility semantics | FPM trace |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| ACT-088 | Retry now | Tap | Attempt counter increments, success haptic only on user-initiated retry | Immediate probe; polling resumes on success | none | Class-specific message in place; automatic retries continue on the backoff schedule | Button, labelled with the attempt number | FPM section 3 (tick) |
| ACT-089 | Edit settings | Tap | Navigates to SCR-12 with the current values | SCR-12 opens | none | not applicable | Button | FPM section 3 (updateConnection) |
| ACT-090 | Use demo system | Tap | Sheet closes, demo chip appears | Source = demo | source = demo | not applicable | Button, label 'Use the demo system instead' | FPM section 3 |
| ACT-091 | Copy failure detail | Tap | Snackbar 'Diagnostic copied' | Clipboard | none | Fallback selection | Button | FPM section 4 |
| ACT-092 | Dismiss | Swipe down or back | Sheet follows the finger and dismisses | No change to the link state | none | not applicable | Drag handle with a dismiss action available to TalkBack | FPM section 6 (sheet dismissal) |

**Notes**

- The sheet is read-only with respect to hardware: it cannot silently rewrite the saved config, which is why Edit settings is a navigation rather than a save.

### SCR-19 — Charge-mode confirmation dialog

- Route: RTE-19 (dialog, not addressable)  
- Surface: Material 3 AlertDialog  
- Purpose: Confirm a change that affects hardware behaviour, and state its effect on backup reserve.  
- Android status now: Missing.  
- FPM trace: FPM section 1 (charge mode selector)

**States**

| State ID | State | Applies when | Treatment |
| --- | --- | --- | --- |
| STS-087 | confirm | Dialog open | Mode name, effect on reserve, and the current mode named |
| STS-088 | applying | Confirmation in flight | Confirm button busy, cancel disabled |
| STS-089 | failed | The change failed | Failure named in the dialog with retry and cancel |

**Visible actions**

| Action ID | Control | Trigger | Feedback | State change | Persistence | Failure response | Accessibility semantics | FPM trace |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| ACT-093 | Confirm | Tap | Applying state, success haptic, dialog closes | Mode applied and persisted | local preference plus device state | STS-089 with the reason and the reverted selection on SCR-10 | Confirm button labelled with the mode | FPM section 1 |
| ACT-094 | Cancel | Tap, back, or outside tap | Dialog closes | No change | none | not applicable | Cancel button, reachable and labelled | FPM section 1 |

**Notes**

- Backup-only mode states its reserve floor (80 percent) in the dialog, because that is the fact the user is trading away.

## 5. Components

| ID | Component | Purpose | Android status now | Required work |
| --- | --- | --- | --- | --- |
| CMP-01 | EnergyFlow | Four-node live energy model with wattage-modulated trails | Present but static: feature/dashboard/EnergyFlow.kt | Parity: full FPM section 2 row; states live, night, stale, reduced motion; nodes are buttons |
| CMP-02 | BatteryRing | SoC arc with charge glow and discharge pulse | Inline arc only: feature/battery/BatteryScreen.kt lines 46-84 | Extract to a component; add threshold haptics and the text label |
| CMP-03 | ProductionChart | Today curve with scrub readout | Present: feature/dashboard/ProductionChart.kt | Add scrub readout and per-point accessibility values |
| CMP-04 | WeekChart | Produced versus consumed, seven days | Missing (series exists: TelemetryRepository.buildWeekSeries line 206) | New component, tappable bars |
| CMP-05 | ForecastCard | Seven-day forecast rows | Missing | New component with loading and error states |
| CMP-06 | ForecastStrip | Five-day glance strip | Present: feature/dashboard/ForecastStrip.kt | Wire to real forecast state; add skeleton and empty handling |
| CMP-07 | InsightCard | Advisory row with optional action | Missing | New component; the action must navigate (the PWA action button is a dead control at src/components/InsightCard.tsx lines 57-62) |
| CMP-08 | InsightHighlight | Featured advisory | Present: feature/dashboard/InsightHighlight.kt | Add severity wording and the demo qualifier |
| CMP-09 | MetricTile | Labelled metric with optional delta | Present: feature/dashboard/MetricTile.kt with MetricsGrid | Keep; add unit handling and large-text reflow |
| CMP-10 | LiveNumber | Numeric ticker | Present: feature/dashboard/LiveNumber.kt | Wire to the telemetry flow; stop ticking when stale |
| CMP-11 | StatusPill | Inverter status word plus dot | Missing (TopBar shows a bare dot) | New component covering producing, standby, curtailed, night, fault, offline, demo |
| CMP-12 | ShareButton | Builds and shares a snapshot | Missing as a component; the Settings stub has a dead share button (feature/settings/SettingsScreen.kt lines 78-88) | New component with a 48 dp target, haptic, and copy fallback |
| CMP-13 | TopBar | Brand mark, freshness, status | Partial: feature/dashboard/TopBar.kt (no freshness, no share) | Extend to the three-slot layout |
| CMP-14 | BottomNav | Five destinations | Present but never composed (feature/dashboard/BottomNav.kt, referenced nowhere) | Wire into a Scaffold; the header comment says four tabs and must become five |
| CMP-15 | HeliosMark | Brand mark, eight-blade radial | Missing (TopBar renders the text 'Helios') | New Canvas component; text mark for white labels |
| CMP-16 | WeatherIcon | Condition to icon plus colour | Missing (WeatherCondition enum exists, no mapping) | New component with text labels for accessibility |
| CMP-17 | SectionHeader | Eyebrow, title, optional trailing | Private helper inside feature/dashboard/DashboardScreen.kt lines 110-125 | Promote to a shared component |
| CMP-18 | FreshnessStamp | Age of the reading, opens the connection sheet | Missing | New: live, aging, stale, offline, demo |
| CMP-19 | ConnectionBanner | Persistent failure notice with retry | Missing | New: offline with reason, reconnecting, recovered |
| CMP-20 | ConnectionSheet | Inspect and fix the link | Missing | New modal with the classified failure states |
| CMP-21 | ChargeModeOption | Charge strategy row | Missing | New: selected, applying, confirmed, failed with revert |
| CMP-22 | SetupField | Onboarding and connection input | Missing | New: default, focused, invalid, valid |
| CMP-23 | StepProgress | Onboarding position | Missing | New: four steps with a current and completed state |
| CMP-24 | SnapshotSummaryRow | Shared viewer metric line | Inline only in feature/shared/SharedScreen.kt (SnapshotRow) | Extract; handle missing optional values |

Contracts (props, variants, states, accessibility) for the new and changed components are specified in DESIGN.md section 8. Component sources must stay inside the existing design-system and feature packages; no second design system is introduced.

## 6. Services, types, and persistence

| ID | Concern | Android status now | Notes for the build step |
| --- | --- | --- | --- |
| SVC-01 | readTelemetry — TelemetryRepository.readTelemetry | Present (TelemetryRepository.kt lines 71-150) | Simulated source today; needs a source switch between demo and real SunSpec |
| SVC-02 | buildTodaySeries — TelemetryRepository.buildTodaySeries | Present | 48 half-hour points |
| SVC-03 | buildWeekSeries — TelemetryRepository.buildWeekSeries | Present (line 206) | Wire to the week chart |
| SVC-04 | fetchForecast — ForecastRepository.loadForecast | Stub (hard-coded days, ForecastRepository.kt lines 26-52) | Real Open-Meteo call with status and error flow |
| SVC-05 | reverseGeocode — none | Missing | Open-Meteo geocoder with a coordinate fallback |
| SVC-06 | mapWeatherCode — none | Missing | 15-code table from the PWA |
| SVC-07 | generateInsights — InsightsRepository.generateInsights | Present | Must not run when the source is stale, offline, or unqualified demo |
| SVC-08 | computeSavings — none | Missing | Rates must be visible in the UI (0.32 import, 0.08 export) |
| SVC-09 | generateForecastInsights — none | Missing | Depends on SVC-04 |
| SVC-10 | resolveBrand — BrandRepository.resolve | Present but with the wrong registry (helios, solaris, volt, aether) | Must become helios, voltcraft, sunworks, meridian to match src/services/brand.ts |
| SVC-11 | applyBrandAccent — none | Missing | Accent override that never touches the semantic hue roles |
| SVC-12 | encodeSnapshot — ShareRepository.encodeSnapshot | Present | base64url without padding; must round-trip against the PWA |
| SVC-13 | decodeSnapshot — ShareRepository.decodeSnapshot | Present | Must reject v other than 1 |
| SVC-14 | buildSnapshot — ShareRepository.buildSnapshot | Present | Check the rounding rules against src/services/share.ts (ac 2 dp, todayKwh 1 dp, lifeKwh integer, soc and selfUse integers) |
| SVC-15 | buildShareUrl — ShareRepository.buildShareUrl | Present | https://helios.app/share/{payload} |
| SVC-16 | SolarCurve — SolarCurve.kt | Present | Matches shared-spec/simulation-formulas.md section 1 |
| SVC-17 | Formatters — none | Missing | One formatting source for kW, kWh, percent, temperature, currency, relative time |
| SVC-18 | ThemeManager — ThemeRepository | Partial (persists the mode; nothing applies it) | Apply before the first frame |
| SVC-19 | ConnectionRepository — none | Missing | Persist ConnectionConfig, expose status and last error, own the retry schedule |

Types come from FPM section 5 and already exist in `android/app/src/main/java/com/helios/core/domain/model/` with the same field names as the PWA (`SolarTelemetry`, `PanelString`, `HistoryPoint`, `Insight`, `ConnectionConfig`, `Location`, `ForecastDay`, `ProductionForecast`, `Brand`, `SnapshotPayload`, `InverterStatus`, `WeatherCondition`). Two type-level facts the build step must fix:

- `ConnectionConfig` is a Room entity with no DAO, no database, and no repository; nothing reads or writes it today.
- `Brand` ids in `BrandRepository.kt` (helios, solaris, volt, aether) do not match `src/services/brand.ts` (helios, voltcraft, sunworks, meridian). Snapshot payloads and deep links carry those ids, so the registry must match the PWA or shared links resolve to the wrong brand.

## 7. Flows

| Flow ID | Flow | Entry route | Screens | Requirements | Exit criterion |
| --- | --- | --- | --- | --- | --- |
| FLW-01 | First run: onboarding and inverter connection | RTE-02 | SCR-02..SCR-06, SCR-07 | REQ-01, REQ-02, REQ-12 | Live values on SCR-07 from a real inverter or the demo system, with first_run_complete persisted |
| FLW-02 | Principal repeated task: check live solar and battery state | RTE-07 | SCR-07 plus drill-downs SCR-08, SCR-09, SCR-10 | REQ-03, REQ-04, REQ-05, REQ-06, REQ-11 | Six facts visible in the first 430 dp without a tap, with freshness stated |
| FLW-03 | Recovery from inverter and network failure | any | SCR-07, SCR-08, SCR-10, SCR-18, SCR-12 | REQ-02, REQ-11 | A classified reason, a bounded retry schedule, a working configuration preserved, and no stale value shown as live |
| FLW-04 | Return use | RTE-01 | SCR-01, SCR-07, SCR-17 | REQ-11, REQ-12 | Theme applied before the first frame, last-known values labelled with age, forecast refreshed when older than an hour |
| FLW-05 | Share a snapshot and view a shared snapshot | RTE-17 | SCR-07, SCR-16, SCR-17 | REQ-09, REQ-10 | A payload that round-trips with the PWA, and a viewer that works offline and rejects invalid links with a clear state |
| FLW-06 | Supporting flows: charge strategy, theme and brand, connection editing | RTE-10, RTE-12, RTE-14, RTE-15 | SCR-10, SCR-19, SCR-12, SCR-14, SCR-15 | REQ-07, REQ-08 | Hardware-affecting change confirmed and reverted on failure; appearance changes applied and persisted |

Each flow is specified step by step, with its failure and recovery branches, in `design/ux-flows.md`.

## 8. Coverage and gaps

| Check | Result | Evidence |
| --- | --- | --- |
| Every brief feature has at least one screen and one flow | Yes | REQ-01 to REQ-12 mapped in sections 2, 4, and 7 |
| Every screen has stable state IDs | Yes | 89 states across 19 screens |
| Every visible action states trigger, feedback, state change, persistence, failure, and accessibility semantics | Yes | 94 actions in section 4 |
| Every action traces to the feature-parity matrix | Yes | FPM trace column per action, screen-level trace per screen |
| Screens with no implementation today | 8 of 19 | SCR-01, SCR-02, SCR-03, SCR-04, SCR-05, SCR-06, SCR-12, SCR-16, SCR-18, SCR-19 (missing); SCR-08, SCR-09, SCR-10, SCR-11 (stubs); SCR-07 partial |
| States that cannot occur and are therefore not listed | Documented in place | Metric tiles on SCR-07 are read-only by decision; SCR-08 has no chart range switch; notifications and app lock are not shipped rather than shown as dead rows |

Known gaps the build step must close, in dependency order: the start gate and first-run flag (nothing renders onboarding today), the connection repository and retry schedule (nothing persists or classifies failures today), the telemetry flow into the screens (every screen reads once with `remember`), the five-destination scaffold (BottomNav is never composed), the stub screens, and the theme applied before the first frame (the theme is hard-coded to dark).

Out of scope for this design and not to be presented as designed work: widgets, the Quick Settings tile, the Wear OS module, iOS, store submission, accounts, cloud sync, and analytics.
