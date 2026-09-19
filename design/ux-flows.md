# helios Android — UX flows (v1)

Companion to `design/DESIGN.md`. Requirement, route, screen, state, action, component
and service IDs are defined in `design/screen-inventory.md`; this document uses them.

Flows covered: FLW-01 first run (onboarding + inverter connection), FLW-02 the
principal repeated task (checking live solar and battery state), FLW-03 recovery from
inverter and network failure, FLW-04 return use, FLW-05 share and the shared-snapshot
viewer. FLW-06 covers two short supporting flows (charge strategy, theme and brand).

## 0. State taxonomy used by every flow

| State kind | Definition used in this design | Visible treatment |
| --- | --- | --- |
| loading | A request is in flight and no value exists yet | Skeleton in the shape of the eventual content (`motion-language.md` section 10); never a spinner over old values |
| live | Last successful read is 5 s old or less | Ticker running, freshness stamp reads "Live · n s ago", status pill shows the inverter status |
| aging | 5-15 s since the last read (2-7 missed polls) | Ticker stops; stamp reads "Updated 14:32:06"; values stay at full contrast for 15 s |
| stale | More than 15 s since the last read | Values dim to 60 percent, `ConnectionBanner` appears with the age and reason, insight block replaced by the waiting placeholder (DESIGN.md C3) |
| offline | The link is confirmed down (classified failure) | Banner with reason and Retry, last-known values labelled "last known", EnergyFlow trails stopped |
| demo | Telemetry source is the simulated system | Persistent "Demo system" chip in `TopBar`; no "Live" wording; insights carry a "Demo data" qualifier |
| empty | The query is valid and there is genuinely nothing to show | Explanatory line plus the action that fixes it, never a blank card |
| error | A user-visible failure the user can act on | Inline message with reason and one recovery action |
| success | An action completed | Confirmation that states what changed and where it persisted |
| disabled | The action cannot run yet | Control visibly disabled with the precondition named |

A state is only listed for a screen when it can actually occur there. States that
cannot occur are marked "not applicable" in `design/screen-inventory.md` with the
reason, rather than invented for completeness.

## FLW-01 First run — onboarding and inverter connection

Entry: cold start with no `first_run_complete` flag (RTE-02). Trigger of interest: the
owner has just installed helios, is standing next to an inverter, and wants to know
whether the app works with it.

Design rules that shape the flow:

- No account, no email, no cloud step. Anything that cannot be done on the phone is
  not asked for (`README.md`; brief non-goals).
- The demo system is available from the first screen in one tap, so a user without
  credentials, without Wi-Fi on the inverter, or without time still reaches value.
- The inverter connection is the only blocking step, and it can be deferred: "Use
  demo system" is a first-class choice, not an escape hatch.
- Location is requested from the action that needs it, never at launch. The current
  launch-time request in `MainActivity.kt` is removed by this design.
- Permission denial is a supported path, not an error.

| Step | Screen | What the user does | What the system does | Persistence | Failure path |
| --- | --- | --- | --- | --- | --- |
| 1 | SCR-02 Welcome | Reads one sentence and taps "Set up my system" (ACT-001), or taps "Explore demo first" (ACT-002) | Advances to SCR-03; the demo path skips to SCR-07 with the demo source selected | Demo path writes source = demo | None; nothing can fail here |
| 2 | SCR-03 Local-first promise | Confirms understanding (ACT-004) or taps back (ACT-005) | States plainly what stays on the device and that only coordinates go to Open-Meteo | None | None |
| 3 | SCR-04 Connect | Enters host, port, unit ID, poll interval (ACT-007 to ACT-010), submits from the keyboard (ACT-011), taps "Test connection" (ACT-012), or taps "Use demo system" (ACT-013) | Validates fields locally, runs a bounded TCP connect plus SunSpec magic-word probe; shows STS-007 while testing | Nothing yet | Field validation errors inline (STS-006) |
| 4 | SCR-05 Test result | Reads the result; taps "Go to dashboard" (ACT-014), "Try again" (ACT-015), "Edit details" (ACT-016), or "Use demo system" (ACT-017) | On success shows inverter identity from the device (manufacturer, model, firmware, string count) as proof, then persists the config | ConnectionConfig written on success only | Each failure class gets its own message and its own next action (see FLW-03) |
| 5 | SCR-06 Location | Taps "Use my location" (ACT-019), picks a place by name (ACT-020), or taps "Not now" (ACT-021) | Requests location permission at the tap; reverse-geocodes; on denial keeps the default location and says so | Location record plus source | Denied (STS-017) and geocode failure (STS-018) both leave a usable default |
| 6 | SCR-07 Dashboard | Sees live values | Marks first run complete, starts the 2 s poll | `first_run_complete` | None |

Time and effort on the critical path, first run with a real inverter: 6 taps minimum
(through, through, test, go, location choice, done) and roughly 35-60 s, dominated by
typing the inverter host. First run via demo: 1 tap to SCR-07.

Value point: the user sees live production, battery state, and the energy-flow model
on SCR-07. On the demo path that is reachable in one tap from the first screen; on the
real path it is the first frame after a successful test.

Interruptions and edges the design must handle:

- Abandoned onboarding (process death on SCR-04): first run resumes on SCR-04 with the
  typed host and port restored from the saved draft; page index is not restored to a
  screen the user has already passed.
- Back navigation: the system back gesture moves one onboarding page back, never out
  of the app until SCR-02, where back exits.
- Field interaction: the host field uses a URL-capable keyboard, accepts a hostname or
  an IPv4 literal, and trims whitespace; the port field accepts 1-65535; poll interval
  offers the supported values (1 s, 2 s, 5 s, 10 s) rather than free text.
- The connect step never shows a fake progress bar; it shows elapsed seconds while the
  probe runs and cannot exceed a 6 s bound.
- Second run: SCR-02 through SCR-06 are not reachable again except through
  SCR-12 (settings) and an explicit "Run setup again" action on SCR-16 About.

## FLW-02 Principal repeated task — check live solar and battery state

Entry points, all of which must land on live state without an extra tap:

| Entry | Resolution |
| --- | --- |
| Launcher icon | SCR-01 resolves to SCR-07 (returning user) with last-known values painted immediately |
| Resume from background | Same task and tab as before; a poll is issued immediately if the last read is older than 5 s |
| Widget or Quick Settings tile tap | SCR-07, scrolled to the top, latest telemetry requested before first frame if the cached value is older than 60 s |
| `helios://dashboard` | SCR-07 |
| Shared-snapshot link | SCR-17, not the dashboard (see FLW-05) |

The glance, 2-5 s, with no tap and no scroll on a 412 x 915 dp viewport. Visible in
the first 430 dp:

1. Inverter status as a word plus dot (Producing / Standby / Night / Curtailed /
   Fault / Offline / Demo system).
2. Live AC power as the largest number on screen with its unit.
3. Battery state: SoC percent, and direction and magnitude of flow.
4. Grid direction: importing, exporting, or net zero.
5. Home load right now.
6. Freshness: how old the reading is.

The study, 30-60 s, from the same screen: tap the Solar node for SCR-08 (per-string
telemetry, inverter grid, week chart), the Battery node for SCR-10 (ring, backup
readiness, charge strategy), the Grid node for the savings block on SCR-09, or the
Home node for the consumption insight on SCR-09.

Freshness behaviour during the glance (DESIGN.md C1): the reading is live at 5 s or
less; between 5 s and 15 s the ticker stops and the stamp switches to absolute time;
past 15 s the values dim, the banner appears, and the insight block is replaced by the
waiting placeholder. A demonstration of the primary task must therefore include the
transition from live to aging to stale, not only the happy path.

What the design forbids on this path: animating a number that is not being updated;
labelling a simulated system as live; showing 0 W as a measurement when the read
failed; running the insight engine on stale data to avoid an empty block.

Interaction and navigation behaviour:

- Five destinations (Home, Solar, Insights, Battery, Settings) in a `NavigationBar`.
  Each destination keeps its own scroll position and its selected sub-state.
- Tab switches use the `snappy` spring with the moving indicator from the PWA
  (`src/components/BottomNav.tsx`), translated to a Compose spring, not a cross-fade.
- Tapping an already-selected destination scrolls that destination to the top.
- Drill-down from a flow node uses the shared-element transition specified in
  `motion-language.md` section 7. Back returns to the Dashboard with the source card
  re-expanded, and predictive back shows the Dashboard beneath the gesture.
- Haptics: tile tap light, tab change selection, share copy success, connection
  failure error (`design-tokens.json` `haptics.mapping`).
- Reduce Motion: no particle trails (static arrows), no shared-element morph, opacity
  cross-fade page changes only.

## FLW-03 Recovery from inverter and network failure

The inverter is a LAN device with a single Modbus TCP server. Failure is routine, not
exceptional, so every class below gets a specific message and a specific next action.
The app never guesses: it reports what it can prove (no route, refused, timeout,
protocol error).

| Id | Failure class | Detection | What the user sees | Automatic behaviour | Offered action |
| --- | --- | --- | --- | --- | --- |
| F1 | No network / Wi-Fi off | No active transport, or route to host fails immediately | Banner: "Not on the same network as the inverter" with age | Poll paused; retry when transport returns (registered network callback) | Retry, Open Wi-Fi settings |
| F2 | Timeout, host asleep | TCP connect exceeds 2 s | Banner: "Inverter not responding (192.168.1.42:502)" plus attempt count | 3 attempts with 1 s / 2 s / 4 s backoff, then poll every 30 s, then 60 s after five failures | Retry now, Edit settings, Use demo system |
| F3 | Connection refused | TCP connect rejected | Banner: "Port 502 refused the connection" | Same backoff | Edit port, Retry |
| F4 | Wrong unit id | Modbus exception 2 (illegal data address) | Banner: "Answered, but unit id 1 has no SunSpec model" | Stop hammering; hold the session until the user edits | Edit unit id, Retry |
| F5 | Not a SunSpec device | TCP opens, no SunS magic word | Banner: "This device does not speak SunSpec Modbus" | Hold; no automatic retry loops | Read the protocol note, Use demo system |
| F6 | Another master holds the socket | Connect succeeds, reads return nothing, or the peer resets | Banner: "Another Modbus master is connected" | Retry at 30 s; advisory explains that most inverters allow one master | Retry, explanation |
| F7 | Truncated or malformed response | Response shorter than the register block, or CRC/structure error | Banner: "Incomplete data from the inverter — showing the last good reading" | Retry next poll; partial values are not rendered as complete | Retry, last-good timestamp |
| F8 | Forecast network failure (separate from F1-F7) | Open-Meteo request fails | Inline in the forecast section only: "Forecast unavailable — retry" | Retry at 15 min, then hourly | Retry |
| F9 | Location denied or geocode failed | Permission result or geocoder error | Inline note on SCR-06/SCR-13: "Using the default location" | No repeat prompting | Open settings, choose a place by name |

Recovery surface: SCR-18 `ConnectionSheet`, opened from the banner, the freshness
stamp, or the status pill. It shows the current source, host/port/unit, time of the
last good read, the last classified error, the attempt counter, and three actions:
Retry now (ACT-088), Edit settings (ACT-089), Use demo system (ACT-090). It is a
`ModalBottomSheet` with a drag handle, dismissible by swipe or back, and it never
blocks the screen behind it.

Success path: when a retry succeeds the banner becomes "Reconnected · updated now"
for 4 s and auto-dismisses, the success haptic fires only for a user-initiated retry
(never for a background poll), values return to full contrast, and numbers cross-fade
from the last-known value to the current one instead of counting through fabricated
intermediate values.

Hard rules in failure:

- The reading is never labelled live while the link is down; EnergyFlow trails stop
  (not slow down); stale values are dimmed and carry an age.
- Insight generation is suppressed while the source is stale, offline, or demo, so the
  app never advises action on data it cannot support.
- A 0 W grid or battery reading is only shown when the register read succeeded; a
  failed read shows "no data", not zero (the difference matters for a fault diagnosis).
- Retry is bounded: automatic retries stop escalating after five failures and drop to a
  60 s poll; the user can always retry manually.
- After the third failed automatic attempt, the design changes the hypothesis rather
  than repeating: the Connection sheet leads with Edit settings and the demo fallback,
  and the banner states which class of failure it is.

## FLW-04 Return use

| Situation | Behaviour |
| --- | --- |
| Cold start, next morning | Theme resolved from DataStore before the first frame (no flash of the wrong theme); last-known telemetry painted with its age; immediate poll; NIGHT state is normal, not an error, and the screen says so plainly |
| Cold start after 3 days away | Values shown as "last known, 3 days ago" until the first successful poll; the day series is rebuilt from the current day, never reused from the stale day |
| Forecast older than 1 hour | Refreshed in the background on resume, with the existing values kept on screen (no skeleton over good data) |
| Resume after a charge-mode change made elsewhere | Battery screen shows the mode the inverter reports, and a one-line note if it differs from the app's saved preference |
| Return via a shared-snapshot link | SCR-17 opens directly, without onboarding interference and without a login wall; back from SCR-17 returns to the previous destination, or to Home if the viewer was cold-started |
| Return via widget or Quick Settings tile | Home, top of screen, fresh values |
| Return after the app was force-stopped mid-inverter-test | First run resumes at SCR-04 with the draft restored; no half-saved connection config is used for polling |
| Return with a white-label brand set | The brand and its accent apply before the first frame, including on the shared viewer when the payload names a brand |

## FLW-05 Share a snapshot, and the shared-snapshot viewer

Ship-identical constraint: `SnapshotPayload` v1 and base64url encoding stay
byte-identical to the PWA (`src/services/share.ts`). The Android encode and decode
must satisfy the PWA fixtures, and a payload produced on Android must open the PWA
viewer and vice versa. That check belongs in the build step's test set.

Creating a snapshot (SCR-07 hero, ACT-031; also SCR-16 About for support cases):

1. Build the payload from the current telemetry, location label, brand id, and the
   five forecast values, with the PWA rounding rules (`ac` 2 dp, `todayKwh` 1 dp,
   `lifeKwh` integer, `soc` and `selfUse` integers, `fc` integers).
2. Open the Android share sheet with the URL `https://helios.app/share/{payload}`.
3. Feedback on return: success haptic plus a snackbar "Snapshot link shared" or
   "Link copied" when no share target exists; on both failures, a snackbar with the
   URL and a long-press-to-copy fallback.
4. Rules: sharing is never blocked by a stale link (a snapshot is a moment, so a
   last-known reading is valid), but a stale or demo snapshot carries the same qualifier
   in the payload's brand/text and in the share text.

Viewing a snapshot (SCR-17, RTE-17):

| Case | Behaviour |
| --- | --- |
| Valid payload | Brand from `payload.br` (the app has no URL query to read), location label, timestamp in the device locale, live kW, today kWh, battery percent, self-use percent, lifetime MWh, CO2 note, optional 5-day forecast row |
| Payload with no forecast array | The forecast block is absent, not empty; layout closes up |
| Invalid or truncated payload | "Link expired or invalid" with the reason class and one action: open the app (existing user) or start setup (fresh install) |
| White-label payload | Brand name and accent from the payload; the footer reads "Powered by helios" |
| Offline | Decoding is local, so the viewer works with no network; only the "Try helios" call to action degrades |
| Back behaviour | SCR-17 is a leaf: back returns to the previous destination, or exits the app when cold-started from the link |
| Verified App Links | `https://helios.app/share/...` is declared with `autoVerify`, but no `.well-known/assetlinks.json` ships in the repository (`public/` has no `.well-known`), so Android will show a chooser. The custom scheme `helios://share/...` is the testable path until the host file exists; this gap is recorded, not hidden |

## FLW-06 Supporting flows

Charge strategy (SCR-10): the current mode is read from the inverter. Choosing a
different mode opens SCR-19, which states what will change and the effect on backup
reserve. Confirming shows applying, then either a confirmed state with success haptic
and a persisted preference, or a failed state that reverts the selection to the
inverter's reported mode and names the failure. The design never leaves the selection
showing a mode the inverter did not accept.

Theme and brand (SCR-14, SCR-15): applying is instant and persisted
(`ThemeRepository` exists for this; `BrandRepository` already resolves four brands).
Colour change must not be the only signal of the change: the selected option carries a
check mark and a state description for TalkBack. Android dynamic colour stays off, so
the helios palette is never replaced by wallpaper colours.

Connection editing (SCR-12): fields start at the saved values; any edit marks the form
dirty and enables Save; leaving a dirty form warns once; Save validates (host shape,
port range, unit id 1-247, poll interval from the supported set), then tests before
persisting. A failed test does not overwrite the saved working configuration, and the
screen says which config is still in use.
