import {shallowMount} from '@vue/test-utils'
import productUtils from '~/plugins/utils/productUtils'
import Features from '@/components/Features'

const _props = {
  title: '%ID% title',
  limitContent: false,
  showUnits: false,
  isCompare: false,
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
    ImageSet: true,
    ContentLimiter: true,
    SwitchDropdown: '<div />',
    ProductCompare: '<div />'
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
          products: productUtils
        },
        $services: {
          bobcatapi: {
            getConcepts: () => ({})
          }
        }
      }
    }
  }
}

const getWrapper = (opt = options) => shallowMount(Features, opt)
const wrapper = getWrapper()

describe('Features component [data props]', () => {
  it('has title rendered', () => {
    expect(wrapper.find('h2[data-test-id="features-title"]').exists()).toBe(true)
    expect(wrapper.find('h2[data-test-id="features-title"]').text()).toEqual(_props.title.replace('%ID%', _model.id))
  })
})
