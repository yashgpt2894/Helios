---
mission_id: helios-pwa-launch
objective: Launch helios° as a production-ready Progressive Web App, deferring native store submission until PWA is validated
created: 2026-05-16
mode: checkpoint
deploy_policy: on-mission-end
target_branch: main
status: active
---

# Mission: Helios PWA Launch

owner: yashgupta
predecessor: `.factory/missions/helios-native-apps.md` (complete)
successor: `.factory/missions/helios-store-launch.md` (deferred)

## 0. Context

The Helios native apps mission delivered 175 source/spec/release files:
- 52 Swift files (main iOS app + WidgetKit/LiveActivity/Watch/Intents/Control)
- 55 Kotlin files (main Android app + Glance widgets/Wear OS/QS tile/App Actions)
- 14 screen mockups, 5 design specs, 13 release-prep documents
- iOS Localizable.strings + Android strings.xml in 6 languages (1 base + 5 stubs)

All native code is feature-complete against the Helios PWA. Cross-platform share-link format (SnapshotPayload v1, base64url) is identical across web/iOS/Android.

**Strategy pivot (2026-05-16):** Ship the PWA first, validate with real users, then resume native store launch. Rationale:
- PWA reaches users immediately via browser (no store approval delays)
- Same codebase, same features — zero divergence risk
- User feedback from PWA informs native UX refinements before store submission
- Store launch remains a well-scoped deferred mission with all code ready

## 1. Goal

Ship helios° PWA to production on `main` at version `0.1.0`. It must be:
- Installable on iOS Safari (Add to Home Screen) and Android Chrome
- Offline-capable after first load (service worker precaches assets)
- Passing Lighthouse PWA audit (100% on PWA category)
- Live on a public URL with Vercel auto-deploy from `main`

## 2. Decomposition

| # | Title | Depends on | Type | Status | Acceptance |
|---|-------|------------|------|--------|------------|
| P1 | PWA infrastructure audit | — | builder-droid | done | vite-plugin-pwa configured, manifest valid, sw.js generates, icons present |
| P2 | Service worker registration | P1 | builder-droid | done | SW registers on load, no console errors |
| P3 | Build verification | P1 | builder-droid | done | `npm run build` succeeds, dist/ contains manifest + sw.js + precached assets |
| P4 | Store launch mission deferred | P1 | mission-droid | done | `helios-store-launch.md` updated to `status: deferred`, pushed to `main` |
| P5 | README updated | P4 | builder-droid | done | Correct repo URL, PWA-first callout, ios/android listed in structure |
| P6 | Vercel production deploy | P3, P5 | human or auto | pending | Live URL serving the PWA, HTTPS, custom domain or `*.vercel.app` |
| P7 | Lighthouse audit | P6 | human | pending | PWA score >= 95, Performance >= 80, Accessibility >= 90 |
| P8 | Real-device install test | P6 | human | pending | Install on iPhone + Android, verify offline, verify share-link round-trip |
| P9 | Analytics / monitoring setup | P6 | human | pending | (Optional) Sentry or similar for error tracking, no third-party trackers per constraints |

## 3. Progress

### P1 — PWA infrastructure audit — done
- started: 2026-05-16  completed: 2026-05-16
- notes: `vite-plugin-pwa` already configured in `vite.config.ts`. Manifest has name, short_name, theme_color, background_color, display: standalone, orientation: portrait, scope: /, start_url: /. Icons: 192x192 SVG (any), 512x512 SVG (any), 512x512 SVG (maskable). Workbox precaches 13 entries (~779 KiB). Runtime caching for Google Fonts (CacheFirst, 1 year).

### P2 — Service worker registration — done
- started: 2026-05-16  completed: 2026-05-16
- notes: Initially added manual `navigator.serviceWorker.register('/sw.js')` in `src/main.tsx`, then removed it when discovering `vite-plugin-pwa` auto-injects `registerSW.js` into the built `index.html`. Clean build confirmed — no duplicate registration.

### P3 — Build verification — done
- started: 2026-05-16  completed: 2026-05-16
- notes: `npm install` + `npx vite build` succeeds. Output: `dist/sw.js`, `dist/workbox-*.js`, `dist/manifest.webmanifest`, `dist/registerSW.js`, `dist/assets/index-*.js` (758 KiB), `dist/assets/index-*.css` (26 KiB). PWA v0.21.2 generateSW mode. Chunk size warning on JS (758 KiB > 500 KiB) — acceptable for initial launch, can code-split later.

### P4 — Store launch mission deferred — done
- started: 2026-05-16  completed: 2026-05-16
- commits: `cf962b1`
- notes: Updated `.factory/missions/helios-store-launch.md` with YAML frontmatter, `status: deferred`, added Status column to Decomposition table, added Progress section for L0. Pushed to `main`.

### P5 — README updated — done
- started: 2026-05-16  completed: 2026-05-16
- commits: `ea585d1`
- notes: Three changes: (1) repo URL corrected to `https://github.com/yashgpt2894/Helios.git`, (2) PWA-first callout added under "What it is", (3) project structure tree updated to show `ios/`, `android/`, `.factory/missions/`.

### P6 — Vercel production deploy — pending
- started: —
- notes: `vercel.json` already has SPA fallback (`source: /(.*), destination: /index.html`). Repo is connected to GitHub. Auto-deploy from `main` may already have triggered on pushes `cf962b1` and `ea585d1`. If not, run `vercel --prod`.

### P7 — Lighthouse audit — pending
- depends on: P6

### P8 — Real-device install test — pending
- depends on: P6

### P9 — Analytics / monitoring setup — pending
- depends on: P6
- notes: Optional. Constraint: no third-party trackers or analytics SDKs. Sentry is acceptable for error tracking if user opts in.

## 4. Constraints

- No emoji in code.
- No third-party trackers, analytics SDKs, or ad networks.
- Local-first data; no cloud sync, no backend, no accounts.
- Privacy nutrition label and data safety form must match actual app behavior (relevant for deferred store launch).
- Tag `1.0.0` on `main` only when native store launch (deferred mission) reaches 100% rollout.
- PWA version stays at `0.1.0` until native parity is achieved.

## 5. Definition of Done

- [ ] PWA live on public HTTPS URL
- [ ] Lighthouse PWA audit >= 95
- [ ] Installable on iOS Safari (Add to Home Screen) and Android Chrome
- [ ] Offline after first load (airplane mode test passes)
- [ ] Share deep-link (`/share/:payload`) works on mobile browsers
- [ ] Theme (dark/light/auto) persists across reinstalls via `localStorage`
- [ ] No console errors on first load in production

## 6. Risks & rollback

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Vercel build fails on push | low | `vercel.json` and `vite.config.ts` already validated locally. CI not set up but build is deterministic. |
| Service worker doesn't register in production | low | `registerSW.js` is auto-generated and injected by vite-plugin-pwa. Verified in `dist/index.html`. |
| Icons don't render on iOS home screen | low | SVG icons with `purpose: maskable` should work, but iOS historically prefers PNG. May need PNG fallbacks. |
| Chunk size hurts mobile performance | medium | 758 KiB JS bundle. Mitigation: code-split routes with dynamic `import()` after launch. |
| Share-link payload too large for SMS | low | Payload is ~200-400 chars base64url. Well within SMS limits. |

## 7. Architecture Decisions

- **PWA-first over native-first:** Ship to users immediately, gather feedback, then invest in store approval process.
- **Local-first over cloud:** No backend, no auth, no database. All data stays on-device. Share links are self-contained base64url payloads.
- **vite-plugin-pwa over custom SW:** Generates Workbox-based service worker with precaching + runtime caching. Minimal config surface.
- **Zustand over Redux/Context:** Single store file, no boilerplate, persists only theme to localStorage.
- **Open-Meteo over paid weather API:** Free, no API key, shortwave-radiation data ideal for PV modeling.
- **SunSpec Modbus mock over real hardware for MVP:** Simulator produces realistic telemetry. Real inverter integration is a swap-in service.

## 8. Deferred work (helios-store-launch.md)

When this mission is complete and the PWA is validated, resume:
- L0: Apple Developer + Google Play Console account setup
- L1-L4: Xcode / Android target wiring + accessibility (builder-droid ready)
- L5: Translation pass
- L6-L10: Device QA, TestFlight, Play Internal, store submission, production launch

## 9. Handover state

- Repo: `/Users/yashgupta/Desktop/yash/code/Helios`, remote `https://github.com/yashgpt2894/Helios.git`, branch `main`
- Latest commit: `ea585d1`
- PWA build output: `dist/` (static, SPA fallback configured)
- Native code: `ios/` and `android/` — feature-complete, untouched
- Mission files: `.factory/missions/helios-native-apps.md` (complete), `.factory/missions/helios-store-launch.md` (deferred), `.factory/missions/helios-pwa-launch.md` (this file, active)
