import pathUtils, {mapUrlToSliced} from '~/plugins/utils/path'

const mockedPageNodeName = 'bobcat'
global.process.env.VUE_APP_SITE_BASENAME = `/${mockedPageNodeName}`
global.process.env.NUXT_DEFAULT_LANGUAGE = 'en'

const absolutePathMock = [
  {url: '/bobcat/na/pl/bobcat-test/info', result: '/na/pl/bobcat-test/info'},
  {url: '/na/en/bobcat-test/info/', result: '/na/en/bobcat-test/info'},
  {url: '/bobcat/eu/de/test/info/', result: '/eu/de/test/info'},
  {url: '/bobcat/na/en/bobcat/info/', result: '/na/en/info'},
  {url: '/bobcat/na/en/bobcat/info', result: '/na/en/info'},
  {url: '/bobcat/la/es/test-bobcat/info/', result: '/la/es/test-bobcat/info'},
  {url: '/oc/en/test/', result: '/oc/en/test'},
  {url: '/oc/es/test/', result: '/oc/es/test'},
  {url: '/bobcat/oc/pt', result: '/oc/pt'},
  {url: '/oc/fr', result: '/oc/fr'},
  {url: '/bobcat/na/es/test/info', result: '/na/es/test/info'},
  {url: '/bobcat/sea/en/test-bobcat-info', result: '/sea/en/test-bobcat-info'}
]

const externalUrls = [
  {url: 'https://www.bobcat.com/test/bobcat-info/', result: 'https://www.bobcat.com/test/bobcat-info', path: 'test/bobcat-info'},
  {url: 'http://bobcat.com/test/bobcat/info/', result: '/info', path: 'info'},
  {url: 'http://bobcat.eu:223/nothing-bobcat/info/', result: 'http://bobcat.eu:223/nothing-bobcat/info', path: 'info'},
  {url: 'http://localhost:3000/na/en/bobcat/info/', result: '/info', path: 'info'},
  {url: 'https://localhost:3000/bobcat/na/en/bobcat/info', result: '/na/en/bobcat/info', path: 'na/en/info'},
  {url: 'https://localhost:3000/bobcat/na/es/bobcat-info/', result: '/na/es/bobcat-info', path: 'na/es/bobcat-info'},
  {url: 'https://localhost:3000/bobcat/na/en/test-bobcat/info', result: '/na/en/test-bobcat/info', path: 'na/en/test-bobcat/info'},
  {url: 'https://localhost/bobcat/na/en/test/info', result: '/na/en/test/info', path: 'na/en/test/info'},
  {url: 'https://ssr.dev.bobcat.com/bobcat/na/es', result: '/na/es', path: 'na/es'},
  {url: 'https://ssr.dev.bobcat.com/bobcat/na/es/', result: '/na/es', path: 'na/es'}
]

const urlsToSlice = [
  {url: 'https://localhost:3000/na/en/bobcat/info/', result: '/na/en/info'},
  {url: 'https://localhost:3000/na/es/bobcat-info/', result: '/na/es/bobcat-info', lang: 'es'},
  {url: 'https://localhost:3000/na/en/test-bobcat/info', result: '/na/en/test-bobcat/info'},
  {url: 'https://localhost/na/en/test/info/', result: '/na/en/test/info'},
  {url: 'https://ssr.dev.bobcat.com/na/es/', result: '/na/es', lang: 'es'}
]

const inheritancePaths = [
  {url: '/bobcat/na/en/bobcat/test/info', result: '/na/en/bobcat/test'},
  {url: '/bobcat/na/en/bobcat/info/', result: '/na/en/bobcat'},
  {url: '/bobcat/na/en/parts-search', result: '/na/en'},
  {url: '/bobcat/na/en/parts-search/parts', result: '/na/en/parts-search'},
  {url: '/bobcat/na/en/parts-search/search/', result: '/na/en/parts-search'},
  {url: '/bobcat/eu/es/parts-search/parts/search/', result: '/eu/es/parts-search/parts'},
  {url: '/bobcat/eu/es/parts-search-parts/search/test', result: '/eu/es/parts-search-parts/search'},
  {url: '/bobcat/eu/es/parts-search-parts/part-search', result: '/eu/es/parts-search-parts'},
  {url: '/bobcat/eu/es/parts-search-parts/part-search/eu/es/parts-search-parts/part-search/', result: '/eu/es/parts-search-parts/part-search/eu/es/parts-search-parts'},
  {url: '/bobcat/la/de/test/test/test/test', result: '/la/de/test/test/test'},
  {url: '/bobcat/oc/de/test1/test2/test3/test4', result: '/oc/de/test1/test2/test3'},
  {url: '/bobcat/oc/de/test-1/test-2/test-3/test-4', result: '/oc/de/test-1/test-2/test-3'},
  {url: '/bobcat/oc/de/bobcat-1/bobcat/bobcat-2/bobcat', result: '/oc/de/bobcat-1/bobcat/bobcat-2'},
  {url: '/bobcat/oc/de/bobcat-1/bobcat/test/bobcat', result: '/oc/de/bobcat-1/bobcat/test'},
  {url: '/bobcat/oc/de/test1/test2/test4/test3', result: '/oc/de/test1/test2/test4'}
]

describe('PathUtils plugin', () => {
  it('transforms properly absolute paths', () => {
    for (const p of absolutePathMock) {
      const _path = p.url.replace(/^\/bobcat\/(.*)/, '/$1')
      const _expectedPath = `${_path.endsWith('/') ? _path.slice(0, -1) : _path}`
      expect(pathUtils.pathWithoutBaseName(p.url, mockedPageNodeName)).toEqual(_expectedPath)
      expect(pathUtils.pathFix(p.url, mockedPageNodeName)).toEqual([...p.result.split('/').filter(x => x.length)])
    }
  })

  it('transforms properly external urls', () => {
    for (const u of externalUrls) {
      expect(pathUtils.pathWithoutBaseName(u.url, mockedPageNodeName)).toEqual(u.result)
      expect(pathUtils.pathFix(u.url, mockedPageNodeName).join('/')).toEqual(u.path || u.result)
    }
  })

  it('map properly regions with languages from urls', () => {
    for (const a of urlsToSlice) {
      expect(mapUrlToSliced(a.url, a.lang)).toEqual(a.result)
    }
  })

  it('preserves local anchors returned by pageLink fields', () => {
    const item = {
      pageLink: {
        field: 'localAnchor',
        '@nodes': ['link'],
        link: {
          anchorID: 'models',
          localAnchorLabel: 'Models',
          target: '_self'
        }
      }
    }

    expect(pathUtils.getPageUrl(item, 'en')).toBe('#models')
  })

  it('extract inheritance paths form urls', () => {
    for (const a of inheritancePaths) {
      expect(pathUtils.extractInheritancePaths(a.url)).toEqual(a.result.split('/').filter(x => x))
    }
  })
})
