import type { ProductionForecast } from '../types';
import { WeatherIcon } from './WeatherIcon';
import { dayLabelFor } from '../services/weather';

interface ForecastStripProps {
  forecast: ProductionForecast;
  loading?: boolean;
}

export function ForecastStrip({ forecast }: ForecastStripProps) {
  const todayIso = forecast.days[0]?.date ?? '';
  const days = forecast.days.slice(0, 5);
  const max = Math.max(...days.map((d) => d.expectedKwh), 1);

  return (
    <div className="surface p-4">
      <div className="flex items-start justify-between gap-2 mb-3">
        <div className="min-w-0 flex-1">
          <span className="label-cap">5-day forecast</span>
          <div className="text-bone-500 text-[11px] font-mono lowercase truncate mt-0.5">
            {forecast.location.label}
          </div>
        </div>
        <span
          className={`shrink-0 text-[10px] font-mono uppercase tracking-[0.18em] ${
            forecast.vsLastWeekPct >= 0 ? 'text-signal-flow' : 'text-signal-alert'
          }`}
        >
          {forecast.vsLastWeekPct >= 0 ? '+' : ''}
          {forecast.vsLastWeekPct.toFixed(0)}% vs typical
        </span>
      </div>
      <div className="grid grid-cols-5 gap-2">
        {days.map((d) => {
          const ratio = d.expectedKwh / max;
          return (
            <div key={d.date} className="flex flex-col items-center gap-1.5">
              <span className="text-[10px] font-mono uppercase tracking-widest text-bone-500">
                {dayLabelFor(d.date, todayIso).slice(0, 3)}
              </span>
              <WeatherIcon condition={d.condition} size={20} />
              <div className="num-mono text-bone-100 text-[12px] font-medium">{d.expectedKwh.toFixed(0)}</div>
              <div className="text-bone-600 text-[9px] font-mono">kWh</div>
              <div className="w-full h-0.5 rounded-full bg-carbon-800 overflow-hidden">
                <div
                  className="h-full rounded-full bg-signal-solar transition-all duration-700"
                  style={{ width: `${Math.max(8, ratio * 100)}%` }}
                />
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
