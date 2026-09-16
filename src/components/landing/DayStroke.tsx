import { useEffect, useMemo, useRef, useState } from 'react';
import { motion, useReducedMotion, useScroll, useTransform } from 'framer-motion';
import type { Insight } from '../../types';
import { generateInsights } from '../../services/aiInsights';
import { DEMO_DAY_KWH, demoSampleAt, demoTelemetryAt, featuredInsight, formatDemoClock } from '../../lib/demoDay';

interface Moment {
  hour: number;
  insightId: string;
}

const MOMENTS: Moment[] = [
  { hour: 6.5, insightId: 'morning-strategy' },
  { hour: 13.0, insightId: 'peak-production' },
  { hour: 15.7, insightId: 'cloud-coverage' },
  { hour: 18.2, insightId: 'string-imbalance' },
  { hour: 22.0, insightId: 'battery-evening' }
];

interface ResolvedMoment {
  hour: number;
  insight: Insight;
  acPowerW: number;
}

function batteryEvening(hour: number): Insight {
  const t = demoTelemetryAt(hour);
  const remainingKwh = (t.batterySoc / 100) * t.batteryCapacityKwh;
  const drawKw = Math.abs(t.batteryPowerW) / 1000;
  const minutes = Math.round((remainingKwh / Math.max(0.1, drawKw)) * 60);
  return {
    id: 'battery-evening',
    category: 'battery',
    severity: 'neutral',
    title: 'Battery is powering the house',
    body: `State of charge ${Math.round(t.batterySoc)}% — ${remainingKwh.toFixed(1)} kWh left, about ${Math.floor(minutes / 60)}h ${
      minutes % 60
    }m at the current ${drawKw.toFixed(2)} kW draw. Nothing has been bought from the grid since sunrise.`,
    metric: `${Math.round(t.batterySoc)}%`,
    delta: 'powering home'
  };
}

function resolveMoments(): ResolvedMoment[] {
  return MOMENTS.map((m) => {
    const t = demoTelemetryAt(m.hour);
    const insights = generateInsights(t);
    const insight =
      m.insightId === 'battery-evening'
        ? batteryEvening(m.hour)
        : insights.find((i) => i.id === m.insightId) ?? featuredInsight(insights) ?? batteryEvening(m.hour);
    return { hour: m.hour, insight, acPowerW: t.acPowerW };
  });
}

const PAD_TOP = 18;
const AXIS_H = 26;
const LEADER_H = 64;

export function DayStroke() {
  const reduced = useReducedMotion() === true;
  const sectionRef = useRef<HTMLDivElement>(null);
  const wrapRef = useRef<HTMLDivElement>(null);
  const [width, setWidth] = useState(1200);
  const moments = useMemo(resolveMoments, []);

  useEffect(() => {
    const el = wrapRef.current;
    if (!el) return;
    const ro = new ResizeObserver((entries) => {
      const w = entries[0]?.contentRect.width;
      if (w) setWidth(w);
    });
    ro.observe(el);
    setWidth(el.getBoundingClientRect().width);
    return () => ro.disconnect();
  }, []);

  const wide = width >= 1024;
  const curveH = width < 640 ? 150 : width < 1024 ? 210 : 250;
  const totalH = curveH + AXIS_H + (wide ? LEADER_H : 0);

  const { scrollYProgress } = useScroll({ target: sectionRef, offset: ['start 80%', 'start 20%'] });
  const drawn = useTransform(scrollYProgress, [0, 1], [0, 1]);

  const geometry = useMemo(() => {
    const maxW = 9600 * 0.964;
    const x = (h: number) => (h / 24) * width;
    const y = (w: number) => PAD_TOP + (1 - w / maxW) * (curveH - PAD_TOP - 6);
    let solar = '';
    let home = '';
    for (let h = 0; h <= 24.0001; h += 0.1) {
      const s = demoSampleAt(Math.min(h, 23.999));
      const cmd = h === 0 ? 'M' : 'L';
      solar += `${cmd} ${x(h).toFixed(1)} ${y(s.acPowerW).toFixed(1)} `;
      home += `${cmd} ${x(h).toFixed(1)} ${y(s.homeLoadW).toFixed(1)} `;
    }
    const area = `${solar} L ${x(24).toFixed(1)} ${y(0).toFixed(1)} L 0 ${y(0).toFixed(1)} Z`;
    const markers = moments.map((m, i) => {
      const mx = x(m.hour);
      const my = y(m.acPowerW);
      const colX = ((i + 0.5) / moments.length) * width;
      const leader = `M ${mx} ${my + 6} L ${mx} ${curveH + AXIS_H - 4} L ${colX} ${totalH - 6}`;
      return { mx, my, colX, leader };
    });
    return { solar, home, area, markers, baseline: y(0) };
  }, [width, curveH, totalH, moments]);

  return (
    <div ref={sectionRef}>
      <div ref={wrapRef} className="relative w-full">
        <svg
          width={width}
          height={totalH}
          viewBox={`0 0 ${width} ${totalH}`}
          className="block h-auto w-full overflow-visible"
          role="img"
          aria-label={`Solar production and home consumption over the simulated day, ${DEMO_DAY_KWH.toFixed(
            0
          )} kWh produced, with five moments helios° commented on.`}
        >
          <path d={geometry.area} fill="var(--field-wash)" />
          <path
            d={geometry.home}
            fill="none"
            stroke="var(--field-ink-3)"
            strokeWidth="1"
            strokeDasharray="2 4"
            strokeLinejoin="round"
          />
          <motion.path
            d={geometry.solar}
            fill="none"
            stroke="var(--on-field)"
            strokeWidth="2"
            strokeLinejoin="round"
            strokeLinecap="round"
            style={reduced ? undefined : { pathLength: drawn }}
          />
          <line x1="0" x2={width} y1={geometry.baseline} y2={geometry.baseline} stroke="var(--field-line)" strokeWidth="1" />
          {[0, 6, 12, 18, 24].map((h) => {
            const tx = (h / 24) * width;
            const anchor = h === 0 ? 'start' : h === 24 ? 'end' : 'middle';
            return (
              <g key={h}>
                <line x1={tx} x2={tx} y1={geometry.baseline} y2={geometry.baseline + 5} stroke="var(--field-line)" />
                <text
                  x={tx}
                  y={geometry.baseline + 18}
                  textAnchor={anchor}
                  fill="var(--field-ink-2)"
                  style={{ fontFamily: '"JetBrains Mono", ui-monospace, monospace', fontSize: 10, letterSpacing: '0.08em' }}
                >
                  {String(h % 24).padStart(2, '0')}:00
                </text>
              </g>
            );
          })}
          {geometry.markers.map((m, i) => (
            <g key={moments[i].hour}>
              {wide && (
                <path d={m.leader} fill="none" stroke="var(--on-field)" strokeWidth="1" strokeDasharray="2 4" opacity="0.7" />
              )}
              <circle cx={m.mx} cy={m.my} r="7" fill="var(--field)" />
              <circle cx={m.mx} cy={m.my} r="4" fill="var(--on-field)" />
              {!wide && (
                <text
                  x={m.mx}
                  y={m.my - 12}
                  textAnchor="middle"
                  fill="var(--on-field)"
                  style={{ fontFamily: '"JetBrains Mono", ui-monospace, monospace', fontSize: 10 }}
                >
                  {formatDemoClock(moments[i].hour)}
                </text>
              )}
            </g>
          ))}
        </svg>
        <div className="pointer-events-none absolute right-0 top-0 hidden items-center gap-4 font-mono text-[10px] uppercase tracking-widest text-[var(--field-ink-2)] sm:flex">
          <span className="flex items-center gap-2">
            <span className="block h-0.5 w-5 bg-[var(--on-field)]" aria-hidden="true" />
            Solar
          </span>
          <span className="flex items-center gap-2">
            <span className="block h-px w-5 border-t border-dashed border-[var(--field-ink-3)]" aria-hidden="true" />
            Home
          </span>
        </div>
      </div>

      <ol className="mt-4 grid grid-cols-1 gap-x-6 gap-y-8 sm:grid-cols-2 lg:mt-0 lg:grid-cols-5" aria-label="Moments in the simulated day">
        {moments.map((m) => (
          <li key={m.hour} className="border-t border-[var(--field-line)] pt-4 lg:border-t-0 lg:pt-0">
            <h3 className="text-[15px] font-semibold leading-snug">
              <span className="num-mono mr-2 text-[12px] font-normal text-[var(--field-ink-2)]">{formatDemoClock(m.hour)}</span>{' '}
              {m.insight.title}
            </h3>
            <p className="mt-2 text-[13px] leading-[1.55] text-[var(--field-ink-2)]">{m.insight.body}</p>
            {m.insight.metric && (
              <p className="num-mono mt-3 text-[18px]">
                {m.insight.metric}
                {m.insight.delta && (
                  <span className="ml-2 font-mono text-[10px] uppercase tracking-widest text-[var(--field-ink-2)]">{m.insight.delta}</span>
                )}
              </p>
            )}
          </li>
        ))}
      </ol>
    </div>
  );
}
