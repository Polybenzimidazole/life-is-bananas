import assert from 'node:assert/strict'
import test from 'node:test'
import axios from 'axios'
import { installAuthRedirect } from '../src/api.js'

function setup({ status = 200, data = [], headers = {}, responseURL, error } = {}) {
  const redirects = []
  const client = axios.create({
    adapter: async config => {
      if (error) throw error
      const response = { status, data, headers, config, request: { responseURL } }
      if (status >= 400) {
        throw new axios.AxiosError('Request failed', undefined, config, response.request, response)
      }
      return response
    }
  })
  installAuthRedirect(client, {
    href: 'http://127.0.0.1:8081/recipes',
    assign: path => redirects.push(path)
  })
  return { client, redirects }
}

test('concurrent unauthenticated requests redirect once and cancel generic errors', async () => {
  const { client, redirects } = setup({ status: 401 })
  const results = await Promise.allSettled([
    client.get('/api/recipes/all'),
    client.get('/api/stocks/all')
  ])
  assert.deepEqual(redirects, ['/login'])
  assert.ok(results.every(result => result.status === 'rejected' && axios.isCancel(result.reason)))
})

test('login HTML reached through an old API redirect also requires sign in', async () => {
  const { client, redirects } = setup({
    data: '<html>login</html>',
    headers: { 'content-type': 'text/html;charset=UTF-8' },
    responseURL: 'http://127.0.0.1:8081/login'
  })
  await assert.rejects(client.get('/api/recipes/all'), axios.isCancel)
  assert.deepEqual(redirects, ['/login'])
})

test('successful API JSON remains available to components', async () => {
  const { client, redirects } = setup({ data: [{ id: 1 }] })
  assert.deepEqual((await client.get('/api/recipes/all')).data, [{ id: 1 }])
  assert.deepEqual(redirects, [])
})

test('permission failures remain errors without a login loop', async () => {
  const { client, redirects } = setup({ status: 403 })
  await assert.rejects(client.post('/api/recipes/add', {}), error => error.response.status === 403)
  assert.deepEqual(redirects, [])
})

test('network failures and non-API unauthorized requests do not redirect', async () => {
  const networkError = new axios.AxiosError('Network Error', 'ERR_NETWORK')
  const network = setup({ error: networkError })
  await assert.rejects(network.client.get('/api/stocks/all'), error => error === networkError)
  assert.deepEqual(network.redirects, [])

  const unrelated = setup({ status: 401 })
  await assert.rejects(unrelated.client.get('/other-service'), error => error.response.status === 401)
  assert.deepEqual(unrelated.redirects, [])
})
