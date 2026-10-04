import React from 'react'
import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import LoadingSpinner from './LoadingSpinner.jsx'

// Route-level protection is a UX convenience only - the backend independently
// enforces every permission via @PreAuthorize and service-layer checks, so a
// user can never bypass authorization simply by forging a client route.
export default function ProtectedRoute() {
  const { user, loading } = useAuth()
  if (loading) return <LoadingSpinner />
  if (!user) return <Navigate to="/login" replace />
  return <Outlet />
}
