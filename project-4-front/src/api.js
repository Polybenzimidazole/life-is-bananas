import axios from 'axios'

export function installAuthRedirect(client, location) {
  let redirecting = false

  function needsSignIn(response, config) {
    if (!response || !config?.url?.startsWith('/api/')) return false
    if (response.status === 401) return true

    // Older servers can redirect API requests to an HTML login page.
    const contentType = response.headers?.['content-type'] || ''
    const finalUrl = response.request?.responseURL
    return contentType.includes('text/html') && finalUrl &&
      new URL(finalUrl, location.href).pathname === '/login'
  }

  function redirectToSignIn(config) {
    if (!redirecting) {
      redirecting = true
      location.assign('/login')
    }
    return Promise.reject(new axios.CanceledError('Redirecting to sign in', config))
  }

  return client.interceptors.response.use(
    response => needsSignIn(response, response.config)
      ? redirectToSignIn(response.config)
      : response,
    error => needsSignIn(error.response, error.config)
      ? redirectToSignIn(error.config)
      : Promise.reject(error)
  )
}
