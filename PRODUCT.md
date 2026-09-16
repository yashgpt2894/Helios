# Product

<!-- impeccable:product-schema 1 -->

## Platform

web

## Users

Primary: homeowners with a residential rooftop solar array (typically with a hybrid inverter and a home battery) who want to know what their system is doing right now and what to do about it: when to run heavy appliances, whether the battery will carry the evening, whether a string is under-performing. They check on a phone, often outdoors or in daylight, and install the app to the home screen. [Inferred from README and app copy; not user-confirmed.]

Secondary: solar installers who re-skin the app for their customers through the `?brand=` white-label parameter and share branded snapshot links. [Inferred from `src/services/brand.ts` and README white-labeling section; example brands (Voltcraft, SunWorks, Meridian) are fixtures, not customers.]

## Product Purpose

helios° is a Progressive Web App that turns raw inverter telemetry into something a homeowner can act on. It reads SunSpec Modbus data from the inverter, fuses it with a real solar-irradiance forecast (Open-Meteo), and produces concrete recommendations: peak-production windows for heavy loads, cloud warnings, string-imbalance and thermal alerts, savings estimates, and a multi-day battery strategy. Success is a homeowner who understands their system at a glance and changes a decision because of it.

## Positioning

- Local-first and vendor-neutral: talks the open SunSpec Modbus standard directly on the home network, so it works across inverter brands (SMA, Fronius, SolarEdge, Enphase, Schneider and others) without a manufacturer cloud account.
- No backend, no accounts, no tracking. Forecast is the only network call; share links are base64url snapshots decoded entirely on the client.
- Insight, not just charts: the mechanism is telemetry fused with forecast to produce recommendations.
- Installable PWA that works offline after first load; native iOS and Android apps exist in-repo but are deferred (PWA-first strategy, `.factory/missions/helios-pwa-launch.md`).

## Operating Context

Routes: `/` marketing landing; `/app` the PWA (Dashboard, Production, Insights, Battery, Settings, 5-tab bottom nav, centered phone-width frame on desktop); `/share/:payload` read-only snapshot. `?brand=<id>` on any URL re-skins accent, mark and copy.

The shipped build runs a mock SunSpec service simulating a 9.6 kW hybrid system with three strings (Roof South-East, Roof South-West, Garage South, 8 panels each), a 13.5 kWh battery, a sun-curve production model and a 4.2 kW-peak household load. Real inverters need a small WebSocket relay or local gateway because a browser cannot speak raw TCP.

Theme: light / dark / auto via `data-theme` on `<html>`, set before first paint from `localStorage['helios-theme']`; light theme is designed to be readable in direct sunlight.

## Capabilities and Constraints

Confirmed capabilities: live energy-flow diagram (solar → home ↔ battery ↔ grid) with live wattages; per-string telemetry and inverter health (DC/AC voltage, frequency, heatsink and cabinet temperatures); AI insights (peak production, cloud cover, export active, overnight low, thermal, self-consumption, savings, string imbalance, morning strategy); 7-day production forecast from Open-Meteo shortwave radiation (`expected_kWh = peak_sun_hours × system_kW × 0.82`); battery strategies Self-consumption / Time-of-use / Backup-only with state-of-charge ring and backup-readiness; share deep-links; white-labeling; installable PWA with service worker.

Technical constraints (from repo and mission files): Vite 5 + React 18 + TypeScript strict, Tailwind 3 with CSS custom properties, Zustand, React Router 6, Framer Motion, Recharts, Lucide icons. No new npm dependencies. No third-party trackers or analytics. No emoji anywhere. `prefers-reduced-motion` respected. `?brand=` white-labeling must keep working (accent read from `--signal-solar` / `brand.accent`). Do not touch `ios/`, `android/`, `release/`, `dist/`, `dist-tsc/`. Must pass `npm run typecheck` and `npm run build`.

Terminology: string (a series of panels on one inverter input), state of charge (SoC), self-consumption, time-of-use, feed-in / export, irradiance (W/m²), SunSpec Modbus TCP, inverter status PRODUCING / STANDBY / CURTAILED / NIGHT / FAULT.

Undecided product facts: whether the landing should show store badges for the deferred native apps (an earlier mission asked for "Coming soon" placeholders; the current strategy is PWA-first). Public production URL not yet recorded.

## Brand Commitments

- Name is always written lowercase with the degree sign: **helios°**. Legal name `helios° energy`. Tagline: "Precision energy intelligence for your solar array."
- Mark: the eight-blade aperture/sun mark in `src/components/HeliosMark.tsx` (also `design/helios-icon.svg`, `public/icons/*`). White-label brands use a lettered monogram in a circle.
- Accent: `#f0c674` (dark) / `#b8862e` (light) — exposed as `--signal-solar` and overridden per brand. Signal palette: flow `#7fb069`, grid `#5d8aa8`, battery `#c5a572`, alert `#d97757`.
- Identity: "precision instrument" — carbon (`#070708` → `#1a1a1c`) grounds and bone (`#f4f1ea`, `#dcd6c8`) foregrounds in dark; parchment-and-graphite inversion in light. JetBrains Mono for technical labels, units and measured values; Plus Jakarta Sans display (latest commit) with Inter body.
- Voice: precise, plain, confident, dry; speaks to a homeowner in their own language ("when to run the dishwasher") while staying technically exact. No hype.

## Evidence on Hand

- Real product components that can be rendered on the landing with the mock service: `EnergyFlow`, `BatteryRing`, `ProductionChart`, `ForecastStrip`, `InsightCard`, `MetricTile`, `StatusPill`, `LiveNumber`, `HeliosMark`.
- Mock telemetry (`src/services/sunspec.ts`) and real insight generator (`src/services/aiInsights.ts`) — demonstration data, labeled as simulated in the app (`connection.status: 'simulated'`).
- Store-listing copy: `release/store-listings/app-store.md`, `release/store-listings/play-store.md`.
- Screenshots: `public/mobile-dashboard-viewport.png`, `public/mobile-dashboard-full.png`.
- Absent, must not be fabricated: customer testimonials, named customers, install counts, savings benchmarks, pricing, uptime or accuracy figures, press. The previous landing page invented these and they are not to be reused.

## Product Principles

1. Prove with the real instrument: the product's own live components and real insight logic are the demonstration; no mockups pretending to be the app.
2. Homeowner-first language, engineer-grade precision: every number carries its unit and its source.
3. Local-first is a feature to state plainly, not a badge to shout.
4. Vendor-neutral by standard, not by claim: name the protocol, not competitors' weaknesses.
5. Works where the sun is: readable outdoors, on a phone, in either theme, without motion dependence.

## Accessibility & Inclusion

Light theme readable in direct sunlight on a phone; `prefers-reduced-motion` respected; keyboard-reachable controls and visible focus (established in app conventions). No product-specific standard beyond WCAG AA has been recorded.
