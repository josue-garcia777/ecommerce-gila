import type { SWRConfiguration } from 'swr'
import { ApiError } from '../services/httpClient'

export const swrConfig: SWRConfiguration = {
  dedupingInterval: 1000,
  errorRetryCount: 2,
  shouldRetryOnError: (error: unknown) => !(error instanceof ApiError) || error.status >= 500,
}
