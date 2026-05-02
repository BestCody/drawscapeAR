import { useMemo } from 'react'
import { Instances, Instance } from '@react-three/drei'

export default function Horizon({
  type = 'mountains',
  count = 80,
  radius = 800,
  spread = 250,
  minHeight = 60,
  maxHeight = 220,
  color = '#3a4453',
  seed = 0,
}) {
  const items = useMemo(() => {
    const arr = []
    const rand = mulberry32(seed)
    for (let i = 0; i < count; i++) {
      const a = (i / count) * Math.PI * 2 + (rand() - 0.5) * 0.15
      const dist = radius + (rand() - 0.5) * spread
      const h = minHeight + rand() * (maxHeight - minHeight)
      const w = (type === 'buildings' ? 30 : 60) + rand() * 80
      arr.push({
        pos: [Math.cos(a) * dist, h / 2 - 4, Math.sin(a) * dist],
        scale: [w, h, w],
        rot: [0, rand() * Math.PI, 0],
      })
    }
    return arr
  }, [type, count, radius, spread, minHeight, maxHeight, seed])

  return (
    <Instances limit={count}>
      {type === 'mountains' && <coneGeometry args={[1, 1, 6]} />}
      {type === 'buildings' && <boxGeometry args={[1, 1, 1]} />}
      {type === 'rocks' && <dodecahedronGeometry args={[1, 0]} />}
      <meshStandardMaterial color={color} roughness={0.95} flatShading />
      {items.map((it, i) => (
        <Instance key={i} position={it.pos} scale={it.scale} rotation={it.rot} />
      ))}
    </Instances>
  )
}

function mulberry32(a) {
  return function () {
    a |= 0
    a = (a + 0x6d2b79f5) | 0
    let t = Math.imul(a ^ (a >>> 15), 1 | a)
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296
  }
}