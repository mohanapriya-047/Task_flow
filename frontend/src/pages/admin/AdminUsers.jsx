import React, { useEffect, useState } from 'react'
import { listAllUsers } from '../../api/projects.js'
import LoadingSpinner from '../../components/LoadingSpinner.jsx'
import UserAvatar from '../../components/UserAvatar.jsx'
import AvailabilityBadge from '../../components/AvailabilityBadge.jsx'

export default function AdminUsers() {
  const [users, setUsers] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    listAllUsers().then(({ data }) => setUsers(data))
      .catch((e) => setError(e.response?.data?.message || 'Could not load users.'))
  }, [])

  if (!users) return error ? <p className="error-text">{error}</p> : <LoadingSpinner />

  return (
    <div className="page">
      <h2>Users</h2>
      <table className="table">
        <thead>
          <tr><th></th><th>Name</th><th>Username</th><th>Email</th><th>Role</th><th>Availability</th></tr>
        </thead>
        <tbody>
          {users.map((u) => (
            <tr key={u.id}>
              <td><UserAvatar name={u.name} status={u.availabilityStatus} size={32} /></td>
              <td>{u.name}</td>
              <td>{u.username}</td>
              <td>{u.maskedEmail}</td>
              <td>{u.role.replaceAll('_', ' ')}</td>
              <td><AvailabilityBadge status={u.availabilityStatus} /></td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
