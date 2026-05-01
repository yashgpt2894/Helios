import { Sun, Moon, MonitorCog } from 'lucide-react';
import { HeliosMark } from './HeliosMark';
import { useTickingTime } from '../hooks/useTickingTime';
import { useStore } from '../store/useStore';

export function TopBar() {
  const time = useTickingTime();
  const status = useStore((s) => s.connection.status);
  const theme = useStore((s) => s.theme);
  const cycleTheme = useStore((s) => s.cycleTheme);
  const brand = useStore((s) => s.brand);
  const dot =
    status === 'connected' ? 'bg-signal-flow' : status === 'simulated' ? 'bg-signal-solar' : 'bg-signal-alert';

  const ThemeIcon = theme === 'light' ? Sun : theme === 'dark' ? Moon : MonitorCog;
  const themeLabel = theme === 'auto' ? 'auto theme' : `${theme} theme`;

  return (
    <header className="px-5 pt-[max(env(safe-area-inset-top),16px)] pb-3 flex items-center justify-between">
      <div className="flex items-center gap-2.5">
        <HeliosMark size={32} brand={brand} />
        <div className="flex flex-col leading-tight">
          <span className="text-bone-100 text-[15px] font-medium">{brand.name}</span>
          <span className="text-bone-500 text-[10px] font-mono uppercase tracking-widest flex items-center gap-1.5">
            <span className={`size-1 rounded-full ${dot}`} />
            {status === 'simulated' ? 'simulated' : status}
          </span>
        </div>
      </div>
      <div className="flex items-center gap-2">
        <div className="px-2.5 py-1.5 rounded-full border border-hairline bg-carbon-800/60 flex items-center gap-1.5">
          <span className="text-bone-300 text-[11px] font-mono">
            {time.toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit' })}
          </span>
        </div>
        <button
          onClick={cycleTheme}
          aria-label={`Switch theme — currently ${themeLabel}`}
          title={themeLabel}
          className="size-9 rounded-full border border-hairline bg-carbon-800/60 flex items-center justify-center text-bone-300 hover:text-bone-100 transition-colors"
        >
          <ThemeIcon size={14} strokeWidth={1.6} />
        </button>
      </div>
    </header>
  );
}
