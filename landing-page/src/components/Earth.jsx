import { useRef, useMemo, useState, useEffect } from 'react'
import { useFrame } from '@react-three/fiber'
import * as THREE from 'three'

export default function Earth({ rotationSpeed = 0, children }) {
  const ref = useRef()
  const [maps, setMaps] = useState({ day: null, normal: null, spec: null })

  useEffect(() => {
    const loader = new THREE.TextureLoader()
    const tryLoad = (path) =>
      new Promise((res) => loader.load(path, (t) => res(t), undefined, () => res(null)))
    Promise.all([
      tryLoad('/textures/earth_day.jpg'),
      tryLoad('/textures/earth_normal.jpg'),
      tryLoad('/textures/earth_specular.jpg')
    ]).then(([day, normal, spec]) => {
      if (day) day.colorSpace = THREE.SRGBColorSpace
      setMaps({ day, normal, spec })
    })
  }, [])

  const fallback = useMemo(() => {
    const c = document.createElement('canvas'); c.width = 1024; c.height = 512
    const ctx = c.getContext('2d')
    const g = ctx.createLinearGradient(0, 0, 0, 512)
    g.addColorStop(0, '#3a3631'); g.addColorStop(1, '#1d1b18')
    ctx.fillStyle = g; ctx.fillRect(0, 0, 1024, 512)
    for (let i = 0; i < 6000; i++) {
      const x = Math.random() * 1024, y = Math.random() * 512
      const v = 50 + Math.random() * 90
      ctx.fillStyle = `rgba(${v + 30},${v + 10},${v - 10},${Math.random() * 0.6})`
      ctx.fillRect(x, y, 2 + Math.random() * 4, 2 + Math.random() * 4)
    }
    const t = new THREE.CanvasTexture(c)
    t.colorSpace = THREE.SRGBColorSpace
    return t
  }, [])

  useFrame((_, dt) => {
    if (ref.current && rotationSpeed) ref.current.rotation.y += dt * rotationSpeed
  })

  return (
    <group>
      {/* rotating group — markers passed as children stay locked to the surface */}
      <group ref={ref}>
        <mesh>
          <sphereGeometry args={[1, 96, 96]} />
          <meshStandardMaterial
            map={maps.day || fallback}
            normalMap={maps.normal || undefined}
            roughnessMap={maps.spec || undefined}
            roughness={0.85}
            metalness={0.05}
          />
        </mesh>
        {children}
      </group>
      <mesh scale={1.025}>
        <sphereGeometry args={[1, 64, 64]} />
        <meshBasicMaterial color="#8a857c" transparent opacity={0.06} side={THREE.BackSide} />
      </mesh>
    </group>
  )
}