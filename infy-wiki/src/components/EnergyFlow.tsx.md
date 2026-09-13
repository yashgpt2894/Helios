# EnergyFlow

```tsx
import { Sun, Home, BatteryCharging, Cable } from 'lucide-react';
import type { SolarTelemetry } from '../types';
import { formatW } from '../lib/format';

interface EnergyFlowProps {
  t: SolarTelemetry;
}

interface NodeProps {
  x: number;
  y: number;
  icon: React.ReactNode;
  label: string;
  value: string;
  active: boolean;
  size?: number;
  accent?: string;
}

const W = 320;
const H = 320;
const CX = W / 2;
const CY = H / 2 + 8;
const HUB_R = 44;

function Node({ x, y, icon, label, value, active, size = 56, accent }: NodeProps) {
  return (
    <g transform={`translate(${x} ${y})`}>
      <circle
        r={size / 2 + 8}
        fill="none"
        stroke="var(--hairline)"
        strokeWidth="1"
      />
      <circle
        r={size / 2}
        fill="var(--node-fill)"
        stroke={active ? accent : 'var(--node-stroke)'}
        strokeWidth={active ? 1.5 : 1}
      />
      <foreignObject x={-size / 2} y={-size / 2} width={size} height={size}>
        <div className="size-full flex items-center justify-center text-bone-200">{icon}</div>
      </foreignObject>
      <text
        textAnchor="middle"
        y={size / 2 + 24}
        fill="var(--text-500)"
        className="font-mono uppercase"
        style={{ fontSize: 9, letterSpacing: '0.22em' }}
      >
        {label}
      </text>
      <text
        textAnchor="middle"
        y={size / 2 + 40}
        fill="var(--text-100)"
        className="font-mono"
        style={{ fontSize: 11, fontWeight: 500 }}
      >
        {value}
      </text>
    </g>
  );
}

interface FlowLineProps {
  from: [number, number];
  to: [number, number];
  active: boolean;
  reverse?: boolean;
  color: string;
}

function FlowLine({ from, to, active, reverse, color }: FlowLineProps) {
  const [x1, y1] = from;
  const [x2, y2] = to;
  const start = reverse ? [x2, y2] : [x1, y1];
  const end = reverse ? [x1, y1] : [x2, y2];
  const dx = end[0] - start[0];
  const dy = end[1] - start[1];
  const length = Math.sqrt(dx * dx + dy * dy);
  const ux = dx / length;
  const uy = dy / length;
  const padStart = HUB_R + 4;
  const padEnd = 38;
  const sx = start[0] + ux * padStart;
  const sy = start[1] + uy * padStart;
  const ex = end[0] - ux * padEnd;
  const ey = end[1] - uy * padEnd;

  return (
    <g>
      <line
        x1={sx}
        y1={sy}
        x2={ex}
        y2={ey}
        stroke="var(--line-quiet)"
        strokeWidth="1"
        strokeLinecap="round"
      />
      {active && (
        <>
          <line
            x1={sx}
            y1={sy}
            x2={ex}
            y2={ey}
            stroke={color}
            strokeWidth="1.5"
            strokeLinecap="round"
            strokeDasharray="2 6"
            className="animate-flow"
            opacity="0.7"
          />
          <circle
            r="2.5"
            fill={color}
            className="flow-dot"
            style={{
              offsetPath: `path('M ${sx} ${sy} L ${ex} ${ey}')`,
              filter: `drop-shadow(0 0 4px ${color})`
            }}
          />
        </>
      )}
    </g>
  );
}

export function EnergyFlow({ t }: EnergyFlowProps) {
  const solarActive = t.acPowerW > 30;
  const homeActive = t.homeLoadW > 30;
  const batteryCharging = t.batteryPowerW > 30;
  const batteryDischarging = t.batteryPowerW < -30;
  const gridImporting = t.gridImportW > 30;
  const gridExporting = t.gridExportW > 30;

  const solarPos: [number, number] = [CX, CY - 116];
  const batteryPos: [number, number] = [CX - 116, CY + 86];
  const gridPos: [number, number] = [CX + 116, CY + 86];
  const homePos: [number, number] = [CX, CY];

  return (
    <div className="relative">
      <svg viewBox={`0 0 ${W} ${H + 40}`} className="w-full max-w-md mx-auto">
        <defs>
          <radialGradient id="hubGlow" cx="50%" cy="50%" r="50%">
            <stop offset="0%" stopColor="rgba(244,198,116,0.15)" />
            <stop offset="100%" stopColor="rgba(244,198,116,0)" />
          </radialGradient>
        </defs>

        <FlowLine
          from={solarPos}
          to={homePos}
          active={solarActive}
          color="#f0c674"
        />
        <FlowLine
          from={batteryPos}
          to={homePos}
          active={batteryCharging || batteryDischarging}
          reverse={batteryCharging}
          color="#c5a572"
        />
        <FlowLine
          from={gridPos}
          to={homePos}
          active={gridImporting || gridExporting}
          reverse={gridExporting}
          color="#5d8aa8"
        />

        <circle cx={CX} cy={CY} r={HUB_R + 18} fill="url(#hubGlow)" />

        <Node
          x={solarPos[0]}
          y={solarPos[1]}
          icon={<Sun size={22} strokeWidth={1.4} />}
          label="Solar"
          value={formatW(t.acPowerW)}
          active={solarActive}
          accent="#f0c674"
        />
        <Node
          x={batteryPos[0]}
          y={batteryPos[1]}
          icon={<BatteryCharging size={22} strokeWidth={1.4} />}
          label="Battery"
          value={formatW(Math.abs(t.batteryPowerW))}
          active={batteryCharging || batteryDischarging}
          accent="#c5a572"
        />
        <Node
          x={gridPos[0]}
          y={gridPos[1]}
          icon={<Cable size={22} strokeWidth={1.4} />}
          label="Grid"
          value={formatW(gridImporting ? t.gridImportW : t.gridExportW)}
          active={gridImporting || gridExporting}
          accent="#5d8aa8"
        />

        <g transform={`translate(${CX} ${CY})`}>
          <circle r={HUB_R} fill="var(--hub-fill)" stroke="var(--hub-stroke)" strokeWidth="1" />
          <circle r={HUB_R - 6} fill="none" stroke="var(--hairline)" strokeWidth="0.6" />
          <foreignObject x={-HUB_R} y={-HUB_R} width={HUB_R * 2} height={HUB_R * 2}>
            <div className="size-full flex flex-col items-center justify-center text-bone-200">
              <Home size={20} strokeWidth={1.4} className="mb-1" />
              <div className="num-mono text-[11px] text-bone-100 font-medium">{formatW(t.homeLoadW)}</div>
              <div className={`label-cap text-[8px] mt-0.5 ${homeActive ? 'text-signal-flow' : 'text-bone-500'}`}>
                Home
              </div>
            </div>
          </foreignObject>
        </g>
      </svg>
    </div>
  );
}

```