import React, { useEffect, useState } from 'react'
import { listProjects, listAllUsers } from '../../api/projects.js'
import LoadingSpinner from '../../components/LoadingSpinner.jsx'

export default function AdminDashboard() {
  const [stats, setStats] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    (async () => {
      try {
        const [{ data: projects }, { data: users }] = await Promise.all([listProjects(), listAllUsers()])
        setStats({
          totalUsers: users.length,
          managers: users.filter((u) => u.role === 'MANAGER').length,
          teamMembers: users.filter((u) => u.role === 'TEAM_MEMBER').length,
          totalProjects: projects.length,
          activeProjects: projects.filter((p) => p.status === 'IN_PROGRESS').length,
          completedProjects: projects.filter((p) => p.status === 'COMPLETED').length,
        })
      } catch (e) {
        setError(e.response?.data?.message || 'Could not load dashboard.')
      }
    })()
  }, [])

  if (!stats) return error ? <p className="error-text">{error}</p> : <LoadingSpinner />

  return (
    <div className="page">
      <h2>Admin Dashboard</h2>
      <div className="stat-grid">
        <div className="card stat-card"><span className="stat-value">{stats.totalUsers}</span><span>Total Users</span></div>
        <div className="card stat-card"><span className="stat-value">{stats.managers}</span><span>Managers</span></div>
        <div className="card stat-card"><span className="stat-value">{stats.teamMembers}</span><span>Team Members</span></div>
        <div className="card stat-card"><span className="stat-value">{stats.totalProjects}</span><span>Total Projects</span></div>
        <div className="card stat-card"><span className="stat-value">{stats.activeProjects}</span><span>Active Projects</span></div>
        <div className="card stat-card"><span className="stat-value">{stats.completedProjects}</span><span>Completed Projects</span></div>
      </div>
    </div>
  )
}
