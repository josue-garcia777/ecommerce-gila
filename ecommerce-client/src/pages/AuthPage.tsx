import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { Message } from '../components/Message'
import { useAuth } from '../hooks/useAuth'
import { errorMessage } from '../services/httpClient'
import { RegisterForm } from '../components/auth/RegisterForm'
import { LoginForm } from '../components/auth/LoginForm'

type LocationState = {
  from?: string
}

const AuthPage = () => {
  const { isAuthenticated } = useAuth()
  const location = useLocation()
  const navigate = useNavigate()
  const isRegistering = location.pathname === '/register'


  if (isAuthenticated) {
    return <Navigate to="/" replace />
  }

const handleSuccess = () => {
    const destination =
      (location.state as LocationState | null)?.from ?? '/'

    navigate(destination, { replace: true })
}


  return (
      <section className="content-section narrow">
      <div className="mx-auto max-w-[480px] rounded-[18px] border border-line bg-paper p-7 shadow-card">
        {isRegistering ? (
          <RegisterForm onSuccess={handleSuccess} />
        ) : (
          <LoginForm onSuccess={handleSuccess} />
        )}
      </div>
    </section>
  )
}

export default AuthPage
