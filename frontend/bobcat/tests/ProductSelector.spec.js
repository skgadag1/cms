import {shallowMount, createLocalVue} from '@vue/test-utils'
import ProductSelector from '@/components/elements/Form/ProductSelector'
import mock from '~/server/responses/bobcat-api/pages/components/productSelector'

const localVue = createLocalVue()

localVue.directive('click-outside', () => {})

const wrapper = shallowMount(ProductSelector, {
  localVue,
  propsData: {
    ...mock.props
  },
  mocks: {
    $nuxt: {
      context: {
        $services: {
          content: {
            getProductSelector: () => ({
              data: {
                selectorResult: {
                  categories: mock.productSelectorItems.categories
                }
              }
            })
          }
        }
      }
    }
  },
  stubs: {
    'b-badge': true,
    'b-icon': true,
    'b-form-input': true,
    ProductList: true,
    WarningAlert: true
  }
})

describe('ProductSelector component [data props]', () => {
  it('has wrapper', () => {
    expect(wrapper.find('div[data-test-id="product-selector"]').exists()).toBe(true)
  })

  it('has label', () => {
    expect(wrapper.find('label[data-test-id="product-selector-label"]').exists()).toBe(true)
  })

  it('has input', () => {
    expect(wrapper.find('div[data-test-id="product-selector-input"]').exists()).toBe(true)
  })

  it('has search', () => {
    expect(wrapper.find('b-form-input-stub').exists()).toBe(true)
    expect(wrapper.find('b-form-input-stub').attributes('placeholder')).toEqual(mock.props.placeholder)
  })

  it('has arrow icon', () => {
    expect(wrapper.find('.sort-icon').exists()).toBe(true)
    expect(wrapper.find('b-icon-stub').exists()).toBe(true)
    expect(wrapper.find('b-icon-stub').attributes('icon')).toEqual('sort-down')
  })

  it('dropdown which can be opened', async () => {
    await wrapper.setData({isHidden: false})
    expect(wrapper.find('div[data-test-id="product-selector-dropdown"]').exists()).toBe(true)
  })

  it('dropdown items', async () => {
    await wrapper.setData({isHidden: false})
    expect(wrapper.find('productlist-stub').exists()).toBe(true)
  })
})
