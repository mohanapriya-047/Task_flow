import React, { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { register } from '../../api/auth.js'

const initialForm = { name: '', username: '', email: '', password: '', phoneNumber: '' }

export default function Register() {
  const navigate = useNavigate()
  const [form, setForm] = useState(initialForm)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  function update(field, value) {
    setForm((f) => ({ ...f, [field]: value }))
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setBusy(true); setError('')
    try {
      const { data } = await register(form)
      navigate('/verify-registration-otp', { state: { tempToken: data.tempToken, maskedEmail: data.maskedEmail } })
    } catch (e) {
      setError(e.response?.data?.message || 'Registration failed. Please check your details.')
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="auth-page">
      <form className="auth-card" onSubmit={handleSubmit}>
        <h1>Create your TaskFlow account</h1>
        <p className="muted">New accounts start as Team Members. Managers are set up by an Admin.</p>
        {error && <p className="error-text">{error}</p>}

        <label>Full Name</label>
        <input value={form.name} onChange={(e) => update('name', e.target.value)} required />

        <label>Username</label>
        <input value={form.username} onChange={(e) => update('username', e.target.value)} required />

        <label>Email Address</label>
        <input type="email" value={form.email} onChange={(e) => update('email', e.target.value)} required />

        <label>Password</label>
        <input type="password" value={form.password} onChange={(e) => update('password', e.target.value)} required />
        <p className="hint">At least 8 characters, with a letter and a number.</p>

        <label>Phone Number</label>
        <input value={form.phoneNumber} onChange={(e) => update('phoneNumber', e.target.value)} placeholder="+91XXXXXXXXXX" required />

        <button className="btn btn-primary btn-block" disabled={busy} type="submit">
          {busy ? 'Creating account…' : 'Register'}
        </button>

        <p className="muted center">Already have an account? <Link to="/login">Login</Link></p>
      </form>
    </div>
  )
}
