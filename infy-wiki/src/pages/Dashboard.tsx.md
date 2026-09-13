# Dashboard

```tsx
import { useMemo } from 'react';
import { Sun, Zap, TrendingUp, Leaf } from 'lucide-react';
import { useStore } from '../store/useStore';
import { generateInsights } from '../services/aiInsights';
import { generateForecastInsights } from '../services/forecastInsights';
import { EnergyFlow } from '../components/EnergyFlow';
import { MetricTile } from '../components/MetricTile';
import { SectionHeader } from '../components/SectionHeader';
import { ProductionChart } from '../components/ProductionChart';
import { StatusPill } from '../components/StatusPill';
import { LiveNumber } from '../components/LiveNumber';
import { InsightHighlight } from '../components/InsightCard';
import { ForecastStrip } from '../components/ForecastStrip';
import { ShareButton } from '../components/ShareButton';
import { formatKwh, formatRelative } from '../lib/format';

export function Dashboard() {
  const t = useStore((s) => s.telemetry);
  const series = useStore((s) => s.todaySeries);
  const forecast = useStore((s) => s.forecast);
  const forecastStatus = useStore((s) => s.forecastStatus);
  const liveInsights = useMemo(() => generateInsights(t), [t]);
  const fcInsights = useMemo(() => generateForecastInsights(forecast, t), [forecast, t]);
  const insights = [...fcInsights, ...liveInsights];
  const featured = insights.find((i) => i.severity !== 'neutral') ?? insights[0];

  const co2KgToday = t.energyTodayKwh * 0.42;
  const treesEquivalent = (co2KgToday / 21).toFixed(2);

  return (
    <div className="px-5 pb-32 pt-2 space-y-5">
      <section className="surface-raised p-5 grid-bg overflow-hidden">
        <div className="flex items-start justify-between gap-3 mb-4">
          <div className="min-w-0">
            <span className="label-cap">Live · {formatRelative(t.timestamp)}</span>
            <div className="mt-2 flex items-baseline gap-2">
              <LiveNumber
                value={t.acPowerW / 1000}
                digits={2}
                className="num-display text-bone-100 text-6xl leading-none"
              />
              <span className="text-bone-400 text-sm font-mono uppercase tracking-widest">kW</span>
            </div>
            <p className="text-bone-500 text-[12px] mt-1.5 font-mono">
              from <span className="text-bone-300">{t.irradianceWm2.toFixed(0)} W/m²</span> · cabinet{' '}
              <span className="text-bone-300">{t.cabinetTempC.toFixed(0)}°C</span>
            </p>
          </div>
          <div className="flex items-center gap-2 shrink-0">
            <ShareButton />
            <StatusPill status={t.status} />
          </div>
        </div>

        <EnergyFlow t={t} />

        <div className="mt-5 grid grid-cols-3 gap-3 pt-4 border-t border-hairline">
          <div className="text-center">
            <div className="label-cap mb-1">Today</div>
            <div className="num-display text-bone-100 text-2xl">
              <LiveNumber value={t.energyTodayKwh} digits={1} />
            </div>
            <div className="text-bone-500 text-[10px] font-mono uppercase tracking-widest">kWh</div>
          </div>
          <div className="text-center border-x border-hairline">
            <div className="label-cap mb-1">Self-use</div>
            <div className="num-display text-bone-100 text-2xl">
              <LiveNumber
                value={
                  t.acPowerW > 0
                    ? Math.max(0, Math.min(100, (1 - t.gridExportW / Math.max(1, t.acPowerW)) * 100))
                    : 0
                }
                digits={0}
              />
            </div>
            <div className="text-bone-500 text-[10px] font-mono uppercase tracking-widest">%</div>
          </div>
          <div className="text-center">
            <div className="label-cap mb-1">CO₂ saved</div>
            <div className="num-display text-bone-100 text-2xl">
              <LiveNumber value={co2KgToday} digits={1} />
            </div>
            <div className="text-bone-500 text-[10px] font-mono uppercase tracking-widest">kg today</div>
          </div>
        </div>
      </section>

      {featured && (
        <section>
          <SectionHeader eyebrow="ai" title="What helios noticed" />
          <InsightHighlight insight={featured} />
        </section>
      )}

      <section>
        <SectionHeader
          eyebrow="weather"
          title="Solar forecast"
          trailing={forecastStatus === 'loading' ? 'loading…' : forecastStatus === 'error' ? 'unavailable' : undefined}
        />
        {forecast ? (
          <ForecastStrip forecast={forecast} />
        ) : (
          <div className="surface p-4">
            <div className="flex items-center justify-between">
              <span className="text-bone-400 text-[12px]">
                {forecastStatus === 'loading' ? 'Fetching forecast…' : 'Forecast not loaded yet.'}
              </span>
              {forecastStatus === 'error' && (
                <span className="text-signal-alert text-[10px] font-mono uppercase tracking-widest">offline</span>
              )}
            </div>
          </div>
        )}
      </section>

      <section>
        <SectionHeader eyebrow="today" title="Production curve" trailing={`${formatKwh(t.energyTodayKwh)}`} />
        <div className="surface p-4">
          <ProductionChart data={series} />
          <div className="flex items-center justify-between mt-3 pt-3 border-t border-hairline text-[10px] font-mono uppercase tracking-widest">
            <span className="flex items-center gap-1.5">
              <span className="size-1.5 rounded-full bg-signal-solar" />
              <span className="text-bone-400">Solar</span>
            </span>
            <span className="flex items-center gap-1.5">
              <span className="size-1.5 rounded-full bg-bone-500" />
              <span className="text-bone-400">Home</span>
            </span>
            <span className="text-bone-600">kW · 0–24h</span>
          </div>
        </div>
      </section>

      <section className="grid grid-cols-2 gap-3">
        <MetricTile
          label="Production today"
          value={t.energyTodayKwh.toFixed(1)}
          unit="kWh"
          delta="+12% vs avg"
          deltaTone="positive"
          icon={<Sun size={14} strokeWidth={1.6} />}
        />
        <MetricTile
          label="Home usage"
          value={(t.homeLoadW / 1000).toFixed(2)}
          unit="kW now"
          subtle="4.2 kW peak"
          icon={<Zap size={14} strokeWidth={1.6} />}
        />
        <MetricTile
          label="Lifetime"
          value={(t.energyLifetimeKwh / 1000).toFixed(2)}
          unit="MWh"
          subtle={`since 2023`}
          icon={<TrendingUp size={14} strokeWidth={1.6} />}
        />
        <MetricTile
          label="Trees equivalent"
          value={treesEquivalent}
          unit="trees / day"
          delta={`${co2KgToday.toFixed(0)} kg CO₂ avoided`}
          deltaTone="positive"
          icon={<Leaf size={14} strokeWidth={1.6} />}
        />
      </section>
    </div>
  );
}

```