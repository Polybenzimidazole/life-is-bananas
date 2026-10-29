
<template>
  <div class="callback">
    <h2>OAuth 2.0 authenticatie</h2>

    <p v-if="loading">
      Je aanmelding wordt verwerkt...
    </p>

    <p v-else-if="error" class="error">
      {{ error }}
    </p>
  </div>
</template>

<script>
import { handleCallback } from '../services/auth'

export default {
  name: 'CallbackView',

  data() {
    return {
      loading: true,
      error: null
    }
  },

  async mounted() {


    try {
      const tokens = await handleCallback()

      console.log(
          'OAuth-login succesvol:',
          !!tokens.access_token
      )

      console.log(
          'Token type:',
          tokens.token_type
      )

      // Terug naar de homepage na succesvolle login
      this.$router.replace('/')

    } catch (err) {
      console.error('OAuth-callback mislukt:', err)
      this.error = err.message

    } finally {
      this.loading = false
    }
  }
}
</script>

<style scoped>
.callback {
  text-align: center;
  margin-top: 60px;
}

.error {
  color: red;
}
</style>
