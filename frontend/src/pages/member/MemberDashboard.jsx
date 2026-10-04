import React, { useEffect, useState } from 'react'
import { listMyTasks, listProjects, updateTask } from '../../api/projects.js'
import LoadingSpinner from '../../components/LoadingSpinner.jsx'
import TaskCard from '../../components/TaskCard.jsx'
import ProjectCard from '../../components/ProjectCard.jsx'
import { useAuth } from '../../context/AuthContext.jsx'

export default function MemberDashboard() {
  const { user } = useAuth()
  const [tasks, setTasks] = useState(null)
  const [projects, setProjects] = useState([])
  const [error, setError] = useState('')

  useEffect(() => { load() }, [])

  async function load() {
    try {
      const [{ data: myTasks }, { data: myProjects }] = await Promise.all([listMyTasks(), listProjects()])
      setTasks(myTasks)
      setProjects(myProjects)
    } catch (e) {
      setError(e.response?.data?.message || 'Could not load dashboard.')
    }
  }

  if (!tasks) return error ? <p className="error-text">{error}</p> : <LoadingSpinner />

  const completed = tasks.filter((t) => t.status === 'COMPLETED').length

  return (
    <div className="page">
      <h2>My Dashboard</h2>
      <div className="stat-grid">
        <div className="card stat-card"><span className="stat-value">{projects.length}</span><span>Assigned Projects</span></div>
        <div className="card stat-card"><span className="stat-value">{tasks.length}</span><span>My Tasks</span></div>
        <div className="card stat-card"><span className="stat-value">{completed}</span><span>Completed</span></div>
      </div>

      <h3>My Tasks</h3>
      <div className="grid">
        {tasks.map((t) => (
          <TaskCard key={t.id} task={t} canAdvance onAdvance={async (task, next) => {
            await updateTask(task.id, { status: next })
            load()
          }} />
        ))}
        {tasks.length === 0 && <p className="muted">No tasks assigned yet.</p>}
      </div>

      <h3>My Projects</h3>
      <div className="grid">
        {projects.map((p) => <ProjectCard key={p.id} project={p} />)}
        {projects.length === 0 && <p className="muted">Not assigned to any projects yet.</p>}
      </div>
    </div>
  )
}
