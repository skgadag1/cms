// DISABLED DUE TO NGB-3782

import * as fs from 'fs'
import * as path from 'path'
import axios from 'axios'

const PAGE_TEMPLATE_NAMES = [
  'bobcat:pages/index',
  'bobcat:pages/article',
  'bobcat:pages/attachment-details',
  'bobcat:pages/machine-details',
  'bobcat:pages/historical-machine-details',
  'bobcat:pages/region-selector',
  'bobcat:pages/video'
]

export default async function (options) {
  const $config = this.options.publicRuntimeConfig
  const _tdJson = await (async function () {
    const r = {}
    for (const templateName of PAGE_TEMPLATE_NAMES) {
      try {
        const url = `${$config.MAGNOLIA_CONTENT_API_SERVER.replace('api/', '')}${$config.MAGNOLIA_TEMPLATE_DEFINITIONS}${templateName}`
        const {data} = await axios.get(url)
        r[templateName] = data
      } catch (e) {
        // eslint-disable-next-line no-console
        console.error(`[TemplateDefinitions] Unable to get template config for template ${templateName} (check connection?): ${e}`)
      }
    }
    return r
  })()

  fs.writeFile(
    path.resolve(options.filePath),
    JSON.stringify(_tdJson),
    (e) => {
      if (e) { throw new Error('[TemplateDefinitions] Could not write TemplateDefinitions json') }
    }
  )
}
