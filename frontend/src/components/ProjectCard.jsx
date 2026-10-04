import React from 'react'
import { Link } from 'react-router-dom'
import StatusBadge from './StatusBadge.jsx'

export default function ProjectCard({ project }) {
  return (
    <Link to={`/projects/${project.id}`} className="card project-card">
      <div className="card-header">
        <h3>{project.name}</h3>
        <StatusBadge status={project.status} />
      </div>
      <p className="card-description">{project.description}</p>
      <div className="progress-bar">
        <div className="progress-bar-fill" style={{ width: `${project.progress}%` }} />
      </div>
      <div className="card-footer">
        <span>{project.progress}% complete</span>
        <span>Manager: {project.managerName}</span>
      </div>
    </Link>
  )
}
