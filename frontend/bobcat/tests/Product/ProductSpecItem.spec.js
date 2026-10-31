import {shallowMount} from '@vue/test-utils'
import ProductSpecItem from '@/components/elements/ProductSpecItem'

const _props = {
  title: 'Extra title',
  properties: {},
  opened: false,
  keys: ['Mtest']
}

const options = {
  propsData: _props,
  stubs: {
    'b-icon': true,
    'b-collapse': true,
    ImageSet: true,
    LazyHydrate: true
  }
}

const getWrapper = (opt = options) => shallowMount(ProductSpecItem, opt)
const wrapper = getWrapper()

describe('ProductSpecItem component [data props]', () => {
  it('is rendered', () => {
    const _main = wrapper.find('[data-test-id="product-spec-item"]')
    expect(_main.exists()).toBe(true)
    expect(_main.classes()).toContain('specification-compare-table__container')
  })

  it('renders opened', async () => {
    const _wrapper = getWrapper({...options, propsData: {..._props, opened: true}})
    const _containerWrapper = _wrapper.find('[data-test-id="product-spec-item"] .specification-compare-table__items-1')
    expect(_containerWrapper.classes()).not.toContain('rounded-bottom')
  })

  it('renders properties', async () => {
    const _wrapper = getWrapper({...options, propsData: {..._props, opened: true}})
    expect(_wrapper.findAll('[data-test-id^="product-spec-item__"]')).toHaveLength(0)
    await _wrapper.setProps({
      properties: {
        general: {
          label: 'test',
          Mtest: {
            value: [{
              label: 'test0',
              value: 'test0'
            }, {
              label: 'test1',
              value: 'test1'
            }]
          }
        },
        common: {
          label: 'test',
          Mtest: {
            value: [{
              label: 'test0',
              value: 'test0'
            }, {
              label: 'test1',
              value: 'test1'
            }]
          }
        }
      }
    })
    expect(_wrapper.findAll('[data-test-id^="product-spec-item__"]')).toHaveLength(2)
  })
})
