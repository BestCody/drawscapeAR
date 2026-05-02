import { createContext, useContext, useState, useCallback, useRef } from 'react'
import { useNavigate } from 'react-router-dom'

const TransitionContext = createContext({
  phase: 'idle',
  label: '',
  travel: () => {},
})

const ENTER_MS = 800   // overlay swooping in
const HOLD_MS = 900    // logo + label visible (route swap happens here)
const EXIT_MS = 700    // overlay leaving

export function TransitionProvider({ children }) {
  const [phase, setPhase] = useState('idle') // 'idle' | 'enter' | 'hold' | 'exit'
  const [label, setLabel] = useState('')
  const navigate = useNavigate()
  const busy = useRef(false)

  const travel = useCallback(
    (to, destinationLabel = '') => {
      if (busy.current) return
      busy.current = true
      setLabel(destinationLabel)
      setPhase('enter')

      setTimeout(() => {
        setPhase('hold')
        navigate(to) // page swap is hidden by overlay

        setTimeout(() => {
          setPhase('exit')

          setTimeout(() => {
            setPhase('idle')
            busy.current = false
          }, EXIT_MS)
        }, HOLD_MS)
      }, ENTER_MS)
    },
    [navigate]
  )

  return (
    <TransitionContext.Provider value={{ phase, label, travel }}>
      {children}
    </TransitionContext.Provider>
  )
}

export const useTransition = () => useContext(TransitionContext)