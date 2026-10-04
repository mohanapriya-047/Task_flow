import React, { useEffect, useState } from 'react'
import { listTeamMembers } from '../api/projects.js'
import LoadingSpinner from '../components/LoadingSpinner.jsx'
import UserAvatar from '../components/UserAvatar.jsx'
import AvailabilityBadge from '../components/AvailabilityBadge.jsx'

export default function Team() {
  const [members, setMembers] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    listTeamMembers().then(({ data }) => setMembers(data))
      .catch((e) => setError(e.response?.data?.message || 'Could not load team.'))
  }, [])

  if (!members) return error ? <p className="error-text">{error}</p> : <LoadingSpinner />

  return (
    <div className="page">
      <h2>Team Members</h2>
      <div className="team-list">
        {members.map((m) => (
          <div className="card team-row" key={m.id}>
            <UserAvatar name={m.name} status={m.availabilityStatus} size={40} />
            <div>
              <div>{m.name}</div>
              <div className="muted">{m.username}</div>
            </div>
            <AvailabilityBadge status={m.availabilityStatus} />
          </div>
        ))}
        {members.length === 0 && <p className="muted">No team members yet.</p>}
      </div>
    </div>
  )
}
