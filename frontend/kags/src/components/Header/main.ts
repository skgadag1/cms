import '@/vueCompatConfig'
import Vue from 'vue'
import Header from './Header.vue'
import store from '@/store'
import { createComponentConfigurationPlugin } from '@/configureComponentApp'
import commonPlugin from '@/plugins/commonPlugin'
import { registerBootstrapVueNext } from '@/plugins/registerBootstrapVueNext'

const app = Vue.createApp(Header);
registerBootstrapVueNext(app)
app.use(store)
app.use(commonPlugin)
const mountEl = document.querySelector<HTMLElement>('#header')
if (!mountEl) throw new Error('Mount element #header was not found')
app.use(createComponentConfigurationPlugin(mountEl))
window.vm1 = app.mount(mountEl)
