import Vue from 'vue'
import LazyHydrate from 'vue-lazy-hydration'

if (Vue.__lazy_hydrate_plugin__ !== true) {
  Vue.__lazy_hydrate_plugin__ = true
  Vue.component('LazyHydrate', LazyHydrate)
}
