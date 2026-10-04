import React from 'react'
import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import LoadingSpinner from './LoadingSpinner.jsx'

export default function RoleProtectedRoute({ allowedRoles }) {
  const { user, loading } = useAuth()
  if (loading) return <LoadingSpinner />
  if (!user) return <Navigate to="/login" replace />
  if (!allowedRoles.includes(user.role)) {
    return <Navigate to="/dashboard" replace />
  }
  return <Outlet />
}
