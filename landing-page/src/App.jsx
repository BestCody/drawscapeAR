import './styles/travel.css'
import { Routes, Route } from 'react-router-dom'
import LandingPage from './pages/LandingPage'
import LibertyPage from './pages/LibertyPage'
import WallPage from './pages/WallPage'
import EiffelPage from './pages/EiffelPage'
import DebugPage from './pages/DebugPage'
import { TransitionProvider } from './context/TransitionContext'
import TravelOverlay from './components/TravelOverlay'

export default function App() {

  return (
    <TransitionProvider>
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route path="/liberty" element={<LibertyPage />} />
        <Route path="/wall" element={<WallPage />} />
        <Route path="/eiffel" element={<EiffelPage />} />
        <Route path="/debug" element={<DebugPage />} />
      </Routes>
      <TravelOverlay />
    </TransitionProvider>
  )
}