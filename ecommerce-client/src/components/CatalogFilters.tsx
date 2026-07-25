import type { FormEvent } from 'react'

type CatalogFiltersProps = {
  query: string
  category: string
  categories: string[]
  onQueryChange: (value: string) => void
  onCategoryChange: (value: string) => void
  onSubmit: () => void
  className?: string
}

export const CatalogFilters = ({
  query,
  category,
  categories,
  onQueryChange,
  onCategoryChange,
  onSubmit,
  className = '',
}: CatalogFiltersProps) => {
  const submit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    onSubmit()
  }

  return (
    <form
      className={`grid grid-cols-[minmax(230px,1fr)_minmax(170px,.65fr)_auto] items-end gap-2.5 max-[760px]:grid-cols-1 ${className}`}
      onSubmit={submit}
    >
      <label>
        <span>Product name</span>
        <input
          value={query}
          onChange={(event) => onQueryChange(event.target.value)}
          placeholder="Search product names"
        />
      </label>
      <label>
        <span>Category</span>
        <select value={category} onChange={(event) => onCategoryChange(event.target.value)}>
          <option value="">All categories</option>
          {categories.map((value) => (
            <option key={value} value={value}>
              {value}
            </option>
          ))}
        </select>
      </label>
      <button className="primary" type="submit">
        Search
      </button>
    </form>
  )
}
