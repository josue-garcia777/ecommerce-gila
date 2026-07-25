import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { Message } from '../Message'
import { useAuth } from '../../hooks/useAuth'
import { errorMessage } from '../../services/httpClient'

type LoginFormProps = {
  onSuccess: () => void
}

export const LoginForm = ({ onSuccess }: LoginFormProps) => {
  const { login } = useAuth()

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const submit = async (event: FormEvent<HTMLFormElement>): Promise<void> => {
    event.preventDefault()
    setSubmitting(true)
    setError(null)

    try {
      await login(email, password)
      onSuccess()
    } catch (caught) {
      setError(errorMessage(caught))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <>
      <p className="eyebrow">Customer account</p>

      <h1 className="m-0 font-display text-[40px] font-bold tracking-[-1px]">Welcome back</h1>

      <p className="mb-6 leading-6 text-muted">Log in to manage your cart, orders, and account.</p>
      <span className='mb-2 text-xs leading-normal text-muted'>for demo: default admin creds: josue@gmail.com - 123456789</span>

      {error && <Message tone="error">{error}</Message>}

      <form className="grid gap-4" onSubmit={(event) => void submit(event)}>
        <label>
          <span>Email</span>
          <input
            type="email"
            autoComplete="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            required
          />
        </label>

        <label>
          <span>Password</span>
          <input
            type="password"
            autoComplete="current-password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            maxLength={72}
            required
          />
        </label>

        <button className="primary mt-2" type="submit" disabled={submitting}>
          {submitting ? 'Please wait…' : 'Log in'}
        </button>
      </form>

      <p className="mb-0 mt-5 text-sm text-muted">
        New here?{' '}
        <Link className="font-bold text-moss" to="/register">
          Create an account
        </Link>
      </p>
    </>
  )
}
