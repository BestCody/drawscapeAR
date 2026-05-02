import { useTransition } from '../context/TransitionContext'

const ICONS = [
  '/drawings/China_1.webp',
  '/drawings/China_2.webp',
  '/drawings/China_3.png',
  '/drawings/Paris_1.webp',
  '/drawings/Paris_2.webp',
  '/drawings/Paris_3.webp',
  '/drawings/New_York_1.webp',
  '/drawings/New_York_2.webp',
  '/drawings/New_York_3.png',
]

export default function TravelOverlay() {
  const { phase, label } = useTransition()
  const active = phase !== 'idle'

  return (
    <div
      className={`travel-overlay phase-${phase} ${active ? 'is-active' : ''}`}
      aria-hidden={!active}
    >
      <div className="travel-bg" />

      <div className="travel-stage">
        <div className="orbits">
          {ICONS.map((src, i) => {
            const angle = (i / ICONS.length) * 360
            const radius = i % 2 === 0 ? 180 : 240
            const speed = 6 + (i % 3) * 2
            const dir = i % 2 === 0 ? 'normal' : 'reverse'
            const delay = i * 0.07
            return (
              <div
                key={i}
                className="orbit"
                style={{
                  '--start-angle': `${angle}deg`,
                  '--radius': `${radius}px`,
                  '--speed': `${speed}s`,
                  '--dir': dir,
                  '--delay': `${delay}s`,
                }}
              >
                <img
                  src={src}
                  alt=""
                  className="orbit-icon"
                  style={{ '--ico-delay': `${delay}s` }}
                  onError={(e) => {
                    e.currentTarget.style.background = 'rgba(255,0,0,0.4)'
                    console.warn('Missing icon:', src)
                  }}
                />
              </div>
            )
          })}
        </div>

        <img src="/drawscape-title.gif" alt="Drawscape" className="travel-logo" />
      </div>

      <div className="travel-caption">
        <span className="travel-caption-from">Traveling to</span>
        <span className="travel-caption-to">{label}</span>
      </div>
    </div>
  )
}