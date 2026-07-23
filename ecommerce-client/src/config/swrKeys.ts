export const swrKeys = {
  productCategories: ['products', 'categories'] as const,
  
  productPage: (query: string, category: string, cursor: string | null, limit: number) =>
    ['products', 'page', query, category, cursor, limit] as const,
  
  importSubmission: ['product-imports', 'submission'] as const,
  
  importStatus: (statusUrl: string) => ['product-imports', 'status', statusUrl] as const,
  
  orders: ['orders', 'list'] as const,
  
  order: (orderId: string) => ['orders', 'detail', orderId] as const,
  
  activeCart: ['carts', 'active'] as const,
  
  cart: (cartId: string) => ['carts', 'detail', cartId] as const,
}
