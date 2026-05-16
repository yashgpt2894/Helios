# iOS Parallax App Icon Specification

## Overview

The helios app icon uses Apple's parallax icon format with three composited layers. When the user tilts their device, each layer shifts at a different depth, creating a 3D parallax effect.

## Layer Composition

### Layer 3: Background (Depth: 0%)
- **Content**: Radial gradient centered at the icon midpoint.
- **Gradient stops**:
  - Center (0%): `#1A1A1C` (carbon, neutral.400 dark)
  - Mid (45%): `#0F0F10` (deeper carbon, neutral.200 dark)
  - Edge (90%): `#070708` (deepest carbon, neutral.50 dark)
  - Outer rim (100%): `#0B0B0C` (neutral.100 dark)
- **Overlay**: Aurora accent — a subtle sweep of solar hue. Radial gradient from top-left at 30% opacity, transitioning to transparent. Stop colors:
  - 0%: `rgba(240,198,116,0.08)` — solar 700
  - 60%: `rgba(240,198,116,0.02)`
  - 100%: transparent
- **Noise texture**: Subtle 2% monochrome noise overlay (Add Noise at 2%, Uniform, Monochromatic) to prevent banding on OLED displays.

### Layer 2: Helios Mark (Depth: 33%)
- **Content**: The HeliosMark — an 8-blade radial mark centered in the icon.
- **Geometry**: 
  - Center at (512, 512) in 1024x1024 canvas.
  - 8 blades radiating from center, each blade a narrow wedge tapering outward.
  - Blades spaced at 45-degree intervals starting from 0 degrees (12 o'clock position).
  - Each blade: inner radius 40pt, outer radius 400pt, blade width tapers from 18pt at base to 2pt at tip.
- **Color**: Bone (#F4F1EA, neutral.950 dark) at 92% opacity.
- **Glow**: Solar hue (#F0C674, solar.700 dark) drop shadow at 24% opacity, blur radius 32pt, offset (0, 0).
- **Alignment**: Centered in the 1024x1024 canvas. The mark occupies the central 80% of the icon area.

### Layer 1: Foreground Sun Glyph (Depth: 50%)
- **Content**: A simplified circular sun glyph — a central circle with 4 short radial rays.
- **Geometry**:
  - Central circle: center (512, 512), radius 56pt.
  - 4 rays at 0, 90, 180, 270 degrees, each 86pt from center, 12pt wide, 18pt tall, rounded caps.
- **Color**: Solar gold (#D4A843, solar.600 dark) with a subtle inner glow of #F0C674 at 40%.
- **Purpose**: Provides the closest parallax layer, giving depth to the icon when the device tilts.
- **Drop shadow**: None on this layer (it sits above the helios mark).

## Dimensions

| Asset | Size | Format |
|-------|------|--------|
| App Store Icon | 1024 x 1024 pt | PNG (non-interpolated) |
| Layer 3 (Background) | 1024 x 1024 pt | PNG-24, no transparency |
| Layer 2 (Helios Mark) | 1024 x 1024 pt | PNG-24, transparent background |
| Layer 1 (Foreground) | 1024 x 1024 pt | PNG-24, transparent background |

## Xcode Asset Catalog Configuration

In `Assets.xcassets > AppIcon`:

1. Set "iOS App Icon" to "Include all sizes".
2. For the 1024x1024 slot, import the three layer PNGs.
3. In the Attributes inspector, enable "iOS App Icon is a parallax icon".
4. Assign:
   - Background: Layer 3 PNG
   - Middle: Layer 2 PNG  
   - Foreground: Layer 1 PNG

## Derived Sizes

Xcode automatically generates all required sizes from the 1024x1024 source:
- 20pt (2x, 3x): Notification icon
- 29pt (2x, 3x): Settings icon
- 40pt (2x, 3x): Spotlight
- 60pt (2x, 3x): App icon on Home Screen
- 76pt (2x): iPad app icon
- 83.5pt (2x): iPad Pro app icon

## Color Reference

All colors from the Helios design token system (see `shared-spec/design-tokens.json`):

| Role | Token | Hex (Dark) |
|------|-------|------------|
| Deepest background | neutral.50 | #070708 |
| Carbon background | neutral.100 | #0B0B0C |
| Mid carbon | neutral.200 | #0F0F10 |
| Elevated surface | neutral.400 | #1A1A1C |
| Bone text | neutral.950 | #F4F1EA |
| Solar gold | solar.600 | #D4A843 |
| Solar highlight | solar.700 | #F0C674 |

## Visual Balance Notes

- The aurora overlay must be subtle — just enough to give the icon warmth against the system background, never so bright that it distracts.
- The helios mark blades should feel sharp and precise at small sizes (40pt). Test at 40x40pt rendered size to confirm legibility.
- The sun glyph should remain recognizable at 20x20pt (notification icon size). If the rays disappear at that scale, increase the minimum ray length to 22pt.
