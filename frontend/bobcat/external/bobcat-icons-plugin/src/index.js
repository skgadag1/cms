import BobcatIcon from './components/BobcatIcon.vue'

export default {
  install(Vue) {
    Vue.config.ignoredElements = ['b-icon']
    Vue.component('BIcon', BobcatIcon)
  }
}
