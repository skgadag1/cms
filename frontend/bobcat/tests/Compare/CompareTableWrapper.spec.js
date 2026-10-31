import {shallowMount} from '@vue/test-utils'
import CompareTableWrapper from '@/components/Compare/CompareTableWrapper'
import {METRICS} from '~/constants'
import mockedSpecs from '~/server/responses/products-api/specs'
import productUtils from '~/plugins/utils/productUtils'

const _model = {id: 'M0055', '@name': 'test', overview: '<p class="test-class">Sth very mocked</p>', '@path': '/na/test', '@nodeType': 'dxp:product'}

const _props = {
  models: [_model],
  uom: METRICS[0]
}

const options = {
  propsData: _props,
  stubs: {
    'b-icon': true,
    ImageSet: true,
    'b-overlay': true,
    'b-collapse': true,
    LazyHydrate: true,
    CompareTable: true,
    WarningAlert: true
  },
  provide: {
    model: _model
  },
  mocks: {
    $fetchState: {pending: false},
    $nuxt: {
      context: {
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
        $utils: {
          isMobile: () => true,
          isTablet: () => true,
          products: productUtils
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

const getWrapper = (opt = {}) => shallowMount(CompareTableWrapper, {...options, ...opt})
const wrapper = getWrapper()

describe('CompareTableWrapper component [data props]', () => {
  it('has being rendered in single state', () => {
    expect(wrapper.find('[data-test-id=specification-table-compare]').isVisible()).toBe(true)
    expect(wrapper.find('[data-test-id="compare-table-error"]').exists()).toBe(false)
  })

  it('renders error', async () => {
    await wrapper.setData({error: true})
    expect(wrapper.find('[data-test-id="compare-table-error"]').isVisible()).toBe(true)
  })
})
