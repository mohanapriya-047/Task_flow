import client from './client'

export const listProjects = () => client.get('/projects')
export const getProject = (id) => client.get(`/projects/${id}`)
export const createProject = (payload) => client.post('/projects', payload)
export const updateProject = (id, payload) => client.put(`/projects/${id}`, payload)
export const deleteProject = (id, payload) => client.delete(`/projects/${id}`, { data: payload })

export const listTasksForProject = (projectId) => client.get(`/projects/${projectId}/tasks`)
export const listMyTasks = () => client.get('/tasks/mine')
export const createTask = (projectId, payload) => client.post(`/projects/${projectId}/tasks`, payload)
export const updateTask = (taskId, payload) => client.put(`/tasks/${taskId}`, payload)
export const deleteTask = (taskId) => client.delete(`/tasks/${taskId}`)

export const getAvailability = (userId) => client.get(`/users/${userId}/availability`)
export const updateMyAvailability = (status) => client.put('/users/me/availability', { status })

export const listTeamMembers = () => client.get('/team/members')
export const listAllUsers = () => client.get('/admin/users')
export const listAuditLogs = (page = 0, size = 50) => client.get(`/admin/audit-logs?page=${page}&size=${size}`)
