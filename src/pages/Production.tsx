import { Thermometer, Activity, Zap } from 'lucide-react';
import { useStore } from '../store/useStore';
import { ProductionChart } from '../components/ProductionChart';
import { WeekChart } from '../components/WeekChart';
import { MetricTile } from '../components/MetricTile';
import { SectionHeader } from '../components/SectionHeader';
import { LiveNumber } from '../components/LiveNumber';
import { buildWeekSeries } from '../services/sunspec';
import { useMemo } from 'react';

export function Production() {
  const t = useStore((s) => s.telemetry);
  const series = useStore((s) => s.todaySeries);
  const week = useMemo(() => buildWeekSeries(), []);

  return (
    <div className="px-5 pb-32 pt-2 space-y-5">
      <header className="px-1">
        <span className="label-cap">Solar array</span>
        <h1 className="text-bone-100 text-[28px] font-medium tracking-tight mt-1">Production</h1>
        <p className="text-bone-500 text-[13px] mt-1">
          {t.panels.length} strings · 24 panels · 9.6 kW DC rating
        </p>
      </header>

      <section className="surface-raised p-5">
        <div className="flex items-baseline justify-between mb-1">
          <span className="label-cap">Generating now</span>
          <span className="label-cap text-signal-solar">{t.irradianceWm2.toFixed(0)} W/m²</span>
        </div>
        <div className="flex items-baseline gap-2 mb-4">
          <LiveNumber
            value={t.acPowerW / 1000}
            digits={2}
            className="num-display text-bone-100 text-5xl leading-none"
          />
          <span className="text-bone-400 text-sm font-mono uppercase tracking-widest">kW AC</span>
        </div>
        <ProductionChart data={series} height={180} />
      </section>

      <section>
        <SectionHeader eyebrow="strings" title="Per-string output" description="Imbalance > 15% triggers an inspection alert." />
        <div className="space-y-2">
          {t.panels.map((p) => {
            const utilization = p.powerW / p.ratedW;
            return (
              <div key={p.id} className="surface p-4">
                <div className="flex items-center justify-between mb-2">
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="size-6 rounded-full bg-carbon-700 border border-hairline flex items-center justify-center font-mono text-[10px] text-bone-300">
                        {p.id}
                      </span>
                      <span className="text-bone-100 text-[13px] font-medium">{p.label}</span>
                    </div>
                    <span className="text-bone-500 text-[11px] font-mono mt-1 block">
                      {p.panels} panels · {p.ratedW.toLocaleString()} W rated
                    </span>
                  </div>
                  <div className="text-right">
                    <div className="num-display text-bone-100 text-xl leading-none">
                      {(p.powerW / 1000).toFixed(2)}
                      <span className="text-bone-500 text-[11px] font-mono ml-1">kW</span>
                    </div>
                    <div className="num-mono text-bone-500 text-[10px] mt-1">
                      {p.voltageV.toFixed(0)} V · {p.currentA.toFixed(1)} A
                    </div>
                  </div>
                </div>
                <div className="h-1 rounded-full bg-carbon-800 overflow-hidden">
                  <div
                    className="h-full rounded-full bg-gradient-to-r from-signal-solar/70 to-signal-solar transition-all duration-700"
                    style={{ width: `${Math.min(100, utilization * 100)}%` }}
                  />
                </div>
                <div className="flex items-center justify-between mt-2 text-[10px] font-mono uppercase tracking-widest">
                  <span className="text-bone-600">utilization</span>
                  <span className="text-bone-400">{(utilization * 100).toFixed(0)}%</span>
                </div>
              </div>
            );
          })}
        </div>
      </section>

      <section>
        <SectionHeader eyebrow="last 7 days" title="Daily production vs use" />
        <div className="surface p-4">
          <WeekChart data={week} />
        </div>
      </section>

      <section>
        <SectionHeader eyebrow="inverter" title="System telemetry" />
        <div className="grid grid-cols-2 gap-3">
          <MetricTile
            label="DC voltage"
            value={t.dcVoltageV.toFixed(0)}
            unit="V"
            subtle={`${t.dcCurrentA.toFixed(1)} A`}
            icon={<Activity size={14} strokeWidth={1.6} />}
          />
          <MetricTile
            label="AC frequency"
            value={t.acFrequencyHz.toFixed(2)}
            unit="Hz"
            subtle="60 Hz nominal"
            icon={<Zap size={14} strokeWidth={1.6} />}
          />
          <MetricTile
            label="Heatsink"
            value={t.heatsinkTempC.toFixed(1)}
            unit="°C"
            delta={t.heatsinkTempC > 50 ? 'warm' : 'normal'}
            deltaTone={t.heatsinkTempC > 50 ? 'negative' : 'positive'}
            icon={<Thermometer size={14} strokeWidth={1.6} />}
          />
          <MetricTile
            label="Cabinet"
            value={t.cabinetTempC.toFixed(1)}
            unit="°C"
            subtle={`ambient ${t.ambientTempC.toFixed(0)}°C`}
            icon={<Thermometer size={14} strokeWidth={1.6} />}
          />
        </div>
      </section>
    </div>
  );
}
