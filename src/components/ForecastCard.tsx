import { motion } from 'framer-motion';
import type { ProductionForecast } from '../types';
import { WeatherIcon } from './WeatherIcon';
import { dayLabelFor } from '../services/weather';

interface ForecastCardProps {
  forecast: ProductionForecast;
}

export function ForecastCard({ forecast }: ForecastCardProps) {
  const todayIso = forecast.days[0]?.date ?? '';
  const max = Math.max(...forecast.days.map((d) => d.expectedKwh), 1);

  return (
    <div className="surface p-4">
      <div className="flex items-center justify-between mb-1">
        <span className="label-cap">7-day production forecast</span>
        <span className="num-mono text-bone-300 text-[11px]">{forecast.totalKwh.toFixed(0)} kWh</span>
      </div>
      <div className="text-bone-500 text-[11px] font-mono mb-4">
        {forecast.location.label} · refreshed {timeSince(forecast.fetchedAt)}
      </div>

      <div className="space-y-2.5">
        {forecast.days.map((d, i) => {
          const ratio = d.expectedKwh / max;
          const dayLabel = dayLabelFor(d.date, todayIso);
          return (
            <motion.div
              key={d.date}
              initial={{ opacity: 0, x: -8 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ duration: 0.3, delay: i * 0.04 }}
              className="flex items-center gap-3"
            >
              <div className="w-14 shrink-0">
                <div className={`text-[12px] font-medium ${i === 0 ? 'text-bone-100' : 'text-bone-300'}`}>
                  {dayLabel}
                </div>
                <div className="num-mono text-bone-600 text-[10px]">
                  {d.tempLowC.toFixed(0)}° / {d.tempHighC.toFixed(0)}°
                </div>
              </div>
              <WeatherIcon condition={d.condition} size={22} />
              <div className="flex-1">
                <div className="flex items-baseline justify-between mb-1">
                  <span className="text-bone-400 text-[11px] font-mono lowercase">{d.conditionLabel}</span>
                  <span className="num-mono text-bone-100 text-[12px] font-medium">
                    {d.expectedKwh.toFixed(0)}
                    <span className="text-bone-500 text-[10px] ml-0.5">kWh</span>
                  </span>
                </div>
                <div className="h-1 rounded-full bg-carbon-800 overflow-hidden">
                  <div
                    className="h-full rounded-full bg-gradient-to-r from-signal-solar/60 to-signal-solar transition-all duration-700"
                    style={{ width: `${Math.max(4, ratio * 100)}%` }}
                  />
                </div>
              </div>
            </motion.div>
          );
        })}
      </div>
    </div>
  );
}

function timeSince(ts: number): string {
  const diff = Date.now() - ts;
  const m = Math.round(diff / 60000);
  if (m < 1) return 'just now';
  if (m < 60) return `${m}m ago`;
  const h = Math.round(m / 60);
  return `${h}h ago`;
}
