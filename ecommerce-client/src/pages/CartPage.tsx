import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { errorMessage } from '../services/httpClient'
import { Message } from '../components/Message'
import { MoneyText } from '../components/MoneyText'
import { useCart } from '../context/CartContext'
import type { CartItem } from '../types'

export default function CartPage() {
  const { cart, isLoading, ensureCart, setQuantity, removeItem, checkout: checkoutCart } = useCart()
  const navigate = useNavigate()
  const [checkoutPending, setCheckoutPending] = useState(false)
  const [updating, setUpdating] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (cart) return
    void ensureCart().catch((caught) => setError(errorMessage(caught)))
  }, [cart, ensureCart])

  const update = async (item: CartItem, quantity: number) => {
    if (!cart || quantity <= 0) return
    try {
      setUpdating(item.productId)
      setError(null)
      await setQuantity(item.productId, quantity)
    } catch (caught) {
      setError(errorMessage(caught))
    } finally {
      setUpdating(null)
    }
  }

  const remove = async (productId: string) => {
    if (!cart) return
    try {
      setUpdating(productId)
      setError(null)
      await removeItem(productId)
    } catch (caught) {
      setError(errorMessage(caught))
    } finally {
      setUpdating(null)
    }
  }

  const checkout = async () => {
    if (!cart || checkoutPending) return
    try {
      setCheckoutPending(true)
      setError(null)
      const order = await checkoutCart()
      navigate(`/orders/${order.id}/confirmation`, { state: { order } })
    } catch (caught) {
      setError(errorMessage(caught))
    } finally {
      setCheckoutPending(false)
    }
  }

  return (
    <section className="content-section">
      <div className="section-heading">
        <div>
          <p className="eyebrow">Demo customer</p>
          <h1>Your cart</h1>
        </div>
        <Link className="text-button" to="/">
          Continue shopping →
        </Link>
      </div>
      {error && <Message tone="error">{error}</Message>}
      {isLoading && <p className="muted">Loading your cart…</p>}
      {!isLoading && cart && cart.items.length === 0 && (
        <div className="empty-state">
          <h3>Your cart is empty</h3>
          <p>Add something useful from the storefront.</p>
          <Link className="primary inline-block" to="/">
            Browse products
          </Link>
        </div>
      )}
      {cart && cart.items.length > 0 && (
        <div className="cart-layout">
          <div className="cart-items">
            {cart.items.map((item) => (
              <article key={item.productId} className="cart-item">
                <div className="cart-thumb">{item.sku?.slice(0, 2) ?? '—'}</div>
                <div className="cart-copy">
                  <h3>{item.productName}</h3>
                  <p>{item.sku ?? 'Product unavailable'}</p>
                  {!item.available && <span className="availability">No more stock available</span>}
                </div>
                <div className="quantity-control" aria-label={`Quantity for ${item.productName}`}>
                  <button
                    disabled={item.quantity <= 1 || updating === item.productId}
                    onClick={() => void update(item, item.quantity - 1)}
                  >
                    −
                  </button>
                  <span>{item.quantity}</span>
                  <button
                    disabled={updating === item.productId}
                    onClick={() => void update(item, item.quantity + 1)}
                  >
                    +
                  </button>
                </div>
                <strong>
                  <MoneyText money={item.lineTotal} />
                </strong>
                <button
                  className="remove-button"
                  disabled={updating === item.productId}
                  onClick={() => void remove(item.productId)}
                >
                  Remove
                </button>
              </article>
            ))}
          </div>
          <aside className="cart-summary">
            <p className="eyebrow">Order summary</p>
            <div>
              <span>Items</span>
              <span>{cart.items.reduce((sum, item) => sum + item.quantity, 0)}</span>
            </div>
            <div className="summary-total">
              <span>Total</span>
              <strong>
                <MoneyText money={cart.subtotal} />
              </strong>
            </div>
            <button
              className="primary wide"
              disabled={
                checkoutPending || cart.items.some((item) => !item.available) || !cart.subtotal
              }
              onClick={() => void checkout()}
            >
              {checkoutPending ? 'Completing checkout…' : 'Checkout securely'}
            </button>
            <small>Prices and inventory are revalidated by the server.</small>
          </aside>
        </div>
      )}
    </section>
  )
}
