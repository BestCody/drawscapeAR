const rand = (s) => { const x = Math.sin(s * 9999) * 10000; return x - Math.floor(x) }

export default function QRCode({ size = 240, fg = '#0a0a0a', bg = '#f5f3ee', className }) {
  const cells = 25
  const quiet = 12
  const inner = size - quiet * 2
  const cell = inner / cells

  const rects = []
  // position markers (the three "eyes")
  const eyeAt = (cx, cy) => {
    for (let y = 0; y < 7; y++) {
      for (let x = 0; x < 7; x++) {
        const ring = x === 0 || x === 6 || y === 0 || y === 6
        const core = x >= 2 && x <= 4 && y >= 2 && y <= 4
        if (ring || core) {
          rects.push(
            <rect
              key={`e${cx}-${cy}-${x}-${y}`}
              x={quiet + (cx + x) * cell}
              y={quiet + (cy + y) * cell}
              width={cell + 0.4}
              height={cell + 0.4}
              fill={fg}
            />
          )
        }
      }
    }
  }
  eyeAt(0, 0)
  eyeAt(cells - 7, 0)
  eyeAt(0, cells - 7)

  // data cells
  for (let y = 0; y < cells; y++) {
    for (let x = 0; x < cells; x++) {
      const inEye =
        (x < 8 && y < 8) ||
        (x >= cells - 8 && y < 8) ||
        (x < 8 && y >= cells - 8)
      if (inEye) continue
      if (rand(x * 17.3 + y * 31.7) > 0.55) {
        rects.push(
          <rect
            key={`d-${x}-${y}`}
            x={quiet + x * cell}
            y={quiet + y * cell}
            width={cell + 0.4}
            height={cell + 0.4}
            fill={fg}
          />
        )
      }
    }
  }

  // alignment marker (small bottom-right)
  const ax = cells - 9, ay = cells - 9
  for (let y = 0; y < 5; y++) {
    for (let x = 0; x < 5; x++) {
      const ring = x === 0 || x === 4 || y === 0 || y === 4
      const core = x === 2 && y === 2
      if (ring || core) {
        rects.push(
          <rect
            key={`a${x}-${y}`}
            x={quiet + (ax + x) * cell}
            y={quiet + (ay + y) * cell}
            width={cell + 0.4}
            height={cell + 0.4}
            fill={fg}
          />
        )
      }
    }
  }

  return (
    <svg viewBox={`0 0 ${size} ${size}`} width={size} height={size} className={className}>
      <rect width={size} height={size} fill={bg} />
      {rects}
    </svg>
  )
}