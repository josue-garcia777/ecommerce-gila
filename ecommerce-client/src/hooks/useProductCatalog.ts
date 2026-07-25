import { useEffect, useState } from 'react'
import { errorMessage } from '../services/httpClient'
import { productService } from '../services/productService'
import type { Product } from '../types'

type ProductOptions = {
  limit?: number
}

export const useProductList = ({ limit = 20 }: ProductOptions = {}) => {
  
  const [query, setQuery] = useState('')
  const [activeQuery, setActiveQuery] = useState('')
  
  const [category, setCategory] = useState('')
  const [categories, setCategories] = useState<string[]>([])
  
  const [products, setProducts] = useState<Product[]>([])
  
  const [nextCursor, setNextCursor] = useState<string | null>(null)
  
  const [hasMore, setHasMore] = useState(false)
  
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const loadFirstPage = async (nextQuery: string, nextCategory: string): Promise<void> => {
    setLoading(true)

    try {
      const [page, availableCategories] = await Promise.all([
        productService.searchProduct({ q: nextQuery, category: nextCategory, limit }),
        productService.listCategories(),
      ])

      setProducts(page.items)
      setNextCursor(page.nextCursor)
      setHasMore(page.hasMore)
      setCategories(availableCategories)
      setError(null)
    } catch (caught) {
      setError(errorMessage(caught))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadFirstPage(activeQuery, category)
  }, [activeQuery, category, limit])

  const updateQuery = (value: string): void => {
    setQuery(value)
    setError(null)
  }

  const updateCategory = (value: string): void => {
    setCategory(value)
    setError(null)
  }

  const search = async (): Promise<void> => {
    const nextQuery = query.trim()
    setError(null)

    if (nextQuery === activeQuery) {
      await loadFirstPage(nextQuery, category)
      return
    }

    setActiveQuery(nextQuery)
  }

  const loadMore = async (): Promise<void> => {
    if (loading || !hasMore || !nextCursor) {
      return
    }

    setLoading(true)

    try {
      const page = await productService.searchProduct({
        q: activeQuery,
        category,
        cursor: nextCursor,
        limit,
      })

      setProducts((current) => [...current, ...page.items])
      setNextCursor(page.nextCursor)
      setHasMore(page.hasMore)
      setError(null)
    } catch (error) {
      setError(errorMessage(error))
    } finally {
      setLoading(false)
    }
  }

  const refresh = async (): Promise<void> => {
    await loadFirstPage(activeQuery, category)
  }

  return {
    query,
    setQuery: updateQuery,
    category,
    setCategory: updateCategory,
    categories,
    products,
    hasMore,
    loading,
    error,
    setError,
    search,
    loadMore,
    refresh,
  }
}
