import type { Order, OrderSummary } from '../types'
import { request } from './httpClient'

export const orderService = {
  getOrder: async (orderId: string, signal? : AbortSignal): Promise<Order> => await request<Order>(`/api/v1/orders/${orderId}`, { method: 'GET'}, signal),
  getOrders: async (): Promise<OrderSummary[]> => await request<OrderSummary[]>('/api/v1/orders'),
}
