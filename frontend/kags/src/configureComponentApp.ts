import type {App, Plugin} from 'vue'
import DatePicker from '@vuepic/vue-datepicker'
import CardIcon from '@/components/common/CardIcon.vue'
import BIcon from '@/components/common/BIcon.vue'
import {STATE_RESTORE} from '@/store/mutations'
import store from '@/store'
import http from '@/utils'
import '@vuepic/vue-datepicker/dist/main.css'
import '@/scss/custom.scss'
import '@/assets/bobcaticons.generated.css'
import {library, dom} from '@fortawesome/fontawesome-svg-core'
import icons from '@/icons'

// Font Awesome's icon packages resolve duplicate versions of its common type definitions.
library.add(icons as never)
dom.watch()

type BodyConfiguration = {
  apiServers: Record<string, string> | null
  apiToken: string | null
  assetServer: string
  isMagnoliaEditMode: boolean
}

const readBodyConfiguration = (): BodyConfiguration => {
  const body = document.body
  const data = body.dataset

  return {
    apiServers: data.apiServers ? JSON.parse(data.apiServers) : null,
    apiToken: data.apiToken || null,
    assetServer: data.assetServer || '',
    isMagnoliaEditMode: Boolean(data.isEdit)
  }
}

export const configureApiClient = (apiServers: BodyConfiguration['apiServers'], apiToken: string | null): void => {
  const client = http as typeof http & {__kagsApiServerInterceptorInstalled?: boolean}
  if (!apiServers || client.__kagsApiServerInterceptorInstalled) return

  http.interceptors.request.use(request => {
    if (request.url && request.url.startsWith('%')) {
      const server = request.url.split('%')[1]
      request.url = request.url.replace('%' + server + '%', apiServers[server.toLowerCase()])
      if (apiToken) request.headers.Authorization = `Bearer ${apiToken}`
    }
    return request
  })

  client.__kagsApiServerInterceptorInstalled = true
}

export const createComponentConfigurationPlugin = (mountEl: HTMLElement): Plugin => ({
  install(app: App): void {
    const bodyConfiguration = readBodyConfiguration()
    const mountData = mountEl.dataset

    app.component('card-icon', CardIcon)
    app.component('b-icon', BIcon)
    app.component('date-picker', DatePicker)

    Object.assign(app.config.globalProperties, bodyConfiguration, {
      uuid: mountData.uuid,
      config: mountData.config ? JSON.parse(mountData.config) : {}
    })

    configureApiClient(bodyConfiguration.apiServers, bodyConfiguration.apiToken)
    store.commit(STATE_RESTORE)
  }
})
