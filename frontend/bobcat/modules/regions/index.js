import axios from 'axios'
import mockedRegions from '../../server/responses/bobcat-api/regions/bobcat'
const path = require('path')

const _isAuthor = process.argv.includes('--author')

export default async function (options) {
  const $config = this.options.publicRuntimeConfig
  const useNative = $config.USE_NATIVE_MAGNOLIA
  // provide all regions for page
  const regions = (async function () {
    if (process.env.MOCK_MAGNOLIA > 0) {
      // eslint-disable-next-line no-console
      console.debug('[Regions] mocking regions in module')
      return mockedRegions
    }
    let r = []

    const bobcatApiUrl = $config.BOBCAT_API_KEY ? $config.BUILD_API_SERVER : $config.BOBCAT_API_SERVER
    const url = $config.USE_NATIVE_MAGNOLIA
      ? `${$config.MAGNOLIA_CONTENT_API_SERVER.replace('.rest/api/', '.rest/')}${$config.BOBCAT_API_REGIONS.replace('content/%INSTANCE%/', '')}`
      : `${bobcatApiUrl}${$config.BOBCAT_API_REGIONS.replace('%INSTANCE%', _isAuthor ? 'author' : 'public')}`

    const params = {
      headers: {
        'x-api-key': $config.BOBCAT_API_KEY ?? ''
      }
    }

    try {
      r = (await axios.get(url, params)).data
    } catch (e) {
      // eslint-disable-next-line no-console
      console.error('[Regions] unable to get regions (check connection?) from:', url, e)
      throw new Error('Could not connect with Magnolia instance to retrieve regions')
    }
    return r
  })()

  options.allRegions = await regions

  // Register plugin
  this.addPlugin({
    src: path.resolve(__dirname, 'plugin.ejs'),
    fileName: 'regions.js',
    options
  })
}
