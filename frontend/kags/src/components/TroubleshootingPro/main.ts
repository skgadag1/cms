import '@/vueCompatConfig'
import Vue from 'vue'
import TroubleshootingPro from './TroubleshootingPro.vue'
import store from '@/store'
import { createComponentConfigurationPlugin } from '@/configureComponentApp'
import commonPlugin from '@/plugins/commonPlugin'
import { registerBootstrapVueNext } from '@/plugins/registerBootstrapVueNext'


const app = Vue.createApp(TroubleshootingPro)
registerBootstrapVueNext(app)
app.use(store)
app.use(commonPlugin)
const mountEl = document.querySelector<HTMLElement>('#troubleshooting')
if (!mountEl) throw new Error('Mount element #troubleshooting was not found')
app.use(createComponentConfigurationPlugin(mountEl))
window.vm = app.mount(mountEl)
