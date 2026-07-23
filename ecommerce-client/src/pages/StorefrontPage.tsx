import { useState } from 'react'
import { errorMessage } from '../services/httpClient'
import { CatalogFilters } from '../components/CatalogFilters'
import { Message } from '../components/Message'
import { ProductCard } from '../components/ProductCard'
import { useCart } from '../context/CartContext'
import { useProductCatalog } from '../hooks/useProductCatalog'

export default function StorefrontPage() {
  const { addItem } = useCart()
  const catalog = useProductCatalog()
  const [adding, setAdding] = useState<string | null>(null)
  const [notice, setNotice] = useState<string | null>(null)

  const add = async (productId: string) => {
    try {
      setAdding(productId)
      setNotice(null)
      await addItem(productId)
      setNotice('Added to cart')
    } catch (caught) {
      catalog.setError(errorMessage(caught))
    } finally {
      setAdding(null)
    }
  }

  return (
    <section className="mx-[clamp(20px,5vw,72px)] my-14 max-[500px]:mx-3.5">
      {notice && (
        <button className="toast" onClick={() => setNotice(null)} aria-label="Dismiss notification">
          {notice}
        </button>
      )}
      <div className="mb-7 flex items-end justify-between gap-8 max-[1050px]:flex-col max-[1050px]:items-start">
        <div>
          <h1 className="m-0 font-display text-[clamp(34px,4vw,52px)] leading-[1.05] font-bold tracking-[-1px]">
            Browse the catalog
          </h1>
        </div>
        <CatalogFilters
          className="max-[1050px]:w-full"
          query={catalog.query}
          category={catalog.category}
          categories={catalog.categories}
          onQueryChange={catalog.setQuery}
          onCategoryChange={catalog.setCategory}
          onSubmit={catalog.search}
        />
      </div>

      {catalog.error && <Message tone="error">{catalog.error}</Message>}
      {catalog.loading && catalog.products.length === 0 && (
        <div className="grid grid-cols-3 gap-[18px] max-[760px]:grid-cols-2 max-[500px]:grid-cols-1">
          <span className="skeleton-card" />
          <span className="skeleton-card" />
          <span className="skeleton-card" />
        </div>
      )}
      {!catalog.loading && catalog.products.length === 0 && (
        <div className="empty-state">
          <h3>No products found</h3>
          <p>Change the product name or category and try again.</p>
        </div>
      )}

      <div className="grid grid-cols-4 gap-[18px] max-[1050px]:grid-cols-3 max-[760px]:grid-cols-2 max-[500px]:grid-cols-1">
        {catalog.products.map((product) => (
          <ProductCard
            key={product.id}
            product={product}
            adding={adding === product.id}
            onAdd={(productId) => void add(productId)}
          />
        ))}
      </div>

      {catalog.hasMore && (
        <div className="py-8 pb-[70px] text-center">
          <button className="secondary" disabled={catalog.loading} onClick={catalog.loadMore}>
            {catalog.loading ? 'Loading…' : 'Load more products'}
          </button>
        </div>
      )}
    </section>
  )
}
