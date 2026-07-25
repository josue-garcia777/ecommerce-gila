import { createContext, useContext, useEffect, useState } from 'react'
import type { ReactNode } from 'react'
import { useAuth } from '../hooks/useAuth'
import { cartService } from '../services/cartService'
import { ApiError, errorMessage } from '../services/httpClient'
import type { Cart, Order } from '../types'

const cartStorageKey = 'e-commerce-cart-id'

const checkoutStorageKey = (cartId: string): string => `checkout-idempotency:${cartId}`

const removeCheckoutFromStorage = (cartId: string): void => {
  sessionStorage.removeItem(checkoutStorageKey(cartId))
}

const isNotFound = (error: unknown): boolean => error instanceof ApiError && error.status === 404

type CartContextValue = {
  cart: Cart | null
  itemCount: number
  isLoading: boolean
  restoreError: string | null
  addItem: (productId: string) => Promise<Cart>
  setQuantity: (productId: string, quantity: number) => Promise<Cart>
  removeItem: (productId: string) => Promise<Cart>
  checkout: () => Promise<Order>
}

type CartUpdate = (cart: Cart) => Promise<Cart>

const CartContext = createContext<CartContextValue | null>(null)

const loadOrCreateActiveCart = async (): Promise<Cart> => {
  const storedCartId = localStorage.getItem(cartStorageKey)

  if (!storedCartId) {
    return await cartService.createOrGetCart()
  }

  try {
    const storedCart = await cartService.getCart(storedCartId)

    if (storedCart.status === 'ACTIVE') {
      return storedCart
    }
  } catch (error) {
    if (!isNotFound(error)) {
      console.error('Unknow error happened loading the Cart', error)
      throw error
    }
  }

  localStorage.removeItem(cartStorageKey)

  return await cartService.createOrGetCart()
}

export const useCart = (): CartContextValue => {
  const context = useContext(CartContext)

  if (!context) {
    throw new Error('useCart must be used within CartProvider')
  }

  return context
}

export const CartProvider = ({ children }: { children: ReactNode }) => {
  const { isAuthenticated, user } = useAuth()
  const [cart, setCart] = useState<Cart | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const loadCartFromLocalStorage = (nextCart: Cart): Cart => {
    localStorage.setItem(cartStorageKey, nextCart.id)
    setCart(nextCart)

    return nextCart
  }

  const removeCartFromLocalStorage = (discardedCartId?: string): void => {
    const cartId = discardedCartId ?? cart?.id

    localStorage.removeItem(cartStorageKey)
    setCart(null)

    if (cartId) {
      removeCheckoutFromStorage(cartId)
    }
  }

  const restoreCart = async (): Promise<Cart> => {
    setIsLoading(true)

    try {
      const activeCart = await loadOrCreateActiveCart()
      setError(null)
      return loadCartFromLocalStorage(activeCart)
    } finally {
      setIsLoading(false)
    }
  }

  const returnValidCart = async (): Promise<Cart> => {
    if (cart?.status === 'ACTIVE') {
      return cart
    }

    return await restoreCart()
  }

  const retrieveCart = async (staleCartId: string): Promise<Cart> => {
    removeCartFromLocalStorage(staleCartId)

    return await restoreCart()
  }

  const updateActiveCart = async (update: CartUpdate): Promise<Cart> => {
    let activeCart = await returnValidCart()

    try {
      const updatedCart = await update(activeCart)
      return loadCartFromLocalStorage(updatedCart)
    } catch (error) {
      if (!isNotFound(error)) {
        throw error
      }

      activeCart = await retrieveCart(activeCart.id)

      const updatedCart = await update(activeCart)
      return loadCartFromLocalStorage(updatedCart)
    } finally {
      removeCheckoutFromStorage(activeCart.id)
    }
  }

  const addItem = async (productId: string): Promise<Cart> => {
    return await updateActiveCart(async (activeCart) => {
      const currentQuantity =
        activeCart.items.find((item) => item.productId === productId)?.quantity ?? 0

      return await cartService.setQuantity(activeCart.id, productId, currentQuantity + 1)
    })
  }

  const setQuantity = async (productId: string, quantity: number): Promise<Cart> => {
    return await updateActiveCart(async (activeCart) => {
      return await cartService.setQuantity(activeCart.id, productId, quantity)
    })
  }

  const removeItem = async (productId: string): Promise<Cart> => {
    const activeCart = await returnValidCart()

    try {
      const updatedCart = await cartService.removeItem(activeCart.id, productId)
      return loadCartFromLocalStorage(updatedCart)
    } catch (error) {
      if (!isNotFound(error)) {
        throw error
      }

      return await retrieveCart(activeCart.id)
    } finally {
      removeCheckoutFromStorage(activeCart.id)
    }
  }

  const checkout = async (): Promise<Order> => {
    const activeCart = await returnValidCart()
    const storageKey = checkoutStorageKey(activeCart.id)
    const idempotencyKey = sessionStorage.getItem(storageKey) ?? crypto.randomUUID()

    sessionStorage.setItem(storageKey, idempotencyKey)

    const order = await cartService.checkout(activeCart.id, idempotencyKey)
    removeCartFromLocalStorage(activeCart.id)

    return order
  }

  useEffect(() => {
    if (!isAuthenticated) {
      const storedCartId = localStorage.getItem(cartStorageKey)
      localStorage.removeItem(cartStorageKey)

      if (storedCartId) {
        removeCheckoutFromStorage(storedCartId)
      }

      setCart(null)
      setError(null)
      setIsLoading(false)
    }

    const initializeCart = async (): Promise<void> => {
      setIsLoading(true)

      try {
        const activeCart = await loadOrCreateActiveCart()

        setError(null)
        loadCartFromLocalStorage(activeCart)
      } catch (error) {
        setError(errorMessage(error))
      } finally {
          setIsLoading(false)
      }
    }

    initializeCart()
  }, [isAuthenticated, user?.id])

  const itemCount = cart?.items.reduce((total, item) => total + item.quantity, 0) ?? 0

  const value: CartContextValue = {
    cart,
    itemCount,
    isLoading,
    restoreError: error,
    addItem,
    setQuantity,
    removeItem,
    checkout,
  }

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>
}
