# iPad Marketing Screenshots Specification

## Overview

A single iPad screenshot set at the required 13" display size. iPad screenshots should showcase the expanded layout that takes advantage of the larger canvas — multi-column views, sidebars, and richer data density.

## Visual Theme

- **Background**: Dark carbon (`#070708` to `#0B0B0C` gradient, top to bottom).
- **Aurora overlay**: Solar hue (`#F0C674`) at 6% opacity, radial gradient from top-center.
- **Device frame**: Minimal — thin dark outline (1pt, `#2A2A2D`). No bezel emphasis.
- **Typography**: SF Pro Display for headlines, SF Pro Text for body. Bone (`#F4F1EA`) primary, warm gray (`#A59F90`) secondary.
- **Layout**: Two-column where appropriate. iPad gets a persistent sidebar on certain screens.

---

## Size: iPad 13" Display

**Dimensions**: 2048 x 2732 px  
**Models**: iPad Pro 13" (M4), iPad Pro 12.9" (M2/M1)

### Scene 1: Dashboard Hero
- **Layout**: Two-column. Left column (40%): Live kW hero number ("6.8 kW", 84pt), EnergyFlow diagram below. Right column (60%): Production chart (stacked area, full day) with metric tiles in a 3x2 grid below.
- **Metric tiles (6 tiles)**: Today (24.3 kWh), Peak (7.2 kW), Self-use (82%), Grid import (3.1 kWh), Grid export (12.4 kWh), CO2 saved (18.2 kg).
- **Forecast strip**: Horizontal scroll across the bottom, 7 days visible.
- **TopBar**: Full-width with brand mark, connection status, clock on the trailing edge.

### Scene 2: Production Strings
- **Layout**: Two-column. Left (35%): Live kW + 3 string cards stacked vertically (more room for per-string detail). Right (65%): Production chart with full-day view, plus week comparison chart (grouped bar: produced vs consumed, Mon–Sun).
- **String cards**: Full detail — string name, power, voltage, current, panels count, rated power, performance % bar. Each card uses the full column width.
- **Inverter telemetry grid**: 2x3 grid — Status, Temperature, Frequency, DC Voltage, AC Voltage, Efficiency %.
- **Bottom strip**: Export toggle for CSV/JSON data.

### Scene 3: Insights Forecast
- **Layout**: Sidebar + content. Left sidebar (25%): Insight categories filter (Production, Consumption, Battery, Savings, Maintenance, Forecast). Right content (75%): Featured insight card at top, then 4 insight cards in a 2x2 grid below.
- **Featured insight**: Full-width elevated card with severity-colored left border. Title, body, metric with delta. Action button ("View forecast").
- **Insight cards (4)**: Category icon, severity dot, title, body. Each card 2 lines of body text visible.
- **Forecast detail**: 7-day vertical list with weather icon, temp range, expected kWh bar, vs-average % indicator.

### Scene 4: Battery Ring
- **Layout**: Two-column. Left column (45%): Large BatteryRing (counterclockwise, 78% SOC) with SOC % in center. Below the ring: charging/discharging power indicator, time-to-full/time-to-empty estimate. Right column (55%): Battery stats in a vertical stack — Temperature (31C), Cycles (847), State of Health (96%), Backup Readiness (14.2 hrs), Charge Mode selector (Self-consumption / Time-of-use / Backup priority).
- **Charge mode selector**: 3 segmented options with current selection highlighted in battery bronze.
- **History sparkline**: Small 24-hour battery SOC trend line at the bottom of the right column.

### Scene 5: Settings Connection
- **Layout**: Sidebar + content. Left sidebar (25%): Settings sections — Connection, Location, Appearance, Brand, About, Data. Right content (75%): Expanded form for the selected section.
- **For "Connection" section**: Protocol picker (Modbus TCP / Sungrow / SMA / Fronius), host and port fields, unit ID field, poll interval slider (2s–60s), "Test Connection" button, live connection log at the bottom.
- **For "Location" section**: Map preview showing pinned location, coordinates display, "Use My Location" toggle, manual lat/lng entry fields, Open-Meteo attribution.
- **For "Appearance" section**: Theme picker (Auto/Light/Dark) with live preview thumbnails. Preview shows a miniature Dashboard card in each theme.
- **Status bar**: "Connected" status pill visible in top bar across all screenshots.

---

## Screenshot Capture Notes

1. Use iPad Simulator at 100% scale (no scaling artifacts).
2. Set simulator to Dark Mode.
3. Use the 13" iPad Pro simulator for the largest canvas.
4. All text must use real data values. No placeholder text.
5. Status bar: time at "9:41", battery 85%+, Wi-Fi connected, no carrier.
6. iPad split-view and slide-over should not be visible in screenshots — full-screen app only.
7. Export as PNG-24, flattened, no alpha channel.
