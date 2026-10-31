import '@/vueCompatConfig'
import Vue from 'vue'
import MaintenanceSchedule from './MaintenanceSchedule.vue'
import store from '@/store'
import { createComponentConfigurationPlugin } from '@/configureComponentApp'
import commonPlugin from '@/plugins/commonPlugin'
import { registerBootstrapVueNext } from '@/plugins/registerBootstrapVueNext'

const app = Vue.createApp(MaintenanceSchedule)
registerBootstrapVueNext(app)
app.use(store)
app.use(commonPlugin)
const mountEl = document.querySelector<HTMLElement>('#maintenanceschedule')
if (!mountEl) throw new Error('Mount element #maintenanceschedule was not found')
app.use(createComponentConfigurationPlugin(mountEl))
window.vm = app.mount(mountEl)
