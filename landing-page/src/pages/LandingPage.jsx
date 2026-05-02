import { useState } from 'react'
import { Canvas, useFrame, useThree } from '@react-three/fiber'
import { OrbitControls } from '@react-three/drei'
import { useNavigate } from 'react-router-dom'
import Earth from '../components/Earth'
import Title from '../components/Title'
import Markers from '../components/Markers'
import DrawingPlane from '../components/DrawingPlane'



const TITLE_HIDE_DISTANCE = 2.6

function CameraWatcher({ onChange }) {
  const { camera } = useThree()
  useFrame(() => {
    onChange(camera.position.length() > TITLE_HIDE_DISTANCE)
  })
  return null
}

export default function LandingPage() {
  const [titleVisible, setTitleVisible] = useState(true)
  const [transitioning, setTransitioning] = useState(false)
  const navigate = useNavigate()

  const handleSelect = (id) => {
  setTransitioning(true)
  setTimeout(() => {
    if (id === 'liberty') navigate('/liberty')
    if (id === 'eiffel')  navigate('/eiffel')
    if (id === 'wall')    navigate('/wall')
  }, 900)
}

  const updateTitle = (next) => {
    setTitleVisible((prev) => (prev === next ? prev : next))
  }

  return (
    <div className="fixed inset-0 bg-ink">
      <Canvas
        camera={{ position: [0, 0, 3.2], fov: 45, near: 0.01, far: 100 }}
        gl={{ antialias: true, powerPreference: 'high-performance' }}
        dpr={[1, 2]}
      >
        <color attach="background" args={['#0a0a0a']} />
        <ambientLight intensity={0.45} />
        <directionalLight position={[5, 3, 5]} intensity={1.4} color="#f5f3ee" />
        <directionalLight position={[-4, -2, -3]} intensity={0.25} color="#8a857c" />
        <hemisphereLight args={['#d8d4cb', '#1a1a1a', 0.25]} />

        <Earth />
        <Title visible={titleVisible} />
        <Markers visible={!titleVisible} onSelect={handleSelect} />

        <OrbitControls
          enablePan={false}
          minDistance={1.15}
          maxDistance={7.0}
          rotateSpeed={0.4}
          zoomSpeed={0.6}
          enableDamping
          dampingFactor={0.08}
        />
        <CameraWatcher onChange={updateTitle} />
      </Canvas>

      <div
        className={`pointer-events-none absolute inset-0 flex flex-col items-center justify-center select-none transition-opacity duration-700 ${titleVisible ? 'opacity-100' : 'opacity-0'}`}
      >
        <img
          src="/drawscape-title.gif"
          alt="Drawscape"
          className="w-[min(70vw,1100px)] h-auto drop-shadow-[0_0_30px_rgba(0,0,0,0.6)]"
        />
        <div className="-mt-2 text-bone/70 text-sm tracking-[0.4em] uppercase">
          The world is your canvas
        </div>
        <div className="absolute bottom-10 text-bone/40 text-xs tracking-[0.3em] uppercase">
          Zoom in to begin
        </div>
      </div>

      {transitioning && <div className="page-wipe" />}
    </div>
  )
}