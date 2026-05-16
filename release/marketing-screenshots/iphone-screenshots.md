# iPhone Marketing Screenshots Specification

## Overview

Screenshot sets for the three required iPhone display sizes on the App Store. Each set contains 5 scenes. All screenshots share a consistent dark carbon background with aurora gradient.

## Visual Theme (All Scenes)

- **Background**: Dark carbon (`#070708` to `#0B0B0C` gradient, top to bottom).
- **Aurora overlay**: Solar hue (`#F0C674`) at 6% opacity, radial gradient from top-center fading to transparent.
- **Device frame**: Minimal bezel — thin dark outline (1pt, `#2A2A2D`). No notch or Dynamic Island emphasis. The frame should be barely visible against the dark screenshot background.
- **Typography**: SF Pro Display for headlines, SF Pro Text for body. Bone (`#F4F1EA`) for primary text, warm gray (`#A59F90`) for secondary.
- **Grid pattern**: Subtle dot grid (1px dots at 24px intervals, `#EFECE5` at 4% opacity) on content areas.

---

## Size: iPhone 6.7" Display

**Dimensions**: 1290 x 2796 px  
**Models**: iPhone 16 Pro Max, iPhone 15 Pro Max, iPhone 14 Pro Max

### Scene 1: Dashboard Hero
- **Hero section (top 40%)**: Live kW number displayed large — "6.8 kW" in Solar gold (`#F0C674`), hero size equivalent (68pt). Below it: "Generating now" subtitle. Live number ticker animation implied.
- **EnergyFlow diagram (middle 25%)**: Abstracted 4-node diamond energy flow showing Solar→Home (gold path), Battery↔Home (bronze path), and Grid→Home (orange path). Flowing dots along paths.
- **Metric tiles (bottom 35%)**: Row of 3 tiles — "Today" (24.3 kWh), "Peak" (7.2 kW), "Self-use" (82%). Each tile: rounded card (radius 16pt) with carbon surface background (`#1A1A1C` at 90% opacity), bone text, solar-gold accent value.
- **Status**: Top-left corner shows "helios-degree-sign" mark + "Connected" status pill (green dot, Flow 500).

### Scene 2: Production Strings
- **Header**: "Production" title, subtitle "Per-string telemetry".
- **Hero kW (top 20%)**: "6.8 kW" large, with delta "+1.2 vs yesterday".
- **Production chart (middle 40%)**: Stacked area chart — solar production in solar gold (`#F0C674` at 45% opacity fill), home consumption in bone (`#F4F1EA` at 15% dashed). Time axis: 6am–6pm. Vertical "now" line at simulated current time.
- **String cards (bottom 40%)**: 3–4 per-string cards in a scroll view. Each shows: string label ("String 1 — Roof South"), power (2.4 kW), voltage (385V), current (6.2A). Small bar showing % of rated power.
- **Inverter status**: Green status pill "PRODUCING" at top.

### Scene 3: Insights Forecast
- **Header**: "Insights" with severity dot indicator (positive = green).
- **Featured insight (top 30%)**: Elevated card with severity-colored left border (solar gold). Title: "Perfect solar day ahead". Body: "Tomorrow's clear skies will deliver an estimated 32.4 kWh -- 18% above your weekly average."
- **7-day forecast strip (middle 25%)**: Horizontal scroll of 7 day cards. Each card: day label ("Mon"), weather icon (sun/cloud/rain), expected kWh ("32.4"), micro-bar showing relative production.
- **Savings grid (bottom 45%)**: 2x2 grid of savings cards — "This month: $47.20", "Lifetime: $3,842", "CO2 offset: 6.2 tons", "Grid independence: 84%".

### Scene 4: Battery Ring
- **Header**: "Battery" with SOC percentage badge "78%".
- **BatteryRing hero (center, 50% of height)**: Large counterclockwise ring indicating 78% State of Charge. Ring color: bronze/battery hue (`#856B32` at 78%, fading to `#251D0D` at 0%). Glow on the filled arc. Center of the ring shows "78%" in large bone text, "State of Charge" below.
- **Power flow (below ring)**: Battery power indicator — "Charging at 2.1 kW" with Flow green accent dot.
- **Metrics row (bottom)**: 3 tiles — "Temperature" (31C), "Cycles" (847), "Backup" (14.2 hrs estimated). Backup tile highlighted with a subtle green tint when above 12 hours.

### Scene 5: Settings Connection
- **Header**: "Settings" with gear indicator.
- **Connection card (top 40%)**: Carbon surface card showing active connection details. Protocol badge ("Modbus TCP"), host IP ("192.168.1.150:502"), unit ID ("1"), poll interval ("5s"). Green "Connected" status pill.
- **Location card (middle 25%)**: Shows resolved location label ("San Jose, CA") with lat/lng coordinates. "Use My Location" toggle (on). Source: "GPS".
- **Preferences (bottom 35%)**: List of preference rows — Theme ("Auto" with moon/sun indicators), Brand ("helios"), App version ("1.0.0 build 1"). Each row with label on left, value on right, chevron indicator.

---

## Size: iPhone 6.1" Display

**Dimensions**: 1179 x 2556 px  
**Models**: iPhone 16, iPhone 15, iPhone 14

Same 5 scenes as 6.7" with proportional scaling:
- Hero kW text: 62pt (vs 68pt on 6.7")
- Metric tiles: slightly narrower, same 112pt minimum height
- BatteryRing: proportionally smaller but same visual weight
- String cards: 2 visible instead of 3 in viewport
- Forecast strip: 5 days visible instead of 7

---

## Size: iPhone 5.5" Display

**Dimensions**: 1242 x 2208 px  
**Models**: iPhone 8 Plus, iPhone 7 Plus (required by App Store for legacy support)

Same 5 scenes scaled down:
- Hero kW text: 52pt
- Bottom tab bar visible (5-tab layout: Dashboard, Production, Insights, Battery, Settings)
- Metric tiles: 2 per row instead of 3
- Production chart: compact, single-day view
- BatteryRing: occupies 40% of screen height
- String cards: 1–2 visible
- Forecast strip: 4 days visible

---

## Screenshot Capture Notes

1. Use the iOS Simulator at 100% scale.
2. Set simulator to Dark Mode.
3. Enable "Show Device Bezel" in Simulator > Window menu, but use a custom bezel overlay that is subtle (1pt dark outline only).
4. All text must use real layout data — no lorem ipsum. Use plausible solar telemetry values.
5. No status bar time should show "9:41" (Apple's preferred screenshot time).
6. Battery indicator at 85%+, Wi-Fi connected, no carrier text.
7. Export as PNG-24, no alpha channel. App Store requires flattened PNGs.
