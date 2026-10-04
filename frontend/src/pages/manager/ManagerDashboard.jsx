import React, { useEffect, useState } from 'react'
import { listProjects, listMyTasks, listTeamMembers } from '../../api/projects.js'
import LoadingSpinner from '../../components/LoadingSpinner.jsx'
import ProjectCard from '../../components/ProjectCard.jsx'
import UserAvatar from '../../components/UserAvatar.jsx'
import AvailabilityBadge from '../../components/AvailabilityBadge.jsx'
import AdminAuthorizationModal from '../../components/AdminAuthorizationModal.jsx'

export default function ManagerDashboard() {
  const [projects, setProjects] = useState(null)
  const [team, setTeam] = useState([])
  const [error, setError] = useState('')
  const [showAuthModal, setShowAuthModal] = useState(false)

  useEffect(() => {
    (async () => {
      try {
        const [{ data: proj }, { data: members }] = await Promise.all([listProjects(), listTeamMembers()])
        setProjects(proj)
        setTeam(members)
      } catch (e) {
        setError(e.response?.data?.message || 'Could not load dashboard.')
      }
    })()
  }, [])

  if (!projects) return error ? <p className="error-text">{error}</p> : <LoadingSpinner />

  const pending = projects.filter((p) => p.status !== 'COMPLETED').length

  return (
    <div className="page">
      <div className="page-header">
        <h2>Manager Dashboard</h2>
        <button className="btn btn-secondary btn-sm" onClick={() => setShowAuthModal(true)}>
          Request Admin Authorization
        </button>
      </div>

      <div className="stat-grid">
        <div className="card stat-card"><span className="stat-value">{projects.length}</span><span>My Projects</span></div>
        <div className="card stat-card"><span className="stat-value">{pending}</span><span>Pending / In Progress</span></div>
        <div className="card stat-card"><span className="stat-value">{team.length}</span><span>Team Members</span></div>
      </div>

      <h3>My Projects</h3>
      <div className="grid">
        {projects.map((p) => <ProjectCard key={p.id} project={p} />)}
        {projects.length === 0 && <p className="muted">No projects yet. Create one from the Projects page.</p>}
      </div>

      <h3>Team Availability</h3>
      <div className="team-list">
        {team.map((m) => (
          <div className="card team-row" key={m.id}>
            <UserAvatar name={m.name} status={m.availabilityStatus} size={36} />
            <span>{m.name}</span>
            <AvailabilityBadge status={m.availabilityStatus} />
          </div>
        ))}
      </div>

      <AdminAuthorizationModal
        open={showAuthModal}
        operationDescription="Database-related administrative operation"
        onClose={() => setShowAuthModal(false)}
        onGranted={() => setShowAuthModal(false)}
      />
    </div>
  )
}
