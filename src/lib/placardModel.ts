/**
 * Deterministic site model for the landing sheet.
 *
 * Pure functions only: no Date, no Math.random, and no mutable state beyond one memoised `dayCache` of
 * the deterministic day, so the same hour always renders the same readings and a capture is reproducible. The physics mirrors the shipped mock in
 * `src/services/sunspec.ts` (9.6 kW DC across three 3.2 kW strings, 13.5 kWh battery, 0.964 conversion,
 * 5 kW charge/discharge ceiling, 12% floor) and reuses the real curve in `src/lib/solarCurve.ts`
 * (sunrise 06:12, solar noon 13:00, sunset 19:48).
 *
 * Every figure the landing page prints comes from here.
 */

import { clamp } from './format';
import { consumptionFractionAt, irradianceAt, nowAsHourFloat, solarFractionAt } from './solarCurve';

export const SITE = {
  ratedDcW: 9600,
  batteryKwh: 13.5,
  conversion: 0.964,
  chargeLimitW: 5000,
  floorSocPct: 12,
  loadPeakW: 4200,
  startSocPct: 62,
  model: 'HX-9.6 HYBRID',
  firmware: '4.12.1',
  serial: 'HX-2025-0F31A2',
  strings: [
    { id: 'A', label: 'ROOF / SOUTH-EAST', ratedW: 3200, panels: 8, orientation: -0.15 },
    { id: 'B', label: 'ROOF / SOUTH-WEST', ratedW: 3200, panels: 8, orientation: 0.15 },
    { id: 'C', label: 'GARAGE / SOUTH', ratedW: 3200, panels: 8, orientation: 0 }
  ]
} as const;

export const STEP_HOURS = 0.25;
export const FRAME_COUNT = Math.round((24 / STEP_HOURS)) + 1;

export type InverterState = 'NIGHT' | 'STANDBY' | 'PRODUCING' | 'CURTAILED';

export interface StringReading {
  id: string;
  label: string;
  powerW: number;
  ratedW: number;
  voltageV: number;
  currentA: number;
  shareOfPeers: number;
}

export interface FlowFrame {
  hour: number;
  irradianceWm2: number;
  cloudCoverPct: number;
  dcPowerW: number;
  acPowerW: number;
  homeLoadW: number;
  batteryPowerW: number;
  batterySocPct: number;
  gridImportW: number;
  gridExportW: number;
  heatsinkTempC: number;
  ambientTempC: number;
  strings: StringReading[];
  stringSpread: number;
  state: InverterState;
}

/** Deterministic sky: a clear morning, a partly cloudy afternoon, clearing again by evening. */
export function cloudCoverAt(hour: number): number {
  const wave = Math.sin(((hour - 15) / 24) * Math.PI * 2);
  return clamp(0.34 + 0.26 * wave, 0.05, 0.85);
}

function stringReadings(dcPowerW: number, hour: number): StringReading[] {
  const sunSkew = clamp((hour - 12) / 6, -1, 1);
  const perString = dcPowerW / SITE.strings.length;
  const ratings = SITE.strings.map((s) => perString * (1 + s.orientation * sunSkew));
  const max = Math.max(...ratings, 1);
  return SITE.strings.map((s, i) => {
    const powerW = clamp(ratings[i], 0, s.ratedW);
    const voltageV = powerW > 30 ? 380 + (i * 7.5) : 0;
    return {
      id: s.id,
      label: s.label,
      powerW,
      ratedW: s.ratedW,
      voltageV,
      currentA: voltageV > 0 ? powerW / voltageV : 0,
      shareOfPeers: powerW / max
    };
  });
}

function stateFor(hour: number, dcPowerW: number, socPct: number, acPowerW: number): InverterState {
  if (hour < 5.8 || hour > 20.2) return 'NIGHT';
  if (dcPowerW < 50) return 'STANDBY';
  if (acPowerW > SITE.ratedDcW * SITE.conversion * 0.95 && socPct > 99) return 'CURTAILED';
  return 'PRODUCING';
}

/** One frame with a supplied state of charge; `buildDay` supplies that by integrating the day. */
function frameWithSoc(hour: number, socPct: number): FlowFrame {
  const cloud = cloudCoverAt(hour);
  const irradiance = irradianceAt(hour, cloud);
  const dcPowerW = (irradiance / 1000) * SITE.ratedDcW;
  const acPowerW = dcPowerW * SITE.conversion;
  const homeLoadW = consumptionFractionAt(hour) * SITE.loadPeakW;
  const surplus = acPowerW - homeLoadW;

  let batteryPowerW = 0;
  let gridImportW = 0;
  let gridExportW = 0;
  if (surplus >= 0) {
    batteryPowerW = socPct < 100 ? Math.min(surplus, SITE.chargeLimitW) : 0;
    gridExportW = surplus - batteryPowerW;
  } else {
    const deficit = -surplus;
    batteryPowerW = socPct > SITE.floorSocPct ? -Math.min(deficit, SITE.chargeLimitW) : 0;
    gridImportW = deficit + batteryPowerW;
  }

  const strings = stringReadings(dcPowerW, hour);
  const stringSpread = strings.some((s) => s.powerW > 0)
    ? 1 - Math.min(...strings.map((s) => s.shareOfPeers))
    : 0;

  return {
    hour,
    irradianceWm2: irradiance,
    cloudCoverPct: cloud * 100,
    dcPowerW,
    acPowerW,
    homeLoadW,
    batteryPowerW,
    batterySocPct: clamp(socPct, 0, 100),
    gridImportW,
    gridExportW,
    heatsinkTempC: 27.5 + (dcPowerW / SITE.ratedDcW) * 22,
    ambientTempC: 17 + 9 * solarFractionAt(hour),
    strings,
    stringSpread,
    state: stateFor(hour, dcPowerW, socPct, acPowerW)
  };
}

export interface DayTotals {
  generatedKwh: number;
  consumedKwh: number;
  importedKwh: number;
  exportedKwh: number;
  selfConsumptionPct: number;
  peakAcW: number;
  peakHour: number;
}

let dayCache: FlowFrame[] | null = null;

/** 97 frames at quarter-hour resolution, battery state of charge integrated across the whole day. */
export function buildDay(): FlowFrame[] {
  if (dayCache) return dayCache;
  const frames: FlowFrame[] = [];
  let soc: number = SITE.startSocPct;
  for (let i = 0; i < FRAME_COUNT; i += 1) {
    const hour = i * STEP_HOURS;
    const frame = frameWithSoc(hour, soc);
    frames.push(frame);
    soc = clamp(soc + (frame.batteryPowerW * STEP_HOURS) / (SITE.batteryKwh * 10), 0, 100);
  }
  dayCache = frames;
  return frames;
}

export function frameIndexForHour(hour: number): number {
  return clamp(Math.round(hour / STEP_HOURS), 0, FRAME_COUNT - 1);
}

export function frameAt(index: number): FlowFrame {
  const day = buildDay();
  return day[clamp(index, 0, day.length - 1)];
}

export function dayTotals(): DayTotals {
  const day = buildDay();
  let generatedKwh = 0;
  let consumedKwh = 0;
  let importedKwh = 0;
  let exportedKwh = 0;
  let peakAcW = 0;
  let peakHour = 0;
  for (const f of day) {
    generatedKwh += (f.acPowerW * STEP_HOURS) / 1000;
    consumedKwh += (f.homeLoadW * STEP_HOURS) / 1000;
    importedKwh += (f.gridImportW * STEP_HOURS) / 1000;
    exportedKwh += (f.gridExportW * STEP_HOURS) / 1000;
    if (f.acPowerW > peakAcW) {
      peakAcW = f.acPowerW;
      peakHour = f.hour;
    }
  }
  return {
    generatedKwh,
    consumedKwh,
    importedKwh,
    exportedKwh,
    selfConsumptionPct: generatedKwh > 0 ? (1 - exportedKwh / generatedKwh) * 100 : 0,
    peakAcW,
    peakHour
  };
}

export function hourFloatNow(): number {
  return nowAsHourFloat(new Date());
}

export function formatHour(hour: number): string {
  const h = Math.floor(hour) % 24;
  const m = Math.round((hour - Math.floor(hour)) * 60) % 60;
  return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`;
}

/* ------------------------------------------------------------------ notice set */

export type SignalWord = 'DANGER' | 'WARNING' | 'CAUTION' | 'NOTICE' | 'GUIDE';

export interface Notice {
  id: string;
  word: SignalWord;
  title: string;
  body: string;
  metric: string;
  unit: string;
  delta: string;
  action: string;
}

/**
 * The product's own output vocabulary (`src/services/aiInsights.ts`), reduced to the fields a placard
 * field can carry: signal word, message, metric, action. Thresholds are the shipped ones.
 */
export function noticesFor(frame: FlowFrame, totals: DayTotals): Notice[] {
  const out: Notice[] = [];

  if (frame.state === 'PRODUCING' && frame.acPowerW > 5500 && frame.cloudCoverPct < 40) {
    out.push({
      id: 'peak-production',
      word: 'NOTICE',
      title: 'Peak production window',
      body: `Output is at ${Math.round((frame.acPowerW / SITE.ratedDcW) * 100)}% of the array's DC rating. This is the window to run heavy loads on your own generation.`,
      metric: (frame.acPowerW / 1000).toFixed(2),
      unit: 'kW',
      delta: 'AC OUTPUT',
      action: 'Schedule appliances'
    });
  }

  if (frame.stringSpread > 0.15 && frame.acPowerW > 1000) {
    out.push({
      id: 'string-imbalance',
      word: 'WARNING',
      title: 'String output imbalance',
      body: 'One string is producing well below its peers. Possible shading, soiling, or a degraded panel.',
      metric: String(Math.round(frame.stringSpread * 100)),
      unit: '%',
      delta: 'SPREAD ACROSS STRINGS',
      action: 'Inspect after sunset'
    });
  }

  if (frame.heatsinkTempC > 48) {
    out.push({
      id: 'thermal',
      word: 'CAUTION',
      title: 'Inverter running warm',
      body: 'Output may derate above 55 °C. Check the cabinet vent for obstructions.',
      metric: frame.heatsinkTempC.toFixed(1),
      unit: '°C',
      delta: 'HEATSINK',
      action: 'Check cabinet vent'
    });
  }

  if (frame.batterySocPct > 92 && frame.gridExportW > 1500) {
    out.push({
      id: 'export-active',
      word: 'NOTICE',
      title: 'Exporting surplus to grid',
      body: 'The battery is full and the remainder is leaving the house rather than staying in it.',
      metric: (frame.gridExportW / 1000).toFixed(2),
      unit: 'kW',
      delta: 'TO GRID',
      action: 'Shift a load forward'
    });
  }

  if (frame.batterySocPct < 25 && frame.hour < 6.5) {
    out.push({
      id: 'overnight-low',
      word: 'CAUTION',
      title: 'Battery low overnight',
      body: 'State of charge is below the morning reserve. Off-peak charging protects autonomy if dawn is overcast.',
      metric: String(Math.round(frame.batterySocPct)),
      unit: '%',
      delta: 'STATE OF CHARGE',
      action: 'Plan off-peak charge'
    });
  }

  if (frame.state === 'NIGHT' || frame.state === 'STANDBY') {
    out.push({
      id: 'night',
      word: 'GUIDE',
      title: 'Array at rest',
      body: 'No harvest is available. The monitor keeps reading the inverter, and the battery covers the house.',
      metric: frame.homeLoadW >= 1000 ? (frame.homeLoadW / 1000).toFixed(2) : String(Math.round(frame.homeLoadW)),
      unit: frame.homeLoadW >= 1000 ? 'kW' : 'W',
      delta: 'HOUSE LOAD',
      action: 'See tomorrow forecast'
    });
  }

  out.push({
    id: 'self-consumption',
    word: 'NOTICE',
    title: 'Self-consumption for the day',
    body: 'Share of the day\u2019s generation used in the house rather than exported to the grid.',
    metric: String(Math.round(totals.selfConsumptionPct)),
    unit: '%',
    delta: `OF ${totals.generatedKwh.toFixed(1)} kWh GENERATED`,
    action: 'Open the dashboard'
  });

  return out.slice(0, 3);
}
