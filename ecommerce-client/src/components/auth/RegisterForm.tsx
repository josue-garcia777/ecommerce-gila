import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { Message } from '../Message'
import { useAuth } from '../../hooks/useAuth'
import { errorMessage } from '../../services/httpClient'
import { RegisterRequest } from '../../types'

type RegisterFormProps = {
  onSuccess: () => void
}

export const RegisterForm = ({ onSuccess }: RegisterFormProps) => {
  const { register } = useAuth()

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [line1, setLine1] = useState('')
  const [line2, setLine2] = useState('')
  const [city, setCity] = useState('')
  const [state, setState] = useState('')
  const [postalCode, setPostalCode] = useState('')
  const [countryCode, setCountryCode] = useState('MX')

  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const submit = async (event: FormEvent<HTMLFormElement>): Promise<void> => {
    event.preventDefault()
    setSubmitting(true)
    setError(null)

    const request: RegisterRequest = {
      email,
      password,
      address: {
        line1,
        line2: line2 || null,
        city,
        state: state || null,
        postalCode,
        countryCode: countryCode.toUpperCase(),
      },
    }

    try {
      await register(request)
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

      <h1 className="m-0 font-display text-[40px] font-bold tracking-[-1px]">
        Create your account
      </h1>

      <p className="mb-6 leading-6 text-muted">
        Create a customer account to add products to a cart and check out.
      </p>

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
          <span>
            Password <small>at least 12 characters</small>
          </span>
          <input
            type="password"
            autoComplete="new-password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            minLength={12}
            maxLength={72}
            required
          />
        </label>

                <div className="mt-2">
          <p className="eyebrow mb-1">Shipping address</p>
        </div>

        <label>
          <span>Address line 1</span>
          <input
            type="text"
            autoComplete="address-line1"
            value={line1}
            onChange={(event) => setLine1(event.target.value)}
            required
          />
        </label>

        <label>
          <span>
            Address line 2 <small>optional</small>
          </span>
          <input
            type="text"
            autoComplete="address-line2"
            value={line2}
            onChange={(event) => setLine2(event.target.value)}
          />
        </label>

        <div className="grid gap-4 sm:grid-cols-2">
          <label>
            <span>City</span>
            <input
              type="text"
              autoComplete="address-level2"
              value={city}
              onChange={(event) => setCity(event.target.value)}
              required
            />
          </label>

          <label>
            <span>
              State <small>optional</small>
            </span>
            <input
              type="text"
              autoComplete="address-level1"
              value={state}
              onChange={(event) => setState(event.target.value)}
            />
          </label>
        </div>

        <div className="grid gap-4 sm:grid-cols-2">
          <label>
            <span>Postal code</span>
            <input
              type="text"
              autoComplete="postal-code"
              value={postalCode}
              onChange={(event) => setPostalCode(event.target.value)}
              required
            />
          </label>

          <label>
            <span>Country code</span>
            <input
              type="text"
              autoComplete="country"
              value={countryCode}
              onChange={(event) =>
                setCountryCode(event.target.value.toUpperCase())
              }
              minLength={2}
              maxLength={2}
              placeholder="MX"
              required
            />
          </label>
        </div>

        <button className="primary mt-2" type="submit" disabled={submitting}>
          {submitting ? 'Please wait…' : 'Create account'}
        </button>
      </form>

      <p className="mb-0 mt-5 text-sm text-muted">
        Already have an account?{' '}
        <Link className="font-bold text-moss" to="/login">
          Log in
        </Link>
      </p>
    </>
  )
}
