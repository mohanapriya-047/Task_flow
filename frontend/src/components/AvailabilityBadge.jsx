import React from 'react'

const LABEL = {
  AVAILABLE: { text: 'Available', emoji: '🟢', className: 'badge-available' },
  BUSY: { text: 'Busy', emoji: '🟠', className: 'badge-busy' },
  NOT_AVAILABLE: { text: 'Not Available', emoji: '🔴', className: 'badge-not-available' },
}

export default function AvailabilityBadge({ status }) {
  const info = LABEL[status] || { text: status, emoji: '⚪', className: '' }
  return (
    <span className={`badge ${info.className}`}>
      {info.emoji} {info.text}
    </span>
  )
}
