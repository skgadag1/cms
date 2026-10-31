import {shallowMount} from '@vue/test-utils'
import BuildQuoteSelector from '@/components/BuildQuoteSelector'
import categoryMock from '~/server/responses/bobcat-api/products/categories'

const options = {
  propsData: {
    categories: {
      '@nodes': ['categories0', 'categories1'],
      categories0: {
        productCategory: categoryMock[0]['@id']
      },
      categories1: {
        productCategory: categoryMock[1]['@id']
      }
    },
    switchable: {
      field: 'automatic',
      automaticUrl: 'test'
    }
  },
  stubs: {
    'b-overlay': true,
    LazyHydrate: true,
    BuildAndQuoteCategoryItem: true,
    WarningAlert: true
  },
  mocks: {
    $fetchState: {pending: false},
    $nuxt: {
      context: {
        $services: {
          bobcatapi: {
            getCategories: () => {
              return [categoryMock[0], categoryMock[1]]
            }
          }
        }
      }
    }
  }
}

const getWrapper = (opt = options) => shallowMount(BuildQuoteSelector, opt)
const wrapper = getWrapper()

describe('BuildQuoteSelector component', () => {
  it('renders component without title', () => {
    expect(wrapper.find('[data-test-id="bq-selector__title"] h2').exists()).toBe(false)
  })

  it('renders component in default state', async () => {
    expect(wrapper.find('[data-test-id="bq-selector__categories"]').exists()).toBe(true)
    expect(wrapper.find('[data-test-id="bq-selector-error__nocategories"]').exists()).toBe(false)
    expect(wrapper.vm.allCategories.length).toEqual(2)
  })

  it('renders no category error', async () => {
    const _wrapper = getWrapper({
      ...options,
      propsData: {...options.propsData, categories: {}},
      mocks: {
        $fetchState: {pending: false},
        $nuxt: {
          context: {
            $services: {
              bobcatapi: {
                getCategories: () => {
                  return []
                }
              }
            }
          }
        }
      }
    })
    await _wrapper.setData({inEditorEdit: true})
    await _wrapper.vm.$forceUpdate()

    expect(_wrapper.find('[data-test-id="bq-selector__categories"]').exists()).toBe(false)
    expect(_wrapper.find('[data-test-id="bq-selector-error__nocategories"]').exists()).toBe(true)
  })

  it('pass global url', async () => {
    expect(wrapper.vm.bqUrl).toBe('test')
    const _wrapper = getWrapper({...options, propsData: {...options.propsData, switchable: {field: 'manual'}}})
    expect(_wrapper.vm.bqUrl).toBe(null)
  })
})
