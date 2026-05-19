import { Link, useSearchParams } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { 
  ArrowRight, Sun, Cable, BatteryCharging, Zap, 
  MapPin, Volume2, VolumeX, RefreshCw, Cpu, Activity, HelpCircle, ChevronDown
} from 'lucide-react';
import { HeliosMark } from '../components/HeliosMark';
import { useStore } from '../store/useStore';
import { useEffect, useState, useRef } from 'react';
import { telemetryAudio } from '../lib/audio';

export function Landing() {
  const [params] = useSearchParams();
  const brand = useStore((s) => s.brand);
  const setBrandFromSearch = useStore((s) => s.setBrandFromSearch);

  // Preloader state
  const [showPreloader, setShowPreloader] = useState(true);
  const [preloaderProgress, setPreloaderProgress] = useState(0);
  const [preloaderLogs, setPreloaderLogs] = useState<string[]>([]);

  // Telemetry sound toggle
  const [audioEnabled, setAudioEnabled] = useState(false);

  // 3D Inverter Rotation
  const [rotation, setRotation] = useState({ x: -12, y: 25 });
  const [isDragging, setIsDragging] = useState(false);
  const [autoRotate, setAutoRotate] = useState(true);
  const dragStart = useRef({ x: 0, y: 0 });
  const dragRotation = useRef({ x: -12, y: 25 });

  // Interactive app preview tabs
  const [activeTab, setActiveTab] = useState<'flow' | 'forecast' | 'strings' | 'battery'>('flow');

  // FAQ states
  const [openFaq, setOpenFaq] = useState<number | null>(null);

  useEffect(() => {
    setBrandFromSearch(window.location.search);
  }, [setBrandFromSearch]);

  const search = params.toString();
  const appHref = search ? `/app?${search}` : '/app';

  // 1. Run Preloader sequence
  useEffect(() => {
    let start = Date.now();
    const duration = 1800; // 1.8 seconds

    const timer = setInterval(() => {
      const elapsed = Date.now() - start;
      const progress = Math.min(100, Math.floor((elapsed / duration) * 100));
      setPreloaderProgress(progress);

      // Trigger logs at intervals
      if (progress >= 5 && preloaderLogs.length === 0) {
        setPreloaderLogs(prev => [...prev, 'INITIALIZING MODBUS TCP CLIENT CORE ON PORT 502...']);
      }
      if (progress >= 30 && preloaderLogs.length === 1) {
        setPreloaderLogs(prev => [...prev, 'ESTABLISHING HANDSHAKE WITH MOCK SUNSPEC SLAVE REGISTRY [ADDR=1]...']);
      }
      if (progress >= 60 && preloaderLogs.length === 2) {
        setPreloaderLogs(prev => [...prev, 'DOWNLOADING 7-DAY SOLAR RADIATION MODEL FROM OPEN-METEO...']);
      }
      if (progress >= 85 && preloaderLogs.length === 3) {
        setPreloaderLogs(prev => [...prev, 'SYNTHESIZING PREDICTIVE ENERGY INTELLIGENCE ENGINE...']);
      }
      if (progress >= 100) {
        setPreloaderLogs(prev => [...prev, 'READY: SYSTEM VECTOR ONLINE. INITIATING INTERFACE.']);
        clearInterval(timer);
        setTimeout(() => {
          setShowPreloader(false);
        }, 400);
      }
    }, 30);

    return () => clearInterval(timer);
  }, [preloaderLogs.length]);

  // 2. Gentle auto-rotation for the 3D Inverter when not dragging
  useEffect(() => {
    if (isDragging || !autoRotate) return;

    const frame = () => {
      setRotation(prev => ({
        x: prev.x,
        y: (prev.y + 0.15) % 360
      }));
    };

    const interval = setInterval(frame, 16);
    return () => clearInterval(interval);
  }, [isDragging, autoRotate]);

  // Audio trigger utility
  const handleAudioToggle = () => {
    if (audioEnabled) {
      telemetryAudio.stopHum();
      setAudioEnabled(false);
    } else {
      telemetryAudio.startHum();
      setAudioEnabled(true);
      telemetryAudio.playRelayClick(true);
    }
  };

  const playClick = (type: 'click' | 'toggle') => {
    if (audioEnabled) {
      telemetryAudio.playRelayClick(type === 'click');
    }
  };

  // Drag handlers for inverter
  const handleStart = (clientX: number, clientY: number) => {
    setIsDragging(true);
    setAutoRotate(false);
    dragStart.current = { x: clientX, y: clientY };
    dragRotation.current = { ...rotation };
  };

  const handleMove = (clientX: number, clientY: number) => {
    if (!isDragging) return;
    const deltaX = clientX - dragStart.current.x;
    const deltaY = clientY - dragStart.current.y;
    setRotation({
      x: Math.max(-45, Math.min(45, dragRotation.current.x - deltaY * 0.5)),
      y: dragRotation.current.y + deltaX * 0.5
    });
  };

  const handleEnd = () => {
    setIsDragging(false);
    // Restart auto rotate after brief delay
    setTimeout(() => {
      setAutoRotate(true);
    }, 4000);
  };

  const trigger360Spin = () => {
    playClick('click');
    setAutoRotate(false);
    let step = 0;
    const animate = () => {
      if (step < 30) {
        setRotation(prev => ({ ...prev, y: prev.y + 12 }));
        step++;
        requestAnimationFrame(animate);
      } else {
        setAutoRotate(true);
      }
    };
    animate();
  };

  const specs = [
    { label: 'MODBUS PROTOCOL', val: 'SunSpec TCP v1.2' },
    { label: 'SAMPLING INTERVAL', val: '1.0 sec (Realtime)' },
    { label: 'DEFAULT CAPACITY', val: '9.6 kW Hybrid' },
    { label: 'DC STRINGS', val: '3x Active (A, B, C)' },
    { label: 'BATTERY STORAGE', val: '13.5 kWh Smart-Ring' },
    { label: 'AI FORECAST RAD', val: 'Open-Meteo Shortwave' },
  ];

  const featuresList = [
    {
      id: 'flow',
      title: 'Live Flow Telemetry',
      label: 'DYNAMIC POWER ROUTING',
      desc: 'Real-time animation pathways mapping generation from your solar arrays directly into household load, battery storage, and utility grid exchanges.',
      Icon: Activity
    },
    {
      id: 'forecast',
      title: '7-Day Solar Forecast',
      label: 'PREDICTIVE RADIATION MODELING',
      desc: 'Harnesses raw geographic shortwave radiation algorithms. Schedules power-hungry loads around peak generation times, avoiding grid charges.',
      Icon: Sun
    },
    {
      id: 'strings',
      title: 'Multi-String Balancing',
      label: 'STRING VOLTAGE ANOMALIES',
      desc: 'Monitors the health and efficiency of every string. Alerts you to shading obstructions, dust degradation, or hardware anomalies.',
      Icon: Cable
    },
    {
      id: 'battery',
      title: 'Smart Storage Strategy',
      label: 'PEAK-SHAVING OPTIMIZATION',
      desc: 'Select Self-Consumption, Time-of-Use, or Emergency Backup. Adjusts charging rates based on storm alerts and solar yield projections.',
      Icon: BatteryCharging
    }
  ];

  const testimonials = [
    {
      name: 'Dr. Marcus Vance',
      role: 'Solar Grid Analyst',
      quote: 'Helios gives installers and homeowners deep, unfiltered SunSpec access. No vendor cloud locking, just raw mathematical precision.'
    },
    {
      name: 'Sophia Thorne',
      role: 'Home Automation Architect',
      quote: 'The integration of weather patterns and local grid rates allows for perfect device scheduling. Saves my clients an extra 35% on average.'
    },
    {
      name: 'Liam Chen',
      role: 'Sustainable Energy Lead',
      quote: 'Runs completely client-side, loads offline instantly. Exactly what a modern utility PWA should be. Tactile, precise, privacy-respecting.'
    },
    {
      name: 'Hana Kovalenko',
      role: 'Beta Tester',
      quote: 'Switching brands from Meridian to Voltcraft retains my telemetry snapshots seamlessly. Incredible white-labeling architecture.'
    }
  ];

  const faqs = [
    {
      q: 'What is SunSpec Modbus TCP?',
      a: 'SunSpec Modbus TCP is the global open standard interface for solar inverters, meters, and battery systems. By using SunSpec, Helios bypasses proprietary manufacturer APIs, letting you connect to SMA, Fronius, SolarEdge, Enphase, and other brands directly over your local home network without internet lock-in.'
    },
    {
      q: 'Does Helios store my solar telemetry in the cloud?',
      a: 'No. Helios is a local-first application. All Modbus readings remain entirely on your local network. Deep-link sharing encodes telemetry snapshots directly into a compressed URL base64 payload, which is decoded client-side by whoever you share it with. Your private grid metrics never touch a server database.'
    },
    {
      q: 'How does the 7-day production forecast operate?',
      a: 'Using your geographical coordinates (stored strictly in your browser), Helios queries Open-Meteo’s shortwave solar radiation forecast variables. It combines direct and diffuse solar radiation variables with your system\'s kW capacity and a performance ratio (0.82) to compute highly accurate expected daily kWh production.'
    },
    {
      q: 'Can Helios manage custom rate schedules and utility tariffs?',
      a: 'Yes. The Battery settings screen allows you to configure utility Peak, Off-Peak, and Mid-Peak hours. When set to Time-of-Use mode, the system calculates the optimal periods to charge your batteries using excess solar power and when to dispatch it to shave peak loads.'
    }
  ];

  return (
    <div className="min-h-screen bg-carbon-950 text-bone-100 relative overflow-x-hidden select-none">
      
      {/* 1. Telemetry Startup Preloader */}
      <AnimatePresence>
        {showPreloader && (
          <motion.div 
            initial={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.4, ease: 'easeInOut' }}
            className="fixed inset-0 z-50 bg-carbon-950 flex flex-col justify-between p-8 font-mono select-none"
          >
            <div className="grid-bg absolute inset-0 opacity-15 pointer-events-none" />
            
            {/* Header info */}
            <div className="flex items-center justify-between text-bone-500 text-[10px] uppercase tracking-widest relative z-10">
              <span>SYSTEM DIAGNOSTICS DEPLOYMENT</span>
              <span>VER 4.2.0-STABLE</span>
            </div>

            {/* Core loading info */}
            <div className="max-w-xl mx-auto w-full text-left relative z-10 flex flex-col justify-center flex-grow">
              <div className="flex items-center gap-3 mb-6">
                <HeliosMark size={36} brand={brand} />
                <span className="text-[18px] text-bone-100 font-bold tracking-tight lowercase">
                  {brand.name}<span className="text-signal-solar">.core</span>
                </span>
              </div>

              {/* Live updating logs */}
              <div className="space-y-2.5 min-h-[140px] mb-8 bg-carbon-900/60 border border-hairline p-4 rounded-lg overflow-y-auto">
                {preloaderLogs.map((log, i) => (
                  <div key={i} className="text-[10px] leading-relaxed text-bone-400 flex items-start gap-2">
                    <span className="text-signal-solar font-bold">❯</span>
                    <span>{log}</span>
                  </div>
                ))}
                {preloaderProgress < 100 && (
                  <div className="text-[10px] text-bone-600 animate-pulse-soft">
                    ❯ EXECUTING MATRIX INITIALIZATION SEQUENCE...
                  </div>
                )}
              </div>

              {/* Progress visual */}
              <div className="space-y-2">
                <div className="flex justify-between text-[11px] text-bone-300">
                  <span>TELEMETRY SYNAPSE LOAD</span>
                  <span className="text-signal-solar font-bold">{preloaderProgress}%</span>
                </div>
                <div className="w-full h-1 bg-carbon-800 rounded-full overflow-hidden border border-hairline">
                  <motion.div 
                    className="h-full bg-signal-solar"
                    style={{ width: `${preloaderProgress}%` }}
                    transition={{ ease: 'easeOut' }}
                  />
                </div>
              </div>
            </div>

            {/* Footer legal */}
            <div className="text-center text-bone-600 text-[9px] relative z-10">
              HELIOS GRID LOGISTICS SYSTEM © 2026. ALL TELEMETRY TRANSMITTED LOCALLY.
            </div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Aurora glow background */}
      <div className="fixed inset-0 pointer-events-none aurora z-0" />
      <div className="grid-bg absolute inset-0 opacity-10 pointer-events-none z-0" />

      {/* Main Content Frame */}
      <div className="relative z-10">
        
        {/* Navigation Header */}
        <header className="sticky top-0 bg-carbon-950/70 backdrop-blur-md border-b border-hairline z-40 px-6 py-4 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <HeliosMark size={30} brand={brand} />
            <span className="text-bone-100 text-[15px] font-bold tracking-tight">{brand.name}</span>
          </div>

          <div className="flex items-center gap-6">
            {/* Telemetry hum sound toggle */}
            <button 
              onClick={handleAudioToggle}
              className={`p-2 rounded-full border border-hairline transition-all flex items-center justify-center ${
                audioEnabled ? 'bg-signal-solar/15 text-signal-solar border-signal-solar/35 shadow-inner' : 'text-bone-400 hover:text-bone-100'
              }`}
              title="Toggle Audio Feedback"
            >
              {audioEnabled ? <Volume2 size={13} /> : <VolumeX size={13} />}
              <span className="font-mono text-[8px] tracking-widest uppercase ml-1.5 hidden md:inline">
                {audioEnabled ? 'Audio On' : 'Audio Off'}
              </span>
            </button>

            <Link
              to={appHref}
              onClick={() => playClick('click')}
              className="text-[11px] font-mono uppercase tracking-widest px-4 py-2 rounded-full bg-bone-100 text-carbon-950 hover:bg-bone-200 transition-colors font-semibold"
            >
              Open app
            </Link>
          </div>
        </header>

        {/* HERO SECTION */}
        <section className="relative min-h-[calc(100vh-68px)] flex flex-col justify-between py-12 px-6">
          <div className="max-w-6xl mx-auto w-full grid grid-cols-1 lg:grid-cols-12 gap-12 items-center my-auto">
            
            {/* Left Specs Panel */}
            <div className="lg:col-span-3 order-2 lg:order-1 space-y-6">
              <div className="flex items-center gap-2 text-bone-400">
                <Cpu size={12} className="text-signal-solar" />
                <span className="label-cap">Engine Specifications</span>
              </div>
              <div className="bg-carbon-900/60 backdrop-blur-md border border-hairline rounded-xl divide-y divide-hairline">
                {specs.map(spec => (
                  <div key={spec.label} className="p-3.5 flex justify-between items-center text-[11px]">
                    <span className="text-bone-500 font-mono tracking-wider">{spec.label}</span>
                    <span className="text-bone-200 font-mono font-medium">{spec.val}</span>
                  </div>
                ))}
              </div>
              <div className="p-4 bg-signal-solar/5 border border-signal-solar/10 rounded-xl">
                <div className="flex items-center gap-2 mb-1.5">
                  <div className="w-1.5 h-1.5 rounded-full bg-signal-flow animate-ping" />
                  <span className="text-[11px] font-mono text-signal-solar font-bold uppercase tracking-wider">MODBUS LINK ACTIVE</span>
                </div>
                <p className="text-[11px] text-bone-400 leading-relaxed">
                  Direct telemetry pipeline configured over TCP gateway. Simulated register array updating live on the loop.
                </p>
              </div>
            </div>

            {/* Center 3D Rotating Inverter Canvas */}
            <div className="lg:col-span-5 order-1 lg:order-2 flex flex-col items-center justify-center py-6">
              <div 
                className="perspective-1000 relative w-[240px] h-[340px] flex items-center justify-center cursor-grab active:cursor-grabbing select-none"
                onMouseDown={(e) => handleStart(e.clientX, e.clientY)}
                onMouseMove={(e) => handleMove(e.clientX, e.clientY)}
                onMouseUp={handleEnd}
                onMouseLeave={handleEnd}
                onTouchStart={(e) => handleStart(e.touches[0].clientX, e.touches[0].clientY)}
                onTouchMove={(e) => handleMove(e.touches[0].clientX, e.touches[0].clientY)}
                onTouchEnd={handleEnd}
              >
                <div 
                  className="preserve-3d absolute w-[200px] h-[280px] transition-transform duration-75"
                  style={{ transform: `rotateX(${rotation.x}deg) rotateY(${rotation.y}deg)` }}
                >
                  {/* FRONT FACE */}
                  <div 
                    className="absolute inset-0 bg-carbon-900 border-2 border-bone-300 rounded-xl flex flex-col justify-between p-4 backface-hidden shadow-2xl"
                    style={{ transform: 'translateZ(40px)' }}
                  >
                    <div className="flex justify-between items-start">
                      <div className="space-y-0.5">
                        <h4 className="text-[12px] font-bold text-bone-100 font-mono tracking-tight lowercase">
                          {brand.name}<span className="text-signal-solar">°</span>
                        </h4>
                        <p className="text-[7px] text-bone-500 font-mono">HYBRID INVERTER H-9600</p>
                      </div>
                      {/* Interactive blinking LED */}
                      <div className="flex items-center gap-1">
                        <div className="w-1.5 h-1.5 rounded-full bg-signal-flow animate-pulse-soft" />
                        <span className="text-[7px] text-bone-400 font-mono">COMM</span>
                      </div>
                    </div>

                    {/* Glowing LED display */}
                    <div className="bg-carbon-950 border border-hairline p-2.5 rounded-lg font-mono space-y-1 text-left">
                      <div className="flex justify-between text-[8px] text-bone-500">
                        <span>METRIC</span>
                        <span>VALUE</span>
                      </div>
                      <div className="h-px bg-hairline mb-1" />
                      <div className="flex justify-between text-[9px] text-signal-solar">
                        <span>PV FLOW:</span>
                        <span>3.42 kW</span>
                      </div>
                      <div className="flex justify-between text-[9px] text-signal-flow">
                        <span>LOAD:</span>
                        <span>1.18 kW</span>
                      </div>
                      <div className="flex justify-between text-[9px] text-signal-battery">
                        <span>BATT SOC:</span>
                        <span>76.4%</span>
                      </div>
                      <div className="flex justify-between text-[9px] text-signal-grid">
                        <span>GRID:</span>
                        <span>+2.24 kW</span>
                      </div>
                    </div>

                    {/* Technical grill */}
                    <div className="space-y-1">
                      <div className="flex gap-1 justify-center">
                        {[...Array(6)].map((_, i) => (
                          <div key={i} className="w-6 h-1 bg-carbon-800 rounded-full" />
                        ))}
                      </div>
                      <p className="text-[6px] text-center text-bone-600 font-mono tracking-wider">
                        SUNSPEC COMPLIANT PROTOCOL REGISTER
                      </p>
                    </div>
                  </div>

                  {/* BACK FACE */}
                  <div 
                    className="absolute inset-0 bg-carbon-950 border-2 border-bone-600/60 rounded-xl flex flex-col justify-between p-4 backface-hidden"
                    style={{ transform: 'rotateY(180deg) translateZ(40px)' }}
                  >
                    <div className="text-[7px] text-bone-500 font-mono tracking-widest text-center uppercase">
                      HEATSINK ELEMENT - DO NOT OBSTRUCT
                    </div>
                    {/* Cooling fin slots */}
                    <div className="flex-grow my-4 flex flex-col justify-between">
                      {[...Array(10)].map((_, i) => (
                        <div key={i} className="w-full h-1.5 bg-carbon-800 border-b border-carbon-950" />
                      ))}
                    </div>
                    <div className="text-[6px] text-center text-bone-600 font-mono">
                      IP65 WEATHERPROOF ALUMINUM HOUSING
                    </div>
                  </div>

                  {/* LEFT FACE */}
                  <div 
                    className="absolute top-0 bottom-0 w-[80px] bg-carbon-850 border-y-2 border-l-2 border-r border-bone-500/60 rounded-xl backface-hidden flex flex-col justify-between p-3 text-center"
                    style={{ 
                      left: '60px', 
                      transform: 'rotateY(-90deg) translateZ(100px)' 
                    }}
                  >
                    <span className="text-[6px] text-bone-500 font-mono uppercase">CONDUIT ENTRIES</span>
                    <div className="space-y-2">
                      <div className="size-6 rounded-full bg-carbon-950 border border-hairline mx-auto flex items-center justify-center">
                        <div className="size-3 rounded-full bg-carbon-800" />
                      </div>
                      <div className="size-6 rounded-full bg-carbon-950 border border-hairline mx-auto flex items-center justify-center">
                        <div className="size-3 rounded-full bg-carbon-800" />
                      </div>
                    </div>
                    <span className="text-[6px] text-bone-600 font-mono">DC INPUTS</span>
                  </div>

                  {/* RIGHT FACE */}
                  <div 
                    className="absolute top-0 bottom-0 w-[80px] bg-carbon-850 border-y-2 border-r-2 border-l border-bone-500/60 rounded-xl backface-hidden flex flex-col justify-between p-3 text-center"
                    style={{ 
                      left: '60px', 
                      transform: 'rotateY(90deg) translateZ(100px)' 
                    }}
                  >
                    <span className="text-[6px] text-bone-500 font-mono uppercase">INTERFACE PORT</span>
                    <div className="space-y-3">
                      <div className="w-8 h-4 bg-carbon-950 border border-hairline rounded mx-auto flex items-center justify-center">
                        <span className="text-[5px] text-bone-400 font-mono">RJ45 MODBUS</span>
                      </div>
                      <div className="size-4 bg-carbon-900 border border-hairline rounded-full mx-auto flex items-center justify-center">
                        <div className="size-2 bg-signal-alert rounded-full" />
                      </div>
                    </div>
                    <span className="text-[6px] text-bone-600 font-mono">ON/OFF BREAKER</span>
                  </div>

                  {/* TOP FACE */}
                  <div 
                    className="absolute left-0 right-0 h-[80px] bg-carbon-800 border-x-2 border-t-2 border-b border-bone-500/60 rounded-xl backface-hidden p-2 flex items-center justify-center"
                    style={{ 
                      top: '100px', 
                      transform: 'rotateX(90deg) translateZ(140px)' 
                    }}
                  >
                    <div className="w-12 h-6 border border-hairline rounded-full bg-carbon-950 flex items-center justify-center">
                      <div className="w-8 h-2 bg-carbon-700 transform rotate-[45deg]" />
                    </div>
                  </div>

                  {/* BOTTOM FACE */}
                  <div 
                    className="absolute left-0 right-0 h-[80px] bg-carbon-800 border-x-2 border-b-2 border-t border-bone-500/60 rounded-xl backface-hidden p-2"
                    style={{ 
                      top: '100px', 
                      transform: 'rotateX(-90deg) translateZ(140px)' 
                    }}
                  />
                </div>
              </div>
              <span className="text-[9px] font-mono tracking-widest text-bone-500 uppercase mt-4 animate-pulse-soft">
                [ DRAG TO ROTATE INVERTER ]
              </span>
            </div>

            {/* Right Hero Info Pitch */}
            <div className="lg:col-span-4 order-3 space-y-6 text-left">
              <span className="label-cap text-signal-solar">{brand.id === 'helios' ? 'Solar intelligence' : `By ${brand.name}`}</span>
              <div className="space-y-2">
                <h1 className="text-5xl md:text-6xl font-bold font-mono tracking-tighter leading-none uppercase">
                  <span className="text-outline">PRECISION</span>
                  <br />
                  <span className="text-shimmer italic font-display lowercase font-normal leading-[0.8] block mb-2">energy.</span>
                  <span>INTELLIGENCE</span>
                </h1>
              </div>
              
              <p className="text-bone-400 text-[14px] leading-relaxed max-w-sm">
                {brand.tagline} Direct local telemetry feeds from any SunSpec-compatible inverter merged with forecast modules and AI logic.
              </p>

              <div className="flex flex-wrap gap-3.5 pt-2">
                <Link
                  to={appHref}
                  onClick={() => playClick('click')}
                  className="inline-flex items-center gap-2 px-6 py-3.5 rounded-full bg-bone-100 text-carbon-950 hover:bg-bone-200 transition-colors text-[13px] font-bold shadow-lg"
                >
                  Launch Live System
                  <ArrowRight size={14} strokeWidth={2} />
                </Link>
                <button
                  onClick={trigger360Spin}
                  className="inline-flex items-center gap-2 px-5 py-3.5 rounded-full border border-hairline text-bone-300 hover:text-bone-100 hover:bg-carbon-900/40 transition-all text-[13px]"
                >
                  <RefreshCw size={13} className="animate-spin-slow" />
                  Spin Hardware
                </button>
              </div>
            </div>

          </div>

          {/* Supported protocols scrolling marquee */}
          <div className="w-full overflow-hidden border-y border-hairline py-4 mt-12 bg-carbon-900/30 relative z-20">
            <div className="flex w-[200%] animate-marquee">
              {[...Array(2)].map((_, i) => (
                <div key={i} className="flex justify-around min-w-full font-mono text-[10px] tracking-widest text-bone-500 uppercase select-none">
                  <span>SUNSPEC PROTOCOL v1.2</span>
                  <span className="text-signal-solar">·</span>
                  <span>SMA INVERTERS</span>
                  <span className="text-signal-solar">·</span>
                  <span>FRONIUS SMART GRID</span>
                  <span className="text-signal-solar">·</span>
                  <span>SOLAREDGE CONTROLLER</span>
                  <span className="text-signal-solar">·</span>
                  <span>ENPHASE MICROINVERTERS</span>
                  <span className="text-signal-solar">·</span>
                  <span>VICTRON ENERGY STORAGE</span>
                  <span className="text-signal-solar">·</span>
                  <span>SUNGROW SYSTEM</span>
                  <span className="text-signal-solar">·</span>
                  <span>SCHNEIDER ELECTRIC</span>
                  <span className="text-signal-solar">·</span>
                </div>
              ))}
            </div>
          </div>
        </section>

        {/* STATS SECTION */}
        <section className="py-24 px-6 border-b border-hairline bg-carbon-900/20">
          <div className="max-w-4xl mx-auto text-center space-y-12">
            <span className="label-cap text-bone-500">PHILOSOPHY OF MEASUREMENT</span>
            <h2 className="text-3xl md:text-5xl font-mono uppercase tracking-tight leading-[1.0] text-bone-100 max-w-2xl mx-auto">
              Where raw electrical data becomes <span className="text-shimmer italic font-display lowercase font-normal">actionable intelligence.</span>
            </h2>
            
            <div className="grid grid-cols-2 md:grid-cols-4 gap-6 pt-8">
              <div className="p-6 bg-carbon-900/50 border border-hairline rounded-xl space-y-1 text-center">
                <span className="num-display text-5xl text-signal-solar block">1.0s</span>
                <span className="label-cap text-[9px]">Metrics Sampling</span>
              </div>
              <div className="p-6 bg-carbon-900/50 border border-hairline rounded-xl space-y-1 text-center">
                <span className="num-display text-5xl text-signal-flow block">99.2%</span>
                <span className="label-cap text-[9px]">Irradiance Precision</span>
              </div>
              <div className="p-6 bg-carbon-900/50 border border-hairline rounded-xl space-y-1 text-center">
                <span className="num-display text-5xl text-signal-grid block">0</span>
                <span className="label-cap text-[9px]">Third Party Cookies</span>
              </div>
              <div className="p-6 bg-carbon-900/50 border border-hairline rounded-xl space-y-1 text-center">
                <span className="num-display text-5xl text-signal-battery block">100%</span>
                <span className="label-cap text-[9px]">Offline PWA Load</span>
              </div>
            </div>
          </div>
        </section>

        {/* INTERACTIVE FEATURES GRID */}
        <section className="py-24 px-6 max-w-6xl mx-auto">
          <div className="text-center mb-16 space-y-3">
            <span className="label-cap text-signal-solar">Tactile Feature Modules</span>
            <h2 className="text-3xl md:text-4xl font-mono uppercase tracking-tight text-bone-100">
              Interactive Live Telemetry Preview
            </h2>
            <p className="text-bone-400 text-sm max-w-md mx-auto">
              Select an engine capability module below to preview live layout data directly from our simulated telemetry array.
            </p>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-center">
            
            {/* Left selector menu */}
            <div className="lg:col-span-5 space-y-3">
              {featuresList.map(feat => {
                const isSelected = activeTab === feat.id;
                const FeatIcon = feat.Icon;
                return (
                  <button
                    key={feat.id}
                    onClick={() => {
                      playClick('click');
                      setActiveTab(feat.id as any);
                    }}
                    className={`w-full text-left p-5 rounded-xl border transition-all duration-300 flex items-start gap-4 ${
                      isSelected 
                        ? 'bg-carbon-900 border-bone-300 shadow-md translate-x-1.5' 
                        : 'bg-carbon-900/30 border-hairline hover:border-bone-600 hover:bg-carbon-900/50'
                    }`}
                  >
                    <div className={`p-2.5 rounded-lg border transition-colors ${
                      isSelected ? 'bg-signal-solar/20 text-signal-solar border-signal-solar/40' : 'bg-carbon-950 text-bone-500 border-hairline'
                    }`}>
                      <FeatIcon size={18} />
                    </div>
                    <div className="space-y-1">
                      <span className="label-cap text-[9px] text-bone-500 block leading-none">{feat.label}</span>
                      <h4 className={`text-[15px] font-bold ${isSelected ? 'text-bone-100' : 'text-bone-300'}`}>
                        {feat.title}
                      </h4>
                      <p className="text-[12px] text-bone-400 leading-relaxed font-sans mt-1.5">
                        {feat.desc}
                      </p>
                    </div>
                  </button>
                );
              })}
            </div>

            {/* Right dynamic visual frame */}
            <div className="lg:col-span-7 flex justify-center relative">
              
              {/* Outer frame design */}
              <div className="relative w-full max-w-[420px] aspect-[4/5] bg-carbon-900 border border-bone-600/40 rounded-2xl p-4 shadow-2xl flex flex-col justify-between overflow-hidden">
                <div className="grid-bg absolute inset-0 opacity-5 pointer-events-none" />
                
                {/* Simulated App Header */}
                <div className="flex justify-between items-center text-[10px] font-mono border-b border-hairline pb-2.5">
                  <div className="flex items-center gap-1.5">
                    <HeliosMark size={16} brand={brand} />
                    <span className="font-bold text-bone-200">HELIOS OS</span>
                  </div>
                  <span className="px-2 py-0.5 rounded bg-carbon-950 border border-hairline text-signal-flow uppercase font-bold text-[8px] tracking-wider animate-pulse-soft">
                    LIVE STREAM
                  </span>
                </div>

                {/* Main Dynamic View Screen */}
                <div className="flex-grow my-4 relative flex flex-col justify-center items-center">
                  
                  {activeTab === 'flow' && (
                    <div className="w-full h-full flex flex-col justify-between py-4">
                      {/* Flow Diagram */}
                      <div className="grid grid-cols-3 gap-2 flex-grow items-center relative">
                        
                        {/* Core central Hub */}
                        <div className="col-span-3 flex justify-center my-2 relative z-10">
                          <div className="size-16 rounded-full bg-carbon-950 border-2 border-bone-300 shadow-glow-bone flex flex-col justify-center items-center">
                            <span className="text-[8px] font-mono text-bone-500">SYS-HUB</span>
                            <span className="text-[12px] font-mono font-bold text-bone-100">3.4kW</span>
                          </div>
                        </div>

                        {/* Solar Node */}
                        <div className="flex flex-col items-center">
                          <div className="size-12 rounded-xl bg-carbon-950 border border-signal-solar/50 flex flex-col justify-center items-center text-signal-solar">
                            <Sun size={18} />
                            <span className="text-[8px] font-mono mt-1">3.4 kW</span>
                          </div>
                          <span className="label-cap text-[8px] mt-1">Solar PV</span>
                        </div>

                        {/* Battery Storage */}
                        <div className="flex flex-col items-center">
                          <div className="size-12 rounded-xl bg-carbon-950 border border-signal-battery/50 flex flex-col justify-center items-center text-signal-battery">
                            <BatteryCharging size={18} />
                            <span className="text-[8px] font-mono mt-1">76%</span>
                          </div>
                          <span className="label-cap text-[8px] mt-1">Storage</span>
                        </div>

                        {/* Utility Grid */}
                        <div className="flex flex-col items-center">
                          <div className="size-12 rounded-xl bg-carbon-950 border border-signal-grid/50 flex flex-col justify-center items-center text-signal-grid">
                            <Zap size={18} />
                            <span className="text-[8px] font-mono mt-1">+2.2 kW</span>
                          </div>
                          <span className="label-cap text-[8px] mt-1">Grid Out</span>
                        </div>

                      </div>
                    </div>
                  )}

                  {activeTab === 'forecast' && (
                    <div className="w-full h-full flex flex-col justify-between py-2 text-left">
                      <div className="flex items-center justify-between text-[11px] font-mono mb-3">
                        <span className="text-bone-400">IRRADIANCE CHART</span>
                        <span className="text-signal-solar font-bold">AVG Yield: 48 kWh</span>
                      </div>
                      
                      {/* Fake graph container using simple CSS divs */}
                      <div className="flex-grow flex items-end gap-2.5 h-[160px] bg-carbon-950/60 border border-hairline p-3.5 rounded-lg">
                        {[
                          { day: 'Mon', val: 'h-[30%]', label: '31 kWh' },
                          { day: 'Tue', val: 'h-[60%]', label: '42 kWh' },
                          { day: 'Wed', val: 'h-[90%] bg-signal-solar/80 border-signal-solar', label: '62 kWh' },
                          { day: 'Thu', val: 'h-[75%]', label: '50 kWh' },
                          { day: 'Fri', val: 'h-[50%]', label: '38 kWh' },
                          { day: 'Sat', val: 'h-[80%]', label: '55 kWh' },
                          { day: 'Sun', val: 'h-[65%]', label: '45 kWh' }
                        ].map((item, idx) => (
                          <div key={idx} className="flex-grow flex flex-col justify-end items-center h-full group">
                            <span className="text-[7px] font-mono text-bone-400 opacity-0 group-hover:opacity-100 transition-opacity mb-1">
                              {item.label}
                            </span>
                            <div className={`w-full rounded-t-sm transition-all duration-500 bg-carbon-800 border border-hairline ${item.val}`} />
                            <span className="text-[8px] font-mono text-bone-500 mt-2">{item.day}</span>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}

                  {activeTab === 'strings' && (
                    <div className="w-full h-full flex flex-col justify-between py-2 text-left">
                      <div className="text-[11px] font-mono text-bone-400 mb-3 flex items-center gap-2">
                        <Cable size={12} className="text-signal-solar" />
                        <span>STRING LOAD COMPARISON REGISTER</span>
                      </div>
                      <div className="space-y-4 flex-grow justify-center flex flex-col">
                        {[
                          { name: 'STRING A (SOUTH)', volt: '412V', current: '8.2A', width: 'w-[90%]', color: 'bg-signal-flow' },
                          { name: 'STRING B (WEST - PARTIAL SHADE)', volt: '298V', current: '5.4A', width: 'w-[55%]', color: 'bg-signal-alert' },
                          { name: 'STRING C (EAST)', volt: '408V', current: '8.1A', width: 'w-[88%]', color: 'bg-signal-flow' }
                        ].map((str, idx) => (
                          <div key={idx} className="space-y-1.5">
                            <div className="flex justify-between text-[9px] font-mono">
                              <span className="text-bone-300 font-bold">{str.name}</span>
                              <span className="text-bone-500">{str.volt} · {str.current}</span>
                            </div>
                            <div className="w-full h-2 bg-carbon-950 border border-hairline rounded-full overflow-hidden">
                              <div className={`h-full rounded-full ${str.color} ${str.width}`} />
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}

                  {activeTab === 'battery' && (
                    <div className="w-full h-full flex flex-col justify-center items-center py-2 space-y-4">
                      {/* State of charge ring */}
                      <div className="relative size-32 flex items-center justify-center">
                        <svg viewBox="0 0 100 100" className="size-full transform -rotate-90">
                          <circle cx="50" cy="50" r="42" fill="none" stroke="rgba(239,236,229,0.06)" strokeWidth="6" />
                          <circle cx="50" cy="50" r="42" fill="none" stroke="#c5a572" strokeWidth="6" strokeDasharray="263.8" strokeDashoffset="62.4" strokeLinecap="round" className="shadow-lg" />
                        </svg>
                        <div className="absolute flex flex-col items-center justify-center font-mono">
                          <span className="text-2xl font-bold text-bone-100 leading-none">76.4%</span>
                          <span className="text-[8px] text-bone-500 tracking-wider uppercase mt-1">DISCHARGING</span>
                        </div>
                      </div>
                      
                      <div className="flex justify-between gap-2.5 w-full text-center font-mono">
                        <div className="flex-grow p-2 bg-carbon-950 border border-hairline rounded-lg">
                          <span className="text-[7px] text-bone-500 block">CURRENT DRAW</span>
                          <span className="text-[11px] font-bold text-signal-alert">-1.4 kW</span>
                        </div>
                        <div className="flex-grow p-2 bg-carbon-950 border border-hairline rounded-lg">
                          <span className="text-[7px] text-bone-500 block">REMAINING LIFE</span>
                          <span className="text-[11px] font-bold text-bone-200">7.2 Hours</span>
                        </div>
                      </div>
                    </div>
                  )}

                </div>

                {/* Simulated App Footer */}
                <div className="flex justify-between items-center text-[9px] font-mono border-t border-hairline pt-2.5 text-bone-500">
                  <span>SUNSPEC ADDR: IP.192.168.1.84</span>
                  <span>HELIOS ENGINE ACTIVE</span>
                </div>

              </div>
            </div>

          </div>
        </section>

        {/* TESTIMONIALS SLIDER SECTION */}
        <section className="py-24 border-y border-hairline bg-carbon-900/30 overflow-hidden relative z-20">
          <div className="max-w-6xl mx-auto px-6 mb-12 flex flex-col md:flex-row md:items-end justify-between gap-6">
            <div className="space-y-3">
              <span className="label-cap text-signal-solar">INSTALLER FEEDBACK</span>
              <h2 className="text-3xl font-mono uppercase tracking-tight text-bone-100">
                Endorsed by Grid Professionals
              </h2>
            </div>
            <p className="text-bone-400 text-sm max-w-sm">
              Read how solar integrators, home engineers, and tech leads utilize Helios to audit inverter behaviors.
            </p>
          </div>

          <div className="flex w-[200%] animate-marquee gap-6 px-6">
            {[...Array(2)].map((_, i) => (
              <div key={i} className="flex gap-6 min-w-full justify-around select-none">
                {testimonials.map((test, index) => (
                  <div key={index} className="w-[300px] flex-shrink-0 bg-carbon-900/80 border border-hairline p-6 rounded-xl flex flex-col justify-between gap-6">
                    <p className="text-[13px] text-bone-300 leading-relaxed font-sans italic">
                      "{test.quote}"
                    </p>
                    <div className="border-t border-hairline pt-4 flex flex-col">
                      <span className="text-[12px] font-bold text-bone-200">{test.name}</span>
                      <span className="text-[10px] text-bone-500 font-mono tracking-wider">{test.role}</span>
                    </div>
                  </div>
                ))}
              </div>
            ))}
          </div>
        </section>

        {/* FAQ ACCORDION SECTION */}
        <section className="py-24 px-6 max-w-3xl mx-auto">
          <div className="text-center mb-16 space-y-3">
            <HelpCircle className="mx-auto text-signal-solar" size={24} />
            <span className="label-cap text-bone-500">TECHNICAL INFORMATION</span>
            <h2 className="text-3xl font-mono uppercase tracking-tight text-bone-100">
              Frequently Asked Questions
            </h2>
          </div>

          <div className="space-y-4">
            {faqs.map((faq, index) => {
              const isOpen = openFaq === index;
              return (
                <div 
                  key={index}
                  className="bg-carbon-900/40 border border-hairline rounded-xl overflow-hidden transition-all duration-300"
                >
                  <button
                    onClick={() => {
                      playClick('toggle');
                      setOpenFaq(isOpen ? null : index);
                    }}
                    className="w-full flex items-center justify-between p-5 text-left text-bone-200 hover:text-bone-100"
                  >
                    <span className="text-[14px] font-bold tracking-tight font-sans">
                      {faq.q}
                    </span>
                    <ChevronDown 
                      size={16} 
                      className={`text-bone-400 transition-transform duration-300 ${isOpen ? 'transform rotate-180' : ''}`}
                    />
                  </button>
                  <div 
                    className={`transition-all duration-300 ease-in-out overflow-hidden ${
                      isOpen ? 'max-h-[300px] border-t border-hairline opacity-100' : 'max-h-0 opacity-0'
                    }`}
                  >
                    <p className="p-5 text-[13px] text-bone-400 leading-relaxed font-sans">
                      {faq.a}
                    </p>
                  </div>
                </div>
              );
            })}
          </div>
        </section>

        {/* PRODUCT CALL-TO-ACTION */}
        <section className="py-24 px-6 max-w-4xl mx-auto text-center border-t border-hairline relative z-20">
          <div className="flex justify-center mb-6">
            <HeliosMark size={56} brand={brand} />
          </div>
          <h2 className="text-4xl md:text-5xl font-mono tracking-tighter uppercase text-bone-100">
            Audit your array today.
          </h2>
          <p className="text-bone-400 text-[14px] mt-4 max-w-md mx-auto leading-relaxed">
            The application boots in mock telemetry mode by default. Explore diagnostic graphs, weather forecast modeling, and battery rate planners without modifying hardware.
          </p>
          <div className="mt-8 flex justify-center">
            <Link
              to={appHref}
              onClick={() => playClick('click')}
              className="inline-flex items-center gap-2 px-8 py-4 rounded-full bg-bone-100 text-carbon-950 hover:bg-bone-200 transition-colors text-[14px] font-bold shadow-lg"
            >
              Open Helios App
              <ArrowRight size={14} strokeWidth={2} />
            </Link>
          </div>
        </section>

        {/* FOOTER */}
        <footer className="border-t border-hairline bg-carbon-950/80 pt-16 pb-8 px-6 relative overflow-hidden">
          
          {/* Huge background text banner overlay */}
          <div className="absolute bottom-0 left-0 right-0 text-center select-none pointer-events-none z-0 overflow-hidden">
            <h1 className="text-[15vw] font-bold text-outline-strong tracking-tighter leading-none opacity-5 uppercase font-mono">
              {brand.name}
            </h1>
          </div>

          <div className="max-w-6xl mx-auto grid grid-cols-1 md:grid-cols-12 gap-8 relative z-10 pb-12">
            
            <div className="md:col-span-6 space-y-4">
              <div className="flex items-center gap-2.5">
                <HeliosMark size={24} brand={brand} />
                <span className="text-bone-100 text-[15px] font-bold tracking-tight uppercase">{brand.name}</span>
              </div>
              <p className="text-bone-500 text-[12px] max-w-sm leading-relaxed">
                Precision energy intelligence modules compliant with SunSpec Modbus standards. Optimized client-side telemetry analysis frameworks.
              </p>
            </div>

            <div className="md:col-span-6 flex flex-col md:items-end justify-between gap-4 font-mono text-[10px] text-bone-400">
              <div className="space-y-1 md:text-right">
                <span className="block text-bone-500 text-[9px] uppercase tracking-widest">SUPPORT EMAIL</span>
                <a href={`mailto:${brand.supportEmail ?? 'care@helios.example'}`} className="hover:text-signal-solar transition-colors">
                  {brand.supportEmail ?? 'care@helios.example'}
                </a>
              </div>
              <div className="flex flex-wrap gap-6 uppercase tracking-wider text-[9px]">
                <a href="#how-it-works" className="hover:text-bone-100 transition-colors">Documentation</a>
                <a href="https://sunspec.org/" target="_blank" rel="noopener noreferrer" className="hover:text-bone-100 transition-colors">SunSpec Standard</a>
              </div>
            </div>

          </div>

          <div className="max-w-6xl mx-auto pt-8 border-t border-hairline flex flex-col sm:flex-row items-center justify-between gap-4 text-bone-500 text-[10px] font-mono relative z-10">
            <div className="flex items-center gap-2">
              <span>{brand.legalName ?? brand.name}</span>
              <span>·</span>
              <span>Made for the sun · 2026</span>
            </div>
            
            <div className="flex items-center gap-1.5">
              <MapPin size={11} strokeWidth={1.6} />
              <span>{brand.id === 'helios' ? 'Decentralized Telemetry Mesh' : `Powered by helios°`}</span>
            </div>
          </div>

        </footer>

      </div>
    </div>
  );
}
