import { useState } from 'react';
import { Battery } from 'lucide-react';
import type { Brand, SolarTelemetry } from '../../types';
import { HeliosMark } from '../HeliosMark';
import { EnergyFlow } from '../EnergyFlow';
import { StatusPill } from '../StatusPill';
import { LiveNumber } from '../LiveNumber';
import { BatteryRing } from '../BatteryRing';
import { formatDemoClock } from '../../lib/demoDay';

const STRATEGIES = [
  { id: 'self', name: 'Self-consumption', desc: 'Maximize using your own solar before drawing from grid.' },
  { id: 'tou', name: 'Time-of-use', desc: 'Charge from grid in off-peak; discharge during peak rates.' },
  { id: 'backup', name: 'Backup-only', desc: 'Hold ≥80% reserve at all times for outage protection.' }
] as const;

type StrategyId = (typeof STRATEGIES)[number]['id'];

export function PhoneDashboard({ t, hour, brand }: { t: SolarTelemetry; hour: number; brand: Brand }) {
  const selfUse = t.acPowerW > 0 ? Math.max(0, Math.min(100, (1 - t.gridExportW / Math.max(1, t.acPowerW)) * 100)) : 0;
  const co2KgToday = t.energyTodayKwh * 0.42;

  return (
    <div className="phone-frame mx-auto w-full max-w-[392px] overflow-hidden rounded-[40px] bg-ground-1">
      <div className="flex items-center justify-between px-5 pb-3 pt-5">
        <div className="flex items-center gap-2.5">
          <HeliosMark size={30} brand={brand} />
          <div className="flex flex-col leading-tight">
            <span className="text-[14px] font-medium text-ink-100">{brand.name}</span>
            <span className="flex items-center gap-1.5 font-mono text-[10px] uppercase tracking-widest text-ink-500">
              <span className="size-1 rounded-full bg-sig-solar" aria-hidden="true" />
              simulated
            </span>
          </div>
        </div>
        <div className="rounded-full border border-line px-2.5 py-1.5">
          <span className="num-mono text-[11px] text-ink-300">{formatDemoClock(hour)}</span>
        </div>
      </div>

      <div className="px-2.5 pb-3 sm:px-4 sm:pb-4">
        <div className="surface-raised grid-bg overflow-hidden p-5">
          <div className="mb-4 flex items-start justify-between gap-3">
            <div className="min-w-0">
              <span className="label-cap">Live · {formatDemoClock(hour)}</span>
              <div className="mt-2 flex items-baseline gap-2">
                <LiveNumber value={t.acPowerW / 1000} digits={2} className="num-display text-6xl leading-none text-ink-100" />
                <span className="font-mono text-sm uppercase tracking-widest text-ink-400">kW</span>
              </div>
              <p className="mt-1.5 font-mono text-[12px] text-ink-500">
                from <span className="text-ink-300">{t.irradianceWm2.toFixed(0)} W/m²</span> · cabinet{' '}
                <span className="text-ink-300">{t.cabinetTempC.toFixed(0)}°C</span>
              </p>
            </div>
            <StatusPill status={t.status} />
          </div>

          <EnergyFlow t={t} />

          <div className="mt-5 grid grid-cols-3 gap-3 border-t border-line pt-4">
            <div className="text-center">
              <div className="label-cap mb-1">Today</div>
              <div className="num-display text-2xl text-ink-100">
                <LiveNumber value={t.energyTodayKwh} digits={1} />
              </div>
              <div className="font-mono text-[10px] uppercase tracking-widest text-ink-500">kWh</div>
            </div>
            <div className="border-x border-line text-center">
              <div className="label-cap mb-1">Self-use</div>
              <div className="num-display text-2xl text-ink-100">
                <LiveNumber value={selfUse} digits={0} />
              </div>
              <div className="font-mono text-[10px] uppercase tracking-widest text-ink-500">%</div>
            </div>
            <div className="text-center">
              <div className="label-cap mb-1">CO₂ saved</div>
              <div className="num-display text-2xl text-ink-100">
                <LiveNumber value={co2KgToday} digits={1} />
              </div>
              <div className="font-mono text-[10px] uppercase tracking-widest text-ink-500">kg today</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export function BatteryModule({ t }: { t: SolarTelemetry }) {
  const [strategy, setStrategy] = useState<StrategyId>('self');
  const hours = Math.floor(((t.batterySoc / 100) * t.batteryCapacityKwh) / Math.max(0.4, t.homeLoadW / 1000));

  return (
    <div className="surface p-5 md:p-6">
      <div className="flex flex-col gap-6 sm:flex-row sm:items-center">
        <div className="shrink-0 self-center">
          <BatteryRing t={t} size={196} />
        </div>
        <div className="min-w-0 flex-1">
          <div className="flex items-baseline justify-between gap-3 border-b border-line pb-3">
            <span className="text-[14px] font-medium text-ink-100">Backup readiness</span>
            <span className="num-mono text-[13px] text-ink-300">
              {hours} h <span className="text-ink-500">of essentials</span>
            </span>
          </div>
          <div role="radiogroup" aria-label="Battery strategy" className="mt-3 space-y-1.5">
            {STRATEGIES.map((s) => {
              const active = s.id === strategy;
              return (
                <button
                  key={s.id}
                  type="button"
                  role="radio"
                  aria-checked={active}
                  onClick={() => setStrategy(s.id)}
                  className={`flex w-full items-start gap-3 rounded-xl border px-3 py-2.5 text-left transition-colors ${
                    active ? 'border-line-strong bg-ground-3' : 'border-line hover:border-line-strong'
                  }`}
                >
                  <span
                    className={`mt-1 flex size-3.5 shrink-0 items-center justify-center rounded-full border-2 ${
                      active ? 'border-sig-battery' : 'border-ink-500'
                    }`}
                    aria-hidden="true"
                  >
                    {active && <span className="size-1.5 rounded-full bg-sig-battery" />}
                  </span>
                  <span className="min-w-0 flex-1">
                    <span className="flex items-center gap-2">
                      <span className="text-[13px] font-medium text-ink-100">{s.name}</span>
                      {active && (
                        <span className="font-mono text-[10px] uppercase tracking-widest text-sig-battery">active</span>
                      )}
                    </span>
                    <span className="mt-0.5 block text-[12px] leading-relaxed text-ink-500">{s.desc}</span>
                  </span>
                  <Battery size={15} strokeWidth={1.4} className="mt-1 shrink-0 text-ink-500" aria-hidden="true" />
                </button>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
}
