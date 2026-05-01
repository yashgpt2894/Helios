<div align="center">

# helios°

**Precision energy intelligence for residential solar arrays.**

A mobile-first Progressive Web App with AI-driven insights, live telemetry from any SunSpec-compatible inverter, and a precision-instrument visual identity.

[![Vite](https://img.shields.io/badge/Vite-5-646cff?logo=vite&logoColor=white)](https://vitejs.dev)
[![React](https://img.shields.io/badge/React-18-61dafb?logo=react&logoColor=black)](https://react.dev)
[![TypeScript](https://img.shields.io/badge/TypeScript-strict-3178c6?logo=typescript&logoColor=white)](https://www.typescriptlang.org)
[![Tailwind](https://img.shields.io/badge/Tailwind-3-38bdf8?logo=tailwindcss&logoColor=white)](https://tailwindcss.com)
[![PWA](https://img.shields.io/badge/PWA-installable-5a0fc8)](https://web.dev/progressive-web-apps/)

</div>

---

## What it is

helios° is a Progressive Web App that turns raw inverter telemetry into something a homeowner can actually use. It reads SunSpec Modbus data from your inverter, fuses it with a real solar-irradiance forecast, and produces concrete recommendations: when to run the dishwasher, when to pre-charge the battery, which string is shading. It runs on phones and tablets, installs to the home screen, and works offline after first load.

It is **mobile-first but renders cleanly on desktop browsers** — the app shell is centered in a precision-instrument frame at wider breakpoints rather than stretched across the viewport.

## Highlights

- **Live energy-flow visualization** — animated solar → home ↔ battery ↔ grid hub with live wattages.
- **AI insights, not just charts** — peak-production windows, cloud warnings, string-imbalance, thermal alerts, savings tracking, multi-day battery strategy.
- **7-day production forecast** — Open-Meteo shortwave-radiation modeling, no API key required.
- **Per-string telemetry** — utilization bars, full inverter health (DC/AC voltage, frequency, heatsink/cabinet temps).
- **Battery strategy** — Self-consumption, Time-of-use, or Backup-only modes with state-of-charge ring and backup-readiness estimate.
- **Light/dark/auto theme** — CSS variables, no flash of wrong theme on load.
- **Share deep-links** — base64url-encoded snapshots, fully client-side, no backend.
- **White-labeling** — `?brand=voltcraft` switches accent color, mark, and copy. Add brands in one file.
- **Installable** — PWA manifest + service worker, runs offline, maskable icons for iOS/Android.

## Routes

| Path                | Purpose                                                                 |
|---------------------|-------------------------------------------------------------------------|
| `/`                 | Marketing landing page — hero, features, CTA.                           |
| `/app`              | Full PWA experience (dashboard, production, insights, battery, settings). |
| `/share/:payload`   | Read-only shared snapshot — `:payload` is base64url JSON, decoded client-side. |

Add `?brand=<id>` to any URL for white-labeling. Out of the box: `helios`, `voltcraft`, `sunworks`, `meridian`.

## Tech stack

| Concern            | Tool                                                                 |
|--------------------|----------------------------------------------------------------------|
| Build / HMR        | Vite 5 with `vite-plugin-pwa`                                        |
| Framework          | React 18 + TypeScript (strict)                                       |
| Styling            | Tailwind CSS, CSS custom properties for theming                      |
| State              | Zustand                                                              |
| Routing            | React Router 6                                                       |
| Animations         | Framer Motion                                                        |
| Charts             | Recharts                                                             |
| Icons              | Lucide React                                                         |
| Forecast           | [Open-Meteo](https://open-meteo.com) — free, no API key              |
| Inverter protocol  | SunSpec Modbus TCP (mock service ships by default)                   |

## Quick start

Requires Node 18+.

```bash
git clone https://github.com/<your-username>/helios-app.git
cd helios-app
npm install
npm run dev      # http://localhost:5173
```

Other scripts:

```bash
npm run build      # production build + service worker
npm run preview    # serve the production build locally
npm run typecheck  # tsc --noEmit
```

## Connection

The MVP ships with **SunSpec Modbus TCP** as the default protocol — the industry-standard interface supported by the vast majority of inverters (SMA, Fronius, SolarEdge, Enphase, Schneider, etc.). The included mock service simulates a realistic 9.6 kW hybrid system with three strings, a 13.5 kWh battery, and a sun-curve-driven production model.

To wire up a real inverter, swap [src/services/sunspec.ts](src/services/sunspec.ts) for a `node-modbus` or `jsmodbus` reader that returns the same `SolarTelemetry` shape.

## Forecast

Solar production forecasting uses [Open-Meteo](https://open-meteo.com) — a free, no-API-key weather service with shortwave-radiation-sum data ideal for PV modeling. Each day's expected kWh is computed as:

```
expected_kWh = peak_sun_hours × system_rating_kW × performance_ratio (0.82)
```

The store refetches hourly. Location defaults to a fixed point; tapping **Use my location** in Insights or Settings triggers `navigator.geolocation` and reverse-geocodes via Open-Meteo's geocoder. Coordinates stay on-device except for the API call.

## Share deep-links

The Dashboard has a small share button next to the status pill. Tapping it builds a `SnapshotPayload`, base64url-encodes it, and either calls `navigator.share()` (mobile) or copies the URL to clipboard. Visiting the URL on any device renders a polished read-only view with the same brand as the original. URLs decode entirely on the client — no backend, no database, no tracking.

## White-labeling

Out of the box: `helios` (default), `voltcraft`, `sunworks`, `meridian`. Each brand has a name, accent color (light + dark variants), legal name, tagline, and either the helios mark or a text-based monogram in a colored circle.

The brand persists into share-snapshot URLs so customers see the installer's brand when sharing. Add new brands in [src/services/brand.ts](src/services/brand.ts) — no rebuild required, just push and the URL works.

## Project structure

```
helios-app/
├── src/
│   ├── App.tsx                  # router (/, /app, /share/:payload)
│   ├── MainApp.tsx              # main app shell with desktop frame
│   ├── pages/
│   │   ├── Landing.tsx          # marketing page
│   │   ├── Dashboard.tsx        # live energy flow, today's curve
│   │   ├── Production.tsx       # per-string + inverter telemetry
│   │   ├── Insights.tsx         # AI advisories + 7-day forecast
│   │   ├── Battery.tsx          # SOC ring, charge mode
│   │   ├── Settings.tsx         # connection, theme, location
│   │   └── SharedView.tsx       # read-only /share/:payload view
│   ├── components/              # EnergyFlow, ProductionChart, ShareButton, ...
│   ├── services/
│   │   ├── sunspec.ts           # mock Modbus reader
│   │   ├── weather.ts           # Open-Meteo forecast
│   │   ├── aiInsights.ts        # live-telemetry rules
│   │   ├── forecastInsights.ts  # multi-day rules
│   │   ├── brand.ts             # white-label registry
│   │   └── share.ts             # snapshot encode/decode
│   ├── store/useStore.ts        # Zustand
│   ├── lib/                     # theme, format helpers
│   ├── types/index.ts           # SolarTelemetry, Brand, SnapshotPayload, ...
│   └── index.css                # CSS variables, theme overrides
├── public/                      # PWA icons, favicon
├── vite.config.ts               # PWA manifest, font caching
└── tailwind.config.ts           # carbon/bone palette
```

## PWA

Open in any Chromium-based browser → install icon in the address bar, or **Add to Home Screen** on iOS Safari. The app runs offline after first load; live telemetry is the only thing that requires a connection.

## Deployment

The build output (`dist/`) is a fully static site — works on any static host:

- **Vercel / Netlify** — zero-config, just point at the repo. Set the SPA fallback to `index.html` so `/app` and `/share/:payload` resolve.
- **Cloudflare Pages** — same. Build command `npm run build`, output `dist`.
- **GitHub Pages** — works with a 404 fallback hack, but Vercel/Netlify is simpler.

For SunSpec connectivity in production you'll want a small WebSocket relay or a local-network gateway — the browser cannot speak raw TCP.

## Design

Carbon (`#070708` → `#1a1a1c`) backgrounds with bone (`#f4f1ea`, `#dcd6c8`) accents. Typography pairs **Instrument Serif** for display numerics, **JetBrains Mono** for technical labels and units, **Inter** for body. Built mobile-first with a 5-tab bottom nav, safe-area-aware layout, and reduced-motion friendly transitions.

Light theme is a parchment-and-graphite inversion — same precision-instrument feel, designed to be readable in direct sunlight on a phone.

## License

MIT — do what you want, but don't blame us if your inverter catches fire.
