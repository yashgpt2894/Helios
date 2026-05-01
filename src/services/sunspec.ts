import type { HistoryPoint, InverterStatus, PanelString, SolarTelemetry } from '../types';
import { clamp } from '../lib/format';
import { consumptionFractionAt, irradianceAt, nowAsHourFloat, solarFractionAt } from '../lib/solarCurve';

const SYSTEM_RATED_W = 9600;
const BATTERY_CAPACITY_KWH = 13.5;
const STRINGS: { id: string; label: string; ratedW: number; panels: number; orientation: number }[] = [
  { id: 'A', label: 'Roof · South-East', ratedW: 3200, panels: 8, orientation: -0.15 },
  { id: 'B', label: 'Roof · South-West', ratedW: 3200, panels: 8, orientation: 0.15 },
  { id: 'C', label: 'Garage · South', ratedW: 3200, panels: 8, orientation: 0 }
];

let cachedBattery = 62;
let lifetimeKwh = 18420.5;
let monthKwh = 412.7;
let lastSampleAt = Date.now();

function jitter(base: number, pct: number): number {
  return base * (1 + (Math.random() * 2 - 1) * pct);
}

function buildPanels(totalDcPowerW: number, hour: number): PanelString[] {
  const sunSkew = clamp((hour - 12) / 6, -1, 1);
  return STRINGS.map((s) => {
    const orientationGain = 1 + s.orientation * sunSkew;
    const fractionOfRated = (s.ratedW / SYSTEM_RATED_W) * orientationGain;
    const power = clamp(totalDcPowerW * fractionOfRated, 0, s.ratedW);
    const voltage = power > 30 ? jitter(380 + s.id.charCodeAt(0) % 20, 0.01) : 0;
    const current = voltage > 0 ? power / voltage : 0;
    return {
      id: s.id,
      label: s.label,
      powerW: power,
      voltageV: voltage,
      currentA: current,
      ratedW: s.ratedW,
      panels: s.panels
    };
  });
}

function deriveStatus(hour: number, dcPowerW: number, batterySoc: number): InverterStatus {
  if (hour < 5.8 || hour > 20.2) return 'NIGHT';
  if (dcPowerW < 50) return 'STANDBY';
  if (dcPowerW > SYSTEM_RATED_W * 0.95 && batterySoc > 99) return 'CURTAILED';
  return 'PRODUCING';
}

export function readTelemetry(): SolarTelemetry {
  const now = new Date();
  const hour = nowAsHourFloat(now);
  const cloudCover = 0.18 + 0.12 * Math.sin(now.getTime() / 1000 / 240);

  const irradiance = irradianceAt(hour, cloudCover);
  const dcPowerIdeal = (irradiance / 1000) * SYSTEM_RATED_W;
  const dcPowerW = jitter(Math.max(0, dcPowerIdeal), 0.03);

  const conversionEfficiency = 0.964;
  const acPowerW = dcPowerW * conversionEfficiency;

  const homeLoadIdeal = consumptionFractionAt(hour) * 4200;
  const homeLoadW = jitter(homeLoadIdeal, 0.07);

  const surplusW = acPowerW - homeLoadW;

  const dt = (now.getTime() - lastSampleAt) / 1000;
  lastSampleAt = now.getTime();

  let batteryPowerW = 0;
  let gridImportW = 0;
  let gridExportW = 0;

  if (surplusW >= 0) {
    if (cachedBattery < 100) {
      batteryPowerW = Math.min(surplusW, 5000);
      cachedBattery += (batteryPowerW * (dt / 3600)) / (BATTERY_CAPACITY_KWH * 10);
      const remaining = surplusW - batteryPowerW;
      gridExportW = remaining;
    } else {
      gridExportW = surplusW;
    }
  } else {
    const deficit = -surplusW;
    if (cachedBattery > 12) {
      batteryPowerW = -Math.min(deficit, 5000);
      cachedBattery += (batteryPowerW * (dt / 3600)) / (BATTERY_CAPACITY_KWH * 10);
      const stillNeeded = deficit - Math.abs(batteryPowerW);
      gridImportW = Math.max(0, stillNeeded);
    } else {
      gridImportW = deficit;
    }
  }

  cachedBattery = clamp(cachedBattery, 0, 100);

  const dcVoltageV = dcPowerW > 50 ? jitter(382, 0.012) : 0;
  const dcCurrentA = dcVoltageV > 0 ? dcPowerW / dcVoltageV : 0;
  const acVoltageV = jitter(240, 0.005);
  const acCurrentA = acPowerW / acVoltageV;
  const acFrequencyHz = jitter(60, 0.0008);

  const baseHeat = 28 + (dcPowerW / SYSTEM_RATED_W) * 22;
  const heatsinkTempC = jitter(baseHeat, 0.04);
  const cabinetTempC = jitter(baseHeat - 6, 0.04);
  const ambientTempC = jitter(18 + 8 * solarFractionAt(hour), 0.03);
  const batteryTempC = jitter(24 + Math.abs(batteryPowerW) / 800, 0.03);

  const energyAddedKwh = (acPowerW * (dt / 3600)) / 1000;
  lifetimeKwh += Math.max(0, energyAddedKwh);
  monthKwh += Math.max(0, energyAddedKwh);
  const energyTodayKwh = computeTodayKwh(hour, cloudCover);

  return {
    timestamp: now.getTime(),
    manufacturer: 'helios°',
    model: 'HX-9.6 Hybrid Inverter',
    serialNumber: 'HX-2025-0F31A2',
    firmware: '4.12.1',
    status: deriveStatus(hour, dcPowerW, cachedBattery),

    acPowerW,
    acVoltageV,
    acCurrentA,
    acFrequencyHz,

    dcPowerW,
    dcVoltageV,
    dcCurrentA,

    cabinetTempC,
    heatsinkTempC,

    energyTodayKwh,
    energyMonthKwh: monthKwh,
    energyLifetimeKwh: lifetimeKwh,

    batterySoc: cachedBattery,
    batteryPowerW,
    batteryHealthPct: 97.4,
    batteryCycles: 312,
    batteryCapacityKwh: BATTERY_CAPACITY_KWH,
    batteryTempC,

    homeLoadW,
    gridImportW,
    gridExportW,

    irradianceWm2: irradiance,
    ambientTempC,
    cloudCoverPct: cloudCover * 100,

    panels: buildPanels(dcPowerW, hour)
  };
}

function computeTodayKwh(currentHour: number, cloudCover: number): number {
  let total = 0;
  const step = 0.25;
  for (let h = 0; h <= currentHour; h += step) {
    const w = (irradianceAt(h, cloudCover) / 1000) * SYSTEM_RATED_W * 0.964;
    total += (w * step) / 1000;
  }
  return total;
}

export function buildTodaySeries(): HistoryPoint[] {
  const points: HistoryPoint[] = [];
  const now = new Date();
  const hourNow = nowAsHourFloat(now);
  const cloudCover = 0.2;
  for (let h = 0; h <= 24; h += 0.5) {
    const irr = irradianceAt(h, cloudCover);
    const ideal = (irr / 1000) * SYSTEM_RATED_W * 0.964;
    const isPast = h <= hourNow;
    const productionW = isPast ? ideal * (1 + (Math.sin(h * 3) * 0.05)) : ideal;
    const consumptionW = consumptionFractionAt(h) * 4200;
    const surplus = productionW - consumptionW;
    const batteryW = clamp(surplus, -5000, 5000) * 0.7;
    const gridW = surplus - batteryW;
    points.push({
      t: h,
      productionW,
      consumptionW,
      batteryW,
      gridW,
      irradianceWm2: irr
    });
  }
  return points;
}

export function buildWeekSeries(): { day: string; produced: number; consumed: number }[] {
  const days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
  return days.map((day, i) => {
    const factor = 0.85 + 0.18 * Math.sin(i * 1.3);
    const produced = 38 * factor + (Math.random() * 4 - 2);
    const consumed = 28 + (Math.random() * 6 - 3);
    return { day, produced: Math.max(0, produced), consumed: Math.max(0, consumed) };
  });
}
