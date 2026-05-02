import { useMemo } from 'react'
import { useGLTF } from '@react-three/drei'
import { Box3, Vector3 } from 'three'

export default function GLBModel({
  url,
  height = 60,                  // desired world-space height
  position = [0, 0, 0],
  rotation = [0, 0, 0],
}) {
  const { scene } = useGLTF(url)

  const { object, scale, offset } = useMemo(() => {
    const obj = scene.clone(true)
    const box = new Box3().setFromObject(obj)
    const size = box.getSize(new Vector3())
    const center = box.getCenter(new Vector3())
    const s = height / size.y
    // re-center horizontally, sit base on y=0
    const off = [-center.x * s, -box.min.y * s, -center.z * s]
    return { object: obj, scale: s, offset: off }
  }, [scene, height])

  return (
    <primitive
      object={object}
      scale={scale}
      position={[
        position[0] + offset[0],
        position[1] + offset[1],
        position[2] + offset[2],
      ]}
      rotation={rotation}
    />
  )
}

useGLTF.preload('/models/liberty.glb')
useGLTF.preload('/models/eiffel.glb')
useGLTF.preload('/models/wall.glb')