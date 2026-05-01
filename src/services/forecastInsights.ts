import type { ForecastDay, Insight, ProductionForecast, SolarTelemetry } from '../types';

const KWH_RATE_GRID = 0.32;
const OFF_PEAK_RATE = 0.11;

export function generateForecastInsights(
  forecast: ProductionForecast | null,
  t: SolarTelemetry
): Insight[] {
  if (!forecast || forecast.days.length === 0) return [];

  const insights: Insight[] = [];
  const today = forecast.days[0];
  const tomorrow = forecast.days[1];
  const next3 = forecast.days.slice(0, 3);

  if (tomorrow && tomorrow.expectedKwh < 8 && t.batterySoc < 60) {
    const chargeKwh = Math.max(0, t.batteryCapacityKwh * 0.95 - (t.batterySoc / 100) * t.batteryCapacityKwh);
    insights.push({
      id: 'precharge-storm',
      category: 'forecast',
      severity: 'attention',
      title: `${tomorrow.conditionLabel} tomorrow — pre-charge tonight`,
      body: `Forecast: only ${tomorrow.expectedKwh.toFixed(0)} kWh production expected. Top up battery from off-peak grid (~$${(chargeKwh * OFF_PEAK_RATE).toFixed(2)}) so you ride out the day on storage.`,
      metric: `${tomorrow.expectedKwh.toFixed(0)} kWh`,
      delta: 'tomorrow',
      actionLabel: 'Schedule pre-charge'
    });
  }

  const sunnyDay = forecast.days.find((d) => d.expectedKwhVsTypical > 0.25 && d.expectedKwh > 38);
  if (sunnyDay) {
    const dayLabel = sunnyDay === today ? 'Today' : sunnyDay === tomorrow ? 'Tomorrow' : friendlyDay(sunnyDay.date);
    insights.push({
      id: 'high-yield-day',
      category: 'forecast',
      severity: 'positive',
      title: `${dayLabel}: brilliant solar window`,
      body: `${sunnyDay.expectedKwh.toFixed(0)} kWh expected — ${(sunnyDay.expectedKwhVsTypical * 100).toFixed(0)}% above typical. Run pool pump, dishwasher, and EV charge between 11 AM and 3 PM to capture peak surplus.`,
      metric: `${sunnyDay.expectedKwh.toFixed(0)} kWh`,
      delta: `+${(sunnyDay.expectedKwhVsTypical * 100).toFixed(0)}%`,
      actionLabel: 'Plan heavy loads'
    });
  }

  const stormDay = forecast.days.find(
    (d) => d.condition === 'thunderstorm' || d.condition === 'heavy-rain' || (d.precipitationMm > 8 && d.cloudCoverPct > 70)
  );
  if (stormDay && stormDay !== today) {
    const dayLabel = stormDay === tomorrow ? 'Tomorrow' : friendlyDay(stormDay.date);
    insights.push({
      id: 'storm-warning',
      category: 'forecast',
      severity: 'attention',
      title: `${stormDay.conditionLabel} ${dayLabel.toLowerCase()}`,
      body: `${stormDay.precipitationMm.toFixed(1)} mm precipitation with ${stormDay.cloudCoverPct.toFixed(0)}% cloud cover. Production likely to drop to ${stormDay.expectedKwh.toFixed(0)} kWh. Pre-cool the home and finish laundry before midday.`,
      metric: stormDay.conditionLabel,
      delta: dayLabel.toLowerCase()
    });
  }

  const next3Total = next3.reduce((s, d) => s + d.expectedKwh, 0);
  const avgDailyUseKwh = (t.homeLoadW > 0 ? t.homeLoadW : 800) * 24 / 1000 * 0.7;
  if (next3Total > avgDailyUseKwh * 3 * 1.3) {
    insights.push({
      id: 'self-sufficient-streak',
      category: 'forecast',
      severity: 'positive',
      title: 'Off-grid streak ahead',
      body: `Next 3 days forecast ${next3Total.toFixed(0)} kWh against ~${(avgDailyUseKwh * 3).toFixed(0)} kWh of typical use. You can stay 100% off-grid through ${friendlyDay(forecast.days[2].date)}.`,
      metric: `${next3Total.toFixed(0)} kWh`,
      delta: '3-day forecast'
    });
  }

  insights.push({
    id: 'weekly-outlook',
    category: 'forecast',
    severity: 'neutral',
    title: '7-day production outlook',
    body: `${forecast.totalKwh.toFixed(0)} kWh expected across the week — ${forecast.vsLastWeekPct >= 0 ? '+' : ''}${forecast.vsLastWeekPct.toFixed(0)}% vs typical. Cleanest sky on ${bestDay(forecast.days)}, dimmest on ${worstDay(forecast.days)}.`,
    metric: `${forecast.totalKwh.toFixed(0)} kWh`,
    delta: `${forecast.vsLastWeekPct >= 0 ? '+' : ''}${forecast.vsLastWeekPct.toFixed(0)}% vs typical`
  });

  const projectedSavings = forecast.totalKwh * KWH_RATE_GRID * 0.7;
  if (projectedSavings > 10) {
    insights.push({
      id: 'projected-savings',
      category: 'savings',
      severity: 'neutral',
      title: 'Projected savings this week',
      body: `If forecast holds and self-consumption stays near 70%, you'll avoid roughly $${projectedSavings.toFixed(0)} in grid imports over the next 7 days.`,
      metric: `$${projectedSavings.toFixed(0)}`,
      delta: 'next 7 days'
    });
  }

  return insights;
}

function friendlyDay(dateStr: string): string {
  return new Date(dateStr + 'T00:00:00').toLocaleDateString('en-US', { weekday: 'long' });
}

function bestDay(days: ForecastDay[]): string {
  const best = [...days].sort((a, b) => b.expectedKwh - a.expectedKwh)[0];
  return friendlyDay(best.date);
}

function worstDay(days: ForecastDay[]): string {
  const worst = [...days].sort((a, b) => a.expectedKwh - b.expectedKwh)[0];
  return friendlyDay(worst.date);
}
