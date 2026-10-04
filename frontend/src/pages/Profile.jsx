import React from 'react'
import { useAuth } from '../context/AuthContext.jsx'
import UserAvatar from '../components/UserAvatar.jsx'
import AvailabilityBadge from '../components/AvailabilityBadge.jsx'
import AvailabilitySelector from '../components/AvailabilitySelector.jsx'

export default function Profile() {
  const { user, completeLogin } = useAuth()
  if (!user) return null

  function handleUpdated(status) {
    const token = localStorage.getItem('taskflow_access_token')
    completeLogin(token, { ...user, availabilityStatus: status })
  }

  return (
    <div className="page">
      <h2>Profile</h2>
      <div className="card profile-card">
        <UserAvatar name={user.name} status={user.availabilityStatus} size={64} />
        <div className="profile-fields">
          <div><span className="label">Name</span><span>{user.name}</span></div>
          <div><span className="label">Username</span><span>{user.username}</span></div>
          <div><span className="label">Email</span><span>{user.maskedEmail}</span></div>
          <div><span className="label">Role</span><span>{user.role.replaceAll('_', ' ')}</span></div>
          <div><span className="label">Availability</span><AvailabilityBadge status={user.availabilityStatus} /></div>
        </div>
      </div>

      <div className="card">
        <h3>Update availability</h3>
        <AvailabilitySelector current={user.availabilityStatus} onUpdated={handleUpdated} />
      </div>
    </div>
  )
}
