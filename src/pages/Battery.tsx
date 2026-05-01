import { useStore } from '../store/useStore';
import { BatteryRing } from '../components/BatteryRing';
import { MetricTile } from '../components/MetricTile';
import { SectionHeader } from '../components/SectionHeader';
import { Battery, Clock, Activity, ShieldCheck, Thermometer } from 'lucide-react';
import { formatW } from '../lib/format';

export function BatteryPage() {
  const t = useStore((s) => s.telemetry);
  const charging = t.batteryPowerW > 30;
  const discharging = t.batteryPowerW < -30;

  return (
    <div className="px-5 pb-32 pt-2 space-y-5">
      <header className="px-1">
        <span className="label-cap">Energy storage</span>
        <h1 className="text-bone-100 text-[28px] font-medium tracking-tight mt-1">Battery</h1>
        <p className="text-bone-500 text-[13px] mt-1">
          {t.batteryCapacityKwh.toFixed(1)} kWh · {t.batteryCycles} cycles · {t.batteryHealthPct.toFixed(1)}% health
        </p>
      </header>

      <section className="surface-raised p-6 flex flex-col items-center">
        <BatteryRing t={t} size={260} />
        <div className="mt-4 grid grid-cols-3 gap-2 w-full pt-4 border-t border-hairline">
          <div className="text-center">
            <div className="label-cap mb-1">Power</div>
            <div className="num-mono text-bone-100 text-[15px]">
              {charging ? '+' : discharging ? '−' : ''}
              {formatW(Math.abs(t.batteryPowerW))}
            </div>
          </div>
          <div className="text-center border-x border-hairline">
            <div className="label-cap mb-1">Temp</div>
            <div className="num-mono text-bone-100 text-[15px]">{t.batteryTempC.toFixed(1)}°C</div>
          </div>
          <div className="text-center">
            <div className="label-cap mb-1">Cycles</div>
            <div className="num-mono text-bone-100 text-[15px]">{t.batteryCycles}</div>
          </div>
        </div>
      </section>

      <section>
        <SectionHeader eyebrow="reserve" title="Backup readiness" description="Estimated runtime if grid were lost right now." />
        <div className="surface p-5">
          <div className="flex items-end justify-between mb-3">
            <div>
              <div className="num-display text-bone-100 text-4xl leading-none">
                {Math.floor(((t.batterySoc / 100) * t.batteryCapacityKwh) / Math.max(0.4, t.homeLoadW / 1000))}
              </div>
              <div className="label-cap mt-1">hours of essentials</div>
            </div>
            <div className="text-right">
              <div className="num-mono text-bone-300 text-[12px]">
                {((t.batterySoc / 100) * t.batteryCapacityKwh).toFixed(1)} kWh available
              </div>
              <div className="num-mono text-bone-500 text-[10px] mt-0.5">
                at {(t.homeLoadW / 1000).toFixed(2)} kW current draw
              </div>
            </div>
          </div>
          <div className="h-1.5 rounded-full bg-carbon-800 overflow-hidden">
            <div
              className="h-full rounded-full bg-signal-battery transition-all duration-700"
              style={{ width: `${t.batterySoc}%` }}
            />
          </div>
        </div>
      </section>

      <section className="grid grid-cols-2 gap-3">
        <MetricTile
          label="Health"
          value={t.batteryHealthPct.toFixed(1)}
          unit="%"
          delta={t.batteryHealthPct > 95 ? 'excellent' : 'normal'}
          deltaTone="positive"
          icon={<ShieldCheck size={14} strokeWidth={1.6} />}
        />
        <MetricTile
          label="Round-trip"
          value="94.2"
          unit="%"
          subtle="efficiency"
          icon={<Activity size={14} strokeWidth={1.6} />}
        />
        <MetricTile
          label="Cycles"
          value={t.batteryCycles.toString()}
          subtle="of 6,000 rated"
          icon={<Clock size={14} strokeWidth={1.6} />}
        />
        <MetricTile
          label="Cell temp"
          value={t.batteryTempC.toFixed(1)}
          unit="°C"
          delta="thermal · ok"
          deltaTone="positive"
          icon={<Thermometer size={14} strokeWidth={1.6} />}
        />
      </section>

      <section>
        <SectionHeader eyebrow="strategy" title="Charge mode" />
        <div className="surface p-4 space-y-3">
          {[
            { id: 'self', name: 'Self-consumption', desc: 'Maximize using your own solar before drawing from grid.', active: true },
            { id: 'tou', name: 'Time-of-use', desc: 'Charge from grid in off-peak; discharge during peak rates.' },
            { id: 'backup', name: 'Backup-only', desc: 'Hold ≥80% reserve at all times for outage protection.' }
          ].map((mode) => (
            <button
              key={mode.id}
              className={`w-full text-left flex items-start gap-3 p-3 rounded-xl border transition-colors ${
                mode.active
                  ? 'border-bone-700/50 bg-carbon-700/40'
                  : 'border-hairline bg-carbon-800/40 hover:border-bone-700/40'
              }`}
            >
              <div
                className={`mt-1 size-3.5 rounded-full border-2 ${
                  mode.active ? 'border-signal-battery bg-signal-battery/20' : 'border-bone-600'
                } flex items-center justify-center`}
              >
                {mode.active && <span className="size-1.5 rounded-full bg-signal-battery" />}
              </div>
              <div className="flex-1">
                <div className="flex items-center gap-2">
                  <span className="text-bone-100 text-[13px] font-medium">{mode.name}</span>
                  {mode.active && (
                    <span className="text-[9px] font-mono uppercase tracking-widest text-signal-battery">active</span>
                  )}
                </div>
                <p className="text-bone-500 text-[12px] mt-0.5 leading-relaxed">{mode.desc}</p>
              </div>
              <Battery size={16} strokeWidth={1.4} className="text-bone-500 mt-1" />
            </button>
          ))}
        </div>
      </section>
    </div>
  );
}
