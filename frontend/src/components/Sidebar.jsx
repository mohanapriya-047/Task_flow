import React from 'react'
import { NavLink } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

const LINKS = {
  ADMIN: [
    { to: '/admin/dashboard', label: 'Dashboard' },
    { to: '/projects', label: 'Projects' },
    { to: '/admin/users', label: 'Users' },
    { to: '/team', label: 'Team Members' },
    { to: '/admin/audit-logs', label: 'Audit Logs' },
    { to: '/profile', label: 'Profile' },
  ],
  MANAGER: [
    { to: '/manager/dashboard', label: 'Dashboard' },
    { to: '/projects', label: 'My Projects' },
    { to: '/team', label: 'Team' },
    { to: '/profile', label: 'Profile' },
  ],
  TEAM_MEMBER: [
    { to: '/member/dashboard', label: 'Dashboard' },
    { to: '/projects', label: 'My Projects' },
    { to: '/profile', label: 'Profile' },
  ],
}

export default function Sidebar() {
  const { user } = useAuth()
  if (!user) return null
  const links = LINKS[user.role] || []
  return (
    <nav className="sidebar">
      {links.map((link) => (
        <NavLink
          key={link.to}
          to={link.to}
          className={({ isActive }) => 'sidebar-link' + (isActive ? ' active' : '')}
        >
          {link.label}
        </NavLink>
      ))}
    </nav>
  )
}
