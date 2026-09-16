import { useCallback, useEffect, useLayoutEffect, useMemo, useRef, useState, type CSSProperties } from 'react';
import { AnimatePresence, motion } from 'framer-motion';
import { Pause, Play, SkipForward } from 'lucide-react';
import type { Insight, SolarTelemetry } from '../../types';
import type { DemoClock } from '../../hooks/useDemoClock';
import { generateInsights } from '../../services/aiInsights';
import { INSIGHT_SOURCES, featuredInsight, formatDemoClock, registerRows, type RegisterId } from '../../lib/demoDay';

const EASE = [0.16, 1, 0.3, 1] as const;
const GRID = 24;

const SEVERITY_DOT: Record<Insight['severity'], string> = {
  positive: 'bg-sig-flow',
  attention: 'bg-sig-solar',
  critical: 'bg-sig-alert',
  neutral: 'bg-ink-500'
};

/* The savings insight quotes dollar figures that depend on a tariff the page does not state,
   so the landing never features it. The app still shows it inside the dashboard. */
const EXCLUDED_ON_LANDING = new Set(['today-savings']);

interface Connector {
  id: RegisterId;
  d: string;
  x: number;
  y: number;
}

interface Featured {
  insight: Insight;
  sources: RegisterId[];
  quiet: boolean;
}

/* When the engine has nothing to say, the card says so and shows what the registers read instead. */
function quietNote(t: SolarTelemetry): Featured {
  const soc = Math.round(t.batterySoc);
  const load = (t.homeLoadW / 1000).toFixed(2);
  const discharging = t.batteryPowerW < -30;
  const importing = t.gridImportW > 30;
  const body = discharging
    ? `The house is drawing ${load} kW and the battery is carrying it at ${soc}%. Nothing here needs a decision; the next read may change that.`
    : importing
      ? `The house is drawing ${load} kW from the grid with the battery at ${soc}%. Nothing here needs a decision; the next read may change that.`
      : `The house is drawing ${load} kW with the battery at ${soc}%. Nothing here needs a decision; the next read may change that.`;
  return {
    quiet: true,
    sources: discharging ? ['bw', 'soc'] : importing ? ['mw', 'soc'] : ['w', 'soc'],
    insight: {
      id: 'quiet',
      category: 'battery',
      severity: 'neutral',
      title: 'Nothing to act on.',
      body,
      metric: `${load} kW`,
      delta: 'house load'
    }
  };
}

export function Ledger({ clock }: { clock: DemoClock }) {
  const { t, hour, tick, paused, running, reduced, toggle, step } = clock;
  const rows = useMemo(() => registerRows(t), [t]);
  const insights = useMemo(() => generateInsights(t).filter((i) => !EXCLUDED_ON_LANDING.has(i.id)), [t]);
  const featured = useMemo<Featured>(() => {
    const top = featuredInsight(insights);
    return top ? { insight: top, sources: INSIGHT_SOURCES[top.id] ?? [], quiet: false } : quietNote(t);
  }, [insights, t]);
  const others = insights.filter((i) => i.id !== featured.insight.id).slice(0, 3);
  const { sources } = featured;
  const leadId = sources.find((id) => rows.find((r) => r.id === id)?.value !== undefined);

  const prevValues = useRef<Map<RegisterId, number>>(new Map());
  const deltas = useMemo(() => {
    const out = new Map<RegisterId, string>();
    for (const row of rows) {
      const before = prevValues.current.get(row.id);
      if (row.value === undefined || before === undefined) continue;
      const decimals = (row.reading.split('.')[1] ?? '').length;
      const d = row.value - before;
      const rounded = Number(d.toFixed(decimals));
      const sign = rounded > 0 ? '+' : rounded < 0 ? '−' : '';
      out.set(row.id, `${sign}${Math.abs(rounded).toFixed(decimals)}`);
    }
    return out;
  }, [rows]);
  useEffect(() => {
    const next = new Map<RegisterId, number>();
    for (const row of rows) if (row.value !== undefined) next.set(row.id, row.value);
    prevValues.current = next;
  }, [rows]);

  const gridRef = useRef<HTMLDivElement>(null);
  const cardRef = useRef<HTMLDivElement>(null);
  const listRef = useRef<HTMLOListElement>(null);
  const rowRefs = useRef(new Map<RegisterId, HTMLLIElement>());
  const [connectors, setConnectors] = useState<Connector[]>([]);

  /* Snap the first row's top hairline onto a dot row of the page's 24px grid.
     The grid's origin is the hero container; dots sit 1px inside each cell. */
  const snapRef = useRef(0);
  const [snap, setSnap] = useState(0);
  const snapToGrid = useCallback(() => {
    const list = listRef.current;
    const origin = list?.closest('.grid-bg');
    if (!list || !origin) return;
    if (!window.matchMedia('(min-width: 768px)').matches) {
      if (snapRef.current !== 0) {
        snapRef.current = 0;
        setSnap(0);
      }
      return;
    }
    const offset = list.getBoundingClientRect().top - origin.getBoundingClientRect().top - snapRef.current - 1;
    const rem = ((Math.round(offset) % GRID) + GRID) % GRID;
    const next = rem === 0 ? 0 : GRID - rem;
    if (next !== snapRef.current) {
      snapRef.current = next;
      setSnap(next);
    }
  }, []);

  const measure = useCallback(() => {
    const grid = gridRef.current;
    const card = cardRef.current;
    if (!grid || !card) return;
    if (!window.matchMedia('(min-width: 768px)').matches) {
      setConnectors([]);
      return;
    }
    const g = grid.getBoundingClientRect();
    const c = card.getBoundingClientRect();
    const x2 = c.left - g.left;
    const yBase = c.top - g.top + 40;
    const next: Connector[] = [];
    sources.forEach((id, i) => {
      const el = rowRefs.current.get(id);
      if (!el) return;
      const r = el.getBoundingClientRect();
      const x1 = r.right - g.left;
      const y1 = r.top - g.top + r.height / 2;
      const y2 = yBase + i * 14;
      const dx = Math.max(24, (x2 - x1) * 0.55);
      next.push({ id, x: x1, y: y1, d: `M ${x1} ${y1} C ${x1 + dx} ${y1}, ${x2 - dx} ${y2}, ${x2} ${y2}` });
    });
    setConnectors(next);
  }, [sources]);

  useLayoutEffect(() => {
    snapToGrid();
    measure();
  }, [measure, snapToGrid, tick, snap]);

  useEffect(() => {
    const grid = gridRef.current;
    if (!grid) return;
    const relayout = () => {
      snapToGrid();
      measure();
    };
    const ro = new ResizeObserver(relayout);
    ro.observe(grid);
    ro.observe(document.body);
    window.addEventListener('resize', relayout);
    document.fonts?.ready.then(relayout).catch(() => undefined);
    return () => {
      ro.disconnect();
      window.removeEventListener('resize', relayout);
    };
  }, [measure, snapToGrid]);

  const transition = reduced ? { duration: 0 } : { duration: 0.55, ease: EASE };
  const { insight } = featured;

  return (
    <div className="mt-8 md:mt-12" aria-label="Register ledger">
      <div className="flex flex-wrap items-center justify-between gap-x-6 gap-y-2 border-b border-line pb-3 font-mono text-[11px] text-ink-500">
        <span className="flex items-center gap-2">
          <span className="size-1.5 rounded-full bg-sig-flow" aria-hidden="true" />
          <span className="hidden sm:inline">SunSpec Modbus TCP · 192.168.1.42:502 · unit 1</span>
          <span className="sm:hidden">SunSpec Modbus TCP · 192.168.1.42:502</span>
        </span>
        <span className="flex flex-wrap items-center gap-x-4 gap-y-2">
          <span className="hidden sm:inline">Simulated 9.6 kW system · 12 min of the day per read</span>
          <span className="sm:hidden">Simulated · 12 min/read</span>
          <span className="flex items-center gap-2 text-ink-200">
            <PollArc key={tick} running={running} />
            <span className="num-mono tabular-nums">{formatDemoClock(hour)}</span>
          </span>
          <span className="flex items-center gap-1">
            {reduced ? (
              <button
                type="button"
                onClick={step}
                aria-label="Next read of the simulated clock"
                className="tap inline-flex h-7 items-center gap-1.5 rounded-full border border-line px-2.5 text-ink-300 transition-colors hover:text-ink-100"
              >
                <SkipForward size={11} strokeWidth={1.8} aria-hidden="true" />
                <span className="hidden sm:inline">Next read</span>
              </button>
            ) : (
              <button
                type="button"
                onClick={toggle}
                aria-pressed={paused}
                aria-label="Pause the simulated clock"
                className="tap inline-flex h-7 items-center gap-1.5 rounded-full border border-line px-2.5 text-ink-300 transition-colors hover:text-ink-100"
              >
                {paused ? (
                  <Play size={11} strokeWidth={1.8} aria-hidden="true" />
                ) : (
                  <Pause size={11} strokeWidth={1.8} aria-hidden="true" />
                )}
                <span className="hidden sm:inline">{paused ? 'Resume' : 'Pause'}</span>
              </button>
            )}
          </span>
        </span>
      </div>

      <div
        ref={gridRef}
        className="relative grid grid-cols-1 gap-6 pt-5 md:grid-cols-12 md:gap-12 md:pt-6"
        style={snap ? { paddingTop: GRID + snap } : undefined}
      >
        <svg className="pointer-events-none absolute inset-0 hidden h-full w-full md:block" aria-hidden="true">
          <AnimatePresence>
            {connectors.map((c) => (
              <motion.g
                key={c.id}
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                exit={{ opacity: 0 }}
                transition={transition}
              >
                <motion.path
                  initial={false}
                  animate={{ d: c.d }}
                  transition={transition}
                  fill="none"
                  stroke="var(--signal-solar)"
                  strokeWidth="1.25"
                  strokeDasharray="2 5"
                  strokeLinecap="round"
                />
                <motion.circle
                  initial={false}
                  animate={{ cx: c.x, cy: c.y }}
                  transition={transition}
                  r="3"
                  fill="var(--signal-solar)"
                />
              </motion.g>
            ))}
          </AnimatePresence>
        </svg>

        <ol
          ref={listRef}
          className="divide-y divide-[var(--hairline)] border-y border-line md:col-span-5"
          aria-label="SunSpec points read from the inverter"
        >
          {rows.map((row) => {
            const hot = sources.includes(row.id);
            const lead = row.id === leadId;
            return (
              <li
                key={row.id}
                ref={(el) => {
                  if (el) rowRefs.current.set(row.id, el);
                  else rowRefs.current.delete(row.id);
                }}
                className={`grid h-12 grid-cols-[5.25rem_minmax(0,1fr)_minmax(0,6.75rem)] items-center gap-x-3 pl-2 pr-3 transition-colors duration-500 md:grid-cols-[5.5rem_minmax(0,1fr)_2.75rem_minmax(0,7rem)] ${
                  hot ? 'row-hot' : ''
                }`}
              >
                <span className="flex items-center gap-2 font-mono text-[11px]">
                  <span
                    className={`size-1.5 shrink-0 rounded-full transition-colors duration-500 ${hot ? 'bg-sig-solar' : 'bg-transparent'}`}
                    aria-hidden="true"
                  />
                  <span className={hot ? 'text-ink-100' : 'text-ink-300'}>
                    <span className={hot ? 'text-ink-400' : 'text-ink-500'}>{row.model}·</span>
                    {row.point}
                  </span>
                </span>
                <span className="min-w-0 truncate text-[12px] text-ink-500" title={row.label}>
                  <span className="hidden sm:inline">{row.label}</span>
                  <span className="num-mono text-ink-400 sm:hidden">{row.raw}</span>
                </span>
                <span className="num-mono hidden text-right text-[11px] text-ink-400 md:block">{row.raw}</span>
                <span className="flex flex-col items-end leading-none">
                  <span
                    className={`num-mono text-right tabular-nums transition-[font-size,color] duration-500 ${
                      lead ? 'text-[22px] text-ink-100' : hot ? 'text-[13px] text-ink-100' : 'text-[13px] text-ink-300'
                    }`}
                  >
                    {row.reading}
                    {row.unit && <span className={`ml-1 text-ink-500 ${lead ? 'text-[11px]' : 'text-[10px]'}`}>{row.unit}</span>}
                  </span>
                  {deltas.has(row.id) && (
                    <span className="num-mono mt-1 text-[10px] text-ink-500" aria-label={`change since last read ${deltas.get(row.id)}`}>
                      {deltas.get(row.id)}
                    </span>
                  )}
                </span>
              </li>
            );
          })}
        </ol>

        <div className="order-first md:order-none md:col-span-7">
          <div ref={cardRef} className="surface-raised p-5 md:p-7 lg:p-8">
            <AnimatePresence mode="wait" initial={false}>
              <motion.article
                key={insight.id}
                initial={reduced ? false : { opacity: 0, y: 8 }}
                animate={{ opacity: 1, y: 0 }}
                exit={reduced ? undefined : { opacity: 0, y: -6 }}
                transition={transition}
                aria-label="What helios makes of it"
              >
                <h3 className="display flex items-start gap-3 text-[1.75rem] text-ink-100 md:text-[2.25rem] lg:text-[2.6rem]">
                  <span
                    className={`mt-[0.5em] size-2 shrink-0 rounded-full ${SEVERITY_DOT[insight.severity]}`}
                    aria-hidden="true"
                  />
                  <span>{insight.title}</span>
                </h3>
                <p className="mt-4 max-w-[32rem] text-[15px] leading-[1.6] text-ink-300 md:text-[16px]">{insight.body}</p>
                <div className="mt-6 flex flex-wrap items-baseline gap-x-5 gap-y-2 border-t border-line pt-4">
                  {insight.metric && <span className="num-mono text-[22px] text-ink-100">{insight.metric}</span>}
                  {insight.delta && (
                    <span className="font-mono text-[11px] uppercase tracking-widest text-ink-500">{insight.delta}</span>
                  )}
                  <span className="ml-auto font-mono text-[11px] text-ink-500">
                    {featured.quiet ? 'no insight active · reading ' : 'from '}
                    {sources.map((id, i) => {
                      const row = rows.find((r) => r.id === id);
                      return (
                        <span key={id} className="text-ink-300">
                          {i > 0 && <span className="text-ink-500"> · </span>}
                          {row?.point}
                        </span>
                      );
                    })}
                  </span>
                </div>
              </motion.article>
            </AnimatePresence>

            {others.length > 0 && (
              <ul className="mt-6 hidden space-y-2 border-t border-line pt-4 md:block" aria-label="Also noticed">
                {others.map((i) => (
                  <li key={i.id} className="flex items-center gap-2.5 text-[12px] text-ink-400">
                    <span className={`size-1.5 shrink-0 rounded-full ${SEVERITY_DOT[i.severity]}`} aria-hidden="true" />
                    <span className="min-w-0 truncate">{i.title}</span>
                    {i.metric && <span className="num-mono ml-auto shrink-0 text-[11px] text-ink-500">{i.metric}</span>}
                  </li>
                ))}
              </ul>
            )}
          </div>
          <p className="mt-3 hidden max-w-[26rem] font-mono text-[11px] leading-relaxed text-ink-500 md:block">
            Left: the SunSpec point, its raw register value and the decoded reading. Right: the insight the app derives
            from the highlighted rows. Simulated system; the logic is the shipped one.
          </p>
        </div>
      </div>
    </div>
  );
}

function PollArc({ running }: { running: boolean }) {
  const r = 5;
  const circ = 2 * Math.PI * r;
  return (
    <svg width="14" height="14" viewBox="0 0 14 14" aria-hidden="true" className="-rotate-90">
      <circle cx="7" cy="7" r={r} fill="none" stroke="var(--hairline-strong)" strokeWidth="1.5" />
      <circle
        cx="7"
        cy="7"
        r={r}
        fill="none"
        stroke="var(--signal-solar)"
        strokeWidth="1.5"
        strokeLinecap="round"
        strokeDasharray={circ}
        strokeDashoffset={running ? circ : 0}
        className={running ? 'poll-arc' : ''}
        style={{ '--circ': circ } as CSSProperties}
      />
    </svg>
  );
}
