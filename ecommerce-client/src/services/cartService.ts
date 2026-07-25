import type { Cart, Order } from '../types'
import { jsonRequest, request } from './httpClient'

export const cartService = {
  createOrGetCart: async (): Promise<Cart> =>
    await request<Cart>('/api/v1/carts', { method: 'POST' }),

  getCart: async (cartId: string): Promise<Cart> => await request<Cart>(`/api/v1/carts/${cartId}`),

  setQuantity: async (cartId: string, productId: string, quantity: number): Promise<Cart> =>
    await request<Cart>(
      `/api/v1/carts/${cartId}/items/${productId}`,
      jsonRequest('PUT', { quantity }),
    ),

  removeItem: async (cartId: string, productId: string): Promise<Cart> =>
    await request<Cart>(`/api/v1/carts/${cartId}/items/${productId}`, {
      method: 'DELETE',
    }),

  checkout: async (cartId: string, idempotencyKey: string): Promise<Order> =>
    await request<Order>(`/api/v1/carts/${cartId}/checkout`, {
      method: 'POST',
      headers: { 'Idempotency-Key': idempotencyKey },
    }),
}
