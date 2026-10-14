import axios from 'axios'

/**
 * Base URL of the AcxiomCRM Spring Boot API.
 */
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || ''

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

let cachedCsrfToken = null

export async function fetchCsrfToken() {
  try {
    const res = await axios.get(`${API_BASE_URL}/api/auth/csrf`, {
      withCredentials: true,
      headers: { Accept: 'application/json' },
    })
    if (res.data?.token) {
      cachedCsrfToken = res.data.token
    }
  } catch {
    // Ignore error; the mutating call will proceed and fail with proper API error if token missing
  }
  return cachedCsrfToken
}

export function resetCsrfToken() {
  cachedCsrfToken = null
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

api.interceptors.request.use(async (config) => {
  const method = (config.method || 'get').toUpperCase()
  if (['POST', 'PUT', 'PATCH', 'DELETE'].includes(method)) {
    let token = null
    if (typeof document !== 'undefined') {
      const match = document.cookie.match(/XSRF-TOKEN=([^;]+)/)
      if (match) {
        token = decodeURIComponent(match[1])
      }
    }
    if (!token) {
      if (!cachedCsrfToken) {
        await fetchCsrfToken()
      }
      token = cachedCsrfToken
    }
    if (token) {
      if (config.headers && typeof config.headers.set === 'function') {
        config.headers.set('X-XSRF-TOKEN', token)
      } else {
        config.headers = config.headers || {}
        config.headers['X-XSRF-TOKEN'] = token
      }
    }
  }
  return config
})

api.interceptors.response.use(
  (response) => {
    if (typeof document !== 'undefined') {
      const match = document.cookie.match(/XSRF-TOKEN=([^;]+)/)
      if (match) {
        cachedCsrfToken = decodeURIComponent(match[1])
      }
    }
    return response
  },
  (error) => {
    const status = error?.response?.status
    const url = error?.config?.url || ''
    const isLoginRequest = url.includes('/api/auth/login')
    if (status === 401 && !isLoginRequest) {
      handleUnauthorized()
    }
    if (status === 403) {
      resetCsrfToken()
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
