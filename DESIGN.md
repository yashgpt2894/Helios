---
name: helios°
description: Precision solar telemetry for homeowners — carbon ground, bone text, one amber signal, numbers set in mono.
colors:
  carbon-ground: "#070708"
  carbon-ground-1: "#0b0b0c"
  carbon-ground-2: "#0f0f10"
  carbon-ground-3: "#141416"
  carbon-ground-4: "#1a1a1c"
  bone-100: "#efece5"
  bone-200: "#dcd6c8"
  bone-300: "#bdb6a6"
  bone-400: "#a59f90"
  bone-500: "#7a7568"
  bone-500-landing: "#918c7d"
  bone-600: "#544f47"
  signal-solar: "#f0c674"
  signal-solar-light: "#b8862e"
  signal-flow: "#7fb069"
  signal-grid: "#5d8aa8"
  signal-battery: "#c5a572"
  signal-alert: "#d97757"
  field-ink: "#0b0b0c"
  paper-ground: "#f4f1ea"
  paper-ground-1: "#efece5"
  paper-surface: "#ffffff"
  ink-100: "#0b0b0c"
  ink-300: "#2a2a2d"
  ink-400: "#544f47"
  ink-500-landing: "#6b665a"
typography:
  display:
    fontFamily: "Plus Jakarta Sans, Inter, system-ui, sans-serif"
    fontSize: "4.25rem"
    fontWeight: 700
    lineHeight: 1.02
    letterSpacing: "-0.03em"
  display-l:
    fontFamily: "Plus Jakarta Sans, Inter, system-ui, sans-serif"
    fontSize: "3.25rem"
    fontWeight: 700
    lineHeight: 1.02
    letterSpacing: "-0.03em"
  headline:
    fontFamily: "Plus Jakarta Sans, Inter, system-ui, sans-serif"
    fontSize: "2.6rem"
    fontWeight: 700
    lineHeight: 1.02
    letterSpacing: "-0.03em"
  headline-s:
    fontFamily: "Plus Jakarta Sans, Inter, system-ui, sans-serif"
    fontSize: "2.25rem"
    fontWeight: 700
    lineHeight: 1.02
    letterSpacing: "-0.03em"
  headline-xs:
    fontFamily: "Plus Jakarta Sans, Inter, system-ui, sans-serif"
    fontSize: "1.75rem"
    fontWeight: 700
    lineHeight: 1.02
    letterSpacing: "-0.03em"
  numeral:
    fontFamily: "Plus Jakarta Sans, sans-serif"
    fontSize: "3.75rem"
    fontWeight: 800
    lineHeight: 1
    letterSpacing: "-0.04em"
  metric:
    fontFamily: "JetBrains Mono, ui-monospace, monospace"
    fontSize: "22px"
    fontWeight: 400
    lineHeight: 1
    fontFeature: "'tnum' 1, 'zero' 1"
  metric-s:
    fontFamily: "JetBrains Mono, ui-monospace, monospace"
    fontSize: "18px"
    fontWeight: 400
    lineHeight: 1
    fontFeature: "'tnum' 1, 'zero' 1"
  title:
    fontFamily: "Plus Jakarta Sans, Inter, system-ui, sans-serif"
    fontSize: "16px"
    fontWeight: 600
    lineHeight: 1.35
  body:
    fontFamily: "Plus Jakarta Sans, Inter, system-ui, sans-serif"
    fontSize: "16px"
    fontWeight: 400
    lineHeight: 1.6
  body-s:
    fontFamily: "Plus Jakarta Sans, Inter, system-ui, sans-serif"
    fontSize: "15px"
    fontWeight: 400
    lineHeight: 1.6
  small:
    fontFamily: "Plus Jakarta Sans, Inter, system-ui, sans-serif"
    fontSize: "14px"
    fontWeight: 400
    lineHeight: 1.6
  mono:
    fontFamily: "JetBrains Mono, ui-monospace, monospace"
    fontSize: "13px"
    fontWeight: 400
    lineHeight: 1.4
    fontFeature: "'tnum' 1, 'zero' 1"
  label:
    fontFamily: "Plus Jakarta Sans, Inter, system-ui, sans-serif"
    fontSize: "12px"
    fontWeight: 400
    lineHeight: 1.4
  mono-meta:
    fontFamily: "JetBrains Mono, ui-monospace, monospace"
    fontSize: "11px"
    fontWeight: 400
    lineHeight: 1.4
  label-cap:
    fontFamily: "JetBrains Mono, ui-monospace, monospace"
    fontSize: "10px"
    fontWeight: 500
    letterSpacing: "0.3em"
rounded:
  scrollbar: "3px"
  control: "12px"
  surface: "18px"
  surface-raised: "20px"
  device: "40px"
  pill: "9999px"
spacing:
  cell: "24px"
  row: "48px"
  gutter-mobile: "20px"
  gutter: "32px"
  section-mobile: "64px"
  section: "80px"
  container: "1280px"
components:
  button-primary:
    backgroundColor: "{colors.bone-300}"
    textColor: "{colors.carbon-ground}"
    typography: "{typography.title}"
    rounded: "{rounded.pill}"
    height: "48px"
    padding: "0 24px"
  button-primary-compact:
    backgroundColor: "{colors.bone-300}"
    textColor: "{colors.carbon-ground}"
    rounded: "{rounded.pill}"
    height: "36px"
    padding: "0 16px"
  button-primary-hover:
    backgroundColor: "{colors.bone-200}"
    textColor: "{colors.carbon-ground}"
    rounded: "{rounded.pill}"
  button-secondary:
    backgroundColor: "transparent"
    textColor: "{colors.bone-200}"
    rounded: "{rounded.pill}"
    height: "48px"
    padding: "0 20px"
  link-text:
    backgroundColor: "transparent"
    textColor: "{colors.bone-200}"
    typography: "{typography.body-s}"
  chip:
    backgroundColor: "transparent"
    textColor: "{colors.bone-300}"
    typography: "{typography.mono}"
    rounded: "{rounded.pill}"
    height: "32px"
    padding: "0 12px"
  chip-selected:
    backgroundColor: "{colors.carbon-ground-3}"
    textColor: "{colors.bone-100}"
    rounded: "{rounded.pill}"
    height: "32px"
    padding: "0 12px"
  card-surface:
    backgroundColor: "{colors.carbon-ground-4}"
    textColor: "{colors.bone-100}"
    rounded: "{rounded.surface}"
    padding: "20px"
  card-surface-raised:
    backgroundColor: "{colors.carbon-ground-3}"
    textColor: "{colors.bone-100}"
    rounded: "{rounded.surface-raised}"
    padding: "24px"
  control-icon:
    backgroundColor: "transparent"
    textColor: "{colors.bone-300}"
    rounded: "{rounded.pill}"
    size: "36px"
---

# Design System: helios°

## Overview

**Creative North Star: "The Instrument Panel at Night"**

helios° looks like a precision instrument read in low light: a near-black carbon ground, warm bone text, and one amber signal that means "the sun is doing something". Density is high but ordered; every measured value is set in tabular JetBrains Mono so columns of numbers line up and a change since the last read is legible at a glance. Display type is Plus Jakarta Sans at 700–800 with tight tracking, used sparingly for one statement per view. The system was established by the app's Dashboard (`/app`) and the landing page inherits it whole; the landing added composition, not identity.

The world is honest by construction. Demonstration data is labeled simulated; the only colors besides carbon and bone are the five signals (solar, flow, grid, battery, alert), each bound to a physical meaning; nothing is decorated for its own sake. The previous landing's vocabulary — preloader, 3D device, marquee, shimmer text, invented testimonials — is the confirmed anti-reference.

**Key Characteristics:**
- One brightest element per view (the live reading); headlines sit one step down at bone-200, filled buttons at bone-300, and everything else steps down to bone-500.
- Measured values always in mono with `tnum`; prose never carries a number that the mono column could.
- Amber is a signal, not a theme: it owns exactly one page-scale field (the peak-sun passage) and otherwise appears as dots, arcs and connector strokes.
- A visible 24px dot grid (dots at 10% bone) is the armature; ledger rows are 48px (two cells) and snap their top hairline to a dot row.
- Motion is functional: the two-second poll tick and a scroll-lit day stroke. Nothing is hidden behind an entrance.

## Colors

A carbon-and-bone neutral pair with five meaning-bound signal hues; light theme swaps to warm paper and near-black ink using the same roles.

### Primary
- **Signal Solar** (`{colors.signal-solar}` dark / `{colors.signal-solar-light}` light): the sun's own color. Live-reading emphasis, the poll arc, the row dot and dashed connector from register to insight, selection highlight, focus ring, and the one committed field (the "One simulated day" section). White-label brands replace this value through `--signal-solar` and `brand.accent`.

### Secondary
- **Signal Flow** (`{colors.signal-flow}`): energy delivered to the house; positive insight dots; the "connected" dot in the ledger header.
- **Signal Grid** (`{colors.signal-grid}`): grid import/export only.
- **Signal Battery** (`{colors.signal-battery}`): battery ring, strategy radio, battery power.
- **Signal Alert** (`{colors.signal-alert}`): critical insight dot and fault state only.

### Neutral
- **Carbon Ground** (`{colors.carbon-ground}`): the page field. Steps up to ground-1 (alternate sections), ground-3 (hot ledger rows, selected chips), ground-4 (surface tops).
- **Bone 100** (`{colors.bone-100}`): the live reading, featured titles, card titles and the wordmark; the brightest value on any view and reserved for what is measured or derived.
- **Bone 200 / Bone 300** (`{colors.bone-200}` / `{colors.bone-300}`): page headlines (bone-200), body copy and secondary readings (bone-300), and the filled primary button (bone-300 with carbon text, lifting to bone-200 on hover) so a control never outshines a reading.
- **Bone 400** (`{colors.bone-400}`): labels, raw register values, section links.
- **Bone 500 / Bone 500 landing** (`{colors.bone-500}` / `{colors.bone-500-landing}`): quietest labels. The app uses bone-500; the landing lifts it to `#918c7d` (light: `#6b665a`) inside `.lp` so 10–13px labels clear 4.5:1 on every landing surface.
- **Field Ink** (`{colors.field-ink}`): text on an accent field. Secondary field text is `color-mix(in srgb, ink 76%, field)`; hairlines on the field are ink at 20%.
- **Paper Ground / Paper Surface** (`{colors.paper-ground}` / `{colors.paper-surface}`): light-theme field and card top; ink-100..500 mirror the bone ramp.

### Named Rules
**The One Signal Rule.** Amber appears at page scale exactly once per page (the peak-sun field); everywhere else it is a dot, a 1.25px stroke, or a single word. Its rarity is what makes the field land.
**The Meaning-Bound Hue Rule.** Flow, grid, battery and alert are never used decoratively; a colored element must be about the thing the color names.
**The Field Contrast Rule.** On any brand accent field, body text is ink at ≥76% mix and headlines are pure ink; this holds ≥4.9:1 on all four shipped brands.

## Typography

**Display Font:** Plus Jakarta Sans (with Inter, system-ui)
**Body Font:** Plus Jakarta Sans (with Inter, system-ui)
**Label/Mono Font:** JetBrains Mono (with ui-monospace)

Both faces are self-hosted variable fonts (400–800, latin + latin-ext, Greek for JetBrains Mono) in `public/fonts/`, declared in `src/fonts.css`; nothing is fetched from a font CDN.

**Character:** A geometric humanist sans carries both display and body, so hierarchy comes from weight, size and value rather than a second voice; the mono face is the second voice and it is reserved for things that were measured.

### Hierarchy
- **Display** (700, 4.25rem desktop → 3.25rem tablet → 2.25rem phone, 1.02, −0.03em, `text-wrap: balance`): one statement per page; the H1's first line is bone-200 and its second line bone-400 so the live reading below stays brightest.
- **Headline** (700, 2.6rem → 2.25rem, 1.02, −0.03em): section openers; the featured insight title (2.6rem → 2.25rem → 1.75rem) is the one bone-100 headline on the page.
- **Numeral** (800, 3.75rem, 1, −0.04em): the live kW reading and the battery ring's percentage inside the dashboard frame.
- **Metric** (mono 400, 22px; 18px in the day moments): the featured insight's figure and the lead register's reading in the ledger.
- **Title** (600, 16px, 1.35): card, node and column titles.
- **Body** (400, 16px; 15px in cards, 14px in the three-column notes; 1.6; max 32–34rem ≈ 60–66 characters): explanatory paragraphs.
- **Mono** (400, 13px, `tnum zero`): register readings, facts, decoded values; 11px for register points, raw values, clocks and captions.
- **Label** (400, 12px): row labels, chips, footer; the app's `label-cap` is 10px mono uppercase at 0.3em and is also the unit glyph size.

### Named Rules
**The Measured-Means-Mono Rule.** Any value that came from a register, a clock or a calculation is set in JetBrains Mono with tabular figures; prose never restates it.
**The Tracking Floor Rule.** Display tracking is −0.03em and numerals −0.04em; nothing goes tighter.

## Layout

Content sits in a 1280px container with 20px gutters on phones and 32px from `md` (768px). The visible 24px dot grid (`radial-gradient` 1px dots at 10% bone, 11% ink in light) is anchored to the container edge and the ledger snaps its first row's hairline onto a dot row; ledger rows are 48px, section padding is 64px on phones and 80px from `md`, and internal spacing runs in 24px multiples. Twelve-column grids split instrument views 5/12 + 7/12 (ledger rows / insight; copy / phone) and collapse to a single column below `md` (ledger) or `lg` (1024px, instrument). The day stroke is a measured-width SVG: 150px tall under 640px, 210px to 1024px, 250px above, with the five moments on a 5-column row that only shows leader lines at `lg`. The app itself keeps a phone-width frame centered on desktop; the landing shows it at true size (392px).

## Elevation & Depth

Tonal layering first, shadows second. Depth is carried by the carbon steps (ground → ground-1 → ground-3 → ground-4) and 1px hairlines at 6% bone; surfaces add a soft, offset-and-blurred drop shadow plus a 1px inner top highlight so a card reads as a plate lifted off the ground rather than a glow.

### Shadow Vocabulary
- **Surface raised** (`box-shadow: 0 24px 48px -16px rgba(0,0,0,0.6), inset 0 1px 0 0 rgba(239,236,229,0.06)`; light: `0 12px 28px -12px rgba(11,11,12,0.12), inset 0 1px 0 0 rgba(255,255,255,0.6)`): the insight card, path nodes, the dashboard card.
- **Surface inner** (`inset 0 1px 0 0 rgba(239,236,229,0.04)`): flat surfaces (battery module).
- **Device frame** (`0 48px 96px -32px rgba(0,0,0,0.7), 0 0 0 1px hairline`; light: `0 32px 64px -28px rgba(11,11,12,0.35)`): the phone frame only.

### Named Rules
**The Plate Not Glow Rule.** Shadows always have a vertical offset and a blur larger than the offset; no symmetric glows, no zero-offset halos.
**The Hairline Rule.** Dividers are 1px at 6% bone (15% for strong); never a 2px or solid-gray line.

## Shapes

Soft rectangles with a fixed radius family: 12px for controls (strategy radios), 18px for flat surfaces, 20px for raised surfaces, 40px for the phone frame, full pills for buttons, chips and status. Dots (6–8px) mark states; dashed 1px strokes (`2 5` and `2 4` patterns) mean "derived from"; the eight-blade aperture mark is the only ornament. No hard corners, no bevels, no imitation material.

## Components

### Buttons
- **Shape:** full pill.
- **Primary:** bone-300 fill, carbon text, 48px tall (36px compact in the header), 15px/13px medium; arrow icon at 16px/14px, stroke 2. Hover: fill lifts to bone-200. Focus: 2px amber outline at 3px offset. The fill is deliberately one step below the reading so the control never outshines the instrument.
- **Text link:** bone-200 with a hairline underline at 6px offset and a 14px arrow; hover lifts text to bone-100 and the underline to bone-300. Used for the second action beside a primary pill.
- **Secondary pill:** transparent, 1px strong hairline, bone-200 text; hover lifts border to bone-500 and text to bone-100 (the "Add to home screen" disclosure).
- **Compact controls (Pause / Next read):** 28px pill, mono 11px, bone-300, icon-only under 640px with a constant `aria-label`; `aria-pressed` on the pause toggle; a `::after` hit area extends every compact control to ≥44px.

### Chips
- **Style:** 32px pill, 1px hairline, 12px, bone-300, an 8px brand-colored dot leading the name.
- **State:** selected = ground-3 fill, strong hairline, bone-100 text, `aria-current`.

### Cards / Containers
- **Corner Style:** 20px raised / 18px flat.
- **Background:** raised = gradient ground-4 → ground-3 at 90%; flat = gradient 26/26/28 → 15/15/16 at 90% (paper white → paper surface in light).
- **Shadow Strategy:** see Elevation; raised gets the drop shadow, flat gets the inner highlight only.
- **Border:** 1px hairline.
- **Internal Padding:** 20px phones, 24–32px desktop.

### Inputs / Fields
- **Style:** (app Settings) carbon-900 at 80%, 1px hairline, 8px radius, mono 13px.
- **Focus:** border lifts to bone-700 at 40%; the landing adds a 2px amber outline at 3px offset via `.lp :focus-visible`.

### Navigation
- **Style:** sticky 64px bar, ground at 95%, 1px hairline below; mark + wordmark left, 13px bone-400 section links center (hidden under `md`), theme toggle and compact primary right. Links lift to bone-100 on hover.

### Register Ledger (signature)
Two columns on one 2-second clock. Left: a `<ol>` of 48px rows — SunSpec model·point (mono 11px), plain label (12px bone-500), raw register value (mono 11px bone-400), decoded reading (mono 13px) with the change since the last read beneath (mono 10px). Rows that feed the current insight are "hot": ground-3 fill, amber dot, bone-100 text; the lead source row's reading steps up to the 22px metric size. Right: a raised card with the featured insight (headline + body + metric/delta + "from W · GHI · St"), followed on desktop by up to three "also noticed" lines; when the engine has nothing to say the card says "Nothing to act on." and reads the battery and load registers instead. The savings insight is never featured on the landing because its dollar figure depends on an unstated tariff. An SVG overlay draws a 1.25px amber dashed cubic from each hot row to the card, re-measured on every tick with a 0.55s expo-out; below 768px the card comes first, the connectors are dropped and the hot rows' amber dots carry the pairing. The header carries the connection line, the simulated-day disclaimer, a 14px poll arc that fills over 2s, the simulated clock, and Pause/Resume (Next read under reduced motion). Pauses when the tab is hidden.

### Signal Path (connect passage)
Three nodes on one dashed amber line in the ledger's connector language (`.dash-h` / `.dash-v`: 2px dash, 5px gap, 1px): an 8px amber dot per node, a 16px title, mono 11px spec lines and a 13px body; horizontal across three columns from 768px, vertical down the left edge below. No containers.

### Day Stroke (signature)
A page-scale accent field (`--field` = brand accent, `--on-field` = ink) with a measured-width SVG: 2px ink production stroke lit by scroll (`pathLength` 0→1 between 80% and 20% of the viewport), 1px dashed home-load stroke, 7% ink area wash, 24h axis in mono, five 8px ink markers with 1px dashed leaders to a 5-column moment row.

## Do's and Don'ts

### Do:
- **Do** keep one brightest element per view (the live reading in bone-100); headlines take bone-200 and filled controls bone-300.
- **Do** set every measured value in JetBrains Mono with `tnum`, and show change as a signed delta in the same column.
- **Do** anchor layouts to the 24px dot grid: 48px rows, 24px multiples for internal spacing, 1px hairlines.
- **Do** use offset + blurred shadows (the Surface raised token) and 1px hairlines for depth; use tonal steps before shadows.
- **Do** label simulated data in the frame that shows it ("simulated", "Simulated 9.6 kW system").
- **Do** respect `prefers-reduced-motion`: the final state is static and stepping is manual.

### Don't:
- **Don't** use signal hues decoratively; each of flow/grid/battery/alert must name the thing it colors.
- **Don't** put amber on more than one page-scale region per page.
- **Don't** use gradient text, glass or blur as decoration, glyph icons in place of the aperture mark, or a preloader.
- **Don't** invent claims: no testimonials, customer names, counts, benchmarks, prices or store badges (the honest maximum is "iOS and Android apps are in development").
- **Don't** set body text wider than ~34rem or display tracking tighter than −0.04em.
- **Don't** load fonts, scripts or images from a third-party origin; the only network request the product makes is the Open-Meteo forecast.
