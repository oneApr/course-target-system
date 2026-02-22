import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 5173,
    open: true,
    proxy: {
      // 将 /fmk 开头的请求转发到后端，解决跨域问题
      '/fmk': {
        target: 'http://localhost:8088',
        changeOrigin: true
      }
    }
  }
})
