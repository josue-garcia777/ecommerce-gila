export type Money = {
  amount: number
  currency: string
}

export type Product = {
  id: string
  sku: string
  name: string
  description: string
  category: string
  price: Money
  stock: number
  weightKg: number
  imageUrl: string | null
  active: boolean
  version: number
  createdAt: string
  updatedAt: string
}

export type ProductPage = {
  items: Product[]
  nextCursor: string | null
  hasMore: boolean
}

export type ProductPayload = {
  sku?: string
  name: string
  description: string
  category: string
  price: Money
  stock: number
  weightKg: number
  imageUrl: string | null
  version?: number
}

export type ImportStatus =
  'PENDING' | 'PROCESSING' | 'COMPLETED' | 'COMPLETED_WITH_ERRORS' | 'FAILED'

export type ImportSubmission = {
  importId: string
  status: ImportStatus
  submittedAt: string
  statusUrl: string
}

export type ImportResult = {
  importId: string
  status: ImportStatus
  filename: string
  summary: { created: number; updated: number; rejected: number }
  submittedAt: string
  completedAt: string | null
  rejectedRows: Array<{ rowNumber: number; sku: string | null; reason: string }>
}

export type CartItem = {
  productId: string
  sku: string | null
  productName: string
  unitPrice: Money | null
  quantity: number
  lineTotal: Money | null
  stock: number
  available: boolean
  imageUrl: string | null
}

export type Cart = {
  id: string
  status: 'ACTIVE' | 'CHECKED_OUT' | 'ABANDONED'
  items: CartItem[]
  subtotal: Money | null
  version: number
  createdAt: string
  updatedAt: string
}

export type OrderItem = {
  productId: string
  sku: string
  productName: string
  unitPrice: Money
  quantity: number
  lineTotal: Money
}

export type Order = {
  id: string
  cartId: string
  status: 'CONFIRMED'
  total: Money
  paymentReference: string
  createdAt: string
  items: OrderItem[]
}

export type OrderSummary = Omit<Order, 'paymentReference' | 'items'>
