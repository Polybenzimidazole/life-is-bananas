const AUTH_SERVER = window.location.origin
const CLIENT_ID = 'life-is-bananas-vue'
const REDIRECT_URI = `${window.location.origin}/callback`

function randomString() {
    const bytes = new Uint8Array(32)
    window.crypto.getRandomValues(bytes)

    return Array.from(bytes, byte =>
        byte.toString(16).padStart(2, '0')
    ).join('')
}

function base64UrlEncode(buffer) {
    const bytes = new Uint8Array(buffer)
    let binary = ''

    bytes.forEach(byte => {
        binary += String.fromCharCode(byte)
    })

    return btoa(binary)
        .replace(/\+/g, '-')
        .replace(/\//g, '_')
        .replace(/=+$/, '')
}

async function generateCodeChallenge(verifier) {
    const data = new TextEncoder().encode(verifier)
    const digest = await window.crypto.subtle.digest('SHA-256', data)

    return base64UrlEncode(digest)
}

// Start de OAuth-login met PKCE
export async function login() {
    const codeVerifier = randomString()
    const codeChallenge = await generateCodeChallenge(codeVerifier)
    const state = randomString()

    sessionStorage.setItem('pkce_verifier', codeVerifier)
    sessionStorage.setItem('oauth_state', state)

    const params = new URLSearchParams({
        response_type: 'code',
        client_id: CLIENT_ID,
        redirect_uri: REDIRECT_URI,
        scope: 'recipes.read recipes.write',
        code_challenge: codeChallenge,
        code_challenge_method: 'S256',
        state: state
    })

    window.location.assign(
        `${AUTH_SERVER}/oauth2/authorize?${params.toString()}`
    )
}

// Verwerk de authorization code na de login
export async function handleCallback() {
    const params = new URLSearchParams(window.location.search)

    const error = params.get('error')
    if (error) {
        throw new Error(params.get('error_description') || error)
    }

    const code = params.get('code')
    const state = params.get('state')
    const savedState = sessionStorage.getItem('oauth_state')
    const verifier = sessionStorage.getItem('pkce_verifier')

    if (!code || !state || !savedState || state !== savedState || !verifier) {
        throw new Error('Ongeldige OAuth-callback')
    }

    // Een authorization code mag maar één keer gebruikt worden
    sessionStorage.removeItem('oauth_state')
    sessionStorage.removeItem('pkce_verifier')

    const body = new URLSearchParams({
        grant_type: 'authorization_code',
        client_id: CLIENT_ID,
        redirect_uri: REDIRECT_URI,
        code: code,
        code_verifier: verifier
    })

    const response = await fetch(`${AUTH_SERVER}/oauth2/token`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: body.toString()
    })

    if (!response.ok) {
        throw new Error('Access token aanvragen mislukt')
    }

    const tokens = await response.json()

    if (!tokens.access_token) {
        throw new Error('Geen access token ontvangen')
    }

    // Voorlopig in het geheugen van de applicatie bewaren
    accessToken = tokens.access_token

    return tokens
}

let accessToken = null

export function getAccessToken() {
    return accessToken
}
