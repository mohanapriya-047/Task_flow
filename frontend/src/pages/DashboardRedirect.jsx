import React from 'react'
import { Navigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

const DASHBOARD_BY_ROLE = {
  ADMIN: '/admin/dashboard',
  MANAGER: '/manager/dashboard',
  TEAM_MEMBER: '/member/dashboard',
}

export default function DashboardRedirect() {
  const { user } = useAuth()
  if (!user) return <Navigate to="/login" replace />
  return <Navigate to={DASHBOARD_BY_ROLE[user.role] || '/login'} replace />
}
