class AudioEngine {
  private ctx: AudioContext | null = null;
  private humOscs: OscillatorNode[] = [];
  private humGain: GainNode | null = null;
  private isHumming = false;

  private initCtx() {
    if (!this.ctx) {
      const AudioCtx = window.AudioContext || (window as any).webkitAudioContext;
      if (AudioCtx) {
        this.ctx = new AudioCtx();
      }
    }
    if (this.ctx && this.ctx.state === 'suspended') {
      this.ctx.resume();
    }
  }

  playRelayClick(isClosing = true) {
    try {
      this.initCtx();
      if (!this.ctx) return;
      const now = this.ctx.currentTime;

      // Click 1: transient noise burst
      const bufferSize = this.ctx.sampleRate * 0.005; // 5ms
      const buffer = this.ctx.createBuffer(1, bufferSize, this.ctx.sampleRate);
      const data = buffer.getChannelData(0);
      for (let i = 0; i < bufferSize; i++) {
        data[i] = Math.random() * 2 - 1;
      }

      const noise = this.ctx.createBufferSource();
      noise.buffer = buffer;

      const filter = this.ctx.createBiquadFilter();
      filter.type = 'bandpass';
      filter.frequency.setValueAtTime(isClosing ? 8000 : 6000, now);
      filter.Q.setValueAtTime(5, now);

      const gain = this.ctx.createGain();
      gain.gain.setValueAtTime(0.08, now);
      gain.gain.exponentialRampToValueAtTime(0.0001, now + 0.004);

      noise.connect(filter);
      filter.connect(gain);
      gain.connect(this.ctx.destination);
      noise.start(now);

      // Click 2: slightly delayed metallic ring
      const ringOsc = this.ctx.createOscillator();
      ringOsc.type = 'triangle';
      ringOsc.frequency.setValueAtTime(isClosing ? 1200 : 950, now + 0.001);

      const ringGain = this.ctx.createGain();
      ringGain.gain.setValueAtTime(0.03, now + 0.001);
      ringGain.gain.exponentialRampToValueAtTime(0.0001, now + 0.02);

      ringOsc.connect(ringGain);
      ringGain.connect(this.ctx.destination);
      ringOsc.start(now + 0.001);
      ringOsc.stop(now + 0.02);
    } catch (e) {
      console.warn('Failed to play click:', e);
    }
  }

  startHum() {
    if (this.isHumming) return;
    try {
      this.initCtx();
      if (!this.ctx) return;
      const now = this.ctx.currentTime;

      this.humGain = this.ctx.createGain();
      this.humGain.gain.setValueAtTime(0, now);
      this.humGain.gain.linearRampToValueAtTime(0.02, now + 1.0); // slow fade-in

      // Fundamental 60Hz
      const osc1 = this.ctx.createOscillator();
      osc1.type = 'sine';
      osc1.frequency.setValueAtTime(60, now);

      // 120Hz Harmonic
      const osc2 = this.ctx.createOscillator();
      osc2.type = 'sine';
      osc2.frequency.setValueAtTime(120, now);

      // 180Hz Harmonic
      const osc3 = this.ctx.createOscillator();
      osc3.type = 'triangle';
      osc3.frequency.setValueAtTime(180, now);

      const osc2Gain = this.ctx.createGain();
      osc2Gain.gain.setValueAtTime(0.3, now);

      const osc3Gain = this.ctx.createGain();
      osc3Gain.gain.setValueAtTime(0.15, now);

      osc1.connect(this.humGain);
      
      osc2.connect(osc2Gain);
      osc2Gain.connect(this.humGain);
      
      osc3.connect(osc3Gain);
      osc3Gain.connect(this.humGain);

      const lp = this.ctx.createBiquadFilter();
      lp.type = 'lowpass';
      lp.frequency.setValueAtTime(250, now);

      this.humGain.connect(lp);
      lp.connect(this.ctx.destination);

      osc1.start(now);
      osc2.start(now);
      osc3.start(now);

      this.humOscs = [osc1, osc2, osc3];
      this.isHumming = true;
    } catch (e) {
      console.warn('Failed to start hum:', e);
    }
  }

  stopHum() {
    if (!this.isHumming || !this.humGain || !this.ctx) return;
    try {
      const now = this.ctx.currentTime;
      const currentGain = this.humGain.gain;
      currentGain.cancelScheduledValues(now);
      currentGain.setValueAtTime(currentGain.value ?? 0.02, now);
      currentGain.linearRampToValueAtTime(0, now + 0.3); // rapid fade-out

      const oscsToStop = [...this.humOscs];
      setTimeout(() => {
        try {
          oscsToStop.forEach(osc => {
            try { osc.stop(); } catch(e) {}
            try { osc.disconnect(); } catch(e) {}
          });
        } catch (e) {}
      }, 400);

      this.humOscs = [];
      this.isHumming = false;
    } catch (e) {
      console.warn('Failed to stop hum:', e);
    }
  }
}

export const telemetryAudio = new AudioEngine();
