import Vue from 'vue'
import App from './App.vue'
import router from './router'
import axios from 'axios'
import { installAuthRedirect } from './api'

Vue.config.productionTip = false
installAuthRedirect(axios, window.location)

new Vue({
  router,
  render: h => h(App),
}).$mount('#app')
