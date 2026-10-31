import pathUtils from '~/middleware/regions'

global.process.env.VUE_APP_SITE_BASENAME = '/na'
global.process.env.NUXT_DEFAULT_LANGUAGE = 'en'
global.process.env.NUXT_DEFAULT_REGION = 'na'

const _lang = {
  name: 'en',
  '@name': 'en',
  enabled: 'true',
  isDefault: 'true'
}

const _region = {
  language: _lang,
  name: 'na',
  '@name': 'na',
  businessUnit: 'na',
  '@nodes': ['en'],
  en: _lang
}

const $regions = {
  current: _region,
  all: [_region]
}

const urls = [
  {url: '/error', result: null},
  {url: '/regions', result: null},
  {url: '/na/en/bobcat/info/', result: undefined},
  {url: '/na/bobcat-info/', result: '/na/en/bobcat-info'},
  {url: '/na/en/test-bobcat/info', result: undefined},
  {url: '/na/en/test/info/', result: undefined},
  {url: '/na/bobcat/info/', result: '/na/en/bobcat/info'},
  {url: '/na/bobcat-info/', result: '/na/en/bobcat-info'},
  {url: '/na/test-bobcat/info', result: '/na/en/test-bobcat/info'},
  {url: '/na/test/info/', result: '/na/en/test/info'},
  {url: '/na/', result: '/na/en'}
]

const urlsWithParams = [
  {url: '/regions?test', result: null},
  {url: '/error?test', result: null},
  {url: '/na/?test', result: '/na/en?test'},
  {url: '/na?test', result: '/na/en?test'},
  {url: '/?test=123&sth=else', result: undefined},
  {url: '/na/sthElse?test', result: '/na/en/sthElse?test'},
  {url: '/na/?test=123&sth=else', result: '/na/en?test=123&sth=else'},
  {url: '/na/?test', result: '/na/en?test'},
  {url: '/?test', result: undefined},
  {url: '/na?test=123&sth=else', result: '/na/en?test=123&sth=else'}
]

describe('Regions middleware', () => {
  it('transforms properly paths without params (en)', async () => {
    for (const p of urls) {
      expect(await pathUtils({$regions, route: {fullPath: p.url, path: p.url.split('?')[0]}, redirect: x => x, $services: {}, req: {}})).toEqual(p.result)
    }
  })

  it('transforms properly paths with params (en)', async () => {
    for (const p of urlsWithParams) {
      expect(await pathUtils({$regions, route: {fullPath: p.url, path: p.url.split('?')[0]}, redirect: x => x, $services: {}, req: {}})).toEqual(p.result)
    }
  })

  describe('should use default language for detected region', () => {
    // Given
    const langEN = {
      name: 'en',
      '@name': 'en',
      enabled: 'true',
      isDefault: 'false'
    }

    const langKO = {
      name: 'ko',
      '@name': 'ko',
      enabled: 'true',
      isDefault: 'true'
    }

    const region = {
      language: langKO,
      name: 'kr',
      '@name': 'kr',
      businessUnit: 'alao',
      enabled: 'true',
      '@nodes': ['en', 'ko'],
      en: langEN,
      ko: langKO
    }

    const regions = {
      all: [region, _region]
    }

    const services = {
      content: {
        getClientLocation: undefined
      }
    }

    beforeEach(() => {
      services.content.getClientLocation = jest.fn().mockReturnValue({data: {countryCode: 'KR', countryName: 'Korea Republic of', region: 'ALAO', siteRegion: 'kr'}})
    })

    it('Should pass for standard path', async () => {
      const response = await pathUtils({$regions: regions, route: {fullPath: '/', path: '/'}, redirect: x => x, $services: services, req: {}})

      expect(response).toEqual('/kr/ko')
    })

    it('Should pass for path with queryString', async () => {
      const response = await pathUtils({$regions: regions, route: {fullPath: '/?test=test', path: '/'}, redirect: x => x, $services: services, req: {}})

      expect(response).toEqual('/kr/ko?test=test')
    })
  })
})
