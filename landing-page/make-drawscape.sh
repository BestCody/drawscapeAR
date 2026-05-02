#!/usr/bin/env bash
set -e
mkdir -p drawscape/public/fonts drawscape/public/textures
mkdir -p drawscape/src/{components,pages,hooks,lib}
cd drawscape

# ---------- root files ----------
cat > package.json <<'EOF'
{
  "name": "drawscape",
  "private": true,
  "version": "1.0.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "@react-three/drei": "^9.114.0",
    "@react-three/fiber": "^8.17.10",
    "clsx": "^2.1.1",
    "react": "^18.3.1",
    "react-dom": "^18.3.1",
    "react-router-dom": "^6.27.0",
    "tailwind-merge": "^2.5.4",
    "three": "^0.169.0"
  },
  "devDependencies": {
    "@vitejs/plugin-react": "^4.3.3",
    "autoprefixer": "^10.4.20",
    "fluid-tailwind": "^1.0.4",
    "postcss": "^8.4.47",
    "tailwindcss": "^3.4.14",
    "vite": "^5.4.10"
  }
}
EOF

cat > vite.config.js <<'EOF'
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
export default defineConfig({ plugins: [react()], server: { port: 5173, open: true } })
EOF

cat > tailwind.config.js <<'EOF'
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
EOF

cat > postcss.config.js <<'EOF'
export default { plugins: { tailwindcss: {}, autoprefixer: {} } }
EOF

cat > index.html <<'EOF'
<!doctype html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width,initial-scale=1.0" />
    <title>Drawscape — The world is your canvas</title>
  </head>
  <body class="bg-ink overflow-hidden">
    <div id="root"></div>
    <script type="module" src="/src/main.jsx"></script>
  </body>
</html>
EOF

# ---------- src ----------
cat > src/main.jsx <<'EOF'
import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import App from './App.jsx'
import './index.css'
ReactDOM.createRoot(document.getElementById('root')).render(
  <BrowserRouter><App /></BrowserRouter>
)
EOF

cat > src/index.css <<'EOF'
@tailwind base;
@tailwind components;
@tailwind utilities;

@font-face { font-family: 'Bricolage Grotesque'; src: url('/fonts/BricolageGrotesque.ttf') format('truetype'); font-display: swap; }
@font-face { font-family: 'Central'; src: url('/fonts/Central.ttf') format('truetype'); font-display: swap; }
@font-face { font-family: 'AltFont1'; src: url('/fonts/Alt1.ttf') format('truetype'); font-display: swap; }
@font-face { font-family: 'AltFont2'; src: url('/fonts/Alt2.ttf') format('truetype'); font-display: swap; }
@font-face { font-family: 'AltFont3'; src: url('/fonts/Alt3.ttf') format('truetype'); font-display: swap; }

html, body, #root { height:100%; width:100%; margin:0; padding:0;
  font-family: 'Bricolage Grotesque', system-ui, sans-serif;
  background:#0a0a0a; color:#f5f3ee; -webkit-font-smoothing: antialiased; }
canvas { display:block; touch-action:none; }
.fade-in { animation: fadeIn 0.9s ease forwards; }
@keyframes fadeIn { from { opacity:0 } to { opacity:1 } }
.page-wipe { position:fixed; inset:0; pointer-events:none; z-index:9999; background:#0a0a0a; animation: wipe 1.1s ease forwards; }
@keyframes wipe { 0%{opacity:1} 100%{opacity:0} }
EOF

cat > src/App.jsx <<'EOF'
import { Routes, Route } from 'react-router-dom'
import LandingPage from './pages/LandingPage'
import MountainPage from './pages/MountainPage'
import CampusPage from './pages/CampusPage'
import ARPhonePage from './pages/ARPhonePage'
export default function App() {
  return (
    <Routes>
      <Route path="/" element={<LandingPage />} />
      <Route path="/mountain" element={<MountainPage />} />
      <Route path="/campus" element={<CampusPage />} />
      <Route path="/ar" element={<ARPhonePage />} />
    </Routes>
  )
}
EOF

cat > src/lib/utils.js <<'EOF'
import { clsx } from 'clsx'
import { twMerge } from 'tailwind-merge'
export function cn(...inputs) { return twMerge(clsx(inputs)) }
EOF

cat > src/hooks/usePlayerControls.js <<'EOF'
import { useEffect, useRef } from 'react'
export default function usePlayerControls() {
  const keys = useRef({ w:false, a:false, s:false, d:false, jump:false })
  useEffect(() => {
    const down = (e) => {
      if (e.code === 'KeyW') keys.current.w = true
      if (e.code === 'KeyA') keys.current.a = true
      if (e.code === 'KeyS') keys.current.s = true
      if (e.code === 'KeyD') keys.current.d = true
      if (e.code === 'Space') keys.current.jump = true
    }
    const up = (e) => {
      if (e.code === 'KeyW') keys.current.w = false
      if (e.code === 'KeyA') keys.current.a = false
      if (e.code === 'KeyS') keys.current.s = false
      if (e.code === 'KeyD') keys.current.d = false
      if (e.code === 'Space') keys.current.jump = false
    }
    window.addEventListener('keydown', down)
    window.addEventListener('keyup', up)
    return () => { window.removeEventListener('keydown', down); window.removeEventListener('keyup', up) }
  }, [])
  return keys
}
EOF

# ---------- components ----------
cat > src/components/Button.jsx <<'EOF'
import { cn } from '../lib/utils'
export function Button({ className, variant = 'solid', children, ...props }) {
  const base = 'inline-flex items-center justify-center px-5 py-2.5 text-sm tracking-wide font-medium border transition-all duration-[160ms] ease-out select-none'
  const variants = {
    solid: 'bg-bone text-ink border-bone hover:brightness-95 hover:border-stone',
    ghost: 'bg-transparent text-bone border-bone/40 hover:border-bone hover:brightness-110',
    outline: 'bg-transparent text-bone border-bone/60 hover:bg-bone/5 hover:border-bone'
  }
  return <button className={cn(base, variants[variant], className)} {...props}>{children}</button>
}
EOF

cat > src/components/ControlsModal.jsx <<'EOF'
import { useEffect, useState } from 'react'
export default function ControlsModal({ controls, autoDismissMs = 3500, onClose }) {
  const [visible, setVisible] = useState(true)
  useEffect(() => {
    const t = setTimeout(() => { setVisible(false); onClose?.() }, autoDismissMs)
    return () => clearTimeout(t)
  }, [autoDismissMs, onClose])
  if (!visible) return null
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-ink/70 backdrop-blur-sm fade-in">
      <div className="border border-bone/20 bg-graphite/90 px-10 py-8 max-w-md">
        <div className="text-bone/60 ~text-xs/sm uppercase tracking-[0.3em] mb-3">Controls</div>
        <div className="text-bone ~text-xl/2xl font-central mb-6">Get oriented.</div>
        <ul className="space-y-3 text-bone/80 ~text-sm/base">
          {controls.map((c, i) => (
            <li key={i} className="flex justify-between gap-8 border-b border-bone/10 pb-2">
              <span className="font-mono text-bone">{c.key}</span>
              <span className="text-bone/60">{c.action}</span>
            </li>
          ))}
        </ul>
        <div className="mt-6 text-bone/40 text-xs">This message dismisses automatically.</div>
      </div>
    </div>
  )
}
EOF

cat > src/components/Earth.jsx <<'EOF'
import { useRef, useMemo } from 'react'
import { useFrame } from '@react-three/fiber'
import * as THREE from 'three'
export default function Earth({ rotationSpeed = 0.03 }) {
  const ref = useRef()
  const textures = useMemo(() => {
    const loader = new THREE.TextureLoader()
    const day = loader.load('/textures/earth_day.jpg')
    const normal = loader.load('/textures/earth_normal.jpg')
    const spec = loader.load('/textures/earth_specular.jpg')
    day.colorSpace = THREE.SRGBColorSpace
    return { day, normal, spec }
  }, [])
  useFrame((_, dt) => { if (ref.current) ref.current.rotation.y += dt * rotationSpeed })
  return (
    <group>
      <mesh ref={ref}>
        <sphereGeometry args={[1, 96, 96]} />
        <meshStandardMaterial map={textures.day} normalMap={textures.normal}
          roughnessMap={textures.spec} roughness={0.85} metalness={0.05}
          emissive={new THREE.Color('#1a1a1a')} emissiveIntensity={0.05} />
      </mesh>
      <mesh scale={1.025}>
        <sphereGeometry args={[1, 64, 64]} />
        <meshBasicMaterial color="#8a857c" transparent opacity={0.06} side={THREE.BackSide} />
      </mesh>
    </group>
  )
}
EOF

cat > src/components/Title.jsx <<'EOF'
import { useEffect, useState, useRef } from 'react'
import { Billboard, Text } from '@react-three/drei'
import { useFrame } from '@react-three/fiber'
const FONT_SEQUENCE = ['Central','AltFont1','Central','AltFont2','Central','AltFont3','Central']
export default function Title({ visible }) {
  const [idx, setIdx] = useState(0)
  const groupRef = useRef()
  const opacityRef = useRef(1)
  useEffect(() => { const t = setInterval(() => setIdx(i => (i + 1) % FONT_SEQUENCE.length), 220); return () => clearInterval(t) }, [])
  useFrame((_, dt) => {
    if (!groupRef.current) return
    const target = visible ? 1 : 0
    opacityRef.current += (target - opacityRef.current) * Math.min(1, dt * 4)
    groupRef.current.visible = opacityRef.current > 0.01
    groupRef.current.traverse(o => { if (o.material) { o.material.opacity = opacityRef.current; o.material.transparent = true } })
  })
  return (
    <group ref={groupRef} position={[0, 0, -3]}>
      <Billboard>
        <Text fontSize={0.55} color="#f5f3ee" anchorX="center" anchorY="middle" letterSpacing={0.04} position={[0, 0.4, 0]}>
          DRAWSCAPE
          <meshBasicMaterial attach="material" color="#f5f3ee" toneMapped={false} />
        </Text>
        <Text fontSize={0.13} color="#d8d4cb" anchorX="center" anchorY="middle" letterSpacing={0.18} position={[0, -0.25, 0]}>
          THE WORLD IS YOUR CANVAS
        </Text>
      </Billboard>
    </group>
  )
}
EOF

cat > src/components/Markers.jsx <<'EOF'
import { useRef, useState } from 'react'
import { useFrame } from '@react-three/fiber'
import { Html } from '@react-three/drei'
import * as THREE from 'three'
function latLonToVector3(lat, lon, r = 1.012) {
  const phi = (90 - lat) * (Math.PI / 180)
  const theta = (lon + 180) * (Math.PI / 180)
  return new THREE.Vector3(-r*Math.sin(phi)*Math.cos(theta), r*Math.cos(phi), r*Math.sin(phi)*Math.sin(theta))
}
const MARKERS = [
  { id: 'mountain', lat: 39.5, lon: -105.7, label: 'NORTH AMERICA · ASCEND' },
  { id: 'campus',   lat: 35.6, lon: 139.7, label: 'ASIA · INSTITUTION' },
  { id: 'ar',       lat: 48.8, lon: 2.35,  label: 'EUROPE · CREATE' }
]
export default function Markers({ visible, onSelect }) {
  return <group>{MARKERS.map(m => <Marker key={m.id} {...m} visible={visible} onSelect={onSelect} />)}</group>
}
function Marker({ id, lat, lon, label, visible, onSelect }) {
  const pos = latLonToVector3(lat, lon)
  const ref = useRef()
  const [hovered, setHovered] = useState(false)
  const opacityRef = useRef(0)
  useFrame((_, dt) => {
    const target = visible ? 1 : 0
    opacityRef.current += (target - opacityRef.current) * Math.min(1, dt * 5)
    if (ref.current) {
      ref.current.scale.setScalar(0.018 + (hovered ? 0.008 : 0))
      ref.current.children.forEach(c => { if (c.material) { c.material.opacity = opacityRef.current; c.material.transparent = true } })
    }
  })
  return (
    <group position={pos}>
      <group ref={ref} onClick={() => visible && onSelect?.(id)}
             onPointerOver={() => setHovered(true)} onPointerOut={() => setHovered(false)}>
        <mesh><sphereGeometry args={[1, 16, 16]} /><meshBasicMaterial color="#f5f3ee" /></mesh>
        <mesh scale={2.4}><sphereGeometry args={[1, 16, 16]} /><meshBasicMaterial color="#f5f3ee" transparent opacity={0.18} /></mesh>
      </group>
      {visible && (
        <Html distanceFactor={6} position={[0, 0.04, 0]} style={{ pointerEvents: 'none' }}>
          <div className={`text-bone text-[10px] tracking-[0.3em] uppercase whitespace-nowrap transition-all duration-[160ms] ${hovered ? 'opacity-100' : 'opacity-70'}`}>{label}</div>
        </Html>
      )}
    </group>
  )
}
EOF

cat > src/components/Terrain.jsx <<'EOF'
import { useMemo } from 'react'
import * as THREE from 'three'
export default function Terrain({ size = 1000, segments = 200 }) {
  const { geometry } = useMemo(() => {
    const geo = new THREE.PlaneGeometry(size, size, segments, segments)
    const pos = geo.attributes.position
    for (let i = 0; i < pos.count; i++) {
      const x = pos.getX(i), y = pos.getY(i)
      const h = Math.sin(x*0.012)*Math.cos(y*0.012)*18 + Math.sin(x*0.04+1.3)*Math.cos(y*0.035)*6 + Math.sin(x*0.1)*Math.cos(y*0.09)*1.5 + Math.max(0, 30 - Math.sqrt(x*x+y*y)*0.05)*0.6
      pos.setZ(i, h)
    }
    geo.computeVertexNormals()
    return { geometry: geo }
  }, [size, segments])
  const heightAt = (wx, wz) => {
    const x = wx, y = -wz
    return Math.sin(x*0.012)*Math.cos(y*0.012)*18 + Math.sin(x*0.04+1.3)*Math.cos(y*0.035)*6 + Math.sin(x*0.1)*Math.cos(y*0.09)*1.5 + Math.max(0, 30 - Math.sqrt(x*x+y*y)*0.05)*0.6
  }
  if (typeof window !== 'undefined') window.__terrainHeight = heightAt
  const texture = useMemo(() => {
    const c = document.createElement('canvas'); c.width = c.height = 512
    const ctx = c.getContext('2d')
    ctx.fillStyle = '#6b6960'; ctx.fillRect(0, 0, 512, 512)
    for (let i = 0; i < 8000; i++) { const x = Math.random()*512, y = Math.random()*512, v = 80+Math.random()*80
      ctx.fillStyle = `rgb(${v-20},${v},${v-30})`; ctx.fillRect(x, y, 2, 2) }
    for (let i = 0; i < 800; i++) { const x = Math.random()*512, y = Math.random()*512
      ctx.fillStyle = `rgba(40,40,30,${0.2+Math.random()*0.4})`; ctx.fillRect(x, y, 4+Math.random()*6, 4+Math.random()*6) }
    const t = new THREE.CanvasTexture(c); t.wrapS = t.wrapT = THREE.RepeatWrapping; t.repeat.set(40, 40); t.colorSpace = THREE.SRGBColorSpace
    return t
  }, [])
  return (
    <mesh geometry={geometry} rotation={[-Math.PI/2, 0, 0]}>
      <meshStandardMaterial map={texture} roughness={0.95} metalness={0} color="#a8a397" />
    </mesh>
  )
}
EOF

cat > src/components/PlayerController.jsx <<'EOF'
import { useRef, useEffect } from 'react'
import { useFrame, useThree } from '@react-three/fiber'
import { PointerLockControls } from '@react-three/drei'
import * as THREE from 'three'
import usePlayerControls from '../hooks/usePlayerControls'
export default function PlayerController({ speed = 8, onPosition }) {
  const { camera } = useThree()
  const keys = usePlayerControls()
  const velocity = useRef(new THREE.Vector3())
  const onGround = useRef(true)
  useEffect(() => { camera.position.set(0, 30, 0) }, [camera])
  useFrame((_, dt) => {
    const d = Math.min(dt, 0.05)
    const dir = new THREE.Vector3()
    const front = new THREE.Vector3()
    camera.getWorldDirection(front); front.y = 0; front.normalize()
    const right = new THREE.Vector3().crossVectors(front, new THREE.Vector3(0,1,0))
    if (keys.current.w) dir.add(front)
    if (keys.current.s) dir.sub(front)
    if (keys.current.d) dir.add(right)
    if (keys.current.a) dir.sub(right)
    dir.normalize().multiplyScalar(speed * d)
    camera.position.x += dir.x; camera.position.z += dir.z
    velocity.current.y -= 25 * d
    if (keys.current.jump && onGround.current) { velocity.current.y = 9; onGround.current = false }
    camera.position.y += velocity.current.y * d
    const heightAt = window.__terrainHeight
    if (heightAt) {
      const groundY = heightAt(camera.position.x, camera.position.z) + 2.2
      if (camera.position.y <= groundY) { camera.position.y = groundY; velocity.current.y = 0; onGround.current = true }
    }
    onPosition?.(camera.position)
  })
  return <PointerLockControls />
}
EOF

cat > src/components/Slideshow.jsx <<'EOF'
import { useEffect, useState } from 'react'
const SLIDES = [
  { title: 'Trail Markings', desc: 'Drawscape annotates real trails with collaborative AR strokes carried by GPS.', img: 'https://picsum.photos/seed/draw1/800/500' },
  { title: 'Mountain Studio', desc: 'Capture form. Sketch contour lines onto the world itself, anchored to terrain.', img: 'https://picsum.photos/seed/draw2/800/500' },
  { title: 'Summit Gallery', desc: 'Leave a piece behind. Future hikers will see it perched on the ridge.', img: 'https://picsum.photos/seed/draw3/800/500' }
]
export default function Slideshow({ playerPos }) {
  const [active, setActive] = useState(-1)
  const [expanded, setExpanded] = useState(false)
  useEffect(() => {
    if (!playerPos) return
    const d = Math.sqrt(playerPos.x**2 + playerPos.z**2), y = playerPos.y
    let zone = -1
    if (d < 60 && y > 18) zone = 0
    else if (d > 100 && d < 180) zone = 1
    else if (d > 220) zone = 2
    setActive(zone)
  }, [playerPos])
  useEffect(() => { const onKey = (e) => { if (e.code === 'Escape') setExpanded(false) }
    window.addEventListener('keydown', onKey); return () => window.removeEventListener('keydown', onKey) }, [])
  if (active < 0) return null
  const s = SLIDES[active]
  return (
    <>
      <div className="absolute right-8 top-1/2 -translate-y-1/2 w-[360px] border border-bone/30 bg-ink/80 p-5 cursor-pointer transition-all duration-[160ms] ease-out hover:border-bone hover:brightness-110 fade-in" onClick={() => setExpanded(true)}>
        <img src={s.img} className="w-full h-44 object-cover mb-3" alt="" />
        <div className="text-bone/60 text-[10px] tracking-[0.3em] uppercase mb-1">Zone {active + 1}</div>
        <div className="text-bone ~text-lg/xl mb-2 font-central">{s.title}</div>
        <div className="text-bone/70 text-sm leading-relaxed">{s.desc}</div>
        <div className="mt-3 text-bone/40 text-[10px] tracking-[0.3em] uppercase">Click to expand</div>
      </div>
      {expanded && (
        <div className="fixed inset-0 z-50 bg-ink/95 flex items-center justify-center fade-in" onClick={() => setExpanded(false)}>
          <div className="max-w-3xl w-full p-8" onClick={e => e.stopPropagation()}>
            <img src={s.img} className="w-full h-[480px] object-cover mb-6" alt="" />
            <div className="text-bone/60 text-xs tracking-[0.3em] uppercase mb-2">Zone {active + 1}</div>
            <div className="text-bone ~text-3xl/5xl font-central mb-4">{s.title}</div>
            <div className="text-bone/80 ~text-base/lg max-w-xl leading-relaxed">{s.desc}</div>
            <div className="mt-8 text-bone/40 text-xs">Press ESC to leave</div>
          </div>
        </div>
      )}
    </>
  )
}
EOF

cat > src/components/Campus.jsx <<'EOF'
import { useMemo } from 'react'
import * as THREE from 'three'
export default function Campus() {
  const buildings = useMemo(() => {
    const arr = []
    const rng = (s) => { let x = Math.sin(s) * 10000; return x - Math.floor(x) }
    for (let i = 0; i < 30; i++) {
      const w = 8 + rng(i*2.1)*18, d = 8 + rng(i*3.7)*18, h = 6 + rng(i*5.3)*24
      const x = (rng(i*7.2) - 0.5)*220, z = (rng(i*9.1) - 0.5)*220
      arr.push({ w, d, h, x, z, c: 0.7 + rng(i*11.3)*0.25 })
    }
    return arr
  }, [])
  return (
    <group>
      <mesh rotation={[-Math.PI/2, 0, 0]}><planeGeometry args={[600, 600]} /><meshStandardMaterial color="#bdb6a8" roughness={0.95} /></mesh>
      {buildings.map((b, i) => (
        <mesh key={i} position={[b.x, b.h/2, b.z]}>
          <boxGeometry args={[b.w, b.h, b.d]} />
          <meshStandardMaterial color={new THREE.Color(b.c, b.c*0.97, b.c*0.92)} roughness={0.7} metalness={0.05} />
        </mesh>
      ))}
      <mesh rotation={[-Math.PI/2, 0, 0]} position={[0, 0.05, 0]}>
        <ringGeometry args={[20, 22, 64]} /><meshStandardMaterial color="#867f72" />
      </mesh>
    </group>
  )
}
EOF

cat > src/components/Phone.jsx <<'EOF'
import { useRef } from 'react'
import { useFrame } from '@react-three/fiber'
export default function Phone() {
  const ref = useRef()
  useFrame((s) => {
    if (!ref.current) return
    const t = s.clock.elapsedTime
    ref.current.rotation.y = Math.sin(t*0.25)*0.15
    ref.current.position.y = Math.sin(t*0.6)*0.04
  })
  return (
    <group ref={ref}>
      <mesh><boxGeometry args={[1.0, 2.0, 0.08]} /><meshStandardMaterial color="#1a1a1a" roughness={0.4} metalness={0.7} /></mesh>
      <mesh position={[0, 0, 0.041]}><boxGeometry args={[0.95, 1.95, 0.005]} /><meshStandardMaterial color="#0a0a0a" roughness={0.2} metalness={0.5} /></mesh>
      <mesh position={[0, 0, 0.046]}><planeGeometry args={[0.88, 1.86]} /><meshStandardMaterial color="#f5f3ee" emissive="#f5f3ee" emissiveIntensity={0.6} /></mesh>
      <mesh position={[0, 0.88, 0.05]}><boxGeometry args={[0.18, 0.04, 0.005]} /><meshStandardMaterial color="#0a0a0a" /></mesh>
    </group>
  )
}
EOF

cat > src/components/DrawingLines.jsx <<'EOF'
import { useMemo, useRef } from 'react'
import { useFrame } from '@react-three/fiber'
import * as THREE from 'three'
function makeCurve(seed) {
  const r = (s) => { const x = Math.sin(s*9999)*10000; return x - Math.floor(x) }
  const pts = []
  const ox = (r(seed) - 0.5)*0.2, oy = (r(seed+1) - 0.5)*0.4
  for (let i = 0; i < 24; i++) {
    const t = i/23
    pts.push(new THREE.Vector3(
      ox + Math.sin(t*Math.PI*2 + seed)*(0.4 + r(seed+2)*0.4) + t*1.2,
      oy + Math.cos(t*Math.PI*1.5 + seed*2)*(0.4 + r(seed+3)*0.4),
      Math.sin(t*Math.PI + seed)*(0.6 + r(seed+4)*0.6) + t*0.6
    ))
  }
  return new THREE.CatmullRomCurve3(pts)
}
export default function DrawingLines({ count = 5 }) {
  const groupRef = useRef()
  const curves = useMemo(() => Array.from({ length: count }, (_, i) => makeCurve((i+1)*1.7)), [count])
  const tubesRef = useRef([])
  useFrame((state) => {
    const t = state.clock.elapsedTime
    curves.forEach((curve, i) => {
      const prog = Math.min(1, ((t*0.35 + i*0.6) % 4) / 1.5)
      const tube = tubesRef.current[i]; if (!tube) return
      const partialPts = []; const N = 60; const upTo = Math.max(0.01, prog)
      for (let j = 0; j <= N; j++) partialPts.push(curve.getPoint((j/N)*upTo))
      const partialCurve = new THREE.CatmullRomCurve3(partialPts)
      const newGeom = new THREE.TubeGeometry(partialCurve, 64, 0.015 + Math.sin(t+i)*0.003, 8, false)
      tube.geometry.dispose(); tube.geometry = newGeom
    })
    if (groupRef.current) groupRef.current.rotation.y = Math.sin(state.clock.elapsedTime*0.15)*0.1
  })
  return (
    <group ref={groupRef} position={[0, 0, 0.05]}>
      {curves.map((c, i) => (
        <mesh key={i} ref={(el) => (tubesRef.current[i] = el)}>
          <tubeGeometry args={[c, 64, 0.015, 8, false]} />
          <meshStandardMaterial color="#f5f3ee" emissive="#f5f3ee" emissiveIntensity={0.4} roughness={0.6} />
        </mesh>
      ))}
    </group>
  )
}
EOF

# ---------- pages ----------
cat > src/pages/LandingPage.jsx <<'EOF'
import { useState, useEffect } from 'react'
import { Canvas, useFrame, useThree } from '@react-three/fiber'
import { OrbitControls } from '@react-three/drei'
import { useNavigate } from 'react-router-dom'
import Earth from '../components/Earth'
import Title from '../components/Title'
import Markers from '../components/Markers'
import ControlsModal from '../components/ControlsModal'
const CENTRAL_FONT_CYCLE = ['Central','AltFont1','Central','AltFont2','Central','AltFont3','Central']
function CameraWatcher({ onTitleVisibilityChange }) {
  const { camera } = useThree()
  useFrame(() => { onTitleVisibilityChange(camera.position.length() > 1.9) })
  return null
}
export default function LandingPage() {
  const [titleVisible, setTitleVisible] = useState(true)
  const [showControls, setShowControls] = useState(true)
  const [transitioning, setTransitioning] = useState(false)
  const [fontIdx, setFontIdx] = useState(0)
  const navigate = useNavigate()
  useEffect(() => { const t = setInterval(() => setFontIdx(i => (i+1) % CENTRAL_FONT_CYCLE.length), 200); return () => clearInterval(t) }, [])
  const handleSelect = (id) => {
    setTransitioning(true)
    setTimeout(() => {
      if (id === 'mountain') navigate('/mountain')
      if (id === 'campus') navigate('/campus')
      if (id === 'ar') navigate('/ar')
    }, 900)
  }
  return (
    <div className="fixed inset-0 bg-ink">
      <Canvas camera={{ position: [0, 0, 3.2], fov: 45, near: 0.01, far: 100 }} gl={{ antialias: true, powerPreference: 'high-performance' }} dpr={[1, 2]}>
        <color attach="background" args={['#0a0a0a']} />
        <ambientLight intensity={0.45} />
        <directionalLight position={[5, 3, 5]} intensity={1.4} color="#f5f3ee" />
        <directionalLight position={[-4, -2, -3]} intensity={0.25} color="#8a857c" />
        <hemisphereLight args={['#d8d4cb', '#1a1a1a', 0.25]} />
        <Earth />
        <Title visible={titleVisible} />
        <Markers visible={!titleVisible} onSelect={handleSelect} />
        <OrbitControls enablePan={false} minDistance={1.15} maxDistance={4.0} rotateSpeed={0.4} zoomSpeed={0.6} enableDamping dampingFactor={0.08} />
        <CameraWatcher onTitleVisibilityChange={setTitleVisible} />
      </Canvas>
      {titleVisible && (
        <div className="pointer-events-none absolute inset-0 flex flex-col items-center justify-center select-none">
          <div className="text-bone ~text-6xl/9xl tracking-[0.04em] leading-none" style={{ fontFamily: CENTRAL_FONT_CYCLE[fontIdx], textShadow: '0 0 30px rgba(0,0,0,0.6)' }}>DRAWSCAPE</div>
          <div className="mt-4 text-bone/70 ~text-xs/sm tracking-[0.4em] uppercase">The world is your canvas</div>
          <div className="absolute bottom-10 text-bone/40 text-xs tracking-[0.3em] uppercase">Zoom in to begin</div>
        </div>
      )}
      {showControls && (
        <ControlsModal autoDismissMs={3500}
          controls={[{ key: 'LMB · Drag', action: 'Rotate the globe' },{ key: 'Scroll · Pinch', action: 'Zoom toward Earth' },{ key: 'Click marker', action: 'Open chapter' }]}
          onClose={() => setShowControls(false)} />
      )}
      {transitioning && <div className="page-wipe" />}
    </div>
  )
}
EOF

cat > src/pages/MountainPage.jsx <<'EOF'
import { useState } from 'react'
import { Canvas } from '@react-three/fiber'
import { Sky } from '@react-three/drei'
import { useNavigate } from 'react-router-dom'
import Terrain from '../components/Terrain'
import PlayerController from '../components/PlayerController'
import Slideshow from '../components/Slideshow'
import ControlsModal from '../components/ControlsModal'
export default function MountainPage() {
  const [pos, setPos] = useState(null)
  const navigate = useNavigate()
  return (
    <div className="fixed inset-0">
      <Canvas camera={{ fov: 70, near: 0.1, far: 2000, position: [0, 30, 0] }} gl={{ antialias: true }} dpr={[1, 2]}>
        <color attach="background" args={['#cdd5dc']} />
        <fog attach="fog" args={['#cdd5dc', 200, 800]} />
        <Sky sunPosition={[100, 80, 100]} turbidity={3} rayleigh={1.2} mieCoefficient={0.005} mieDirectionalG={0.7} />
        <hemisphereLight args={['#fff5e0', '#7a6b5a', 0.55]} />
        <directionalLight position={[120, 200, 80]} intensity={1.5} color="#fff1d0" />
        <ambientLight intensity={0.3} />
        <Terrain />
        <PlayerController speed={8} onPosition={(p) => setPos({ x: p.x, y: p.y, z: p.z })} />
      </Canvas>
      <Slideshow playerPos={pos} />
      <button onClick={() => navigate('/')} className="absolute top-6 left-6 text-bone text-xs tracking-[0.3em] uppercase border border-bone/40 px-4 py-2 bg-ink/40 backdrop-blur-sm transition-all duration-[160ms] ease-out hover:border-bone hover:brightness-110">← Globe</button>
      <div className="absolute bottom-6 left-1/2 -translate-x-1/2 text-bone/60 text-[10px] tracking-[0.3em] uppercase pointer-events-none">Move with WASD · Jump with Space · Climb to find slides</div>
      <ControlsModal autoDismissMs={4000}
        controls={[{ key: 'W A S D', action: 'Move' },{ key: 'Mouse', action: 'Look' },{ key: 'Space', action: 'Jump' },{ key: 'Click slide', action: 'Expand' },{ key: 'ESC', action: 'Exit slide' }]} />
    </div>
  )
}
EOF

cat > src/pages/CampusPage.jsx <<'EOF'
import { useState } from 'react'
import { Canvas } from '@react-three/fiber'
import { useNavigate } from 'react-router-dom'
import Campus from '../components/Campus'
import PlayerController from '../components/PlayerController'
import ControlsModal from '../components/ControlsModal'
if (typeof window !== 'undefined') window.__terrainHeight = () => 0
export default function CampusPage() {
  const navigate = useNavigate()
  const [showOverlay, setShowOverlay] = useState(true)
  return (
    <div className="fixed inset-0 bg-bone">
      <Canvas camera={{ fov: 65, near: 0.1, far: 1500, position: [0, 8, 60] }} gl={{ antialias: true }} dpr={[1, 2]}>
        <color attach="background" args={['#e8e3d6']} />
        <fog attach="fog" args={['#e8e3d6', 150, 500]} />
        <hemisphereLight args={['#ffffff', '#a89c84', 0.7]} />
        <directionalLight position={[80, 120, 60]} intensity={1.1} color="#fff5dc" />
        <ambientLight intensity={0.35} />
        <Campus />
        <PlayerController speed={8} />
      </Canvas>
      {showOverlay && (
        <div className="absolute right-8 top-1/2 -translate-y-1/2 w-[420px] border border-ink/20 bg-bone/95 p-6 fade-in">
          <div className="text-ink/60 text-[10px] tracking-[0.3em] uppercase mb-2">University · Waterloo</div>
          <div className="text-ink font-central ~text-2xl/3xl mb-3">Drawing in Place</div>
          <div className="relative aspect-[4/3] bg-ink mb-4 overflow-hidden">
            <img src="https://picsum.photos/seed/waterloo/800/600" className="w-full h-full object-cover opacity-90" alt="" />
            <svg viewBox="0 0 400 300" className="absolute inset-0 w-full h-full">
              <path d="M 40 250 Q 100 180 160 220 T 260 180 T 360 100" stroke="#fff8b0" strokeWidth="3" fill="none" strokeDasharray="800" strokeDashoffset="800" style={{ animation: 'draw 2.5s ease forwards' }} />
              <path d="M 80 100 q 30 20 60 0 q 30 -20 60 0" stroke="#fff8b0" strokeWidth="2.5" fill="none" strokeDasharray="400" strokeDashoffset="400" style={{ animation: 'draw 2.5s ease 0.4s forwards' }} />
              <style>{`@keyframes draw { to { stroke-dashoffset: 0 } }`}</style>
            </svg>
          </div>
          <div className="text-ink/70 text-sm leading-relaxed">On campus, students leave AR drawings tied to specific GPS pins. Walk past, look through your phone, and read what others left in light.</div>
          <button onClick={() => setShowOverlay(false)} className="mt-4 text-ink text-[10px] tracking-[0.3em] uppercase border border-ink/30 px-4 py-2 transition-all duration-[160ms] ease-out hover:border-ink hover:brightness-95">Dismiss</button>
        </div>
      )}
      <button onClick={() => navigate('/')} className="absolute top-6 left-6 text-ink text-xs tracking-[0.3em] uppercase border border-ink/40 px-4 py-2 bg-bone/60 backdrop-blur-sm transition-all duration-[160ms] ease-out hover:border-ink hover:brightness-95">← Globe</button>
      <ControlsModal autoDismissMs={4000} controls={[{ key: 'W A S D', action: 'Walk the campus' },{ key: 'Mouse', action: 'Look around' },{ key: 'Space', action: 'Jump' }]} />
    </div>
  )
}
EOF

cat > src/pages/ARPhonePage.jsx <<'EOF'
import { useEffect, useState } from 'react'
import { Canvas } from '@react-three/fiber'
import { OrbitControls } from '@react-three/drei'
import { useNavigate } from 'react-router-dom'
import Phone from '../components/Phone'
import DrawingLines from '../components/DrawingLines'
import { Button } from '../components/Button'
import ControlsModal from '../components/ControlsModal'
export default function ARPhonePage() {
  const navigate = useNavigate()
  const [showLines, setShowLines] = useState(false)
  const [fade, setFade] = useState(0)
  useEffect(() => {
    const t1 = setTimeout(() => setFade(1), 50)
    const t2 = setTimeout(() => setShowLines(true), 900)
    return () => { clearTimeout(t1); clearTimeout(t2) }
  }, [])
  return (
    <div className="fixed inset-0 bg-ink" style={{ opacity: fade, transition: 'opacity 900ms ease' }}>
      <Canvas camera={{ position: [0, 0, 3.5], fov: 40 }} gl={{ antialias: true }} dpr={[1, 2]}>
        <color attach="background" args={['#0e0e0e']} />
        <fog attach="fog" args={['#0a0a0a', 6, 18]} />
        <ambientLight intensity={0.4} />
        <directionalLight position={[3, 4, 4]} intensity={1.1} color="#f5f3ee" />
        <directionalLight position={[-3, -2, 3]} intensity={0.4} color="#8a857c" />
        <hemisphereLight args={['#d8d4cb', '#0a0a0a', 0.3]} />
        <mesh rotation={[-Math.PI/2, 0, 0]} position={[0, -1.6, 0]}>
          <circleGeometry args={[8, 64]} /><meshStandardMaterial color="#15140f" roughness={1} />
        </mesh>
        <Phone />
        {showLines && <DrawingLines count={5} />}
        <OrbitControls enablePan={false} autoRotate autoRotateSpeed={0.5} minDistance={2.5} maxDistance={5} enableDamping dampingFactor={0.06} />
      </Canvas>
      <div className="absolute inset-0 pointer-events-none flex flex-col">
        <div className="flex items-center justify-between p-8 pointer-events-auto">
          <button onClick={() => navigate('/')} className="text-bone text-xs tracking-[0.3em] uppercase border border-bone/40 px-4 py-2 transition-all duration-[160ms] ease-out hover:border-bone hover:brightness-110">← Globe</button>
          <div className="text-bone/40 text-[10px] tracking-[0.4em] uppercase">Drawscape · Mobile</div>
        </div>
        <div className="flex-1 flex items-end justify-between p-12">
          <div className="max-w-md pointer-events-auto fade-in">
            <div className="text-bone/50 text-[10px] tracking-[0.4em] uppercase mb-3">Augmented Reality</div>
            <div className="text-bone font-central ~text-5xl/7xl leading-[0.95] mb-4">Create<br/>Anywhere</div>
            <div className="text-bone/70 ~text-sm/base leading-relaxed mb-6 max-w-sm">Step into the world with your phone and draw lines that stay where you put them. Drawscape turns physical space into your sketchbook.</div>
            <div className="flex gap-3">
              <Button variant="solid">Download App</Button>
              <Button variant="outline">Learn More</Button>
            </div>
          </div>
          <div className="text-bone/30 text-[10px] tracking-[0.4em] uppercase rotate-90 origin-bottom-right">v 1.0 · 2025</div>
        </div>
      </div>
      <ControlsModal autoDismissMs={3000} controls={[{ key: 'LMB · Drag', action: 'Orbit phone' },{ key: 'Scroll', action: 'Zoom' }]} />
    </div>
  )
}
EOF

cat > README.md <<'EOF'
# Drawscape

Desktop-only landing experience built with React, react-three-fiber, Tailwind, and fluid-tailwind.

## Setup
1. `npm install`
2. Add fonts to `public/fonts/`:
   - `Central.ttf` (used most)
   - `BricolageGrotesque.ttf`
   - `Alt1.ttf`, `Alt2.ttf`, `Alt3.ttf`
3. Add Earth textures to `public/textures/`:
   - `earth_day.jpg`, `earth_normal.jpg`, `earth_specular.jpg`
4. `npm run dev`

## Routes
- `/` — globe + title + 3 markers
- `/mountain` — terrain + WASD + slideshow
- `/campus` — Waterloo-style campus
- `/ar` — phone with AR drawings
EOF
