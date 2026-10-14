import { SearchIcon } from './Icons'

/** Search box with a leading icon and a clear button. */
export default function SearchInput({
  value,
  onChange,
  placeholder = 'Search…',
  id = 'search',
}) {
  return (
    <div className="axc-toolbar__search">
      <span className="axc-search-icon">
        <SearchIcon />
      </span>
      <input
        id={id}
        type="search"
        className="form-control axc-search-input"
        placeholder={placeholder}
        value={value}
        onChange={(event) => onChange(event.target.value)}
        aria-label={placeholder}
      />
      {value && (
        <button
          type="button"
          className="axc-search-clear"
          aria-label="Clear search"
          onClick={() => onChange('')}
        >
          &times;
        </button>
      )}
    </div>
  )
}
