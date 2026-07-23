import type { Cart, Order } from '../types'
import { jsonRequest, request } from './httpClient'

export const cartService = {
  createOrGet: () => request<Cart>('/api/v1/carts', { method: 'POST' }),

  get: (cartId: string) => request<Cart>(`/api/v1/carts/${cartId}`),

  setQuantity: (cartId: string, productId: string, quantity: number) =>
    request<Cart>(`/api/v1/carts/${cartId}/items/${productId}`, jsonRequest('PUT', { quantity })),

  removeItem: (cartId: string, productId: string) =>
    request<Cart>(`/api/v1/carts/${cartId}/items/${productId}`, { method: 'DELETE' }),

  checkout: (cartId: string, idempotencyKey: string) =>
    request<Order>(`/api/v1/carts/${cartId}/checkout`, {
      method: 'POST',
      headers: { 'Idempotency-Key': idempotencyKey },
    }),
}
