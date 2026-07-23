import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import Vue from '@vitejs/plugin-vue'
import { createSvgIconsPlugin } from 'vite-plugin-svg-icons'
import Vuetify, { transformAssetUrls } from 'vite-plugin-vuetify'


// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    Vue({
      template: { transformAssetUrls },
    }),
    // https://github.com/vuetifyjs/vuetify-loader/tree/master/packages/vite-plugin#readme
    Vuetify({ autoImport: true }),
    createSvgIconsPlugin({
      iconDirs: [fileURLToPath(new URL('./src/assets/icons', import.meta.url))],
      symbolId: 'icon-[name]'
    }),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('src', import.meta.url)),
    }
  },
  server: {
    open: true,
    port: 8888,
    proxy: {
      '/api': {
        target: 'http://localhost:8899',
        changeOrigin: true,
        ws: true,
        rewrite: (path) => path.replace(/^\/api/, 'api')
      }
    }
  },
})
