import '@/vueCompatConfig'
import Vue from 'vue'
import Feedback from './Feedback.vue'
import store from '@/store'
import { createComponentConfigurationPlugin } from '@/configureComponentApp'
import commonPlugin from '@/plugins/commonPlugin'
import { registerBootstrapVueNext } from '@/plugins/registerBootstrapVueNext'

const mountEl = document.querySelector<HTMLElement>('#feedback');
if(mountEl) {
  const app = Vue.createApp(Feedback)
  registerBootstrapVueNext(app)
  app.use(store)
  app.use(commonPlugin)
  app.use(createComponentConfigurationPlugin(mountEl))
  window.vm3 = app.mount(mountEl)
}
