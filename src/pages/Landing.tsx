import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import '../landing.css';
import { HeliosMark } from '../components/HeliosMark';
import { DualSupplyPictogram } from '../components/landing/Pictograms';
import { InstrumentPlate } from '../components/landing/InstrumentPlate';
import { DayBand } from '../components/landing/DayBand';
import { Conductors } from '../components/landing/Conductors';
import { NoticeSet } from '../components/landing/NoticeSet';
import { LabelSheet } from '../components/landing/LabelSheet';
import { applyTheme, loadTheme, persistTheme, resolveTheme, type Theme } from '../lib/theme';
import { brandFromSearch, applyBrandAccent } from '../services/brand';
import {
  SITE,
  buildDay,
  dayTotals,
  formatHour,
  frameIndexForHour,
  hourFloatNow,
  noticesFor
} from '../lib/placardModel';

const FIELDS = [
  { id: '01', name: 'System identification' },
  { id: '02', name: 'The day' },
  { id: '03', name: 'The conductors' },
  { id: '04', name: 'Notice set' },
  { id: '05', name: 'Label set' },
  { id: '06', name: 'Operating instructions' }
] as const;

const CLOCK_MS = 30_000;

function useStillness() {
  const [still, setStill] = useState(false);
  useEffect(() => {
    if (typeof window.matchMedia !== 'function') return;
    const query = window.matchMedia('(prefers-reduced-motion: reduce)');
    setStill(query.matches);
    const handler = (event: MediaQueryListEvent) => setStill(event.matches);
    query.addEventListener('change', handler);
    return () => query.removeEventListener('change', handler);
  }, []);
  return still;
}

/** Real install affordance when the browser offers one; the button simply is not rendered otherwise. */
function useInstallPrompt() {
  const [event, setEvent] = useState<(Event & { prompt?: () => Promise<void> }) | null>(null);
  useEffect(() => {
    const handler = (e: Event) => {
      e.preventDefault();
      setEvent(e as Event & { prompt?: () => Promise<void> });
    };
    window.addEventListener('beforeinstallprompt', handler);
    return () => window.removeEventListener('beforeinstallprompt', handler);
  }, []);
  const prompt = useCallback(async () => {
    await event?.prompt?.();
    setEvent(null);
  }, [event]);
  return { available: Boolean(event), prompt };
}

export function Landing() {
  const frames = useMemo(() => buildDay(), []);
  const totals = useMemo(() => dayTotals(), []);
  const still = useStillness();
  const install = useInstallPrompt();

  const [theme, setTheme] = useState<Theme>(() => loadTheme());
  const [index, setIndex] = useState(() => frameIndexForHour(hourFloatNow()));
  const [following, setFollowing] = useState(true);
  const [progress, setProgress] = useState(0);
  const [fieldName, setFieldName] = useState<string>(FIELDS[0].name);

  const brand = useMemo(() => brandFromSearch(window.location.search), []);
  const rootRef = useRef<HTMLDivElement | null>(null);
  const frame = frames[Math.min(index, frames.length - 1)];
  const notices = useMemo(() => noticesFor(frame, totals), [frame, totals]);

  // The sheet joins the app's existing theme contract: same storage key, same resolved attribute.
  useEffect(() => {
    applyTheme(theme);
    const root = document.documentElement;
    root.classList.add('pc-root');
    applyBrandAccent(brand, theme);
    return () => {
      root.classList.remove('pc-root');
    };
  }, [theme, brand]);

  // Follow the local clock while the visitor has not taken the selector, and stop while hidden.
  useEffect(() => {
    if (!following) return;
    const tick = () => {
      if (document.hidden) return;
      setIndex(frameIndexForHour(hourFloatNow()));
    };
    tick();
    const id = window.setInterval(tick, CLOCK_MS);
    const onVisible = () => {
      if (!document.hidden) tick();
    };
    document.addEventListener('visibilitychange', onVisible);
    return () => {
      window.clearInterval(id);
      document.removeEventListener('visibilitychange', onVisible);
    };
  }, [following]);

  // The lamp: pointer position drives the sheeting's retroreflective response.
  useEffect(() => {
    if (still) return;
    const node = rootRef.current;
    if (!node) return;
    let frameId = 0;
    let x = 0.72;
    let y = 0.12;
    const apply = () => {
      frameId = 0;
      node.style.setProperty('--pc-lamp-x', `${(x * 100).toFixed(1)}%`);
      node.style.setProperty('--pc-lamp-y', `${(y * 100).toFixed(1)}%`);
    };
    const onMove = (event: PointerEvent) => {
      x = event.clientX / window.innerWidth;
      y = event.clientY / window.innerHeight;
      if (!frameId) frameId = requestAnimationFrame(apply);
    };
    window.addEventListener('pointermove', onMove, { passive: true });
    return () => {
      window.removeEventListener('pointermove', onMove);
      if (frameId) cancelAnimationFrame(frameId);
    };
  }, [still]);

  // Sheet position: how far through the six fields the reader is.
  useEffect(() => {
    const onScroll = () => {
      const max = document.documentElement.scrollHeight - window.innerHeight;
      setProgress(max > 0 ? Math.min(1, Math.max(0, window.scrollY / max)) : 0);
    };
    onScroll();
    window.addEventListener('scroll', onScroll, { passive: true });
    window.addEventListener('resize', onScroll);
    return () => {
      window.removeEventListener('scroll', onScroll);
      window.removeEventListener('resize', onScroll);
    };
  }, []);

  useEffect(() => {
    const sections = Array.from(document.querySelectorAll<HTMLElement>('[data-pc-field]'));
    if (!sections.length || typeof IntersectionObserver === 'undefined') return;
    const observer = new IntersectionObserver(
      (entries) => {
        const visible = entries
          .filter((entry) => entry.isIntersecting)
          .sort((a, b) => b.intersectionRatio - a.intersectionRatio)[0];
        const name = visible?.target.getAttribute('data-pc-field');
        if (name) setFieldName(name);
      },
      { rootMargin: '-30% 0px -55% 0px', threshold: [0.08, 0.3, 0.6] }
    );
    sections.forEach((section) => observer.observe(section));
    return () => observer.disconnect();
  }, []);

  const toggleTheme = useCallback(
    (next: 'light' | 'dark') => {
      persistTheme(next);
      setTheme(next);
    },
    []
  );

  const resolvedTheme = resolveTheme(theme);

  return (
    <div className="pc" ref={rootRef}>
      <div className="pc-sheen" aria-hidden="true" />

      <div className={`pc-position${progress > 0.012 ? ' pc-position--active' : ''}`} aria-hidden="true">
        <span className="pc-position__track" />
        <span className="pc-position__fill" style={{ width: `${progress * 100}%` }} />
        <span className="pc-position__name">{fieldName}</span>
      </div>

      <div className="pc-sheet">
        <header className="pc-issuer">
          <span className="pc-issuer__brand">
            <span className="pc-logoblock" aria-hidden="true">
              <HeliosMark size={24} brand={brand} />
            </span>
            {brand.id === 'helios' ? 'helios°' : brand.name}
          </span>
          <span className="pc-issuer__meta">
            <span>
              Sheet <strong>01</strong> · six fields · rev <strong>0.1.0</strong>
            </span>
            <span>
              Marked system: <strong>PV + utility</strong>
            </span>
            <span>
              Reference: <strong>{SITE.model}</strong>
            </span>
            <span className="pc-brandflag">
              <i
                style={{ background: resolvedTheme === 'light' ? brand.accentLight : brand.accent }}
                aria-hidden="true"
              />
              Brand · <strong>{brand.name}</strong>
            </span>
          </span>
          <span className="pc-issuer__meta" style={{ marginLeft: 0 }} role="group" aria-label="Sheet light">
            <span className="pc-tag" id="pc-sheet-light">
              Sheet light
            </span>
            <button
              type="button"
              className="pc-btn pc-btn--ghost"
              style={{ padding: '7px 12px 6px', fontSize: '0.86rem' }}
              aria-pressed={resolvedTheme === 'light'}
              onClick={() => toggleTheme('light')}
            >
              Daylight
            </button>
            <button
              type="button"
              className="pc-btn pc-btn--ghost"
              style={{ padding: '7px 12px 6px', fontSize: '0.86rem' }}
              aria-pressed={resolvedTheme === 'dark'}
              onClick={() => toggleTheme('dark')}
            >
              Headlamp
            </button>
          </span>
        </header>

        <main>
        <section className="pc-field" data-pc-field="System identification" id="pc-01" style={{ borderTop: 0 }}>
          <div className="pc-signal">
            <span className="pc-signal__word">Notice</span>
            <span className="pc-signal__text">
              <span>Photovoltaic system present</span>
              <span>·</span>
              <span>Dual supply</span>
              <span>·</span>
              <span>Marked at the utility wall</span>
            </span>
          </div>

          <div className="pc-hero">
            <div>
              <h1 className="pc-display pc-h1">
                <DualSupplyPictogram className="pc-hero__pict" />
                Your house has two supplies.
              </h1>
              <p className="pc-display pc-hero__second">helios° reads the one on the roof.</p>
              <p className="pc-lead pc-hero__lead">
                The grid is one. The array is the other. It wakes at 06:12, peaks at 13:00 and stops
                at 19:48. helios° reads that second supply through the inverter on the wall and turns it into
                one instruction at a time: run the heavy load now, pre-charge before the cloud, string C
                is shaded.
              </p>
              <div className="pc-facts">
                <div>
                  <span className="pc-facts__k">System</span>
                  <span className="pc-facts__v">{SITE.ratedDcW / 1000} kW DC · 3 strings</span>
                </div>
                <div>
                  <span className="pc-facts__k">Storage</span>
                  <span className="pc-facts__v">{SITE.batteryKwh} kWh</span>
                </div>
                <div>
                  <span className="pc-facts__k">Conversion</span>
                  <span className="pc-facts__v">{SITE.conversion}</span>
                </div>
                <div>
                  <span className="pc-facts__k">Model</span>
                  <span className="pc-facts__v">{SITE.model}</span>
                </div>
              </div>
            </div>

            <div className="pc-hero__plate">
              <InstrumentPlate frame={frame} following={following} />
            </div>
          </div>

          <div className="pc-actline">
            <Link className="pc-btn" to="/app">
              Open the dashboard
            </Link>
            {install.available ? (
              <button type="button" className="pc-btn pc-btn--ghost" onClick={() => void install.prompt()}>
                Add to home screen
              </button>
            ) : (
              <span className="pc-note" style={{ maxWidth: '34ch' }}>
                Installable from the browser menu: <strong>Install app</strong>, or{' '}
                <strong>Add to Home Screen</strong> on iOS.
              </span>
            )}
            <p className="pc-guarantee">
              No account · no server · readings stay on the device
            </p>
          </div>

          <div className="pc-amend">
            <span className="pc-amend__k">Amendment, pasted over the sign</span>
            <p className="pc-amend__body">
              helios° is a one-person project, so this is the honest version of it: the readings are
              simulated, no inverter is connected yet, and there are no customers to quote. Everything
              else on the sheet is real and in the repository: the model, the protocol, the sharing, the
              marking. <strong>Look it up before you believe a word of it.</strong>
            </p>
          </div>
        </section>

        <section className="pc-field" data-pc-field="The day" id="pc-02">
          <div className="pc-field__head">
            <span className="pc-field__id">02 · The day</span>
            <h2 className="pc-display pc-h2 pc-field__title">One roof, one day, quarter-hour by quarter-hour</h2>
            <p className="pc-field__aside pc-tag">
              Simulated · peak {(totals.peakAcW / 1000).toFixed(2)} kW at {formatHour(totals.peakHour)} ·{' '}
              {Math.round(totals.selfConsumptionPct)}% used on site
            </p>
          </div>
          <DayBand
            frames={frames}
            index={index}
            totals={totals}
            following={following}
            onSelect={(next) => {
              setFollowing(false);
              setIndex(next);
            }}
            onNow={() => {
              setFollowing(true);
              setIndex(frameIndexForHour(hourFloatNow()));
            }}
          />
        </section>

        <section className="pc-field" data-pc-field="The conductors" id="pc-03">
          <div className="pc-field__head">
            <span className="pc-field__id">03 · The conductors</span>
            <h2 className="pc-display pc-h2 pc-field__title">Every path named, every path accounted for</h2>
            <p className="pc-field__aside pc-tag">Simulated · read at {formatHour(frame.hour)}</p>
          </div>
          <Conductors frame={frame} />
        </section>

        <section className="pc-field" data-pc-field="Notice set" id="pc-04">
          <div className="pc-field__head">
            <span className="pc-field__id">04 · Notice set</span>
            <h2 className="pc-display pc-h2 pc-field__title">Notices, not charts</h2>
            <p className="pc-field__aside pc-tag">
              Simulated · read at {formatHour(frame.hour)} · {notices.length} raised
            </p>
          </div>
          <NoticeSet frame={frame} notices={notices} />
          <p className="pc-note" style={{ marginTop: '18px' }}>
            Thresholds are the ones the app ships: a string more than 15% behind its peers, a heatsink
            above 48 °C, a battery above 92% with surplus leaving the house. Nothing here is a score
            against other homes. The app has never seen another home.
          </p>
        </section>

        <section className="pc-field" data-pc-field="Label set" id="pc-05">
          <div className="pc-field__head">
            <span className="pc-field__id">05 · Label set</span>
            <h2 className="pc-display pc-h2 pc-field__title">The rest of the marking, honestly stated</h2>
            <p className="pc-field__aside pc-tag">Each label checkable in the repository</p>
          </div>
          <LabelSheet light={resolvedTheme === 'light'} />
        </section>

        <section className="pc-field" data-pc-field="Operating instructions" id="pc-06">
          <div className="pc-field__head">
            <span className="pc-field__id">06 · Operating instructions</span>
            <h2 className="pc-display pc-h2 pc-field__title">Three steps, then the roof talks</h2>
          </div>

          <ol className="pc-steps">
            <li className="pc-step">
              <span className="pc-step__n">Step 01</span>
              <h3 className="pc-step__title">Install it</h3>
              <p className="pc-step__body">
                Add helios° to the home screen. There is no sign-up, no trial, and nothing to cancel.
              </p>
            </li>
            <li className="pc-step">
              <span className="pc-step__n">Step 02</span>
              <h3 className="pc-step__title">Point it at your inverter</h3>
              <p className="pc-step__body">
                Out of the box it reads a simulated HX-9.6. Your hardware is a gateway away: a browser
                cannot speak Modbus TCP itself, so a small relay on the home network hands the app its own
                registers.
              </p>
            </li>
            <li className="pc-step">
              <span className="pc-step__n">Step 03</span>
              <h3 className="pc-step__title">Read the notices</h3>
              <p className="pc-step__body">
                Peak windows, shading, thermal margin, overnight reserve. One instruction at a time,
                while there is still time to act on it.
              </p>
            </li>
          </ol>

          <div className="pc-honest">
            <div>
              <h3 className="pc-honest__title">What this sheet does not have</h3>
              <p className="pc-body">
                A landing page is where products usually inflate. This one would rather be believed, so
                here is the full list of what is missing.
              </p>
            </div>
            <ul className="pc-honest__list">
              <li>
                <b>01</b>
                <span>
                  <strong>No customers shown.</strong> Nobody has installed this yet, so no faces, no
                  quotes, no logos.
                </span>
              </li>
              <li>
                <b>02</b>
                <span>
                  <strong>No press, awards or certifications.</strong> There are none to cite.
                </span>
              </li>
              <li>
                <b>03</b>
                <span>
                  <strong>No price.</strong> Nothing is charged for the build in the repository.
                </span>
              </li>
              <li>
                <b>04</b>
                <span>
                  <strong>No live inverter.</strong> Every figure on this page comes from the shipped
                  simulator, and each one is labelled that way.
                </span>
              </li>
            </ul>
          </div>

          <div className="pc-close">
            <Link className="pc-btn" to="/app">
              Open the dashboard
            </Link>
            <a
              className="pc-btn pc-btn--ghost"
              href="https://github.com/yashgpt2894/Helios"
              rel="noreferrer noopener"
              target="_blank"
            >
              Read the source
            </a>
            <p className="pc-guarantee">Works offline after first load</p>
          </div>

          <footer className="pc-foot">
            <span>
              <strong>helios° energy</strong> · precision energy intelligence for your solar array
            </span>
            <span>Sheet 01 · revision 0.1.0</span>
            <span className="pc-foot__end">Marked for a residence with a photovoltaic system</span>
          </footer>
        </section>
        </main>
      </div>
    </div>
  );
}
