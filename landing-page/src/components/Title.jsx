import { useRef } from 'react'
import { useFrame } from '@react-three/fiber'

export default function Title({ visible }) {
  const ref = useRef()
  const opacityRef = useRef(1)

  useFrame((_, dt) => {
    if (!ref.current) return
    const target = visible ? 1 : 0
    opacityRef.current += (target - opacityRef.current) * Math.min(1, dt * 4)
    ref.current.visible = opacityRef.current > 0.01
    ref.current.children.forEach((c) => {
      if (c.material) {
        c.material.opacity = opacityRef.current
        c.material.transparent = true
      }
    })
  })

  return (
    <group ref={ref} position={[0, 0, -3]}>
      <mesh visible={false}>
        <planeGeometry args={[4, 1]} />
        <meshBasicMaterial transparent opacity={0} />
      </mesh>
    </group>
  )
}