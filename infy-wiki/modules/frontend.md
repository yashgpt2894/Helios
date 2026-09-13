# Frontend Pages & Components

**Summary:** The React frontend pages and UI components render the Helios dashboard views, production charts, energy flow visualizations, battery status, intelligence insights, and system settings.

## How it works

The user interface is structured around page-level components in `src/pages/` that pull telemetry and state from a central Zustand store (`src/store/useStore.ts`) and render modular subcomponents from `src/components/`.

The main entry point is the dashboard view in `src/pages/Dashboard.tsx`, which organizes live metrics, AI insights, weather forecasts, the daily production curve, and summary metric tiles. The energy flow visualization (`src/components/EnergyFlow.tsx`) renders an interactive SVG diagram depicting real-time power routing between solar panels, the home load hub, energy storage, and the grid. 

Production analytics are rendered by `src/pages/Production.tsx`, combining per-string panel metrics with the historical chart component `src/components/ProductionChart.tsx` (built on Recharts). Additional views include `src/pages/Battery.tsx` for energy storage, `src/pages/Insights.tsx` for AI-generated advisories and savings, and `src/pages/Settings.tsx` for SunSpec Modbus connections, location settings, and theme configuration.

## Key files

| File | Role |
| :--- | :--- |
| `src/pages/Dashboard.tsx` | Main dashboard view combining live power, energy flow, forecast strips, production charts, and metric tiles. |
| `src/pages/Production.tsx` | Detailed solar array view displaying string breakdowns, daily production charts, 7-day trends, and telemetry. |
| `src/pages/Insights.tsx` | Intelligence feed rendering AI-generated advisories, production forecasts, and financial savings summaries. |
| `src/pages/Battery.tsx` | Energy storage view showing battery health, backup readiness runtime, round-trip efficiency, and charge modes. |
| `src/pages/Settings.tsx` | System settings view managing SunSpec Modbus connection parameters, forecast locations, and UI themes. |
| `src/components/EnergyFlow.tsx` | SVG component visualizing real-time power flow between solar, home, battery, and grid nodes. |
| `src/components/ProductionChart.tsx` | Recharts-based area chart rendering daily solar production and home consumption curves over a 24-hour axis. |

## Gotchas

None found in the supplied evidence.

## Sources

- [src/pages/Dashboard.tsx](infy://wiki/40d98eee-a3a7-4272-ba0a-285793586ec1/pages/55779491-e3cb-4ad5-a081-ce9d8a51c501) · SHA-256 8afea29bebfeb928f37b942465a110dfeabfa538b668b553330cd2bb4e8a7461
- [src/pages/Production.tsx](infy://wiki/40d98eee-a3a7-4272-ba0a-285793586ec1/pages/a7f75928-b2a3-42d8-97dd-717afcbde05c) · SHA-256 1ec77c2607de3f058a110953f15067038317b11424ed4c87a9ff575f1bbfcbb0
- [src/pages/Insights.tsx](infy://wiki/40d98eee-a3a7-4272-ba0a-285793586ec1/pages/405eda08-9c77-498d-80a4-5ceca34606f4) · SHA-256 eceffb2a976cadfb96e57d4d450ad71456e20eafcafdcaede39229fc224f4637
- [src/pages/Battery.tsx](infy://wiki/40d98eee-a3a7-4272-ba0a-285793586ec1/pages/c8aea50c-a2e3-43b4-9ab0-b849728527a1) · SHA-256 13398bac8f418f777b4e90a8924803b862bbc3c82810e74cf82babf441cca1b1
- [src/pages/Settings.tsx](infy://wiki/40d98eee-a3a7-4272-ba0a-285793586ec1/pages/d1e223b7-51cd-4e9d-8ae0-418cc3e42183) · SHA-256 83a4299346e8273f9d27f182eaa0b60809ece1e9e249197708d836c35ccd5a25
- [src/components/EnergyFlow.tsx](infy://wiki/40d98eee-a3a7-4272-ba0a-285793586ec1/pages/60614557-8173-40c0-b360-5d093d0bd6d9) · SHA-256 dbd1938808d73fb967d8495d3d6afb697ecd2ab1c2ca1f99e629b8d7b38e09d3
- [src/components/ProductionChart.tsx](infy://wiki/40d98eee-a3a7-4272-ba0a-285793586ec1/pages/71145719-0c60-4b99-8c6b-566ff164c482) · SHA-256 8948ee6582ccdf78785850423ba013ba2a3e395ac20470af99d2b5654dcd222c