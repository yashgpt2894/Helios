import { useEffect } from 'react';
import { AnimatePresence, motion } from 'framer-motion';
import { useStore } from './store/useStore';
import { applyTheme } from './lib/theme';
import { applyBrandAccent } from './services/brand';
import { TopBar } from './components/TopBar';
import { BottomNav } from './components/BottomNav';
import { Dashboard } from './pages/Dashboard';
import { Production } from './pages/Production';
import { Insights } from './pages/Insights';
import { BatteryPage } from './pages/Battery';
import { SettingsPage } from './pages/Settings';

const PAGES = {
  dashboard: Dashboard,
  production: Production,
  insights: Insights,
  battery: BatteryPage,
  settings: SettingsPage
} as const;

export function MainApp() {
  const page = useStore((s) => s.page);
  const tick = useStore((s) => s.tick);
  const pollMs = useStore((s) => s.connection.pollIntervalMs);
  const loadForecast = useStore((s) => s.loadForecast);
  const theme = useStore((s) => s.theme);
  const brand = useStore((s) => s.brand);
  const setBrandFromSearch = useStore((s) => s.setBrandFromSearch);

  useEffect(() => {
    setBrandFromSearch(window.location.search);
  }, [setBrandFromSearch]);

  useEffect(() => {
    const id = setInterval(tick, pollMs);
    return () => clearInterval(id);
  }, [tick, pollMs]);

  useEffect(() => {
    void loadForecast();
    const id = setInterval(() => void loadForecast(), 1000 * 60 * 60);
    return () => clearInterval(id);
  }, [loadForecast]);

  useEffect(() => {
    if (theme !== 'auto') return;
    const mq = window.matchMedia('(prefers-color-scheme: light)');
    const handler = () => {
      applyTheme('auto');
      applyBrandAccent(brand, 'auto');
    };
    mq.addEventListener('change', handler);
    return () => mq.removeEventListener('change', handler);
  }, [theme, brand]);

  useEffect(() => {
    applyBrandAccent(brand, theme);
  }, [brand, theme]);

  const Page = PAGES[page];

  return (
    <div className="min-h-screen bg-carbon-950 text-bone-100 relative overflow-x-hidden">
      <div className="fixed inset-0 pointer-events-none aurora" />
      <div className="relative z-10 mx-auto max-w-md min-h-screen flex flex-col md:my-6 md:min-h-[calc(100vh-3rem)] md:rounded-[32px] md:border md:border-hairline md:shadow-[0_32px_64px_-12px_rgba(0,0,0,0.5)] md:overflow-hidden">
        <TopBar />
        <main className="flex-1">
          <AnimatePresence mode="wait">
            <motion.div
              key={page}
              initial={{ opacity: 0, y: 8 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -8 }}
              transition={{ duration: 0.25, ease: [0.16, 1, 0.3, 1] }}
            >
              <Page />
            </motion.div>
          </AnimatePresence>
        </main>
        <BottomNav />
      </div>
    </div>
  );
}
