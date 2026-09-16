import type { Insight, InverterStatus, PanelString, SolarTelemetry } from '../types';
import { clamp } from './format';
import { consumptionFractionAt, irradianceAt, solarFractionAt } from './solarCurve';

export const DEMO_RATED_W = 9600;
export const DEMO_BATTERY_KWH = 13.5;
export const DEMO_LOAD_SCALE_W = 3000;
export const DEMO_START_HOUR = 14.0;
export const DEMO_STEP_HOURS = 0.2;
export const DEMO_POLL_MS = 2000;

const CONVERSION = 0.964;
const CHARGE_LIMIT_W = 5000;
const SOC_FLOOR = 12;
const MINUTES = 24 * 60;

const STRINGS = [
  { id: 'A', label: 'Roof · South-East', ratedW: 3200, panels: 8, orientation: -0.15 },
  { id: 'B', label: 'Roof · South-West', ratedW: 3200, panels: 8, orientation: 0.15 },
  { id: 'C', label: 'Garage · South', ratedW: 3200, panels: 8, orientation: 0 }
];

export function demoCloudCoverAt(hour: number): number {
  const afternoonEvent = 0.55 * Math.exp(-Math.pow(hour - 15.7, 2) / 0.35);
  return clamp(0.15 + afternoonEvent, 0, 0.95);
}

interface MinuteSample {
  acPowerW: number;
  homeLoadW: number;
  batteryPowerW: number;
  gridImportW: number;
  gridExportW: number;
  batterySoc: number;
  irradianceWm2: number;
  energyTodayKwh: number;
}

const DAY: MinuteSample[] = (() => {
  const out: MinuteSample[] = [];
  const step = 1 / 60;
  let soc = 100;
  let energy = 0;
  for (let i = 0; i < MINUTES; i++) {
    const hour = i / 60;
    const irradianceWm2 = irradianceAt(hour, demoCloudCoverAt(hour));
    const acPowerW = (irradianceWm2 / 1000) * DEMO_RATED_W * CONVERSION;
    const homeLoadW = consumptionFractionAt(hour) * DEMO_LOAD_SCALE_W;
    const surplus = acPowerW - homeLoadW;
    let batteryPowerW = 0;
    let gridImportW = 0;
    let gridExportW = 0;
    if (surplus >= 0) {
      if (soc < 100) {
        batteryPowerW = Math.min(surplus, CHARGE_LIMIT_W);
        soc += ((batteryPowerW * step) / 1000 / DEMO_BATTERY_KWH) * 100;
        gridExportW = surplus - batteryPowerW;
      } else {
        gridExportW = surplus;
      }
    } else {
      const deficit = -surplus;
      if (soc > SOC_FLOOR) {
        batteryPowerW = -Math.min(deficit, CHARGE_LIMIT_W);
        soc += ((batteryPowerW * step) / 1000 / DEMO_BATTERY_KWH) * 100;
        gridImportW = Math.max(0, deficit - Math.abs(batteryPowerW));
      } else {
        gridImportW = deficit;
      }
    }
    soc = clamp(soc, 0, 100);
    energy += (acPowerW * step) / 1000;
    out.push({ acPowerW, homeLoadW, batteryPowerW, gridImportW, gridExportW, batterySoc: soc, irradianceWm2, energyTodayKwh: energy });
  }
  return out;
})();

export const DEMO_DAY_KWH = DAY[MINUTES - 1].energyTodayKwh;

export function demoSampleAt(hour: number): MinuteSample {
  const idx = clamp(Math.round(((hour % 24) + 24) % 24 * 60), 0, MINUTES - 1);
  return DAY[idx];
}

function seeded(n: number): number {
  const x = Math.sin(n * 12.9898 + 78.233) * 43758.5453;
  return x - Math.floor(x);
}

function wobble(base: number, pct: number, seed: number): number {
  return base * (1 + (seeded(seed) * 2 - 1) * pct);
}

function deriveStatus(hour: number, dcPowerW: number, soc: number): InverterStatus {
  if (hour < 5.8 || hour > 20.2) return 'NIGHT';
  if (dcPowerW < 50) return 'STANDBY';
  if (dcPowerW > DEMO_RATED_W * 0.95 && soc > 99) return 'CURTAILED';
  return 'PRODUCING';
}

function buildPanels(totalDcPowerW: number, hour: number, seed: number): PanelString[] {
  const sunSkew = clamp((hour - 12) / 6, -1, 1);
  return STRINGS.map((s, i) => {
    const gain = 1 + s.orientation * sunSkew;
    const fraction = (s.ratedW / DEMO_RATED_W) * gain;
    const powerW = clamp(totalDcPowerW * fraction, 0, s.ratedW);
    const voltageV = powerW > 30 ? wobble(380 + (s.id.charCodeAt(0) % 20), 0.01, seed + i) : 0;
    return {
      id: s.id,
      label: s.label,
      powerW,
      voltageV,
      currentA: voltageV > 0 ? powerW / voltageV : 0,
      ratedW: s.ratedW,
      panels: s.panels
    };
  });
}

export function demoTimestamp(hour: number): number {
  const d = new Date();
  const h = Math.floor(hour);
  const m = Math.floor((hour - h) * 60);
  d.setHours(h, m, 0, 0);
  return d.getTime();
}

export function demoTelemetryAt(hour: number, tick = 0): SolarTelemetry {
  const s = demoSampleAt(hour);
  const dcPowerW = s.acPowerW / CONVERSION;
  const heat = 28 + (dcPowerW / DEMO_RATED_W) * 21.5;
  const acVoltageV = wobble(240, 0.004, tick + 11);
  return {
    timestamp: demoTimestamp(hour),
    manufacturer: 'helios°',
    model: 'HX-9.6 Hybrid Inverter',
    serialNumber: 'HX-2025-0F31A2',
    firmware: '4.12.1',
    status: deriveStatus(hour, dcPowerW, s.batterySoc),
    acPowerW: wobble(s.acPowerW, 0.012, tick),
    acVoltageV,
    acCurrentA: s.acPowerW / acVoltageV,
    acFrequencyHz: wobble(60, 0.0006, tick + 3),
    dcPowerW,
    dcVoltageV: dcPowerW > 50 ? wobble(382, 0.008, tick + 5) : 0,
    dcCurrentA: dcPowerW > 50 ? dcPowerW / 382 : 0,
    cabinetTempC: heat - 6,
    heatsinkTempC: heat,
    energyTodayKwh: s.energyTodayKwh,
    energyMonthKwh: 412.7 + s.energyTodayKwh,
    energyLifetimeKwh: 18420.5 + s.energyTodayKwh,
    batterySoc: s.batterySoc,
    batteryPowerW: s.batteryPowerW,
    batteryHealthPct: 97.4,
    batteryCycles: 312,
    batteryCapacityKwh: DEMO_BATTERY_KWH,
    batteryTempC: 24 + Math.abs(s.batteryPowerW) / 800,
    homeLoadW: wobble(s.homeLoadW, 0.02, tick + 7),
    gridImportW: s.gridImportW,
    gridExportW: s.gridExportW,
    irradianceWm2: s.irradianceWm2,
    ambientTempC: 18 + 8 * solarFractionAt(hour),
    cloudCoverPct: demoCloudCoverAt(hour) * 100,
    panels: buildPanels(dcPowerW, hour, tick)
  };
}

export type RegisterId = 'w' | 'hz' | 'dcv' | 'tmp' | 'st' | 'ghi' | 'soc' | 'bw' | 'mw' | 'str';

export interface RegisterRow {
  id: RegisterId;
  model: string;
  point: string;
  raw: string;
  scale: string;
  reading: string;
  /** Decoded numeric value in reading units, when the point is numeric. */
  value?: number;
  unit: string;
  label: string;
}

const SUNSPEC_STATE: Record<InverterStatus, { code: number; name: string }> = {
  PRODUCING: { code: 4, name: 'MPPT' },
  STANDBY: { code: 8, name: 'STANDBY' },
  CURTAILED: { code: 5, name: 'THROTTLED' },
  NIGHT: { code: 2, name: 'SLEEPING' },
  FAULT: { code: 7, name: 'FAULT' }
};

const group = (n: number): string => Math.round(n).toLocaleString('en-US').replace(/,/g, '\u2009');

export function registerRows(t: SolarTelemetry): RegisterRow[] {
  const meterW = t.gridImportW - t.gridExportW;
  const state = SUNSPEC_STATE[t.status];
  return [
    { id: 'w', model: '101', point: 'W', raw: group(t.acPowerW), scale: '0', reading: (t.acPowerW / 1000).toFixed(2), value: t.acPowerW / 1000, unit: 'kW', label: 'AC power out' },
    { id: 'hz', model: '101', point: 'Hz', raw: group(t.acFrequencyHz * 100), scale: '−2', reading: t.acFrequencyHz.toFixed(2), value: t.acFrequencyHz, unit: 'Hz', label: 'Line frequency' },
    { id: 'dcv', model: '101', point: 'DCV', raw: group(t.dcVoltageV * 10), scale: '−1', reading: t.dcVoltageV.toFixed(1), value: t.dcVoltageV, unit: 'V', label: 'DC bus voltage' },
    { id: 'tmp', model: '101', point: 'TmpSnk', raw: group(t.heatsinkTempC * 10), scale: '−1', reading: t.heatsinkTempC.toFixed(1), value: t.heatsinkTempC, unit: '°C', label: 'Heatsink temperature' },
    { id: 'st', model: '101', point: 'St', raw: String(state.code), scale: '—', reading: state.name, unit: '', label: 'Operating state' },
    { id: 'ghi', model: '302', point: 'GHI', raw: group(t.irradianceWm2), scale: '0', reading: Math.round(t.irradianceWm2).toString(), value: Math.round(t.irradianceWm2), unit: 'W/m²', label: 'Irradiance' },
    { id: 'soc', model: '802', point: 'SoC', raw: group(t.batterySoc * 100), scale: '−2', reading: t.batterySoc.toFixed(1), value: t.batterySoc, unit: '%', label: 'Battery state of charge' },
    { id: 'bw', model: '802', point: 'W', raw: signed(t.batteryPowerW), scale: '0', reading: signedKw(t.batteryPowerW), value: t.batteryPowerW / 1000, unit: 'kW', label: t.batteryPowerW >= 0 ? 'Battery charging' : 'Battery discharging' },
    { id: 'mw', model: '203', point: 'W', raw: signed(meterW), scale: '0', reading: signedKw(meterW), value: meterW / 1000, unit: 'kW', label: meterW > 30 ? 'Grid import' : meterW < -30 ? 'Grid export' : 'Grid idle' },
    {
      id: 'str',
      model: '160',
      point: 'DCW ×3',
      raw: t.panels.map((p) => group(p.powerW)).join(' · '),
      scale: '0',
      reading: t.panels.map((p) => (p.powerW / 1000).toFixed(2)).join(' · '),
      unit: 'kW',
      label: 'String power A · B · C'
    }
  ];
}

function signed(w: number): string {
  const s = Math.round(w) > 0 ? '+' : Math.round(w) < 0 ? '−' : '';
  return `${s}${group(Math.abs(w))}`;
}

function signedKw(w: number): string {
  const s = Math.round(w) > 0 ? '+' : Math.round(w) < 0 ? '−' : '';
  return `${s}${(Math.abs(w) / 1000).toFixed(2)}`;
}

export const INSIGHT_SOURCES: Record<string, RegisterId[]> = {
  'peak-production': ['w', 'ghi', 'st'],
  'cloud-coverage': ['ghi', 'w'],
  'export-active': ['soc', 'mw'],
  'overnight-low': ['soc', 'bw'],
  thermal: ['tmp'],
  'self-consumption-high': ['w', 'mw'],
  'today-savings': ['w', 'mw'],
  'string-imbalance': ['str'],
  'morning-strategy': ['soc', 'ghi']
};

const SEVERITY_RANK: Record<Insight['severity'], number> = { critical: 0, attention: 1, positive: 2, neutral: 3 };

export function featuredInsight(insights: Insight[]): Insight | undefined {
  return [...insights].sort((a, b) => SEVERITY_RANK[a.severity] - SEVERITY_RANK[b.severity])[0];
}

export function formatDemoClock(hour: number): string {
  const h = Math.floor(((hour % 24) + 24) % 24);
  const m = Math.floor((hour - Math.floor(hour)) * 60);
  return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`;
}
