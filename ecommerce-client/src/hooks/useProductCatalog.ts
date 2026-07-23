import { useCallback, useState } from 'react'
import useSWR from 'swr'
import useSWRInfinite from 'swr/infinite'
import { swrKeys } from '../config/swrKeys'
import { errorMessage } from '../services/httpClient'
import { productService } from '../services/productService'
import type { ProductPage } from '../types'

type ProductCatalogOptions = {
  limit?: number
}

type ProductPageKey = ReturnType<typeof swrKeys.productPage>

const fetchProductPage = ([, , query, category, cursor, limit]: ProductPageKey) =>
  productService.search({ q: query, category, cursor, limit })

export function useProductCatalog({ limit = 20 }: ProductCatalogOptions = {}) {
  const [query, setQueryValue] = useState('')
  const [activeQuery, setActiveQuery] = useState('')
  const [category, setCategoryValue] = useState('')
  const [localError, setError] = useState<string | null>(null)

  const categoriesRequest = useSWR(swrKeys.productCategories, productService.listCategories)

  const getPageKey = useCallback(
    (pageIndex: number, previousPage: ProductPage | null): ProductPageKey | null => {
      if (previousPage && !previousPage.hasMore) return null
      const cursor = pageIndex === 0 ? null : (previousPage?.nextCursor ?? null)
      return swrKeys.productPage(activeQuery, category, cursor, limit)
    },
    [activeQuery, category, limit],
  )

  const catalogRequest = useSWRInfinite<ProductPage>(getPageKey, fetchProductPage)
  const products = catalogRequest.data?.flatMap((page) => page.items) ?? []
  const lastPage = catalogRequest.data?.at(-1)

  const setQuery = useCallback((value: string) => {
    setQueryValue(value)
    setError(null)
  }, [])

  const search = useCallback(() => {
    const normalizedQuery = query.trim()
    setError(null)
    if (normalizedQuery === activeQuery) {
      void catalogRequest.setSize(1).then(() => catalogRequest.mutate())
      return
    }
    setActiveQuery(normalizedQuery)
  }, [activeQuery, catalogRequest, query])

  const setCategory = useCallback((value: string) => {
    setCategoryValue(value)
    setError(null)
  }, [])

  const loadMore = useCallback(() => {
    if (!lastPage?.hasMore || catalogRequest.isValidating) return
    void catalogRequest.setSize((size) => size + 1)
  }, [catalogRequest, lastPage?.hasMore])

  const refresh = useCallback(async () => {
    await catalogRequest.setSize(1)
    await Promise.all([catalogRequest.mutate(), categoriesRequest.mutate()])
  }, [catalogRequest, categoriesRequest])

  const requestError = catalogRequest.error ?? categoriesRequest.error

  return {
    query,
    setQuery,
    category,
    setCategory,
    categories: categoriesRequest.data ?? [],
    products,
    hasMore: lastPage?.hasMore ?? false,
    loading: catalogRequest.isLoading || catalogRequest.isValidating,
    error: localError ?? (requestError ? errorMessage(requestError) : null),
    setError,
    search,
    loadMore,
    refresh,
  }
}
