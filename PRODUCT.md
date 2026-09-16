# Product

<!-- impeccable:product-schema 1 -->

## Platform

web

## Stack

Already answered by the codebase: Vite 5 + React 18 + TypeScript (strict) + Tailwind CSS 3,
React Router 6, Zustand, Framer Motion, Recharts, Lucide icons, `vite-plugin-pwa`.
Routes live in `src/App.tsx`: `/` (landing), `/app` (the PWA), `/share/:payload` (shared snapshot).
No stack decision is open.

## Users

**Primary — the array owner.** A homeowner with a rooftop PV array and a hybrid inverter. They are
usually standing in the kitchen, the garage, or at the utility wall with a phone in one hand. Their
job: *"is my array working, what is it doing right now, and what should I run while the sun is up?"*
They are not electricians. They want a specific instruction, not a chart.

**Secondary — the installer.** An installer or energy contractor who hands the finished dashboard to a
customer. They need the customer-facing surface to carry their own name and accent colour, and they
need to hand over a link that keeps working without an account, a server, or a database.
(`?brand=helios|voltcraft|sunworks|meridian`, `src/services/brand.ts`.)

**Reader of a shared link.** Someone who received a `helios°` snapshot link and has no install.

## Product Purpose

`helios°` turns raw inverter telemetry into something a homeowner can act on. It reads SunSpec Modbus
data from the inverter, fuses it with a solar-irradiance forecast, and produces concrete
recommendations: when to run the dishwasher, when to pre-charge the battery, which string is shading.
It is an installable, offline-capable Progressive Web App with no backend.

Success for this landing surface: a first-time visitor understands what the product does, sees the
mechanism working with real units and real states, and installs it — or, if they are an installer,
understands they can put their own name on it.

## Positioning

The parts a neighbouring solar-monitoring product could not truthfully copy:

1. **It reads the inverter directly, over the industry's own protocol.** SunSpec Modbus TCP — the
   interface SMA, Fronius, SolarEdge, Enphase and Schneider inverters already speak. Not a
   proprietary cloud API.
2. **The forecast needs no key and no account.** Open-Meteo shortwave radiation, modelled locally:
   `expected_kWh = peak_sun_hours × system_rating_kW × performance_ratio (0.82)`.
3. **No backend at all.** Cloud sync, accounts, and a database are absent by design. A reading can be
   encoded into a URL (`base64url`, client-side, `src/services/share.ts`) and opened on any device. No
   tracking, because there is nowhere to send it.
4. **White-label by URL.** `?brand=` swaps accent, mark and copy from one file, and the brand survives
   into a shared snapshot, so the installer's name travels with the customer's link.
5. **It answers, it does not just chart.** Peak-production windows, cloud warnings, string imbalance,
   thermal derate risk, self-consumption vs the neighbourhood median, multi-day battery strategy
   (`src/services/aiInsights.ts`).

## Operating Context

- The visitor is usually on a phone, often outdoors or in bright daylight. The app ships both a dark
  carbon ground and a "parchment and graphite" light inversion, for reading in direct sunlight.
- The subject's own physical world is the utility wall: the inverter and its riveted nameplate, the
  breaker panel with its taped circuit directory, the meter, the string wiring, the printed utility
  statement, the module datasheet.
- Two directions of travel on this surface: the owner (persuaded to install and open `/app`) and the
  installer (persuaded that this is presentable to their own customers).
- Environment: static hosting only. `dist/` deploys anywhere; the browser cannot speak raw TCP, so a
  production inverter link needs a local gateway or relay (README "Deployment").

## Capabilities and Constraints

Confirmed and shipped:

- Live energy-flow view: solar → home ↔ battery ↔ grid, with live wattages (`EnergyFlow.tsx`).
- 7-day production forecast from Open-Meteo; refetched hourly.
- Per-string telemetry: three strings, utilisation, DC/AC voltage, current, frequency, heatsink and
  cabinet temperature (`src/services/sunspec.ts`).
- Battery: Self-consumption, Time-of-use, Backup-only; state-of-charge ring; backup-readiness estimate.
- Insights with severity, metric, delta and an action label (`aiInsights.ts`).
- Share deep-links; light/dark/auto theme with no flash of wrong theme; installable PWA, offline after
  first load; maskable icons.
- Reference hardware in the shipped mock: a 9.6 kW hybrid system, three 3.2 kW strings of 8 panels
  each, a 13.5 kWh battery, 0.964 DC→AC conversion, model `HX-9.6 Hybrid Inverter`, firmware `4.12.1`.

Constraints and things that are not true:

- No prices, tariffs, subscriptions, customer counts, or store listings exist. `$0.32/kWh` and
  `$0.08/kWh` in `aiInsights.ts` are mock constants for the demo model, not product claims.
- No testimonials, press, logos, certifications, awards, or case studies exist. Do not invent them.
- Native iOS/Android apps exist in `ios/` and `android/` but are explicitly deferred; the PWA is the
  shipping product. Do not sell the native apps.
- The mock service (`readTelemetry()`) is the only live data source today; a real inverter needs the
  documented `sunspec.ts` swap. Any live figure on a marketing surface must be labelled as a demo.

## Brand Commitments

- Name and wordmark: **`helios°`** — lowercase, with the degree glyph as part of the mark. Legal name
  `helios° energy`. The mark is `HeliosMark.tsx`; `design/helios-icon.svg` and `public/favicon.svg`
  are the same identity.
- Tagline in `src/services/brand.ts`: *"Precision energy intelligence for your solar array."*
- README states the visual identity is a **"precision-instrument"** identity, carbon (`#070708` →
  `#1a1a1c`) with bone (`#f4f1ea`, `#dcd6c8`), and names **Instrument Serif** for display numerics,
  **JetBrains Mono** for technical labels and units, **Inter** for body.
- Committed colour roles exist as tokens: `--signal-solar #f0c674`, `--signal-flow #7fb069`,
  `--signal-grid #5d8aa8`, `--signal-battery #c5a572`, `--signal-alert #d97757`. Solar amber is the
  identity accent; the other four are semantic.
- Voice: technical, plain, dry-witted. The README's own register ("do what you want, but don't blame us
  if your inverter catches fire") is the upper bound of the humour.

## Evidence on Hand

- `README.md` — the product's own description, routes, forecast formula, deployment reality.
- `src/services/sunspec.ts` — the full telemetry model and the reference system's real numbers.
- `src/lib/solarCurve.ts` — sunrise 06:12, solar noon 13:00, sunset 19:48, consumption curve.
- `src/services/aiInsights.ts` — the real insight vocabulary and thresholds.
- `src/components/*.tsx` — EnergyFlow, ProductionChart, BatteryRing, ForecastStrip, StatusPill.
- `public/mobile-dashboard-full.png`, `public/mobile-dashboard-viewport.png` — real app screenshots;
  note both are near-black captures.
- `design/helios-icon.svg`, `public/favicon.svg`, `public/icons/*` — real identity assets.
- Absent, and therefore never to be fabricated: customers, testimonials, press coverage, certifications,
  pricing, installed-capacity totals, benchmarks against named competitors.

## Product Principles

1. **Specific over impressive.** A number with a unit and a state beats a bigger number.
2. **Answer, don't just chart.** Every figure exists to produce a decision the owner can take today.
3. **Show the real mechanism.** SunSpec, Open-Meteo, and the client-side snapshot are the product; show
   them working rather than describing them.
4. **No account, no server, no tracking.** Absence of infrastructure is a feature and should read as one.
5. **The instrument, not the brochure.** Precision-instrument credibility is the brand; hype is off-brand.

## Accessibility & Inclusion

No formal standard has been recorded for this project. The incumbent app already respects
`prefers-reduced-motion`, keeps a visible theme system with a no-flash boot script, and targets touch
on phones. This surface must keep: text contrast at 4.5:1 or better, keyboard-reachable controls with a
visible focus ring, real text rather than text inside images, and a full-strength static rendering under
reduced motion.

---

## Inferred, not interviewed

The skill's init flow normally asks the user before writing this file. The invocation that produced this
run carries an explicit strict no-popup rule, so the facts above were taken from the repository
(README, `src/`, `design/`, git history) rather than from an interview. Everything under **Users**,
**Positioning** and **Product Principles** is an inference over that evidence; it is reviewable and
should be corrected by the owner where it is wrong. No fact was invented to fill a section: where the
repository is silent (pricing, customers, certifications, formal accessibility standard) this file
records the absence instead.
