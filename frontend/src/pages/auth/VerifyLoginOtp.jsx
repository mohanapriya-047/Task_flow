import React, { useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import OTPInput from '../../components/OTPInput.jsx'
import { verifyLoginOtp, resendOtp } from '../../api/auth.js'
import { useAuth } from '../../context/AuthContext.jsx'

const DASHBOARD_BY_ROLE = {
  ADMIN: '/admin/dashboard',
  MANAGER: '/manager/dashboard',
  TEAM_MEMBER: '/member/dashboard',
}

export default function VerifyLoginOtp() {
  const { state } = useLocation()
  const navigate = useNavigate()
  const { completeLogin } = useAuth()
  const [tempToken, setTempToken] = useState(state?.tempToken || '')
  const [otp, setOtp] = useState('')
  const [error, setError] = useState('')
  const [info, setInfo] = useState('')
  const [busy, setBusy] = useState(false)

  if (!state?.tempToken && !tempToken) {
    navigate('/login')
    return null
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setBusy(true); setError(''); setInfo('')
    try {
      const { data } = await verifyLoginOtp({ tempToken, otp })
      completeLogin(data.accessToken, data.user)
      navigate(DASHBOARD_BY_ROLE[data.user.role] || '/dashboard')
    } catch (e) {
      setError(e.response?.data?.message || 'Invalid or expired OTP.')
    } finally {
      setBusy(false)
    }
  }

  async function handleResend() {
    setBusy(true); setError(''); setInfo('')
    try {
      const { data } = await resendOtp({ tempToken }, 'LOGIN_EMAIL')
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
        <h1>Verify it's you</h1>
        <p className="muted">OTP sent to {state?.maskedEmail || 'your email'}</p>
        {error && <p className="error-text">{error}</p>}
        {info && <p className="info-text">{info}</p>}

        <OTPInput value={otp} onChange={setOtp} />

        <button className="btn btn-primary btn-block" disabled={busy || otp.length !== 6} type="submit">
          {busy ? 'Verifying…' : 'Verify & Continue'}
        </button>
        <button type="button" className="btn btn-ghost btn-block" disabled={busy} onClick={handleResend}>
          Resend OTP
        </button>
      </form>
    </div>
  )
}
