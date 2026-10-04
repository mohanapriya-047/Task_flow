import React, { useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import OTPInput from '../../components/OTPInput.jsx'
import { verifyRegistrationOtp, resendOtp } from '../../api/auth.js'

export default function VerifyRegistrationOtp() {
  const { state } = useLocation()
  const navigate = useNavigate()
  const [tempToken, setTempToken] = useState(state?.tempToken || '')
  const [otp, setOtp] = useState('')
  const [error, setError] = useState('')
  const [info, setInfo] = useState('')
  const [busy, setBusy] = useState(false)

  if (!state?.tempToken && !tempToken) {
    navigate('/register')
    return null
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setBusy(true); setError(''); setInfo('')
    try {
      await verifyRegistrationOtp({ tempToken, otp })
      navigate('/login', { state: { justRegistered: true } })
    } catch (e) {
      setError(e.response?.data?.message || 'Invalid or expired OTP.')
    } finally {
      setBusy(false)
    }
  }

  async function handleResend() {
    setBusy(true); setError(''); setInfo('')
    try {
      const { data } = await resendOtp({ tempToken }, 'REGISTRATION_EMAIL')
      setTempToken(data.tempToken)
      setInfo('A new OTP has been sent to your email.')
    } catch (e) {
      setError(e.response?.data?.message || 'Could not resend OTP.')
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="auth-page">
      <form className="auth-card" onSubmit={handleSubmit}>
        <h1>Verify your email</h1>
        <p className="muted">Enter the OTP sent to {state?.maskedEmail || 'your email'}.</p>
        {error && <p className="error-text">{error}</p>}
        {info && <p className="info-text">{info}</p>}

        <OTPInput value={otp} onChange={setOtp} />

        <button className="btn btn-primary btn-block" disabled={busy || otp.length !== 6} type="submit">
          {busy ? 'Verifying…' : 'Verify OTP'}
        </button>
        <button type="button" className="btn btn-ghost btn-block" disabled={busy} onClick={handleResend}>
          Resend OTP
        </button>
      </form>
    </div>
  )
}
