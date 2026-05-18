import { Link, useSearchParams } from 'react-router-dom';
import { motion } from 'framer-motion';
import { ArrowRight, Sparkles, Sun, Cable, BatteryCharging, Zap, ShieldCheck, Apple, Play } from 'lucide-react';
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

      <div className="relative z-10">
        <header className="mx-auto max-w-7xl px-6 py-8 flex items-center justify-between">
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

        <section className="mx-auto max-w-7xl px-6 editorial-pad">
          <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-12">
            <div className="flex-1">
              <span className="label-cap">{brand.id === 'helios' ? 'Solar intelligence' : `By ${brand.name}`}</span>
              <div className="mt-4">
                <div className="text-behind">ENERGY</div>
                <div className="text-front">intelligence, daylight-driven.</div>
              </div>
              <p className="text-bone-300 text-[16px] md:text-[17px] mt-8 max-w-lg leading-relaxed">
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
                  href="#features"
                  className="inline-flex items-center gap-2 px-5 py-3 rounded-full border border-hairline text-bone-200 hover:text-bone-100 transition-colors text-[13px]"
                >
                  How it works
                </a>
              </div>
            </div>

            <div className="hidden md:flex shrink-0 justify-center">
              <div
                className="relative rounded-[40px] border-[10px] p-1.5"
                style={{ borderColor: '#1a1a1c', background: '#0b0b0c', width: 280, boxShadow: '0 32px 64px -20px rgba(0,0,0,0.7)' }}
              >
                <div
                  className="absolute top-0 left-1/2 -translate-x-1/2 z-20"
                  style={{ width: 100, height: 24, background: '#0b0b0c', borderBottomLeftRadius: 14, borderBottomRightRadius: 14 }}
                />
                <div className="rounded-[32px] overflow-hidden bg-carbon-900">
                  <img
                    src="/mobile-dashboard-viewport.png"
                    alt="Helios dashboard preview"
                    className="w-full h-auto block"
                    loading="eager"
                    width={260}
                    height={560}
                  />
                </div>
              </div>
            </div>
          </div>
        </section>

        <section className="bg-bone-100 text-carbon-950 editorial-pad">
          <div className="mx-auto max-w-7xl px-6">
            <span className="label-cap">ABOUT HELIOS</span>
            <h2 className="num-display text-4xl md:text-5xl mt-4 max-w-2xl tracking-tight">
              Where photons meet intelligence.
            </h2>
            <p className="text-carbon-700 text-[16px] md:text-[17px] mt-6 max-w-xl leading-relaxed">
              {brand.tagline} Every watt is tracked, every forecast is tuned to your roof, and every insight is built to save you money.
            </p>
          </div>
        </section>

        <section id="features" className="editorial-pad">
          <div className="mx-auto max-w-7xl px-6">
            <span className="label-cap">What you get</span>
            <h2 className="num-display text-bone-100 text-3xl md:text-4xl mt-2 font-medium tracking-tight mb-12">
              Six things your inverter app won&apos;t do.
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {features.map((f, i) => (
                <motion.div
                  key={f.title}
                  initial={{ opacity: 0, y: 12 }}
                  whileInView={{ opacity: 1, y: 0 }}
                  viewport={{ once: true, margin: '-50px' }}
                  transition={{ duration: 0.4, delay: i * 0.05, ease: [0.16, 1, 0.3, 1] }}
                  className="surface p-6"
                >
                  <div className="flex items-start justify-between gap-4">
                    <div className="size-9 rounded-full bg-carbon-800 border border-hairline flex items-center justify-center text-signal-solar shrink-0">
                      <f.Icon size={16} strokeWidth={1.5} />
                    </div>
                    <span className="num-display text-signal-solar text-3xl leading-none opacity-60">
                      {String(i + 1).padStart(2, '0')}
                    </span>
                  </div>
                  <h3 className="text-bone-100 text-[16px] font-medium mt-4 mb-2">{f.title}</h3>
                  <p className="text-bone-400 text-[13px] leading-relaxed">{f.body}</p>
                </motion.div>
              ))}
            </div>
          </div>
        </section>

        <section className="editorial-pad">
          <div className="mx-auto max-w-7xl px-6">
            <div className="flex flex-col md:flex-row md:items-center gap-12">
              <div className="flex-1">
                <span className="label-cap">Live preview</span>
                <h2 className="num-display text-bone-100 text-4xl md:text-5xl mt-4 tracking-tight">
                  Watch it,{' '}
                  <span className="italic text-signal-solar">Optimize</span> it.
                </h2>
                <p className="text-bone-300 text-[16px] md:text-[17px] mt-6 max-w-lg leading-relaxed">
                  AI insights surface the moment your data changes. Share a live snapshot with your installer or partner — no account, no cloud, just a link.
                </p>
                <div className="mt-8">
                  <Link
                    to={appHref}
                    className="inline-flex items-center gap-2 px-5 py-3 rounded-full bg-bone-100 text-carbon-900 hover:bg-bone-200 transition-colors text-[13px] font-medium"
                  >
                    Open the app
                    <ArrowRight size={14} strokeWidth={1.8} />
                  </Link>
                </div>
              </div>
              <div className="flex justify-center md:justify-end">
                <div
                  className="relative rounded-[40px] border-[10px] p-1.5"
                  style={{
                    borderColor: '#1a1a1c',
                    background: '#0b0b0c',
                    width: 260,
                    transform: 'rotate(6deg)',
                    boxShadow: '0 32px 64px -20px rgba(0,0,0,0.7)'
                  }}
                >
                  <div
                    className="absolute top-0 left-1/2 -translate-x-1/2 z-20"
                    style={{ width: 100, height: 24, background: '#0b0b0c', borderBottomLeftRadius: 14, borderBottomRightRadius: 14 }}
                  />
                  <div className="rounded-[32px] overflow-hidden bg-carbon-900">
                    <img
                      src="/mobile-dashboard-full.png"
                      alt="Helios full dashboard"
                      className="w-full h-auto block"
                      loading="lazy"
                      width={240}
                      height={520}
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>

        <section className="editorial-pad border-t border-hairline">
          <div className="mx-auto max-w-7xl px-6">
            <h2 className="num-display text-bone-100 text-3xl md:text-4xl tracking-tight mb-10">
              Get {brand.name} on your phone.
            </h2>
            <div className="flex flex-col md:flex-row items-start md:items-center gap-6 md:gap-10">
              <div className="flex flex-wrap gap-4">
                <div className="flex flex-col items-center gap-2">
                  <div
                    aria-disabled="true"
                    tabIndex={-1}
                    className="inline-flex items-center gap-3 px-5 py-3 rounded-xl bg-carbon-900 border border-hairline cursor-not-allowed opacity-70 select-none"
                    style={{ minWidth: 180 }}
                  >
                    <Apple size={24} strokeWidth={1.5} />
                    <div className="flex flex-col leading-tight">
                      <span className="text-[10px] font-medium">Download on the</span>
                      <span className="text-[15px] font-semibold">App Store</span>
                    </div>
                  </div>
                  <span className="text-[10px] font-mono uppercase tracking-widest text-bone-500">Coming soon</span>
                </div>
                <div className="flex flex-col items-center gap-2">
                  <div
                    aria-disabled="true"
                    tabIndex={-1}
                    className="inline-flex items-center gap-3 px-5 py-3 rounded-xl bg-carbon-900 border border-hairline cursor-not-allowed opacity-70 select-none"
                    style={{ minWidth: 180 }}
                  >
                    <Play size={24} strokeWidth={1.5} />
                    <div className="flex flex-col leading-tight">
                      <span className="text-[10px] font-medium">Get it on</span>
                      <span className="text-[15px] font-semibold">Google Play</span>
                    </div>
                  </div>
                  <span className="text-[10px] font-mono uppercase tracking-widest text-bone-500">Coming soon</span>
                </div>
              </div>
              <div className="md:ml-auto">
                <Link
                  to={appHref}
                  className="text-[13px] font-medium text-bone-200 hover:text-bone-100 transition-colors underline underline-offset-4"
                >
                  Open the web app →
                </Link>
              </div>
            </div>
          </div>
        </section>

        <footer className="relative overflow-hidden border-t border-hairline">
          <div className="relative mx-auto max-w-7xl px-6 pt-12">
            <div className="flex justify-center overflow-hidden">
              <h2
                className="num-display font-medium leading-[0.75] select-none text-center"
                style={{ fontSize: 'clamp(6rem, 22vw, 20rem)', color: 'var(--text-100)', opacity: 0.07 }}
              >
                {brand.name}
              </h2>
            </div>
          </div>
          <div className="mx-auto max-w-7xl px-6 py-8 flex flex-col md:flex-row items-center justify-between gap-3">
            <div className="flex items-center gap-2 text-bone-500 text-[11px] font-mono">
              <HeliosMark size={20} brand={brand} />
              <span>{brand.legalName ?? brand.name}</span>
            </div>
            <div className="flex items-center gap-4 text-bone-500 text-[11px] font-mono">
              <span>2026</span>
              <a
                href="https://github.com/yashgpt2894/Helios"
                target="_blank"
                rel="noopener noreferrer"
                className="hover:text-bone-300 transition-colors"
              >
                GitHub
              </a>
            </div>
          </div>
        </footer>
      </div>
    </div>
  );
}
