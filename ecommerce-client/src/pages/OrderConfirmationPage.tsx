import { useEffect, useState } from 'react'
import { Link, useLocation, useParams } from 'react-router-dom'
import { Message } from '../components/Message'
import { MoneyText } from '../components/MoneyText'
import { errorMessage } from '../services/httpClient'
import { orderService } from '../services/orderService'
import type { Order } from '../types'
import { OrderSummary } from '../components/order/OrderDetail'

type ConfirmationLocationState = {
  order?: Order
}

const OrderConfirmationPage = () => {
  const { orderId } = useParams()
  const location = useLocation()

  const locationOrder = (location.state as ConfirmationLocationState | null)?.order
  const initialOrder = locationOrder && locationOrder.id === orderId ? locationOrder : null
  
  const [order, setOrder] = useState<Order | null>(initialOrder)
  const [loading, setLoading] = useState(!initialOrder)
  const [error, setError] = useState<string | null>(null)

useEffect(() => {
  if (!orderId) {
    setOrder(null)
    setError(null)
    setLoading(false)
    return
  }

  const controller = new AbortController()

  const loadOrder = async (): Promise<void> => {
    const cachedOrder =
      locationOrder?.id === orderId ? locationOrder : null

    setOrder(cachedOrder)
    setError(null)
    setLoading(cachedOrder === null)

    try {
      const loadedOrder = await orderService.getOrder(
        orderId,
        controller.signal,
      )

      if (controller.signal.aborted) {
        return
      }

      setOrder(loadedOrder)
      setError(null)
    } catch (error) {
      if (controller.signal.aborted) {
        return
      }

      setError(errorMessage(error))
    }

    setLoading(false)
  }

  void loadOrder()

  return () => {
    controller.abort()
  }
}, [locationOrder, orderId])

  return (
    <section className="content-section narrow">
      {error && <Message tone="error">{error}</Message>}
      {loading && <p className="muted">Loading your confirmation…</p>}
      {order && (
        <article className="order-confirmation">
          <span className="success-mark">✓</span>
          <p className="eyebrow">Payment accepted</p>
          <h1>Thanks for your order.</h1>
          <p>Inventory is committed and your confirmation is ready.</p>
          <div className="confirmation-meta">
            <span>
              <small>Order</small>
              {order.id}
            </span>
            <span>
              <small>Payment</small>
              {order.paymentReference}
            </span>
          </div>
          <OrderSummary order={order} />
          
          <Link className="primary inline-block" to="/">
            Continue shopping
          </Link>
        </article>
      )}
    </section>
  )
}

export default OrderConfirmationPage
