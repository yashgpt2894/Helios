import { Link, useSearchParams } from 'react-router-dom';
import { motion } from 'framer-motion';
import { ArrowRight, Sparkles, Sun, Cable, BatteryCharging, Zap, ShieldCheck, MapPin } from 'lucide-react';
import { HeliosMark } from '../components/HeliosMark';
import { useStore } from '../store/useStore';
import { useEffect } from 'react';

export function Landing() {
  const [params] = useSearchParams();
  const brand = useStore((s) => s.brand);
  const setBrandFromSearch = useStore((s) => s.setBrandFromSearch);

  useEffect(() => {
    setBrandFromSearch(window.location.search);
  }, [setBrandFromSearch]);

  const search = params.toString();
  const appHref = search ? `/app?${search}` : '/app';

  const features = [
    {
      Icon: Sparkles,
      title: 'AI insights, not just charts',
      body: 'Helios reads your inverter, battery, weather, and rate plan together. It tells you when to run the dishwasher, pre-charge before storms, and which string is shading.'
    },
    {
      Icon: Sun,
      title: '7-day production forecast',
      body: 'Real solar irradiance modeling powered by Open-Meteo. See exactly how much energy your array will make tomorrow, Saturday, and the whole week.'
    },
    {
      Icon: Cable,
      title: 'Works with any inverter',
      body: 'Speaks SunSpec Modbus — the industry standard. SMA, Fronius, SolarEdge, Enphase, Schneider. One app for your whole system, even after you upgrade.'
    },
    {
      Icon: BatteryCharging,
      title: 'Battery strategy that actually thinks',
      body: 'Self-consumption, time-of-use, or backup-only — pick the mode and Helios optimizes against your forecast and rate plan automatically.'
    },
    {
      Icon: ShieldCheck,
      title: 'Local-first, privacy-respecting',
      body: 'Telemetry stays on your network. Coordinates only go to Open-Meteo for the forecast. No third-party tracking, no behavioral ads.'
    },
    {
      Icon: Zap,
      title: 'Installs in 60 seconds',
      body: 'A Progressive Web App — no App Store. Tap "Add to Home Screen" and you are done. Works offline after first load.'
    }
  ];

  return (
    <div className="min-h-screen bg-carbon-950 text-bone-100 relative overflow-x-hidden">
      <div className="fixed inset-0 pointer-events-none aurora" />
      <div className="relative z-10 mx-auto max-w-2xl px-6 py-12">
        <header className="flex items-center justify-between mb-16">
          <div className="flex items-center gap-2.5">
            <HeliosMark size={32} brand={brand} />
            <span className="text-bone-100 text-[15px] font-medium">{brand.name}</span>
          </div>
          <Link
            to={appHref}
            className="text-[11px] font-mono uppercase tracking-widest text-bone-300 hover:text-bone-100 transition-colors"
          >
            Open app →
          </Link>
        </header>

        <motion.section
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, ease: [0.16, 1, 0.3, 1] }}
          className="mb-20"
        >
          <span className="label-cap">{brand.id === 'helios' ? 'Solar intelligence' : `By ${brand.name}`}</span>
          <h1 className="num-display text-bone-100 text-6xl md:text-7xl leading-[0.95] mt-3 tracking-tight">
            Your solar array,
            <br />
            <span className="italic text-shimmer">finally explained.</span>
          </h1>
          <p className="text-bone-300 text-[16px] md:text-[17px] mt-6 max-w-xl leading-relaxed">
            {brand.tagline} {brand.id === 'helios' ? 'Live telemetry from any SunSpec-compatible inverter, plus AI insights that tell you what to do — not just what happened.' : ''}
          </p>
          <div className="mt-8 flex flex-wrap gap-3">
            <Link
              to={appHref}
              className="inline-flex items-center gap-2 px-5 py-3 rounded-full bg-bone-100 text-carbon-900 hover:bg-bone-200 transition-colors text-[13px] font-medium"
            >
              Open the app
              <ArrowRight size={14} strokeWidth={1.8} />
            </Link>
            <a
              href="#how-it-works"
              className="inline-flex items-center gap-2 px-5 py-3 rounded-full border border-hairline text-bone-200 hover:text-bone-100 transition-colors text-[13px]"
            >
              How it works
            </a>
          </div>
        </motion.section>

        <section id="how-it-works" className="mb-20">
          <span className="label-cap">What you get</span>
          <h2 className="text-bone-100 text-3xl md:text-4xl mt-2 font-medium tracking-tight mb-10">
            Six things your inverter app won't do.
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
            {features.map((f, i) => (
              <motion.div
                key={f.title}
                initial={{ opacity: 0, y: 12 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true, margin: '-50px' }}
                transition={{ duration: 0.4, delay: i * 0.05, ease: [0.16, 1, 0.3, 1] }}
                className="surface p-5"
              >
                <div className="size-9 rounded-full bg-carbon-800 border border-hairline flex items-center justify-center text-signal-solar mb-3">
                  <f.Icon size={16} strokeWidth={1.5} />
                </div>
                <h3 className="text-bone-100 text-[15px] font-medium mb-1.5">{f.title}</h3>
                <p className="text-bone-400 text-[13px] leading-relaxed">{f.body}</p>
              </motion.div>
            ))}
          </div>
        </section>

        <section className="mb-20 surface-raised p-8 text-center">
          <div className="flex justify-center mb-4">
            <HeliosMark size={56} brand={brand} />
          </div>
          <h2 className="num-display text-bone-100 text-3xl md:text-4xl tracking-tight">
            Start using {brand.name} today.
          </h2>
          <p className="text-bone-400 text-[14px] mt-3 max-w-md mx-auto">
            The app loads in simulation mode by default — explore every screen with realistic sample data before connecting your inverter.
          </p>
          <Link
            to={appHref}
            className="inline-flex items-center gap-2 px-6 py-3 rounded-full bg-bone-100 text-carbon-900 hover:bg-bone-200 transition-colors text-[14px] font-medium mt-6"
          >
            Open the app
            <ArrowRight size={14} strokeWidth={1.8} />
          </Link>
        </section>

        <footer className="flex flex-col md:flex-row items-start md:items-center justify-between gap-4 pt-8 border-t border-hairline text-bone-500 text-[11px] font-mono">
          <div className="flex items-center gap-2">
            <HeliosMark size={20} brand={brand} />
            <span>{brand.legalName ?? brand.name}</span>
          </div>
          <div className="flex items-center gap-1.5">
            <MapPin size={11} strokeWidth={1.6} />
            <span>{brand.id === 'helios' ? 'Made for the sun · 2026' : `Powered by helios° · ${brand.name}`}</span>
          </div>
        </footer>
      </div>
    </div>
  );
}
