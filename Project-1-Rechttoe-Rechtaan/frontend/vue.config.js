const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
  publicPath: '/app/',
  devServer: {
    port: 8082,
    proxy: {
      '/api': {
        target: process.env.API_PROXY_TARGET || 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
