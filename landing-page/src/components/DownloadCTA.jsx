import { useState } from 'react'
import QRCode from 'react-qr-code'

const DOWNLOAD_URL = 'https://demolinkforeureka.lol' // change to your real link

export default function DownloadCTA() {
  const [open, setOpen] = useState(false)

  return (
    <div className="fixed bottom-8 right-8 z-50">
      {/* expanded card */}
      {open && (
        <div className="mb-4 w-[300px] bg-bone border border-ink/20 shadow-2xl p-6 fade-in">
          <div className="flex items-start justify-between mb-4">
            <div>
              <div className="text-ink/60 text-[11px] tracking-[0.22em] uppercase mb-1">
                Get the app
              </div>
              <div className="text-ink text-xl font-medium leading-tight">
                Scan to download
              </div>
            </div>
            <button
              onClick={() => setOpen(false)}
              className="text-ink/60 hover:text-ink text-2xl leading-none ml-3"
              aria-label="Close"
            >
              ×
            </button>
          </div>

          <div className="bg-white p-4 rounded">
            <QRCode
              value={DOWNLOAD_URL}
              size={232}
              style={{ width: '100%', height: 'auto' }}
              viewBox="0 0 232 232"
            />
          </div>

          <a
            href={DOWNLOAD_URL}
            target="_blank"
            rel="noreferrer"
            className="mt-4 block w-full text-center bg-ink text-bone text-sm tracking-[0.18em] uppercase py-3 hover:brightness-110 transition"
          >
            Open Link →
          </a>

          <div className="mt-3 text-ink/55 text-[11px] tracking-[0.18em] uppercase text-center">
            iOS · Android · TestFlight
          </div>
        </div>
      )}

      {/* pill trigger */}
      <button
        onClick={() => setOpen((o) => !o)}
        className="flex items-center gap-3 bg-ink text-bone pl-5 pr-6 py-4 border border-ink shadow-2xl hover:brightness-110 transition group"
      >
        <span className="w-9 h-9 grid place-items-center bg-bone/15 rounded-sm group-hover:bg-bone/25 transition">
          {/* simple QR glyph */}
          <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
            <path d="M3 3h7v7H3V3zm2 2v3h3V5H5zm9-2h7v7h-7V3zm2 2v3h3V5h-3zM3 14h7v7H3v-7zm2 2v3h3v-3H5zm9-2h3v3h-3v-3zm5 0h2v2h-2v-2zm-5 5h2v2h-2v-2zm3 0h2v2h-2v-2zm2-3h2v2h-2v-2zm-2-2h-2v-2h2v2z"/>
          </svg>
        </span>
        <span className="flex flex-col items-start leading-tight">
          <span className="text-[10px] tracking-[0.22em] uppercase opacity-60">
            Drawscape
          </span>
          <span className="text-base font-medium tracking-wide">
            Download App
          </span>
        </span>
      </button>
    </div>
  )
}