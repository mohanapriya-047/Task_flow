import React from 'react'

export default function OTPInput({ value, onChange, length = 6 }) {
  function handleChange(e) {
    const digits = e.target.value.replace(/\D/g, '').slice(0, length)
    onChange(digits)
  }
  return (
    <input
      className="otp-input"
      inputMode="numeric"
      autoComplete="one-time-code"
      maxLength={length}
      value={value}
      onChange={handleChange}
      placeholder={'•'.repeat(length)}
    />
  )
}
