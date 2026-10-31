import axios from 'axios'
import BUSINESS_REGIONS from '~/constants/businessRegions'

const _isAuthor = process.argv.includes('--author')

const GTM_LOADER_PREFIX = 'https://www.googletagmanager.com/gtm.js?id='
const GATEWAY_PATH_PATTERN = /^\/[a-z0-9-]+\/$/
const CREATE_ELEMENT = 'j=d.createElement(s)'
const DATA_OT_IGNORE = 'j.setAttributeNode(d.createAttribute(\'data-ot-ignore\'))'

function normalizeGatewayPath(gatewayPath) {
  if (gatewayPath == null) { return {state: 'absent'} }
  const value = String(gatewayPath).trim()
  if (!value) { return {state: 'absent'} }
  if (!GATEWAY_PATH_PATTERN.test(value)) { return {state: 'invalid', value} }
  return {state: 'valid', value}
}

function cameThroughCloudFront(req) {
  const headers = req?.headers || {}
  return Object.entries(headers).some(([name, value]) => {
    const headerName = name.toLowerCase()
    const isCloudFrontHeader = headerName === 'x-amz-cf-id' || headerName.startsWith('cloudfront-')
    return isCloudFrontHeader && typeof value === 'string' && value.trim()
  })
}

function invalidGatewayWarning(value) {
  const logged = String(value).replace(/\s+/g, ' ').slice(0, 100)
  return `[GTM tag] ignoring invalid gatewayPath: ${logged}`
}

function resolveGatewayPrefix(region, gatewayPath, viaCloudFront) {
  const gateway = normalizeGatewayPath(gatewayPath)
  if (gateway.state === 'invalid') {
    return {prefix: null, warning: invalidGatewayWarning(gateway.value)}
  }

  const useGateway = gateway.state === 'valid' && BUSINESS_REGIONS.na.includes(region) && viaCloudFront
  return {
    prefix: useGateway ? `${gateway.value}?id=` : null,
    warning: null
  }
}

export function patchGtmHeadScript(innerHTML, {region, gatewayPath, viaCloudFront, disableAutoblock} = {}) {
  if (typeof innerHTML !== 'string') { return {html: innerHTML, warning: null} }

  const gateway = resolveGatewayPrefix(region, gatewayPath, viaCloudFront)
  let html = innerHTML
  let warning = gateway.warning

  if (gateway.prefix) {
    if (html.includes(GTM_LOADER_PREFIX)) {
      html = html.replace(GTM_LOADER_PREFIX, gateway.prefix)
    } else {
      warning = '[GTM tag] gatewayPath set but GTM loader URL was not found in the head script'
    }
  }

  if (disableAutoblock === 'true') {
    html = html.replace(CREATE_ELEMENT, `${CREATE_ELEMENT};${DATA_OT_IGNORE}`)
  }

  return {html, warning}
}

function getHead(app) {
  return typeof app.head === 'function' ? app.head() : app.head
}

async function loadGtmConfig($config) {
  const url = `${$config.BOBCAT_API_SERVER}${$config.BOBCAT_API_CONFIG.replace('%INSTANCE%', _isAuthor ? 'author' : 'public').replace('%PATH%', 'gtmConfig')}`
  try {
    return (await axios.get(url))?.data || {}
  } catch {
    // eslint-disable-next-line no-console
    console.warn('[GTM Config] unable to get gtm config (check connection?) from:', url)
  }
  return {}
}

export default function (context) {
  const {app, beforeNuxtRender, $config, req} = context
  if (!process.server) { return }

  beforeNuxtRender(async () => {
    const options = await loadGtmConfig($config)
    if (options?.disableAutoblock === 'true') {
      // eslint-disable-next-line no-console
      console.log('[GTM tag] Autoblock disabled')
    }

    const gtmScript = getHead(app)?.script?.find(s => s.hid === 'gtm-script')
    if (typeof gtmScript?.innerHTML !== 'string') { return }

    // Direct SSR renders (the CMS indexer uses ssr.<env>.bobcat.com) do not
    // proxy the gateway path, so only rewrite requests CloudFront forwarded.
    // $regions is assigned by a plugin that runs after this one, so read it
    // when the hook runs rather than from the arguments at registration.
    const {html, warning} = patchGtmHeadScript(gtmScript.innerHTML, {
      region: context.$regions?.current?.name,
      gatewayPath: options?.gatewayPath,
      viaCloudFront: cameThroughCloudFront(req),
      disableAutoblock: options?.disableAutoblock
    })
    if (warning) {
      // eslint-disable-next-line no-console
      console.warn(warning)
    }
    gtmScript.innerHTML = html
  })
}
