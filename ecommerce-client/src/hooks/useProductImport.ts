import { useCallback, useState } from 'react'
import useSWR from 'swr'
import useSWRMutation from 'swr/mutation'
import { swrKeys } from '../config/swrKeys'
import { errorMessage } from '../services/httpClient'
import { productImportService } from '../services/productImportService'
import type { ImportResult, ImportStatus } from '../types'

const terminalStatuses: ReadonlySet<ImportStatus> = new Set([
  'COMPLETED',
  'COMPLETED_WITH_ERRORS',
  'FAILED',
])

export function isTerminalImportStatus(status: ImportStatus) {
  return terminalStatuses.has(status)
}

export function useProductImport() {
  const [statusUrl, setStatusUrl] = useState<string | null>(null)
  const [submittedResult, setSubmittedResult] = useState<ImportResult | null>(null)
  const [localError, setError] = useState<string | null>(null)

  const submission = useSWRMutation(
    swrKeys.importSubmission,
    (_key, { arg: file }: { arg: File }) => productImportService.submit(file),
  )

  const statusRequest = useSWR(
    statusUrl ? swrKeys.importStatus(statusUrl) : null,
    () => productImportService.getStatus(statusUrl!),
    {
      fallbackData: submittedResult ?? undefined,
      refreshInterval: (latestResult) =>
        latestResult && isTerminalImportStatus(latestResult.status) ? 0 : 2000,
    },
  )

  const startImport = useCallback(
    async (file: File) => {
      submission.reset()
      setError(null)
      setStatusUrl(null)
      setSubmittedResult(null)
      try {
        const submitted = await submission.trigger(file)
        const pendingResult: ImportResult = {
          ...submitted,
          filename: file.name,
          summary: { created: 0, updated: 0, rejected: 0 },
          completedAt: null,
          rejectedRows: [],
        }
        setSubmittedResult(pendingResult)
        setStatusUrl(submitted.statusUrl)
      } catch (caught) {
        setError(errorMessage(caught))
      }
    },
    [submission],
  )

  const requestError = submission.error ?? statusRequest.error

  return {
    result: statusRequest.data ?? submittedResult,
    uploading: submission.isMutating,
    error: localError ?? (requestError ? errorMessage(requestError) : null),
    setError,
    startImport,
  }
}
