import '@/vueCompatConfig'
import Vue from 'vue'
import Video from './Video.vue'
import store from '@/store'
import commonPlugin from '@/plugins/commonPlugin'
import { createComponentConfigurationPlugin } from '@/configureComponentApp'
import { registerBootstrapVueNext } from '@/plugins/registerBootstrapVueNext'

const app = Vue.createApp(Video)

registerBootstrapVueNext(app)
app.use(store)
app.use(commonPlugin)
const mountEl = document.querySelector<HTMLElement>('#video')
if (!mountEl) throw new Error('Video mount element #video was not found')
app.use(createComponentConfigurationPlugin(mountEl))
window.vm = app.mount(mountEl)
