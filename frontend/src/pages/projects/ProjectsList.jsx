import React, { useEffect, useState } from 'react'
import { listProjects, listTeamMembers, createProject } from '../../api/projects.js'
import ProjectCard from '../../components/ProjectCard.jsx'
import LoadingSpinner from '../../components/LoadingSpinner.jsx'
import { useAuth } from '../../context/AuthContext.jsx'

const STATUS_OPTIONS = ['PLANNING', 'IN_PROGRESS', 'COMPLETED', 'ON_HOLD']

export default function ProjectsList() {
  const { user } = useAuth()
  const [projects, setProjects] = useState(null)
  const [error, setError] = useState('')
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState({ name: '', description: '', status: 'PLANNING', managerId: '' })
  const [managers, setManagers] = useState([])
  const [busy, setBusy] = useState(false)

  const canCreate = user.role === 'ADMIN' || user.role === 'MANAGER'

  useEffect(() => {
    load()
    if (user.role === 'ADMIN') {
      listTeamMembers().catch(() => {}) // placeholder; admin manager list could come from a dedicated endpoint
    }
  }, [])

  async function load() {
    setError('')
    try {
      const { data } = await listProjects()
      setProjects(data)
    } catch (e) {
      setError(e.response?.data?.message || 'Could not load projects.')
    }
  }

  async function handleCreate(e) {
    e.preventDefault()
    setBusy(true); setError('')
    try {
      const payload = { ...form }
      if (user.role !== 'ADMIN') delete payload.managerId
      else payload.managerId = Number(payload.managerId)
      await createProject(payload)
      setShowForm(false)
      setForm({ name: '', description: '', status: 'PLANNING', managerId: '' })
      load()
    } catch (e) {
      setError(e.response?.data?.message || 'Could not create project.')
    } finally {
      setBusy(false)
    }
  }

  if (!projects) return error ? <p className="error-text">{error}</p> : <LoadingSpinner />

  return (
    <div className="page">
      <div className="page-header">
        <h2>{user.role === 'MANAGER' ? 'My Projects' : 'Projects'}</h2>
        {canCreate && (
          <button className="btn btn-primary" onClick={() => setShowForm((s) => !s)}>
            {showForm ? 'Cancel' : '+ New Project'}
          </button>
        )}
      </div>

      {error && <p className="error-text">{error}</p>}

      {showForm && (
        <form className="card form-card" onSubmit={handleCreate}>
          <label>Project Name</label>
          <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />

          <label>Description</label>
          <textarea value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />

          <label>Status</label>
          <select value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}>
            {STATUS_OPTIONS.map((s) => <option key={s} value={s}>{s.replaceAll('_', ' ')}</option>)}
          </select>

          {user.role === 'ADMIN' && (
            <>
              <label>Manager ID</label>
              <input value={form.managerId} onChange={(e) => setForm({ ...form, managerId: e.target.value })}
                     placeholder="Numeric user id of the manager" required />
            </>
          )}

          <button className="btn btn-primary" disabled={busy} type="submit">
            {busy ? 'Creating…' : 'Create Project'}
          </button>
        </form>
      )}

      {projects.length === 0 ? (
        <p className="muted">No projects to show yet.</p>
      ) : (
        <div className="grid">
          {projects.map((p) => <ProjectCard key={p.id} project={p} />)}
        </div>
      )}
    </div>
  )
}
