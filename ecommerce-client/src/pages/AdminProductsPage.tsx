import { useState } from 'react'
import { errorMessage } from '../services/httpClient'
import { productService } from '../services/productService'
import { CatalogFilters } from '../components/CatalogFilters'
import { Message } from '../components/Message'
import { MoneyText } from '../components/MoneyText'
import { ProductForm } from '../components/product/ProductForm'
import { useProductList } from '../hooks/useProductCatalog'
import type { Product } from '../types'

const AdminProductsPage = () => {
  const productList = useProductList({ limit: 50 })
  const [editing, setEditing] = useState<Product | null | undefined>(undefined)
  const [success, setSuccess] = useState<string | null>(null)

  const saved = async (message: string) => {
    setEditing(undefined)
    setSuccess(message)
    await productList.refresh()
  }

  const remove = async (product: Product) => {
    try {
        
        productList.setError(null)
        await productService.removeProduct(product.id)
        setSuccess(`${product.name} was deleted`)
        
        await productList.refresh()
    } catch (caught) {
        productList.setError(errorMessage(caught))
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
        query={productList.query}
        category={productList.category}
        categories={productList.categories}
        onQueryChange={productList.setQuery}
        onCategoryChange={productList.setCategory}
        onSubmit={productList.search}
      />

      {productList.error && <Message tone="error">{productList.error}</Message>}
      {success && <Message tone="success">{success}</Message>}
      {productList.loading && productList.products.length === 0 && (
        <p className="muted">Loading products…</p>
      )}
      {!productList.loading && productList.products.length === 0 && (
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
            {productList.products.map((product) => (
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
      {productList.hasMore && (
        <button
          className="secondary load-table"
          disabled={productList.loading}
          onClick={productList.loadMore}
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

export default AdminProductsPage
