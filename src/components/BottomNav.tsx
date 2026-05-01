import { Home, Sun, Sparkles, BatteryCharging, Settings } from 'lucide-react';
import { motion } from 'framer-motion';
import { useStore } from '../store/useStore';

const TABS = [
  { id: 'dashboard', label: 'Home', icon: Home },
  { id: 'production', label: 'Solar', icon: Sun },
  { id: 'insights', label: 'Insights', icon: Sparkles },
  { id: 'battery', label: 'Battery', icon: BatteryCharging },
  { id: 'settings', label: 'Settings', icon: Settings }
] as const;

export function BottomNav() {
  const page = useStore((s) => s.page);
  const setPage = useStore((s) => s.setPage);

  return (
    <nav
      className="fixed bottom-0 inset-x-0 z-30 px-3"
      style={{ paddingBottom: 'max(env(safe-area-inset-bottom), 12px)' }}
    >
      <div className="surface-raised mx-auto max-w-md flex items-stretch justify-between px-1 py-1.5 gap-0.5 backdrop-blur-xl">
        {TABS.map((tab) => {
          const active = tab.id === page;
          const Icon = tab.icon;
          return (
            <button
              key={tab.id}
              onClick={() => setPage(tab.id)}
              className="relative flex-1 flex flex-col items-center gap-0.5 py-2 rounded-xl transition-colors"
            >
              {active && (
                <motion.div
                  layoutId="nav-active"
                  className="absolute inset-0 rounded-xl bg-carbon-700/70 border border-bone-700/30"
                  transition={{ type: 'spring', stiffness: 380, damping: 32 }}
                />
              )}
              <Icon
                size={18}
                strokeWidth={active ? 1.8 : 1.4}
                className={`relative z-10 transition-colors ${active ? 'text-bone-100' : 'text-bone-500'}`}
              />
              <span
                className={`relative z-10 text-[9px] font-mono uppercase tracking-widest transition-colors ${
                  active ? 'text-bone-100' : 'text-bone-600'
                }`}
              >
                {tab.label}
              </span>
            </button>
          );
        })}
      </div>
    </nav>
  );
}
