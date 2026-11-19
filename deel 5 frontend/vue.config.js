
module.exports = {
  lintOnSave: false,

  devServer: {
    port: 8085,
    proxy: {
      '/api': { target: 'http://localhost:8084', pathRewrite: { '^/api': '' } },
      '/oauth2': { target: 'http://localhost:8084' },
      '/.well-known': { target: 'http://localhost:8084' },
      '/login': { target: 'http://localhost:8084' },
      '/logout': { target: 'http://localhost:8084' },
      '/default-ui.css': { target: 'http://localhost:8084' }
    }
  }
}
