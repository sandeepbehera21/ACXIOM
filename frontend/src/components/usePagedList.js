import { useCallback, useEffect, useState } from 'react'
import { extractApiError } from '../services/api'

/**
 * Paged list state shared by the Customers, Leads and Opportunities tables.
 *
 * Search input is debounced, and changing any filter resets to page one in the
 * same state update so only one request is issued per change.
 *
 * `fetcher` must be a stable reference (a module-level service function).
 */
export function usePagedList({ fetcher, size = 10, status = '', stage = '' }) {
  const [query, setQuery] = useState({
    search: '',
    status,
    stage,
    page: 0,
  })
  const [searchInput, setSearchInput] = useState('')
  const [result, setResult] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [reloadToken, setReloadToken] = useState(0)

  // Debounce the search box before it reaches the query.
  useEffect(() => {
    const next = searchInput.trim()
    const handle = setTimeout(() => {
      setQuery((prev) =>
        prev.search === next ? prev : { ...prev, search: next, page: 0 },
      )
    }, 350)
    return () => clearTimeout(handle)
  }, [searchInput])

  const { search, status: statusFilter, stage: stageFilter, page } = query

  useEffect(() => {
    let active = true

    async function load() {
      setLoading(true)
      setError('')
      try {
        const data = await fetcher({
          search,
          status: statusFilter,
          stage: stageFilter,
          page,
          size,
        })
        if (active) setResult(data)
      } catch (err) {
        if (active) {
          setError(extractApiError(err).message)
          setResult(null)
        }
      } finally {
        if (active) setLoading(false)
      }
    }

    load()

    return () => {
      active = false
    }
  }, [fetcher, search, statusFilter, stageFilter, page, size, reloadToken])

  const setStatusFilter = useCallback((value) => {
    setQuery((prev) => ({ ...prev, status: value, page: 0 }))
  }, [])

  const setStageFilter = useCallback((value) => {
    setQuery((prev) => ({ ...prev, stage: value, page: 0 }))
  }, [])

  const setPage = useCallback((next) => {
    setQuery((prev) => ({ ...prev, page: Math.max(0, next) }))
  }, [])

  const reload = useCallback(() => setReloadToken((token) => token + 1), [])

  return {
    items: result?.content || [],
    totalElements: result?.totalElements ?? 0,
    totalPages: result?.totalPages ?? 0,
    page,
    size,
    searchInput,
    setSearchInput,
    status: statusFilter,
    stage: stageFilter,
    setStatusFilter,
    setStageFilter,
    setPage,
    loading,
    error,
    setError,
    reload,
  }
}

export default usePagedList
