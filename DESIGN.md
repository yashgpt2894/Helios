---
name: helios° landing sheet
description: The retroreflective placard system of the / surface of helios°, scoped to .pc beside the app's own system.
colors:
  field: "#efc400"
  headlamp-field: "#0a0a0b"
  field-2: "#e5bb00"
  headlamp-field-2: "#0f0f10"
  ink: "#14120a"
  headlamp-ink: "#f4f1ea"
  ink-2: "#463f2a"
  headlamp-ink-2: "#dcd6c8"
  ink-3: "#524a2f"
  headlamp-ink-3: "#a59f90"
  rule: "#14120a"
  headlamp-rule: "#f4f1ea"
  rule-quiet: "rgba(20, 18, 10, 0.24)"
  headlamp-rule-quiet: "rgba(239, 236, 229, 0.16)"
  blaze: "rgba(255, 249, 214, 0.62)"
  headlamp-blaze: "rgba(245, 197, 24, 0.18)"
  plate: "#14120a"
  headlamp-plate: "#121215"
  plate-ink: "#f6d64a"
  headlamp-plate-ink: "#f5c518"
  plate-ink-2: "#c9b478"
  headlamp-plate-ink-2: "#bdb6a6"
  plate-rule: "rgba(246, 214, 74, 0.34)"
  headlamp-plate-rule: "rgba(245, 197, 24, 0.28)"
  danger: "#c8102e"
  headlamp-danger: "#d9302f"
  danger-ink: "#ffffff"
  headlamp-danger-ink: "#ffffff"
  warning: "#e2711d"
  headlamp-warning: "#f08a24"
  warning-ink: "#14120a"
  headlamp-warning-ink: "#0a0a0b"
  caution: "#14120a"
  headlamp-caution: "#f5c518"
  caution-ink: "#efc400"
  headlamp-caution-ink: "#0a0a0b"
  notice: "#0b5da7"
  headlamp-notice: "#2f72ce"
  notice-ink: "#ffffff"
  headlamp-notice-ink: "#ffffff"
  guide: "#146b3a"
  headlamp-guide: "#1e8e54"
  guide-ink: "#ffffff"
  headlamp-guide-ink: "#0a0a0b"
  solar: "#f0c674"
  flow: "#7fb069"
  grid: "#5d8aa8"
  battery: "#c5a572"
  mark-ink: "#14120a"
  headlamp-mark-ink: "#0a0a0b"
typography:
  display:
    fontFamily: "Barlow Condensed, 'Arial Narrow', system-ui, sans-serif"
    fontSize: "clamp(2.3rem, 6.6vw, 4.9rem)"
    fontWeight: 700
    lineHeight: 0.92
    letterSpacing: "-0.005em"
  statement:
    fontFamily: "Barlow Condensed, 'Arial Narrow', system-ui, sans-serif"
    fontSize: "clamp(1.5rem, 3.1vw, 2.5rem)"
    fontWeight: 700
    lineHeight: 1
    letterSpacing: "-0.005em"
  headline:
    fontFamily: "Barlow Condensed, 'Arial Narrow', system-ui, sans-serif"
    fontSize: "clamp(1.85rem, 4vw, 3.1rem)"
    fontWeight: 700
    lineHeight: 0.95
    letterSpacing: "-0.005em"
  plate-state:
    fontFamily: "Barlow Condensed, 'Arial Narrow', system-ui, sans-serif"
    fontSize: "clamp(1.9rem, 3.6vw, 2.8rem)"
    fontWeight: 700
    lineHeight: 1
    letterSpacing: "0.005em"
  lead:
    fontFamily: "Barlow Signage, 'Helvetica Neue', Arial, sans-serif"
    fontSize: "clamp(1.02rem, 1.15vw, 1.19rem)"
    fontWeight: 400
    lineHeight: 1.5
    letterSpacing: "normal"
  body:
    fontFamily: "Barlow Signage, 'Helvetica Neue', Arial, sans-serif"
    fontSize: "1rem"
    fontWeight: 400
    lineHeight: 1.55
    letterSpacing: "normal"
  note:
    fontFamily: "Barlow Signage, 'Helvetica Neue', Arial, sans-serif"
    fontSize: "0.875rem"
    fontWeight: 400
    lineHeight: 1.5
    letterSpacing: "normal"
  label:
    fontFamily: "Plate Mono, ui-monospace, SFMono-Regular, monospace"
    fontSize: "12px"
    fontWeight: 500
    lineHeight: 1.4
    letterSpacing: "0.12em"
    fontFeature: "'tnum' 1"
  measurement:
    fontFamily: "Plate Mono, ui-monospace, SFMono-Regular, monospace"
    fontSize: "clamp(2.4rem, 5.4vw, 3.6rem)"
    fontWeight: 700
    lineHeight: 0.9
    letterSpacing: "-0.02em"
    fontFeature: "'tnum' 1"
rounded:
  none: "0px"
spacing:
  rail: "clamp(18px, 4.6vw, 76px)"
  field-block: "clamp(30px, 4.4vw, 62px)"
  action-line: "clamp(30px, 4vw, 54px)"
  rule-heavy: "3px"
  rule-hair: "1px"
components:
  placard-ground:
    backgroundColor: "{colors.field}"
    textColor: "{colors.ink}"
    rounded: "{rounded.none}"
    padding: "16px 0 clamp(56px, 8vw, 120px)"
  issuer-strip:
    backgroundColor: "{colors.field}"
    textColor: "{colors.ink}"
    padding: "16px 0 14px"
  signal-band:
    backgroundColor: "{colors.notice}"
    textColor: "{colors.notice-ink}"
    rounded: "{rounded.none}"
    padding: "12px 20px 10px"
  field-header:
    textColor: "{colors.ink}"
    padding: "clamp(30px, 4.4vw, 62px) 0 0"
  field-id-chip:
    backgroundColor: "{colors.ink}"
    textColor: "{colors.field}"
    rounded: "{rounded.none}"
    padding: "4px 9px 3px"
  button-primary:
    backgroundColor: "{colors.ink}"
    textColor: "{colors.field}"
    rounded: "{rounded.none}"
    padding: "13px 22px 12px"
  button-primary-hover:
    backgroundColor: "{colors.field}"
    textColor: "{colors.ink}"
    rounded: "{rounded.none}"
    padding: "13px 22px 12px"
  button-ghost:
    backgroundColor: "transparent"
    textColor: "{colors.ink}"
    rounded: "{rounded.none}"
    padding: "13px 22px 12px"
  button-ghost-hover:
    backgroundColor: "{colors.ink}"
    textColor: "{colors.field}"
    rounded: "{rounded.none}"
    padding: "13px 22px 12px"
  button-disabled:
    backgroundColor: "transparent"
    textColor: "{colors.ink-3}"
    rounded: "{rounded.none}"
    padding: "13px 22px 12px"
  instrument-plate:
    backgroundColor: "{colors.plate}"
    textColor: "{colors.plate-ink}"
    rounded: "{rounded.none}"
    padding: "clamp(14px, 1.8vw, 22px) clamp(15px, 1.9vw, 24px) clamp(14px, 1.8vw, 20px)"
  plate-readout:
    textColor: "{colors.plate-ink}"
    typography: "{typography.measurement}"
  plate-grid-cell:
    backgroundColor: "{colors.plate}"
    textColor: "{colors.plate-ink}"
    padding: "9px 11px 10px"
  reading-selector:
    backgroundColor: "transparent"
    textColor: "{colors.ink}"
    height: "26px"
  range-thumb:
    backgroundColor: "{colors.ink}"
    rounded: "{rounded.none}"
    width: "12px"
    height: "34px"
  day-band:
    backgroundColor: "{colors.field}"
    textColor: "{colors.ink}"
    rounded: "{rounded.none}"
  band-readout-chip:
    backgroundColor: "{colors.ink}"
    textColor: "{colors.field}"
    rounded: "{rounded.none}"
    padding: "5px 9px 4px"
  conductor-node-source:
    backgroundColor: "{colors.field}"
    textColor: "{colors.ink}"
    rounded: "{rounded.none}"
    padding: "12px 16px 13px"
  conductor-node-device:
    backgroundColor: "{colors.plate}"
    textColor: "{colors.plate-ink}"
    rounded: "{rounded.none}"
    padding: "12px 16px 13px"
  link-rail:
    backgroundColor: "{colors.ink}"
    width: "3px"
  notice-word-danger:
    backgroundColor: "{colors.danger}"
    textColor: "{colors.danger-ink}"
    rounded: "{rounded.none}"
    padding: "6px 11px 5px"
  notice-word-warning:
    backgroundColor: "{colors.warning}"
    textColor: "{colors.warning-ink}"
    rounded: "{rounded.none}"
    padding: "6px 11px 5px"
  notice-word-caution:
    backgroundColor: "{colors.caution}"
    textColor: "{colors.caution-ink}"
    rounded: "{rounded.none}"
    padding: "6px 11px 5px"
  notice-word-notice:
    backgroundColor: "{colors.notice}"
    textColor: "{colors.notice-ink}"
    rounded: "{rounded.none}"
    padding: "6px 11px 5px"
  notice-word-guide:
    backgroundColor: "{colors.guide}"
    textColor: "{colors.guide-ink}"
    rounded: "{rounded.none}"
    padding: "6px 11px 5px"
  notice-metric:
    textColor: "{colors.ink}"
    typography: "{typography.measurement}"
    padding: "0 0 0 14px"
  label-tile:
    backgroundColor: "{colors.field}"
    textColor: "{colors.ink}"
    rounded: "{rounded.none}"
    padding: "clamp(16px, 2vw, 24px) clamp(16px, 2vw, 24px) clamp(18px, 2.2vw, 26px)"
  formula-strip:
    backgroundColor: "{colors.ink}"
    textColor: "{colors.field}"
    rounded: "{rounded.none}"
    padding: "9px 11px 8px"
  amendment-sticker:
    backgroundColor: "{colors.ink}"
    textColor: "{colors.field}"
    rounded: "{rounded.none}"
    padding: "14px 18px 15px"
  sheet-position-rule:
    backgroundColor: "{colors.ink}"
    rounded: "{rounded.none}"
    height: "3px"
  sheet-position-name:
    backgroundColor: "{colors.ink}"
    textColor: "{colors.field}"
    rounded: "{rounded.none}"
    padding: "3px 8px 2px"
  logoblock:
    backgroundColor: "#0b0b0c"
    textColor: "{colors.plate-ink}"
    rounded: "{rounded.none}"
    width: "34px"
    height: "34px"
  mark-chip-solar:
    backgroundColor: "{colors.solar}"
    textColor: "{colors.mark-ink}"
    rounded: "{rounded.none}"
    padding: "3px 8px 2px"
  mark-chip-flow:
    backgroundColor: "{colors.flow}"
    textColor: "{colors.mark-ink}"
    rounded: "{rounded.none}"
    padding: "3px 8px 2px"
  mark-chip-grid:
    backgroundColor: "{colors.grid}"
    textColor: "{colors.mark-ink}"
    rounded: "{rounded.none}"
    padding: "3px 8px 2px"
  mark-chip-battery:
    backgroundColor: "{colors.battery}"
    textColor: "{colors.mark-ink}"
    rounded: "{rounded.none}"
    padding: "3px 8px 2px"
  brand-flag:
    backgroundColor: "{colors.ink}"
    width: "26px"
    height: "12px"
  legend-swatch:
    backgroundColor: "{colors.ink}"
    width: "28px"
    height: "14px"
---

# Design System: helios° landing sheet

## Overview

**Creative North Star: "The Retroreflective Placard"**

**This file records a scoped surface system, not the app's system.** `helios°` ships two visual systems side by side and they share no tokens. The app's system is the incumbent one and it stays untouched: the `:root` custom properties in `src/index.css` (carbon and bone ramps, the `--signal-*` marks, the `--surface-*` elevation set), the `carbon` / `bone` / `signal` palette and the Inter, Instrument Serif and JetBrains Mono families in `tailwind.config.js`, and the `.surface` component classes. It owns `/app` and `/share/:payload`. What follows documents the second system: the landing sheet at route `/`, a self-contained world scoped to the `.pc` class, carried by `src/landing.css` (all tokens and every component class, prefix `.pc-`), composed by `src/pages/Landing.tsx` from six components in `src/components/landing/`, and fed by the deterministic site model in `src/lib/placardModel.ts` and the hooks in `src/hooks/`. Apply these rules inside `.pc` and nowhere else. Do not carry a `.pc-*` value into the app, and do not import an app token into the sheet.

The sheet is a residential photovoltaic installation's marking, not a dashboard. The ground is one saturated signal yellow, the ink is black silkscreen, and every reading on the page is a field printed on that sheet: the selected hour, the AC output, the array's DC power, the heatsink temperature, the four paths of energy, the notice set. Six numbered fields run top to bottom, each opening on a 3px rule, every one of them answering to the single hour control in field 02. Nothing floats above the sheet and nothing is a card inside a card; a figure that has no source and no time stamp does not ship. Severity is named rather than decorated: the sheet borrows the ANSI Z535 signal registers (DANGER, WARNING, CAUTION, NOTICE, GUIDE) and the placard's own conventions, including a rotating beacon of a readout and leader lines that name each path with its unit.

The material has two states, and they are the same sheet. **Daylight** is the sheeting in sun: `{colors.field}` ground, `{colors.ink}` ink. **Headlamp** is that same sheeting at night with only the lamp's cone lit: near-black ground, retroreflective ink blazing at `{colors.headlamp-ink}`. Both states declare the same token names in two blocks (`.pc` and `[data-theme='dark'] .pc`), so every component is populated twice and nothing is removed at night. The flip is in the ground and the ink, not in the content. The sheet joins the app's existing theme contract (`document.documentElement[data-theme]`, `localStorage['helios-theme']`, `auto | light | dark`) instead of inventing a second one; the two controls in the issuer strip are labelled Daylight and Headlamp, and their state is exposed as `aria-pressed`.

**Font provenance.** The three faces are self-hosted woff2 files in `public/fonts/`, retrieved from Google Fonts for this build in the `latin` subset only: `barlow-condensed-500/600/700.woff2`, `barlow-400/500/600.woff2` and `jetbrains-mono-var.woff2`. `src/landing.css` declares all seven `@font-face` blocks with `font-display: swap` and a latin `unicode-range`; the page makes no font request to a CDN at runtime, which is what the offline-first PWA requires. The annotation face is Barlow renamed to `Barlow Signage` so that it is addressed by its job on the sheet rather than by its foundry name.

The build is code-led. This machine has no image generation, so there is no comp and no photography, and the world is drawn entirely in CSS, inline SVG pictograms (`src/components/landing/Pictograms.tsx`) and real CSS-pixel geometry: the day band measures its own box with a `ResizeObserver` (`src/hooks/useElementSize.ts`) and draws in those pixels, so the printed hatching stays square at any width and the SVG needs no `preserveAspectRatio` guesswork.

**Key Characteristics:**
- One saturated field owns whole regions. Signal yellow is the ground, not an accent sprinkled on a neutral.
- Exactly two rule weights: 3px for structure, 1px for interiors. No third stroke exists.
- Square corners only. The only radius declarations in the stylesheet are literal `0`.
- Every measurement is Plate Mono with tabular numerals: kW, kWh, V, A, W/m², °C, every clock stamp and every reference line.
- Direction and severity are words first. Colour repeats the word; it never carries meaning alone.
- Flat printed ink. The single non-inset shadow in the system belongs to the amendment sticker, because a pasted sticker physically lifts off the sheeting.
- Two states of one material, Daylight and Headlamp, both fully populated.
- Nothing is legible only because of the blaze layer, and no text renders below 12px.

## Colors

A drenched yellow field of retroreflective sheeting, black silkscreen printing on top of it, one black instrument window cut into the sheet, and the five ANSI Z535 registers held back for state.

### Primary
- **Sheeting Yellow** (#efc400 / `{colors.field}`): the ground of the whole light sheet, edge to edge, behind every field. It is the subject's own material and the page's largest colour by far. **Night Field** (#0a0a0b / `{colors.headlamp-field}`) is the same role after dark.
- **Sheeting Yellow, Shaded** (#e5bb00 / `{colors.field-2}`): the second ground, used where the browser's own surfaces need to sit on the sheet (the scrollbar trough and its thumb border, with the flat value repeated as a fallback). **Night Field, Raised** (#0f0f10 / `{colors.headlamp-field-2}`) is its dark counterpart, deliberately the same value the app already uses for its second dark ground.

### Secondary
- **Plate Black** (#14120a / `{colors.plate}`): the instrument window in field 01 and any conductor node that represents a device. It is a printed black rectangle, not a raised surface; it carries no radius and no shadow. **Plate Black, Night** (#121215 / `{colors.headlamp-plate}`) stays near-black so the lamp-lit amber keeps its full punch.
- **Retro Amber** (#f6d64a / `{colors.plate-ink}`): every value, label and tick inside the plate. It is the ink, not an accent. **Lamp Amber** (#f5c518 / `{colors.headlamp-plate-ink}`) is the same role at night.
- **Amber Print, Dimmed** (#c9b478 / `{colors.plate-ink-2}`): secondary plate type (units, the scale legend, the state sub-line). Night value #bdb6a6 / `{colors.headlamp-plate-ink-2}`.
- **Plate Hairline** (rgba(246, 214, 74, 0.34) / `{colors.plate-rule}`): the tick grid, the plate's internal 1px divisions and the 1px gap grounds inside the plate. Night value rgba(245, 197, 24, 0.28) / `{colors.headlamp-plate-rule}`.

### Tertiary
The semantic registers. Each one is a **filled band with reversed type**, and each keeps the same meaning in both states.
- **Danger Red** (#c8102e / `{colors.danger}`, reversed white): spent once, on the one hazard that is always true of a photovoltaic system, in field 04's opening block. Night value #d9302f / `{colors.headlamp-danger}`.
- **Warning Orange** (#e2711d / `{colors.warning}`, reversed `{colors.ink}`): conditions to act on before dark (string imbalance). Night value #f08a24 / `{colors.headlamp-warning}`.
- **Caution Yellow** (#efc400 / `{colors.caution-ink}`) and **Caution Ground** (#14120a / `{colors.caution}`): the caution register is the sheeting's own colour, so in Daylight the pair is inverted — a black band carrying the yellow word (`{colors.caution}` ground, `{colors.caution-ink}` word) — which is what lets the band hold its own edge against a sheet of the same yellow. In Headlamp it prints as the conventional solid yellow band with ink type (#f5c518 / `{colors.headlamp-caution}` carrying #0a0a0b / `{colors.headlamp-caution-ink}`). Both states use the same two colours; only which one is the ground changes, and the word is what carries the severity.
- **Notice Blue** (#0b5da7 / `{colors.notice}`, reversed white): information that is not a hazard, including the sheet's own top signal band ("Photovoltaic system present · Dual supply"). Night value #2f72ce / `{colors.headlamp-notice}`.
- **Guide Green** (#146b3a / `{colors.guide}`, reversed white): the resting state and the positive instruction. Night value #1e8e54 / `{colors.headlamp-guide}`.
- **The room marks**, kept from the product's own signal set and re-declared inside `.pc` so the sheet never depends on the app's stylesheet: **Solar Amber** (#f0c674 / `{colors.solar}`), **Flow Green** (#7fb069 / `{colors.flow}`), **Grid Blue** (#5d8aa8 / `{colors.grid}`) and **Battery Tan** (#c5a572 / `{colors.battery}`), each filled with **Mark Ink** (#14120a / `{colors.mark-ink}`, night #0a0a0b / `{colors.headlamp-mark-ink}`). They appear only on the four conductor chips (Solar, House, Battery, Utility), always beside a word that already says what the chip means — the label sheet's brand swatches are a separate, supplied accent, not one of these marks. The `?brand=` accent arrives as an inline style on the two identity surfaces (the issuer flag stripe and those swatches) and is never load-bearing.
- **Daylight Blaze** (rgba(255, 249, 214, 0.62) / `{colors.blaze}`) and **Headlamp Blaze** (rgba(245, 197, 24, 0.18) / `{colors.headlamp-blaze}`): the two values of the sheeting's retroreflective response, painted by the fixed `.pc-sheen` layer as a 28rem radial. They are not text or border colours and they are never the reason something can be read.

### Neutral
- **Silkscreen Black** (#14120a / `{colors.ink}`): display type, body emphasis, rules, every reversed band's border. Night **Blaze Bone** (#f4f1ea / `{colors.headlamp-ink}`).
- **Printed Grey** (#463f2a / `{colors.ink-2}`): prose, notice bodies, secondary values, every aside tag (6.27:1 on the field). Night #dcd6c8 / `{colors.headlamp-ink-2}`.
- **Faded Ink** (#524a2f / `{colors.ink-3}`): quiet labels, deltas, disabled control text, the note that admits what the page cannot prove. Night #a59f90 / `{colors.headlamp-ink-3}`.
- **Rule Black** (#14120a / `{colors.rule}`): the 3px field rules, the issuer and footer rules, the action line. Night #f4f1ea / `{colors.headlamp-rule}`.
- **Ink Wash** (rgba(20, 18, 10, 0.24) / `{colors.rule-quiet}`): every 1px interior division, the field-set gap grounds, the range track rules. Night rgba(239, 236, 229, 0.16) / `{colors.headlamp-rule-quiet}`.

### Measured contrast
Ratios are contrasts between the shipped token values over the colours that actually sit behind them (WCAG relative luminance over the composited colour); they were taken in real Chrome against computed styles and re-checked against the token values in `src/landing.css`.
- **Daylight:** ink on field **11.21:1**, ink-2 **6.27:1**, ink-3 **5.28:1**; plate-ink on plate **13.06:1**, plate-ink-2 **9.18:1**; every signal band passes — danger **5.88:1**, warning **5.90:1**, caution **11.21:1** (the yellow word on its ink ground, the same pair as ink on field, reversed), notice **6.69:1**, guide **6.57:1**.
- **Headlamp:** ink **17.54:1**, ink-2 **13.66:1**, ink-3 **7.50:1**; plate-ink **11.47:1**, plate-ink-2 **9.27:1**; bands — danger **4.76:1**, warning **7.88:1**, caution **12.14:1**, notice **4.75:1**, guide **4.76:1**.
- **Worst case, with the blaze layer composited over the ground:** headlamp ink-3 **5.27:1** (5.28:1 recomputed), daylight ink-3 **6.96:1** recomputed at the shipped daylight blaze of 0.62 — the earlier Chrome pass recorded **6.73:1** against the 0.55 it replaced. The blaze alphas are capped by this measurement rather than by taste: at 0.22 on the night ground the quietest text token drops to 4.73:1, which is why the shipped night value is 0.18.
- That floor is why `{colors.ink-3}` (night `{colors.headlamp-ink-3}`) is the darkest ink allowed on a body-sized label and why disabled button text is allowed to use it. No text anywhere on the sheet renders below 12px (verified against the built page).

### Named Rules
**The Drenched Field Rule.** One saturated field owns a whole region. Yellow fills the sheet or a band edge to edge; it is never a thin accent line on a neutral panel, and no register colour appears as a tint, a gradient stop or a small decorated dot.
**The Reversed Signal Rule.** A signal word is always set inside its own register colour with reversed type, at the same size in both light states, and it carries the same meaning in Daylight and Headlamp. The pair may invert where the sheet demands it (Daylight's caution band) but the word stays inside its own colour and the word still carries the severity. If a new severity needs a colour it does not have, it needs a word first.
**The Honest Register Rule.** The top signal band is NOTICE, because information is not a hazard. DANGER is spent once, on the array's DC conductors staying live whenever the modules see light, and nothing else on the sheet may use the danger register.
**The Nothing-Only-By-Blaze Rule.** The sheeting's retroreflective response is decoration. Nothing on the sheet may be legible only because of `{colors.blaze}`; the composite floor above (5.27:1 at night, 6.96:1 by day) is the number to beat before a new ink, ground or blaze alpha ships.
**The Two-State Rule.** Every colour token is declared twice, in `.pc` and in `[data-theme='dark'] .pc`. A token defined in one state only is a defect: the night sheet keeps every field, every notice and every label.

## Typography

**Display Font:** Barlow Condensed, self-hosted 500/600/700 (fallback 'Arial Narrow', system-ui)
**Body / Annotation Font:** Barlow Signage, self-hosted Barlow 400/500/600 under a renamed family (fallback 'Helvetica Neue', Arial)
**Label / Measurement Font:** Plate Mono, self-hosted JetBrains Mono variable 400–700 (fallback ui-monospace, SFMono-Regular)

**Character:** Three jobs, three faces, no overlap. Anything that would be painted or stamped on the hardware is Barlow Condensed in uppercase with tightened tracking at 0.92–1.0 line-height; anything a person reads as a sentence is the plain grotesk at 1.5–1.55; anything that is a number is monospace with tabular figures. The pairing reads as a printed placard rather than a web page, which is the point: the type is doing the job the material does.

### Hierarchy
- **Display** (700, `clamp(2.3rem, 6.6vw, 4.9rem)`, 0.92, -0.005em, uppercase): the single h1 line of field 01, "Your house has two supplies." Nothing else on the sheet reaches this size.
- **Statement** (700, `clamp(1.5rem, 3.1vw, 2.5rem)`, 1, uppercase, `{colors.ink-2}`): the sentence that follows the h1 and answers it. It is the second voice of the first viewport, not a sub-heading.
- **Headline** (700, `clamp(1.85rem, 4vw, 3.1rem)`, 0.95, uppercase): the six field titles. One per field, never two in a row.
- **Plate State** (700, `clamp(1.9rem, 3.6vw, 2.8rem)`, 1, 0.005em, uppercase): the inverter's own word — `NIGHT`, `STANDBY`, `PRODUCING`, `CURTAILED` — inside the instrument plate. A state is set like a headline because on a meter it is one.
- **Lead** (400, `clamp(1.02rem, 1.15vw, 1.19rem)`, 1.5, 58ch): the one paragraph that explains the two supplies. Line length is capped at 58ch.
- **Body** (400, 1rem, 1.55, 66ch, `{colors.ink-2}`): label-tile prose, the operating steps and the honest list.
- **Note** (400, 0.875rem, 1.5, `{colors.ink-3}`, 70ch): the caveats, including the statements that the readings are simulated.
- **Label** (500, 12px, 0.12em, uppercase, Plate Mono, tabular figures): field ids, tags, hour stamps, notice deltas, unit lines, the selector's own label, table headers. The sheet's smallest type, and its most used.
- **Measurement** (700, `clamp(2.4rem, 5.4vw, 3.6rem)`, 0.9, -0.02em, Plate Mono, tabular figures): the AC power readout. Its smaller siblings keep the same face and figures at 0.9–1.9rem: the plate's four-cell grid, the conductor values, the totals, the notice metric.

### Named Rules
**The Mono Measurement Rule.** If it is a measurement it is Plate Mono with tabular numerals: every kW, kWh, V, A, W/m², °C, every clock stamp and every reference line such as `HX-9.6 HYBRID` or `37.77, -122.42`. A figure set in the display or annotation face is a defect on this sheet; it is how a placard stops being a placard.
**The Printed Caps Rule.** Display, statement, headline and plate-state type is uppercase by class (`text-transform`), never typed in capitals by hand, and never carries sentence punctuation. Annotation and body text is sentence case with real punctuation. A paragraph is never set in the condensed caps.
**The 12px Floor Rule.** No text renders below 12px anywhere on the sheet; 12px mono is the smallest label the system owns. Long prose drops to 0.875rem, never to a smaller label, and nothing shrinks to make a layout fit.

## Layout

One sheet, one document column, no floating panels. `.pc-sheet` is centred at a maximum width of 1440px and inset by the sheet margin `{spacing.rail}` (`--pc-rail: clamp(18px, 4.6vw, 76px)`), with bottom padding `clamp(56px, 8vw, 120px)`. Inside it, six numbered fields run in order, each separated by a 3px rule (`{spacing.rule-heavy}`) spanning the full sheet width; field 01 removes its own top border because the issuer strip above it already draws that rule. Field rhythm is `{spacing.field-block}` (`clamp(30px, 4.4vw, 62px)`) above content and `clamp(34px, 4.6vw, 66px)` below, collapsing to 24px/28px below 720px. Each field head is a baseline-aligned flex row: numbered id chip, title, then a right-aligned aside pushed by `margin-left: auto`. Below 719px the aside and the issuer metadata drop to full width and left-align rather than shrinking.

Column behaviour, as built:
- Hero: one column, then two at 1040px, `minmax(0, 1.12fr) minmax(330px, 0.78fr)`. The plate column is separated by a 1px hairline and `clamp(26px, 3.6vw, 56px)` of left padding.
- Label sheet: 1 column, 2 at 700px, then a 12-column grid at 1080px with tile spans of 7, 5, 4, 4, 4 and 12. The spans are the composition; a new tile picks a span rather than a width.
- Notice rows: one column, then `128px minmax(0, 1fr) minmax(0, 210px)` at 760px, so the signal word, the message and the metric each hold a printed column.
- Danger block: one column, then `128px 1fr 0.62fr` at 820px.
- Operating steps: one column, then three equal columns at 800px.
- Honest list: one column, then `0.85fr 1.15fr` at 900px.
- Day-band totals: two columns, then four at 780px. Per-string rows: `30px 1fr auto`, then six fixed columns at 720px.

Hairlines are drawn by grid gap, not by borders on tiles: `.pc-labels`, `.pc-totals`, `.pc-grid4` and the notice list all use `gap: 1px` over a `{colors.rule-quiet}` ground with the tiles painted in the field colour. A hairline drawn this way is exactly 1px at any zoom and cannot double at a corner. Breakpoints in use: 700, 720, 760, 780, 800, 820, 900, 1040 and 1080 min-width, plus a single max-width block at 719px that carries the mobile density changes.

The sheet is 5,917px tall at 1440px across its six fields, which is why exactly two things are fixed to the viewport and nothing else. The **sheet position rule** is a 26px-tall strip at the top: a 3px `{colors.rule-quiet}` track, a 3px ink fill for scroll progress, and the current field's name reversed out of ink, faded in only after 1.2% scroll. It is `pointer-events: none` and `aria-hidden`, a printed edge marker rather than navigation; it is an approved exception to the "nothing floats" instinct, recorded because six fields need wayfinding and the sheet could not otherwise say where the reader is. The **blaze layer** (`.pc-sheen`) is a fixed full-viewport gradient. The sheet itself has no sticky header, no floating action and no drawer.

The document-level ground (`html.pc-root`, `html.pc-root body` and the document scrollbar) sits outside the `.pc` scope where the tokens are defined, so it declares its own `--pc-doc-ground` and `--pc-doc-thumb` per state: one declaration per state, not a second copy of every token. Two tokens the earlier draft of this file recorded as unused, `--pc-field-gap` and `--pc-brand-accent`, were removed from the stylesheet after the finish review; they no longer exist.

### Named Rules
**The Sheet Margin Rule.** The sheet is inset by one margin value, `{spacing.rail}`, and that value is the same at the top, the sides and the fixed position rule. Nothing full-bleed may exceed it except a horizontal rule.
**The Nothing Floats Rule.** Every element sits on the field grid inside a numbered field. There is no sticky bar, no floating card, no overlay panel; the only two fixed layers are the position rule and the decoration of the blaze.
**The Hairline-By-Gap Rule.** A 1px division inside a field is drawn with a 1px grid gap over a `{colors.rule-quiet}` ground, or with a 1px border. It is never a smaller-than-1px line, an opacity trick, or a lighter grey pretending to be a hairline.

## Elevation & Depth

The system is flat printed ink and it says so: there is no elevation vocabulary, no layering of surfaces, no blur and no `backdrop-filter` anywhere in `src/landing.css`. Exactly one non-inset shadow exists, on the amendment sticker in field 01, and it exists because the sticker is pasted over the printed sheet. Three inset rings are also drawn with `box-shadow`, and all three are printed strokes rather than depth: the 1px ink-wash ring around the label-sheet id chip (`L-01`), the 2px ink ring around each brand swatch mark in label L-05, and the 2px ink ring around the issuer strip's brand flag stripe. Each of them exists so a mark or a stripe holds an edge on a field of its own colour. Depth is otherwise conveyed by ink weight and field order: a black plate inside a yellow field, a 3px rule against 1px hairlines, reversed type on an ink block.

The one thing that reads as material depth is the retroreflective response, and it is decoration. `.pc-sheen` is a fixed, full-viewport layer at z-index 0 under the sheet: a 28rem radial blaze (`{colors.blaze}` at 0.62 by day, `{colors.headlamp-blaze}` at 0.18 at night) plus a 45° prism texture, oil-slick style. Its centre follows the pointer through two custom properties, `--pc-lamp-x` and `--pc-lamp-y`, written from a `pointermove` listener throttled by `requestAnimationFrame`; the layer is `pointer-events: none` and `aria-hidden`, and the whole effect is skipped under `prefers-reduced-motion`. Both alphas are capped by the contrast measurement in Colors, not by taste, because the cone sits behind body text. The browser's own surfaces are brought into the world too: `::selection` reverses to ink-on-field, and the scrollbar is 12px with an ink thumb carrying a 3px field-coloured border (a bone thumb on the night sheet).

### Shadow Vocabulary
- **Amendment contact shadow** (`box-shadow: 0 3px 7px rgba(0, 0, 0, 0.28), 0 1px 2px rgba(0, 0, 0, 0.24)`): the only elevation shadow in the system. Used once, on `.pc-amend`, because the amendment is physically pasted on top of the sign. Nothing else may borrow it.
- **Brand ring** (`box-shadow: inset 0 0 0 2px <ink>`): an inset ring on the label sheet's brand swatch marks and the issuer strip's brand flag stripe, so a supplied accent keeps an edge against the field.
- **Chip keyline** (`box-shadow: inset 0 0 0 1px <rule-quiet>`): an inset hairline ring around the label sheet's id chips.

### Named Rules
**The One Shadow Rule.** One elevation shadow exists and it belongs to the amendment sticker. Inset rings are printed strokes, not elevation: they are allowed only where a mark would otherwise dissolve into a field of its own colour. A new shadow is a new world; a card with a shadow on this sheet is a defect.
**The Blaze Is Decoration Rule.** The pointer-driven blaze never carries contrast, meaning or state. It cannot be the reason a value is readable, it is never shown under reduced motion, and it never takes pointer events.

## Shapes

**Square and printed.** Every corner on the sheet is 90°: the three `border-radius` declarations in the stylesheet are all literal `0` (the instrument plate, the WebKit range thumb and the Firefox range thumb), and nothing is rounded, pill-shaped or clipped by an arc. There is no `overflow: hidden` mask, no `backdrop-filter`, and the only clip in the system is `overflow-x: hidden` on the sheet root and `overflow-x: auto` on the forecast formula strip so a long line can be scrolled without wrapping.

**Exactly two rule weights.** `{spacing.rule-heavy}` (3px) is structure: field tops, the issuer and footer rules, the action line, the close rule, source and device node outlines, the button border, the notice metric's left rule, the conductor rails, the rail bars on the link rows, the scroll-progress fill, and the focus ring. `{spacing.rule-hair}` (1px, usually as `{colors.rule-quiet}`) is everything interior: field-head underlines, set dividers, string-row rules, the plate's internal grid, the label tile artifacts, the range track's top and bottom edges. There is no 2px rule in the layout; the numbers that look like one are the notice action's 2px underline, the 2px vertical band mark at the selected hour, the 2px amber index mark on the printed range scale, and the 2px inset rings on the brand marks. No signal band is outlined: all five are filled bands with reversed type.

The sheet's other geometry: right-angle elbows (a 3px left border meeting a 3px bottom border) for the branch conductors; a 12px × 34px rectangular slider thumb, taller than its track, like a sliding index mark; 6px-wide triangular chevrons and hand-drawn pictograms in a single 4px stroke weight; two hatch directions as the sheet's shading language (surplus at 45° in a 3.4px stroke, deficit at -45° in a 2.2px stroke), each always accompanied by a legend word. The amendment sticker is the one rotated form in the system: `rotate(-0.55deg)` about `12% 40%`, because a sticker applied by hand is never straight.

### Named Rules
**The Square Sheet Rule.** Corners are 90°. No radius, no pill, no circular chip, no clipped arc. Even the pictograms are built from straight strokes — a house outline, a tilted rail of two rectangles, two supply stubs and a bolt — so nothing on the sheet reads as a rounded icon.
**The Two Weights Rule.** Two rule weights, 3px and 1px, and no third. A new divider either carries structure or it does not exist; if it needs to be seen, it is 3px, and if it needs to be felt, it is 1px.

## Components

Full drop-in HTML and CSS for each of these lives in `.impeccable/design.json` (Step 4b); this section is the doctrine and the exact values they must keep.

### Placard Sheet Ground
- **Shape:** a full-width rectangle with square corners, no radius and no shadow, inset by `{spacing.rail}`.
- **Background:** `{colors.field}`, drenched, edge to edge, with `{colors.ink}` as the text colour and `{colors.field-2}` reserved for browser chrome on the sheet.
- **Typography:** the annotation face is the sheet default at 16px/1.55.
- **Behaviour:** `::selection` reverses (ink block, field-coloured text). The sheet is a document, not a viewport-sized app frame: it scrolls and its position rule reports where you are.

### Issuer Strip
- **Shape:** the sheet's first rule is its own: a 3px bottom border under a 16px/14px padded row, no box.
- **Content:** left cluster is the logoblock plus the wordmark in `Barlow Condensed` at 26px, lowercase, with the degree glyph as part of the mark; then, pushed right, a mono metadata row (`Sheet 01 · six fields · rev 0.1.0`, `Marked system: PV + utility`, `Reference: HX-9.6 HYBRID`, `Brand · <name>` with a 26px × 12px accent stripe ringed in ink); then the sheet-light control pair.
- **Rules:** all metadata is 12px mono, 0.1em tracking, uppercase; only the values are ink, the keys are `{colors.ink-2}`. Below 719px the metadata wraps to a full-width row and distributes with `justify-content: space-between`.

### Signal Band
- **Shape:** a 3px-bordered rectangle, square, holding a filled signal word at the left and a mono metadata line at the right, divided by a 1px hairline.
- **Colour:** the word is reversed out of its register colour; on field 01 that register is `{colors.notice}`, because the band states information ("Photovoltaic system present · Dual supply · Marked at the utility wall") and not a hazard.
- **Type:** the word is `Barlow Condensed` 700 at `clamp(1.35rem, 2.6vw, 2.05rem)` uppercase; the metadata line is 12px mono with `·` separators.
- **Behaviour:** static. It is the top of the sheet, not a banner, and it never animates.

### Field Header and Id Chip
- **Shape:** a 3px top rule (omitted on field 01), then a baseline-aligned flex row: the id chip, the title, an optional right-aligned aside.
- **Id chip:** `02 · The day` — 12px bold mono, 0.14em tracking, uppercase, reversed out of `{colors.ink}` with `{colors.field}` text, `4px 9px 3px` padding, no radius. It is the field's number plate and it is not decorative: every field on the sheet is numbered.
- **Title:** display face at headline size, uppercase, one per field.
- **Aside:** 12px mono tag in `{colors.ink-3}` carrying what the field was read at and where the numbers came from (`Simulated · peak 7.89 kW at 13:00 · 69% used on site`, `Simulated · read at 13:00 · 3 raised`). This is where the provenance of a reading is printed, and every simulated field says so here.

### Buttons
- **Shape:** rectangular, square corners, 3px `{colors.ink}` border, `13px 22px 12px` padding, `Barlow Condensed` 700 at 1.12rem uppercase with 0.03em tracking.
- **Primary (filled):** ink background, `{colors.field}` text.
- **Ghost:** transparent background, ink text and ink border.
- **Hover / Focus:** both variants invert — the filled button becomes field-coloured with ink text, the ghost becomes ink with field-coloured text. The transition is 160ms on background-color and color, using the sheet's single easing curve `--pc-ease: cubic-bezier(0.16, 1, 0.3, 1)`.
- **Focus:** one rule for the whole sheet, `.pc :focus-visible { outline: 3px solid ink; outline-offset: 3px }` — a square ring, the same weight as a field rule, never a glow or a border-colour change.
- **Disabled:** transparent background, `{colors.ink-3}` text and border, no hover change. It is used exactly once, on `Following now` in the reading selector, and its contrast is why `{colors.ink-3}` is the recorded floor.

### Action Line
- **Shape:** a 3px top rule across the sheet, `{spacing.action-line}` of margin above it, and one flex row holding the primary action, the secondary action and the guarantee.
- **Rule:** the primary action is bolted to the sheet's rule, never floating in the viewport. The guarantee ("No account · no server · readings stay on the device") is 12px mono pushed right by `margin-left: auto`; it is a printed footnote and it holds its position in both fields that use the line.
- **Behaviour:** if the browser exposes `beforeinstallprompt`, the second control becomes a real `Add to home screen` button; if it does not, the same slot prints the instruction as prose. The layout does not change between the two.

### Instrument Plate (signature)
- **Shape:** a black rectangle printed into the sheet, square corners, `clamp(14px, 1.8vw, 22px)` by `clamp(15px, 1.9vw, 24px)` padding, `{colors.plate}` ground and `{colors.plate-ink}` ink.
- **Anatomy:** a mono head row (`NOW · 13:00` left, `SIMULATED · 30 s` or `SIMULATED · HELD` right, divided by a 1px `{colors.plate-rule}` rule); the inverter state word in the display face; the AC readout at measurement size with a mono unit; the printed range marking; then a 2×2 grid of cells (DC array, house, battery, utility) separated by 1px plate hairlines over a plate-rule ground.
- **Printed range marking:** a 30px track with 12 tick rules, a 5px filled extent for the current output and a 2px sliding index mark, over a mono legend reading `0 · array rating 9.6 kW · 9.6`.
- **Behaviour:** every moving number on the plate is sprung, not tweened. `src/hooks/useBallistics.ts` integrates a spring (stiffness 210, damping 26) at 60fps, so a reading leans toward its new value and overshoots once, the way a needle does; under `prefers-reduced-motion` the value is set instantly. The plate is the only place where the sheet admits that it is alive.

### Reading Selector (signature)
- **Shape:** a real `<input type="range">` restyled into the sheet: a 26px-high transparent input, a track with 24 printed hour ticks (`repeating-linear-gradient` at 1px per 4.1667%) and 1px rules top and bottom, and a 12px × 34px rectangular thumb in `{colors.ink}` that stands taller than its track.
- **Anatomy:** the mono label `Reading selector`, the range, the current `HH:MM` in a tabular mono box, and a ghost button reading `Following now` (disabled while following) or `Return to now`.
- **Behaviour:** this is the sheet's one control and it restructures the whole document — field 02's band, field 03's conductors, field 04's notice set and the plate all re-read from it. Dragging or arrowing sets `following = false`; `Return to now` snaps back to the local clock, which otherwise re-reads every 30 seconds while the tab is visible. The control is labelled, keyboard operable and exposes `aria-valuetext` as a clock time, not as a frame index.

### Day Band (signature)
- **Shape:** a full-width plot bounded by 1px hairlines top and bottom, height `clamp(150px, 20vw, 250px)`.
- **Drawing:** production as a filled area at 92% ink, house load as a 2.4px line, surplus as a 45° hatch, deficit as a -45° hatch; 9 printed hour rules at 18% opacity and three dashed marks at 1.5px for sunrise 06:12, solar noon 13:00 and sunset 19:48, each labelled in 12px mono.
- **Overlays:** a 2px vertical mark at the selected hour and a readout chip reversed out of ink (`13:00 · 7.89 kW`) that stays inside the band by clamping its own position between 12% and 88%.
- **Legend and totals:** four legend swatches (solid, line, surplus hatch, deficit hatch) labelled `Solar produced`, `House load`, `Surplus, to battery and grid`, `From the grid` — the swatch names the series, the totals carry the numbers — then four totals tiles (`Generated`, `Used in the house`, `Bought from grid`, `Exported to grid`, each in kWh) separated by 1px gap hairlines.
- **Behaviour:** the SVG's viewBox is the band's measured CSS pixel box (`src/hooks/useElementSize.ts`, fallback 1000 × 240), so hatch strokes and mark weights do not scale with the viewport. Minor hour labels hide below 720px.

### Conductor Nodes and Link Rails
- **Shape:** an ordered list printed like a one-line diagram. Each node is a square rectangle with `12px 16px 13px` padding; each link between nodes is a 74px rail column holding a 3px ink bar, a chevron pointing in the direction of flow, then another bar.
- **Variants:** source (`3px` ink outline on the field), device (ink plate with `{colors.plate-ink}` id), plain (1px hairline outline); branch links replace the bar-chevron-bar rail with a right-angle elbow.
- **Colour:** every path carries a room mark chip (Solar, House, Battery, Utility) filled with `{colors.solar}`, `{colors.flow}`, `{colors.grid}` or `{colors.battery}` and reversed in `{colors.mark-ink}`.
- **Rule:** colour never states the direction or the state on its own. Each row prints the value, the unit and a word (`CHARGING`, `DISCHARGING`, `IDLE`, `IMPORTING`, `EXPORTING`, `NO FLOW`), and the chevron points the way the arrows of a printed diagram do. Rows of per-string readings below the diagram use a 12px mono id block, fixed mono columns and a 12px share bar.

### Notice Rows and the Danger Field
- **Danger field:** a full-width block in `{colors.danger}` with reversed white type, `128px 1fr 0.62fr` at 820px and above, holding the signal word, the always-true DC hazard statement, a mono provenance line (`06:00 · marked supply: photovoltaic · second supply: utility`) and a 112px authored hazard pictogram. It appears once per sheet.
- **Notice rows:** `128px minmax(0,1fr) minmax(0,210px)` at 760px, separated by 1px hairlines. The signal word is a plain filled band reversed out of its own register (no outline, no keyline; the daylight caution band simply inverts its pair), the title is the display face, the body is 0.97rem annotation text, and the metric column is separated by a 3px ink left rule: a tabular mono value with its unit, a mono delta line (`SPREAD ACROSS STRINGS`) and an underlined action label.
- **Behaviour:** the notice set is computed, not decorated — thresholds are the ones the app ships (string imbalance above 15%, heatsink above 48 °C, battery above 92% while exporting). The list shows the first three raised notices and the row count is printed in the field's aside. A notice without a metric and an action is not a notice.

### Label Sheet Fields
- **Shape:** the marking's sibling labels as a 1px-gap grid of tiles, each tile a field-coloured square with `clamp(16px, 2vw, 24px)` padding.
- **Anatomy per tile:** an id chip (`L-01`), a mono role line that names the mechanism rather than the mood (`SunSpec Modbus TCP`, `Open-Meteo · no key`, `No account · no database`, `base64url · client-side`, `?brand= · four brands ship`, `PWA · service worker`), a display-face title, 0.97rem body text, then an artifact block pinned to the bottom by `margin-top: auto` above a 1px rule: mono field-shape rows, an ink formula strip (`EXPECTED kWh = PEAK SUN HOURS × 9.6 kW × 0.82`), a share-link fragment, brand swatches (a 26px square mark carrying the brand's accent, white type by day and mark ink at night, ringed by a 2px inset ink rule), or the absence list, where a 22px × 3px ink bar precedes each thing the product does not do.
- **Rule:** a tile's artifact is evidence, not decoration. Every claim in the label set is checkable in the repository, and where a mechanism is narrower than the table-layout pitches usually are (a gateway is needed for a live inverter), the tile says so in the body text.

### Amendment Sticker
- **Shape:** the one rotated form on the sheet, `rotate(-0.55deg)` about `12% 40%`, ink ground, field-coloured text, `14px 18px 15px` padding, maximum width 44ch, and the system's only elevation shadow.
- **Anatomy:** a 12px mono key (`Amendment, pasted over the sign`) at 86% opacity, then 0.94rem annotation text.
- **Rule:** the sticker is where the sheet states what it cannot prove — the readings are simulated, no inverter is connected, there are no customers — and it is set as an amendment because that is what a real placard does when it changes. It is a content commitment, not a motif: do not add a second sticker.

### Sheet Position Rule
- **Shape:** a fixed 26px strip at the top of the viewport, `pointer-events: none`, `aria-hidden`, inset by the same `{spacing.rail}` as the sheet.
- **Anatomy:** a 3px `{colors.rule-quiet}` track across the full width, a 3px ink fill at the scroll fraction, and the current field's name reversed out of ink at the right, at 0% opacity until 1.2% scroll.
- **Behaviour:** the name is driven by an `IntersectionObserver` over `[data-pc-field]` with `rootMargin: '-30% 0px -55% 0px'`, so it reports the field the reader is actually in, and it fades in over 220ms on `--pc-ease`. It is a printed edge marker; it is not a navigation menu and it never becomes one.

### Logoblock
- **Shape:** a 34px square ink tile (`#0b0b0c`, the one value on the sheet taken from the app's darkest ground so the mark keeps its own black) with a 26px mark centred in it.
- **Use:** the issuer strip's wordmark cluster; the same block sits above the wordmark in the app. The wordmark itself is `Barlow Condensed` 700 at 26px, lowercase, with the degree glyph as part of the name.

### Mark Chips
- **Shape:** a small square chip, `3px 8px 2px` padding, 12px mono at 0.12em tracking, uppercase, no radius.
- **Colour:** each of the four rooms has one: `{colors.solar}`, `{colors.flow}`, `{colors.grid}`, `{colors.battery}`, all reversed in `{colors.mark-ink}`. The neutral chip variant uses ink with field-coloured text (the field id, the step number, the string id).
- **Rule:** a chip names a room or identifies a field. It never encodes a value or a severity; severity belongs to the five registers and their words. The brand swatch mark is the same idea with a supplied colour: the accent arrives inline (`?brand=`, `accentLight` by day and `accent` at night), the monogram letter stays, and no page meaning depends on the colour.

## Do's and Don'ts

### Do:
- **Do** keep every reading on the sheet grid: it belongs to a numbered field, with a 3px rule above the field and a stated source and time stamp beside it.
- **Do** set every measurement in Plate Mono with tabular numerals, including units, hour stamps and reference lines.
- **Do** state direction and severity as a word first, then let the register colour or the room chip repeat it: `BATTERY · 3.40 kW · CHARGING`, `WARNING · String output imbalance`.
- **Do** declare every new colour twice, in `.pc` and in `[data-theme='dark'] .pc`, and check the pair with the blaze composited before shipping it (the worst case recorded is 5.27:1).
- **Do** hold the two rule weights: 3px for structure, 1px for interiors, and draw interior hairlines with a 1px grid gap over a `{colors.rule-quiet}` ground.
- **Do** keep the brand accent on its two identity surfaces only (the issuer flag stripe, the label sheet's brand swatches), passed inline and switched by state (`accentLight` in Daylight, `accent` at night); give each a 2px inset ink ring so a light accent still holds an edge.
- **Do** keep the focus ring on every focusable element: 3px ink outline at 3px offset via `:focus-visible`, and give controls real names (`Reading selector` is the range's label, and its value is exposed as a clock time).
- **Do** label simulated figures as simulated. The plate head prints `SIMULATED · 30 s` or `SIMULATED · HELD`, and every field that shows a reading says so in its aside (`Simulated · peak 7.89 kW at 13:00 · 69% used on site`); a figure that cannot be sourced and time-stamped does not ship.
- **Do** keep both states fully populated, and keep the sheet legible as a static page under `prefers-reduced-motion`: the springs, the blaze and the smooth scroll all switch off, the content does not.

### Don't:
- **Don't** introduce a third rule weight, a border radius, a pill, a circular chip or a second shadow.
- **Don't** use a register colour as an accent sprinkled on a neutral ground, a gradient stop, or a decorative dot. A saturated colour fills a band or a field, or it is not used.
- **Don't** set a number in Barlow Condensed or Barlow Signage, and don't shrink any text below 12px.
- **Don't** encode meaning in colour alone: every path, state and severity carries its own word, and no page meaning may depend on the `?brand=` accent.
- **Don't** outline a signal band. All five registers are filled bands with reversed type; if a band cannot be seen against the field, invert its own pair (as Daylight's caution does) rather than drawing a box around it.
- **Don't** let the blaze layer carry contrast or meaning, don't give it pointer events, and don't render it under reduced motion.
- **Don't** put a floating card, drawer, sticky header or overlay on the sheet, and don't nest a card inside a field.
- **Don't** build `.pc` styles out of the app's tokens (`src/index.css` `:root`, the Tailwind `carbon` / `bone` / `signal` palette, the Inter and Instrument Serif families), and don't push `.pc-*` tokens into `/app` or `/share/:payload`. The two systems stay separate.
- **Don't** fabricate proof. There are no customers, testimonials, press, certifications or prices in this repository; the honest list in field 06 prints the absences instead of padding them, and the mock tariff constants in `aiInsights.ts` stay demo material.
