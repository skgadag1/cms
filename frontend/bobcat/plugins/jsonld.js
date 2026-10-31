import Vue from 'vue'
import NuxtJsonld from 'nuxt-jsonld'

if (Vue.__jsonld_plugin__ !== true) {
  Vue.use(NuxtJsonld)
  Vue.__jsonld_plugin__ = true
}
