# Android Adaptive Icon Specification

## Overview

The helios app icon follows Android's adaptive icon format with a foreground layer, background layer, and monochrome layer. The system applies the device's configured mask shape (circle, squircle, rounded square, teardrop) to the foreground.

## Layer Composition

### Background Layer
- **Content**: Radial gradient across the full 108dp x 108dp canvas.
- **Gradient stops**:
  - Center (0%): `#1A1A1C` (carbon, neutral.400 dark)
  - Mid (50%): `#0F0F10` (neutral.200 dark)
  - Edge (100%): `#070708` (neutral.50 dark)
- **Aurora accent**: A second radial gradient overlaying the background, anchored at top-left (20%, 20%), transitioning to transparent at 70% radius:
  - 0%: `#F0C674` at 8% opacity (solar.700)
  - 50%: `#F0C674` at 2% opacity
  - 100%: transparent
- **Texture**: Subtle 2% noise to prevent OLED banding.

### Foreground Layer
- **Content**: The HeliosMark — an 8-blade radial mark — centered on a transparent background.
- **Geometry**:
  - Center at (54dp, 54dp) in the 108dp x 108dp canvas.
  - 8 blades radiating from center. Each blade: inner radius 4dp, outer radius 42dp, width tapers from 2dp at base to 0.5dp at tip.
  - Spaced at 45-degree intervals starting from 0 degrees (12 o'clock).
- **Color**: Bone (#F4F1EA, neutral.950 dark) at 92% opacity.
- **Glow**: Solar gold (#F0C674) drop shadow at 16% opacity, blur radius 4dp, centered.
- **Safe zone**: The mark fits within a 66dp-diameter circle centered in the canvas. This ensures it is fully visible regardless of OEM mask shape.

### Monochrome Layer
- **Content**: Same HeliosMark geometry as foreground, but rendered as a solid silhouette.
- **Color**: #FFFFFF at 100% opacity — a pure white silhouette on transparent background.
- **Purpose**: Used by the system for Themed Icons (Android 13+) and notification icons where a single-color representation is required.
- **Safe zone**: Same 66dp circle constraint.

## Adaptive Icon Sizes

| Density Bucket | Full Canvas | Safe Zone (66dp equivalent) |
|---------------|-------------|---------------------------|
| mdpi (1x) | 108 x 108 px | 66 x 66 px |
| hdpi (1.5x) | 162 x 162 px | 99 x 99 px |
| xhdpi (2x) | 216 x 216 px | 132 x 132 px |
| xxhdpi (3x) | 324 x 324 px | 198 x 198 px |
| xxxhdpi (4x) | 432 x 432 px | 264 x 264 px |

## Asset Files

```
res/
  mipmap-mdpi/
    ic_launcher.xml              (adaptive-icon definition)
    ic_launcher_background.png   (108x108)
    ic_launcher_foreground.png   (108x108)
    ic_launcher_monochrome.png   (108x108)
  mipmap-hdpi/
    ic_launcher.xml
    ic_launcher_background.png   (162x162)
    ic_launcher_foreground.png   (162x162)
    ic_launcher_monochrome.png   (162x162)
  mipmap-xhdpi/
    ic_launcher.xml
    ic_launcher_background.png   (216x216)
    ic_launcher_foreground.png   (216x216)
    ic_launcher_monochrome.png   (216x216)
  mipmap-xxhdpi/
    ic_launcher.xml
    ic_launcher_background.png   (324x324)
    ic_launcher_foreground.png   (324x324)
    ic_launcher_monochrome.png   (324x324)
  mipmap-xxxhdpi/
    ic_launcher.xml
    ic_launcher_background.png   (432x432)
    ic_launcher_foreground.png   (432x432)
    ic_launcher_monochrome.png   (432x432)
```

## ic_launcher.xml Template

```xml
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@mipmap/ic_launcher_background"/>
    <foreground android:drawable="@mipmap/ic_launcher_foreground"/>
    <monochrome android:drawable="@mipmap/ic_launcher_monochrome"/>
</adaptive-icon>
```

## Legacy Icon (API < 26)

For pre-Oreo devices, provide a fallback 48dp icon at each density (48x48 mdpi through 192x192 xxxhdpi) as `ic_launcher.png`. Use the helios mark with the carbon gradient background baked in (not adaptive — a single flat PNG).

## Color Reference

| Role | Token | Hex (Dark) |
|------|-------|------------|
| Deepest background | neutral.50 | #070708 |
| Carbon background | neutral.100 | #0B0B0C |
| Mid carbon | neutral.200 | #0F0F10 |
| Elevated surface | neutral.400 | #1A1A1C |
| Bone text/mark | neutral.950 | #F4F1EA |
| Solar gold | solar.600 | #D4A843 |
| Solar highlight | solar.700 | #F0C674 |

## Visual Balance Notes

- The 66dp safe zone is the critical constraint. All meaningful visual content (the mark) must fit within this circle.
- Test on Pixel (circle mask), Samsung (squircle with rounded corners), and OnePlus (near-square mask) to ensure the mark looks intentional on all shapes.
- The monochrome layer must look good as a pure silhouette — test it by viewing the launcher icon with Themed Icons enabled on Android 13+.
- The aurora overlay on the background layer should be subtle. It should feel like a natural glow behind the mark, not a separate visual element.
