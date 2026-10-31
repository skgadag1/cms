import axios from 'axios'
const path = require('path')

const _isAuthor = process.argv.includes('--author')

export default async function (options) {
  const $config = this.options.publicRuntimeConfig
  // provide gtm config per region
  const config = (async function () {
    const r = {}
    const url = `${$config.BOBCAT_API_SERVER}${$config.BOBCAT_API_CONFIG.replace('%INSTANCE%', _isAuthor ? 'author' : 'public').replace('%PATH%', 'gtmConfig')}`
    try {
      return (await axios.get(url))?.data
    } catch {
      // eslint-disable-next-line no-console
      console.warn('[GTM Config] unable to get gtm config (check connection?) from:', url)
    }
    return r
  })()

  options = await config

  // Register plugin
  this.addPlugin({
    src: path.resolve(__dirname, 'plugin.ejs'),
    fileName: 'gtmConfig.js',
    options
  })

  this.addPlugin({
    src: path.resolve(__dirname, 'gtm-tag.js'),
    fileName: 'gtmDisableAutoblock.js',
    mode: 'server'
  })
}
