import {shallowMount} from '@vue/test-utils'
import ProductSpecifications from '@/components/ProductSpecifications'
import ProductSpecItem from '@/components/elements/ProductSpecItem'
import CompareTableWrapper from '@/components/Compare/CompareTableWrapper'
import WarningAlert from '@/components/elements/WarningAlert'
import productUtils from '~/plugins/utils/productUtils'
import mockedSpecs from '~/server/responses/products-api/specs'

const _props = {
  title: '%ID% title',
  keySpecsTitle: '',
  limitContent: 'false',
  showUnits: 'false',
  labels: {
    units: 'Units mocked'
  }
}
const _model = {id: 'M0055', '@name': 'test', overview: '<p class="test-class">Sth very mocked</p>', '@path': '/na/test', '@nodeType': 'dxp:product'}

const options = {
  propsData: _props,
  stubs: {
    'b-img': true,
    'b-icon': true,
    'b-overlay': true,
    'b-collapse': true,
    'b-button': true,
    'b-button-group': true,
    ImageSet: true,
    LazyHydrate: true,
    ContentLimiter: true,
    SwitchDropdown: '<div />',
    ProductCompare: '<div />',
    ProductSpecItem,
    CompareTableWrapper,
    CompareTable: true,
    Specifications: true,
    WarningAlert
  },
  provide: {
    model: _model,
    pageTemplateId: 'test'
  },
  mocks: {
    $fetchState: {pending: false},
    pageTemplateId: 'test',
    $nuxt: {
      context: {
        $utils: {
          products: productUtils
        },
        $services: {
          bobcatapi: {
            getConcepts: () => {
              return {data: {test: 'test'}}
            }
          },
          products: {
            getModelSpecifications: (model, uom, level) => level === 2 ? {data: mockedSpecs} : {data: mockedSpecs}
          }
        },
        $regions: {
          current: {
            name: 'en',
            label: 'english',
            language: 'en'
          }
        }
      }
    }
  }
}

const getWrapper = (opt = options) => shallowMount(ProductSpecifications, opt)

describe('ProductSpecifications component [data props]', () => {
  it('has rendered error info message in editmode', async () => {
    const _wrapper = getWrapper({...options, provide: {model: {}, pageTemplateId: 'test'}})
    await _wrapper.setData({inEditorEdit: true, $fetchState: {pending: false}})
    expect(_wrapper.find('[data-test-id="product-specifications"]').exists()).toBe(true)
    expect(_wrapper.find('[data-test-id="product-specifications-nospecs-alert"]').exists()).toBe(true)
  })

  it('has rendered error info message in editmode', async () => {
    const _wrapper = getWrapper({...options, provide: {model: {id: _model.id}, pageTemplateId: 'test'}})
    await _wrapper.setData({inEditorEdit: true, $fetchState: {pending: false}})
    expect(_wrapper.find('[data-test-id="product-specifications-nospecs-alert"]').exists()).toBe(true)
  })

  it('has renders nospecs alert', async () => {
    const _wrapper = getWrapper({...options, provide: {model: {}, pageTemplateId: 'test'}})
    await _wrapper.setData({inEditorEdit: true})
    await _wrapper.vm.$forceUpdate()
    expect(_wrapper.find('[data-test-id="product-specifications-nospecs-alert"]').isVisible()).toBe(true)
  })
})
