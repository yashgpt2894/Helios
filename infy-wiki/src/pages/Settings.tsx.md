# Settings

```tsx
import { useStore } from '../store/useStore';
import { SectionHeader } from '../components/SectionHeader';
import { HeliosMark } from '../components/HeliosMark';
import { Wifi, Cpu, Info, Bell, Lock, ChevronRight, RefreshCw, MapPin, Sun, Moon, MonitorCog } from 'lucide-react';
import type { Theme } from '../lib/theme';

export function SettingsPage() {
  const t = useStore((s) => s.telemetry);
  const conn = useStore((s) => s.connection);
  const updateConnection = useStore((s) => s.updateConnection);
  const location = useStore((s) => s.location);
  const forecastStatus = useStore((s) => s.forecastStatus);
  const useMyLocation = useStore((s) => s.useMyLocation);
  const theme = useStore((s) => s.theme);
  const setTheme = useStore((s) => s.setTheme);

  return (
    <div className="px-5 pb-32 pt-2 space-y-5">
      <header className="px-1">
        <span className="label-cap">System</span>
        <h1 className="text-bone-100 text-[28px] font-medium tracking-tight mt-1">Settings</h1>
      </header>

      <section className="surface-raised p-5 flex items-center gap-4">
        <HeliosMark size={56} />
        <div className="flex-1">
          <div className="text-bone-100 text-[14px] font-medium">{t.model}</div>
          <div className="num-mono text-bone-500 text-[11px] mt-0.5">
            S/N {t.serialNumber} · fw {t.firmware}
          </div>
        </div>
      </section>

      <section>
        <SectionHeader
          eyebrow="connection"
          title="SunSpec Modbus"
          description="MVP supports SunSpec — the industry-standard protocol for inverters."
        />
        <div className="surface p-4 space-y-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2.5">
              <div className="size-9 rounded-full bg-carbon-700 border border-hairline flex items-center justify-center text-bone-300">
                <Wifi size={15} strokeWidth={1.6} />
              </div>
              <div>
                <div className="text-bone-100 text-[13px] font-medium">
                  {conn.status === 'simulated' ? 'Simulation mode' : 'Live link'}
                </div>
                <div className="num-mono text-bone-500 text-[11px]">
                  {conn.host}:{conn.port} · unit {conn.unitId}
                </div>
              </div>
            </div>
            <span
              className={`text-[10px] font-mono uppercase tracking-widest ${
                conn.status === 'connected'
                  ? 'text-signal-flow'
                  : conn.status === 'simulated'
                  ? 'text-signal-solar'
                  : 'text-signal-alert'
              }`}
            >
              {conn.status}
            </span>
          </div>

          <div className="grid grid-cols-2 gap-3 pt-3 border-t border-hairline">
            <SettingField
              label="Host"
              value={conn.host}
              onChange={(v) => updateConnection({ host: v })}
            />
            <SettingField
              label="Port"
              value={conn.port.toString()}
              onChange={(v) => updateConnection({ port: parseInt(v) || 502 })}
            />
            <SettingField
              label="Unit ID"
              value={conn.unitId.toString()}
              onChange={(v) => updateConnection({ unitId: parseInt(v) || 1 })}
            />
            <SettingField
              label="Poll (ms)"
              value={conn.pollIntervalMs.toString()}
              onChange={(v) => updateConnection({ pollIntervalMs: parseInt(v) || 2000 })}
            />
          </div>

          <button
            onClick={() => updateConnection({ status: conn.status === 'connected' ? 'simulated' : 'connected' })}
            className="w-full surface px-4 py-3 flex items-center justify-center gap-2 text-bone-100 text-[13px] font-medium hover:border-bone-700/40 transition-colors"
          >
            <RefreshCw size={14} strokeWidth={1.6} />
            {conn.status === 'connected' ? 'Switch to simulation' : 'Test connection'}
          </button>
        </div>
      </section>

      <section>
        <SectionHeader
          eyebrow="location"
          title="Forecast location"
          description="Used only to query Open-Meteo for solar irradiance. Coords stay on-device otherwise."
        />
        <div className="surface p-4">
          <div className="flex items-center gap-3">
            <div className="size-9 rounded-full bg-carbon-700 border border-hairline flex items-center justify-center text-bone-300">
              <MapPin size={15} strokeWidth={1.6} />
            </div>
            <div className="flex-1">
              <div className="text-bone-100 text-[13px] font-medium">{location.label}</div>
              <div className="num-mono text-bone-500 text-[11px] mt-0.5">
                {location.lat.toFixed(4)}, {location.lng.toFixed(4)} · {location.source}
              </div>
            </div>
            <button
              onClick={() => void useMyLocation()}
              disabled={forecastStatus === 'loading'}
              className="text-[11px] font-mono uppercase tracking-widest text-bone-300 hover:text-bone-100 disabled:opacity-50 transition-colors px-3 py-1.5 rounded-full border border-hairline"
            >
              {forecastStatus === 'loading' ? 'detecting…' : 'use mine'}
            </button>
          </div>
        </div>
      </section>

      <section>
        <SectionHeader eyebrow="appearance" title="Theme" description="Choose carbon, paper, or follow your system." />
        <div className="surface p-2">
          <div className="grid grid-cols-3 gap-1.5">
            {([
              { id: 'dark', label: 'Carbon', sub: 'dark', Icon: Moon },
              { id: 'light', label: 'Paper', sub: 'light', Icon: Sun },
              { id: 'auto', label: 'Auto', sub: 'system', Icon: MonitorCog }
            ] as const).map((opt) => {
              const active = theme === opt.id;
              return (
                <button
                  key={opt.id}
                  onClick={() => setTheme(opt.id as Theme)}
                  className={`flex flex-col items-center gap-1 py-3 rounded-xl border transition-colors ${
                    active
                      ? 'border-bone-700/50 bg-carbon-700/40'
                      : 'border-transparent hover:bg-carbon-800/40'
                  }`}
                >
                  <opt.Icon size={18} strokeWidth={1.4} className={active ? 'text-bone-100' : 'text-bone-400'} />
                  <span className={`text-[12px] font-medium ${active ? 'text-bone-100' : 'text-bone-300'}`}>
                    {opt.label}
                  </span>
                  <span className="text-[9px] font-mono uppercase tracking-widest text-bone-500">{opt.sub}</span>
                </button>
              );
            })}
          </div>
        </div>
      </section>

      <section>
        <SectionHeader eyebrow="preferences" title="App" />
        <div className="surface divide-y divide-bone-700/10">
          <Row icon={<Bell size={15} strokeWidth={1.6} />} label="Notifications" sub="Anomalies & weekly digest" />
          <Row icon={<Cpu size={15} strokeWidth={1.6} />} label="Data residency" sub="Local-only · no cloud" trailing="On" />
          <Row icon={<Lock size={15} strokeWidth={1.6} />} label="App lock" sub="Face ID on launch" />
          <Row icon={<Info size={15} strokeWidth={1.6} />} label="About helios°" sub="MVP v0.1" />
        </div>
      </section>

      <p className="text-center text-bone-700 text-[10px] font-mono uppercase tracking-widest pt-2">
        helios° · precision energy intelligence
      </p>
    </div>
  );
}

function SettingField({
  label,
  value,
  onChange
}: {
  label: string;
  value: string;
  onChange: (v: string) => void;
}) {
  return (
    <label className="flex flex-col gap-1.5">
      <span className="label-cap">{label}</span>
      <input
        value={value}
        onChange={(e) => onChange(e.target.value)}
        className="bg-carbon-900/80 border border-hairline rounded-lg px-3 py-2 text-bone-100 text-[13px] font-mono focus:outline-none focus:border-bone-700/40 transition-colors"
      />
    </label>
  );
}

function Row({
  icon,
  label,
  sub,
  trailing
}: {
  icon: React.ReactNode;
  label: string;
  sub?: string;
  trailing?: string;
}) {
  return (
    <button className="w-full flex items-center gap-3 px-4 py-3.5 hover:bg-carbon-800/40 transition-colors">
      <div className="size-8 rounded-full bg-carbon-700/60 border border-hairline flex items-center justify-center text-bone-300">
        {icon}
      </div>
      <div className="flex-1 text-left">
        <div className="text-bone-100 text-[13px] font-medium">{label}</div>
        {sub && <div className="text-bone-500 text-[11px] mt-0.5">{sub}</div>}
      </div>
      {trailing && <span className="text-bone-400 text-[11px] font-mono">{trailing}</span>}
      <ChevronRight size={14} strokeWidth={1.6} className="text-bone-600" />
    </button>
  );
}

```