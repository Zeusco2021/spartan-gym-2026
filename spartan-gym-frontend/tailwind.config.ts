import type { Config } from 'tailwindcss';

const config: Config = {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  important: '#root',
  corePlugins: {
    preflight: false,
  },
  theme: {
    extend: {
      colors: {
        spartan: {
          gold: '#D4AF37',
          'gold-light': '#FFD700',
          'gold-dark': '#B8960C',
          black: '#1A1A1A',
          'dark-gray': '#2D2D2D',
          'medium-gray': '#4A4A4A',
          'light-gray': '#F5F5F5',
          white: '#FFFFFF',
          red: '#C62828',
          green: '#2E7D32',
          blue: '#1565C0',
        },
      },
      fontFamily: {
        sans: ['Inter', 'Roboto', 'Helvetica', 'Arial', 'sans-serif'],
      },
    },
  },
  plugins: [],
};

export default config;
