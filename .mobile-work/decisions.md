# Design decision log — helios Android

Scope and architecture decisions made while writing the design foundation. Each one is
reversible in the build step, and each one names what it costs to reverse.

| ID | Decision | Reason | Cost to reverse |
| --- | --- | --- | --- |
| D-01 | Direction D1 "Live flow console" is the Dashboard direction | It answers the brief's principal repeated task (live solar and battery state) with one instrument and no prose, and it is the only direction that shows the product's energy model at the glance layer | High: it re-cuts the key screen and the motion baseline |
| D-02 | D2 "Day ledger" and D3 "Today timeline" are rejected, with their strengths absorbed (ranked advisory under the hero; analysis on the Production screen) | D2 needs recommendation confidence the insight engine does not produce and risks advising from stale data; D3's chart hero slows the 2-5 s glance it must serve | Low |
| D-03 | Freshness is a first-class element and no value is shown as live without an age statement | The inverter link is a LAN link that fails routinely; a stale number presented as live is the worst failure mode in a monitoring app | Low |
| D-04 | The connection is reachable in one tap from the hero (freshness stamp and status pill open the connection sheet) | A user whose system is down should not hunt through Settings | Low |
| D-05 | Insight generation is suppressed when telemetry is stale, offline, or demo | The engine is template-based; advisories from unsupported data read as authority the app does not have | Low |
| D-06 | The marketing landing page is not shipped as the app's start destination; the start gate plus working onboarding replaces it | FPM section 1 already assigns that route to onboarding in native apps, and a marketing page as the app's first frame blocks the actual first-run task | Medium: the landing content remains available on the web |
| D-07 | Location permission is requested from "Use my location", never at launch | A permission wall before any user action is the wrong first run; the current launch-time request in MainActivity.kt is the defect being fixed | Low |
| D-08 | Five bottom-navigation destinations, with notifications and app lock removed rather than shown as dead rows | FPM section 1 and src/components/BottomNav.tsx define five; a control with no implementation is unacceptable | Low, if the owner wants the features designed and built |
| D-09 | Failure taxonomy F1-F9 with a bounded retry schedule (1 s / 2 s / 4 s, then 30 s, then 60 s) | Routine LAN and Modbus failures need different messages and different next actions; unbounded retries drain the battery and lie about progress | Low |
| D-10 | Insight action labels keep PWA copy but must perform a real navigation; hidden when they cannot be honoured | The PWA renders the action label as a dead button (src/components/InsightCard.tsx lines 57-62) | Low |
| D-11 | The brand registry adopts the PWA ids (helios, voltcraft, sunworks, meridian) | Shared snapshot payloads and deep links carry those ids, so a mismatch resolves to the wrong brand | Low |
| D-12 | Snapshot parity is judged at the schema and rounding level, with round-trip tests in both directions | Kotlin JSON key order need not match JSON.stringify; decode compatibility is what keeps shared links working | None |
| D-13 | Metric tiles on the Dashboard are read-only | Four drill-down paths already exist on the flow nodes; four more targets add cost without adding a task | Low |
| D-14 | Documented gap rather than a workaround for verified App Links | No .well-known/assetlinks.json ships in the repository, so the honest testable path is the custom scheme | Removed by hosting the file |

Open for the owner, not decided here: whether notifications (anomalies, weekly digest)
and a biometric app lock should be designed and built, and whether a future revision
should add inverter discovery on the LAN.
