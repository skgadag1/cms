import '@/vueCompatConfig'
import Vue from 'vue'
import MachineLibrary from './MachineLibrary.vue'
import store from '@/store'
import { createComponentConfigurationPlugin } from '@/configureComponentApp'
import commonPlugin from '@/plugins/commonPlugin'
import { registerBootstrapVueNext } from '@/plugins/registerBootstrapVueNext'

const app = Vue.createApp(MachineLibrary)
registerBootstrapVueNext(app)
app.use(store)
app.use(commonPlugin)
const mountEl = document.querySelector<HTMLElement>('#machinelibrary')
if (!mountEl) throw new Error('Mount element #machinelibrary was not found')
app.use(createComponentConfigurationPlugin(mountEl))
window.vm = app.mount(mountEl)
