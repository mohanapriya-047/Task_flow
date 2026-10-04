import React, { useState } from 'react'
import { updateMyAvailability } from '../api/projects.js'

const OPTIONS = [
  { value: 'AVAILABLE', label: '🟢 Available' },
  { value: 'BUSY', label: '🟠 Busy' },
  { value: 'NOT_AVAILABLE', label: '🔴 Not Available' },
]

export default function AvailabilitySelector({ current, onUpdated }) {
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  async function handleClick(status) {
    if (status === current) return
    setBusy(true); setError('')
    try {
      await updateMyAvailability(status)
      onUpdated(status)
    } catch (e) {
      setError(e.response?.data?.message || 'Could not update availability.')
    } finally {
      setBusy(false)
    }
  }

  return (
    <div>
      {error && <p className="error-text">{error}</p>}
      <div className="availability-options">
        {OPTIONS.map((opt) => (
          <button
            key={opt.value}
            className={`btn ${opt.value === current ? 'btn-primary' : 'btn-secondary'}`}
            disabled={busy}
            onClick={() => handleClick(opt.value)}
          >
            {opt.label}
          </button>
        ))}
      </div>
    </div>
  )
}
