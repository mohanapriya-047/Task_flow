import React, { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import {
  getProject, updateProject, deleteProject,
  listTasksForProject, createTask, updateTask, deleteTask,
} from '../../api/projects.js'
import { useAuth } from '../../context/AuthContext.jsx'
import LoadingSpinner from '../../components/LoadingSpinner.jsx'
import StatusBadge from '../../components/StatusBadge.jsx'
import TaskCard from '../../components/TaskCard.jsx'
import ConfirmationModal from '../../components/ConfirmationModal.jsx'
import ManagerPhoneOtpModal from '../../components/ManagerPhoneOtpModal.jsx'

const STATUS_OPTIONS = ['PLANNING', 'IN_PROGRESS', 'COMPLETED', 'ON_HOLD']

export default function ProjectDetail() {
  const { id } = useParams()
  const { user } = useAuth()
  const [project, setProject] = useState(null)
  const [tasks, setTasks] = useState([])
  const [error, setError] = useState('')
  const [editing, setEditing] = useState(false)
  const [editForm, setEditForm] = useState(null)
  const [otpModal, setOtpModal] = useState({ open: false, purpose: null })
  const [confirmDelete, setConfirmDelete] = useState(false)
  const [showTaskForm, setShowTaskForm] = useState(false)
  const [taskForm, setTaskForm] = useState({ name: '', description: '', assignedUserId: '', priority: 'MEDIUM', dueDate: '' })

  const isAdmin = user.role === 'ADMIN'
  const isOwnerManager = user.role === 'MANAGER' && project?.managerId === user.id
  const canEdit = isAdmin || isOwnerManager
  const canManageTasks = isAdmin || isOwnerManager

  useEffect(() => { load() }, [id])

  async function load() {
    setError('')
    try {
      const [{ data: proj }, { data: taskList }] = await Promise.all([
        getProject(id), listTasksForProject(id),
      ])
      setProject(proj)
      setTasks(taskList)
      setEditForm({ name: proj.name, description: proj.description, status: proj.status, progress: proj.progress })
    } catch (e) {
      setError(e.response?.data?.message || 'Could not load project.')
    }
  }

  function startEdit() {
    setEditing(true)
  }

  async function submitEdit(managerActionToken) {
    setError('')
    try {
      const payload = { ...editForm, progress: Number(editForm.progress) }
      if (isOwnerManager) payload.managerActionToken = managerActionToken
      await updateProject(id, payload)
      setEditing(false)
      load()
    } catch (e) {
      setError(e.response?.data?.message || 'Could not update project.')
    }
  }

  function handleSaveClick(e) {
    e.preventDefault()
    if (isOwnerManager) {
      setOtpModal({ open: true, purpose: 'edit' })
    } else {
      submitEdit(null)
    }
  }

  async function handleDelete(managerActionToken) {
    setError('')
    try {
      await deleteProject(id, isOwnerManager ? { managerActionToken } : undefined)
      window.location.href = '/projects'
    } catch (e) {
      setError(e.response?.data?.message || 'Could not delete project.')
      setConfirmDelete(false)
    }
  }

  function handleDeleteClick() {
    setConfirmDelete(false)
    if (isOwnerManager) {
      setOtpModal({ open: true, purpose: 'delete' })
    } else {
      handleDelete(null)
    }
  }

  async function handleCreateTask(e) {
    e.preventDefault()
    setError('')
    try {
      const payload = { ...taskForm }
      if (payload.assignedUserId) payload.assignedUserId = Number(payload.assignedUserId)
      else delete payload.assignedUserId
      if (!payload.dueDate) delete payload.dueDate
      await createTask(id, payload)
      setShowTaskForm(false)
      setTaskForm({ name: '', description: '', assignedUserId: '', priority: 'MEDIUM', dueDate: '' })
      load()
    } catch (e) {
      setError(e.response?.data?.message || 'Could not create task.')
    }
  }

  async function handleAdvanceTask(task, nextStatus) {
    setError('')
    try {
      await updateTask(task.id, { status: nextStatus })
      load()
    } catch (e) {
      setError(e.response?.data?.message || 'Could not update task.')
    }
  }

  async function handleDeleteTask(taskId) {
    setError('')
    try {
      await deleteTask(taskId)
      load()
    } catch (e) {
      setError(e.response?.data?.message || 'Could not delete task.')
    }
  }

  if (!project) return error ? <p className="error-text">{error}</p> : <LoadingSpinner />

  return (
    <div className="page">
      {error && <p className="error-text">{error}</p>}

      {!editing ? (
        <div className="card">
          <div className="page-header">
            <h2>{project.name}</h2>
            <StatusBadge status={project.status} />
          </div>
          <p>{project.description}</p>
          <div className="progress-bar"><div className="progress-bar-fill" style={{ width: `${project.progress}%` }} /></div>
          <p className="muted">{project.progress}% complete · Manager: {project.managerName}</p>
          {canEdit && (
            <div className="card-actions">
              <button className="btn btn-secondary" onClick={startEdit}>Edit Project</button>
              <button className="btn btn-danger" onClick={() => setConfirmDelete(true)}>Delete Project</button>
            </div>
          )}
        </div>
      ) : (
        <form className="card form-card" onSubmit={handleSaveClick}>
          <h3>Edit Project</h3>
          <label>Name</label>
          <input value={editForm.name} onChange={(e) => setEditForm({ ...editForm, name: e.target.value })} required />
          <label>Description</label>
          <textarea value={editForm.description} onChange={(e) => setEditForm({ ...editForm, description: e.target.value })} />
          <label>Status</label>
          <select value={editForm.status} onChange={(e) => setEditForm({ ...editForm, status: e.target.value })}>
            {STATUS_OPTIONS.map((s) => <option key={s} value={s}>{s.replaceAll('_', ' ')}</option>)}
          </select>
          <label>Progress (%)</label>
          <input type="number" min="0" max="100" value={editForm.progress}
                 onChange={(e) => setEditForm({ ...editForm, progress: e.target.value })} />
          <div className="card-actions">
            <button type="button" className="btn btn-secondary" onClick={() => setEditing(false)}>Cancel</button>
            <button type="submit" className="btn btn-primary">
              {isOwnerManager ? 'Save (requires phone OTP)' : 'Save'}
            </button>
          </div>
        </form>
      )}

      <div className="page-header">
        <h3>Tasks</h3>
        {canManageTasks && (
          <button className="btn btn-primary btn-sm" onClick={() => setShowTaskForm((s) => !s)}>
            {showTaskForm ? 'Cancel' : '+ New Task'}
          </button>
        )}
      </div>

      {showTaskForm && (
        <form className="card form-card" onSubmit={handleCreateTask}>
          <label>Task Name</label>
          <input value={taskForm.name} onChange={(e) => setTaskForm({ ...taskForm, name: e.target.value })} required />
          <label>Description</label>
          <textarea value={taskForm.description} onChange={(e) => setTaskForm({ ...taskForm, description: e.target.value })} />
          <label>Assigned Team Member (user id)</label>
          <input value={taskForm.assignedUserId} onChange={(e) => setTaskForm({ ...taskForm, assignedUserId: e.target.value })} />
          <label>Priority</label>
          <select value={taskForm.priority} onChange={(e) => setTaskForm({ ...taskForm, priority: e.target.value })}>
            <option value="LOW">Low</option>
            <option value="MEDIUM">Medium</option>
            <option value="HIGH">High</option>
            <option value="URGENT">Urgent</option>
          </select>
          <label>Due Date</label>
          <input type="date" value={taskForm.dueDate} onChange={(e) => setTaskForm({ ...taskForm, dueDate: e.target.value })} />
          <button className="btn btn-primary" type="submit">Create Task</button>
        </form>
      )}

      <div className="grid">
        {tasks.map((t) => (
          <div key={t.id}>
            <TaskCard
              task={t}
              canAdvance={user.role === 'ADMIN' || isOwnerManager || (user.role === 'TEAM_MEMBER' && t.assignedUserId === user.id)}
              onAdvance={handleAdvanceTask}
            />
            {canManageTasks && (
              <button className="btn btn-danger btn-sm" onClick={() => handleDeleteTask(t.id)}>Delete Task</button>
            )}
          </div>
        ))}
        {tasks.length === 0 && <p className="muted">No tasks yet.</p>}
      </div>

      <ConfirmationModal
        open={confirmDelete}
        title="Delete Project"
        message="This will permanently delete the project and its tasks. This cannot be undone."
        confirmLabel="Delete"
        danger
        onCancel={() => setConfirmDelete(false)}
        onConfirm={handleDeleteClick}
      />

      <ManagerPhoneOtpModal
        open={otpModal.open}
        onClose={() => setOtpModal({ open: false, purpose: null })}
        onVerified={(actionToken) => {
          const purpose = otpModal.purpose
          setOtpModal({ open: false, purpose: null })
          if (purpose === 'edit') submitEdit(actionToken)
          if (purpose === 'delete') handleDelete(actionToken)
        }}
      />
    </div>
  )
}
