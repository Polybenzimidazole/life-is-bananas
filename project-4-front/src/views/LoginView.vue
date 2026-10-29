
<template>
  <div>
    <h2>Login - Life Is Bananas</h2>

    <form @submit.prevent="login">
      <input
          v-model="username"
          placeholder="Username"
          required
      />

      <input
          v-model="password"
          type="password"
          placeholder="Password"
          required
      />

      <button type="submit" :disabled="loading">
        {{ loading ? 'Inloggen...' : 'Login' }}
      </button>
    </form>

    <p v-if="error" style="color: red">
      {{ error }}
    </p>
  </div>
</template>

<script>
export default {
  name: 'LoginView',

  data() {
    return {
      username: '',
      password: '',
      error: '',
      loading: false
    }
  },

  methods: {
    async login() {
      this.error = ''
      this.loading = true

      try {
        // 1. CSRF-token ophalen
        const csrfResponse = await fetch(
            'http://localhost:8080/recipes/csrf',
            {
              credentials: 'include',
              headers: {
                Accept: 'application/json'
              }
            }
        )

        if (!csrfResponse.ok) {
          throw new Error(
              'CSRF-aanvraag mislukt: HTTP ' + csrfResponse.status
          )
        }

        const contentType =
            csrfResponse.headers.get('content-type') || ''

        if (!contentType.includes('application/json')) {
          throw new Error(
              'Spring Boot stuurt HTML terug in plaats van een CSRF-token. Controleer SecurityConfig.'
          )
        }

        const csrf = await csrfResponse.json()

        if (!csrf.token || !csrf.headerName) {
          throw new Error('Ongeldig CSRF-token ontvangen')
        }

        // 2. Inloggegevens voorbereiden
        const formData = new URLSearchParams()
        formData.append('username', this.username)
        formData.append('password', this.password)

        // 3. Inloggen via Spring Security
        const response = await fetch(
            'http://localhost:8080/login',
            {
              method: 'POST',
              credentials: 'include',
              redirect: 'manual',
              headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                [csrf.headerName]: csrf.token
              },
              body: formData
            }
        )

        if (!response.ok) {
          throw new Error(
              'Login geweigerd. HTTP-status: ' + response.status
          )
        }

        // 4. Controleer of de sessie geldig is
        const checkResponse = await fetch(
            'http://localhost:8080/recipes/whoami',
            {
              credentials: 'include',
              headers: {
                Accept: 'application/json'
              }
            }
        )

        if (!checkResponse.ok) {
          throw new Error('Login niet bevestigd door Spring Boot')
        }

        const contentTypeCheck =
            checkResponse.headers.get('content-type') || ''

        if (!contentTypeCheck.includes('application/json')) {
          throw new Error('Geen geldige gebruikersgegevens ontvangen')
        }

        const user = await checkResponse.json()

        if (!user || !user.username) {
          throw new Error('Gebruiker is niet aangemeld')
        }

        // 5. Naar de oorspronkelijk gevraagde pagina
        const redirect = this.$route.query.redirect

        const destination =
            typeof redirect === 'string' &&
            redirect.startsWith('/') &&
            !redirect.startsWith('//')
                ? redirect
                : '/recipes'

        await this.$router.push(destination)

      } catch (error) {
        console.error('Login error:', error)
        this.error = 'Inloggen mislukt: ' + error.message
      } finally {
        this.loading = false
      }
    }
  }
}
</script>
