import type { AuthSession, Credentials, CurrentUser, RegisterRequest } from '../types'
import { jsonRequest, request } from './httpClient'




export const authService = {
  login: async (credentials: Credentials): Promise<AuthSession> =>
    await request<AuthSession>('/api/v1/auth/login', jsonRequest('POST', credentials)),

  register: async (register: RegisterRequest): Promise<AuthSession> =>
    await request<AuthSession>('/api/v1/auth/register', jsonRequest('POST', register)),

}
