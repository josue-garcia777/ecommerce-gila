import type { Product, ProductPage, ProductPayload } from '../types'
import { jsonRequest, request } from './httpClient'

type ProductSearchQuery = {
  q?: string
  category?: string
  cursor?: string | null
  limit?: number
}

export const productService = {
  search(query: ProductSearchQuery = {}) {
    const params = new URLSearchParams()
    if (query.q) params.set('q', query.q)
    if (query.category) params.set('category', query.category)
    if (query.cursor) params.set('cursor', query.cursor)
    params.set('limit', String(query.limit ?? 20))
    return request<ProductPage>(`/api/v1/products?${params}`)
  },

  listCategories: () => request<string[]>('/api/v1/categories'),

  create: (payload: ProductPayload) =>
    request<Product>('/api/v1/products', jsonRequest('POST', payload)),

  update: (id: string, payload: ProductPayload) =>
    request<Product>(`/api/v1/products/${id}`, jsonRequest('PUT', payload)),

  remove: (id: string) => request<void>(`/api/v1/products/${id}`, { method: 'DELETE' }),
}
