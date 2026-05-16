# Android Marketing Screenshots Specification

## Overview

Screenshot sets for the three required Android form factors on Google Play. Each set contains 5 scenes. All screenshots share dark carbon background with aurora gradient. Android screenshots emphasize Material Design 3 styling within the Helios visual identity.

## Visual Theme (All Scenes)

- **Background**: Dark carbon (`#070708` to `#0B0B0C` gradient, top to bottom).
- **Aurora overlay**: Solar hue (`#F0C674`) at 6% opacity, radial gradient from top-center.
- **Device frame**: Minimal dark outline (1dp, `#2A2A2D`). No punch-hole camera or notch emphasis. Status bar icons subtle.
- **Typography**: Roboto Flex for all text. Bone (`#F4F1EA`) primary, warm gray (`#A59F90`) secondary.
- **Surface cards**: Material 3 tonal surfaces at elevation 3 with `#1A1A1C` at 90%.
- **Navigation bar**: 5-tab bottom navigation (Dashboard, Production, Insights, Battery, Settings) visible on phone. 2-tone icon treatment: active tab in solar gold, inactive in warm gray.
- **Top app bar**: Centered "helios-degree-sign" mark + brand name, connection status dot on trailing edge.

---

## Size: Phone

**Dimensions**: 1080 x 2400 px  
**Representative devices**: Pixel 9 Pro, Galaxy S25

### Scene 1: Dashboard Hero
- **Hero (top 35%)**: Live kW number "6.8 kW" in Solar gold (`#F0C674`), 64sp. "Generating now" subtitle in body type below. Live number ticker treatment.
- **EnergyFlow (middle 25%)**: 4-node diamond with flowing dots along bezier paths. Solar→Home (gold), Battery↔Home (bronze), Grid→Home (orange import). Compose Canvas rendering.
- **Metric chips (bottom 40%)**: Horizontal scrolling row of 3 elevated surface chips — "Today: 24.3 kWh", "Peak: 7.2 kW", "Self-use: 82%". Material 3 `ElevatedCard` styling.

### Scene 2: Production Strings
- **Header**: "Production" in title-2. "Per-string telemetry" subtitle.
- **Hero kW (top 15%)**: "6.8 kW" with "+1.2 kW vs yesterday" delta in Flow green.
- **Production chart (middle 35%)**: Stacked area — solar in gold fill, home in bone dashed. Time axis 6am–6pm. Vertical "now" indicator line. Material 3 surface container.
- **String cards (bottom 50%)**: Vertical scrollable list. Each card shows string label, power, voltage, current, and a linear progress indicator showing % of rated power. 2 cards visible in viewport.

### Scene 3: Insights Forecast
- **Header**: "Insights" with Material 3 filter chip for severity.
- **Featured insight (top 30%)**: `ElevatedCard` with 4dp colored left border in solar gold. Shows insight title and 2-line body.
- **Forecast row (middle 20%)**: `LazyRow` of 7 forecast day chips. Each chip: abbreviated day, weather icon (Material symbol mapped to condition), kWh value, micro progress bar.
- **Savings grid (bottom 50%)**: 2x2 grid of `ElevatedCard` items. Month savings ($47.20), lifetime ($3,842), CO2 (6.2 tons), grid independence (84%). Each with label above, value prominent, subtle delta where applicable.

### Scene 4: Battery Ring
- **Header**: "Battery" with SoC chip showing "78%".
- **BatteryRing (center, 45% height)**: Compose Canvas arc, counterclockwise fill from 12 o'clock. Bronze/battery hue. 78% fill with glow on arc. Center: "78%" in large bone text, "State of Charge" below.
- **Power status (below ring)**: "Charging at 2.1 kW" with Flow green `AssistChip`.
- **Metrics (bottom)**: Row of 3 `ElevatedCard` tiles — Temperature (31C), Cycles (847), Backup (14.2 hrs). Backup tile with green tint.

### Scene 5: Settings Connection
- **Header**: "Settings".
- **Connection card (top 35%)**: `ElevatedCard` showing connection details. Protocol chip ("Modbus TCP"), host ("192.168.1.150:502"), unit ID ("1"), poll interval ("5s"). Green "Connected" `AssistChip`.
- **Location card (middle 25%)**: Shows location label ("San Jose, CA") with coordinates. "Use My Location" switch (Material 3 `Switch`, on).
- **Preferences (bottom 40%)**: Preference list items — Theme (showing "Auto" with trailing icon), Brand ("helios"), App version ("1.0.0"). Material 3 `ListItem` styling.

---

## Size: Foldable

**Dimensions**: 1768 x 2208 px  
**Representative devices**: Pixel Fold 2, Galaxy Z Fold 6 (unfolded inner display)

Same 5 scenes, adapted for the larger unfolded canvas:

### Scene 1: Dashboard Hero
- **Layout**: Two-column. Left (40%): Live kW + EnergyFlow stacked. Right (60%): Production chart with 6 metric tiles in 3x2 grid.
- **Metric tiles**: Today (24.3 kWh), Peak (7.2 kW), Self-use (82%), Grid Import (3.1 kWh), Grid Export (12.4 kWh), CO2 Saved (18.2 kg).
- **Forecast strip**: 7 days visible in horizontal chip row at bottom.

### Scene 2: Production Strings
- **Layout**: Two-column. Left (35%): Live kW + per-string detail cards (2 visible, stacked). Right (65%): Day production chart + week comparison grouped bar chart.
- **Inverter grid**: 2x3 grid of small stat tiles (Status, Temp, Freq, DC V, AC V, Efficiency).

### Scene 3: Insights Forecast
- **Layout**: Two-column. Left (50%): Featured insight + 2 insight cards stacked. Right (50%): 7-day forecast vertical list with rich detail — weather icon, temp range, kWh bar, vs-average.
- **Savings**: 2x2 grid below the two columns.

### Scene 4: Battery Ring
- **Layout**: Two-column. Left (45%): BatteryRing + power status. Right (55%): Battery stats stack + charge mode selector + 24h SOC trend sparkline.

### Scene 5: Settings Connection
- **Layout**: Two-column with left navigation rail (20%) and right content (80%).
- **Nav rail**: Icon-only navigation items for Connection, Location, Appearance, Brand, About.
- **Content area**: Full form for the selected section, matching the phone layout but with more vertical space.

---

## Size: Tablet

**Dimensions**: 1600 x 2560 px  
**Representative devices**: Pixel Tablet, Galaxy Tab S10

Same layout strategy as foldable, with tablet-specific refinements:

### Scene 1: Dashboard Hero
- **Layout**: Two-column, wider left column (45%). Larger EnergyFlow (fills left column). 3x2 metric tile grid.
- **Forecast**: 7-day chips at bottom, fully visible without scrolling.

### Scene 2: Production Strings
- **Layout**: Two-column with 3 string cards visible in the left column (more vertical space). Right column: stacked day + week charts.

### Scene 3: Insights Forecast
- **Layout**: Sidebar (25%) with category filter. 3x2 insight card grid in main content area. Detailed forecast list at the bottom.

### Scene 4: Battery Ring
- **Layout**: Two-column. BatteryRing proportionally larger to fill left column. Right column has more vertical space for expanded stats and charge mode details.

### Scene 5: Settings Connection
- **Layout**: Navigation rail (persistent, 20%) + content (80%). Full-width connection form with additional "Advanced" section (timeout, retry, TLS toggle).

---

## Screenshot Capture Notes

1. Use Android Emulator at 100% scale with GPU acceleration enabled.
2. Set system to Dark theme.
3. Set display size to "Default" and font size to "Default" in device settings.
4. Status bar: time at "9:41" (consistent with iOS screenshots), battery 85%+, Wi-Fi connected, no carrier on tablets.
5. For foldable screenshots, use the unfolded (inner) display only. Do not include outer cover display screenshots unless Google Play specifically requests them.
6. All text must use real data values. No "Lorem ipsum" or placeholder copy.
7. Material You dynamic color should be disabled — Helios uses its own color system, not system dynamic color.
8. Export as PNG-24, flattened, sRGB color space, no alpha channel.
