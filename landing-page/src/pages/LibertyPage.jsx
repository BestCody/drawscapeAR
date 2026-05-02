import { useState, useEffect, useCallback, useRef, Suspense } from 'react'
import { Canvas } from '@react-three/fiber'
import { Html } from '@react-three/drei'
import { useNavigate } from 'react-router-dom'
import StatueOfLiberty from '../components/StatueOfLiberty'
import CameraRig from '../components/CameraRig'
import DownloadCTA from '../components/DownloadCTA'
import Atmosphere from '../components/Atmosphere'
import Horizon from '../components/Horizon'
import DrawingPlane from '../components/DrawingPlane'
import { useTransition } from '../context/TransitionContext'

const NEXT = '/eiffel'
const PREV = '/wall'
const NEXT_LABEL = 'Paris'
const PREV_LABEL = 'Beijing'

const SLIDES = [
  {
    camPos: [12.3, 57, -462.3], lookAt: [-28.6, 0, -64.1],
    title: 'Drawscape · New York',
    desc: 'A mobile app that turns the world into your canvas. Open it on Liberty Island and the harbor air becomes a place you can draw — every stroke locked to the spot you made it.',
    worldText: [
      { pos: [-28.6, 90, -64.1], text: 'TAP TO RECORD · DRAW IN AIR' },
    ],
  },
  {
    camPos: [-32, 504, 855], lookAt: [-28.6, 0, -64.1],
    title: 'Layers from every visitor',
    desc: 'Built in React Native with ViroReact. GPS and feature-point tracking anchor each 3D stroke to its exact coordinates — open the app here tomorrow and the lines left today still hang in the air.',
    worldText: [
      { pos: [-28.6, 110, -64.1], text: 'ANCHORED · PERSISTENT · SHARED' },
      { pos: [60, 60, -64.1], text: 'PUBLIC LAYER' },
      { pos: [-120, 60, -64.1], text: 'YOUR LAYER' },
    ],
  },
  {
    camPos: [81.3, 288.2, 1485.5], lookAt: [-28.6, 0, -64.1],
    title: 'Mark the landmark',
    desc: 'The Statue has welcomed every arrival since 1886. Drawscape lets the next century of visitors leave something the one after will see — a slow, public sketchbook climbing the harbor sky.',
    worldText: [
      { pos: [-28.6, 140, -64.1], text: 'DRAW IT INTO HISTORY' },
    ],
  },
]

const OVERVIEW = SLIDES[0]

export default function LibertyPage() {
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
      <Atmosphere preset="ny" clouds={true} />
      <Horizon
        type="buildings"
        color="#2d3a4d"
        radius={1100}
        count={150}
        minHeight={40}
        maxHeight={220}
        seed={42}
      />
      <ambientLight intensity={0.5} />
      <directionalLight position={[200, 400, 100]} intensity={1.2} />

      <Suspense fallback={null}>
        <StatueOfLiberty />
      </Suspense>
      <Suspense fallback={null}>
      <StatueOfLiberty />
      <DrawingPlane
        url="/drawings/New_York_1.webp"
        position={[50, 55, -350]}
        height={100}
        />
      <DrawingPlane
        url="/drawings/New_York_2.webp"
        position={[-30, 455, 600]}
        height={50}
        />
      <DrawingPlane
        url="/drawings/New_York_3.png"
        position={[65, 275, 1250]}
        height={150}
        />
      </Suspense>

        {slide.worldText?.map((t, i) => (
          <Html
            key={`${slideIdx}-${i}`}
            position={t.pos}
            center
            distanceFactor={36}
            style={{ pointerEvents: 'none' }}
          >
            <div
              className="text-bone whitespace-nowrap fade-in"
              style={{ fontSize: 13, letterSpacing: '0.32em', textShadow: '0 2px 12px rgba(0,0,0,0.6)' }}
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
          className="pointer-events-auto text-bone text-xs tracking-[0.18em] uppercase border border-bone/40 px-4 py-2 bg-ink/40 backdrop-blur-sm transition-all duration-[160ms] ease-out hover:border-bone hover:brightness-110"
        >
          ← Globe
        </button>
        <div className="text-bone/55 text-[10px] tracking-[0.3em] uppercase pointer-events-auto">
          NEW YORK · LIBERTY ISLAND
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
            className={`w-1.5 h-1.5 rounded-full transition-all duration-[160ms] ${i === slideIdx ? 'bg-bone scale-150' : 'bg-bone/30'}`}
          />
        ))}
      </div>

      <div className="fixed bottom-8 left-1/2 -translate-x-1/2 z-30 text-bone/60 text-[10px] tracking-[0.3em] uppercase">
        Scroll continues to Paris
      </div>

      <DownloadCTA />
    </div>
  )
}