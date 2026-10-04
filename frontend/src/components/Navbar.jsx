import React from 'react'
import { useAuth } from '../context/AuthContext.jsx'
import UserAvatar from './UserAvatar.jsx'
import AvailabilityBadge from './AvailabilityBadge.jsx'

export default function Navbar() {
  const { user, signOut } = useAuth()
  if (!user) return null
  return (
    <header className="navbar">
      <div className="navbar-title">TaskFlow</div>
      <div className="navbar-right">
        <AvailabilityBadge status={user.availabilityStatus} />
        <div className="navbar-user">
          <UserAvatar name={user.name} status={user.availabilityStatus} size={32} />
          <span>{user.name}</span>
        </div>
        <button className="btn btn-ghost" onClick={signOut}>Logout</button>
      </div>
    </header>
  )
}
