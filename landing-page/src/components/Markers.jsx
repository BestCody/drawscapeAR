import { useRef, useState } from 'react'
import { useFrame } from '@react-three/fiber'
import { Html, Billboard } from '@react-three/drei'
import * as THREE from 'three'

function latLonToVector3(lat, lon, r = 1.008) {
  const phi = (90 - lat) * (Math.PI / 180)
  const theta = (lon + 180) * (Math.PI / 180)
  return new THREE.Vector3(
    -r * Math.sin(phi) * Math.cos(theta),
    r * Math.cos(phi),
    r * Math.sin(phi) * Math.sin(theta)
  )
}

const MARKERS = [
  { id: 'liberty', lat: 40.6892, lon: -74.0445, label: 'New York' },
  { id: 'eiffel',  lat: 48.8584, lon: 2.2945,   label: 'Paris' },
  { id: 'wall',    lat: 40.4319, lon: 116.5704, label: 'Beijing' }
]

export default function Markers({ visible, onSelect }) {
  return (
    <group>
      {MARKERS.map((m) => (
        <Marker key={m.id} {...m} visible={visible} onSelect={onSelect} />
      ))}
    </group>
  )
}

function Marker({ id, lat, lon, label, visible, onSelect }) {
  const pos = latLonToVector3(lat, lon)
  const ref = useRef()
  const ringRef = useRef()
  const [hovered, setHovered] = useState(false)
  const opacityRef = useRef(0)

  useFrame((s, dt) => {
    const target = visible ? 1 : 0
    opacityRef.current += (target - opacityRef.current) * Math.min(1, dt * 5)
    if (ref.current) {
      ref.current.traverse((o) => {
        if (o.material && 'opacity' in o.material) {
          const base = o.userData.baseOpacity ?? 1
          o.material.opacity = opacityRef.current * base
          o.material.transparent = true
        }
      })
    }
    if (ringRef.current) {
      ringRef.current.scale.setScalar(1 + Math.sin(s.clock.elapsedTime * 2.4) * 0.18)
    }
  })

  return (
    <group
      ref={ref}
      position={pos}
      onClick={(e) => { e.stopPropagation(); if (visible) onSelect?.(id) }}
      onPointerOver={(e) => { e.stopPropagation(); setHovered(true); document.body.style.cursor = 'pointer' }}
      onPointerOut={(e) => { e.stopPropagation(); setHovered(false); document.body.style.cursor = 'default' }}
    >
      <Billboard>
        <mesh userData={{ baseOpacity: 1 }}>
          <circleGeometry args={[0.0055, 24]} />
          <meshBasicMaterial color="#f5f3ee" toneMapped={false} />
        </mesh>
        <mesh ref={ringRef} userData={{ baseOpacity: hovered ? 1 : 0.75 }}>
          <ringGeometry args={[0.012, 0.014, 64]} />
          <meshBasicMaterial color="#f5f3ee" toneMapped={false} transparent />
        </mesh>
        {hovered && (
          <mesh userData={{ baseOpacity: 0.18 }}>
            <circleGeometry args={[0.025, 32]} />
            <meshBasicMaterial color="#f5f3ee" toneMapped={false} transparent />
          </mesh>
        )}
      </Billboard>
      {visible && (
        <Html distanceFactor={9} position={[0.018, 0.01, 0]} style={{ pointerEvents: 'none' }}>
          <div className={`text-bone text-[10px] tracking-[0.08em] whitespace-nowrap font-medium transition-opacity duration-[160ms] ${hovered ? 'opacity-100' : 'opacity-70'}`}>
            {label}
          </div>
        </Html>
      )}
    </group>
  )
}