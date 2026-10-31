import axios from 'axios'
import BUSINESS_REGIONS from '~/constants/businessRegions'
import gtmTag, {patchGtmHeadScript} from '~/modules/gtmConfig/gtm-tag'

jest.mock('axios')

const NA_REGION = BUSINESS_REGIONS.na[0]
const OTHER_REGIONS = Object.values(BUSINESS_REGIONS).flat().filter(region => !BUSINESS_REGIONS.na.includes(region))
const STANDARD_GTM_URL = 'https://www.googletagmanager.com/gtm.js?id='

const GTM_HEAD_SCRIPT = `if(!window._gtm_init){window._gtm_init=1;(function(w,d,s,l,x,y){w[x]={};w._gtm_inject=function(i){w[x][i]=1;w[l]=w[l]||[];w[l].push({'gtm.start':new Date().getTime(),event:'gtm.js'});var f=d.getElementsByTagName(s)[0],j=d.createElement(s);j.async=true;j.src='${STANDARD_GTM_URL}'+i;f.parentNode.insertBefore(j,f);};})(window,document,'script','dataLayer','_gtm_ids','_gtm_inject')}`
const NOSCRIPT_IFRAME = '<iframe src="https://www.googletagmanager.com/ns.html?id=GTM-K8JRBX3" height="0" width="0" style="display:none;visibility:hidden" title="gtm"></iframe>'

function patch(overrides = {}) {
  return patchGtmHeadScript(GTM_HEAD_SCRIPT, {
    region: NA_REGION,
    gatewayPath: '/vo59/',
    viaCloudFront: true,
    disableAutoblock: 'false',
    ...overrides
  }).html
}

describe('patchGtmHeadScript', () => {
  it('rewrites the GTM loader URL for NA when gatewayPath is valid', () => {
    const html = patch()

    expect(html).toContain('j.src=\'/vo59/?id=\'+i')
    expect(html).not.toContain(STANDARD_GTM_URL)
    expect(html).not.toContain('www.googletagmanager.com/gtm.js')
  })

  it('does not rewrite the loader URL for other regions', () => {
    OTHER_REGIONS.forEach((region) => {
      expect(patch({region})).toBe(GTM_HEAD_SCRIPT)
    })
  })

  it('does not rewrite when gatewayPath is empty or missing', () => {
    [undefined, null, '', '   '].forEach((gatewayPath) => {
      const result = patchGtmHeadScript(GTM_HEAD_SCRIPT, {
        region: NA_REGION,
        gatewayPath,
        viaCloudFront: true
      })
      expect(result.html).toBe(GTM_HEAD_SCRIPT)
      expect(result.warning).toBeNull()
    })
  })

  it('ignores an invalid gatewayPath and keeps the standard URL', () => {
    const invalid = [
      'https://evil.example/gtm.js',
      '/vo59',
      'vo59/',
      '/vo59/?id=GTM-K8JRBX3',
      '/"></script><script>alert(1)</script>/',
      '/VO59/',
      '//evil.example/'
    ]

    invalid.forEach((gatewayPath) => {
      const result = patchGtmHeadScript(GTM_HEAD_SCRIPT, {
        region: NA_REGION,
        gatewayPath,
        viaCloudFront: true
      })
      expect(result.html).toBe(GTM_HEAD_SCRIPT)
      expect(result.warning).toContain('ignoring invalid gatewayPath')
      expect(result.html).not.toContain(gatewayPath.trim())
    })
  })

  it('does not rewrite when the request did not come through CloudFront', () => {
    expect(patch({viaCloudFront: false})).toBe(GTM_HEAD_SCRIPT)
  })

  it('applies data-ot-ignore alongside the gateway rewrite', () => {
    const html = patch({disableAutoblock: 'true'})

    expect(html).toContain('j.src=\'/vo59/?id=\'+i')
    expect(html).toContain('j=d.createElement(s);j.setAttributeNode(d.createAttribute(\'data-ot-ignore\'))')
    expect(html).not.toContain('www.googletagmanager.com/gtm.js')
  })

  it('still applies data-ot-ignore when the gateway is not used', () => {
    const html = patch({gatewayPath: '', disableAutoblock: 'true'})

    expect(html).toContain(STANDARD_GTM_URL)
    expect(html).toContain('j.setAttributeNode(d.createAttribute(\'data-ot-ignore\'))')
  })
})

describe('gtm-tag plugin', () => {
  const warn = jest.spyOn(console, 'warn').mockImplementation(() => {})
  const log = jest.spyOn(console, 'log').mockImplementation(() => {})

  beforeEach(() => {
    process.server = true
    warn.mockClear()
    log.mockClear()
    axios.get.mockReset()
  })

  afterAll(() => {
    warn.mockRestore()
    log.mockRestore()
  })

  async function render({region = NA_REGION, gatewayPath = '/vo59/', headers = {'x-amz-cf-id': 'cf-request'}, disableAutoblock, reject = false, headAsFunction = false} = {}) {
    if (reject) {
      axios.get.mockRejectedValue(new Error('offline'))
    } else {
      axios.get.mockResolvedValue({data: {id: 'GTM-K8JRBX3', gatewayPath, disableAutoblock}})
    }

    const gtmScript = {hid: 'gtm-script', innerHTML: GTM_HEAD_SCRIPT}
    const noscript = {hid: 'gtm-noscript', innerHTML: NOSCRIPT_IFRAME}
    const head = {script: [gtmScript], noscript: [noscript]}
    let renderHook
    const context = {
      app: {head: headAsFunction ? () => head : head},
      beforeNuxtRender: (fn) => { renderHook = fn },
      $config: {
        BOBCAT_API_SERVER: 'https://api.example',
        BOBCAT_API_CONFIG: '/%INSTANCE%/%PATH%'
      },
      req: {headers}
    }
    gtmTag(context)
    context.$regions = {current: {name: region}}
    await renderHook()
    return {gtmScript, noscript}
  }

  it('rewrites NA HTML served through CloudFront and leaves the noscript iframe alone', async () => {
    const {gtmScript, noscript} = await render()

    expect(gtmScript.innerHTML).toContain('j.src=\'/vo59/?id=\'+i')
    expect(gtmScript.innerHTML).not.toContain('www.googletagmanager.com/gtm.js')
    expect(noscript.innerHTML).toBe(NOSCRIPT_IFRAME)
  })

  it('rewrites NA when the region is assigned after the plugin registers', async () => {
    const {gtmScript} = await render()

    expect(gtmScript.innerHTML).toContain('j.src=\'/vo59/?id=\'+i')
  })

  it('rewrites when head is a function', async () => {
    const {gtmScript} = await render({headAsFunction: true})

    expect(gtmScript.innerHTML).toContain('j.src=\'/vo59/?id=\'+i')
  })

  it('keeps the standard URL for non-NA regions', async () => {
    const {gtmScript} = await render({region: BUSINESS_REGIONS.emea[0]})

    expect(gtmScript.innerHTML).toContain(STANDARD_GTM_URL)
  })

  it('keeps the standard URL when the request bypasses CloudFront', async () => {
    const {gtmScript} = await render({headers: {host: 'ssr.prod.bobcat.com'}})

    expect(gtmScript.innerHTML).toContain(STANDARD_GTM_URL)
    expect(warn).not.toHaveBeenCalled()
  })

  it('logs a warning and keeps the standard URL for an invalid gatewayPath', async () => {
    const {gtmScript} = await render({gatewayPath: 'https://evil.example/gtm.js'})

    expect(gtmScript.innerHTML).toContain(STANDARD_GTM_URL)
    expect(warn).toHaveBeenCalledWith(expect.stringContaining('ignoring invalid gatewayPath'))
    expect(gtmScript.innerHTML).not.toContain('evil.example')
  })

  it('keeps the standard URL when gtm config cannot be loaded', async () => {
    const {gtmScript} = await render({reject: true})

    expect(gtmScript.innerHTML).toContain(STANDARD_GTM_URL)
    expect(warn).toHaveBeenCalledWith('[GTM Config] unable to get gtm config (check connection?) from:', expect.any(String))
  })

  it('applies data-ot-ignore when disableAutoblock is true', async () => {
    const {gtmScript} = await render({disableAutoblock: 'true'})

    expect(log).toHaveBeenCalledWith('[GTM tag] Autoblock disabled')
    expect(gtmScript.innerHTML).toContain('j.setAttributeNode(d.createAttribute(\'data-ot-ignore\'))')
    expect(gtmScript.innerHTML).toContain('j.src=\'/vo59/?id=\'+i')
  })
})
