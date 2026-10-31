import '@/vueCompatConfig'
import Vue from 'vue'
import Footer from './Footer.vue'
import store from '@/store'
import { createComponentConfigurationPlugin } from '@/configureComponentApp'
import commonPlugin from '@/plugins/commonPlugin'
import { registerBootstrapVueNext } from '@/plugins/registerBootstrapVueNext'

const app = Vue.createApp(Footer);
registerBootstrapVueNext(app)
app.use(store)
app.use(commonPlugin)
const mountEl = document.querySelector<HTMLElement>('#footer')
if (!mountEl) throw new Error('Mount element #footer was not found')
app.use(createComponentConfigurationPlugin(mountEl))
window.vm2 = app.mount(mountEl)
