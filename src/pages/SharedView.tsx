import { Link, useParams, useSearchParams } from 'react-router-dom';
import { motion } from 'framer-motion';
import { ArrowRight, MapPin, Sun, Home, BatteryCharging, ExternalLink } from 'lucide-react';
import { decodeSnapshot } from '../services/share';
import { resolveBrand } from '../services/brand';
import { HeliosMark } from '../components/HeliosMark';
import { ShareButtonPlain } from '../components/ShareButton';
import { useEffect } from 'react';
import { useStore } from '../store/useStore';

export function SharedView() {
  const { payload } = useParams<{ payload: string }>();
  const [params] = useSearchParams();
  const setBrandFromSearch = useStore((s) => s.setBrandFromSearch);
  const snapshot = payload ? decodeSnapshot(payload) : null;
  const brand = resolveBrand(snapshot?.br ?? params.get('brand'));

  useEffect(() => {
    setBrandFromSearch(window.location.search);
  }, [setBrandFromSearch]);

  if (!snapshot) {
    return (
      <div className="min-h-screen bg-carbon-950 text-bone-100 flex flex-col items-center justify-center p-8">
        <span className="label-cap mb-4">Snapshot</span>
        <h1 className="num-display text-bone-100 text-4xl mb-3">Link expired or invalid</h1>
        <p className="text-bone-400 text-[14px] text-center max-w-md mb-8">
          This shared snapshot couldn't be decoded. The link may have been truncated.
        </p>
        <Link
          to="/"
          className="inline-flex items-center gap-2 px-5 py-3 rounded-full bg-bone-100 text-carbon-900 text-[13px] font-medium"
        >
          Go to {brand.name}
          <ArrowRight size={14} strokeWidth={1.8} />
        </Link>
      </div>
    );
  }

  const date = new Date(snapshot.ts);
  const dateLabel = date.toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric' });
  const timeLabel = date.toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit' });
  const co2 = (snapshot.todayKwh * 0.42).toFixed(1);
  const url = typeof window !== 'undefined' ? window.location.href : '';

  return (
    <div className="min-h-screen bg-carbon-950 text-bone-100 relative overflow-x-hidden">
      <div className="fixed inset-0 pointer-events-none aurora" />
      <div className="relative z-10 mx-auto max-w-md min-h-screen flex flex-col px-5 py-6">
        <header className="flex items-center justify-between mb-6">
          <Link to={`/?brand=${brand.id}`} className="flex items-center gap-2.5">
            <HeliosMark size={28} brand={brand} />
            <span className="text-bone-100 text-[14px] font-medium">{brand.name}</span>
          </Link>
          <span className="label-cap">Shared snapshot</span>
        </header>

        <motion.section
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, ease: [0.16, 1, 0.3, 1] }}
          className="surface-raised p-6 mb-5"
        >
          <div className="flex items-center gap-1.5 mb-2">
            <MapPin size={11} strokeWidth={1.6} className="text-bone-500" />
            <span className="num-mono text-bone-300 text-[11px]">{snapshot.loc}</span>
          </div>
          <p className="text-bone-500 text-[11px] font-mono uppercase tracking-widest mb-4">
            {dateLabel} · {timeLabel}
          </p>
          <div className="flex items-baseline gap-2">
            <span className="num-display text-bone-100 text-7xl leading-none">{snapshot.ac.toFixed(2)}</span>
            <span className="text-bone-400 text-[15px] font-mono uppercase tracking-widest">kW</span>
          </div>
          <p className="text-bone-400 text-[13px] mt-2">Live solar production at the moment of share.</p>
        </motion.section>

        <section className="grid grid-cols-2 gap-3 mb-5">
          <div className="surface p-4">
            <div className="flex items-center gap-1.5 mb-2 label-cap">
              <Sun size={11} strokeWidth={1.7} />
              Today
            </div>
            <div className="num-display text-bone-100 text-3xl leading-none">{snapshot.todayKwh.toFixed(1)}</div>
            <div className="text-bone-500 text-[10px] font-mono uppercase tracking-widest mt-1">kWh produced</div>
          </div>
          <div className="surface p-4">
            <div className="flex items-center gap-1.5 mb-2 label-cap">
              <BatteryCharging size={11} strokeWidth={1.7} />
              Battery
            </div>
            <div className="num-display text-bone-100 text-3xl leading-none">{snapshot.soc}</div>
            <div className="text-bone-500 text-[10px] font-mono uppercase tracking-widest mt-1">% state of charge</div>
          </div>
          <div className="surface p-4">
            <div className="flex items-center gap-1.5 mb-2 label-cap">
              <Home size={11} strokeWidth={1.7} />
              Self-use
            </div>
            <div className="num-display text-bone-100 text-3xl leading-none">{snapshot.selfUse}</div>
            <div className="text-bone-500 text-[10px] font-mono uppercase tracking-widest mt-1">% on-site</div>
          </div>
          <div className="surface p-4">
            <span className="label-cap mb-2 block">Lifetime</span>
            <div className="num-display text-bone-100 text-3xl leading-none">{(snapshot.lifeKwh / 1000).toFixed(2)}</div>
            <div className="text-bone-500 text-[10px] font-mono uppercase tracking-widest mt-1">MWh · {co2} kg CO₂ today</div>
          </div>
        </section>

        {snapshot.fc && snapshot.fc.length > 0 && (
          <section className="surface p-4 mb-5">
            <span className="label-cap mb-3 block">Next 5 days · forecast</span>
            <div className="grid grid-cols-5 gap-2">
              {snapshot.fc.map((kwh, i) => {
                const max = Math.max(...snapshot.fc!, 1);
                return (
                  <div key={i} className="flex flex-col items-center gap-1.5">
                    <span className="text-[10px] font-mono uppercase tracking-widest text-bone-500">
                      {i === 0 ? 'Tod' : i === 1 ? 'Tom' : `+${i}`}
                    </span>
                    <div className="num-mono text-bone-100 text-[12px] font-medium">{kwh}</div>
                    <div className="text-bone-600 text-[9px] font-mono">kWh</div>
                    <div className="w-full h-0.5 rounded-full bg-carbon-800 overflow-hidden">
                      <div
                        className="h-full rounded-full bg-signal-solar"
                        style={{ width: `${Math.max(8, (kwh / max) * 100)}%` }}
                      />
                    </div>
                  </div>
                );
              })}
            </div>
          </section>
        )}

        <section className="surface-raised p-5 mt-auto">
          <h2 className="text-bone-100 text-[16px] font-medium mb-1">Want one for your roof?</h2>
          <p className="text-bone-400 text-[12px] mb-4 leading-relaxed">
            {brand.name} works with any SunSpec-compatible inverter. Open the demo to see what your system could look like.
          </p>
          <div className="flex items-center gap-2">
            <Link
              to={`/?brand=${brand.id}`}
              className="flex-1 inline-flex items-center justify-center gap-1.5 px-4 py-2.5 rounded-full bg-bone-100 text-carbon-900 text-[12px] font-medium hover:bg-bone-200 transition-colors"
            >
              Try {brand.name}
              <ExternalLink size={12} strokeWidth={1.8} />
            </Link>
            <ShareButtonPlain url={url} />
          </div>
        </section>

        <p className="text-center text-bone-700 text-[10px] font-mono uppercase tracking-widest mt-6">
          {brand.id === 'helios' ? 'helios° · precision energy' : `Powered by helios° · ${brand.name}`}
        </p>
      </div>
    </div>
  );
}
