import productUtils, {fixKey, reValue} from '~/plugins/utils/productUtils'
import specs from '~/server/responses/products-api/specs'
import structurizedSpecs from '~/server/responses/products-api/structurizedSpecs'
import {PRODUCT_SPECS_REMOVED_SECTIONS, PRODUCT_SPECS_REMOVED_PROPERTIES} from '~/constants'

const mockedPaths = [
  {model: {'@path': '/na/test'}, unit: 'na', region: 'na'},
  {model: {'@path': '/mea/test'}, unit: 'emea', region: 'mea'},
  {model: {'@path': '/eu/test'}, unit: 'emea', region: 'eu'},
  {model: {'@path': '/cis/test'}, unit: 'emea', region: 'cis'},
  {model: {'@path': '/ap/test'}, unit: 'alao', region: 'ap'},
  {model: {'@path': '/oc/test'}, unit: 'alao', region: 'oc'},
  {model: {'@path': '/la/test'}, unit: 'alao', region: 'la'},
  {model: {'@path': '/kr/test'}, unit: 'alao', region: 'kr'},
  {model: {'@path': '/in/test'}, unit: 'alao', region: 'in'},
  {model: {'@path': '/sea/test'}, unit: 'alao', region: 'sea'},
  {model: {'@path': '/cn/test'}, unit: 'alao', region: 'cn'},
  {model: {'@path': '/nothing/test'}, unit: undefined, region: 'nothing'},
  {model: {'@path': '/nothing/en/test2'}, unit: undefined, region: 'nothing'},
  {model: {'@path': '/test/nothing/test'}, unit: undefined, region: 'test'}
]

process.env.VUE_APP_SITE_BASENAME = 'bobcat'
process.env.NUXT_DEFAULT_REGION = 'na'

describe('[PathUtils plugin] getBusinessUnit()', () => {
  it('maps region to business unit', () => {
    for (const p of mockedPaths) {
      expect(productUtils.getBusinessUnit(p.model)).toEqual(p.unit)
    }
  })

  it('get proper region from model', () => {
    for (const p of mockedPaths) {
      expect(productUtils.getProductRegion(p.model)).toEqual(p.region)
    }
  })

  it('maps not existing region to no business unit', () => {
    expect(productUtils.getBusinessUnit('/none/test')).toEqual(process.env.NUXT_DEFAULT_REGION)
  })

  it('properly transforms paths with model root folder', () => {
    for (const p of mockedPaths) {
      expect(productUtils.getBusinessUnit({'@path': `/bobcat${p.model['@path']}`})).toEqual(p.unit)
    }
  })
})

describe('[PathUtils plugin] product structurization methods', () => {
  it('structurize specs data properly', () => {
    const tmpSpecs = {
      Mtest: {
        model: {id: 'Mtest'},
        data: specs
      }
    }
    const s = productUtils.structurizeSpecsForCompareTable(tmpSpecs, [], PRODUCT_SPECS_REMOVED_PROPERTIES)
    expect(s).toEqual(structurizedSpecs)
  })

  it('structurize specs data properly with striping sections', () => {
    const tmpSpecs = {
      Mtest: {
        model: {id: 'Mtest'},
        data: specs
      }
    }
    const s = productUtils.structurizeSpecsForCompareTable(tmpSpecs, PRODUCT_SPECS_REMOVED_SECTIONS, PRODUCT_SPECS_REMOVED_PROPERTIES)
    const structurizedSpecsRemoved = Object.assign({}, structurizedSpecs)
    PRODUCT_SPECS_REMOVED_SECTIONS.forEach((x) => {
      const key = 'section-key-' + fixKey(x)
      if (structurizedSpecsRemoved.sections[key]) { delete structurizedSpecsRemoved.sections[key] }
    })

    expect(s).toEqual(structurizedSpecsRemoved)
  })

  it('map values properly', async () => {
    const _labels = {
      test: 'testRemapped',
      yes: '<span class="test" />'
    }
    expect(reValue('test', _labels)).toEqual(_labels.test)
    expect(reValue('yes', _labels)).toEqual(_labels.yes)
  })

  it('formats props properly [text]', async () => {
    let _prop = productUtils.getFormattedValues([structurizedSpecs.sections['section-key-engine'].properties.enginecooling.Mtest])
    expect(_prop[0].value).toEqual('Liquid')
    _prop = productUtils.getFormattedValues([structurizedSpecs.sections['section-key-engine'].properties.enginecooling.Mtest], {Liquid: 'testRemapped'})
    expect(_prop[0].value).toEqual('testRemapped')
  })

  it('formats props properly [unparsed]', async () => {
    const _prop = productUtils.getFormattedValues([{
      id: 'picture',
      label: 'Image',
      type: 'unparsed',
      value: ''
    }])
    expect(_prop.length).toEqual(0)
  })
})

describe('[ProductUtils plugin] getFirstModelImage()', () => {
  const galleryImages = {
    '@nodes': ['images0'],
    images0: {asset: {field: 'img', img: {src: 'cloudinary:test/asset-0#{}'}}}
  }

  it('returns null when no model', () => {
    expect(productUtils.getFirstModelImage(null)).toBeNull()
  })

  it('prefers the single subcategory image over the assets gallery', () => {
    const model = {
      image: {src: 'cloudinary:test/subcategory-image#{}'},
      images: galleryImages
    }
    expect(productUtils.getFirstModelImage(model)).toEqual({img: model.image})
  })

  it('falls back to the first gallery asset when there is no subcategory image', () => {
    const model = {images: galleryImages}
    expect(productUtils.getFirstModelImage(model)).toEqual(galleryImages.images0.asset)
  })

  it('falls back to the gallery when the subcategory image has no src', () => {
    const model = {image: {imageAlt: 'no source'}, images: galleryImages}
    expect(productUtils.getFirstModelImage(model)).toEqual(galleryImages.images0.asset)
  })
})
