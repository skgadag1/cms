import {buildAndPriceUrl} from '~/helpers/buildAndPriceUrl'
import {
  BUILD_AND_PRICE_FIELDS,
  BUILD_AND_PRICE_QUERY,
  BUILD_AND_PRICE_REGION
} from '~/constants/buildAndPrice'

const na = {region: BUILD_AND_PRICE_REGION, language: 'en'}

const product = (overrides = {}) => ({
  [BUILD_AND_PRICE_FIELDS.ENABLED]: true,
  title: 'T450',
  '@path': '/bobcat/na/Loaders/Compact-Track-Loaders/T450',
  ...overrides
})

describe('buildAndPriceUrl', () => {
  it('builds category and subcategory from the catalog path and the model from the title', () => {
    const url = buildAndPriceUrl(product(), na)

    expect(url).toBe('/na/en/build-price?cat=loaders&subcat=compact-track-loaders&model=t450')
    expect(url.indexOf(BUILD_AND_PRICE_QUERY.CATEGORY)).toBeLessThan(url.indexOf(BUILD_AND_PRICE_QUERY.SUBCATEGORY))
    expect(url.indexOf(BUILD_AND_PRICE_QUERY.SUBCATEGORY)).toBeLessThan(url.indexOf(`${BUILD_AND_PRICE_QUERY.MODEL}=`))
  })

  it('keeps the subcategory when a product group sits between it and the model', () => {
    expect(buildAndPriceUrl(product({
      title: 'T740',
      '@path': '/bobcat/na/Loaders/Compact-Track-Loaders/T740/T740-T4'
    }), na)).toBe('/na/en/buying-tools/build-price?cat=loaders&subcat=compact-track-loaders&model=t740')
  })

  it('omits subcategory when the path has only a category and a product', () => {
    expect(buildAndPriceUrl(product({
      title: 'TL723',
      '@path': '/bobcat/na/Telehandlers/TL723'
    }), na)).toBe('/na/en/buying-tools/build-price?cat=telehandlers&model=tl723')
  })

  it('uses the listing name when the hit has no title', () => {
    expect(buildAndPriceUrl(product({
      title: undefined,
      name: 'T450'
    }), na)).toBe('/na/en/buying-tools/build-price?cat=loaders&subcat=compact-track-loaders&model=t450')
  })

  it('shows the link when the model flag is unset or stored as a Magnolia string', () => {
    const {[BUILD_AND_PRICE_FIELDS.ENABLED]: enabled, ...withoutFlag} = product()
    expect(enabled).toBe(true)
    expect(buildAndPriceUrl(withoutFlag, na)).toBe('/na/en/buying-tools/build-price?cat=loaders&subcat=compact-track-loaders&model=t450')
    expect(buildAndPriceUrl(product({
      [BUILD_AND_PRICE_FIELDS.ENABLED]: 'true'
    }), na)).toBe('/na/en/buying-tools/build-price?cat=loaders&subcat=compact-track-loaders&model=t450')
  })

  it('returns null when the product is not enabled, incomplete, or outside North America', () => {
    expect(buildAndPriceUrl(product({[BUILD_AND_PRICE_FIELDS.ENABLED]: false}), na)).toBeNull()
    expect(buildAndPriceUrl(product({[BUILD_AND_PRICE_FIELDS.ENABLED]: 'false'}), na)).toBeNull()
    expect(buildAndPriceUrl(product({title: '  '}), na)).toBeNull()
    expect(buildAndPriceUrl(product({'@path': '/bobcat/na'}), na)).toBeNull()
    expect(buildAndPriceUrl(product(), {region: 'eu', language: 'en'})).toBeNull()
    expect(buildAndPriceUrl(null, na)).toBeNull()
  })

  it('does not add a config parameter', () => {
    expect(buildAndPriceUrl(product(), na).includes('config')).toBe(false)
  })
})
