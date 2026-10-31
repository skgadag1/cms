import '@/vueCompatConfig'
import Vue from 'vue'
import MachineHistory from './MachineHistory.vue'
import store from '@/store'
import { createComponentConfigurationPlugin } from '@/configureComponentApp'
import commonPlugin from '@/plugins/commonPlugin'
import { registerBootstrapVueNext } from '@/plugins/registerBootstrapVueNext'

const app = Vue.createApp(MachineHistory)
registerBootstrapVueNext(app)
app.use(store)
app.use(commonPlugin)
const mountEl = document.querySelector<HTMLElement>('#machinehistory')
if (!mountEl) throw new Error('Mount element #machinehistory was not found')
app.use(createComponentConfigurationPlugin(mountEl))
window.vm = app.mount(mountEl)
