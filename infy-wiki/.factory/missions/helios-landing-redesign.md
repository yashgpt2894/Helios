---
mission_id: helios-landing-redesign
objective: Rebuild the landing page in a Hydroflow-inspired editorial style with placeholder download CTAs
created: 2026-05-18
mode: checkpoint
deploy_policy: on-mission-end
target_branch: test-branch
status: active
---

# Mission: Helios Landing Redesign

owner: yashgupta
predecessor: `.factory/missions/helios-pwa-launch.md` (active)
successor: —

## 0. Context

The existing landing page at `src/pages/Landing.tsx` is a compact single-column dark layout. This mission replaces it with a bold, editorial Hydroflow-inspired redesign: oversized layered typography, cream/dark contrast sections, sticky scroll rhythm, a CSS-only iPhone mockup, and custom hand-rolled download badges.

## 1. Goal

Ship a redesigned landing page to `test-branch` that:
- Uses bold typographic hero with text-behind/text-front layering
- Drops testimonials and FAQ
- Adds App Store and Google Play placeholder badges marked "Coming soon"
- Preserves white-label brand recoloring via `?brand=` query param
- Passes `npm run typecheck` and `npm run build` clean

## 2. Decomposition

| # | Title | Depends on | Type | Status | Acceptance |
|---|-------|------------|------|--------|------------|
| L0 | Mission file | — | ship-droid | done | This file created |
| L1 | Assets | — | builder-droid | pending | PNGs moved to `public/` |
| L2 | CSS utilities | — | builder-droid | pending | `.text-behind`, `.text-front`, `.dotted-rule`, `.editorial-pad` added |
| L3 | Hero | L2 | builder-droid | pending | Layered headline, iPhone mockup, CTAs |
| L4 | Manifesto / About | L2 | builder-droid | pending | Cream section, label, headline, body |
| L5 | Feature grid | L2 | builder-droid | pending | Editorial 2-col grid with numerals 01-06 |
| L6 | Live-preview splash | L1, L2 | builder-droid | pending | "Watch it, Optimize it." + rotated phone |
| L7 | Download CTA | L2 | builder-droid | pending | Two custom badges + "Open the web app" link |
| L8 | Footer wordmark | L2 | builder-droid | pending | Full-bleed viewport-width wordmark + legal line |
| L9 | Brand verification | L3-L8 | ship-droid | pending | No hard-coded accent hex; all 4 brands recolor |
| L10 | Build verification | L3-L8 | ship-droid | pending | `typecheck` + `build` pass clean |
| L11 | Deploy | L9, L10 | deployer-droid | pending | Force-pushed to `test-branch`, `main` untouched |

## 3. Progress

### L0 — Mission file — done
- started: 2026-05-18  completed: 2026-05-18

### L1 — Assets — pending

### L2 — CSS utilities — pending

### L3 — Hero — pending

### L4 — Manifesto / About — pending

### L5 — Feature grid — pending

### L6 — Live-preview splash — pending

### L7 — Download CTA — pending

### L8 — Footer wordmark — pending

### L9 — Brand verification — pending

### L10 — Build verification — pending

### L11 — Deploy — pending

## 4. Constraints

- No emoji anywhere.
- No new npm dependencies.
- No third-party trackers / analytics.
- No comments in code unless explaining a non-obvious constraint.
- White-labeling (`?brand=`) must continue to work — accents read from `brand.accent` / CSS vars.
- `prefers-reduced-motion` respected.
- Do NOT push to `main`. Only `test-branch`.
- Do NOT touch `ios/`, `android/`, `release/`, `dist/`, `dist-tsc/`.
- Do NOT modify `helios-pwa-launch.md` or `helios-store-launch.md`.

## 5. Definition of Done

- [ ] `.factory/missions/helios-landing-redesign.md` created.
- [ ] `src/pages/Landing.tsx` replaced.
- [ ] CSS utilities added to `src/index.css`.
- [ ] PNGs moved to `public/`.
- [ ] `npm run typecheck` passes.
- [ ] `npm run build` passes.
- [ ] All 4 brands recolor correctly.
- [ ] App Store + Google Play badges marked "Coming soon", `aria-disabled`, not focusable.
- [ ] Branch `test-branch` rebased on `main` and force-pushed.
- [ ] `main` branch untouched.

## 6. Risks & rollback

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Lucide icon missing (Apple/Play) | low | Verified present in `lucide-react@0.460.0`. |
| Typecheck failure from new TSX | medium | Strictly type event handlers; no `any`. |
| Bundle size increase from images | low | Images are in `public/` and served as static assets. |
| Light-theme override conflicts | low | Test both themes locally; use CSS vars. |
| Accidental push to `main` | low | Use `--force-with-lease origin test-branch` explicitly. |

## 7. Architecture Decisions

- **Hand-rolled badges over official assets:** Avoids Apple/Google trademark file restrictions; uses lucide icons inside styled divs.
- **CSS-only iPhone frame:** No external image assets for the device shell; pure CSS border-radius + notch.
- **Editorial padding via `.editorial-pad`:** Consistent large vertical rhythm across sections.
- **Static PNGs in `public/`:** Vite serves them at root path; referenced as `/mobile-dashboard-*.png`.
- **No FAQ / no testimonials:** Simplifies page, focuses on product narrative.

## 8. Handover state

- Repo: `/Users/yashgupta/Desktop/yash/code/Helios`, remote `https://github.com/yashgpt2894/Helios.git`, branch `main`
- Latest commit: `b7d6898`
- Target branch: `test-branch` (existing remote)
- Assets: `mobile-dashboard-viewport.png` + `mobile-dashboard-full.png` at repo root (to be moved to `public/`)
