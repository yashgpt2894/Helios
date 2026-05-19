import { Link, useSearchParams } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { 
  ArrowRight, Sun, Cable, BatteryCharging, Zap, 
  MapPin, Volume2, VolumeX, RefreshCw, Cpu, Activity, HelpCircle, ChevronDown, Info, ShieldCheck
} from 'lucide-react';
import { HeliosMark } from '../components/HeliosMark';
import { useStore } from '../store/useStore';
import { useEffect, useState, useRef } from 'react';
import { telemetryAudio } from '../lib/audio';

export function Landing() {
  const [params] = useSearchParams();
  const brand = useStore((s) => s.brand);
  const setBrandFromSearch = useStore((s) => s.setBrandFromSearch);

  // Preloader states
  const [showPreloader, setShowPreloader] = useState(true);
  const [preloaderProgress, setPreloaderProgress] = useState(0);
  const [preloaderLogs, setPreloaderLogs] = useState<string[]>([]);
  const [consoleIndex, setConsoleIndex] = useState(0);

  // Telemetry sound toggle
  const [audioEnabled, setAudioEnabled] = useState(false);

  // 3D Inverter Rotation
  const [rotation, setRotation] = useState({ x: -12, y: 25 });
  const [isDragging, setIsDragging] = useState(false);
  const [autoRotate, setAutoRotate] = useState(true);
  const dragStart = useRef({ x: 0, y: 0 });
  const dragRotation = useRef({ x: -12, y: 25 });

  // Inverter hotspot details
  const [activeHotspot, setActiveHotspot] = useState<null | 'leds' | 'rj45' | 'heatsink' | 'breaker'>(null);

  // Interactive app preview simulation state
  const [activeTab, setActiveTab] = useState<'flow' | 'forecast' | 'strings' | 'battery'>('flow');
  const [gridFeedDirection, setGridFeedDirection] = useState<'export' | 'import'>('export');
  const [cloudDensity, setCloudDensity] = useState<number>(0.85); // 0.2 to 1.2
  const [simulateShading, setSimulateShading] = useState<boolean>(false);
  const [batteryStrategy, setBatteryStrategy] = useState<'self' | 'tou' | 'backup'>('self');

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
    const duration = 2000; // 2.0 seconds

    const timer = setInterval(() => {
      const elapsed = Date.now() - start;
      const progress = Math.min(100, Math.floor((elapsed / duration) * 100));
      setPreloaderProgress(progress);

      // Trigger logs at intervals
      if (progress >= 5 && consoleIndex === 0) {
        setPreloaderLogs(prev => [...prev, 'INITIALIZING MODBUS TCP GATEWAY ENGINE ON PORT 502...']);
        setConsoleIndex(1);
      }
      if (progress >= 22 && consoleIndex === 1) {
        setPreloaderLogs(prev => [...prev, 'ESTABLISHING HANDSHAKE WITH MOCK SUNSPEC SLAVE REGISTRY [ADDR=1]...']);
        setConsoleIndex(2);
      }
      if (progress >= 45 && consoleIndex === 2) {
        setPreloaderLogs(prev => [...prev, 'DOWNLOADING 7-DAY GEOLOCATION SOLAR RADIATION MODEL...']);
        setConsoleIndex(3);
      }
      if (progress >= 68 && consoleIndex === 3) {
        setPreloaderLogs(prev => [...prev, 'SYNTHESIZING PREDICTIVE ENERGY MANAGEMENT STATE MATRICES...']);
        setConsoleIndex(4);
      }
      if (progress >= 88 && consoleIndex === 4) {
        setPreloaderLogs(prev => [...prev, 'LOCAL HOST BINDINGS ONLINE. STARTING GRAPHICS LAYER...']);
        setConsoleIndex(5);
      }
      if (progress >= 100) {
        clearInterval(timer);
        setTimeout(() => {
          setShowPreloader(false);
        }, 500);
      }
    }, 20);

    return () => clearInterval(timer);
  }, [consoleIndex]);

  // 2. Gentle auto-rotation for the 3D Inverter when not dragging
  useEffect(() => {
    if (isDragging || !autoRotate || activeHotspot !== null) return;

    const frame = () => {
      setRotation(prev => ({
        x: prev.x,
        y: (prev.y + 0.12) % 360
      }));
    };

    const interval = setInterval(frame, 16);
    return () => clearInterval(interval);
  }, [isDragging, autoRotate, activeHotspot]);

  // Audio trigger utility
  const handleAudioToggle = () => {
    if (audioEnabled) {
      telemetryAudio.stopHum();
      setAudioEnabled(false);
    } else {
      telemetryAudio.startHum();
      setAudioEnabled(true);
      telemetryAudio.playRelayClick(true);
      telemetryAudio.adjustHum(cloudDensity);
    }
  };

  const playClick = (type: 'click' | 'toggle') => {
    if (audioEnabled) {
      telemetryAudio.playRelayClick(type === 'click');
    }
  };

  // Adjust audio hum pitch when cloud density changes
  useEffect(() => {
    if (audioEnabled) {
      telemetryAudio.adjustHum(cloudDensity);
    }
  }, [cloudDensity, audioEnabled]);

  // Drag handlers for inverter
  const handleStart = (clientX: number, clientY: number) => {
    setIsDragging(true);
    setAutoRotate(false);
    setActiveHotspot(null); // clear hotspot selection on drag
    dragStart.current = { x: clientX, y: clientY };
    dragRotation.current = { ...rotation };
  };

  const handleMove = (clientX: number, clientY: number) => {
    if (!isDragging) return;
    const deltaX = clientX - dragStart.current.x;
    const deltaY = clientY - dragStart.current.y;
    setRotation({
      x: Math.max(-45, Math.min(45, dragRotation.current.x - deltaY * 0.45)),
      y: dragRotation.current.y + deltaX * 0.45
    });
  };

  const handleEnd = () => {
    setIsDragging(false);
    setTimeout(() => {
      if (!isDragging && activeHotspot === null) {
        setAutoRotate(true);
      }
    }, 4500);
  };

  const trigger360Spin = () => {
    playClick('click');
    setAutoRotate(false);
    setActiveHotspot(null);
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

  // Hotspot focusing angles
  const focusHotspot = (hotspot: 'leds' | 'rj45' | 'heatsink' | 'breaker') => {
    playClick('click');
    setActiveHotspot(hotspot);
    setAutoRotate(false);

    if (hotspot === 'leds') {
      setRotation({ x: -10, y: 15 });
    } else if (hotspot === 'rj45') {
      setRotation({ x: 0, y: 90 });
    } else if (hotspot === 'heatsink') {
      setRotation({ x: 0, y: 180 });
    } else if (hotspot === 'breaker') {
      setRotation({ x: 0, y: -90 });
    }
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
      desc: 'Real-time animating flow lines mapping energy production from arrays into utility grid exchanges. Click the Grid toggle to simulate energy routing.',
      Icon: Activity
    },
    {
      id: 'forecast',
      title: '7-Day Solar Forecast',
      label: 'PREDICTIVE RADIATION MODELING',
      desc: 'Harnesses raw geographic shortwave radiation forecast grids. Drag the slider to simulate cloud cover adjustments and watch pitch-shift frequencies.',
      Icon: Sun
    },
    {
      id: 'strings',
      title: 'Multi-String Balancing',
      label: 'STRING VOLTAGE ANOMALIES',
      desc: 'Monitors the health and efficiency of every string. Trigger "Simulate Panel Shading" to drop String B voltage and alert the system diagnostics.',
      Icon: Cable
    },
    {
      id: 'battery',
      title: 'Smart Storage Strategy',
      label: 'PEAK-SHAVING OPTIMIZATION',
      desc: 'Toggle Self-Consumption, Time-of-Use, or Backup. Evaluates utility schedules and grid rates to adjust charging curves.',
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

  // Hotspots definitions
  const hotspotsData = {
    leds: {
      title: 'LCD DISPLAY CONSOLE',
      text: 'Active hardware screen displaying live generation and consumption. Confirms local network connection via blinking COMM lights.'
    },
    rj45: {
      title: 'RJ45 MODBUS TCP INTERFACE',
      text: 'Bypasses proprietary manufacturer servers. Communicates directly with local clients via SunSpec registers at 1-second query speeds.'
    },
    heatsink: {
      title: 'PASSIVE HEATSINK GRILL',
      text: 'Anodized aluminum fins dispersing heat without mechanical fans. Maintains 99% conversion efficiency even under heavy loads.'
    },
    breaker: {
      title: 'DC SWITCH BREAKER',
      text: 'Direct physical isolation switch for string power supplies. Fulfills standard safety disconnect parameters.'
    }
  };

  return (
    <div className="min-h-screen bg-carbon-950 text-bone-100 relative overflow-x-hidden select-none">
      
      {/* 1. Telemetry Startup Preloader (PageCoder-style logs) */}
      <AnimatePresence>
        {showPreloader && (
          <motion.div 
            initial={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.5, ease: 'easeInOut' }}
            className="fixed inset-0 z-50 bg-carbon-950 flex flex-col justify-between p-8 font-mono select-none"
          >
            <div className="grid-bg absolute inset-0 opacity-15 pointer-events-none" />
            
            {/* Header */}
            <div className="flex items-center justify-between text-bone-500 text-[10px] uppercase tracking-widest relative z-10">
              <span>SYSTEM DIAGNOSTICS DEPLOYMENT</span>
              <span>VER 4.2.0-STABLE</span>
            </div>

            {/* Core Loading Panel */}
            <div className="max-w-xl mx-auto w-full text-left relative z-10 flex flex-col justify-center flex-grow">
              <div className="flex items-center gap-3 mb-6">
                <HeliosMark size={36} brand={brand} />
                <span className="text-[18px] text-bone-100 font-bold tracking-tight lowercase">
                  {brand.name}<span className="text-signal-solar">.core</span>
                </span>
              </div>

              {/* Logs Stream */}
              <div className="space-y-2 text-[10px] leading-relaxed text-bone-400 bg-carbon-900/60 border border-hairline p-5 rounded-xl min-h-[160px] overflow-y-auto mb-8 font-mono shadow-inner">
                {preloaderLogs.map((log, i) => (
                  <div key={i} className="flex items-start gap-2">
                    <span className="text-signal-solar font-bold">❯</span>
                    <span>{log}</span>
                  </div>
                ))}
                {preloaderProgress < 100 && (
                  <div className="text-[10px] text-bone-500 animate-pulse flex items-center gap-1.5">
                    <span className="text-bone-600">❯</span>
                    <span>EXECUTING CONSOLE SEQUENCE...</span>
                  </div>
                )}
              </div>

              {/* Bar loader */}
              <div className="space-y-2">
                <div className="flex justify-between text-[11px] text-bone-300 font-mono">
                  <span>TELEMETRY LOAD MATRIX</span>
                  <span className="text-signal-solar font-bold">{preloaderProgress}%</span>
                </div>
                <div className="w-full h-[3px] bg-carbon-800 rounded-full overflow-hidden border border-hairline">
                  <motion.div 
                    className="h-full bg-signal-solar"
                    style={{ width: `${preloaderProgress}%` }}
                    transition={{ ease: 'easeOut' }}
                  />
                </div>
              </div>
            </div>

            {/* Footer */}
            <div className="text-center text-bone-600 text-[9px] relative z-10 uppercase tracking-widest">
              {brand.legalName ?? brand.name} © 2026. ALL METRICS RESOLVED LOCAL-FIRST.
            </div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Background patterns */}
      <div className="fixed inset-0 pointer-events-none aurora z-0" />
      <div className="grid-bg absolute inset-0 opacity-10 pointer-events-none z-0" />

      {/* Main Container Frame */}
      <div className="relative z-10 flex flex-col min-h-screen">
        
        {/* Navigation Bar */}
        <header className="sticky top-0 bg-carbon-950/70 backdrop-blur-md border-b border-hairline z-40 px-6 py-4 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <HeliosMark size={30} brand={brand} />
            <span className="text-bone-100 text-[15px] font-bold tracking-tight lowercase">{brand.name}<span className="text-signal-solar font-bold">°</span></span>
          </div>

          <div className="flex items-center gap-6">
            {/* Audio Feedback controller */}
            <button 
              onClick={handleAudioToggle}
              className={`p-2.5 rounded-full border border-hairline transition-all flex items-center justify-center ${
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
              className="text-[11px] font-mono uppercase tracking-widest px-5 py-2.5 rounded-full bg-bone-100 text-carbon-950 hover:bg-bone-200 transition-colors font-semibold shadow-md"
            >
              Open app
            </Link>
          </div>
        </header>

        {/* HERO SECTION */}
        <section className="relative min-h-[calc(100vh-68px)] flex flex-col justify-between py-12 px-6">
          <div className="max-w-6xl mx-auto w-full grid grid-cols-1 lg:grid-cols-12 gap-12 items-center my-auto">
            
            {/* Left Specifications (Ingredients List design style) */}
            <div className="lg:col-span-3 order-2 lg:order-1 space-y-6">
              <div className="flex items-center gap-2 text-bone-400">
                <Cpu size={12} className="text-signal-solar" />
                <span className="label-cap">Specifications Matrix</span>
              </div>
              <div className="bg-carbon-900/60 backdrop-blur-md border border-hairline rounded-xl divide-y divide-hairline font-mono">
                {specs.map(spec => (
                  <div key={spec.label} className="p-3.5 flex justify-between items-center text-[10px]">
                    <span className="text-bone-500 tracking-wider">{spec.label}</span>
                    <span className="text-bone-200 font-medium">{spec.val}</span>
                  </div>
                ))}
              </div>
              <div className="p-4 bg-signal-solar/5 border border-signal-solar/10 rounded-xl relative overflow-hidden">
                <div className="flex items-center gap-2 mb-1.5 relative z-10">
                  <div className="w-1.5 h-1.5 rounded-full bg-signal-flow animate-ping" />
                  <span className="text-[11px] font-mono text-signal-solar font-bold uppercase tracking-wider">SUNSPEC LOCAL GATEWAY</span>
                </div>
                <p className="text-[11px] text-bone-400 leading-relaxed relative z-10">
                  Secure local polling cycles running without cloud latency. Connect client devices directly via registers.
                </p>
              </div>
            </div>

            {/* Center 3D Inverter Rotation */}
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
                  className="preserve-3d absolute w-[200px] h-[280px] transition-transform duration-100"
                  style={{ transform: `rotateX(${rotation.x}deg) rotateY(${rotation.y}deg)` }}
                >
                  {/* FRONT FACE (LCD display Console) */}
                  <div 
                    onClick={() => focusHotspot('leds')}
                    className={`absolute inset-0 bg-carbon-900 border-2 rounded-xl flex flex-col justify-between p-4 backface-hidden shadow-2xl transition-colors cursor-pointer ${
                      activeHotspot === 'leds' ? 'border-signal-solar' : 'border-bone-300'
                    }`}
                    style={{ transform: 'translateZ(40px)' }}
                  >
                    <div className="flex justify-between items-start">
                      <div className="space-y-0.5">
                        <h4 className="text-[12px] font-bold text-bone-100 font-mono tracking-tight lowercase">
                          {brand.name}<span className="text-signal-solar">°</span>
                        </h4>
                        <p className="text-[7px] text-bone-500 font-mono">HYBRID INVERTER H-9600</p>
                      </div>
                      <div className="flex items-center gap-1">
                        <div className="w-1.5 h-1.5 rounded-full bg-signal-flow animate-pulse-soft" />
                        <span className="text-[7px] text-bone-400 font-mono">COMM</span>
                      </div>
                    </div>

                    {/* LCD Grid Data */}
                    <div className="bg-carbon-950 border border-hairline p-2.5 rounded-lg font-mono space-y-1 text-left relative overflow-hidden">
                      <div className="flex justify-between text-[8px] text-bone-500">
                        <span>METRIC</span>
                        <span>VALUE</span>
                      </div>
                      <div className="h-px bg-hairline mb-1" />
                      <div className="flex justify-between text-[9px] text-signal-solar">
                        <span>PV ARRAY:</span>
                        <span>3.42 kW</span>
                      </div>
                      <div className="flex justify-between text-[9px] text-signal-flow">
                        <span>HOME LOAD:</span>
                        <span>1.18 kW</span>
                      </div>
                      <div className="flex justify-between text-[9px] text-signal-battery">
                        <span>BATT SOC:</span>
                        <span>76.4%</span>
                      </div>
                      <div className="flex justify-between text-[9px] text-signal-grid">
                        <span>GRID export:</span>
                        <span>+2.24 kW</span>
                      </div>
                    </div>

                    {/* Grills */}
                    <div className="space-y-1">
                      <div className="flex gap-1 justify-center">
                        {[...Array(6)].map((_, i) => (
                          <div key={i} className="w-6 h-1 bg-carbon-800 rounded-full" />
                        ))}
                      </div>
                      <p className="text-[6px] text-center text-bone-600 font-mono tracking-wider">
                        SUNSPEC REGISTER COMPLIANCE MODULE
                      </p>
                    </div>

                    {/* Hotspot indicator marker */}
                    <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 flex items-center justify-center">
                      <div className="size-3 rounded-full bg-signal-solar/35 animate-ping absolute" />
                      <div className="size-1.5 rounded-full bg-signal-solar" />
                    </div>
                  </div>

                  {/* BACK FACE (Passive heat dissipation fins) */}
                  <div 
                    onClick={() => focusHotspot('heatsink')}
                    className={`absolute inset-0 bg-carbon-950 border-2 rounded-xl flex flex-col justify-between p-4 backface-hidden cursor-pointer transition-colors ${
                      activeHotspot === 'heatsink' ? 'border-signal-solar' : 'border-bone-600/60'
                    }`}
                    style={{ transform: 'rotateY(180deg) translateZ(40px)' }}
                  >
                    <div className="text-[7px] text-bone-500 font-mono tracking-widest text-center uppercase">
                      HEATSINK ELEMENT - PASSIVE DISPERSAL
                    </div>
                    <div className="flex-grow my-4 flex flex-col justify-between">
                      {[...Array(10)].map((_, i) => (
                        <div key={i} className="w-full h-1.5 bg-carbon-800 border-b border-carbon-950" />
                      ))}
                    </div>
                    <div className="text-[6px] text-center text-bone-600 font-mono">
                      ANODIZED INTEGRAL ALUMINUM GRIDS
                    </div>

                    <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 flex items-center justify-center">
                      <div className="size-3 rounded-full bg-signal-solar/35 animate-ping absolute" />
                      <div className="size-1.5 rounded-full bg-signal-solar" />
                    </div>
                  </div>

                  {/* LEFT FACE (DC Breaker entries) */}
                  <div 
                    onClick={() => focusHotspot('breaker')}
                    className={`absolute top-0 bottom-0 w-[80px] bg-carbon-850 border-y-2 border-l-2 border-r rounded-xl backface-hidden flex flex-col justify-between p-3 text-center cursor-pointer transition-colors ${
                      activeHotspot === 'breaker' ? 'border-signal-solar' : 'border-bone-500/60'
                    }`}
                    style={{ 
                      left: '60px', 
                      transform: 'rotateY(-90deg) translateZ(100px)' 
                    }}
                  >
                    <span className="text-[6px] text-bone-500 font-mono uppercase">SAFETY ISOLATION</span>
                    <div className="space-y-2">
                      <div className="size-6 rounded-full bg-carbon-950 border border-hairline mx-auto flex items-center justify-center">
                        <div className="w-4 h-1.5 bg-signal-alert rounded-sm rotate-45" />
                      </div>
                      <div className="size-6 rounded-full bg-carbon-950 border border-hairline mx-auto flex items-center justify-center">
                        <div className="w-4 h-1.5 bg-signal-alert rounded-sm rotate-45" />
                      </div>
                    </div>
                    <span className="text-[6px] text-bone-600 font-mono">DC BREAKER</span>

                    <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 flex items-center justify-center">
                      <div className="size-3 rounded-full bg-signal-solar/35 animate-ping absolute" />
                      <div className="size-1.5 rounded-full bg-signal-solar" />
                    </div>
                  </div>

                  {/* RIGHT FACE (RJ45 modbus network socket) */}
                  <div 
                    onClick={() => focusHotspot('rj45')}
                    className={`absolute top-0 bottom-0 w-[80px] bg-carbon-850 border-y-2 border-r-2 border-l rounded-xl backface-hidden flex flex-col justify-between p-3 text-center cursor-pointer transition-colors ${
                      activeHotspot === 'rj45' ? 'border-signal-solar' : 'border-bone-500/60'
                    }`}
                    style={{ 
                      left: '60px', 
                      transform: 'rotateY(90deg) translateZ(100px)' 
                    }}
                  >
                    <span className="text-[6px] text-bone-500 font-mono uppercase">NETWORK PORT</span>
                    <div className="space-y-3">
                      <div className="w-8 h-4 bg-carbon-950 border border-hairline rounded mx-auto flex items-center justify-center">
                        <span className="text-[5px] text-bone-300 font-mono">RJ45 MODBUS</span>
                      </div>
                      <div className="size-4 bg-carbon-900 border border-hairline rounded-full mx-auto flex items-center justify-center">
                        <div className="size-2 bg-signal-flow rounded-full animate-ping" />
                      </div>
                    </div>
                    <span className="text-[6px] text-bone-600 font-mono">LAN BUS</span>

                    <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 flex items-center justify-center">
                      <div className="size-3 rounded-full bg-signal-solar/35 animate-ping absolute" />
                      <div className="size-1.5 rounded-full bg-signal-solar" />
                    </div>
                  </div>

                  {/* TOP FACE */}
                  <div 
                    className="absolute left-0 right-0 h-[80px] bg-carbon-800 border-x-2 border-t-2 border-b border-bone-500/60 rounded-xl backface-hidden p-2 flex items-center justify-center"
                    style={{ 
                      top: '100px', 
                      transform: 'rotateX(90deg) translateZ(140px)' 
                    }}
                  />

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

              {/* Hotspot details overlay */}
              <div className="min-h-[64px] max-w-xs text-center mt-4 space-y-1 font-mono">
                {activeHotspot ? (
                  <div className="bg-carbon-900/80 border border-signal-solar/30 p-2.5 rounded-lg text-left">
                    <div className="flex items-center gap-1.5 mb-1">
                      <Info size={11} className="text-signal-solar" />
                      <span className="text-[10px] font-bold text-signal-solar uppercase">
                        {hotspotsData[activeHotspot].title}
                      </span>
                    </div>
                    <p className="text-[9px] text-bone-400 leading-normal">
                      {hotspotsData[activeHotspot].text}
                    </p>
                  </div>
                ) : (
                  <span className="text-[9px] tracking-widest text-bone-500 uppercase animate-pulse-soft">
                    [ CLICK HOTSPOTS OR DRAG INVERTER ]
                  </span>
                )}
              </div>
            </div>

            {/* Right Pitch (Display Font with outline dividers) */}
            <div className="lg:col-span-4 order-3 space-y-6 text-left">
              <span className="label-cap text-signal-solar">{brand.id === 'helios' ? 'Solar intelligence' : `By ${brand.name}`}</span>
              <div className="space-y-2">
                <h1 className="text-5xl md:text-6xl font-bold font-mono tracking-tighter leading-none uppercase">
                  <span className="text-outline">LOCAL FIRST</span>
                  <br />
                  <span className="text-shimmer italic font-display lowercase font-normal leading-[0.8] block mb-2">grid telemetry.</span>
                  <span>SUNSPEC CORE</span>
                </h1>
              </div>
              
              <div className="w-full h-px bg-hairline relative">
                <div className="absolute top-[-4px] left-0 size-2 rounded-full bg-signal-solar" />
              </div>

              <p className="text-bone-400 text-[14px] leading-relaxed max-w-sm">
                {brand.tagline} Interrogate registers, map yields, and forecast arrays directly from client-side Web Modbus connectors.
              </p>

              <div className="flex flex-wrap gap-3.5 pt-2">
                <Link
                  to={appHref}
                  onClick={() => playClick('click')}
                  className="inline-flex items-center gap-2 px-6 py-3.5 rounded-full bg-bone-100 text-carbon-950 hover:bg-bone-200 transition-all text-[13px] font-bold shadow-lg transform hover:-translate-y-0.5"
                >
                  Launch App Console
                  <ArrowRight size={14} strokeWidth={2} />
                </Link>
                <button
                  onClick={trigger360Spin}
                  className="inline-flex items-center gap-2 px-5 py-3.5 rounded-full border border-hairline text-bone-300 hover:text-bone-100 hover:bg-carbon-900/40 transition-all text-[13px]"
                >
                  <RefreshCw size={13} className="animate-spin-slow" />
                  Sweep Chassis
                </button>
              </div>
            </div>

          </div>

          {/* Marquee Banner */}
          <div className="w-full overflow-hidden border-y border-hairline py-4.5 mt-12 bg-carbon-900/30 relative z-20">
            <div className="flex w-[200%] animate-marquee">
              {[...Array(2)].map((_, i) => (
                <div key={i} className="flex justify-around min-w-full font-mono text-[9px] tracking-widest text-bone-500 uppercase select-none">
                  <span>SUNSPEC MODBUS PROTOCOL v1.2</span>
                  <span className="text-signal-solar">·</span>
                  <span>SMA SPEEDWIRE GATEWAYS</span>
                  <span className="text-signal-solar">·</span>
                  <span>FRONIUS SYMO INVERTERS</span>
                  <span className="text-signal-solar">·</span>
                  <span>SOLAREDGE POWER REGULATORS</span>
                  <span className="text-signal-solar">·</span>
                  <span>ENPHASE ENERGY ROUTERS</span>
                  <span className="text-signal-solar">·</span>
                  <span>VICTRON CERBO GX LINKS</span>
                  <span className="text-signal-solar">·</span>
                  <span>SUNGROW ARRAY STRINGS</span>
                  <span className="text-signal-solar">·</span>
                </div>
              ))}
            </div>
          </div>
        </section>

        {/* METRICS & PHILOSOPHY */}
        <section className="py-24 px-6 border-b border-hairline bg-carbon-900/10">
          <div className="max-w-4xl mx-auto text-center space-y-12">
            <span className="label-cap text-bone-500">Telemetry Philosophy</span>
            <h2 className="text-3xl md:text-5xl font-mono uppercase tracking-tight leading-[1.0] text-bone-100 max-w-2xl mx-auto">
              Open standards yield <span className="text-shimmer italic font-display lowercase font-normal">absolute sovereign metrics.</span>
            </h2>
            
            <div className="grid grid-cols-2 md:grid-cols-4 gap-6 pt-8 font-mono">
              <div className="p-6 bg-carbon-900/50 border border-hairline rounded-xl space-y-1 text-center shadow-sm">
                <span className="num-display text-5xl text-signal-solar block font-bold">1.0s</span>
                <span className="label-cap text-[8px]">Sampling Interval</span>
              </div>
              <div className="p-6 bg-carbon-900/50 border border-hairline rounded-xl space-y-1 text-center shadow-sm">
                <span className="num-display text-5xl text-signal-flow block font-bold">99.4%</span>
                <span className="label-cap text-[8px]">Query Fidelity</span>
              </div>
              <div className="p-6 bg-carbon-900/50 border border-hairline rounded-xl space-y-1 text-center shadow-sm">
                <span className="num-display text-5xl text-signal-grid block font-bold">0</span>
                <span className="label-cap text-[8px]">Server Databases</span>
              </div>
              <div className="p-6 bg-carbon-900/50 border border-hairline rounded-xl space-y-1 text-center shadow-sm">
                <span className="num-display text-5xl text-signal-battery block font-bold">100%</span>
                <span className="label-cap text-[8px]">Offline Autonomy</span>
              </div>
            </div>
          </div>
        </section>

        {/* BENTO GRID INTERACTIVE CONSOLE */}
        <section className="py-24 px-6 max-w-6xl mx-auto w-full">
          <div className="text-center mb-16 space-y-3">
            <span className="label-cap text-signal-solar">Console Simulator</span>
            <h2 className="text-3xl md:text-4xl font-mono uppercase tracking-tight text-bone-100">
              Interactive Grid Orchestrator
            </h2>
            <p className="text-bone-400 text-sm max-w-md mx-auto">
              Manipulate telemetry variables on the live console below to review direct modular pipeline reactions.
            </p>
          </div>

          {/* Interactive Selector Tabs */}
          <div className="flex flex-wrap gap-2.5 justify-center mb-12">
            {featuresList.map(feat => {
              const isSelected = activeTab === feat.id;
              return (
                <button
                  key={feat.id}
                  onClick={() => {
                    playClick('toggle');
                    setActiveTab(feat.id as any);
                  }}
                  className={`px-4.5 py-2.5 rounded-full border text-[11px] font-mono uppercase tracking-wider transition-all duration-300 ${
                    isSelected 
                      ? 'bg-bone-100 text-carbon-950 border-bone-100 font-bold shadow-md' 
                      : 'bg-carbon-900/40 text-bone-400 border-hairline hover:text-bone-200 hover:border-bone-600'
                  }`}
                >
                  {feat.title}
                </button>
              );
            })}
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-stretch">
            
            {/* Left selector details column */}
            <div className="lg:col-span-4 flex flex-col justify-between space-y-6">
              <div className="bg-carbon-900/60 border border-hairline p-6 rounded-2xl space-y-4">
                <div className="flex items-center gap-2">
                  <div className="p-2 bg-signal-solar/15 text-signal-solar rounded-lg border border-signal-solar/30">
                    {(() => {
                      const feat = featuresList.find(f => f.id === activeTab);
                      const Icon = feat ? feat.Icon : Activity;
                      return <Icon size={16} />;
                    })()}
                  </div>
                  <span className="label-cap text-[9px] text-bone-400 font-bold tracking-widest uppercase">
                    {featuresList.find(f => f.id === activeTab)?.label}
                  </span>
                </div>
                
                <h3 className="text-lg font-bold text-bone-100 font-sans tracking-tight">
                  {featuresList.find(f => f.id === activeTab)?.title}
                </h3>
                
                <p className="text-[13px] text-bone-400 leading-relaxed font-sans">
                  {featuresList.find(f => f.id === activeTab)?.desc}
                </p>

                <div className="pt-2 flex items-center gap-2 text-[11px] text-signal-flow font-mono uppercase font-bold">
                  <ShieldCheck size={14} />
                  <span>Sovereign local calculation</span>
                </div>
              </div>

              {/* Action controller settings block */}
              <div className="bg-carbon-900/40 border border-hairline p-5 rounded-2xl space-y-4">
                <span className="label-cap text-bone-500">Live Parameters</span>
                
                {activeTab === 'flow' && (
                  <div className="space-y-3 font-mono">
                    <span className="text-[10px] text-bone-400 block uppercase">GRID CONNECTION STATE:</span>
                    <div className="grid grid-cols-2 gap-2">
                      <button
                        onClick={() => {
                          playClick('click');
                          setGridFeedDirection('export');
                        }}
                        className={`py-2 rounded-lg border text-[9px] uppercase tracking-widest transition-colors ${
                          gridFeedDirection === 'export' 
                            ? 'bg-signal-solar/15 text-signal-solar border-signal-solar/40 font-bold' 
                            : 'bg-carbon-950 border-hairline text-bone-500 hover:text-bone-300'
                        }`}
                      >
                        GRID EXPORT
                      </button>
                      <button
                        onClick={() => {
                          playClick('click');
                          setGridFeedDirection('import');
                        }}
                        className={`py-2 rounded-lg border text-[9px] uppercase tracking-widest transition-colors ${
                          gridFeedDirection === 'import' 
                            ? 'bg-signal-grid/15 text-signal-grid border-signal-grid/40 font-bold' 
                            : 'bg-carbon-950 border-hairline text-bone-500 hover:text-bone-300'
                        }`}
                      >
                        GRID IMPORT
                      </button>
                    </div>
                  </div>
                )}

                {activeTab === 'forecast' && (
                  <div className="space-y-3 font-mono">
                    <div className="flex justify-between text-[10px] text-bone-400 uppercase">
                      <span>CLOUD COVER INDEX:</span>
                      <span className="text-signal-solar font-bold">{(cloudDensity * 100).toFixed(0)}%</span>
                    </div>
                    <input 
                      type="range"
                      min="0.2"
                      max="1.2"
                      step="0.05"
                      value={cloudDensity}
                      onChange={(e) => {
                        setCloudDensity(parseFloat(e.target.value));
                      }}
                      className="w-full accent-signal-solar cursor-ew-resize bg-carbon-950 border border-hairline rounded-lg"
                    />
                    <span className="text-[8px] text-bone-500 block leading-tight">
                      Adjusting solar radiation modifies background hum frequency & volume dynamically.
                    </span>
                  </div>
                )}

                {activeTab === 'strings' && (
                  <div className="space-y-3 font-mono">
                    <span className="text-[10px] text-bone-400 block uppercase">SHADING FAULT SIMULATION:</span>
                    <button
                      onClick={() => {
                        playClick('click');
                        setSimulateShading(!simulateShading);
                      }}
                      className={`w-full py-2.5 rounded-lg border text-[10px] uppercase font-bold tracking-widest transition-colors ${
                        simulateShading 
                          ? 'bg-signal-alert/20 text-signal-alert border-signal-alert/50' 
                          : 'bg-carbon-950 border-hairline text-bone-400 hover:text-bone-200'
                      }`}
                    >
                      {simulateShading ? 'CLEAR SHADING FAULT' : 'SIMULATE PANEL SHADING'}
                    </button>
                  </div>
                )}

                {activeTab === 'battery' && (
                  <div className="space-y-2.5 font-mono">
                    <span className="text-[10px] text-bone-400 block uppercase">CHARGE DISPATCH STRATEGY:</span>
                    <div className="space-y-1.5">
                      {[
                        { id: 'self', label: 'SELF CONSUMPTION', color: 'border-signal-flow/35 text-signal-flow' },
                        { id: 'tou', label: 'TIME OF USE RATE', color: 'border-signal-solar/35 text-signal-solar' },
                        { id: 'backup', label: 'EMERGENCY BACKUP', color: 'border-signal-battery/35 text-signal-battery' }
                      ].map(str => (
                        <button
                          key={str.id}
                          onClick={() => {
                            playClick('click');
                            setBatteryStrategy(str.id as any);
                          }}
                          className={`w-full text-left p-2 rounded-lg border text-[9px] uppercase tracking-wider transition-colors ${
                            batteryStrategy === str.id 
                              ? `bg-carbon-950 font-bold ${str.color}` 
                              : 'bg-carbon-950/40 border-hairline text-bone-500 hover:text-bone-300'
                          }`}
                        >
                          {str.label}
                        </button>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            </div>

            {/* Right interactive console block (Sleek bento layout grid) */}
            <div className="lg:col-span-8 flex items-stretch">
              <div className="relative w-full bg-carbon-900 border border-bone-600/40 rounded-2xl p-6 shadow-2xl flex flex-col justify-between overflow-hidden min-h-[360px] md:min-h-[440px]">
                <div className="grid-bg absolute inset-0 opacity-5 pointer-events-none" />
                
                {/* Console header */}
                <div className="flex justify-between items-center text-[10px] font-mono border-b border-hairline pb-3 relative z-10">
                  <div className="flex items-center gap-1.5">
                    <HeliosMark size={16} brand={brand} />
                    <span className="font-bold text-bone-200">HELIOS CONSOLE OS</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="size-2 rounded-full bg-signal-flow animate-ping" />
                    <span className="px-2 py-0.5 rounded bg-carbon-950 border border-hairline text-signal-flow uppercase font-bold text-[8px] tracking-wider font-mono">
                      POLLING ACTIVE
                    </span>
                  </div>
                </div>

                {/* Dynamic Screen Viewport */}
                <div className="flex-grow my-6 relative flex flex-col justify-center items-stretch z-10">
                  
                  {activeTab === 'flow' && (
                    <div className="w-full h-full flex flex-col justify-between py-4">
                      {/* Interactive SVG Flow Diagram */}
                      <div className="flex-grow flex flex-col justify-center items-center relative min-h-[220px]">
                        
                        {/* central HUB */}
                        <div className="size-20 rounded-full bg-carbon-950 border-2 border-bone-200 shadow-glow-bone flex flex-col justify-center items-center z-20">
                          <span className="text-[8px] font-mono text-bone-500 uppercase tracking-wider">ENGINE</span>
                          <span className="text-[11px] font-mono font-bold text-bone-100">
                            {(3.42 + (gridFeedDirection === 'import' ? 1.4 : -1.4)).toFixed(2)} kW
                          </span>
                        </div>

                        {/* Top Solar Node */}
                        <div className="absolute top-0 flex flex-col items-center">
                          <div className="size-14 rounded-xl bg-carbon-950 border border-signal-solar/50 flex flex-col justify-center items-center text-signal-solar shadow-md">
                            <Sun size={18} />
                            <span className="text-[9px] font-mono mt-1 font-bold">3.42 kW</span>
                          </div>
                          <span className="label-cap text-[8px] mt-1.5 text-bone-400">Solar Array</span>
                        </div>

                        {/* Bottom Left Battery Node */}
                        <div className="absolute bottom-0 left-6 md:left-24 flex flex-col items-center">
                          <div className="size-14 rounded-xl bg-carbon-950 border border-signal-battery/50 flex flex-col justify-center items-center text-signal-battery shadow-md">
                            <BatteryCharging size={18} />
                            <span className="text-[9px] font-mono mt-1 font-bold">76.4%</span>
                          </div>
                          <span className="label-cap text-[8px] mt-1.5 text-bone-400">Battery Storage</span>
                        </div>

                        {/* Bottom Right Grid Node */}
                        <div className="absolute bottom-0 right-6 md:right-24 flex flex-col items-center">
                          <div className={`size-14 rounded-xl bg-carbon-950 border flex flex-col justify-center items-center shadow-md transition-colors ${
                            gridFeedDirection === 'export' ? 'border-signal-solar/50 text-signal-solar' : 'border-signal-grid/50 text-signal-grid'
                          }`}>
                            <Zap size={18} />
                            <span className="text-[9px] font-mono mt-1 font-bold">
                              {gridFeedDirection === 'export' ? '+1.40 kW' : '-1.40 kW'}
                            </span>
                          </div>
                          <span className="label-cap text-[8px] mt-1.5 text-bone-400">
                            {gridFeedDirection === 'export' ? 'Grid Export' : 'Grid Import'}
                          </span>
                        </div>

                        {/* Telemetry flow line pathways */}
                        <svg className="absolute inset-0 size-full pointer-events-none z-0" viewBox="0 0 400 240">
                          {/* Solar to Hub */}
                          <path 
                            d="M 200 60 L 200 120" 
                            fill="none" 
                            stroke="var(--signal-solar)" 
                            strokeWidth="1.5" 
                            className="telemetry-flow-path"
                          />
                          {/* Hub to Battery */}
                          <path 
                            d="M 200 120 L 120 180" 
                            fill="none" 
                            stroke="var(--signal-battery)" 
                            strokeWidth="1.5" 
                            className="telemetry-flow-path"
                            style={{ animationDirection: 'reverse' }}
                          />
                          {/* Hub to Grid */}
                          <path 
                            d="M 200 120 L 280 180" 
                            fill="none" 
                            stroke={gridFeedDirection === 'export' ? 'var(--signal-solar)' : 'var(--signal-grid)'} 
                            strokeWidth="1.5" 
                            className="telemetry-flow-path"
                            style={{ animationDirection: gridFeedDirection === 'export' ? 'normal' : 'reverse' }}
                          />
                        </svg>

                      </div>
                    </div>
                  )}

                  {activeTab === 'forecast' && (
                    <div className="w-full h-full flex flex-col justify-between py-2 text-left">
                      <div className="flex items-center justify-between text-[11px] font-mono mb-4 border-b border-hairline pb-2">
                        <span className="text-bone-400 uppercase tracking-widest">Solar Radiation Forecast</span>
                        <span className="text-signal-solar font-bold">Yield: {(54.8 * cloudDensity).toFixed(1)} kWh/day</span>
                      </div>
                      
                      {/* Weather graph columns */}
                      <div className="flex-grow flex items-end gap-2.5 h-[160px] bg-carbon-950/60 border border-hairline p-4 rounded-xl">
                        {[
                          { day: 'MON', val: 0.35, label: '34 kWh' },
                          { day: 'TUE', val: 0.65, label: '48 kWh' },
                          { day: 'WED', val: 0.95, label: '72 kWh' },
                          { day: 'THU', val: 0.72, label: '58 kWh' },
                          { day: 'FRI', val: 0.42, label: '38 kWh' },
                          { day: 'SAT', val: 0.82, label: '64 kWh' },
                          { day: 'SUN', val: 0.60, label: '46 kWh' }
                        ].map((item, idx) => {
                          const heightPct = Math.min(100, Math.floor(item.val * cloudDensity * 100));
                          return (
                            <div key={idx} className="flex-grow flex flex-col justify-end items-center h-full group">
                              <span className="text-[7.5px] font-mono text-signal-solar opacity-0 group-hover:opacity-100 transition-opacity mb-1 font-bold">
                                {((item.val * cloudDensity) * 75).toFixed(0)} kWh
                              </span>
                              <div 
                                className="w-full rounded-t bg-carbon-800 border border-hairline transition-all duration-300"
                                style={{ 
                                  height: `${heightPct}%`,
                                  backgroundColor: heightPct > 70 ? 'rgba(240, 198, 116, 0.4)' : 'rgba(239, 236, 229, 0.08)',
                                  borderColor: heightPct > 70 ? 'var(--signal-solar)' : 'var(--hairline)'
                                }}
                              />
                              <span className="text-[8px] font-mono text-bone-500 mt-2.5">{item.day}</span>
                            </div>
                          );
                        })}
                      </div>
                    </div>
                  )}

                  {activeTab === 'strings' && (
                    <div className="w-full h-full flex flex-col justify-between py-2 text-left">
                      <div className="text-[11px] font-mono text-bone-400 mb-4 flex items-center justify-between border-b border-hairline pb-2">
                        <div className="flex items-center gap-1.5">
                          <Cable size={12} className="text-signal-solar" />
                          <span>STRING INPUT COMPARATOR REGISTER</span>
                        </div>
                        {simulateShading && (
                          <span className="text-signal-alert font-bold uppercase text-[9px] animate-pulse-soft">
                            [ Shading Detected ]
                          </span>
                        )}
                      </div>
                      <div className="space-y-5 flex-grow justify-center flex flex-col">
                        {[
                          { name: 'STRING A (SOUTH)', volt: 412, current: 8.2, status: 'NOMINAL', width: 'w-[90%]', color: 'bg-signal-flow border-signal-flow' },
                          { 
                            name: 'STRING B (WEST)', 
                            volt: simulateShading ? 154 : 398, 
                            current: simulateShading ? 2.4 : 7.9, 
                            status: simulateShading ? 'SHADING FAULT' : 'NOMINAL', 
                            width: simulateShading ? 'w-[35%]' : 'w-[85%]', 
                            color: simulateShading ? 'bg-signal-alert border-signal-alert' : 'bg-signal-flow border-signal-flow' 
                          },
                          { name: 'STRING C (EAST)', volt: 405, current: 8.0, status: 'NOMINAL', width: 'w-[88%]', color: 'bg-signal-flow border-signal-flow' }
                        ].map((str, idx) => (
                          <div key={idx} className="space-y-1.5">
                            <div className="flex justify-between text-[9px] font-mono">
                              <span className="text-bone-300 font-bold">{str.name}</span>
                              <span className="text-bone-500">
                                {str.volt}V · {str.current}A · <span className={str.status !== 'NOMINAL' ? 'text-signal-alert font-bold' : ''}>{str.status}</span>
                              </span>
                            </div>
                            <div className="w-full h-2 bg-carbon-950 border border-hairline rounded-full overflow-hidden">
                              <div className={`h-full border rounded-full transition-all duration-500 ${str.color} ${str.width}`} />
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}

                  {activeTab === 'battery' && (
                    <div className="w-full h-full flex flex-col justify-center items-center py-2 space-y-4">
                      {/* Charging dial */}
                      <div className="relative size-32 flex items-center justify-center">
                        <svg viewBox="0 0 100 100" className="size-full transform -rotate-90">
                          <circle cx="50" cy="50" r="42" fill="none" stroke="rgba(239,236,229,0.06)" strokeWidth="6" />
                          <circle 
                            cx="50" 
                            cy="50" 
                            r="42" 
                            fill="none" 
                            stroke={
                              batteryStrategy === 'self' ? '#7fb069' : batteryStrategy === 'tou' ? '#f0c674' : '#c5a572'
                            } 
                            strokeWidth="6" 
                            strokeDasharray="263.8" 
                            strokeDashoffset={
                              batteryStrategy === 'self' ? '62.4' : batteryStrategy === 'tou' ? '120.5' : '15.2'
                            } 
                            strokeLinecap="round" 
                            className="transition-all duration-700" 
                          />
                        </svg>
                        <div className="absolute flex flex-col items-center justify-center font-mono">
                          <span className="text-2xl font-bold text-bone-100 leading-none">
                            {batteryStrategy === 'self' ? '76.4%' : batteryStrategy === 'tou' ? '54.2%' : '94.5%'}
                          </span>
                          <span className="text-[8px] text-bone-500 tracking-wider uppercase mt-1">
                            {batteryStrategy === 'self' ? 'DISCHARGING' : batteryStrategy === 'tou' ? 'PEAK SHAVING' : 'STANDBY'}
                          </span>
                        </div>
                      </div>
                      
                      <div className="grid grid-cols-2 gap-2.5 w-full text-center font-mono max-w-sm">
                        <div className="p-2.5 bg-carbon-950 border border-hairline rounded-lg">
                          <span className="text-[7px] text-bone-500 block uppercase">DISPATCH POWER</span>
                          <span className={`text-[11px] font-bold ${batteryStrategy === 'backup' ? 'text-bone-400' : 'text-signal-alert'}`}>
                            {batteryStrategy === 'self' ? '-1.40 kW' : batteryStrategy === 'tou' ? '-2.80 kW' : '0.00 kW'}
                          </span>
                        </div>
                        <div className="p-2.5 bg-carbon-950 border border-hairline rounded-lg">
                          <span className="text-[7px] text-bone-500 block uppercase">STRATEGY CODE</span>
                          <span className="text-[11px] font-bold text-bone-200 uppercase">
                            {batteryStrategy === 'self' ? 'SELF-CON' : batteryStrategy === 'tou' ? 'RATE-TOU' : 'EMER-BACK'}
                          </span>
                        </div>
                      </div>
                    </div>
                  )}

                </div>

                {/* Console footer */}
                <div className="flex justify-between items-center text-[9px] font-mono border-t border-hairline pt-3 text-bone-500 relative z-10">
                  <span>SUNSPEC ADDR: IP.192.168.1.84</span>
                  <span>HELIOS PIPELINE VER.4.0</span>
                </div>

              </div>
            </div>

          </div>
        </section>

        {/* TESTIMONIALS */}
        <section className="py-24 border-y border-hairline bg-carbon-900/30 overflow-hidden relative z-20">
          <div className="max-w-6xl mx-auto px-6 mb-12 flex flex-col md:flex-row md:items-end justify-between gap-6">
            <div className="space-y-3">
              <span className="label-cap text-signal-solar">INSTALLER LOGISTICS</span>
              <h2 className="text-3xl font-mono uppercase tracking-tight text-bone-100">
                Endorsed by Grid Professionals
              </h2>
            </div>
            <p className="text-bone-400 text-sm max-w-sm">
              Read how solar integrators, home automation engineers, and tech leads utilize Helios to manage inverters.
            </p>
          </div>

          <div className="flex w-[200%] animate-marquee gap-6 px-6">
            {[...Array(2)].map((_, i) => (
              <div key={i} className="flex gap-6 min-w-full justify-around select-none">
                {testimonials.map((test, index) => (
                  <div key={index} className="w-[300px] flex-shrink-0 bg-carbon-900/80 border border-hairline p-6 rounded-xl flex flex-col justify-between gap-6 shadow-sm">
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

        {/* TECHNICAL FAQ ACCORDION */}
        <section className="py-24 px-6 max-w-3xl mx-auto w-full">
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
                  className="bg-carbon-900/40 border border-hairline rounded-xl overflow-hidden transition-all duration-300 shadow-sm"
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

        {/* CALL TO ACTION */}
        <section className="py-24 px-6 max-w-4xl mx-auto text-center border-t border-hairline relative z-20 w-full">
          <div className="flex justify-center mb-6">
            <HeliosMark size={56} brand={brand} />
          </div>
          <h2 className="text-4xl md:text-5xl font-mono tracking-tighter uppercase text-bone-100">
            Monitor your array today.
          </h2>
          <p className="text-bone-400 text-[14px] mt-4 max-w-md mx-auto leading-relaxed">
            The application boots in simulated telemetry mode by default. Explore diagnostic graphs, weather forecast modeling, and battery rate planners without modifying hardware.
          </p>
          <div className="mt-8 flex justify-center">
            <Link
              to={appHref}
              onClick={() => playClick('click')}
              className="inline-flex items-center gap-2 px-8 py-4 rounded-full bg-bone-100 text-carbon-950 hover:bg-bone-200 transition-colors text-[14px] font-bold shadow-lg"
            >
              Open System App
              <ArrowRight size={14} strokeWidth={2} />
            </Link>
          </div>
        </section>

        {/* GRID FOOTER (Huge centering font) */}
        <footer className="border-t border-hairline bg-carbon-950/85 pt-16 pb-8 px-6 relative overflow-hidden mt-auto">
          
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
                Precision energy intelligence compliant with SunSpec Modbus standards. Optimized client-side telemetry analysis frameworks.
              </p>
            </div>

            <div className="md:col-span-6 flex flex-col md:items-end justify-between gap-4 font-mono text-[10px] text-bone-400">
              <div className="space-y-1 md:text-right">
                <span className="block text-bone-500 text-[9px] uppercase tracking-widest">SUPPORT REGISTRY</span>
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
              <span>{brand.id === 'helios' ? 'Decentralized Grid Mesh' : `Powered by helios°`}</span>
            </div>
          </div>

        </footer>

      </div>
    </div>
  );
}
