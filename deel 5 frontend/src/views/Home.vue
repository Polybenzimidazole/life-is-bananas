
<template>
  <div class="home">
    <HomePage />

    <!-- Inlogknop voor OAuth 2.0 met PKCE -->
    <button @click="startLogin">
      Inloggen
    </button>
  </div>
</template>

<script>
import HomePage from '@/components/HomePage.vue'
import { login } from '../services/auth'

export default {
  name: 'HomePageView',

  components: {
    HomePage
  },

  methods: {
    async startLogin() {
      try {
        await login()
      } catch (error) {
        console.error('OAuth-login starten mislukt:', error)
      }
    }
  }
}
</script>
