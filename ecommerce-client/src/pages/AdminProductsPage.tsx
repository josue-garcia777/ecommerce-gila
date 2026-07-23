import { useState } from 'react'
import { errorMessage } from '../services/httpClient'
import { productService } from '../services/productService'
import { CatalogFilters } from '../components/CatalogFilters'
import { Message } from '../components/Message'
import { MoneyText } from '../components/MoneyText'
import { ProductForm } from '../components/product/ProductForm'
import { useProductCatalog } from '../hooks/useProductCatalog'
import type { Product } from '../types'

export default function AdminProductsPage() {
  const catalog = useProductCatalog({ limit: 50 })
  const [editing, setEditing] = useState<Product | null | undefined>(undefined)
  const [success, setSuccess] = useState<string | null>(null)

  const saved = async (message: string) => {
    setEditing(undefined)
    setSuccess(message)
    await catalog.refresh()
  }

  const remove = async (product: Product) => {
    try {
      catalog.setError(null)
      await productService.remove(product.id)
      setSuccess(`${product.name} was deleted`)
      await catalog.refresh()
    } catch (caught) {
      catalog.setError(errorMessage(caught))
    }
  }

  return (
    <section className="content-section">
      <div className="section-heading">
        <div>
          <p className="eyebrow">Catalog operations</p>
          <h1>Product administration</h1>
        </div>
        <button className="primary" onClick={() => setEditing(null)}>
          Create product
        </button>
      </div>

      <CatalogFilters
        className="mb-[22px] max-w-[760px]"
        query={catalog.query}
        category={catalog.category}
        categories={catalog.categories}
        onQueryChange={catalog.setQuery}
        onCategoryChange={catalog.setCategory}
        onSubmit={catalog.search}
      />

      {catalog.error && <Message tone="error">{catalog.error}</Message>}
      {success && <Message tone="success">{success}</Message>}
      {catalog.loading && catalog.products.length === 0 && (
        <p className="muted">Loading products…</p>
      )}
      {!catalog.loading && catalog.products.length === 0 && (
        <div className="empty-state">
          <h3>No active products</h3>
          <p>Create one or import the supplied CSV.</p>
        </div>
      )}

      <div className="table-wrap admin-table">
        <table>
          <thead>
            <tr>
              <th>Product</th>
              <th>Category</th>
              <th>Price</th>
              <th>Stock</th>
              <th>Version</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {catalog.products.map((product) => (
              <tr key={product.id}>
                <td>
                  <strong>{product.name}</strong>
                  <small>{product.sku}</small>
                </td>
                <td>{product.category}</td>
                <td>
                  <MoneyText money={product.price} />
                </td>
                <td>{product.stock}</td>
                <td>{product.version}</td>
                <td className="row-actions">
                  <button onClick={() => setEditing(product)}>Edit</button>
                  <button className="danger" onClick={() => void remove(product)}>
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {catalog.hasMore && (
        <button
          className="secondary load-table"
          disabled={catalog.loading}
          onClick={catalog.loadMore}
        >
          Load more
        </button>
      )}

      {editing !== undefined && (
        <ProductForm product={editing} onCancel={() => setEditing(undefined)} onSaved={saved} />
      )}
    </section>
  )
}
