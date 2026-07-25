import type { ImportResult, ImportSubmission } from '../types'
import { request } from './httpClient'

export const productImportService = {
  submit: async (file: File): Promise<ImportSubmission> => {
    const body = new FormData()
    body.append('file', file)
    return await request<ImportSubmission>('/api/v1/product-imports', {
      method: 'POST',
      body,
    })
  },

  getStatus: async (statusUrl: string, signal?: AbortSignal): Promise<ImportResult> =>
    await request<ImportResult>(statusUrl, { method: 'GET' } , signal),
}
