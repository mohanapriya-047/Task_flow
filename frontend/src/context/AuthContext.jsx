import React, { createContext, useContext, useEffect, useState } from 'react'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const storedUser = localStorage.getItem('taskflow_user')
    const token = localStorage.getItem('taskflow_access_token')
    if (storedUser && token) {
      setUser(JSON.parse(storedUser))
    }
    setLoading(false)
  }, [])

  function completeLogin(accessToken, userResponse) {
    localStorage.setItem('taskflow_access_token', accessToken)
    localStorage.setItem('taskflow_user', JSON.stringify(userResponse))
    setUser(userResponse)
  }

  function signOut() {
    localStorage.removeItem('taskflow_access_token')
    localStorage.removeItem('taskflow_user')
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ user, loading, completeLogin, signOut }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
