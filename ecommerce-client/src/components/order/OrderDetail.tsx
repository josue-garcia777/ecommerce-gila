import { Address, Order } from '../../types'
import { MoneyText } from '../MoneyText'

type OrderDetailProps = {
  order: Order
}

export const OrderDetail = ({ order }: OrderDetailProps) => {
  return (
    <article className="order-detail">
      <span className="success-mark">✓</span>

      <p className="eyebrow">Confirmed</p>

      <h2>Order {order.id.slice(0, 8)}</h2>

      <p className="muted">Payment reference {order.paymentReference}</p>

      <OrderSummary order={order} />
    </article>
  )
}

export const OrderSummary = ({ order }: OrderDetailProps) => {
  return (
    <>
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

      <OrderAddress address={order.address} />

      <div className="confirmation-total">
        <span>Total</span>

        <strong>
          <MoneyText money={order.total} />
        </strong>
      </div>
    </>
  )
}

type OrderAddressProps = {
  address: Address
}

export const OrderAddress = ({ address }: OrderAddressProps) => {
  return (
    <section className="mt-6 border-t border-line pt-5">
      <p className="eyebrow mb-2">Shipping address</p>

      <address className="not-italic leading-6 text-muted">
        <p className="m-0">{address.line1}</p>

        {address.line2 && <p className="m-0">{address.line2}</p>}

        <p className="m-0">
          {address.city}
          {address.state ? `, ${address.state}` : ''}
        </p>

        <p className="m-0">
          {address.postalCode} {address.countryCode}
        </p>
      </address>
    </section>
  )
}
