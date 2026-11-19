<template>
  <div id="app">
    <nav>
      <router-link to="/">Home</router-link> |
      <router-link to="/recipes">Recipes</router-link> |
      <router-link to="/production">Production Management</router-link> |
      <router-link to="/stock">Stock</router-link> |
      <router-link to="/assistant">AI-assistent</router-link> |
      <router-link to="/about">About Us</router-link> |
      <button v-if="signedIn" @click="logout">Sign out</button>
      <button v-else @click="login">Sign in</button>
    </nav>
    <router-view/>
  </div>
</template>

<script>
import { getAccessToken, login, logout } from './services/auth'
export default {
  name: 'App',
  computed: { signedIn: () => !!getAccessToken() },
  methods: { login, logout }
}
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}
</style>
