import axios from 'axios'

/**
 * Base URL of the AcxiomCRM Spring Boot API.
 */
export const API_BASE_URL = 'http://localhost:8080'

/**
 * Handler invoked when the API answers with 401. AuthProvider registers one so
 * the router can drop the session without a full page reload; when no handler
 * is registered (e.g. very early in the app's life) we fall back to a hard
 * redirect to /login.
 */
let unauthorizedHandler = null

export function setUnauthorizedHandler(handler) {
  unauthorizedHandler = handler
}

function handleUnauthorized() {
  if (typeof unauthorizedHandler === 'function') {
    unauthorizedHandler()
    return
  }
  if (typeof window !== 'undefined' && window.location.pathname !== '/login') {
    window.location.assign('/login')
  }
}

/**
 * Shared axios instance.
 *
 * - `withCredentials` sends the ACXIOMSESSION cookie on every call.
 * - `xsrfCookieName` / `xsrfHeaderName` implement Spring Security's
 *   double-submit CSRF protection: the readable XSRF-TOKEN cookie is echoed
 *   back in the X-XSRF-TOKEN header on every non-GET request.
 */
const api = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
  xsrfCookieName: 'XSRF-TOKEN',
  xsrfHeaderName: 'X-XSRF-TOKEN',
  headers: { Accept: 'application/json' },
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error?.response?.status
    const url = error?.config?.url || ''
    const isLoginRequest = url.includes('/api/auth/login')
    if (status === 401 && !isLoginRequest) {
      handleUnauthorized()
    }
    return Promise.reject(error)
  },
)

/**
 * Normalises any thrown value into the error envelope the API promises:
 * { timestamp, status, error, message, fieldErrors, path }
 */
export function extractApiError(error) {
  const response = error?.response
  if (!response) {
    return {
      status: 0,
      message:
        'Unable to reach the server. Check that the API is running and try again.',
      fieldErrors: {},
    }
  }
  const data = response.data
  if (!data || typeof data !== 'object') {
    return {
      status: response.status,
      message: `Request failed with status ${response.status}.`,
      fieldErrors: {},
    }
  }
  return {
    status: data.status ?? response.status,
    message:
      data.message ||
      data.error ||
      `Request failed with status ${response.status}.`,
    fieldErrors: data.fieldErrors || {},
  }
}

export default api
