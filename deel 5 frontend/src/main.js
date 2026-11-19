import Vue from 'vue'
import App from './App.vue'
import router from './router'
import axios from 'axios'
import { clearAccessToken, login } from './services/auth'

let signingIn = false
axios.interceptors.response.use(response => response, error => {
  if (error.response?.status === 401 && error.config?.url?.startsWith('/api/') && !signingIn) {
    signingIn = true
    clearAccessToken()
    sessionStorage.setItem('auth_return_path', window.location.pathname)
    login().catch(() => { signingIn = false })
  }
  return Promise.reject(error)
})

Vue.config.productionTip = false

new Vue({
  router,
  render: h => h(App),
}).$mount('#app')
