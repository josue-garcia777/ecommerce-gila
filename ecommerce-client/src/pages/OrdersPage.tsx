import { Link, useParams } from 'react-router-dom'
import useSWR from 'swr'
import { Message } from '../components/Message'
import { MoneyText } from '../components/MoneyText'
import { swrKeys } from '../config/swrKeys'
import { errorMessage } from '../services/httpClient'
import { orderService } from '../services/orderService'

export default function OrdersPage() {
  const { orderId } = useParams()
  const ordersRequest = useSWR(swrKeys.orders, orderService.list)
  const selectedOrderRequest = useSWR(orderId ? swrKeys.order(orderId) : null, () =>
    orderService.get(orderId!),
  )
  const selected = selectedOrderRequest.data
  const requestError = ordersRequest.error ?? selectedOrderRequest.error

  return (
    <section className="content-section">
      <div className="section-heading">
        <div>
          <p className="eyebrow">Demo customer</p>
          <h1>Order history</h1>
        </div>
      </div>
      {requestError && <Message tone="error">{errorMessage(requestError)}</Message>}
      {ordersRequest.isLoading && <p className="muted">Loading orders…</p>}
      {!ordersRequest.isLoading && ordersRequest.data?.length === 0 && (
        <div className="empty-state">
          <h3>No orders yet</h3>
          <p>Completed checkouts appear here.</p>
        </div>
      )}
      <div className="order-layout">
        <div className="order-list">
          {ordersRequest.data?.map((order) => (
            <Link
              key={order.id}
              to={`/orders/${order.id}`}
              className={selected?.id === order.id ? 'selected' : ''}
            >
              <span>
                <strong>Order {order.id.slice(0, 8)}</strong>
                <small>{new Date(order.createdAt).toLocaleString()}</small>
              </span>
              <span>
                <MoneyText money={order.total} />
                <small>{order.status}</small>
              </span>
            </Link>
          ))}
        </div>
        {selectedOrderRequest.isLoading && <p className="muted">Loading order…</p>}
        {selected && (
          <article className="order-detail">
            <span className="success-mark">✓</span>
            <p className="eyebrow">Confirmed</p>
            <h2>Order {selected.id.slice(0, 8)}</h2>
            <p className="muted">Payment reference {selected.paymentReference}</p>
            <div className="confirmation-items">
              {selected.items.map((item) => (
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
              <span>Total</span>
              <strong>
                <MoneyText money={selected.total} />
              </strong>
            </div>
          </article>
        )}
      </div>
    </section>
  )
}
