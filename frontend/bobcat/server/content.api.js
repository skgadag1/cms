/* eslint-disable no-console */
import api from '../api/index'

export default function (server) {
  // standard response
  server.get(api.getEndpoint(api.endpoints.CONTENT_API.TRANSLATIONS, {path: 'components/:path'}), (schema, request) => {
    const {path} = request.params
    console.debug('[Mock] translations endpoint')
    const component = require(`./responses/content-api/components/${path}`)
    return component
  })

  // simulate network error
  server.get(api.getEndpoint(api.endpoints.CONTENT_API.TRANSLATIONS, {path: 'components/Test'}), () => {
    console.debug('[Mock] error component')
    throw new Error('Network error')
  })
}
