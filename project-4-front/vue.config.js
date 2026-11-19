module.exports = {
  lintOnSave: false,
  devServer: {
    port: 8888,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        pathRewrite: { '^/api': '' }
      },
      '/login': { target: 'http://localhost:8080' },
      '/logout': { target: 'http://localhost:8080' },
      '/default-ui.css': { target: 'http://localhost:8080' }
    }
  }
}
