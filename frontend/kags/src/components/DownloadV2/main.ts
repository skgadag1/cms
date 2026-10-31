import '@/vueCompatConfig'
import Vue from 'vue'
import AutomatedDownload from './AutomatedDownload.vue'
import store from '@/store'
import { createComponentConfigurationPlugin } from '@/configureComponentApp'
import commonPlugin from '@/plugins/commonPlugin'
import { registerBootstrapVueNext } from '@/plugins/registerBootstrapVueNext'
import moment from 'moment'

Vue.prototype.moment = moment

const app = Vue.createApp(AutomatedDownload)
registerBootstrapVueNext(app)
app.use(store)
app.use(commonPlugin)
const mountEl = document.querySelector<HTMLElement>('#downloadV2')
if (!mountEl) throw new Error('Mount element #downloadV2 was not found')
app.use(createComponentConfigurationPlugin(mountEl))
window.vm = app.mount(mountEl)
