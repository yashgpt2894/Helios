---
version: 1
slug: "src-pages-landing-tsx"
primary_target: "src/pages/Landing.tsx"
related_targets: ["src/index.css"]
---

# Surface brief: `/` landing (src/pages/Landing.tsx)

## Scope and mode

Persuade. Replacement of the incumbent landing page (the preloader / 3D-inverter / testimonials page is anti-reference). The app's own world (carbon/bone, signal amber, JetBrains Mono for measured values, Plus Jakarta Sans display, the aperture mark) is inherited unchanged; this brief decides composition only.

## Audience, job, action, proof, constraints

- Audience: a homeowner with a solar array and a hybrid inverter, on a phone or laptop, deciding whether to try a monitoring app; secondarily an installer judging whether to white-label it.
- Job: understand in seconds what helios° is, why it is different from the inverter maker's own app, and open it.
- Primary action: **Open the app** (`/app`, carrying `?brand=`). Secondary: install to home screen (PWA), read how the connection works.
- Proof: the product's real components running on the mock service (EnergyFlow, insight logic, forecast strip, battery ring), labeled as a simulated 9.6 kW system. No testimonials, customers, counts, benchmarks or prices exist; none are invented.
- Constraints: no new dependencies, no trackers, no emoji, reduced-motion respected, `?brand=` recolors, both themes, `npm run typecheck` + `npm run build` clean.

## Direction contract

THESIS: Your inverter speaks in registers; helios° answers in decisions. The first viewport is a translation ledger, raw SunSpec points on the left, the plain-language reading helios° makes of them on the right, updating together on one visible two-second clock. It refuses the category default of headline + phone mockup + three icon cards.

OWN-WORLD (inherited): carbon ground stepped to `--bg-3`; bone text with one brightest element per view (the live reading). Signal amber owns one whole region at page scale (the peak-sun passage, `brand.accent` field with carbon ink); flow green, grid blue, battery tan only where they mean something. JetBrains Mono, tabular, for every measured value and register point; Plus Jakarta Sans 700–800, tracking −0.03em, for display; Inter for body. Hairline rules on a visible 24px dot grid that the ledger rows snap to. Corners 18–20px on surfaces, 999px on pills. No gradient text, no glass-as-decoration, no eyebrows.

STORY: the visitor sees numbers become advice, believes the advice is derived from their own inverter without a cloud, and opens the app.

FIRST VIEWPORT: top bar: mark + wordmark left, theme toggle and **Open the app** pill right. Below, a headline across the full measure at clamp(2.6rem, 6vw, 5.5rem): "Your inverter speaks in registers." / second line in bone-400: "helios° answers in decisions." Under it, the ledger fills the viewport width: a two-column instrument, left column 5/12 mono register rows (`W 6 412`, `Hz 59.98`, `DCV 381.6`, `TmpSnk 41.2 °C`, `St PRODUCING`, `SoC 76 %`), right column 7/12 the insight helios° derives, set large, with a dashed connector from the row that produced it. A poll clock ticks in the ledger header: "SunSpec Modbus TCP · simulated 9.6 kW system · updated 2 s ago". The primary pill sits under the headline; the secondary text link "How it connects" beside it.

FORM: register→advice ledger, index 3 of my ordered structural list (dealt indices 7, 5, 3; index 3 chosen on product clarity and audience identification). Seed key 943e617a.

RAISES (named for the challenger that donated them):
- Crouwel grid specimen (competitive): the dot grid is the page's literal armature; ledger rows and columns snap to its 24px cells.
- Luminescent understory (declined): one luminance hierarchy, one brightest element per view.
- Hand-bent neon circuit (declined): the solar day is one continuous stroke, a single SVG path running the length of the proof passage, lit by scroll.
- Mecha crisis wall (declined): monumental tabular numerals with a signed delta under each reading.
- Algorave source floor (declined): one shared visible clock; register and advice columns update on the same boundary.
- Darkroom safelight bay (declined): total commitment to one wash, the brand accent as a page-scale field for the peak-sun passage.

SIGNATURE INTERACTION: the ledger re-reads every two seconds; the changed digits tick, the connector re-draws to the row that produced the current insight, and the clock resets. Pauses when off-screen or when the tab is hidden; static under reduced motion.

MOTION GRAMMAR: exponential ease-out from visible defaults; only the ledger tick and the day-stroke draw animate; no scroll-jacking; nothing hidden behind an entrance.

FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, DESIGN.md, and every shipping raster carrying its provenance.

## Visitor path after the first viewport

1. Ledger (thesis) → 2. "One day, read by helios°" (the continuous day stroke, 05:00–21:00, with the real insight the product produces at sunrise, peak, afternoon cloud, evening battery, night backup; the peak-sun passage is the amber field) → 3. "The instrument itself" (the real EnergyFlow + live tiles on the mock service, phone frame at true size) → 4. "How it connects" (SunSpec Modbus TCP, local network, forecast is the only network call, share links decode on the client) → 5. Install / open close with the wordmark, legal line, theme toggle.

## Memorable moment

Numbers on the left becoming a sentence on the right, on a clock you can see.

## Unresolved decisions

- Store badges for the deferred native apps: not shown; one line "iOS and Android apps are in development" is the honest maximum until the owner decides.
- Public URL / OG image: not recorded in the repo; `index.html` meta unchanged except title/description if the owner wants.
