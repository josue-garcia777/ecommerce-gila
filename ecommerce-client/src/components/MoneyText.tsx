import type { Money } from '../types'

export const MoneyText = ({ money }: { money: Money | null }) => {
  if (!money) return <span>Multiple currencies</span>
  return (
    <span>
      {new Intl.NumberFormat(undefined, { style: 'currency', currency: money.currency }).format(
        money.amount,
      )}
    </span>
  )
}
