import type { Product } from '../../types'
import { MoneyText } from '../MoneyText'

type ProductCardProps = {
  product: Product
  adding: boolean
  onAdd: (productId: string) => void
}

export const ProductCard = ({ product, adding, onAdd }: ProductCardProps) => {
  return (
    <article className="flex min-h-[410px] flex-col overflow-hidden rounded-[18px] border border-line bg-paper transition-[transform,box-shadow] duration-200 hover:-translate-y-1 hover:shadow-card">
      <div className="relative grid h-[175px] place-items-center overflow-hidden bg-[linear-gradient(135deg,#dfe9d8,#f1eedc)]">
        {product.imageUrl ? (
          <img className="h-full w-full object-cover" src={product.imageUrl} alt="" />
        ) : (
          <span className="font-display text-[58px] font-bold text-moss/50">
            {product.category.slice(0, 2).toUpperCase()}
          </span>
        )}
        <small className="absolute bottom-3 left-3 rounded-full bg-paper/90 px-2.5 py-1.5 font-bold">
          {product.category}
        </small>
      </div>
      <div className="flex flex-1 flex-col p-[18px]">
        <span
          className={`text-[11px] font-bold ${product.stock > 0 ? 'text-moss' : 'text-danger'}`}
        >
          {product.stock > 0 ? `${product.stock} in stock` : 'Out of stock'}
        </span>
        <h2 className="my-2.5 mb-1.5 font-display text-[22px] font-bold">{product.name}</h2>
        <p className="line-clamp-3 m-0 text-[13px] leading-6 text-muted">{product.description}</p>
        <div className="mt-auto flex items-center justify-between pt-5">
          <strong className="text-lg">
            <MoneyText money={product.price} />
          </strong>
          <button
            className="h-[38px] w-[46px] rounded-[10px] border-0 bg-ink font-bold text-white"
            onClick={() => onAdd(product.id)}
            disabled={product.stock === 0 || adding}
            aria-label={`Add ${product.name} to cart`}
          >
            {adding ? 'Adding…' : 'Add'}
          </button>
        </div>
      </div>
    </article>
  )
}
