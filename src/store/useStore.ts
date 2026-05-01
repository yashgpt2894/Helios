import { create } from 'zustand';
import type { Brand, ConnectionConfig, HistoryPoint, Location, ProductionForecast, SolarTelemetry } from '../types';
import { buildTodaySeries, readTelemetry } from '../services/sunspec';
import { fetchForecast, getDefaultLocation, requestBrowserLocation } from '../services/weather';
import { applyTheme, loadTheme, persistTheme, type Theme } from '../lib/theme';
import { applyBrandAccent, brandFromSearch, resolveBrand } from '../services/brand';

type Page = 'dashboard' | 'production' | 'insights' | 'battery' | 'settings';

type ForecastStatus = 'idle' | 'loading' | 'ready' | 'error';

interface State {
  page: Page;
  setPage: (p: Page) => void;
  telemetry: SolarTelemetry;
  todaySeries: HistoryPoint[];
  liveSeries: HistoryPoint[];
  connection: ConnectionConfig;
  updateConnection: (patch: Partial<ConnectionConfig>) => void;
  tick: () => void;
  location: Location;
  forecast: ProductionForecast | null;
  forecastStatus: ForecastStatus;
  forecastError: string | null;
  loadForecast: () => Promise<void>;
  useMyLocation: () => Promise<void>;
  theme: Theme;
  setTheme: (t: Theme) => void;
  cycleTheme: () => void;
  brand: Brand;
  setBrandFromSearch: (search: string) => void;
}

const initial = readTelemetry();

export const useStore = create<State>((set, get) => ({
  page: 'dashboard',
  setPage: (p) => set({ page: p }),
  telemetry: initial,
  todaySeries: buildTodaySeries(),
  liveSeries: [
    {
      t: Date.now(),
      productionW: initial.acPowerW,
      consumptionW: initial.homeLoadW,
      batteryW: initial.batteryPowerW,
      gridW: initial.gridImportW - initial.gridExportW,
      irradianceWm2: initial.irradianceWm2
    }
  ],
  connection: {
    protocol: 'sunspec-modbus-tcp',
    host: '192.168.1.42',
    port: 502,
    unitId: 1,
    pollIntervalMs: 2000,
    status: 'simulated'
  },
  updateConnection: (patch) => set({ connection: { ...get().connection, ...patch } }),
  tick: () => {
    const t = readTelemetry();
    const live = get().liveSeries;
    const next = [
      ...live,
      {
        t: t.timestamp,
        productionW: t.acPowerW,
        consumptionW: t.homeLoadW,
        batteryW: t.batteryPowerW,
        gridW: t.gridImportW - t.gridExportW,
        irradianceWm2: t.irradianceWm2
      }
    ].slice(-180);
    set({ telemetry: t, liveSeries: next });
  },
  location: getDefaultLocation(),
  forecast: null,
  forecastStatus: 'idle',
  forecastError: null,
  loadForecast: async () => {
    const { location } = get();
    set({ forecastStatus: 'loading', forecastError: null });
    try {
      const forecast = await fetchForecast(location);
      set({ forecast, forecastStatus: 'ready' });
    } catch (e) {
      set({ forecastStatus: 'error', forecastError: e instanceof Error ? e.message : 'Forecast unavailable' });
    }
  },
  useMyLocation: async () => {
    set({ forecastStatus: 'loading', forecastError: null });
    const browserLoc = await requestBrowserLocation();
    if (!browserLoc) {
      set({ forecastStatus: 'error', forecastError: 'Location permission denied' });
      return;
    }
    set({ location: browserLoc });
    try {
      const forecast = await fetchForecast(browserLoc);
      set({ forecast, forecastStatus: 'ready' });
    } catch (e) {
      set({ forecastStatus: 'error', forecastError: e instanceof Error ? e.message : 'Forecast unavailable' });
    }
  },
  theme: loadTheme(),
  setTheme: (t) => {
    persistTheme(t);
    applyTheme(t);
    set({ theme: t });
  },
  cycleTheme: () => {
    const order: Theme[] = ['dark', 'light', 'auto'];
    const current = get().theme;
    const next = order[(order.indexOf(current) + 1) % order.length];
    persistTheme(next);
    applyTheme(next);
    applyBrandAccent(get().brand, next);
    set({ theme: next });
  },
  brand: typeof window !== 'undefined' ? brandFromSearch(window.location.search) : resolveBrand(null),
  setBrandFromSearch: (search) => {
    const brand = brandFromSearch(search);
    applyBrandAccent(brand, get().theme);
    set({ brand });
  }
}));
