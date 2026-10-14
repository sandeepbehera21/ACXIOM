/**
 * Pagination footer for the list tables. Renders nothing while there is a
 * single page and no rows to page through.
 */
export default function Pagination({
  page = 0,
  totalPages = 0,
  totalElements = 0,
  size = 10,
  onPageChange,
}) {
  if (!totalElements) return null

  const first = page * size + 1
  const last = Math.min((page + 1) * size, totalElements)
  const canPrev = page > 0
  const canNext = page + 1 < totalPages

  return (
    <div className="axc-pagination">
      <span>
        Showing {first}–{last} of {totalElements}
      </span>
      <div className="axc-pagination__controls">
        <button
          type="button"
          className="btn btn-sm btn-outline-axc"
          onClick={() => onPageChange(page - 1)}
          disabled={!canPrev}
        >
          Previous
        </button>
        <span>
          Page {totalPages === 0 ? 0 : page + 1} of {totalPages}
        </span>
        <button
          type="button"
          className="btn btn-sm btn-outline-axc"
          onClick={() => onPageChange(page + 1)}
          disabled={!canNext}
        >
          Next
        </button>
      </div>
    </div>
  )
}
