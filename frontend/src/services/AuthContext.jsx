import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { setUnauthorizedHandler } from './api'
import * as authService from './authService'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  // Load the current session once on mount. A 401 simply means "signed out".
  useEffect(() => {
    let active = true

    // Any 401 from the API drops the session; ProtectedRoute then redirects.
    setUnauthorizedHandler(() => {
      setUser(null)
    })

    authService
      .getCurrentUser()
      .then((me) => {
        if (active) setUser(me)
      })
      .catch(() => {
        if (active) setUser(null)
      })
      .finally(() => {
        if (active) setLoading(false)
      })

    return () => {
      active = false
      setUnauthorizedHandler(null)
    }
  }, [])

  const login = useCallback(async (email, password) => {
    const authenticated = await authService.login(email, password)
    setUser(authenticated)
    return authenticated
  }, [])

  const logout = useCallback(async () => {
    try {
      await authService.logout()
    } finally {
      setUser(null)
    }
  }, [])

  const value = useMemo(
    () => ({
      user,
      loading,
      login,
      logout,
      isAuthenticated: Boolean(user),
      role: user?.role || null,
    }),
    [user, loading, login, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used inside an AuthProvider')
  }
  return context
}
