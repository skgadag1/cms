import {shallowMount} from '@vue/test-utils'
import Specifications from '@/components/Specifications/Specifications'
import {METRICS} from '~/constants'
import productUtils from '~/plugins/utils/productUtils'

const _props = {
  title: '%ID% title',
  limitContent: false,
  showUnits: false,
  isCompare: false,
  uom: METRICS,
  labels: {
    units: 'Units mocked'
  }
}
const _model = {id: 'M0118', '@name': 'test', overview: '<p class="test-class">Sth very mocked</p>', '@path': '/na/test', '@nodeType': 'dxp:product'}

const options = {
  propsData: _props,
  stubs: {
    'b-img': true,
    'b-icon': true,
    'b-overlay': true,
    'b-collapse': true,
    'b-button': true,
    'b-button-group': true,
    LazyHydrate: true,
    ContentLimiter: true,
    SwitchDropdown: '<div />',
    ProductCompare: '<div />',
    WarningAlert: true
  },
  provide: {
    model: _model
  },
  mocks: {
    $fetchState: {pending: false},
    $regions: {
      current: {
        name: 'en',
        label: 'english',
        language: 'en'
      }
    },
    $nuxt: {
      context: {
        $utils: {
          products: productUtils,
          isMobile: () => false,
          isSmallMobile: () => false,
          isTablet: () => false,
          isDesktopLG: () => false,
          isDesktopXL: () => false
        }
      }
    }
  }
}

const getWrapper = (opt = options) => shallowMount(Specifications, opt)
const wrapper = getWrapper()

describe('Specifications component [data props]', () => {
  it('has rendered error info message in editmode', async () => {
    const _wrapper = getWrapper({...options, provide: {model: {}}})
    await _wrapper.setData({inEditorEdit: true, $fetchState: {pending: false}})
    await _wrapper.vm.$forceUpdate()
    expect(_wrapper.find('h2[data-test-id="product-specifications-title"]').exists()).toBe(false)
    expect(_wrapper.find('[data-test-id="product-specifications-nospecs-alert"]').exists()).toBe(false)
    expect(_wrapper.find('[data-test-id="specifications-alert"]').exists()).toBe(true)
  })

  it('has rendered no error info message in editmode', async () => {
    const _wrapper = getWrapper({...options, provide: {model: {id: _model.id, '@nodeType': 'dxp:product', '@path': '/sth'}}})
    await _wrapper.setData({inEditorEdit: true, $fetchState: {pending: false}})
    await _wrapper.vm.$forceUpdate()
    expect(_wrapper.find('[data-test-id="specifications-alert"]').exists()).toBe(false)
  })

  it('has title rendered', () => {
    expect(wrapper.find('h2[data-test-id="product-specifications-title"]').exists()).toBe(true)
    expect(wrapper.find('h2[data-test-id="product-specifications-title"]').text()).toEqual(_props.title.replace('%ID%', _model.id))
  })

  it('has no units rendered', () => {
    expect(wrapper.find('[data-test-id="specifications-units"]').exists()).toBe(false)
  })

  it('has units rendered', () => {
    const _wrapper = getWrapper({...options, propsData: {...options.propsData, showUnits: true}})
    expect(_wrapper.find('[data-test-id="specifications-units"]').exists()).toBe(true)
  })

  it('has compare rendered', () => {
    expect(wrapper.find('[data-test-id="product-specifications-download-button"]').exists()).toBe(true)
    expect(wrapper.find('[data-test-id="product-specifications-download-button"]').isVisible()).toBe(true)
  })
})
