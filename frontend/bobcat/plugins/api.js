/* eslint-disable no-console */
import createServices from '~/api/services'

if (process.env.MOCK_MAGNOLIA > 0) {
  const mockServer = () => import('~/server' /* webpackChunkName: 'bobcat-miragejs' */)
  mockServer()
}

export default ({$axios, $regions, $utils, $config, redirect}, inject) => {
  $axios.onError((e) => {
    let customParams = {}
    try {
      if (e.config?.params?.noerror) { return Promise.resolve({success: false, ...e.response}) }
      customParams = {
        path: e.config?.params?.path || e.request?.path,
        ref: e.request?.referer || e.request?._header?.match(/^referer: (.*?)$/mi)?.[1] || null,
        region: $regions?.current?.name,
        lang: $regions?.current?.language?.name
      }

      console.error(`[API plugin] Error response with code: ${e?.response?.status}, url: ${e?.request?.protocol + '//' + e?.request?.host + e?.request?.path}, ref: ${customParams?.ref}; ${e}`)

      if (e?.response?.status === 404 && e?.request?.host) {
        return redirect('/error', customParams)
      }
    } catch {
      if (!e.config?.params?.noerror) {
        return redirect('/error', customParams)
      }
    }
  })

  const srv = createServices($axios, $regions, $utils, $config, redirect)
  inject('services', srv)
}
