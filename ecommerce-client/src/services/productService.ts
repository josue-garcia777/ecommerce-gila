import type { Product, ProductPage, ProductPayload } from '../types'
import { jsonRequest, request } from './httpClient'

type ProductSearchQuery = {
  q?: string
  category?: string
  cursor?: string | null
  limit?: number
}

export const productService = {
  searchProduct: async (query: ProductSearchQuery = {}): Promise<ProductPage> => {
    const params = new URLSearchParams()
    if (query.q) params.set('q', query.q)
    if (query.category) params.set('category', query.category)
    if (query.cursor) params.set('cursor', query.cursor)
    params.set('limit', String(query.limit ?? 20))
    return await request<ProductPage>(`/api/v1/products?${params}`)
  },

  listCategories: async (): Promise<string[]> => await request<string[]>('/api/v1/categories'),

  createProduct: async (payload: ProductPayload): Promise<Product> =>
    await request<Product>('/api/v1/products', jsonRequest('POST', payload)),

  updateProduct: async (id: string, payload: ProductPayload): Promise<Product> =>
    await request<Product>(`/api/v1/products/${id}`, jsonRequest('PUT', payload)),

  removeProduct: async (id: string): Promise<void> =>
    await request<void>(`/api/v1/products/${id}`, { method: 'DELETE' }),
}
