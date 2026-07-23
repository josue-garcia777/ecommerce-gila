import type { ImportResult, ImportSubmission } from '../types'
import { request } from './httpClient'

export const productImportService = {
  submit(file: File) {
    const body = new FormData()
    body.append('file', file)
    return request<ImportSubmission>('/api/v1/product-imports', { method: 'POST', body })
  },

  getStatus: (statusUrl: string) => request<ImportResult>(statusUrl),
}
