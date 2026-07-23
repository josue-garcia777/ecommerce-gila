import type { Order, OrderSummary } from '../types'
import { request } from './httpClient'

export const orderService = {
  get: (orderId: string) => request<Order>(`/api/v1/orders/${orderId}`),
  list: () => request<OrderSummary[]>('/api/v1/orders'),
}
