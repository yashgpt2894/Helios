import { useMemo } from 'react';
import { useStore } from '../store/useStore';
import { computeSavings, generateInsights } from '../services/aiInsights';
import { generateForecastInsights } from '../services/forecastInsights';
import { InsightCard, InsightHighlight } from '../components/InsightCard';
import { SectionHeader } from '../components/SectionHeader';
import { MetricTile } from '../components/MetricTile';
import { ForecastCard } from '../components/ForecastCard';
import { Sparkles, DollarSign, Calendar, Globe, MapPin } from 'lucide-react';

export function Insights() {
  const t = useStore((s) => s.telemetry);
  const forecast = useStore((s) => s.forecast);
  const forecastStatus = useStore((s) => s.forecastStatus);
  const useMyLocation = useStore((s) => s.useMyLocation);
  const location = useStore((s) => s.location);
  const liveInsights = useMemo(() => generateInsights(t), [t]);
  const fcInsights = useMemo(() => generateForecastInsights(forecast, t), [forecast, t]);
  const insights = [...fcInsights, ...liveInsights];
  const savings = useMemo(() => computeSavings(t), [t]);

  const highlight = insights[0];
  const rest = insights.slice(1);

  return (
    <div className="px-5 pb-32 pt-2 space-y-5">
      <header className="px-1">
        <div className="flex items-center gap-1.5">
          <Sparkles size={12} strokeWidth={1.6} className="text-bone-400" />
          <span className="label-cap">helios° intelligence</span>
        </div>
        <h1 className="text-bone-100 text-[28px] font-medium tracking-tight mt-1.5">
          What we noticed today
        </h1>
        <p className="text-bone-500 text-[13px] mt-1 leading-relaxed">
          Insights generated from {t.panels.length} strings, your inverter, the battery cabinet, and
          local weather. Updated continuously.
        </p>
      </header>

      {highlight && <InsightHighlight insight={highlight} />}

      <section>
        <SectionHeader
          eyebrow="weather · ai forecast"
          title="Production outlook"
          description="Daily solar yield predictions from Open-Meteo, modeled against your 9.6 kW array."
          trailing={
            location.source === 'browser' ? undefined : (
              <button
                onClick={() => void useMyLocation()}
                className="inline-flex items-center gap-1 text-bone-300 hover:text-bone-100 transition-colors"
              >
                <MapPin size={11} strokeWidth={1.6} />
                use my location
              </button>
            )
          }
        />
        {forecast ? (
          <ForecastCard forecast={forecast} />
        ) : (
          <div className="surface p-6 text-center">
            <span className="text-bone-500 text-[13px]">
              {forecastStatus === 'loading' ? 'Loading 7-day forecast…' : 'Forecast unavailable.'}
            </span>
          </div>
        )}
      </section>

      <section className="grid grid-cols-2 gap-3">
        <MetricTile
          label="Savings · today"
          value={`$${savings.today.toFixed(2)}`}
          icon={<DollarSign size={14} strokeWidth={1.6} />}
          deltaTone="positive"
          delta="vs grid baseline"
        />
        <MetricTile
          label="Savings · month"
          value={`$${savings.month.toFixed(0)}`}
          icon={<Calendar size={14} strokeWidth={1.6} />}
          deltaTone="positive"
          delta="MTD"
        />
        <MetricTile
          label="Lifetime"
          value={`$${savings.lifetime.toFixed(0)}`}
          icon={<DollarSign size={14} strokeWidth={1.6} />}
          subtle="since install"
        />
        <MetricTile
          label="CO₂ avoided"
          value={`${(t.energyLifetimeKwh * 0.42).toFixed(0)}`}
          unit="kg"
          icon={<Globe size={14} strokeWidth={1.6} />}
          subtle="lifetime"
        />
      </section>

      <section>
        <SectionHeader eyebrow="full feed" title="All insights" trailing={`${insights.length} active`} />
        <div className="space-y-3">
          {rest.map((insight, i) => (
            <InsightCard key={insight.id} insight={insight} index={i} />
          ))}
          {rest.length === 0 && (
            <div className="surface p-6 text-center">
              <span className="text-bone-500 text-[13px]">No additional advisories. Your system is operating cleanly.</span>
            </div>
          )}
        </div>
      </section>
    </div>
  );
}
