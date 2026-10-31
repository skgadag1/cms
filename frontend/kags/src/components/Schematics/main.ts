import '@/vueCompatConfig'
import Vue from 'vue'
import Schematics from './Schematics.vue'
import store from '@/store'
import { createComponentConfigurationPlugin } from '@/configureComponentApp'
import commonPlugin from '@/plugins/commonPlugin'
import { registerBootstrapVueNext } from '@/plugins/registerBootstrapVueNext'

const app = Vue.createApp(Schematics)
registerBootstrapVueNext(app)
app.use(store)
app.use(commonPlugin)
const mountEl = document.querySelector<HTMLElement>('#schematics')
if (!mountEl) throw new Error('Mount element #schematics was not found')
app.use(createComponentConfigurationPlugin(mountEl))
window.vm = app.mount(mountEl)
