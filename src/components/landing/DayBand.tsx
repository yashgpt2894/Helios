import { useMemo } from 'react';
import { useBallistics } from '../../hooks/useBallistics';
import { useElementSize } from '../../hooks/useElementSize';
import { LegendMark } from './Pictograms';
import { SITE, formatHour, type DayTotals, type FlowFrame } from '../../lib/placardModel';

interface Props {
  frames: FlowFrame[];
  index: number;
  totals: DayTotals;
  following: boolean;
  onSelect: (index: number) => void;
  onNow: () => void;
}

const HOUR_TICKS = [0, 3, 6, 9, 12, 15, 18, 21, 24];
const MARKS = [
  { hour: 6.2, label: 'SUNRISE 06:12' },
  { hour: 13, label: 'SOLAR NOON 13:00' },
  { hour: 19.8, label: 'SUNSET 19:48' }
];

/** Sub-paths covering the stretches where `upper` sits above `lower`. */
function regionPath(frames: FlowFrame[], upper: (f: FlowFrame) => number, lower: (f: FlowFrame) => number) {
  const spans: FlowFrame[][] = [];
  let run: FlowFrame[] = [];
  for (const f of frames) {
    if (upper(f) > lower(f)) {
      run.push(f);
    } else if (run.length) {
      spans.push(run);
      run = [];
    }
  }
  if (run.length) spans.push(run);
  return spans.map((span) => span.map((f) => f));
}

/**
 * One day on the reference array. The geometry is drawn in real CSS pixels, so the hatches stay square
 * and the printed marks keep their weight at any width.
 */
export function DayBand({ frames, index, totals, following, onSelect, onNow }: Props) {
  const { ref, size } = useElementSize<HTMLDivElement>({ width: 1000, height: 240 });
  const height = size.height || 240;
  const width = size.width || 1000;

  const frame = frames[Math.min(index, frames.length - 1)];
  const liveKw = useBallistics(frame.acPowerW / 1000);
  const position = (frame.hour / 24) * 100;

  const chart = useMemo(() => {
    const padTop = 16;
    const padBottom = 14;
    const plot = height - padTop - padBottom;
    const scale = SITE.ratedDcW / 1000;
    const px = (hour: number) => (hour / 24) * width;
    const py = (kw: number) => padTop + plot - (Math.min(kw, scale) / scale) * plot;

    const line = (value: (f: FlowFrame) => number) =>
      frames.map((f, i) => `${i === 0 ? 'M' : 'L'}${px(f.hour).toFixed(1)},${py(value(f)).toFixed(1)}`).join(' ');

    const production = `${line((f) => f.acPowerW / 1000)} L${width},${padTop + plot} L0,${padTop + plot} Z`;

    const spanArea = (spans: FlowFrame[][], top: (f: FlowFrame) => number, bottom: (f: FlowFrame) => number) =>
      spans
        .map((span) => {
          const forward = span
            .map((f, i) => `${i === 0 ? 'M' : 'L'}${px(f.hour).toFixed(1)},${py(top(f)).toFixed(1)}`)
            .join(' ');
          const back = [...span]
            .reverse()
            .map((f) => `L${px(f.hour).toFixed(1)},${py(bottom(f)).toFixed(1)}`)
            .join(' ');
          return `${forward} ${back} Z`;
        })
        .join(' ');

    return {
      px,
      py,
      production,
      load: line((f) => f.homeLoadW / 1000),
      surplus: spanArea(
        regionPath(frames, (f) => f.acPowerW / 1000, (f) => f.homeLoadW / 1000),
        (f) => f.acPowerW / 1000,
        (f) => f.homeLoadW / 1000
      ),
      deficit: spanArea(
        regionPath(frames, (f) => f.homeLoadW / 1000, (f) => f.acPowerW / 1000),
        (f) => f.homeLoadW / 1000,
        (f) => f.acPowerW / 1000
      )
    };
  }, [frames, width, height]);

  return (
    <div>
      <div className="pc-selector">
        <span className="pc-selector__label" id="pc-hour-label">
          Reading selector
        </span>
        <input
          className="pc-range"
          type="range"
          min={0}
          max={frames.length - 1}
          step={1}
          value={index}
          onChange={(event) => onSelect(Number(event.target.value))}
          aria-labelledby="pc-hour-label"
          aria-valuetext={formatHour(frame.hour)}
        />
        <span className="pc-range__value pc-mono">{formatHour(frame.hour)}</span>
        <button type="button" className="pc-btn pc-btn--ghost" onClick={onNow} disabled={following}>
          {following ? 'Following now' : 'Return to now'}
        </button>
      </div>

      <div className="pc-band" ref={ref}>
        <svg viewBox={`0 0 ${width} ${height}`} width="100%" height={height} role="img" aria-label={`Production and house load across the day, read at ${formatHour(frame.hour)}`}>
          <defs>
            <pattern id="pc-hatch-surplus" width="8" height="8" patternUnits="userSpaceOnUse" patternTransform="rotate(45)">
              <line x1="0" y1="0" x2="0" y2="8" stroke="currentColor" strokeWidth="3.4" />
            </pattern>
            <pattern id="pc-hatch-deficit" width="8" height="8" patternUnits="userSpaceOnUse" patternTransform="rotate(-45)">
              <line x1="0" y1="0" x2="0" y2="8" stroke="currentColor" strokeWidth="2.2" />
            </pattern>
          </defs>

          {/* printed hour rules, drawn like the grid on a chart recorder */}
          {HOUR_TICKS.map((hour) => (
            <line
              key={hour}
              x1={chart.px(hour)}
              y1={0}
              x2={chart.px(hour)}
              y2={height}
              stroke="currentColor"
              strokeOpacity="0.18"
              strokeWidth="1"
            />
          ))}
          {MARKS.map((mark) => (
            <line
              key={mark.label}
              x1={chart.px(mark.hour)}
              y1={0}
              x2={chart.px(mark.hour)}
              y2={height}
              stroke="currentColor"
              strokeOpacity="0.42"
              strokeWidth="1.5"
              strokeDasharray="5 4"
            />
          ))}

          <path d={chart.production} fill="currentColor" fillOpacity="0.92" />
          <path d={chart.surplus} fill="url(#pc-hatch-surplus)" stroke="none" />
          <path d={chart.deficit} fill="url(#pc-hatch-deficit)" stroke="none" />
          <path d={chart.load} fill="none" stroke="currentColor" strokeWidth="2.4" strokeDasharray="0" />
        </svg>

        {MARKS.map((mark) => (
          <span
            key={mark.label}
            className="pc-band__label"
            style={{ top: 6, left: `${(mark.hour / 24) * 100}%`, transform: 'translateX(-50%)' }}
          >
            {mark.label}
          </span>
        ))}

        {HOUR_TICKS.map((hour) => (
          <span
            key={hour}
            className={`pc-band__label pc-band__label--low${hour === 0 ? ' pc-band__label--first' : ''}${
              hour === 24 ? ' pc-band__label--last' : ''
            }${hour % 6 === 0 ? '' : ' pc-band__label--minor'}`}
            style={{ left: `${(hour / 24) * 100}%` }}
          >
            {String(hour).padStart(2, '0')}
          </span>
        ))}

        <span className="pc-band__mark" style={{ left: `${position}%` }} aria-hidden="true" />
        <span
          className="pc-band__readout"
          style={{ left: `${Math.min(88, Math.max(12, position))}%` }}
        >
          {formatHour(frame.hour)} · {liveKw.toFixed(2)} kW
        </span>
      </div>

      <div className="pc-facts">
        <div>
          <LegendMark kind="solid" className="pc-legend__mark" />
          <span className="pc-facts__k">Solar produced</span>
        </div>
        <div>
          <LegendMark kind="line" />
          <span className="pc-facts__k">House load</span>
        </div>
        <div>
          <LegendMark kind="surplus" />
          <span className="pc-facts__k">Surplus, to battery and grid</span>
        </div>
        <div>
          <LegendMark kind="deficit" />
          <span className="pc-facts__k">From the grid</span>
        </div>
      </div>

      <dl className="pc-totals">
        <div>
          <dt>Generated</dt>
          <dd>
            {totals.generatedKwh.toFixed(1)} <span>kWh</span>
          </dd>
        </div>
        <div>
          <dt>Used in the house</dt>
          <dd>
            {totals.consumedKwh.toFixed(1)} <span>kWh</span>
          </dd>
        </div>
        <div>
          <dt>Bought from grid</dt>
          <dd>
            {totals.importedKwh.toFixed(1)} <span>kWh</span>
          </dd>
        </div>
        <div>
          <dt>Exported to grid</dt>
          <dd>
            {totals.exportedKwh.toFixed(1)} <span>kWh</span>
          </dd>
        </div>
      </dl>
    </div>
  );
}
