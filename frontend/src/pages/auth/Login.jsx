import React, { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { login } from '../../api/auth.js'

export default function Login() {
  const navigate = useNavigate()
  const [form, setForm] = useState({ username: '', password: '' })
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setBusy(true); setError('')
    try {
      const { data } = await login(form)
      navigate('/verify-login-otp', { state: { tempToken: data.tempToken, maskedEmail: data.maskedEmail } })
    } catch (e) {
      setError(e.response?.data?.message || 'Invalid username or password.')
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="auth-page">
      <form className="auth-card" onSubmit={handleSubmit}>
        <h1>TaskFlow</h1>
        <p className="muted">Sign in to your account</p>
        {error && <p className="error-text">{error}</p>}

        <label>Username</label>
        <input value={form.username} onChange={(e) => setForm({ ...form, username: e.target.value })} required />

        <label>Password</label>
        <input type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} required />

        <button className="btn btn-primary btn-block" disabled={busy} type="submit">
          {busy ? 'Signing in…' : 'Login'}
        </button>

        <p className="muted center">No account? <Link to="/register">Register</Link></p>
      </form>
    </div>
  )
}
