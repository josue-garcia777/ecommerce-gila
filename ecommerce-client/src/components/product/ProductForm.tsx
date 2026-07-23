import { useState } from 'react'
import type { ChangeEvent, FormEvent } from 'react'
import { errorMessage } from '../../services/httpClient'
import { productService } from '../../services/productService'
import type { Product, ProductPayload } from '../../types'
import { Message } from '../Message'

type FormValues = {
  sku: string
  name: string
  description: string
  category: string
  amount: string
  currency: string
  stock: string
  weightKg: string
  imageUrl: string
}

const emptyForm: FormValues = {
  sku: '',
  name: '',
  description: '',
  category: '',
  amount: '',
  currency: 'USD',
  stock: '0',
  weightKg: '',
  imageUrl: '',
}

export function ProductForm({
  product,
  onCancel,
  onSaved,
}: {
  product: Product | null
  onCancel: () => void
  onSaved: (message: string) => Promise<void>
}) {
  const [values, setValues] = useState<FormValues>(
    product
      ? {
          sku: product.sku,
          name: product.name,
          description: product.description,
          category: product.category,
          amount: String(product.price.amount),
          currency: product.price.currency,
          stock: String(product.stock),
          weightKg: String(product.weightKg),
          imageUrl: product.imageUrl ?? '',
        }
      : emptyForm,
  )
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const field = (name: keyof FormValues) => ({
    value: values[name],
    onChange: (event: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) =>
      setValues((current) => ({ ...current, [name]: event.target.value })),
  })

  const submit = async (event: FormEvent) => {
    event.preventDefault()
    const payload: ProductPayload = {
      ...(product ? {} : { sku: values.sku }),
      name: values.name,
      description: values.description,
      category: values.category,
      price: { amount: Number(values.amount), currency: values.currency },
      stock: Number(values.stock),
      weightKg: Number(values.weightKg),
      imageUrl: values.imageUrl.trim() || null,
      ...(product ? { version: product.version } : {}),
    }
    try {
      setSaving(true)
      setError(null)
      if (product) {
        await productService.update(product.id, payload)
        await onSaved(`${values.name} was updated`)
      } else {
        await productService.create(payload)
        await onSaved(`${values.name} was created`)
      }
    } catch (caught) {
      setError(errorMessage(caught))
    } finally {
      setSaving(false)
    }
  }

  return (
    <div
      className="modal-backdrop"
      role="presentation"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) onCancel()
      }}
    >
      <form className="product-form" onSubmit={submit}>
        <div className="form-title">
          <div>
            <p className="eyebrow">{product ? product.sku : 'New catalog item'}</p>
            <h2>{product ? 'Edit product' : 'Create product'}</h2>
          </div>
          <button type="button" className="icon-button" onClick={onCancel} aria-label="Close">
            ×
          </button>
        </div>
        {error && <Message tone="error">{error}</Message>}
        <div className="form-grid">
          <label>
            <span>SKU</span>
            <input {...field('sku')} required maxLength={64} disabled={Boolean(product)} />
          </label>
          <label>
            <span>Name</span>
            <input {...field('name')} required maxLength={200} />
          </label>
          <label className="full">
            <span>Description</span>
            <textarea {...field('description')} required maxLength={2000} rows={3} />
          </label>
          <label>
            <span>Category</span>
            <input {...field('category')} required maxLength={100} />
          </label>
          <label>
            <span>
              Image URL <small>optional</small>
            </span>
            <input {...field('imageUrl')} maxLength={2048} />
          </label>
          <label>
            <span>Price</span>
            <input {...field('amount')} required type="number" min="0" step="0.01" />
          </label>
          <label>
            <span>Currency</span>
            <input {...field('currency')} required minLength={3} maxLength={3} />
          </label>
          <label>
            <span>Stock</span>
            <input {...field('stock')} required type="number" min="0" step="1" />
          </label>
          <label>
            <span>Weight kg</span>
            <input {...field('weightKg')} required type="number" min="0" step="0.001" />
          </label>
        </div>
        <div className="form-actions">
          <button type="button" className="secondary" onClick={onCancel}>
            Cancel
          </button>
          <button className="primary" disabled={saving}>
            {saving ? 'Saving…' : 'Save product'}
          </button>
        </div>
      </form>
    </div>
  )
}
