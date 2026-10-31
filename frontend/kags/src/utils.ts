import axios from 'axios'
import type {AxiosResponse, InternalAxiosRequestConfig} from 'axios'
import {cacheAdapterEnhancer, throttleAdapterEnhancer} from 'axios-extensions'
import qs from 'qs'

type NavigationItem = {
  url: string
  title: string
  active?: boolean
  icon?: string | null
  isSection?: boolean
  items?: NavigationItem[] | null
  [key: string]: any
}

const globalWithProcess = globalThis as typeof globalThis & {process?: {env?: Record<string, string | undefined>}}
if (!globalWithProcess.process) globalWithProcess.process = {env: {}}
else if (!globalWithProcess.process.env) globalWithProcess.process.env = {}

const _options = {
  headers: {'Cache-Control': 'no-cache'},
  adapter: throttleAdapterEnhancer(cacheAdapterEnhancer(axios.getAdapter(axios.defaults.adapter)))
  /*validateStatus: function (status) { // temporaily disabled, waiting for BE to be ready
    if (status === 401) {
      if (navigator.userAgent.indexOf('MSIE') !== -1 || navigator.appVersion.indexOf('Trident/') > 0) {
        const event = document.createEvent('Event')
        event.initEvent('forceLogout', true, true)
        window.dispatchEvent(event)
      } else {
        window.dispatchEvent(new Event('forceLogout'))
      }
    }
    return status >= 200 && status < 300
  }*/
}

const _devOptions = {
  adapter: function (config: InternalAxiosRequestConfig) {
    config.adapter = null
    return new Promise<AxiosResponse<any>>((resolve, reject) => {
      axios.request(config).then(r => {
        if (r.data && (r.data.errorCode || r.data.error_code)) {
          let code = r.data.errorCode || r.data.error_code
          if (code === 401) window.dispatchEvent(new Event('forceLogout'))
          reject({code, response: r})
        }
        if (import.meta.env.VUE_APP_DEV_THROTTLE > 0) {
          //throttle response to check loaders
          setTimeout(() => resolve(r), (Math.random() * import.meta.env.VUE_APP_DEV_THROTTLE))
        } else resolve(r)
      }).catch(e => reject(e))
    })
  }
}

const _http = axios.create(import.meta.env.VUE_APP_MODE === 'preview' ? _devOptions : _options)

export default _http

export function getMainNavigationStructure(data: {items: NavigationItem[]}): NavigationItem[] {
  const _pathOriginal = window.location.pathname
  const _path = _pathOriginal.endsWith('/') ? _pathOriginal.substring(0, _pathOriginal.length - 1) : _pathOriginal

  return data.items.map(i => {
    if (i.url === _path || i.url === _pathOriginal) i.active = true
    else if (i.items) {
      for (const _sub of i.items) {
        if (_sub.url === _path || _sub.url === _pathOriginal) {
          _sub.active = true
          i.active = true
        }
      }
    }
    if (i.items) {
      const section = Object.assign({}, i, {items: null, isSection: true, active: i.url === _path || i.url === _pathOriginal})
      if(section.url !== i.items[0].url && section.title !== i.items[0].title)
        i.items = [section, ...i.items]
    }
    return i
  })
}

export function extractSubnavigation(n: NavigationItem[], path = window.location.pathname): NavigationItem[] {
  let _activeSection = null
  const pathOriginal = path

  path = window.location.pathname.endsWith('/')
    ? window.location.pathname.substring(0, window.location.pathname.length - 1)
    : window.location.pathname

  const _filteredItems = n.filter(section => {
    let _active = false

    if (section.url === path || section.url === pathOriginal) {
      _active = true
      _activeSection = Object.assign({}, section, {items: null, active: true})
    } else if (section.items) {
      for (const i of section.items) {
        if (i.url === path || i.url === pathOriginal) {
          _active = true
          i.active = true
          _activeSection = Object.assign({}, section, {items: null})
        }
      }
    }

    return _active
  })

  const _items = _filteredItems[0] ? _filteredItems[0].items || [] : []

  const hasIcons = _items.reduce((previousValue, item) => previousValue || (!item.isSection && item.icon && item.icon !== 'null'), false)

  if (!hasIcons) { // remove all icons from submenu
    _activeSection.icon = null
    _items.forEach(item => item.icon = null)
  }

  return _activeSection && _activeSection.title !== _items[0].title ? [_activeSection, ..._items] : _items
}

export function triggerGlobalStoreSync(): void {
  if (navigator.userAgent.indexOf('MSIE') !== -1 || navigator.appVersion.indexOf('Trident/') > 0) {
    const event = document.createEvent('Event')
    event.initEvent('storageUpdated', true, true)
    window.dispatchEvent(event)
  } else {
    window.dispatchEvent(new Event('storageUpdated'))
  }
}

export function isAiServiceEnabled(): boolean {
  const items = location.href.indexOf('?') > 0 ? qs.parse(location.search.substr(1)) : {}
  return items.aiService === 'true'
}

export function truncateFileName(fileName: string, maxLength = 50, tailLength = 6): string {
  if (!fileName || fileName.length <= maxLength) return fileName

  const match = fileName.match(/(\.[^.]+)$/)
  const ext = match ? match[1] : ''
  const nameWithoutExt = ext ? fileName.slice(0, -ext.length) : fileName

  const tail = nameWithoutExt.slice(-tailLength)
  const availableForStart = maxLength - ext.length - tail.length - 3 // 3 for '...'
  const start = nameWithoutExt.slice(0, Math.max(availableForStart, 0))

  return `${start}...${tail}${ext}`
}
