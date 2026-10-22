// Relatieve URLs werken met de ontwikkelproxy en met Spring Boot op dezelfde host.
export async function api(path, options = {}) {
  let response
  try {
    response = await fetch(`/api${path}`, {
      credentials: 'same-origin',
      ...options,
      headers: { 'Content-Type': 'application/json', ...options.headers }
    })
  } catch {
    throw new Error('De server is niet bereikbaar. Controleer of de backend draait.')
  }
  if (response.status === 204) return null
  const data = await response.json().catch(() => ({}))
  if (!response.ok) {
    const error = new Error(data.message || `Het verzoek is mislukt (${response.status}).`)
    error.status = response.status
    throw error
  }
  return data
}
