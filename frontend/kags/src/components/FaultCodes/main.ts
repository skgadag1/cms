import '@/vueCompatConfig'
import Vue from 'vue'
import FaultCodes from './FaultCodes.vue'
import store from '@/store'
import { createComponentConfigurationPlugin } from '@/configureComponentApp'
import commonPlugin from '@/plugins/commonPlugin'
import { registerBootstrapVueNext } from '@/plugins/registerBootstrapVueNext'

const app = Vue.createApp(FaultCodes)
registerBootstrapVueNext(app)
app.use(store)
app.use(commonPlugin)
const mountEl = document.querySelector<HTMLElement>('#faultcodes')
if (!mountEl) throw new Error('Mount element #faultcodes was not found')
app.use(createComponentConfigurationPlugin(mountEl))
window.vm = app.mount(mountEl)
