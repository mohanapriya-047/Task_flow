import React, { useState } from 'react'
import OTPInput from './OTPInput.jsx'
import { sendManagerPhoneOtp, verifyManagerPhoneOtp } from '../api/auth.js'

/**
 * Phone OTP step required before a Manager can edit/delete a project
 * (spec section 10). On success, hands the short-lived action token to
 * onVerified so the caller can attach it to the actual PUT/DELETE request.
 */
export default function ManagerPhoneOtpModal({ open, onClose, onVerified }) {
  const [sent, setSent] = useState(false)
  const [otp, setOtp] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  if (!open) return null

  async function sendOtp() {
    setBusy(true); setError('')
    try {
      await sendManagerPhoneOtp()
      setSent(true)
    } catch (e) {
      setError(e.response?.data?.message || 'Could not send OTP.')
    } finally {
      setBusy(false)
    }
  }

  async function verify() {
    setBusy(true); setError('')
    try {
      const { data } = await verifyManagerPhoneOtp(otp)
      onVerified(data.actionToken)
    } catch (e) {
      setError(e.response?.data?.message || 'Invalid or expired OTP.')
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="modal-overlay">
      <div className="modal">
        <h3>Phone Verification Required</h3>
        <p className="muted">This is a sensitive project change. Verify with the OTP sent to your registered phone.</p>
        {error && <p className="error-text">{error}</p>}

        {!sent ? (
          <div className="modal-actions">
            <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
            <button className="btn btn-primary" disabled={busy} onClick={sendOtp}>Send OTP</button>
          </div>
        ) : (
          <>
            <OTPInput value={otp} onChange={setOtp} />
            <div className="modal-actions">
              <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
              <button className="btn btn-primary" disabled={busy || otp.length !== 6} onClick={verify}>
                Verify &amp; Continue
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  )
}
