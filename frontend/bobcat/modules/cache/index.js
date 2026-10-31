import useCache from '../../helpers/composables/useCache'
const {cacheLRU} = useCache()

export default function () {
  this.nuxt.hook('vue-renderer:ssr:prepareContext', (ssrContext) => {
    ssrContext.$cacheLRU = cacheLRU
  })
}
