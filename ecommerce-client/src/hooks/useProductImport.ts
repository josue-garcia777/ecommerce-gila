import { useEffect, useState } from 'react'
import { errorMessage } from '../services/httpClient'
import { productImportService } from '../services/productImportService'
import type { ImportResult, ImportStatus } from '../types'

const terminalStatuses: ReadonlySet<ImportStatus> = new Set([
  'COMPLETED',
  'COMPLETED_WITH_ERRORS',
  'FAILED',
])

export const isTerminalImportStatus = (status: ImportStatus): boolean =>
  terminalStatuses.has(status)

export const useProductImport = () => {
  const [result, setResult] = useState<ImportResult | null>(null)
  const [statusUrl, setStatusUrl] = useState<string | null>(null)
  const [uploading, setUploading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
  if (!statusUrl) {
    return
  }

  const controller = new AbortController()
  let timeout: number | undefined

  const pollImportStatus = async (): Promise<void> => {
    try {
      const latestResult = await productImportService.getStatus(
        statusUrl,
        controller.signal,
      )

      setResult(latestResult)
      setError(null)

      if (!isTerminalImportStatus(latestResult.status)) {
        timeout = window.setTimeout(pollImportStatus, 2000)
      }
    } catch (error) {
      if (controller.signal.aborted) {
        return
      }

      setError(errorMessage(error))
      timeout = window.setTimeout(pollImportStatus, 2000)
    }
  }

  pollImportStatus()

  return () => {
    controller.abort()

    if (timeout) {
      window.clearTimeout(timeout)
    }
  }
}, [statusUrl])

  const startImport = async (file: File): Promise<void> => {
    setUploading(true)
    setError(null)
    setResult(null)
    setStatusUrl(null)

    try {
      const submitted = await productImportService.submit(file)
      setResult({
        ...submitted,
        filename: file.name,
        summary: { created: 0, updated: 0, rejected: 0 },
        completedAt: null,
        rejectedRows: [],
      })
      setStatusUrl(submitted.statusUrl)
    } catch (error) {
      setError(errorMessage(error))
    } finally {
      setUploading(false)
    }
  }

  return {
    result,
    uploading,
    error,
    setError,
    startImport,
  }
}
