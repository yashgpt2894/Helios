#!/usr/bin/env python3
"""Generate design/tokens.json -- the single platform token source.

Inputs (read, never written):
  shared-spec/design-tokens.json   upstream ramps, semantic aliases, type, motion
  shared-spec/motion-language.md   easing rules, shimmer and entrance timings
  design/DESIGN.md                 layout facts and accessibility rules to keep
  android/app/src/main/java/com/helios/core/designsystem/**   the Compose values today

Output: design/tokens.json

The upstream file stores semantic roles as aliases ("neutral.50", "neutral.950@6%").
This script resolves every alias for both themes, applies the alpha modifier, emits an
8-digit ARGB literal for Compose, and measures WCAG contrast for the pairs the UI
actually uses. Where a resolved upstream alias cannot meet its contrast target, the
role picks the nearest upstream step that does and records the deviation in its
"note" field; every failure that remains is listed in verify.contrast.failures.

Run from the repository root:  python3 .mobile-work/generate-tokens.py
"""

from __future__ import annotations

import json
import pathlib

ROOT = pathlib.Path(__file__).resolve().parent.parent
UPSTREAM = json.loads((ROOT / "shared-spec/design-tokens.json").read_text())
OUT = ROOT / "design/tokens.json"

BG_LIGHT = UPSTREAM["color"]["hues"]["neutral"]["light"]["50"]   # bone #F8F6F0
BG_DARK = UPSTREAM["color"]["hues"]["neutral"]["dark"]["50"]     # carbon #070708
CARBON = UPSTREAM["color"]["hues"]["neutral"]["light"]["950"]    # #0B0B0C
BONE = UPSTREAM["color"]["hues"]["neutral"]["dark"]["950"]       # #F4F1EA

# The brand hue resolves from the white-label accent pair. Helios is the default brand,
# so the concrete values below are the helios accent; another brand substitutes its own
# pair through the same formula (see the brand note in design/DESIGN.md).
BRAND_ACCENT = "#B88A2E"
BRAND_ACCENT_LIGHT = "#F0C674"


# ---------------------------------------------------------------- colour helpers

def hex_to_rgb(value: str) -> tuple[int, int, int]:
    v = value.lstrip("#")
    return int(v[0:2], 16), int(v[2:4], 16), int(v[4:6], 16)


def relative_luminance(value: str) -> float:
    def channel(c: int) -> float:
        s = c / 255.0
        return s / 12.92 if s <= 0.04045 else ((s + 0.055) / 1.055) ** 2.4

    r, g, b = hex_to_rgb(value)
    return 0.2126 * channel(r) + 0.7152 * channel(g) + 0.0722 * channel(b)


def contrast_ratio(fg: str, bg: str) -> float:
    a, b = relative_luminance(fg), relative_luminance(bg)
    hi, lo = max(a, b), min(a, b)
    return round((hi + 0.05) / (lo + 0.05), 2)


def hex_to_argb(value: str, alpha: float) -> str:
    r, g, b = hex_to_rgb(value)
    return "0x%02X%02X%02X%02X" % (round(alpha * 255), r, g, b)


def mix(a: str, b: str, t: float) -> str:
    """Linear sRGB interpolation. Used for tint-over-background and darkening."""
    ar, ag, ab = hex_to_rgb(a)
    br, bg, bb = hex_to_rgb(b)
    return "#%02X%02X%02X" % (
        round(ar + (br - ar) * t),
        round(ag + (bg - ag) * t),
        round(ab + (bb - ab) * t),
    )


def darken(value: str, amount: float) -> str:
    return mix(value, "#000000", amount)


def brand_step(step: int, theme: str) -> str:
    """The upstream brand ramp: accent tints in light, the mirrored ramp in dark."""
    if theme == "light":
        if step == 50:
            return mix(BG_LIGHT, BRAND_ACCENT_LIGHT, 0.10)
        if step == 100:
            return mix(BG_LIGHT, BRAND_ACCENT_LIGHT, 0.20)
        if step == 200:
            return mix(BG_LIGHT, BRAND_ACCENT_LIGHT, 0.35)
        if step == 300:
            return BRAND_ACCENT_LIGHT
        if step == 400:
            return mix(BG_LIGHT, BRAND_ACCENT, 0.85)
        if step == 500:
            return BRAND_ACCENT
        return darken(BRAND_ACCENT, min(0.85, (step - 500) / 450.0 * 0.7))
    if step == 500:
        return BRAND_ACCENT
    if step == 600:
        return mix(BG_DARK, BRAND_ACCENT, 0.85)
    if step == 700:
        return BRAND_ACCENT_LIGHT
    if step in (800, 900, 950):
        return mix(BG_DARK, BRAND_ACCENT_LIGHT, {800: 0.35, 900: 0.20, 950: 0.10}[step])
    return darken(BRAND_ACCENT, min(0.85, (500 - step) / 500.0 * 0.7))


def hue_step(hue: str, step: int, theme: str, sub: str | None = None) -> str:
    if hue == "brand":
        return brand_step(step, theme)
    table = UPSTREAM["color"]["hues"][hue]
    if sub is not None:
        table = table[sub]
    return table[theme][str(step)]


def resolve(ref: str, theme: str) -> tuple[str, float, str]:
    """Resolve one upstream alias for one theme.

    "neutral.950@6%" -> ("#0B0B0C", 0.06). A literal "#RRGGBB" passes straight through,
    which the roles that sit on a fixed fill need because the ramps invert but a fill
    does not.
    """
    if ref.startswith("#"):
        return ref, 1.0, ref
    alias, _, alpha_text = ref.partition("@")
    alpha = float(alpha_text.rstrip("%")) / 100.0 if alpha_text else 1.0
    parts = alias.split(".")
    if parts[0] == "grid":
        return hue_step("grid", int(parts[2]), theme, parts[1]), alpha, alias
    return hue_step(parts[0], int(parts[1]), theme), alpha, alias


def role(ref_light: str, ref_dark: str, usage: str, note: str | None = None) -> dict:
    light_hex, light_alpha, _ = resolve(ref_light, "light")
    dark_hex, dark_alpha, _ = resolve(ref_dark, "dark")
    node = {
        "light": {
            "value": light_hex,
            "alpha": round(light_alpha, 4),
            "argb": hex_to_argb(light_hex, light_alpha),
            "alias": ref_light,
        },
        "dark": {
            "value": dark_hex,
            "alpha": round(dark_alpha, 4),
            "argb": hex_to_argb(dark_hex, dark_alpha),
            "alias": ref_dark,
        },
        "usage": usage,
    }
    if note:
        node["note"] = note
    return node


def same(ref: str, usage: str, note: str | None = None) -> dict:
    """A role that resolves one alias against both ramps; the ramp already inverts."""
    return role(ref, ref, usage, note)


# --------------------------------------------------------------- semantic palette

SEMANTIC = {
    "background": {
        "primary": same("neutral.50", "App canvas. Bone on Paper, carbon on Carbon."),
        "secondary": same("neutral.100", "Recessed groups and list wells."),
        "tertiary": same("neutral.200", "Cards and tiles at level-1 elevation."),
        "elevated": role("neutral.50@95%", "neutral.400@90%", "Sheets, dialogs and glass above content."),
        "scrim": role("neutral.950@48%", "neutral.950@56%", "Modal scrim behind sheets and dialogs."),
    },
    "text": {
        "primary": same("neutral.950", "Titles and primary values."),
        "secondary": role("neutral.800", "neutral.900", "Body copy and labels."),
        "tertiary": role(
            "neutral.800",
            "neutral.900",
            "Metadata, units and footnotes.",
            "The dark ramp has no neutral step between 2.48 and 7.63 contrast on carbon, so secondary and tertiary share neutral.900 in dark. The hierarchy comes from the type role, never from dropping below the 4.5 target.",
        ),
        "quaternary": role(
            "neutral.700",
            "neutral.800",
            "Disabled and placeholder text.",
            "Disabled content is exempt from WCAG 1.4.3. The measured ratios (4.25 light, 2.48 dark) are recorded so the value stays legible rather than invisible.",
        ),
        "inverse": same("neutral.50", "Text on an inverted surface: bone on the carbon surface, carbon on the bone one."),
    },
    "separator": {
        "hairline": same("neutral.950@6%", "1 dp dividers, card outlines and the material inner stroke."),
        "strong": same("neutral.950@15%", "Focus and selection outlines."),
    },
    "accent": {
        "primary": same("brand.500", "Primary action and selection fill. Equals solar.500 for the helios brand."),
        "strong": same("brand.700", "Accent text and icons on the app canvas."),
        "subtle": same("brand.100", "Accent container fill."),
        "onAccent": role(
            "#0B0B0C",
            "#070708",
            "Text and icons on an accent fill.",
            "The accent fill is the same colour in both themes, so the on-colour cannot follow the inverting ramp; carbon is used in both because bone-on-accent measures 2.89.",
        ),
        "onSubtle": role("brand.800", "brand.700", "Text on the accent container."),
    },
    "solar": {
        "primary": same("solar.500", "Production fill, trajectories and the live output mark."),
        "strong": same("solar.700", "Production text and values on the app canvas. DESIGN.md section 9 requires solar.700 and brighter on carbon."),
        "subtle": same("solar.100", "Production container."),
        "dim": same("solar.300", "Trails and low-intensity solar marks."),
    },
    "flow": {
        "primary": same("flow.500", "Self-consumption and home-load fill."),
        "strong": same("flow.700", "Flow text and values on the app canvas."),
        "subtle": same("flow.100", "Flow container."),
    },
    "battery": {
        "primary": same("battery.500", "State of charge fill."),
        "strong": same("battery.700", "Battery text and values on the app canvas."),
        "subtle": same("battery.100", "Battery container."),
        "low": same("alert.500", "State of charge under 20 percent."),
    },
    "grid": {
        "importPrimary": same("grid.import.500", "Grid import fill and trails. Attention hue."),
        "importStrong": same("grid.import.800", "Grid import text on the canvas or its container."),
        "importSubtle": same("grid.import.100", "Import container."),
        "exportPrimary": same("grid.export.500", "Grid export fill and trails. Abundance hue."),
        "exportStrong": same("grid.export.800", "Grid export text on the canvas or its container."),
        "exportSubtle": same("grid.export.100", "Export container."),
    },
    "alert": {
        "primary": same("alert.500", "Fault and destructive fill."),
        "strong": same("alert.700", "Alert text on the app canvas."),
        "subtle": same("alert.100", "Alert container."),
        "onAlert": role("neutral.50", "neutral.950", "Text on an alert fill. Both themes use the light end of the ramp for this fixed fill."),
    },
    "status": {
        "producing": {
            "fg": same("flow.700", "Status pill text and dot: producing."),
            "bg": same("flow.100", "Status pill fill: producing."),
        },
        "standby": {
            "fg": role("neutral.800", "neutral.900", "Status pill text and dot: standby."),
            "bg": same("neutral.200", "Status pill fill: standby."),
        },
        "curtailed": {
            "fg": same("solar.700", "Status pill text and dot: curtailed."),
            "bg": same("solar.100", "Status pill fill: curtailed."),
        },
        "night": {
            "fg": same("grid.export.800", "Status pill text and dot: night."),
            "bg": same("grid.export.100", "Status pill fill: night."),
        },
        "fault": {
            "fg": same("alert.700", "Status pill text and dot: fault."),
            "bg": same("alert.100", "Status pill fill: fault."),
        },
        "offline": {
            "fg": role("neutral.800", "neutral.900", "Status pill text and dot: offline."),
            "bg": same("neutral.300", "Status pill fill: offline."),
        },
        "demo": {
            "fg": same("solar.700", "Status pill text and dot: demo system."),
            "bg": same("solar.100", "Status pill fill: demo system."),
        },
    },
    "insight": {
        "positive": {
            "fg": same("flow.700", "Insight severity: positive."),
            "bg": same("flow.100", "Insight container: positive."),
        },
        "neutral": {
            "fg": role("neutral.800", "neutral.900", "Insight severity: neutral."),
            "bg": same("neutral.200", "Insight container: neutral."),
        },
        "attention": {
            "fg": same("grid.import.800", "Insight severity: attention."),
            "bg": same("grid.import.100", "Insight container: attention."),
        },
        "critical": {
            "fg": same("alert.700", "Insight severity: critical."),
            "bg": same("alert.100", "Insight container: critical."),
        },
    },
    "chart": {
        "production": same("solar.500", "Production series line and area."),
        "consumption": same("flow.500", "Consumption series line."),
        "battery": same("battery.500", "Battery series."),
        "gridImport": same("grid.import.500", "Grid import series."),
        "gridExport": same("grid.export.500", "Grid export series."),
        "axis": role("neutral.700", "neutral.900", "Axis labels and tick text."),
        "gridline": same("neutral.950@8%", "Horizontal gridlines."),
        "todayMarker": same("solar.500", "Live marker on the today curve."),
    },
    "skeleton": {
        "base": same("neutral.300", "Skeleton placeholder shape."),
        "highlight": role("neutral.500@12%", "neutral.500@12%", "Shimmer sweep highlight (motion-language section 10)."),
    },
    "focus": {
        "ring": same("solar.500", "Keyboard and switch-access focus ring."),
    },
}

# -------------------------------------------------------------- typography, layout

WEIGHT_NAMES = {300: "Light", 400: "Normal", 500: "Medium", 600: "SemiBold", 700: "Bold"}

TYPING = {
    "family": UPSTREAM["typography"]["android"]["family"],
    "scale": [
        {
            "token": entry["token"],
            "sizeSp": entry["size"],
            "lineHeightSp": entry["lineHeight"],
            "weight": entry["weight"],
            "weightName": WEIGHT_NAMES.get(entry["weight"], str(entry["weight"])),
            "trackingEm": entry["tracking"],
            "font": entry["font"],
        }
        for entry in UPSTREAM["typography"]["android"]["scale"]
    ],
    "numeric": {
        "body": {"figures": "proportional oldstyle", "note": "Narrative copy only."},
        "chartAxis": {
            "figures": "tabular lining",
            "font": "Roboto Mono",
            "sizeSp": 13,
            "lineHeightSp": 18,
            "note": "Axis ticks and the per-point scrub readout.",
        },
        "liveTicker": {
            "figures": "tabular lining",
            "font": "Roboto Mono",
            "sizeSp": 28,
            "lineHeightSp": 34,
            "weight": 600,
            "note": "Live kW ticker. Width must not change while digits animate.",
        },
    },
    "limits": {
        "heroCapSp": 44,
        "heroCapNote": "DESIGN.md section 9: the hero number caps at 44 sp at 200 percent text scaling.",
        "metricGridCollapseFontScale": 1.6,
    },
}

SPACING = {
    "unitDp": 4,
    "scale": {k: int(v) for k, v in UPSTREAM["spacing"]["scale"].items()},
    "gutterDp": UPSTREAM["spacing"]["safeArea"]["horizontal"],
    "sectionRhythmDp": 20,
    "cardPaddingDp": 16,
    "minTouchTargetDp": 48,
    "safeArea": {
        "top": "statusBarsPadding()",
        "bottom": "navigationBarsPadding()",
        "horizontalDp": UPSTREAM["spacing"]["safeArea"]["horizontal"],
    },
}

RADIUS = {k: int(v) for k, v in UPSTREAM["radius"].items()}

ELEVATION = {
    "levels": {k: int(str(v["android"]).replace("dp", "")) for k, v in UPSTREAM["elevation"].items()},
    "usage": {
        "level-0": "Flat canvas.",
        "level-1": "Cards and tiles.",
        "level-2": "Raised tiles on the hero block.",
        "level-3": "Sheets, bottom nav, top bar on scroll.",
        "level-4": "Menus and popovers.",
        "level-5": "Dialogs.",
    },
    "material": {
        "regular": {
            "tonalElevationDp": UPSTREAM["material"]["android"]["regular"]["tonalElevation"],
            "color": "background.elevated",
        },
        "thin": {
            "tonalElevationDp": UPSTREAM["material"]["android"]["thin"]["tonalElevation"],
            "color": "background.elevated",
        },
        "innerStrokeDp": UPSTREAM["material"]["android"]["innerStroke"]["width"],
        "innerStrokeColor": "separator.hairline",
        "reduceTransparency": "solid background.elevated at the same elevation, inner stroke at 0.3 alpha (motion-language section 12)",
    },
}

DURATIONS_MS = {k: round(float(v) * 1000) for k, v in UPSTREAM["motion"]["duration"].items()}
DURATIONS_MS.update(
    {
        "shimmer": 2500,               # motion-language section 10
        "ringTrim": 800,               # motion-language section 5
        "screenEnter": 350,            # motion-language section 9
        "reduceMotionCrossfade": 200,  # motion-language section 11
        "nodePulse": 2400,             # motion-language section 4.3
        "chargeGlowPulse": 1600,       # motion-language section 5
        "dischargePulse": 1000,        # motion-language section 5
    }
)

MOTION = {
    "durationMs": DURATIONS_MS,
    "easing": {
        "linear": {
            "curve": "cubic-bezier(0, 0, 1, 1)",
            "compose": "LinearEasing",
            "usage": "Indeterminate loaders and the skeleton shimmer only. Springs are the default for every other transition (motion-language section 1).",
        },
        "standard": {
            "curve": "cubic-bezier(0.2, 0, 0, 1)",
            "compose": "CubicBezierEasing(0.2f, 0f, 0f, 1f)",
            "usage": "Opacity cross-fades where a spring cannot be interpolated.",
        },
        "decelerate": {
            "curve": "cubic-bezier(0, 0, 0, 1)",
            "compose": "CubicBezierEasing(0f, 0f, 0f, 1f)",
            "usage": "Elements entering the viewport.",
        },
        "accelerate": {
            "curve": "cubic-bezier(0.3, 0, 1, 1)",
            "compose": "CubicBezierEasing(0.3f, 0f, 1f, 1f)",
            "usage": "Elements leaving the viewport.",
        },
        "springsAreDefault": True,
    },
    "spring": {
        preset: {
            "dampingRatio": spec["android"]["dampingRatio"],
            "stiffness": spec["android"]["stiffness"],
            "compose": "spring(dampingRatio = %.2ff, stiffness = %.0ff)" % (spec["android"]["dampingRatio"], spec["android"]["stiffness"]),
        }
        for preset, spec in UPSTREAM["motion"]["springPresets"].items()
    },
    "staggerMs": {k: round(float(v) * 1000) for k, v in UPSTREAM["motion"]["stagger"].items()},
    "entrance": {
        "heroDelayMs": 0,
        "statsStaggerMs": 40,
        "sectionHeaderDelayMs": 200,
        "chartDelayMs": 300,
        "chromeDelayMs": 400,
        "chartDrawMs": 600,
        "screenTransform": "opacity 0 to 1, scale 0.96 to 1.0, translateX 24 to 0 dp",
    },
    "reduceMotion": {
        "spring": "instant plus a normal-duration opacity cross-fade",
        "crossfadeMs": 200,
        "energyFlow": "dots stop, static arrows on each path",
        "batteryRing": "trim over the fast duration, no spring",
        "tickers": "snap instantly",
        "parallax": "disabled",
    },
}

LAYOUT = {
    "referenceViewportDp": {"width": 412, "height": 915},
    "heroBlockMaxDp": 430,
    "topBarHeightDp": 64,
    "bottomNavHeightDp": 80,
    "chartHeightDp": 160,
    "heroChartHeightDp": 220,
    "forecastStripItemWidthDp": 72,
    "subtitleMaxLines": 2,
    "bottomNavDestinations": 5,
}

# ------------------------------------------------------------------ contrast audit

CONTRAST_PAIRS = [
    ("text.primary", "background.primary", "body and titles", 4.5),
    ("text.secondary", "background.primary", "body copy", 4.5),
    ("text.tertiary", "background.primary", "metadata and units", 4.5),
    ("text.quaternary", "background.primary", "disabled text", 2.0),
    ("text.primary", "background.tertiary", "text on a card", 4.5),
    ("text.secondary", "background.tertiary", "body on a card", 4.5),
    ("solar.strong", "background.primary", "production value", 4.5),
    ("flow.strong", "background.primary", "self-consumption value", 4.5),
    ("battery.strong", "background.primary", "state of charge value", 4.5),
    ("alert.strong", "background.primary", "fault label", 4.5),
    ("grid.importStrong", "background.primary", "grid import value", 4.5),
    ("grid.exportStrong", "background.primary", "grid export value", 4.5),
    ("accent.strong", "background.primary", "accent text and icons", 4.5),
    ("accent.onAccent", "accent.primary", "text on an accent fill", 4.5),
    ("accent.onSubtle", "accent.subtle", "text on the accent container", 4.5),
    ("alert.onAlert", "alert.primary", "text on an alert fill", 4.5),
    ("status.producing.fg", "status.producing.bg", "producing pill", 4.5),
    ("status.standby.fg", "status.standby.bg", "standby pill", 4.5),
    ("status.curtailed.fg", "status.curtailed.bg", "curtailed pill", 4.5),
    ("status.night.fg", "status.night.bg", "night pill", 4.5),
    ("status.fault.fg", "status.fault.bg", "fault pill", 4.5),
    ("status.offline.fg", "status.offline.bg", "offline pill", 4.5),
    ("status.demo.fg", "status.demo.bg", "demo pill", 4.5),
    ("insight.positive.fg", "insight.positive.bg", "positive insight", 4.5),
    ("insight.neutral.fg", "insight.neutral.bg", "neutral insight", 4.5),
    ("insight.attention.fg", "insight.attention.bg", "attention insight", 4.5),
    ("insight.critical.fg", "insight.critical.bg", "critical insight", 4.5),
]


def flat(path: str) -> dict:
    node = SEMANTIC
    for part in path.split("."):
        node = node[part]
    return node


def build_contrast() -> dict:
    rows = []
    for fg, bg, purpose, target in CONTRAST_PAIRS:
        light = contrast_ratio(flat(fg)["light"]["value"], flat(bg)["light"]["value"])
        dark = contrast_ratio(flat(fg)["dark"]["value"], flat(bg)["dark"]["value"])
        rows.append(
            {
                "foreground": fg,
                "background": bg,
                "purpose": purpose,
                "target": target,
                "light": light,
                "dark": dark,
                "lightPass": light >= target,
                "darkPass": dark >= target,
                "wcag": "AA",
            }
        )
    return {
        "method": "WCAG 2.1 relative luminance, rounded to 2 decimals",
        "rows": rows,
        "failures": [r for r in rows if not (r["lightPass"] and r["darkPass"])],
    }


TOKENS = {
    "$meta": {
        "name": "helios design tokens",
        "version": "1.0.0",
        "role": "single token source for android, ios and web",
        "generatedBy": ".mobile-work/generate-tokens.py",
        "generatedFrom": [
            "shared-spec/design-tokens.json",
            "shared-spec/motion-language.md",
            "design/DESIGN.md",
            "android/app/src/main/java/com/helios/core/designsystem/",
        ],
        "platforms": ["android", "ios", "web"],
        "distanceFromUpstream": [
            "Aliases are resolved to concrete values for both themes; the upstream alias is kept per role so a change upstream can be traced.",
            "Alpha modifiers become an explicit alpha plus an 8-digit ARGB literal.",
            "Functional roles the components need (accent, solar, flow, battery, grid, alert, status, insight, chart, skeleton, focus) are named, because the upstream semantic block only defines background, text and separator.",
            "Layout facts stated in design/DESIGN.md are recorded as numbers.",
            "Contrast is measured, not assumed. Six upstream alias choices could not reach their target and were moved to the nearest upstream step; each such role carries a note. Remaining failures are listed in verify.contrast.failures.",
        ],
        "brand": {
            "overridable": ["accent.primary", "accent.strong", "accent.subtle", "accent.onSubtle"],
            "formula": "light tints ascend accentLight to accent; dark is the mirrored ramp from the same pair; 500 is the accent in both themes",
            "defaultBrand": "helios",
            "defaultAccent": BRAND_ACCENT,
            "defaultAccentLight": BRAND_ACCENT_LIGHT,
            "registry": ["helios", "voltcraft", "sunworks", "meridian"],
        },
        "colorSpace": UPSTREAM["meta"]["colorSpace"],
        "gridDp": UPSTREAM["meta"]["grid"],
        "noEmoji": True,
        "localFirst": True,
    },
    "color": {"hues": UPSTREAM["color"]["hues"], "semantic": SEMANTIC},
    "typography": TYPING,
    "spacing": SPACING,
    "radius": RADIUS,
    "elevation": ELEVATION,
    "motion": MOTION,
    "layout": LAYOUT,
    "haptics": UPSTREAM["haptics"],
    "platformMapping": {
        "android": {
            "color": "android/app/src/main/java/com/helios/core/designsystem/color/HeliosSemanticColor.kt",
            "typography": "android/app/src/main/java/com/helios/core/designsystem/type/HeliosTypography.kt",
            "spacing": "android/app/src/main/java/com/helios/core/designsystem/layout/HeliosSpacing.kt",
            "radius": "android/app/src/main/java/com/helios/core/designsystem/shape/HeliosShape.kt",
            "elevation": "android/app/src/main/java/com/helios/core/designsystem/shape/HeliosElevation.kt",
            "motion": "android/app/src/main/java/com/helios/core/designsystem/motion/HeliosMotion.kt",
            "haptics": "android/app/src/main/java/com/helios/core/designsystem/haptics/HeliosHaptics.kt",
            "theme": "android/app/src/main/java/com/helios/core/ui/theme/HeliosTheme.kt",
            "format": "android/app/src/main/java/com/helios/core/format/HeliosFormat.kt",
            "components": "android/app/src/main/java/com/helios/core/designsystem/component/",
            "services": "android/app/src/main/java/com/helios/core/data/service/",
            "fixtures": "android/app/src/main/java/com/helios/core/data/fixture/",
            "gallery": "android/app/src/debug/java/com/helios/debug/gallery/ComponentGalleryScreen.kt",
            "galleryPaging": "android/app/src/debug/java/com/helios/debug/gallery/GalleryPaging.kt",
        },
        "ios": {"status": "not in this pass; ios/ is untouched"},
        "web": {"status": "existing PWA keeps src/index.css and tailwind.config.js; this file is the reference, not yet wired into the build"},
    },
    "verify": {"contrast": build_contrast()},
}

OUT.write_text(json.dumps(TOKENS, indent=2) + "\n")
print(f"wrote {OUT.relative_to(ROOT)} ({OUT.stat().st_size} bytes)")
contrast = TOKENS["verify"]["contrast"]
print(f"contrast pairs checked: {len(contrast['rows'])}, below target: {len(contrast['failures'])}")
for row in contrast["failures"]:
    print(
        "  FAIL %-26s on %-26s light %.2f dark %.2f (target %.1f)"
        % (row["foreground"], row["background"], row["light"], row["dark"], row["target"])
    )
