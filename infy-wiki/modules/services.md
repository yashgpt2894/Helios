# Services & Telemetry

**Summary:** This layer manages live solar inverter simulation, weather forecasting, heuristic AI insights, brand customization, and URL-based telemetry sharing.

## How it works

The services layer combines hardware simulation, third-party API integration, and utility functions to power the application state. In `src/services/sunspec.ts`, simulated SunSpec inverter telemetry is generated using solar curves, string orientations, and battery charging models to return real-time metrics such as AC/DC power, panel voltages, and battery state-of-charge. Weather forecasting is handled in `src/services/weather.ts` by fetching daily forecasts from the Open-Meteo API, supporting geolocation lookups and converting radiation data into expected kWh yields. Operational insights and financial savings are computed in `src/services/aiInsights.ts` by evaluating telemetry conditions like thermal thresholds, cloud cover, and peak production. Additionally, `src/services/brand.ts` manages white-label configurations and accent styling, while `src/services/share.ts` encodes telemetry snapshots into URL-safe base64 strings for native sharing or clipboard copying.

## Key files

| File | Role |
| :--- | :--- |
| `src/services/sunspec.ts` | Simulates SunSpec inverter telemetry, string panels, historical series, and energy totals. |
| `src/services/weather.ts` | Fetches Open-Meteo weather forecasts and handles browser geolocation and reverse geocoding. |
| `src/services/aiInsights.ts` | Generates heuristic operational insights, warnings, and savings estimates based on telemetry. |
| `src/services/brand.ts` | Resolves white-label brand definitions, taglines, and dynamic accent colors. |
| `src/services/share.ts` | Encodes and decodes telemetry snapshot payloads for shareable URLs and clipboard operations. |

## Gotchas

None found in the supplied evidence.

## Sources

- [src/services/sunspec.ts](infy://wiki/40d98eee-a3a7-4272-ba0a-285793586ec1/pages/f1956bfb-fcfe-41cd-91e8-a81aa4c5d1d1) · SHA-256 419becc5485ff3d8d6b4ae2da999e01b7485395696a5e6ca6d7fbe6337ac0206
- [src/services/weather.ts](infy://wiki/40d98eee-a3a7-4272-ba0a-285793586ec1/pages/ecd4f670-add1-4e04-81ed-eb10b749cbd1) · SHA-256 8b4ec9ca8b8252f6b67d81e100d2502eefe25ab126cafa9ccd53ce9211d462de
- [src/services/aiInsights.ts](infy://wiki/40d98eee-a3a7-4272-ba0a-285793586ec1/pages/d8608956-b6da-42ff-a9db-ffe69e816741) · SHA-256 224dd494897e39f26c0fe7aae1795ceaa903128e4c577e3d60a936fd8555c015
- [src/services/brand.ts](infy://wiki/40d98eee-a3a7-4272-ba0a-285793586ec1/pages/c1cc710a-e2fb-4809-95f4-7b1fc7a81d40) · SHA-256 51a4548f4a70d1e2c261c4cfb4ead277b3fe6e0e4a5b54e62204e0a5cf6de827
- [src/services/share.ts](infy://wiki/40d98eee-a3a7-4272-ba0a-285793586ec1/pages/82bfc9cd-45f5-43da-852e-b0b2391e96d9) · SHA-256 a8afeb6662600de5d76f1621982adfed51d2f701caf22625b2fff87e152fc0e0