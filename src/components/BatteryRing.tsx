import { motion } from 'framer-motion';
import type { SolarTelemetry } from '../types';

interface BatteryRingProps {
  t: SolarTelemetry;
  size?: number;
}

export function BatteryRing({ t, size = 240 }: BatteryRingProps) {
  const radius = size / 2 - 16;
  const circumference = 2 * Math.PI * radius;
  const dash = (t.batterySoc / 100) * circumference;
  const charging = t.batteryPowerW > 30;
  const discharging = t.batteryPowerW < -30;
  const status = charging ? 'Charging' : discharging ? 'Powering home' : 'Idle';
  const accent = charging ? '#7fb069' : discharging ? '#f0c674' : '#dcd6c8';

  const remainingKwh = (t.batterySoc / 100) * t.batteryCapacityKwh;
  const minutesEstimate =
    discharging && Math.abs(t.batteryPowerW) > 50
      ? Math.round((remainingKwh / (Math.abs(t.batteryPowerW) / 1000)) * 60)
      : null;

  return (
    <div className="relative" style={{ width: size, height: size }}>
      <svg width={size} height={size} className="-rotate-90">
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          fill="none"
          stroke="rgba(239,236,229,0.06)"
          strokeWidth="2"
        />
        <motion.circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          fill="none"
          stroke={accent}
          strokeWidth="3"
          strokeLinecap="round"
          strokeDasharray={`${dash} ${circumference}`}
          initial={false}
          animate={{ strokeDasharray: `${dash} ${circumference}` }}
          transition={{ duration: 0.8, ease: [0.16, 1, 0.3, 1] }}
        />
      </svg>
      <div className="absolute inset-0 flex flex-col items-center justify-center text-center">
        <span className="label-cap mb-1">State of charge</span>
        <span className="num-display text-bone-100 text-6xl leading-none">{t.batterySoc.toFixed(0)}</span>
        <span className="label-cap mt-1">{status}</span>
        <div className="mt-3 px-3 py-1 rounded-full border border-hairline">
          <span className="num-mono text-bone-300 text-[11px]">
            {remainingKwh.toFixed(1)} / {t.batteryCapacityKwh.toFixed(1)} kWh
          </span>
        </div>
        {minutesEstimate !== null && (
          <span className="num-mono text-bone-500 text-[10px] mt-1.5">
            ~{Math.floor(minutesEstimate / 60)}h {minutesEstimate % 60}m at current draw
          </span>
        )}
      </div>
    </div>
  );
}
