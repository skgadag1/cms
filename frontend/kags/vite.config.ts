import path from 'path'
import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'
import basicSsl from '@vitejs/plugin-basic-ssl'
import autoprefixer from 'autoprefixer'
import pagesMpaPlugin from './build/vite-plugin-pages-mpa'
import iconFontPlugin from './build/vite-plugin-icon-font'
import linksScriptPlugin from './build/vite-plugin-links-script'

const VENDOR_CHUNK = 'chunk-vendors'
const MPA_PAGES_DIR = '.kags-vite-mpa-pages'

export default defineConfig(({mode}) => ({
  resolve: {
    extensions: ['.mjs', '.js', '.mts', '.ts', '.jsx', '.tsx', '.vue', '.json'],
    alias: {
      vue: '@vue/compat',
      '@': path.resolve(__dirname, 'src')
    }
  },
 envPrefix: ['VITE_', 'VUE_APP_'],
  css: {
    // Browser targets come from .browserslistrc.
    postcss: {plugins: [autoprefixer()]}
  },
  plugins: [
    vue({
      template: {
        compilerOptions: {
          // Vue 2 behavior by default (filters, custom-directive hook names, etc.), opt out
          // per-feature as warnings get triaged.
          compatConfig: {MODE: 2}
        }
      }
    }),
    iconFontPlugin(),
    linksScriptPlugin(),
    pagesMpaPlugin(),
    basicSsl()
  ],
  build: {
    outDir: mode === 'preview' ? 'dist-preview' : 'dist',
    cssCodeSplit: false,
    rollupOptions: {
      output: {
        entryFileNames: 'assets/js/[name].js',
        chunkFileNames: chunk => chunk.name === VENDOR_CHUNK ? 'assets/js/[name].js' : 'assets/js/[name]-[hash].js',
        assetFileNames: asset => asset.names.some(n => n.endsWith('.css'))
          ? 'assets/css/chunk-common.css'
          : 'assets/[name]-[hash][extname]',
        manualChunks(id) {
          if (id.includes(MPA_PAGES_DIR)) return undefined
          if (id.includes('node_modules') || id.startsWith('\0commonjsHelpers')) return VENDOR_CHUNK
          return undefined
        }
      }
    }
  }
}))
