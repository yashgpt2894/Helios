/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        carbon: {
          950: '#070708',
          900: '#0b0b0c',
          850: '#0f0f10',
          800: '#141416',
          750: '#1a1a1c',
          700: '#1c1c1e',
          600: '#2a2a2d',
          500: '#3a3a3e'
        },
        bone: {
          50: '#f8f6f0',
          100: '#f4f1ea',
          200: '#efece5',
          300: '#dcd6c8',
          400: '#bdb6a6',
          500: '#a59f90',
          600: '#7a7568',
          700: '#544f47'
        },
        signal: {
          solar: '#f0c674',
          flow: '#7fb069',
          grid: '#5d8aa8',
          battery: '#c5a572',
          alert: '#d97757'
        }
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', '-apple-system', 'sans-serif'],
        mono: ['"JetBrains Mono"', 'ui-monospace', 'SFMono-Regular', 'monospace'],
        display: ['"Instrument Serif"', 'Georgia', 'serif']
      },
      letterSpacing: {
        widest: '0.3em'
      },
      boxShadow: {
        'inner-edge': 'inset 0 1px 0 0 rgba(239,236,229,0.04)',
        'card': '0 1px 0 0 rgba(239,236,229,0.04), 0 24px 48px -16px rgba(0,0,0,0.6)',
        'glow-bone': '0 0 24px 0 rgba(239,236,229,0.08)'
      },
      keyframes: {
        'pulse-soft': {
          '0%, 100%': { opacity: '0.4' },
          '50%': { opacity: '1' }
        },
        'flow': {
          '0%': { strokeDashoffset: '40' },
          '100%': { strokeDashoffset: '0' }
        },
        'shimmer': {
          '0%': { backgroundPosition: '-200% 0' },
          '100%': { backgroundPosition: '200% 0' }
        }
      },
      animation: {
        'pulse-soft': 'pulse-soft 2.4s ease-in-out infinite',
        'flow': 'flow 1.6s linear infinite',
        'shimmer': 'shimmer 2.5s linear infinite'
      }
    }
  },
  plugins: []
};
