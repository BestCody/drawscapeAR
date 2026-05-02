import { useMemo } from 'react'
import { useTexture, Billboard } from '@react-three/drei'
import { DoubleSide } from 'three'

export default function DrawingPlane({
  url,
  position = [0, 0, 0],
  rotation = [0, 0, 0],
  height = 60,
  billboard = true,
  opacity = 0.95,
}) {
  const texture = useTexture(url)

  const [w, h] = useMemo(() => {
    const img = texture.image
    if (!img) return [height, height]
    const aspect = img.width / img.height
    return [height * aspect, height]
  }, [texture, height])

  const mesh = (
    <mesh>
      <planeGeometry args={[w, h]} />
      <meshBasicMaterial
        map={texture}
        transparent
        opacity={opacity}
        toneMapped={false}
        depthWrite={false}
        side={DoubleSide}
      />
    </mesh>
  )

  if (billboard) {
    return <Billboard position={position}>{mesh}</Billboard>
  }
  return (
    <group position={position} rotation={rotation}>
      {mesh}
    </group>
  )
}