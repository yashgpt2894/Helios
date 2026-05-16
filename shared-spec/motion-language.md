# Helios Motion Language

The Helios brand is defined by precision, warmth, and physics. Every animation must feel like a precision instrument coming alive — not a slideshow. This document is the contract between design and engineering for all motion in the native apps.

## 1. Philosophy

- **Physics over time-based**: Every transition is a spring. Linear easings are forbidden except for indeterminate loaders.
- **Interruptible**: All animations can be grabbed mid-flight. A drag during a dismiss follows the finger.
- **Choreographed**: Screen entrances are staged, not simultaneous. Hero first, then stats, then charts.
- **Haptic-coupled**: Meaningful taps always pair with a haptic that matches the animation intensity.
- **Responsive to data**: Animation density modulates with live values (e.g., EnergyFlow dot density increases with wattage).

## 2. Spring Presets

| Preset | Character | iOS (`spring`) | Android (`androidx.compose.animation`) |
|--------|-----------|----------------|----------------------------------------|
| **gentle** | Soft landings, hero cards | `mass:1, stiffness:120, damping:24` | `spring(dampingRatio=0.85, stiffness=120)` |
| **default** | Standard transitions | `mass:1, stiffness:200, damping:26` | `spring(dampingRatio=0.8, stiffness=200)` |
| **snappy** | Toggles, switches, nav | `mass:1, stiffness:380, damping:32` | `spring(dampingRatio=0.7, stiffness=380)` |
| **bouncy** | Playful moments, success | `mass:1.2, stiffness:280, damping:14` | `spring(dampingRatio=0.5, stiffness=280)` |

## 3. Choreographed Entrances

When a screen mounts, elements enter in a strict sequence:

1. **Hero card** (0 ms delay): `gentle` spring, opacity 0→1 + translateY 12→0, duration ~0.5s
2. **Stats row** (80 ms stagger): Each `MetricTile` enters at 40 ms intervals after the hero. `default` spring, opacity + translateY.
3. **Section headers** (after stats): Fade in at 200 ms delay, `normal` duration (0.25s).
4. **Charts / lists** (after headers): Fade + scale 0.98→1.0 at 300 ms delay. Line charts draw from left to right over 600 ms using `deliberate` duration with `gentle` spring for the draw head.
5. **Floating chrome** (400 ms delay): TopBar and BottomNav settle into place with `snappy` spring if they were off-screen.

**Dashboard specific sequence:**
- Live kW number: `contentTransition(.numericText)` on iOS; per-digit `AnimatedContent` slide on Android.
- EnergyFlow: Nodes fade in radially from center (0→1 opacity, 20 ms stagger per node). Flow lines begin animating only after all nodes are visible.
- ForecastStrip / ProductionChart: Fade in after EnergyFlow is fully visible (+200 ms).

## 4. EnergyFlow Signature Animation

The EnergyFlow is the app's signature visual. It must be implemented with platform Canvas (SwiftUI Canvas / Compose Canvas), not pre-baked assets.

### 4.1 Path Geometry

- 4 nodes arranged in a diamond: Solar (top), Home (center), Battery (bottom-left), Grid (bottom-right).
- Bezier connections: Solar→Home (vertical curve), Battery↔Home (diagonal curve), Grid↔Home (diagonal curve).
- Path control points must recompute smoothly when container size changes. No snapping.

### 4.2 Particle System

- **Dots**: Small circles (2.5–3.5 pt) traveling along each path.
- **Density**: Number of concurrent dots scales with active wattage on that path.
  - 0 W: 0 dots
  - 1–500 W: 1 dot
  - 500–2000 W: 2 dots
  - 2000–5000 W: 3 dots
  - 5000+ W: 4 dots
- **Speed**: Dot speed scales linearly with wattage. Base duration 2.2s at 1000 W, down to 1.0s at 10000 W.
- **Color**: Solar path = solar hue; Battery path = battery hue; Grid import = grid.import hue; Grid export = grid.export hue.
- **Glow**: Each dot has a drop-shadow glow matching its path color. Glow radius scales with wattage.
- **Reverse**: Battery charging and grid exporting reverse dot direction.

### 4.3 Node Animation

- Nodes pulse subtly when active: scale 1.0→1.04→1.0 over 2.4s, `gentle` spring.
- Home hub has a radial glow gradient that pulses with total system activity.

## 5. BatteryRing Animation

- **Trim**: `strokeDasharray` animates with `gentle` spring on every SOC change. Duration 0.8s.
- **Direction**: Counterclockwise fill starting from 12 o'clock.
- **Glow on charge**: When `batteryPowerW > 30`, the ring stroke gets a drop-shadow glow in Flow green. Glow intensity pulses: opacity 0.4→0.8→0.4 over 1.6s.
- **Pulse on discharge**: When `batteryPowerW < -30`, the ring stroke opacity pulses 0.6→1.0→0.6 over 1.0s.
- **Threshold haptics**: Crossing 80% → `.light`; 50% → `.medium`; 20% → `.heavy`.

## 6. Number Tickers

- **iOS**: `.contentTransition(.numericText(value:))` on the Text view. No custom spring; uses system default which is already excellent.
- **Android**: `AnimatedContent` with `SizeTransform` + per-digit vertical slide. Digits slide up/out and up/in. Use `snappy` spring. Transition must measure old/new digit bounds to avoid layout jump.
- **Formatter preservation**: Tickers must not lose decimal precision during transition. Animate only the numeric portion, keeping unit labels static.

## 7. Shared-Element Transitions

When tapping a Dashboard card that leads to a detail screen:

- **Source**: The card bounds are captured as a `matchedGeometryEffect` (iOS) or `sharedBounds` modifier (Android custom implementation).
- **Destination**: The hero element in the detail screen shares the same effect ID.
- **Animation**: `default` spring. During the transition, the card morphs into the detail hero. Background cross-fades. Other elements in the detail screen fade in with 40 ms stagger after the shared element lands.
- **Supported pairs**:
  - Dashboard battery card → Battery screen BatteryRing
  - Dashboard production metric → Production screen hero kW
  - Dashboard forecast strip → Insights forecast card
  - Dashboard insight highlight → Insights detail (if added later)

## 8. Parallax on Scroll

- **Production forecast carousel**: Horizontal swipe on forecast days. Each day card has a subtle parallax: the weather icon moves at 0.8× scroll speed, the kWh number at 1.0×, the bar at 1.1×.
- **Landing hero**: Background aurora gradient moves at 0.3× scroll speed. Foreground text at 1.0×. Brand mark at 0.7×.
- **Implementation**: Use scroll offset interpolation. On iOS, `scrollTargetBehavior(.paging)` + `GeometryReader`. On Android, `LazyRow` + `rememberLazyListState` + derived scroll offset.

## 9. Page Transitions

Default push-from-right is banned. Use this hand-tuned blend:

- **Entering screen**: opacity 0→1, scale 0.96→1.0, translateX 24→0
- **Exiting screen**: opacity 1→0.6, scale 1.0→0.98, translateX 0→-12
- **Spring**: `gentle` for the entering screen; `default` for the exiting.
- **Duration**: ~0.35s total.
- **Background**: Shared background color cross-fades (no jarring flash).

On Android predictive back gesture:
- Custom `PredictiveBackHandler` interpolates the same transform based on gesture progress.
- At 50% progress, the exiting screen is fully visible at scale 0.99 and translateX -6.

## 10. Loading & Empty States

- **Skeleton shimmer**: A left-to-right gradient sweep across placeholder shapes. Gradient: `neutral.500@0% → neutral.500@12% → neutral.500@0%`. Animation duration 2.5s, linear, infinite.
- **Skeleton shapes**: Must match the exact corner radius and approximate aspect ratio of the eventual content. No generic rectangles.
- **Empty state entrance**: Illustration scales from 0.9→1.0 with `bouncy` spring. Title and body fade in with 60 ms stagger. CTA button slides up with `snappy` spring.

## 11. Reduce Motion

When the user enables Reduce Motion:

- All springs fall back to `instant` + `normal` duration opacity cross-fade.
- EnergyFlow dots stop animating; paths show static arrows instead.
- BatteryRing trim animates over 0.1s (fast fade, no spring).
- Number tickers snap instantly.
- Parallax is disabled; scroll behaves normally.
- Page transitions become pure opacity cross-fade (0.2s).

## 12. Reduce Transparency

When the user enables Reduce Transparency:

- `.regularMaterial` → `secondarySystemBackground` (solid)
- `.thinMaterial` → `tertiarySystemBackground` (solid)
- Android `Surface(tonalElevation)` → solid color at equivalent elevation token.
- Inner strokes remain visible but at reduced opacity (0.3).

## 13. Sound (Opt-in, Default On)

- **Tile tap**: Short click, 80 ms, low-pass filtered.
- **Sheet open/close**: Soft thud, 120 ms, inverse envelope.
- **Success chime**: Bright ping, 200 ms, solar-frequency overtone.
- **Share copy**: Subtle pop, 60 ms.
- **Theme toggle**: Soft sweep, 100 ms, pitch rises for light, falls for dark.
- **Brand switch**: Glass clink, 150 ms.

All sounds respect the Silent Switch (iOS) and Do Not Disturb / alarm volume stream (Android). Sounds are 32-bit float 48 kHz mono, < 250 ms, < 30 KB each.

## 14. Accessibility Motion

- VoiceOver/TalkBack focus changes trigger a `light` haptic.
- Large content size changes animate layout with `gentle` spring over 0.3s.
- High contrast mode does not alter motion timing; only color values change.
