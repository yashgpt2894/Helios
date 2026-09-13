# AiInsights

```ts
import type { Insight, SolarTelemetry } from '../types';
import { nowAsHourFloat } from '../lib/solarCurve';

const KWH_RATE_GRID = 0.32;
const KWH_RATE_EXPORT = 0.08;

export function generateInsights(t: SolarTelemetry): Insight[] {
  const insights: Insight[] = [];
  const hour = nowAsHourFloat(new Date(t.timestamp));

  const selfConsumptionPct = t.acPowerW > 0
    ? clamp01(1 - t.gridExportW / Math.max(1, t.acPowerW)) * 100
    : 0;

  const todaySavings = t.energyTodayKwh * KWH_RATE_GRID + (t.energyTodayKwh * 0.4) * KWH_RATE_EXPORT;
  const monthSavings = t.energyMonthKwh * (KWH_RATE_GRID * 0.7);

  if (t.status === 'PRODUCING' && t.acPowerW > 5500 && t.cloudCoverPct < 40) {
    insights.push({
      id: 'peak-production',
      category: 'production',
      severity: 'positive',
      title: 'Peak production window',
      body: `You're harvesting ${(t.acPowerW / 1000).toFixed(2)} kW — within 8% of array rating. Now is the best moment to run heavy loads on direct solar.`,
      metric: `${(t.acPowerW / 1000).toFixed(2)} kW`,
      delta: '+8% vs typical',
      actionLabel: 'Schedule appliances'
    });
  }

  if (t.cloudCoverPct > 55 && hour > 9 && hour < 17) {
    insights.push({
      id: 'cloud-coverage',
      category: 'forecast',
      severity: 'attention',
      title: 'Cloud cover suppressing yield',
      body: `Current irradiance is ${Math.round(t.irradianceWm2)} W/m² — about ${Math.round((1 - t.irradianceWm2 / 1000) * 100)}% below clear-sky. Defer dishwasher and dryer until after 2:00 PM if possible.`,
      metric: `${Math.round(t.cloudCoverPct)}%`,
      delta: 'cloud cover'
    });
  }

  if (t.batterySoc > 92 && t.gridExportW > 1500) {
    insights.push({
      id: 'export-active',
      category: 'savings',
      severity: 'positive',
      title: 'Exporting surplus to grid',
      body: `Battery is full and you're exporting ${(t.gridExportW / 1000).toFixed(2)} kW. At your feed-in rate, this earns roughly $${((t.gridExportW / 1000) * KWH_RATE_EXPORT).toFixed(2)}/hr.`,
      metric: `+${(t.gridExportW / 1000).toFixed(2)} kW`,
      delta: 'feed-in active'
    });
  }

  if (t.batterySoc < 25 && hour < 6) {
    insights.push({
      id: 'overnight-low',
      category: 'battery',
      severity: 'attention',
      title: 'Battery low overnight',
      body: `State-of-charge is ${Math.round(t.batterySoc)}%. Off-peak grid charging until sunrise costs roughly $${((t.batteryCapacityKwh - (t.batterySoc / 100) * t.batteryCapacityKwh) * 0.11).toFixed(2)} and protects autonomy if morning is overcast.`,
      metric: `${Math.round(t.batterySoc)}%`,
      delta: 'soc'
    });
  }

  if (t.heatsinkTempC > 48) {
    insights.push({
      id: 'thermal',
      category: 'maintenance',
      severity: 'attention',
      title: 'Inverter running warm',
      body: `Heatsink at ${t.heatsinkTempC.toFixed(1)}°C. Output may derate above 55°C. Consider checking the cabinet vent for obstructions.`,
      metric: `${t.heatsinkTempC.toFixed(1)}°C`,
      delta: 'heatsink'
    });
  }

  if (t.status === 'PRODUCING' && selfConsumptionPct > 75) {
    insights.push({
      id: 'self-consumption-high',
      category: 'savings',
      severity: 'positive',
      title: 'Self-consumption is excellent',
      body: `${Math.round(selfConsumptionPct)}% of generation is being used on-site — well above the 58% neighborhood median. This is the most economically valuable mode of operation.`,
      metric: `${Math.round(selfConsumptionPct)}%`,
      delta: '+17 vs avg'
    });
  }

  insights.push({
    id: 'today-savings',
    category: 'savings',
    severity: 'neutral',
    title: 'Estimated savings today',
    body: `Avoided grid imports plus feed-in credits add up to about $${todaySavings.toFixed(2)} so far today. Month-to-date: $${monthSavings.toFixed(0)}.`,
    metric: `$${todaySavings.toFixed(2)}`,
    delta: 'today'
  });

  const stringSpread = computeStringSpread(t);
  if (stringSpread > 0.15 && t.acPowerW > 1000) {
    insights.push({
      id: 'string-imbalance',
      category: 'maintenance',
      severity: 'attention',
      title: 'String output imbalance detected',
      body: `One string is producing ${Math.round(stringSpread * 100)}% less than peers. Possible shading, soiling, or a degraded panel. Inspect after sunset.`,
      metric: `Δ ${Math.round(stringSpread * 100)}%`,
      delta: 'across strings'
    });
  }

  if (hour > 5.5 && hour < 7.5 && t.batterySoc > 60) {
    insights.push({
      id: 'morning-strategy',
      category: 'forecast',
      severity: 'neutral',
      title: 'Morning strategy looks healthy',
      body: `Battery at ${Math.round(t.batterySoc)}% heading into sunrise — you're set up to ride the solar ramp without grid imports.`,
      metric: `${Math.round(t.batterySoc)}%`,
      delta: 'soc · pre-dawn'
    });
  }

  return insights.slice(0, 8);
}

function computeStringSpread(t: SolarTelemetry): number {
  if (t.panels.length === 0) return 0;
  const expected = t.panels.map((p) => p.powerW / p.ratedW);
  const max = Math.max(...expected);
  const min = Math.min(...expected);
  if (max === 0) return 0;
  return (max - min) / max;
}

function clamp01(v: number): number {
  return Math.max(0, Math.min(1, v));
}

export function computeSavings(t: SolarTelemetry): { today: number; month: number; lifetime: number } {
  return {
    today: t.energyTodayKwh * KWH_RATE_GRID,
    month: t.energyMonthKwh * KWH_RATE_GRID * 0.85,
    lifetime: t.energyLifetimeKwh * KWH_RATE_GRID * 0.8
  };
}

```