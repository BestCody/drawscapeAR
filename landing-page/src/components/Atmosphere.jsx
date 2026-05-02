import { Sky, Environment, Cloud, Clouds } from '@react-three/drei'
import { MeshBasicMaterial } from 'three'

const PRESETS = {
  paris: {
    sky: { sunPosition: [120, 30, 90], turbidity: 6, rayleigh: 1.2, mieCoefficient: 0.005, mieDirectionalG: 0.8 },
    env: 'city',
    fog: ['#cdd9e3', 0.0009],
  },
  ny: {
    sky: { sunPosition: [-40, 18, -120], turbidity: 8, rayleigh: 2, mieCoefficient: 0.008, mieDirectionalG: 0.85 },
    env: 'sunset',
    fog: ['#a8b8c8', 0.0008],
  },
  beijing: {
    sky: { sunPosition: [180, 80, 60], turbidity: 12, rayleigh: 3, mieCoefficient: 0.012, mieDirectionalG: 0.9 },
    env: 'dawn',
    fog: ['#d6c4a3', 0.0007],
  },
}

export default function Atmosphere({ preset = 'paris', clouds = true }) {
  const p = PRESETS[preset] ?? PRESETS.paris
  return (
    <>
      <Sky distance={4500} {...p.sky} />
      <Environment preset={p.env} background={false} environmentIntensity={0.6} />
      <fogExp2 attach="fog" args={p.fog} />
      {clouds && (
        <Clouds material={MeshBasicMaterial} limit={50}>
          <Cloud seed={1} segments={20} volume={300} opacity={0.6} position={[0, 600, -400]} />
          <Cloud seed={2} segments={24} volume={400} opacity={0.4} position={[400, 700, 200]} />
        </Clouds>
      )}
    </>
  )
}