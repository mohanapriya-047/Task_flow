import React from 'react'

export default function LoadingSpinner({ label = 'Loading…' }) {
  return (
    <div className="spinner-wrap">
      <div className="spinner" />
      <span>{label}</span>
    </div>
  )
}
