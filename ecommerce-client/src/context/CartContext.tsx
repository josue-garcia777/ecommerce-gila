import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from 'react'
import type { ReactNode } from 'react'
import useSWR, { useSWRConfig } from 'swr'
import useSWRMutation from 'swr/mutation'
import { swrKeys } from '../config/swrKeys'
import { cartService } from '../services/cartService'
import { ApiError } from '../services/httpClient'
import type { Cart, Order, OrderSummary } from '../types'

const cartStorageKey = 'gila-commerce-cart-id'
const checkoutStorageKey = (cartId: string) => `checkout-idempotency:${cartId}`
const resetCheckout = (cartId: string) => sessionStorage.removeItem(checkoutStorageKey(cartId))

type CartContextValue = {
  cart: Cart | null
  itemCount: number
  isLoading: boolean
  ensureCart: () => Promise<Cart>
  addItem: (productId: string) => Promise<Cart>
  setQuantity: (productId: string, quantity: number) => Promise<Cart>
  removeItem: (productId: string) => Promise<Cart>
  checkout: () => Promise<Order>
}

const CartContext = createContext<CartContextValue | null>(null)

const isNotFound = (error: unknown) => error instanceof ApiError && error.status === 404

async function restoreActiveCart() {
  const storedId = localStorage.getItem(cartStorageKey)
  if (storedId) {
    try {
      const storedCart = await cartService.get(storedId)
      if (storedCart.status === 'ACTIVE') return storedCart
    } catch (error) {
      if (!isNotFound(error)) throw error
    }
    localStorage.removeItem(cartStorageKey)
  }
  return cartService.createOrGet()
}

export function CartProvider({ children }: { children: ReactNode }) {
  const [cartId, setCartId] = useState<string | null>(null)
  const cartRef = useRef<Cart | null>(null)
  const restoreRequestRef = useRef<Promise<Cart> | null>(null)
  const { mutate: mutateCache } = useSWRConfig()

  const cartRequest = useSWR(cartId ? swrKeys.cart(cartId) : null, () => cartService.get(cartId!), {
    revalidateOnMount: false,
  })
  const { trigger: restoreCart, error: restoreError } = useSWRMutation(
    swrKeys.activeCart,
    restoreActiveCart,
  )
  const cart = cartRequest.data ?? null

  useEffect(() => {
    cartRef.current = cart
  }, [cart])

  const rememberCart = useCallback(
    async (nextCart: Cart) => {
      cartRef.current = nextCart
      localStorage.setItem(cartStorageKey, nextCart.id)
      await mutateCache(swrKeys.cart(nextCart.id), nextCart, { revalidate: false })
      setCartId(nextCart.id)
      return nextCart
    },
    [mutateCache],
  )

  const forgetCart = useCallback(
    async (discardedCartId?: string) => {
      const id = discardedCartId ?? cartRef.current?.id ?? cartId
      cartRef.current = null
      localStorage.removeItem(cartStorageKey)
      setCartId(null)
      if (id) {
        resetCheckout(id)
        await mutateCache(swrKeys.cart(id), undefined, { revalidate: false })
      }
    },
    [cartId, mutateCache],
  )

  const ensureCart = useCallback((): Promise<Cart> => {
    if (cartRef.current?.status === 'ACTIVE') {
      return Promise.resolve(cartRef.current)
    }
    if (restoreRequestRef.current) {
      return restoreRequestRef.current
    }

    const request = restoreCart().then(rememberCart)
    restoreRequestRef.current = request
    const clearRequest = () => {
      if (restoreRequestRef.current === request) restoreRequestRef.current = null
    }
    void request.then(clearRequest, clearRequest)
    return request
  }, [rememberCart, restoreCart])

  useEffect(() => {
    void ensureCart().catch(() => undefined)
  }, [ensureCart])

  useEffect(() => {
    const cartIsMissing = isNotFound(cartRequest.error)
    const cartIsInactive = cart !== null && cart.status !== 'ACTIVE'
    if (!cartId || (!cartIsMissing && !cartIsInactive)) return
    void forgetCart(cartId)
      .then(ensureCart)
      .catch(() => undefined)
  }, [cart, cartId, cartRequest.error, ensureCart, forgetCart])

  const recoverCart = useCallback(
    async (staleCartId: string) => {
      await forgetCart(staleCartId)
      return ensureCart()
    },
    [ensureCart, forgetCart],
  )

  const addItem = useCallback(
    async (productId: string) => {
      let active = await ensureCart()
      const update = (current: Cart) => {
        const quantity = current.items.find((item) => item.productId === productId)?.quantity ?? 0
        return cartService.setQuantity(current.id, productId, quantity + 1)
      }
      try {
        return await update(active).then(rememberCart)
      } catch (error) {
        if (!isNotFound(error)) throw error
        active = await recoverCart(active.id)
        return update(active).then(rememberCart)
      } finally {
        resetCheckout(active.id)
      }
    },
    [ensureCart, recoverCart, rememberCart],
  )

  const setQuantity = useCallback(
    async (productId: string, quantity: number) => {
      let active = await ensureCart()
      const update = (current: Cart) => cartService.setQuantity(current.id, productId, quantity)
      try {
        return await update(active).then(rememberCart)
      } catch (error) {
        if (!isNotFound(error)) throw error
        active = await recoverCart(active.id)
        return update(active).then(rememberCart)
      } finally {
        resetCheckout(active.id)
      }
    },
    [ensureCart, recoverCart, rememberCart],
  )

  const removeItem = useCallback(
    async (productId: string) => {
      const active = await ensureCart()
      try {
        return await cartService.removeItem(active.id, productId).then(rememberCart)
      } catch (error) {
        if (!isNotFound(error)) throw error
        return recoverCart(active.id)
      } finally {
        resetCheckout(active.id)
      }
    },
    [ensureCart, recoverCart, rememberCart],
  )

  const checkout = useCallback(async () => {
    const active = await ensureCart()
    const storageKey = checkoutStorageKey(active.id)
    const idempotencyKey = sessionStorage.getItem(storageKey) ?? crypto.randomUUID()
    sessionStorage.setItem(storageKey, idempotencyKey)
    const order = await cartService.checkout(active.id, idempotencyKey)
    await mutateCache(swrKeys.order(order.id), order, { revalidate: false })
    await mutateCache(
      swrKeys.orders,
      (currentOrders: OrderSummary[] | undefined) => {
        const summary: OrderSummary = {
          id: order.id,
          cartId: order.cartId,
          status: order.status,
          total: order.total,
          createdAt: order.createdAt,
        }
        return [summary, ...(currentOrders?.filter((current) => current.id !== order.id) ?? [])]
      },
      { revalidate: false },
    )
    sessionStorage.removeItem(storageKey)
    await forgetCart(active.id)
    return order
  }, [ensureCart, forgetCart, mutateCache])

  const value = useMemo<CartContextValue>(
    () => ({
      cart,
      itemCount: cart?.items.reduce((total, item) => total + item.quantity, 0) ?? 0,
      isLoading: !cart && !restoreError,
      ensureCart,
      addItem,
      setQuantity,
      removeItem,
      checkout,
    }),
    [addItem, cart, checkout, ensureCart, removeItem, restoreError, setQuantity],
  )

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>
}

export function useCart() {
  const context = useContext(CartContext)
  if (!context) throw new Error('useCart must be used within CartProvider')
  return context
}
