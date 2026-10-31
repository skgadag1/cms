/* eslint-disable no-console */
import * as fs from 'fs'
import * as path from 'path'
import axios from 'axios'

const _isAuthor = process.argv.includes('--author')

export default async function (options) {
  const $config = this.options.publicRuntimeConfig
  const getFallbackData = () => import('./errorPageConfigFallback.json' /* webpackChunkName: 'bobcat-error-page-data-fallback' */)
  const errorPageConfig = await (async function () {
    if (process.env.MOCK_MAGNOLIA > 0) {
      console.debug('[Error Page Config] Mocking errorPageConfig in module')
      return getFallbackData()
    }
    let r = {}
    const errorPageUrl = `${$config.BOBCAT_API_SERVER}content/${_isAuthor ? 'author' : 'public'}/api/content/pages/bobcat/error`
    const logoPageUrl = `${$config.BOBCAT_API_SERVER}content/${_isAuthor ? 'author' : 'public'}/api/content/pages/bobcat/na`

    try {
      const content = (await axios.get(errorPageUrl, {params: {lang: 'all'}})).data.main
      const firstComponent = content[content['@nodes'][0]]
      r = adjustSectionsFormat(firstComponent)
      r.logo = (await axios.get(logoPageUrl)).data.header[0].logo.src
    } catch (e) {
      console.error('[Error Page Config] Unable to get Error Page config (check connection?):', e)
      console.info('[Error Page Config] Using MockData as content server is unreachable')
      r = getFallbackData()
    }
    return r
  })()

  fs.writeFile(
    path.resolve(options.filePath),
    JSON.stringify(errorPageConfig),
    (e) => {
      if (e) { throw new Error('[Error Page Config] Could not write Error Page Configuration') }
      console.info('[Error Page Config] Config Saved')
    }
  )
}

const adjustSectionsFormat = (errorObj = {}) => {
  const response = {sections: {}}
  // reformat object to deliver sections
  Object.keys(errorObj).forEach((key) => {
    const currentKey = key.split('_')
    if (currentKey[0] === 'sections') {
      const lang = currentKey?.[1] || process.env.NUXT_DEFAULT_LANGUAGE
      const newSection = errorObj[key]

      response.sections[lang] = {}
      response.sections[lang]['@nodes'] = []

      newSection['@nodes'].forEach((k) => {
        // injecting new section
        const nwSctn = newSection[k]
        response.sections[lang][k] = {
          title: nwSctn[`title${lang !== process.env.NUXT_DEFAULT_LANGUAGE ? `_${lang}` : ''}`],
          content: nwSctn[`content${lang !== process.env.NUXT_DEFAULT_LANGUAGE ? `_${lang}` : ''}`],
          htag: nwSctn.htag,
          region: {'@name': nwSctn.region['@name']},
          cta: nwSctn[`cta${lang !== process.env.NUXT_DEFAULT_LANGUAGE ? `_${lang}` : ''}`],
          links: nwSctn[`links${lang !== process.env.NUXT_DEFAULT_LANGUAGE ? `_${lang}` : ''}`]
        }
        response.sections[lang]['@nodes'].push(k)
      })
    }
  })

  return response
}
