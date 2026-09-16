---
version: 1
slug: "src-pages-landing-tsx"
primary_target: "src/pages/Landing.tsx"
related_targets: ["src/index.css","src/landing.css"]
---

# Surface brief — `/` landing

## Scope and visitor mode

Route `/` (`src/pages/Landing.tsx`) only. `/app` and `/share/:payload` are out of scope and keep their
existing look. Mode: **Persuade** — the visitor decides and acts (installs the PWA, or decides the
product is credible enough to put their own installer name on).

## Audience, job, action, proof

- **Owner:** a homeowner with a rooftop array, phone in hand, usually standing at the meter box or in
  the kitchen. Job: *is it working, what is it doing now, what should I run?*
- **Installer:** wants a customer-facing surface that can carry their own name and colour.
- **Primary action:** install / open the app. **Secondary:** see the mechanism work.
- **Proof available:** the real SunSpec telemetry model (`src/services/sunspec.ts`: 9.6 kW hybrid, three
  3.2 kW strings of 8 panels, 13.5 kWh battery, 0.964 conversion), the real curve (`src/lib/solarCurve.ts`:
  sunrise 06:12, noon 13:00, sunset 19:48), the real insight vocabulary and thresholds
  (`src/services/aiInsights.ts`), the client-side snapshot encoder (`src/services/share.ts`), the brand
  registry (`src/services/brand.ts`), the real mark and icons in `design/` and `public/`.
- **Not available, never fabricated:** customers, testimonials, press, certifications, prices, installed
  capacity, benchmarks. Mock tariff constants in `aiInsights.ts` are demo material only and are labelled
  as such on the page.

## Constraints

- Vite 5 + React 18 + TS strict + Tailwind 3; the page is a route inside the existing app.
- Offline-first PWA: faces are self-hosted, no runtime CDN dependency for the page's own type.
- Existing theme contract: `document.documentElement[data-theme]`, `localStorage['helios-theme']`,
  `auto | light | dark`. The landing must join it instead of inventing a second theme switch.
- White-label: `?brand=helios|voltcraft|sunworks|meridian` can restyle by accent; no meaning on the page
  may depend on that accent.
- Accessibility: contrast ≥4.5:1 for text, visible focus, real text (never text baked into a raster),
  `prefers-reduced-motion` honoured, the hour control keyboard-operable.
- No backend; the page is static.

## Chosen direction

Retroreflective dual-supply placard and label set of a residential PV installation, raised by the hand it
beat (see keeps below). Assigned by the direction roll, seed key `49ff3a4e`.

Challenger verdicts from the dealt hand, decided before any borrowing:

- **VU-meter bridge** — *competitive* (product clarity). Fused, a row of ballistic needles would show
  live flow well, but the recording-desk world is not the homeowner's world, so it loses audience
  identification. Kept: **true ballistics**.
- **Tensegrity column** — *competitive* (product clarity). Force paths are legible and nameable; the
  sculpture world itself is not the audience's. Kept: **named load paths**.
- **WebGL shader portal** — *declined* (loses both). Kept: **one field owns the whole first viewport**.
- **Drawcord transforming cape** — *declined* (loses both). Kept: **one control restructures the form**.
- **Tropicalia sleeve collage** — *declined* (loses both). Kept: **total commitment to one saturated field**.
- **Numbered insert card** — *declined* (loses both). Kept: **absence drawn as deliberately as presence**.

Raises written into the direction: full-bleed single field; one hour control that restructures every
field on the sheet; instrument-grade ballistics on every moving number; leader-line annotations naming
each energy path with its unit; empty and night states drawn as real fields, never hidden.

No pick card: the assigned direction topped the grounded list in its family, so the roll and the ranking
agree here.

## Memorable moment

Dragging the hour control (or pressing the arrow keys) re-issues the whole placard: the signal field
changes word, the four flow fields re-read, and the sheet's day band redraws. At night the sheeting goes
dark and the ink blazes, as retroreflective material actually does under a headlamp.

## Unresolved

- Nothing in the brief pins a photographic asset, and none exists on hand that is not a near-black
  dashboard capture; the page therefore demonstrates the mechanism from the real site model rather than
  from imagery. If the owner has install photos, the label sheet is where they belong.
- `.impeccable/` build metadata is development-only and is not part of the shipped page.


## Approved deviations

- **A fixed sheet-position rule stays, against the FIRST VIEWPORT line "nothing floats".** `.pc-position`
  is pinned to the top edge: a 3px progress rule plus a mono chip naming the current field. It is
  `aria-hidden`, holds no control, and exists because the sheet is six fields and 5,768px tall at 1440px.
  Without it the six field ID plates are the only wayfinding. Recorded here rather than left as a silent
  contradiction, and the issuer strip now also carries the sheet number and revision the contract promised.
- **The sheeting's blaze alpha is capped by measurement, not by taste.** The retroreflective highlight is a
  fixed decorative layer, so it sits behind body text. The shipped cap is **0.18 alpha in the headlamp
  state** (a further 0.9 element opacity, so the effective peak is about 0.16) and **0.62 in daylight**;
  the failing value was measured, not guessed: at 0.24 the quietest text token drops to 4.46:1 and fails,
  while 0.18 holds it at 5.27:1. The cone radius was tightened to 28rem so the lamp reads without
  brightening the page under text. Measured sheet height: 5,917px at 1440px across the six fields.


- **The comp-phase state file was removed.** `.impeccable/build/state.json` existed only because the
  direction round's choice ping creates it; it opened a comp round this machine cannot run (no image
  generation) and left every later phase pending. A code-led build has no comp, spec, plates or hero
  gate, so the file was deleted instead of force-closed, and this line is the record of why.

## Direction contract

**THESIS.** A residential PV installation is *marked*, not described. helios° inherits the placard's
actual job — make an invisible live state legible at a glance, in bad light, from a distance — and
refuses the category's dashboard default: the page is one retroreflective label sheet, and every reading
on it is a field on that sheet.

**OWN-WORLD.** Retroreflective sheeting in signal yellow as the ground, black silkscreen ink, exactly two
rule weights, square sheet corners, ANSI Z535 signal fields as the semantic set (notice blue, caution
yellow, danger red, safety green), placard caps in a condensed industrial grotesk, annotation text in a
neutral grotesk, all measurement in tabular monospace. Night state is the same sheet under a headlamp:
near-black ground, retroreflective ink blazing. Content is never removed in the night state.

**STORY.** The visitor understands that this house has two supplies and that the placard above the meter
only warns about it; believes that helios° is the instrument that reads the solar one because they watch
the day's four flows and the notice set change as they scrub the hour; and installs the PWA.

**FIRST VIEWPORT.** One full-bleed sheet, edge to edge, no floating navigation: issuer strip along the top
rule (wordmark, sheet number, revision), a signal field carrying the honest register for information
followed by the dual-supply statement in placard caps at display scale, an authored ANSI-style pictogram,
and a right-hand live field carrying the current hour, the state word, and AC power as tabular numerals.
The primary action is bolted to the sheet's bottom rule as the placard's mandated action line. Nothing
floats; every element sits on the sheet's field grid.

**FORM.** Assigned index 5 of seven grounded directions (the retrofit label set: placards, disconnect and
string labels, one-line diagram tags, reflective sheeting and safety colour). Seed key 49ff3a4e.
Code-led: no image generation exists on this machine, so the comp round is skipped by contract and the
ambition lives in FIRST VIEWPORT plus the named signature interaction above.

**FINISH.** unreviewed and undocumented is unfinished; this build ends with the finish review, the
verdict, DESIGN.md, and every shipping raster carrying its provenance
