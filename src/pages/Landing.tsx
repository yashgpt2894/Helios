import { useEffect } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { ArrowRight, ArrowUpRight, Moon, MonitorCog, Sun } from 'lucide-react';
import { HeliosMark } from '../components/HeliosMark';
import { Ledger } from '../components/landing/Ledger';
import { DayStroke } from '../components/landing/DayStroke';
import { BatteryModule, PhoneDashboard } from '../components/landing/Instrument';
import { useDemoClock } from '../hooks/useDemoClock';
import { useStore } from '../store/useStore';
import { listBrands, HELIOS } from '../services/brand';
import { DEMO_DAY_KWH } from '../lib/demoDay';

const REPO_URL = 'https://github.com/yashgpt2894/Helios';

export function Landing() {
  const [params] = useSearchParams();
  const brand = useStore((s) => s.brand);
  const setBrandFromSearch = useStore((s) => s.setBrandFromSearch);
  const clock = useDemoClock();

  const search = params.toString();
  useEffect(() => {
    setBrandFromSearch(search ? `?${search}` : '');
  }, [search, setBrandFromSearch]);

  const appHref = search ? `/app?${search}` : '/app';
  const brandParam = params.get('brand');

  return (
    <div className="lp min-h-screen bg-ground-base text-ink-100">
      <a
        href="#main"
        className="sr-only focus:not-sr-only focus:fixed focus:left-4 focus:top-4 focus:z-50 focus:rounded-full focus:bg-ground-3 focus:px-4 focus:py-2 focus:text-sm"
      >
        Skip to content
      </a>

      <header className="sticky top-0 z-40 border-b border-line bg-ground-base/95">
        <div className="mx-auto flex h-16 max-w-[1280px] items-center justify-between gap-4 px-5 md:px-8">
          <Link to={search ? `/?${search}` : '/'} className="tap flex items-center gap-2.5 rounded-full" aria-label={`${brand.name} home`}>
            <HeliosMark size={30} brand={brand} />
            <span className="text-[15px] font-medium tracking-tight text-ink-100">{brand.name}</span>
          </Link>
          <nav className="hidden items-center gap-7 text-[13px] text-ink-400 md:flex" aria-label="Sections">
            <a href="#day" className="tap-lg rounded-sm transition-colors hover:text-ink-100">
              One day
            </a>
            <a href="#instrument" className="tap-lg rounded-sm transition-colors hover:text-ink-100">
              The instrument
            </a>
            <a href="#connect" className="tap-lg rounded-sm transition-colors hover:text-ink-100">
              How it connects
            </a>
          </nav>
          <div className="flex items-center gap-3">
            <ThemeToggle />
            <Link
              to={appHref}
              className="tap inline-flex h-9 items-center gap-1.5 rounded-full bg-ink-300 px-4 text-[13px] font-medium text-ground-base transition-colors hover:bg-ink-200"
            >
              Open the app
              <ArrowRight size={14} strokeWidth={2} aria-hidden="true" />
            </Link>
          </div>
        </div>
      </header>

      <main id="main">
        <section aria-labelledby="hero-title">
          <div className="grid-bg mx-auto max-w-[1280px] bg-[position:1.25rem_0] px-5 pb-14 pt-8 md:bg-[position:2rem_0] md:px-8 md:pb-24 md:pt-[4.5rem]">
            <h1 id="hero-title" className="display text-[2.25rem] text-ink-200 sm:text-[3.25rem] lg:text-[4.25rem]">
              Your inverter speaks in registers.
              <br />
              <span className="text-ink-400">{brand.name} turns them into decisions.</span>
            </h1>
            <div className="mt-6 flex flex-col gap-6 md:mt-10 md:flex-row md:items-end md:justify-between">
              <p className="hidden max-w-[34rem] text-[16px] leading-[1.6] text-ink-300 md:block md:text-[16px]">
                {brand.name} polls the SunSpec points on your inverter over your own network, fuses them with a real
                irradiance forecast, and says what to do about it: run the dryer now, hold the battery for the evening,
                look at string B after sunset. Nothing leaves the house except one weather request.
              </p>
              <div className="flex shrink-0 flex-wrap items-center gap-x-6 gap-y-3">
                <Link
                  to={appHref}
                  className="inline-flex h-12 items-center gap-2 rounded-full bg-ink-300 px-6 text-[15px] font-medium text-ground-base transition-colors hover:bg-ink-200"
                >
                  Open the app
                  <ArrowRight size={16} strokeWidth={2} aria-hidden="true" />
                </Link>
                <a
                  href="#connect"
                  className="tap-lg inline-flex items-center gap-1.5 text-[15px] text-ink-200 underline decoration-[var(--hairline-strong)] underline-offset-[6px] transition-colors hover:text-ink-100 hover:decoration-[var(--text-300)]"
                >
                  How it connects
                  <ArrowRight size={14} strokeWidth={1.8} aria-hidden="true" />
                </a>
              </div>
            </div>

            <Ledger clock={clock} />
            <p className="mt-8 text-[15px] leading-[1.6] text-ink-300 md:hidden">
              {brand.name} polls the SunSpec points on your inverter over your own network, fuses them with a real
              irradiance forecast, and says what to do about it: run the dryer now, hold the battery for the evening,
              look at string B after sunset. Nothing leaves the house except one weather request.
            </p>
          </div>
        </section>

        <section
          id="day"
          className="field scroll-mt-16"
          style={{ ['--field' as string]: brand.accent, ['--on-field' as string]: '#0b0b0c' }}
          aria-labelledby="day-title"
        >
          <div className="mx-auto max-w-[1280px] px-5 py-16 md:px-8 md:py-20">
            <div className="grid gap-6 md:grid-cols-12 md:gap-10">
              <h2 id="day-title" className="display text-[2.25rem] md:col-span-6 md:text-[2.6rem] lg:text-[3.25rem]">
                One simulated day, read by {brand.name}.
              </h2>
              <p className="max-w-[32rem] self-end text-[15px] leading-[1.6] text-[var(--field-ink-2)] md:col-span-6 md:text-[16px]">
                The shipped mock is a 9.6 kW hybrid system: three strings, a 13.5 kWh battery, a house that peaks at
                3.6 kW in the evening. Sampled every two seconds from midnight to midnight it makes{' '}
                <span className="num-mono text-[var(--on-field)]">{DEMO_DAY_KWH.toFixed(1)} kWh</span>. Each note is what
                the app's insight engine actually said at that hour.
              </p>
            </div>
            <div className="mt-12 md:mt-16">
              <DayStroke />
            </div>
          </div>
        </section>

        <section id="instrument" className="scroll-mt-16 border-b border-line" aria-labelledby="instrument-title">
          <div className="mx-auto max-w-[1280px] px-5 py-16 md:px-8 md:py-20">
            <div className="grid grid-cols-1 gap-12 lg:grid-cols-12 lg:gap-10">
              <div className="min-w-0 lg:col-span-5">
                <h2 id="instrument-title" className="display text-[2.25rem] text-ink-100 md:text-[2.6rem] lg:text-[3.25rem]">
                  The instrument itself.
                </h2>
                <p className="mt-6 max-w-[32rem] text-[15px] leading-[1.6] text-ink-300 md:text-[16px]">
                  This is the dashboard as it ships, running on the same simulated system as the ledger, in the frame it
                  renders in on a phone. On a desktop browser the app keeps this width and centers itself. On a phone it
                  installs to the home screen and works offline after the first load.
                </p>
                <dl className="mt-8 grid grid-cols-2 gap-x-6 gap-y-5 border-t border-line pt-6 text-[13px] sm:grid-cols-3 lg:grid-cols-2">
                  <Fact label="Poll interval" value="2 000 ms" />
                  <Fact label="Strings" value="3 × 8 panels" />
                  <Fact label="Battery" value="13.5 kWh" />
                  <Fact label="Forecast" value="7 days · Open-Meteo" />
                  <Fact label="Inverter health" value="DC · AC · Hz · °C" />
                  <Fact label="Themes" value="light · dark · auto" />
                </dl>
                <div className="mt-8">
                  <BatteryModule t={clock.t} />
                </div>
              </div>
              <div className="min-w-0 lg:col-span-7 lg:pl-6">
                <PhoneDashboard t={clock.t} hour={clock.hour} brand={brand} />
              </div>
            </div>
          </div>
        </section>

        <section id="connect" className="grid-bg scroll-mt-16 bg-ground-1" aria-labelledby="connect-title">
          <div className="mx-auto max-w-[1280px] px-5 py-16 md:px-8 md:py-20">
            <div className="grid gap-6 md:grid-cols-12 md:gap-10">
              <h2 id="connect-title" className="display text-[2.25rem] text-ink-100 md:col-span-7 md:text-[2.6rem] lg:text-[3.25rem]">
                Nothing leaves your network except a weather request.
              </h2>
              <p className="max-w-[32rem] self-end text-[15px] leading-[1.6] text-ink-300 md:col-span-5 md:text-[16px]">
                {brand.name} reads SunSpec Modbus TCP, the open register map that SMA, Fronius, SolarEdge, Enphase,
                Schneider and most other inverters expose on the local network. No account, no vendor cloud, no
                analytics. The forecast is one request to Open-Meteo and needs no API key.
              </p>
            </div>

            <ol className="mt-12 grid md:mt-16 md:grid-cols-3 md:gap-10" aria-label="Signal path">
              <PathNode
                title="Inverter"
                lines={['SunSpec Modbus TCP', 'port 502 · unit 1', 'models 101 · 160 · 802']}
                body="Any SunSpec-compatible hybrid inverter. helios° never writes to it; it only reads registers."
              />
              <PathNode
                title="Relay on your LAN"
                lines={['Modbus → WebSocket', 'runs on a Pi or the router', 'stays inside the house']}
                body="Browsers cannot open raw TCP sockets, so a small relay bridges the inverter to the app. The build ships with the simulated inverter enabled instead."
              />
              <PathNode
                title={`${brand.name} on your phone`}
                lines={['reads every 2 s', 'decides on-device', 'installs · works offline']}
                body="The forecast is fetched hourly from Open-Meteo using your coordinates, which stay in the browser."
                last
              />
            </ol>

            <div className="mt-12 grid gap-8 border-t border-line pt-10 md:mt-16 md:grid-cols-3 md:gap-10">
              <div>
                <h3 className="text-[15px] font-semibold text-ink-100">Share without a server</h3>
                <p className="mt-2 text-[14px] leading-relaxed text-ink-400">
                  A snapshot is a base64url payload inside the link itself. Whoever opens it decodes it on their own
                  device, in the brand it was sent from. No database, no expiry server, no tracking pixel.
                </p>
              </div>
              <div>
                <h3 className="text-[15px] font-semibold text-ink-100">Your installer's colors</h3>
                <p className="mt-2 text-[14px] leading-relaxed text-ink-400">
                  <code className="num-mono rounded bg-ground-3 px-1.5 py-0.5 text-[12px] text-ink-200">?brand=</code> re-skins
                  the accent, mark and copy, and rides along in every share link. Try it on this page:
                </p>
                <ul className="mt-3 flex flex-wrap gap-2" aria-label="Brands">
                  {[HELIOS, ...listBrands().filter((b) => b.id !== 'helios')].map((b) => {
                    const active = (brandParam ?? 'helios').toLowerCase() === b.id;
                    return (
                      <li key={b.id}>
                        <Link
                          to={b.id === 'helios' ? '/' : `/?brand=${b.id}`}
                          aria-current={active ? 'true' : undefined}
                          className={`tap inline-flex h-8 items-center gap-2 rounded-full border px-3 text-[12px] transition-colors ${
                            active
                              ? 'border-line-strong bg-ground-3 text-ink-100'
                              : 'border-line text-ink-300 hover:border-line-strong hover:text-ink-100'
                          }`}
                        >
                          <span className="size-2 rounded-full" style={{ background: b.accent }} aria-hidden="true" />
                          {b.name}
                        </Link>
                      </li>
                    );
                  })}
                </ul>
              </div>
              <div>
                <h3 className="text-[15px] font-semibold text-ink-100">Open source</h3>
                <p className="mt-2 text-[14px] leading-relaxed text-ink-400">
                  MIT licensed. Vite, React, TypeScript, a mock SunSpec service you can swap for a real reader that
                  returns the same telemetry shape.
                </p>
                <a
                  href={REPO_URL}
                  className="tap-lg mt-3 inline-flex items-center gap-1.5 text-[13px] text-ink-200 underline decoration-[var(--hairline-strong)] underline-offset-4 transition-colors hover:text-ink-100 hover:decoration-[var(--text-300)]"
                >
                  Source on GitHub
                  <ArrowUpRight size={13} strokeWidth={1.8} aria-hidden="true" />
                </a>
              </div>
            </div>
          </div>
        </section>

        <section aria-labelledby="close-title">
          <div className="mx-auto max-w-[1280px] px-5 pb-16 pt-20 md:px-8 md:pb-20 md:pt-24">
            <div className="grid gap-8 md:grid-cols-12 md:items-end">
              <div className="md:col-span-7">
                <h2 id="close-title" className="display text-[2.25rem] text-ink-100 sm:text-[3.25rem] lg:text-[4.25rem]">
                  Ready when the sun is.
                </h2>
                <p className="mt-6 max-w-[34rem] text-[16px] leading-[1.6] text-ink-300 md:text-[16px]">
                  Open {brand.name} in any browser and add it to your home screen. It runs offline after the first load;
                  live telemetry is the only thing that needs a connection.
                </p>
              </div>
              <div className="flex flex-wrap items-center gap-3 md:col-span-5 md:justify-end">
                <Link
                  to={appHref}
                  className="inline-flex h-12 items-center gap-2 rounded-full bg-ink-300 px-6 text-[15px] font-medium text-ground-base transition-colors hover:bg-ink-200"
                >
                  Open the app
                  <ArrowRight size={16} strokeWidth={2} aria-hidden="true" />
                </Link>
                <details className="group basis-full sm:basis-auto">
                  <summary className="inline-flex h-12 cursor-pointer list-none items-center gap-2 rounded-full border border-line-strong px-5 text-[15px] text-ink-200 transition-colors hover:border-ink-500 hover:text-ink-100 [&::-webkit-details-marker]:hidden">
                    Add to home screen
                  </summary>
                  <div className="surface-raised mt-3 w-full max-w-[22rem] p-5 text-[13px] leading-relaxed text-ink-300">
                    <p>
                      <span className="text-ink-100">iPhone and iPad:</span> open the app in Safari, tap Share, then{' '}
                      <span className="text-ink-100">Add to Home Screen</span>.
                    </p>
                    <p className="mt-2">
                      <span className="text-ink-100">Android and desktop Chrome:</span> use the install icon in the
                      address bar, or the menu's <span className="text-ink-100">Install app</span>.
                    </p>
                    <p className="mt-3 font-mono text-[11px] text-ink-500">iOS and Android apps are in development.</p>
                  </div>
                </details>
              </div>
            </div>
          </div>

          <footer className="border-t border-line">
            <div className="mx-auto flex max-w-[1280px] flex-col gap-4 px-5 py-6 text-[12px] text-ink-500 md:flex-row md:items-center md:justify-between md:px-8">
              <div className="flex items-center gap-2.5">
                <HeliosMark size={22} brand={brand} />
                <span className="text-ink-300">{brand.legalName ?? brand.name}</span>
                <span aria-hidden="true">·</span>
                <span>No accounts. No trackers. MIT licensed.</span>
              </div>
              <div className="flex items-center gap-6">
                <a href={REPO_URL} className="tap-lg transition-colors hover:text-ink-100">
                  GitHub
                </a>
                <Link to={appHref} className="tap-lg transition-colors hover:text-ink-100">
                  Open the app
                </Link>
                <ThemeToggle />
              </div>
            </div>
          </footer>
        </section>
      </main>
    </div>
  );
}

function Fact({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <dt className="text-[12px] text-ink-500">{label}</dt>
      <dd className="num-mono mt-1 text-[13px] text-ink-100">{value}</dd>
    </div>
  );
}

function PathNode({ title, lines, body, last }: { title: string; lines: string[]; body: string; last?: boolean }) {
  return (
    <li className="relative pb-9 pl-7 last:pb-0 md:pb-0 md:pl-0 md:pt-6">
      <span className="absolute left-0 top-1 size-2 rounded-full bg-sig-solar md:top-0" aria-hidden="true" />
      {!last && (
        <>
          <span className="dash-v absolute bottom-0 left-[3.5px] top-4 md:hidden" aria-hidden="true" />
          <span className="dash-h absolute left-3 right-[-2.5rem] top-[3.5px] hidden md:block" aria-hidden="true" />
        </>
      )}
      <h3 className="text-[16px] font-semibold leading-none text-ink-100">{title}</h3>
      <ul className="mt-3 space-y-1 font-mono text-[11px] text-ink-400">
        {lines.map((l) => (
          <li key={l}>{l}</li>
        ))}
      </ul>
      <p className="mt-4 max-w-[26rem] text-[13px] leading-relaxed text-ink-500">{body}</p>
    </li>
  );
}

function ThemeToggle() {
  const theme = useStore((s) => s.theme);
  const cycleTheme = useStore((s) => s.cycleTheme);
  const Icon = theme === 'light' ? Sun : theme === 'dark' ? Moon : MonitorCog;
  const label = theme === 'auto' ? 'auto theme' : `${theme} theme`;
  return (
    <button
      type="button"
      onClick={cycleTheme}
      aria-label={`Switch theme, currently ${label}`}
      title={label}
      className="tap flex size-9 items-center justify-center rounded-full border border-line text-ink-300 transition-colors hover:text-ink-100"
    >
      <Icon size={14} strokeWidth={1.6} aria-hidden="true" />
    </button>
  );
}
