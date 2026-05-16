# Helios PWA → Native Feature-Parity Matrix

## 1. Routes / Screens

| PWA Route | Native Screen (iOS) | Native Screen (Android) | Notes |
|-----------|---------------------|-------------------------|-------|
| `/` Landing | `LandingView` (SwiftUI) | `LandingScreen` (Compose) | Marketing page; only shown on first launch or via deep-link. In native apps, becomes the 4-screen onboarding + cinematic intro. |
| `/app` (Dashboard tab) | `DashboardView` | `DashboardScreen` | Primary screen. Hero: live kW + EnergyFlow + metric tiles + featured insight + forecast strip + production chart. |
| `/app` (Production tab) | `ProductionView` | `ProductionScreen` | Generating now kW + ProductionChart + per-string cards + WeekChart + inverter telemetry grid. |
| `/app` (Insights tab) | `InsightsView` | `InsightsScreen` | AI insight highlight + 7-day forecast card + savings grid + full insight feed. |
| `/app` (Battery tab) | `BatteryView` | `BatteryScreen` | BatteryRing hero + power/temp/cycles grid + backup readiness + charge mode selector. |
| `/app` (Settings tab) | `SettingsView` | `SettingsScreen` | Connection config + location + theme picker + brand chooser + app preferences. |
| `/share/:payload` | `SharedView` | `SharedScreen` | Read-only snapshot decoder. Universal link + custom scheme handler. |

## 2. Components

| PWA Component | iOS Equivalent | Android Equivalent | Key Behaviors to Port |
|---------------|----------------|--------------------|----------------------|
| `EnergyFlow` | `EnergyFlowView` (Canvas + TimelineView) | `EnergyFlowComposable` (Canvas) | 4-node bezier graph: Solar→Home, Battery↔Home, Grid↔Home. Animated dot trails along paths. Density modulates with wattage. |
| `BatteryRing` | `BatteryRingView` (Shape + trim) | `BatteryRingComposable` (Canvas arc) | Counterclockwise fill, glow on charge, pulse on discharge. Threshold haptics at 80/50/20%. |
| `ProductionChart` | `ProductionChart` (Swift Charts) | `ProductionChart` (Compose Canvas / custom) | Stacked area: solar (solid) + home (dashed). Time-of-day vertical line. Tooltip on scrub. |
| `WeekChart` | `WeekChart` (Swift Charts) | `WeekChart` (Compose) | Grouped bar: produced vs consumed. 7 days. |
| `ForecastCard` | `ForecastCard` (List + bars) | `ForecastCard` (LazyColumn item) | 7-day vertical list with weather icon, temp, expected kWh bar. Staggered entrance. |
| `ForecastStrip` | `ForecastStrip` (HStack) | `ForecastStrip` (LazyRow) | 5-day horizontal grid: day label, weather icon, kWh, micro-bar. |
| `InsightCard` | `InsightCard` (VStack) | `InsightCard` (Card) | Category icon, severity dot, title, body, optional action. Swipe-to-dismiss on iOS. |
| `InsightHighlight` | `InsightHighlight` (elevated card) | `InsightHighlight` (ElevatedCard) | Featured insight with severity-colored ring. Scale entrance. |
| `MetricTile` | `MetricTile` (VStack) | `MetricTile` (Surface) | Label + value + unit + delta/subtle. Uniform 112pt min height. |
| `LiveNumber` | `LiveNumber` (`.contentTransition(.numericText)`) | `LiveNumber` (AnimatedContent per-digit) | Ticker animation on value change. 0.8s spring. |
| `StatusPill` | `StatusPill` (Capsule) | `StatusPill` (AssistChip) | Status dot + label. Pulsing dot animation. |
| `ShareButton` | `ShareButton` (ActivityView / URL copy) | `ShareButton` (ShareSheet / clipboard) | Builds SnapshotPayload v1, base64url encodes, shares. Success haptic on copy. |
| `TopBar` | `TopBar` (toolbar) | `TopBar` (TopAppBar) | Brand mark + name + connection dot + clock + theme toggle. |
| `BottomNav` | `BottomNav` (TabView / custom) | `BottomNav` (NavigationBar) | 5 tabs: Home, Solar, Insights, Battery, Settings. Spring layout animation on selection. |
| `HeliosMark` | `HeliosMark` (SwiftUI Shape) | `HeliosMark` (Compose Canvas) | 8-blade radial mark for helios brand; text circle for white-label brands. |
| `WeatherIcon` | `WeatherIcon` (SF Symbol fallback) | `WeatherIcon` (Material Icon fallback) | 10 condition mappings to icon + color. |
| `SectionHeader` | `SectionHeader` (VStack) | `SectionHeader` (Column) | Eyebrow label + title + optional trailing + description. |

## 3. State / Store Actions

| Zustand State (useStore.ts) | iOS (@Observable Repository) | Android (ViewModel + StateFlow) | Notes |
|-----------------------------|------------------------------|---------------------------------|-------|
| `page` | `NavigationState.selectedTab` | `NavController.currentDestination` | 5-tab bottom navigation. |
| `telemetry` | `TelemetryRepository.telemetry` (async stream) | `TelemetryRepository.telemetryFlow` | `readTelemetry()` every `pollIntervalMs`. |
| `todaySeries` | `TelemetryRepository.todaySeries` | `TelemetryRepository.todaySeriesFlow` | 48 half-hour points built by `buildTodaySeries()`. |
| `liveSeries` | `TelemetryRepository.liveSeries` (circular buffer, 180 pts) | `TelemetryRepository.liveSeriesFlow` | Appended on each tick, sliced to last 180. |
| `connection` | `ConnectionConfig` (SwiftData) | `ConnectionConfigEntity` (Room) | Protocol, host, port, unitId, pollIntervalMs, status. |
| `updateConnection` | `ConnectionRepository.update()` | `ConnectionRepository.update()` | Partial patch applied to stored config. |
| `tick` | `TelemetryRepository.tick()` | `TelemetryRepository.tick()` | Calls `readTelemetry()`, appends to liveSeries. |
| `location` | `Location` (SwiftData / UserDefaults) | `LocationEntity` (Room) | Lat, lng, label, source. |
| `forecast` | `ForecastRepository.forecast` | `ForecastRepository.forecastFlow` | Loaded by `fetchForecast()`. |
| `forecastStatus` | `ForecastRepository.status` | `ForecastRepository.statusFlow` | idle/loading/ready/error. |
| `forecastError` | `ForecastRepository.error` | `ForecastRepository.errorFlow` | Human-readable string. |
| `loadForecast` | `ForecastRepository.load()` | `ForecastRepository.load()` | Fetches Open-Meteo, parses, stores. |
| `useMyLocation` | `LocationRepository.useMyLocation()` | `LocationRepository.useMyLocation()` | CoreLocation / FusedLocationProvider → reverse geocode → fetch forecast. |
| `theme` | `ThemePreference` (SwiftData / AppStorage) | `ThemePreference` (DataStore) | auto/light/dark. |
| `setTheme` | `ThemeRepository.set()` | `ThemeRepository.set()` | Persists + applies. |
| `cycleTheme` | `ThemeRepository.cycle()` | `ThemeRepository.cycle()` | dark → light → auto. |
| `brand` | `BrandPreference` (SwiftData) | `BrandPreference` (DataStore) | Resolved from launch URL or default. |
| `setBrandFromSearch` | `BrandRepository.resolve(from:)` | `BrandRepository.resolve(from:)` | Parses query param / deep-link. |

## 4. Services / Business Logic

| PWA Service | iOS Service | Android Service | Port Notes |
|-------------|-------------|-----------------|------------|
| `sunspec.ts` — `readTelemetry()` | `SunspecService.readTelemetry()` | `SunspecService.readTelemetry()` | Byte-for-byte port: solar curve, jitter, battery charge/discharge, grid residual, panel string builder, status derivation. |
| `sunspec.ts` — `buildTodaySeries()` | `SunspecService.buildTodaySeries()` | `SunspecService.buildTodaySeries()` | 48 half-hour points with isPast logic. |
| `sunspec.ts` — `buildWeekSeries()` | `SunspecService.buildWeekSeries()` | `SunspecService.buildWeekSeries()` | 7-day random-walk simulation. |
| `weather.ts` — `fetchForecast()` | `WeatherService.fetchForecast()` | `WeatherService.fetchForecast()` | Open-Meteo API call. Same param builder. Same PSH formula. |
| `weather.ts` — `reverseGeocode()` | `GeocodingService.reverseGeocode()` | `GeocodingService.reverseGeocode()` | Open-Meteo geocoder fallback to lat,lng. |
| `weather.ts` — `mapWeatherCode()` | `WeatherCodeMapper.map()` | `WeatherCodeMapper.map()` | 15-code branching table. |
| `aiInsights.ts` — `generateInsights()` | `InsightsEngine.generate(from:)` | `InsightsEngine.generate()` | 8 insight templates with severity/category logic. |
| `aiInsights.ts` — `computeSavings()` | `InsightsEngine.computeSavings()` | `InsightsEngine.computeSavings()` | Grid rate × energy with month/lifetime multipliers. |
| `forecastInsights.ts` — `generateForecastInsights()` | `InsightsEngine.generateForecast()` | `InsightsEngine.generateForecast()` | 6 forecast-based insight templates. |
| `brand.ts` — `resolveBrand()` | `BrandService.resolve()` | `BrandService.resolve()` | 4-brand registry. |
| `brand.ts` — `applyBrandAccent()` | `BrandService.applyAccent()` | `BrandService.applyAccent()` | Dynamic accent override on non-helios brands. |
| `share.ts` — `encodeSnapshot()` | `ShareService.encode()` | `ShareService.encode()` | base64url with +/ replacement and padding strip. Identical algorithm. |
| `share.ts` — `decodeSnapshot()` | `ShareService.decode()` | `ShareService.decode()` | Reverse base64url + JSON parse + v1 validation. |
| `share.ts` — `buildSnapshot()` | `ShareService.build(from:)` | `ShareService.build()` | Same field mapping, same rounding rules. |
| `share.ts` — `buildShareUrl()` | `ShareService.buildUrl()` | `ShareService.buildUrl()` | `https://helios.app/share/<payload>` |
| `lib/solarCurve.ts` | `SolarCurve` (struct / object) | `SolarCurve` (object) | `solarFractionAt`, `irradianceAt`, `consumptionFractionAt`, `nowAsHourFloat`. |
| `lib/format.ts` | `Formatters` (static methods) | `Formatters` (object) | `formatKw`, `formatW`, `formatKwh`, `formatPercent`, `formatTemp`, `formatCurrency`, `formatTimeOfDay`, `formatRelative`, `clamp`. |
| `lib/theme.ts` | `ThemeManager` | `ThemeManager` | load/persist/resolve/apply auto/light/dark. |

## 5. Types / Data Contracts

| PWA Type | Swift Struct | Kotlin Data Class | Notes |
|----------|--------------|-------------------|-------|
| `InverterStatus` | `InverterStatus` (enum) | `InverterStatus` (enum) | PRODUCING, STANDBY, CURTAILED, NIGHT, FAULT |
| `SolarTelemetry` | `SolarTelemetry` | `SolarTelemetry` | 28 fields. Immutable. Codable/Parcelable where needed. |
| `PanelString` | `PanelString` | `PanelString` | id, label, powerW, voltageV, currentA, ratedW, panels |
| `HistoryPoint` | `HistoryPoint` | `HistoryPoint` | t, productionW, consumptionW, batteryW, gridW, irradianceWm2 |
| `InsightSeverity` | `InsightSeverity` (enum) | `InsightSeverity` (enum) | positive, neutral, attention, critical |
| `InsightCategory` | `InsightCategory` (enum) | `InsightCategory` (enum) | production, consumption, battery, savings, maintenance, forecast |
| `Insight` | `Insight` | `Insight` | id, category, severity, title, body, metric?, delta?, actionLabel? |
| `ConnectionConfig` | `ConnectionConfig` (@Model for SwiftData) | `ConnectionConfigEntity` (Room) | protocol, host, port, unitId, pollIntervalMs, status |
| `Location` | `Location` | `LocationEntity` | lat, lng, label, source |
| `WeatherCondition` | `WeatherCondition` (enum) | `WeatherCondition` (enum) | 10 cases |
| `ForecastDay` | `ForecastDay` | `ForecastDay` | 11 fields |
| `ProductionForecast` | `ProductionForecast` | `ProductionForecast` | fetchedAt, location, days, totalKwh, vsLastWeekPct |
| `Brand` | `Brand` | `Brand` | id, name, legalName?, accent, accentLight, mark, textMark?, supportEmail?, supportUrl?, tagline? |
| `SnapshotPayload` | `SnapshotPayload` (Codable) | `SnapshotPayload` | v:1, ts, loc, ac, todayKwh, lifeKwh, soc, selfUse, fc?, br? — identical v1 schema |

## 6. Platform-Specific Mapping

| Feature | iOS Implementation | Android Implementation |
|---------|--------------------|------------------------|
| Navigation | `NavigationStack` (iPhone) / `NavigationSplitView` (iPad) | `NavHost` with predictive back gesture |
| App Shell | `TabView` with custom style or `VStack` + custom `BottomNav` | `Scaffold` + `NavigationBar` |
| Charts | Swift Charts (iOS 16+) | Compose Canvas or `androidx.compose.material3:material3` chart primitives |
| Data Persistence | SwiftData (iOS 17+) | Room + DataStore |
| Background Fetch | `BGAppRefreshTask` | WorkManager periodic task |
| Location | CoreLocation | Fused Location Provider |
| Haptics | `UIImpactFeedbackGenerator` + `CoreHaptics` | `HapticFeedbackConstants` + `VibrationEffect.Composition` (API 31+) |
| Live Telemetry | `Timer.publish` → `readTelemetry()` | `kotlinx.coroutines.flow` ticker |
| Deep Links | Universal Links + custom URL scheme | App Links + intent filters |
| Share Sheet | `UIActivityViewController` | `ShareCompat.IntentBuilder` |
| Theming | `@Environment(\.colorScheme)` + custom palette | `MaterialTheme` with custom ColorScheme |
| Onboarding | 4-screen `TabView` with cinematic Canvas animations | 4-screen `HorizontalPager` with Canvas animations |
| Accessibility | VoiceOver + custom rotor actions | TalkBack + custom actions |
| Widgets | WidgetKit + SwiftUI | Glance + AppWidget |
| Watch | watchOS app + complications | Wear OS tile + complication |
| Live Activity | ActivityKit + WidgetExtension | N/A (use persistent notification fallback) |
| Siri / Assistant | App Intents + AppShortcuts | App Actions + Google Assistant |
| Control Center | `ControlCenter` API (iOS 18+) | Quick Settings tile |
