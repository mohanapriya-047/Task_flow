import React, { useState } from 'react'
import OTPInput from './OTPInput.jsx'
import { requestAdminAuthorization, verifyAdminAuthEmail, verifyAdminAuthPhone } from '../api/auth.js'

/**
 * Demonstrates the Manager -> Admin dual-OTP authorization handshake for
 * database-related / sensitive administrative operations (spec section 11).
 * On success, calls onGranted(authorizationGrantToken) - callers decide what
 * to do with the temporary grant; TaskFlow never gives a Manager DB access.
 */
export default function AdminAuthorizationModal({ open, operationDescription, onClose, onGranted }) {
  const [stage, setStage] = useState('REQUEST') // REQUEST -> EMAIL -> PHONE -> DONE
  const [authToken, setAuthToken] = useState('')
  const [otp, setOtp] = useState('')
  const [info, setInfo] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  if (!open) return null

  async function start() {
    setBusy(true); setError('')
    try {
      const { data } = await requestAdminAuthorization(operationDescription)
      setAuthToken(data.tempToken)
      setInfo(`Admin email OTP sent to ${data.maskedEmail || 'the admin'}.`)
      setStage('EMAIL')
    } catch (e) {
      setError(e.response?.data?.message || 'Could not request admin authorization.')
    } finally {
      setBusy(false)
    }
  }

  async function submitEmailOtp() {
    setBusy(true); setError('')
    try {
      const { data } = await verifyAdminAuthEmail(authToken, otp)
      setAuthToken(data.tempToken)
      setOtp('')
      setInfo('Admin phone OTP sent.')
      setStage('PHONE')
    } catch (e) {
      setError(e.response?.data?.message || 'Invalid or expired OTP.')
    } finally {
      setBusy(false)
    }
  }

  async function submitPhoneOtp() {
    setBusy(true); setError('')
    try {
      const { data } = await verifyAdminAuthPhone(authToken, otp)
      setStage('DONE')
      onGranted?.(data.authorizationGrantToken)
    } catch (e) {
      setError(e.response?.data?.message || 'Invalid or expired OTP.')
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="modal-overlay">
      <div className="modal">
        <h3>Admin Authorization Required</h3>
        <p className="muted">{operationDescription}</p>
        {error && <p className="error-text">{error}</p>}
        {info && <p className="info-text">{info}</p>}

        {stage === 'REQUEST' && (
          <div className="modal-actions">
            <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
            <button className="btn btn-primary" disabled={busy} onClick={start}>Request Authorization</button>
          </div>
        )}

        {(stage === 'EMAIL' || stage === 'PHONE') && (
          <>
            <p>Enter the {stage === 'EMAIL' ? 'email' : 'phone'} OTP sent to the admin:</p>
            <OTPInput value={otp} onChange={setOtp} />
            <div className="modal-actions">
              <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
              <button
                className="btn btn-primary"
                disabled={busy || otp.length !== 6}
                onClick={stage === 'EMAIL' ? submitEmailOtp : submitPhoneOtp}
              >
                Verify
              </button>
            </div>
          </>
        )}

        {stage === 'DONE' && (
          <div className="modal-actions">
            <p className="info-text">Authorization approved.</p>
            <button className="btn btn-primary" onClick={onClose}>Close</button>
          </div>
        )}
      </div>
    </div>
  )
}
