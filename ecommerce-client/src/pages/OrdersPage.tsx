import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { Message } from '../components/Message'
import { MoneyText } from '../components/MoneyText'
import { errorMessage } from '../services/httpClient'
import { orderService } from '../services/orderService'
import type { Order, OrderSummary } from '../types'
import { OrderDetail } from '../components/order/OrderDetail'

const OrdersPage = () => {
  const { orderId } = useParams()
  const [orders, setOrders] = useState<OrderSummary[]>([])
  const [selectedOrder, setSelectedOrder] = useState<Order | null>(null)
  const [loadingOrders, setLoadingOrders] = useState(true)
  const [loadingOrder, setLoadingOrder] = useState(false)
  const [ordersError, setOrdersError] = useState<string | null>(null)
  const [orderError, setOrderError] = useState<string | null>(null)

  useEffect(() => {
    const loadOrders = async (): Promise<void> => {
      setLoadingOrders(true)

      try {
        const loadedOrders = await orderService.getOrders()

        setOrders(loadedOrders)
        setOrdersError(null)
        
      } catch (error) {
        setOrdersError(errorMessage(error))
      } finally {
        setLoadingOrders(false)  
      }
    }

    loadOrders()
  }, [])

  useEffect(() => {
    if (!orderId) {
      setSelectedOrder(null)
      setLoadingOrder(false)
      setOrderError(null)
      return
    }

    const loadOrder = async (): Promise<void> => {
      setSelectedOrder(null)
      setLoadingOrder(true)

      try {
        const loadedOrder = await orderService.getOrder(orderId)

          setSelectedOrder(loadedOrder)
          setOrderError(null)
        
      } catch (error) {
          setOrderError(errorMessage(error))
      } finally {
          setLoadingOrder(false)
      }
    }

    loadOrder()
  }, [orderId])

  const error = ordersError ?? orderError

  return (
    <section className="content-section">
      <div className="section-heading">
        <div>
          <p className="eyebrow">Demo customer</p>
          <h1>Order history</h1>
        </div>
      </div>
      {error && <Message tone="error">{error}</Message>}
      {loadingOrders && <p className="muted">Loading orders…</p>}
      {!loadingOrders && orders.length === 0 && (
        <div className="empty-state">
          <h3>No orders yet</h3>
          <p>Completed checkouts appear here.</p>
        </div>
      )}
      <div className="order-layout">
        <div className="order-list">
          {orders.map((order) => (
            <Link
              key={order.id}
              to={`/orders/${order.id}`}
              className={selectedOrder?.id === order.id ? 'selected' : ''}
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
        {loadingOrder && <p className="muted">Loading order…</p>}
        
        {selectedOrder && <OrderDetail order={selectedOrder} />}
         
      </div>
    </section>
  )
}

export default OrdersPage
