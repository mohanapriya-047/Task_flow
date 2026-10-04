import React from 'react'
import { Routes, Route } from 'react-router-dom'

import Login from './pages/auth/Login.jsx'
import Register from './pages/auth/Register.jsx'
import VerifyRegistrationOtp from './pages/auth/VerifyRegistrationOtp.jsx'
import VerifyLoginOtp from './pages/auth/VerifyLoginOtp.jsx'

import Layout from './components/Layout.jsx'
import ProtectedRoute from './components/ProtectedRoute.jsx'
import RoleProtectedRoute from './components/RoleProtectedRoute.jsx'

import DashboardRedirect from './pages/DashboardRedirect.jsx'
import AdminDashboard from './pages/admin/AdminDashboard.jsx'
import AdminUsers from './pages/admin/AdminUsers.jsx'
import AuditLogs from './pages/admin/AuditLogs.jsx'
import ManagerDashboard from './pages/manager/ManagerDashboard.jsx'
import MemberDashboard from './pages/member/MemberDashboard.jsx'
import ProjectsList from './pages/projects/ProjectsList.jsx'
import ProjectDetail from './pages/projects/ProjectDetail.jsx'
import Team from './pages/Team.jsx'
import Profile from './pages/Profile.jsx'
import NotFound from './pages/NotFound.jsx'

export default function App() {
  return (
    <Routes>
      {/* Public auth routes */}
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/verify-registration-otp" element={<VerifyRegistrationOtp />} />
      <Route path="/verify-login-otp" element={<VerifyLoginOtp />} />

      {/* Authenticated routes - ProtectedRoute checks a valid session exists;
          the backend independently re-checks every permission on every request. */}
      <Route element={<ProtectedRoute />}>
        <Route element={<Layout />}>
          <Route path="/dashboard" element={<DashboardRedirect />} />

          <Route element={<RoleProtectedRoute allowedRoles={['ADMIN']} />}>
            <Route path="/admin/dashboard" element={<AdminDashboard />} />
            <Route path="/admin/users" element={<AdminUsers />} />
            <Route path="/admin/audit-logs" element={<AuditLogs />} />
          </Route>

          <Route element={<RoleProtectedRoute allowedRoles={['MANAGER']} />}>
            <Route path="/manager/dashboard" element={<ManagerDashboard />} />
          </Route>

          <Route element={<RoleProtectedRoute allowedRoles={['TEAM_MEMBER']} />}>
            <Route path="/member/dashboard" element={<MemberDashboard />} />
          </Route>

          {/* Shared across roles - each page/service call scopes data by role server-side */}
          <Route path="/projects" element={<ProjectsList />} />
          <Route path="/projects/:id" element={<ProjectDetail />} />

          <Route element={<RoleProtectedRoute allowedRoles={['ADMIN', 'MANAGER']} />}>
            <Route path="/team" element={<Team />} />
          </Route>

          <Route path="/profile" element={<Profile />} />
        </Route>
      </Route>

      <Route path="/" element={<DashboardRedirect />} />
      <Route path="*" element={<NotFound />} />
    </Routes>
  )
}
