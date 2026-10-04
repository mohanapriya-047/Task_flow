import React, { useEffect, useState } from 'react'
import { listAuditLogs } from '../../api/projects.js'
import LoadingSpinner from '../../components/LoadingSpinner.jsx'

export default function AuditLogs() {
  const [logs, setLogs] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    listAuditLogs().then(({ data }) => setLogs(data))
      .catch((e) => setError(e.response?.data?.message || 'Could not load audit logs.'))
  }, [])

  if (!logs) return error ? <p className="error-text">{error}</p> : <LoadingSpinner />

  return (
    <div className="page">
      <h2>Security / Audit Logs</h2>
      <table className="table">
        <thead>
          <tr><th>Time</th><th>User ID</th><th>Action</th><th>Resource</th><th>Description</th></tr>
        </thead>
        <tbody>
          {logs.map((log) => (
            <tr key={log.id}>
              <td>{new Date(log.timestamp).toLocaleString()}</td>
              <td>{log.userId ?? '—'}</td>
              <td>{log.action}</td>
              <td>{log.resourceType ? `${log.resourceType} #${log.resourceId}` : '—'}</td>
              <td>{log.description}</td>
            </tr>
          ))}
        </tbody>
      </table>
      {logs.length === 0 && <p className="muted">No audit entries yet.</p>}
    </div>
  )
}
