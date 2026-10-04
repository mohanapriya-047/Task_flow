import React from 'react'

const STATUS_COLOR = {
  AVAILABLE: '#22c55e',
  BUSY: '#f59e0b',
  NOT_AVAILABLE: '#ef4444',
}

function initials(name) {
  if (!name) return '?'
  const parts = name.trim().split(/\s+/)
  return (parts[0][0] + (parts.length > 1 ? parts[parts.length - 1][0] : '')).toUpperCase()
}

/** <UserAvatar name="Mohana Priya" status="AVAILABLE" /> */
export default function UserAvatar({ name, status, size = 40 }) {
  return (
    <span className="avatar" style={{ width: size, height: size, fontSize: size * 0.4 }}>
      {initials(name)}
      {status && (
        <span
          className="avatar-dot"
          style={{ backgroundColor: STATUS_COLOR[status] || '#9ca3af' }}
          title={status.replace('_', ' ')}
        />
      )}
    </span>
  )
}
