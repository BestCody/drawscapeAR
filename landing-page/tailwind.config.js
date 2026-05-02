import fluid, { extract, screens, fontSize } from 'fluid-tailwind'
export default {
  content: { files: ['./index.html', './src/**/*.{js,jsx}'], extract },
  theme: {
    screens, fontSize,
    extend: {
      fontFamily: {
        bricolage: ['"Bricolage Grotesque"', 'system-ui', 'sans-serif'],
        central: ['"Central"', '"Bricolage Grotesque"', 'serif']
      },
      colors: {
        ink: '#0a0a0a', graphite: '#1a1a1a', bone: '#f5f3ee',
        mist: '#d8d4cb', stone: '#8a857c', char: '#2c2a26'
      }
    }
  },
  plugins: [fluid]
}
