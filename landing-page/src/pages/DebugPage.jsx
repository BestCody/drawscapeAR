import { useState, useRef, useEffect, useMemo, Suspense } from 'react'
import { Canvas, useFrame, useThree } from '@react-three/fiber'
import { OrbitControls, Grid, GizmoHelper, GizmoViewport } from '@react-three/drei'
import { Box3, Vector3, Box3Helper } from 'three'
import GLBModel from '../components/GLBModel'
import DrawingPlane from '../components/DrawingPlane'

const MODELS = [
  { label: 'eiffel.glb', url: '/models/eiffel.glb', height: 140 },
  { label: 'liberty.glb', url: '/models/liberty.glb', height: 60 },
  { label: 'wall.glb', url: '/models/wall.glb', height: 40 },
]

const STORAGE_KEY = 'drawscape-debug-slides'

// ---------- live readout ----------
function CameraReadout({ controlsRef, onUpdate }) {
  useFrame(({ camera }) => {
    const c = controlsRef.current
    if (!c) return
    onUpdate({
      pos: [camera.position.x, camera.position.y, camera.position.z],
      target: [c.target.x, c.target.y, c.target.z],
      fov: camera.fov,
    })
  })
  return null
}

// ---------- WASD fly that PLAYS NICE WITH OrbitControls ----------
// Translates camera AND orbit target together, so dragging still orbits
// around whatever you're currently looking at.
function FlyMovement({ controlsRef, speed }) {
  const { camera } = useThree()
  const keys = useRef({})
  const tmp = useMemo(
    () => ({ fwd: new Vector3(), right: new Vector3(), delta: new Vector3() }),
    []
  )

  useEffect(() => {
    const down = (e) => {
      // ignore typing in inputs
      if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA') return
      keys.current[e.code] = true
    }
    const up = (e) => { keys.current[e.code] = false }
    const blur = () => { keys.current = {} }
    window.addEventListener('keydown', down)
    window.addEventListener('keyup', up)
    window.addEventListener('blur', blur)
    return () => {
      window.removeEventListener('keydown', down)
      window.removeEventListener('keyup', up)
      window.removeEventListener('blur', blur)
    }
  }, [])

  useFrame((_, dt) => {
    const k = keys.current
    const any =
      k.KeyW || k.KeyA || k.KeyS || k.KeyD || k.KeyQ || k.KeyE || k.Space
    if (!any) return

    const boost = k.ShiftLeft || k.ShiftRight ? 4 : 1
    const v = speed * boost * dt

    camera.getWorldDirection(tmp.fwd)
    tmp.right.crossVectors(tmp.fwd, camera.up).normalize()
    tmp.delta.set(0, 0, 0)

    if (k.KeyW) tmp.delta.addScaledVector(tmp.fwd, v)
    if (k.KeyS) tmp.delta.addScaledVector(tmp.fwd, -v)
    if (k.KeyD) tmp.delta.addScaledVector(tmp.right, v)
    if (k.KeyA) tmp.delta.addScaledVector(tmp.right, -v)
    if (k.KeyE || k.Space) tmp.delta.y += v
    if (k.KeyQ) tmp.delta.y -= v

    camera.position.add(tmp.delta)
    if (controlsRef.current) {
      controlsRef.current.target.add(tmp.delta)
      controlsRef.current.update()
    }
  })
  return null
}

// ---------- imperative jump (used by "go" buttons) ----------
function CameraFlyer({ controlsRef, flyTo, clearFly }) {
  const { camera } = useThree()
  useEffect(() => {
    if (!flyTo) return
    camera.position.set(...flyTo.pos)
    if (controlsRef.current) {
      controlsRef.current.target.set(...flyTo.target)
      controlsRef.current.update()
    }
    clearFly()
  }, [flyTo])
  return null
}

// ---------- smooth tour through captured slides ----------
function TourPlayer({ slides, playing, secondsPerLeg, onDone, controlsRef }) {
  const { camera } = useThree()
  const stateRef = useRef(null)

  useEffect(() => {
    if (!playing || slides.length === 0) {
      stateRef.current = null
      return
    }
    stateRef.current = {
      idx: 0,
      t: 0,
      startPos: camera.position.clone(),
      startTarget: controlsRef.current?.target.clone() || new Vector3(),
    }
  }, [playing, slides.length])

  useFrame((_, dt) => {
    const s = stateRef.current
    if (!playing || !s) return

    s.t += dt / secondsPerLeg
    const a = Math.min(s.t, 1)
    const ease = 0.5 - 0.5 * Math.cos(Math.PI * a) // smoothstep

    const target = slides[s.idx]
    const tp = new Vector3(...target.pos)
    const tt = new Vector3(...target.target)

    camera.position.lerpVectors(s.startPos, tp, ease)
    if (controlsRef.current) {
      controlsRef.current.target.lerpVectors(s.startTarget, tt, ease)
      controlsRef.current.update()
    }

    if (a >= 1) {
      s.idx += 1
      if (s.idx >= slides.length) {
        onDone()
        return
      }
      s.t = 0
      s.startPos.copy(camera.position)
      if (controlsRef.current) s.startTarget.copy(controlsRef.current.target)
    }
  })

  return null
}

// ---------- bbox helper ----------
function BoundsHelper({ show, modelKey }) {
  const { scene } = useThree()
  const helperRef = useRef()
  useEffect(() => {
    if (helperRef.current) {
      scene.remove(helperRef.current)
      helperRef.current = null
    }
    if (!show) return
    const id = setTimeout(() => {
      const root = scene.getObjectByName('debug-model-root')
      if (!root) return
      const box = new Box3().setFromObject(root)
      const helper = new Box3Helper(box, 0xffaa00)
      helperRef.current = helper
      scene.add(helper)
    }, 50)
    return () => clearTimeout(id)
  }, [show, modelKey, scene])
  return null
}

const fmt = (arr) => `[${arr.map((n) => n.toFixed(1)).join(', ')}]`

// ---------- main page ----------
export default function DebugPage() {
  const [model, setModel] = useState(MODELS[0])
  const [height, setHeight] = useState(MODELS[0].height)
  const [view, setView] = useState({ pos: [0, 0, 0], target: [0, 0, 0], fov: 52 })
  const [slides, setSlides] = useState(() => {
    try {
      const raw = localStorage.getItem(STORAGE_KEY)
      return raw ? JSON.parse(raw) : []
    } catch { return [] }
  })
  const [showGrid, setShowGrid] = useState(true)
  const [showAxes, setShowAxes] = useState(true)
  const [showBounds, setShowBounds] = useState(true)
  const [flySpeed, setFlySpeed] = useState(80)
  const [flyTo, setFlyTo] = useState(null)
  const [tourPlaying, setTourPlaying] = useState(false)
  const [tourLeg, setTourLeg] = useState(2.0) // seconds per slide
  const controlsRef = useRef()

  // persist
  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(slides))
  }, [slides])

  const switchModel = (m) => {
    setModel(m)
    setHeight(m.height)
  }

  const capture = () => {
    setSlides((s) => [
      ...s,
      {
        pos: view.pos.map((n) => +n.toFixed(1)),
        target: view.target.map((n) => +n.toFixed(1)),
        title: `Slide ${s.length + 1}`,
      },
    ])
  }

  const removeSlide = (i) => setSlides((s) => s.filter((_, j) => j !== i))
  const goTo = (s) => setFlyTo({ pos: s.pos, target: s.target })

  const updateSlideAxis = (i, field, axis, value) => {
    setSlides((s) => {
      const ns = [...s]
      const arr = [...ns[i][field]]
      arr[axis] = +value
      ns[i] = { ...ns[i], [field]: arr }
      return ns
    })
  }

  const moveSlide = (i, dir) => {
    setSlides((s) => {
      const j = i + dir
      if (j < 0 || j >= s.length) return s
      const ns = [...s]
      ;[ns[i], ns[j]] = [ns[j], ns[i]]
      return ns
    })
  }

  const exportCode = () => {
    const code =
      'const SLIDES = [\n' +
      slides.map((s) =>
        `  {\n    camPos: [${s.pos.join(', ')}], lookAt: [${s.target.join(', ')}],\n    title: '${s.title}',\n    desc: '',\n    worldText: [],\n  },`
      ).join('\n') +
      '\n]'
    navigator.clipboard.writeText(code)
  }

  return (
    <div className="fixed inset-0 bg-neutral-900">
      <Canvas camera={{ position: [250, 150, 250], fov: 52, near: 0.1, far: 10000 }}>
        <color attach="background" args={['#15161a']} />
        <ambientLight intensity={0.7} />
        <directionalLight position={[120, 200, 100]} intensity={1.1} />

        {showGrid && (
          <Grid
            args={[2000, 2000]}
            cellSize={10}
            sectionSize={100}
            sectionColor="#444"
            cellColor="#222"
            fadeDistance={3000}
            infiniteGrid
          />
        )}
        {showAxes && <axesHelper args={[300]} />}

        <Suspense fallback={null}>
          <group name="debug-model-root" key={`${model.url}-${height}`}>
            <GLBModel url={model.url} height={height} />
          </group>
        </Suspense>

        <BoundsHelper show={showBounds} modelKey={`${model.url}-${height}`} />

        <OrbitControls ref={controlsRef} makeDefault enabled={!tourPlaying} />
        <CameraReadout controlsRef={controlsRef} onUpdate={setView} />
        <FlyMovement controlsRef={controlsRef} speed={flySpeed} />
        <CameraFlyer
          controlsRef={controlsRef}
          flyTo={flyTo}
          clearFly={() => setFlyTo(null)}
        />
        <TourPlayer
          slides={slides}
          playing={tourPlaying}
          secondsPerLeg={tourLeg}
          onDone={() => setTourPlaying(false)}
          controlsRef={controlsRef}
        />

        <GizmoHelper alignment="bottom-right" margin={[80, 80]}>
          <GizmoViewport axisColors={['#ff5555', '#88ff55', '#5588ff']} />
        </GizmoHelper>
      </Canvas>

      {/* ---------- left HUD ---------- */}
      <div className="fixed top-4 left-4 w-[300px] bg-black/85 text-white p-4 rounded-lg font-mono text-[11px] space-y-3 backdrop-blur border border-white/10">
        <div>
          <label className="block text-neutral-400 mb-1">MODEL</label>
          <div className="flex gap-1">
            {MODELS.map((m) => (
              <button
                key={m.url}
                onClick={() => switchModel(m)}
                className={`flex-1 px-2 py-1 rounded ${
                  model.url === m.url ? 'bg-blue-600' : 'bg-neutral-800 hover:bg-neutral-700'
                }`}
              >
                {m.label.replace('.glb', '')}
              </button>
            ))}
          </div>
        </div>

        <div>
          <label className="block text-neutral-400 mb-1">HEIGHT: {height}</label>
          <input
            type="range" min={1} max={500} value={height}
            onChange={(e) => setHeight(+e.target.value)}
            className="w-full"
          />
        </div>

        <div>
          <label className="block text-neutral-400 mb-1">FLY SPEED: {flySpeed}</label>
          <input
            type="range" min={10} max={500} step={10} value={flySpeed}
            onChange={(e) => setFlySpeed(+e.target.value)}
            className="w-full"
          />
        </div>

        <div className="border-t border-white/10 pt-2 space-y-1">
          <div className="text-emerald-400">camPos {fmt(view.pos)}</div>
          <div className="text-cyan-400">lookAt {fmt(view.target)}</div>
          <div className="text-neutral-500">fov &nbsp;&nbsp;&nbsp;{view.fov.toFixed(1)}</div>
        </div>

        <div className="grid grid-cols-3 gap-1 text-[10px]">
          <label className="flex items-center gap-1">
            <input type="checkbox" checked={showGrid} onChange={(e) => setShowGrid(e.target.checked)} />grid
          </label>
          <label className="flex items-center gap-1">
            <input type="checkbox" checked={showAxes} onChange={(e) => setShowAxes(e.target.checked)} />axes
          </label>
          <label className="flex items-center gap-1">
            <input type="checkbox" checked={showBounds} onChange={(e) => setShowBounds(e.target.checked)} />bbox
          </label>
        </div>

        <button
          onClick={capture}
          className="w-full bg-blue-600 hover:bg-blue-500 px-3 py-2 rounded font-bold"
        >
          + CAPTURE SLIDE
        </button>
      </div>

      {/* ---------- right panel ---------- */}
      <div className="fixed top-4 right-4 w-[360px] max-h-[90vh] overflow-y-auto bg-black/85 text-white p-4 rounded-lg font-mono text-[11px] backdrop-blur border border-white/10">
        <div className="flex justify-between items-center mb-3">
          <h3 className="font-bold">SLIDES ({slides.length})</h3>
          <div className="flex gap-1">
            <button
              onClick={exportCode}
              disabled={slides.length === 0}
              className="bg-emerald-600 hover:bg-emerald-500 disabled:bg-neutral-700 disabled:opacity-50 px-2 py-1 rounded"
            >copy js</button>
            <button
              onClick={() => setSlides([])}
              className="bg-red-600 hover:bg-red-500 px-2 py-1 rounded"
            >clear</button>
          </div>
        </div>

        {/* Tour controls */}
        <div className="bg-neutral-900 border border-white/10 p-2 rounded mb-3">
          <div className="flex justify-between items-center mb-2">
            <span className="text-neutral-400">PREVIEW TOUR</span>
            <button
              onClick={() => setTourPlaying((p) => !p)}
              disabled={slides.length < 2}
              className={`${
                tourPlaying ? 'bg-amber-600 hover:bg-amber-500' : 'bg-emerald-600 hover:bg-emerald-500'
              } disabled:bg-neutral-700 disabled:opacity-50 px-3 py-1 rounded font-bold`}
            >
              {tourPlaying ? '■ stop' : '▶ play'}
            </button>
          </div>
          <label className="text-neutral-500 text-[10px] block">
            seconds / leg: {tourLeg.toFixed(1)}
          </label>
          <input
            type="range" min={0.5} max={6} step={0.1} value={tourLeg}
            onChange={(e) => setTourLeg(+e.target.value)}
            className="w-full"
          />
        </div>

        {slides.length === 0 && (
          <div className="text-neutral-500 italic">
            WASD to fly · Q/E down/up · Shift to boost. Capture views to build a tour.
          </div>
        )}

        {slides.map((s, i) => (
          <div
            key={i}
            className="border-t border-white/10 pt-2 mt-2 first:border-0 first:pt-0 first:mt-0"
          >
            <div className="flex gap-1 mb-1">
              <span className="text-neutral-500 self-center">#{i + 1}</span>
              <input
                value={s.title}
                onChange={(e) => {
                  const ns = [...slides]
                  ns[i] = { ...s, title: e.target.value }
                  setSlides(ns)
                }}
                className="bg-neutral-800 px-2 py-1 flex-1 rounded text-white"
              />
              <button onClick={() => moveSlide(i, -1)} className="bg-neutral-700 hover:bg-neutral-600 px-2 rounded">↑</button>
              <button onClick={() => moveSlide(i, 1)} className="bg-neutral-700 hover:bg-neutral-600 px-2 rounded">↓</button>
              <button onClick={() => goTo(s)} className="bg-neutral-700 hover:bg-neutral-600 px-2 rounded">go</button>
              <button onClick={() => removeSlide(i)} className="bg-red-600 hover:bg-red-500 px-2 rounded">×</button>
            </div>
            <div className="grid grid-cols-[40px_1fr_1fr_1fr] gap-1 items-center">
              <span className="text-emerald-400">pos</span>
              {s.pos.map((v, ax) => (
                <input
                  key={ax} type="number" step="1" value={v}
                  onChange={(e) => updateSlideAxis(i, 'pos', ax, e.target.value)}
                  className="bg-neutral-800 px-1 py-0.5 rounded text-emerald-300 w-full"
                />
              ))}
              <span className="text-cyan-400">look</span>
              {s.target.map((v, ax) => (
                <input
                  key={ax} type="number" step="1" value={v}
                  onChange={(e) => updateSlideAxis(i, 'target', ax, e.target.value)}
                  className="bg-neutral-800 px-1 py-0.5 rounded text-cyan-300 w-full"
                />
              ))}
            </div>
          </div>
        ))}
      </div>

      <div className="fixed bottom-4 left-1/2 -translate-x-1/2 text-white/70 text-[10px] font-mono bg-black/70 px-4 py-2 rounded border border-white/10">
        WASD fly · Q/E down/up · SHIFT boost · LEFT-DRAG orbit · RIGHT-DRAG pan · SCROLL zoom · CAPTURE
      </div>
    </div>
  )
}