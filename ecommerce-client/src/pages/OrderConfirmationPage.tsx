import { Link, useLocation, useParams } from 'react-router-dom'
import useSWR from 'swr'
import { Message } from '../components/Message'
import { MoneyText } from '../components/MoneyText'
import { swrKeys } from '../config/swrKeys'
import { errorMessage } from '../services/httpClient'
import { orderService } from '../services/orderService'
import type { Order } from '../types'

type ConfirmationLocationState = {
  order?: Order
}

export default function OrderConfirmationPage() {
  const { orderId } = useParams()
  const location = useLocation()
  const state = location.state as ConfirmationLocationState | null
  const locationOrder = state?.order
  const initialOrder = locationOrder?.id === orderId ? locationOrder : undefined
  
  const orderRequest = useSWR(orderId ? swrKeys.order(orderId) : null, () => orderService.get(orderId!),{ fallbackData: initialOrder },
)
  const order = orderRequest.data

  return (
    <section className="content-section narrow">
      {orderRequest.error && <Message tone="error">{errorMessage(orderRequest.error)}</Message>}
      {orderRequest.isLoading && <p className="muted">Loading your confirmation…</p>}
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
          <div className="confirmation-items">
            {order.items.map((item) => (
              <div key={item.productId}>
                <span>
                  {item.productName}
                  <small>
                    {item.sku} · Qty {item.quantity}
                  </small>
                </span>
                <strong>
                  <MoneyText money={item.lineTotal} />
                </strong>
              </div>
            ))}
          </div>
          <div className="confirmation-total">
            <span>Total paid</span>
            <strong>
              <MoneyText money={order.total} />
            </strong>
          </div>
          <Link className="primary inline-block" to="/">
            Continue shopping
          </Link>
        </article>
      )}
    </section>
  )
}
