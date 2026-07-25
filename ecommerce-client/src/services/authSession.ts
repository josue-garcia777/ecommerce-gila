import type { AuthSession } from '../types'

const authStorageKey = 'e-commerce-auth-session'

export const getAuthSession = (): AuthSession | null => {
  const storedSession = localStorage.getItem(authStorageKey)

  if (!storedSession) {
    return null
  }

  try {
    return JSON.parse(storedSession) as AuthSession
  } catch {
    localStorage.removeItem(authStorageKey)
    return null
  }
}

export const saveAuthSession = (session: AuthSession): void => {
  localStorage.setItem(authStorageKey, JSON.stringify(session))
}

export const clearAuthSession = (): void => {
  localStorage.removeItem(authStorageKey)
}

export const getAccessToken = (): string | null => getAuthSession()?.accessToken ?? null
