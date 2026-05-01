export type InverterStatus = 'PRODUCING' | 'STANDBY' | 'CURTAILED' | 'NIGHT' | 'FAULT';

export interface SolarTelemetry {
  timestamp: number;
  manufacturer: string;
  model: string;
  serialNumber: string;
  firmware: string;
  status: InverterStatus;

  acPowerW: number;
  acVoltageV: number;
  acCurrentA: number;
  acFrequencyHz: number;

  dcPowerW: number;
  dcVoltageV: number;
  dcCurrentA: number;

  cabinetTempC: number;
  heatsinkTempC: number;

  energyTodayKwh: number;
  energyMonthKwh: number;
  energyLifetimeKwh: number;

  batterySoc: number;
  batteryPowerW: number;
  batteryHealthPct: number;
  batteryCycles: number;
  batteryCapacityKwh: number;
  batteryTempC: number;

  homeLoadW: number;
  gridImportW: number;
  gridExportW: number;

  irradianceWm2: number;
  ambientTempC: number;
  cloudCoverPct: number;

  panels: PanelString[];
}

export interface PanelString {
  id: string;
  label: string;
  powerW: number;
  voltageV: number;
  currentA: number;
  ratedW: number;
  panels: number;
}

export interface HistoryPoint {
  t: number;
  productionW: number;
  consumptionW: number;
  batteryW: number;
  gridW: number;
  irradianceWm2: number;
}

export type InsightSeverity = 'positive' | 'neutral' | 'attention' | 'critical';
export type InsightCategory = 'production' | 'consumption' | 'battery' | 'savings' | 'maintenance' | 'forecast';

export interface Insight {
  id: string;
  category: InsightCategory;
  severity: InsightSeverity;
  title: string;
  body: string;
  metric?: string;
  delta?: string;
  actionLabel?: string;
}

export interface ConnectionConfig {
  protocol: 'sunspec-modbus-tcp' | 'sunspec-modbus-rtu';
  host: string;
  port: number;
  unitId: number;
  pollIntervalMs: number;
  status: 'connected' | 'simulated' | 'disconnected';
}

export interface Location {
  lat: number;
  lng: number;
  label: string;
  source: 'default' | 'browser' | 'manual';
}

export type WeatherCondition =
  | 'clear'
  | 'mostly-clear'
  | 'partly-cloudy'
  | 'overcast'
  | 'fog'
  | 'drizzle'
  | 'rain'
  | 'heavy-rain'
  | 'snow'
  | 'thunderstorm';

export interface ForecastDay {
  date: string;
  weatherCode: number;
  condition: WeatherCondition;
  conditionLabel: string;
  tempHighC: number;
  tempLowC: number;
  precipitationMm: number;
  shortwaveRadiationMJ: number;
  cloudCoverPct: number;
  expectedKwh: number;
  expectedKwhVsTypical: number;
}

export interface ProductionForecast {
  fetchedAt: number;
  location: Location;
  days: ForecastDay[];
  totalKwh: number;
  vsLastWeekPct: number;
}

export interface Brand {
  id: string;
  name: string;
  legalName?: string;
  accent: string;
  accentLight: string;
  mark: 'helios' | 'text';
  textMark?: string;
  supportEmail?: string;
  supportUrl?: string;
  tagline?: string;
}

export interface SnapshotPayload {
  v: 1;
  ts: number;
  loc: string;
  ac: number;
  todayKwh: number;
  lifeKwh: number;
  soc: number;
  selfUse: number;
  fc?: number[];
  br?: string;
}
