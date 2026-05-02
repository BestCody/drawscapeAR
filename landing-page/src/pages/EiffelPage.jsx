import { useState, useEffect, useCallback, useRef, Suspense } from 'react'
import { Canvas } from '@react-three/fiber'
import { Html } from '@react-three/drei'
import { useNavigate } from 'react-router-dom'
import EiffelTower from '../components/EiffelTower'
import CameraRig from '../components/CameraRig'
import DownloadCTA from '../components/DownloadCTA'
import Atmosphere from '../components/Atmosphere'
import Horizon from '../components/Horizon'
import DrawingPlane from '../components/DrawingPlane'
import { useTransition } from '../context/TransitionContext'

const NEXT = '/wall'
const PREV = '/liberty'
const NEXT_LABEL = 'Beijing'
const PREV_LABEL = 'New York'

const SLIDES = [
  {
    camPos: [-2.3, 41.8, -402.1], lookAt: [0, 0, 0],
    title: 'Drawscape · Paris',
    desc: 'Stand on the Champ de Mars at dusk. Open the app and the iron lattice becomes a canvas — your phone tracks the scene, your finger draws into the space beside it. The line stays where you left it.',
    worldText: [{ pos: [0, 80, -60], text: 'AR · ANCHORED · PERSISTENT' }],
  },
  {
    camPos: [115, 198.8, 310.7], lookAt: [-28.6, 0, -64.1],
    title: 'Three-dimensional strokes',
    desc: 'Built in React Native with ViroReact. Your finger movement is mapped through feature-point tracking into a 3D spline — the stroke has real depth, locked to the world rather than the camera.',
    worldText: [
      { pos: [-30, 110, -60], text: 'DEPTH-AWARE' },
      { pos: [-30, 60, -60], text: 'WORLD-LOCKED' },
    ],
  },
  {
    camPos: [20.4, 303.7, 536.9], lookAt: [-25, 500, -0],
    title: 'A shared sketchbook climbing the tower',
    desc: 'Every drawing is stored against its GPS anchor. Whoever walks back to this exact spot tomorrow night will see what you left — public, friends-only, or private, your choice.',
    worldText: [{ pos: [-30, 200, -60], text: 'PUBLIC · FRIENDS · PRIVATE' }],
  },
]

const OVERVIEW = SLIDES[0]

export default function EiffelPage() {
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
      resetTimer.current = setTimeout(() => { accum.current = 0 }, 250)
      const now = performance.now()
      if (now - last.current < 750) return
      if (Math.abs(accum.current) < 35) return
      last.current = now
      const dir = accum.current > 0 ? 1 : -1
      accum.current = 0
      if (dir > 0) advance(); else retreat()
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
      camera={{ fov: 52, near: 0.1, far: 5000, position: OVERVIEW.camPos }}
      gl={{ antialias: true }}
      dpr={[1, 2]}
      >
      <Atmosphere preset="paris" clouds={true} />
      <Horizon
        type="buildings"
        color="#6a7585"
        radius={900}
        count={120}
        minHeight={30}
        maxHeight={120}
        seed={7}
      />

      <ambientLight intensity={0.5} />
      <directionalLight position={[200, 400, 100]} intensity={1.2} />
      <Suspense fallback={null}>
      <EiffelTower />
      <DrawingPlane
      url="/drawings/Paris_1.webp"
      position={[10, 35, -310]}
      height={90}
      />
      <DrawingPlane
      url="/drawings/Paris_2.webp"
      position={[80, 135, 210]}
      height={90}
      />
      <DrawingPlane
      url="/drawings/Paris_3.webp"
      position={[-10, 690, 0]}
      height={100}
      />
      </Suspense>

        {slide.worldText?.map((t, i) => (
          <Html
            key={`${slideIdx}-${i}`}
            position={t.pos}
            center
            distanceFactor={60}
            style={{ pointerEvents: 'none' }}
          >
            <div
              className="text-ink whitespace-nowrap fade-in"
              style={{
                fontSize: 13,
                letterSpacing: '0.32em',
                textShadow: '0 2px 12px rgba(255,255,255,0.5)',
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
          PARIS · CHAMP DE MARS
        </div>
      </div>

      <div className="fixed top-1/2 right-14 -translate-y-1/2 w-[520px] max-w-[42vw] z-30 pointer-events-none">
        <div
          key={slideIdx}
          className="border border-bone/25 bg-ink/85 backdrop-blur-md p-9 fade-in shadow-2xl">
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
            className={`w-1.5 h-1.5 rounded-full transition-all duration-[160ms] ${i === slideIdx ? 'bg-ink scale-150' : 'bg-ink/30'}`}
          />
        ))}
      </div>

      <div className="fixed bottom-8 left-1/2 -translate-x-1/2 z-30 text-ink/60 text-[10px] tracking-[0.3em] uppercase">
        Scroll continues to Beijing
      </div>

      <DownloadCTA />
    </div>
  )
}