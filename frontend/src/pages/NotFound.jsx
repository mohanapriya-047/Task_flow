import React from 'react'
import { Link } from 'react-router-dom'

export default function NotFound() {
  return (
    <div className="auth-page">
      <div className="auth-card center">
        <h1>404</h1>
        <p>Page not found.</p>
        <Link to="/" className="btn btn-primary">Go home</Link>
      </div>
    </div>
  )
}
