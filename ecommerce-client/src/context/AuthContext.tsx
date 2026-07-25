import { createContext, useEffect, useState } from 'react'
import type { ReactNode } from 'react'
import { authService } from '../services/authService'
import { clearAuthSession, getAuthSession, saveAuthSession } from '../services/authSession'
import type { AuthSession, CurrentUser, RegisterRequest, UserRole } from '../types'

export type AuthContextValue = {
  user: CurrentUser | null
  isAuthenticated: boolean
  isAdmin: boolean
  login: (email: string, password: string) => Promise<void>
  register: (register: RegisterRequest) => Promise<void>
  logout: () => void
  hasRole: (role: UserRole) => boolean
}

export const AuthContext = createContext<AuthContextValue | null>(null)

const activeSession = (): AuthSession | null => {
  const session = getAuthSession()

  if (!session || new Date(session.expiresAt).getTime() <= Date.now()) {
    clearAuthSession()
    return null
  }

  return session
}

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [session, setSession] = useState<AuthSession | null>(activeSession)

  const setAuthenticatedSession = (nextSession: AuthSession): void => {
    saveAuthSession(nextSession)
    setSession(nextSession)
  }

  const login = async (email: string, password: string): Promise<void> => {
    setAuthenticatedSession(await authService.login({ email, password }))
  }

  const register = async (register: RegisterRequest): Promise<void> => {
    setAuthenticatedSession(await authService.register(register))
  }

  const logout = (): void => {
    clearAuthSession()
    setSession(null)
  }

  useEffect(() => {
    if (!session) {
      return
    }

    const delay = Math.max(new Date(session.expiresAt).getTime() - Date.now(), 0)
    const timeout = window.setTimeout(logout, delay)

    return () => window.clearTimeout(timeout)
  }, [session])

  const hasRole = (role: UserRole): boolean => session?.user.roles.includes(role) ?? false

  return (
    <AuthContext.Provider
      value={{
        user: session?.user ?? null,
        isAuthenticated: session !== null,
        isAdmin: hasRole('ADMIN'),
        login,
        register,
        logout,
        hasRole,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}
