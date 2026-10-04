import React from 'react'
import StatusBadge from './StatusBadge.jsx'

const NEXT_STATUS = { TODO: 'IN_PROGRESS', IN_PROGRESS: 'COMPLETED' }

export default function TaskCard({ task, canAdvance, onAdvance }) {
  const next = NEXT_STATUS[task.status]
  return (
    <div className="card task-card">
      <div className="card-header">
        <h4>{task.name}</h4>
        <StatusBadge status={task.status} />
      </div>
      <p className="card-description">{task.description}</p>
      <div className="card-footer">
        <span>Assigned: {task.assignedUserName || 'Unassigned'}</span>
        <span>Priority: {task.priority}</span>
        {task.dueDate && <span>Due: {task.dueDate}</span>}
      </div>
      {canAdvance && next && (
        <button className="btn btn-primary btn-sm" onClick={() => onAdvance(task, next)}>
          Mark as {next.replaceAll('_', ' ')}
        </button>
      )}
    </div>
  )
}
