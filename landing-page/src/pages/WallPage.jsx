import { useState, useEffect, useCallback, useRef, Suspense } from 'react'
import { Canvas } from '@react-three/fiber'
import { Html } from '@react-three/drei'
import { useNavigate } from 'react-router-dom'
import Wall from '../components/GreatWall'
import CameraRig from '../components/CameraRig'
import DownloadCTA from '../components/DownloadCTA'
import Atmosphere from '../components/Atmosphere'
import Horizon from '../components/Horizon'
import DrawingPlane from '../components/DrawingPlane'
import { useTransition } from '../context/TransitionContext'

const NEXT = '/liberty'
const PREV = '/eiffel'
const NEXT_LABEL = 'New York'
const PREV_LABEL = 'Paris'

const SLIDES = [
  {
    camPos: [284.3, 321.4, 309.8],
    lookAt: [-459, 279, -614.8],
    title: 'Drawscape · Beijing',
    desc: 'The Great Wall stretches further than any single visitor will ever walk. Open the app on the ramparts at Mutianyu and the stone in front of you becomes a canvas — your finger leaves a 3D mark that stays anchored to that exact tower.',
    worldText: [{ pos: [-100, 320, -200], text: 'AR · 21,196 KM OF CANVAS' }],
  },
  {
    camPos: [36.2, 617.6, 18.5],
    lookAt: [-175, -20.3, -130.8],
    title: 'A line that lasts',
    desc: 'Strokes are anchored to GPS and tracked features in the world, not to your phone. Come back next year, point your camera at the same battlement, and the line you drew will be exactly where you left it.',
    worldText: [{ pos: [0, 500, 0], text: 'WORLD-LOCKED · PERSISTENT' }],
  },
  {
    camPos: [85.6, 942.1, 299.3],
    lookAt: [-127.5, -982.3, -127.8],
    title: 'A wall written by everyone',
    desc: 'Toggle a drawing public and every traveller after you sees it. The wall accumulates an invisible second skin — names, sketches, messages — that only reveals itself through the lens of the app.',
    worldText: [{ pos: [0, 800, 100], text: 'PUBLIC · FRIENDS · PRIVATE' }],
  },
]

const OVERVIEW = SLIDES[0]

export default function WallPage() {
  const [slideIdx, setSlideIdx] = useState(0)
  const navigate = useNavigate()
  const { travel } = useTransition()
  const accum = useRef(0)
  const last = useRef(0)
  const resetTimer = useRef(null)

  useEffect(() => {
    const advance = () => {
      setSlideIdx((i) => {
        if (i >= SLIDES.length - 1) { travel(NEXT, NEXT_LABEL); return i }
        return i + 1
      })
    }
    const retreat = () => {
      setSlideIdx((i) => {
        if (i <= 0) { travel(PREV, PREV_LABEL); return i }
        return i - 1
      })
    }

    const onWheel = (e) => {
      e.preventDefault()
      accum.current += e.deltaY

      clearTimeout(resetTimer.current)
      resetTimer.current = setTimeout(() => {
        accum.current = 0
      }, 250)

      const now = performance.now()
      if (now - last.current < 750) return
      if (Math.abs(accum.current) < 35) return

      last.current = now
      const dir = accum.current > 0 ? 1 : -1
      accum.current = 0
      if (dir > 0) advance()
      else retreat()
    }

    const onKey = (e) => {
      if (e.code === 'Escape') navigate('/')
      if (e.code === 'ArrowDown') advance()
      if (e.code === 'ArrowUp') retreat()
    }

    window.addEventListener('wheel', onWheel, { passive: false })
    window.addEventListener('keydown', onKey)
    return () => {
      window.removeEventListener('wheel', onWheel)
      window.removeEventListener('keydown', onKey)
      clearTimeout(resetTimer.current)
    }
  }, [navigate, travel])

  const getTarget = useCallback(() => SLIDES[slideIdx], [slideIdx])
  const slide = SLIDES[slideIdx]

  return (
    <div className="fixed inset-0">
      <Canvas
        camera={{ fov: 52, near: 0.1, far: 10000, position: OVERVIEW.camPos }}
        gl={{ antialias: true }}
        dpr={[1, 2]}
      >
        <Atmosphere preset="beijing" clouds={false} />

        <Horizon
          type="mountains"
          color="#7c6d52"
          radius={1800}
          count={90}
          minHeight={150}
          maxHeight={450}
          spread={600}
          seed={13}
        />

        <Horizon
          type="mountains"
          color="#5e533d"
          radius={900}
          count={50}
          minHeight={70}
          maxHeight={220}
          seed={2}
        />

        <ambientLight intensity={0.5} />
        <directionalLight position={[300, 500, 200]} intensity={1.2} />

        <Suspense fallback={null}>
          <Wall />
          <DrawingPlane url="/drawings/China_1.webp" position={[60, 380, 80]} height={100} />
          <DrawingPlane url="/drawings/China_2.webp" position={[0, 780, 280]} height={150} />
          <DrawingPlane url="/drawings/China_3.png" position={[-20, 490, -30]} height={150} />
        </Suspense>

        {slide.worldText?.map((t, i) => (
          <Html
            key={`${slideIdx}-${i}`}
            position={t.pos}
            center
            distanceFactor={120}
            style={{ pointerEvents: 'none' }}
          >
            <div
              className="text-ink whitespace-nowrap fade-in"
              style={{
                fontSize: 13,
                letterSpacing: '0.32em',
                textShadow: '0 2px 12px rgba(255,255,255,0.6)',
              }}
            >
              {t.text}
            </div>
          </Html>
        ))}

        <CameraRig getTarget={getTarget} />
      </Canvas>

      <div className="fixed top-0 left-0 right-0 z-40 flex items-center justify-between px-8 py-5 pointer-events-none">
        <button
          onClick={() => navigate('/')}
          className="pointer-events-auto text-ink text-xs tracking-[0.18em] uppercase border border-ink/40 px-4 py-2 bg-bone/70 backdrop-blur-sm transition-all duration-[160ms] ease-out hover:border-ink hover:brightness-95"
        >
          ← Globe
        </button>
        <div className="text-ink/55 text-[10px] tracking-[0.3em] uppercase pointer-events-auto">
          BEIJING · MUTIANYU
        </div>
      </div>

      <div className="fixed top-1/2 right-14 -translate-y-1/2 w-[520px] max-w-[42vw] z-30 pointer-events-none">
        <div
          key={slideIdx}
          className="border border-bone/25 bg-ink/85 backdrop-blur-md p-9 fade-in shadow-2xl"
        >
          <div className="text-bone/60 text-[12px] tracking-[0.22em] uppercase mb-5">
            {String(slideIdx + 1).padStart(2, '0')} / {String(SLIDES.length).padStart(2, '0')}
          </div>
          <div className="text-bone text-[34px] font-medium leading-[1.15] mb-5">
            {slide.title}
          </div>
          <div className="text-bone/85 text-[17px] leading-[1.65]">
            {slide.desc}
          </div>
        </div>
      </div>

      <div className="fixed top-1/2 right-4 -translate-y-1/2 z-30 flex flex-col gap-2.5">
        {SLIDES.map((_, i) => (
          <div
            key={i}
            className={`w-1.5 h-1.5 rounded-full transition-all duration-[160ms] ${
              i === slideIdx ? 'bg-ink scale-150' : 'bg-ink/30'
            }`}
          />
        ))}
      </div>

      <div className="fixed bottom-8 left-1/2 -translate-x-1/2 z-30 text-ink/60 text-[10px] tracking-[0.3em] uppercase">
        Scroll continues to New York
      </div>

      <DownloadCTA />
    </div>
  )
}