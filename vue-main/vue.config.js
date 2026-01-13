// vue.config.js
module.exports = {
    devServer: {
        port: 8081, // Vue 개발 서버 포트
        proxy: {
            '/api': {
                target: 'http://localhost:8083', // Spring Boot 서버 주소
                changeOrigin: true
            }
        }
    }
}