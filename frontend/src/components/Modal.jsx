import { useEffect } from 'react'

/**
 * Bootstrap-styled modal built from plain markup, so the app never needs
 * Bootstrap's JavaScript bundle. Escape and the backdrop close it.
 */
export default function Modal({ title, onClose, children, footer, size }) {
  useEffect(() => {
    const onKeyDown = (event) => {
      if (event.key === 'Escape') onClose()
    }
    document.addEventListener('keydown', onKeyDown)
    return () => document.removeEventListener('keydown', onKeyDown)
  }, [onClose])

  return (
    <>
      <div
        className="modal fade show axc-modal"
        style={{ display: 'block' }}
        role="dialog"
        aria-modal="true"
        aria-label={title}
      >
        <div
          className={`modal-dialog modal-dialog-centered${
            size ? ` modal-${size}` : ''
          }`}
        >
          <div className="modal-content">
            <div className="modal-header">
              <h5 className="modal-title">{title}</h5>
              <button
                type="button"
                className="btn-close"
                aria-label="Close"
                onClick={onClose}
              />
            </div>
            <div className="modal-body">{children}</div>
            {footer && <div className="modal-footer">{footer}</div>}
          </div>
        </div>
      </div>
      <div className="modal-backdrop fade show" onClick={onClose} />
    </>
  )
}
