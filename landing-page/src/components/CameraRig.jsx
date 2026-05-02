import { useRef } from 'react'
import { useFrame, useThree } from '@react-three/fiber'
import * as THREE from 'three'

export default function CameraRig({ getTarget, posDamping = 1.2, lookDamping = 1.6 }) {
  const { camera } = useThree()
  const lookRef = useRef(new THREE.Vector3(0, 0, 0))
  const tmpPos = useRef(new THREE.Vector3())
  const tmpLook = useRef(new THREE.Vector3())

  useFrame((state, dt) => {
  const t = getTarget(state)
  if (!t || !t.camPos || !t.lookAt) return

  tmpPos.current.set(t.camPos[0], t.camPos[1], t.camPos[2])
  tmpLook.current.set(t.lookAt[0], t.lookAt[1], t.lookAt[2])

  const safeDt = Math.max(dt, 0.0001)
  const dPos = 1 - Math.pow(0.001, safeDt * posDamping)
  const dLook = 1 - Math.pow(0.001, safeDt * lookDamping)

  camera.position.lerp(tmpPos.current, dPos)
  lookRef.current.lerp(tmpLook.current, dLook)
  camera.lookAt(lookRef.current)
})
  return null
}