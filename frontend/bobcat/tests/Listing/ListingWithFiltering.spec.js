import {shallowMount} from '@vue/test-utils'
import ListingWithFiltering from '@/components/elements/ListingWithFiltering'
import mockedFacets from '~/server/responses/bobcat-api/content/facets'
import mockedProductSearch from '~/server/responses/content-api/productSearch'
import categoryMock from '~/server/responses/bobcat-api/products/categories'
import useConcepts from '~/helpers/composables/useConcepts'

const {generateConceptHint} = useConcepts()

const _props = {
  group: {
    field: 'product',
    productCategory: {
      '@path': '/bobcat/na/Imported'
    }
  },
  perPage: 20,
  title: 'Test',
  hideFilters: false,
  hideSorting: false,
  hideView: false,
  facet: {
    '@path': '/bobcat/na/products/Common'
  }
}

const options = {
  propsData: _props,
  stubs: {
    'b-overlay': true,
    'b-collapse': true,
    'b-card-text': true,
    'b-form': true,
    'b-form-checkbox-group': true,
    'b-form-checkbox': true,
    'b-form-input': true,
    'b-button': true,
    'b-pagination': true,
    'b-card': true,
    'b-card-group': true,
    LazyHydrate: true,
    SwitchDropdown: true,
    FacetsFilter: true,
    FacetsChips: true,
    ListingSkeleton: true,
    WarningAlert: true
  },
  directives: {
    'b-toggle': () => {}
  },
  data() { // to hack api call from fetch
    return {
      generateConceptHint
    }
  },
  mocks: {
    $fetchState: {pending: false},
    $nuxt: {
      context: {
        $services: {
          bobcatapi: {
            getFacets: (path) => {
              return {
                data: mockedFacets
              }
            },
            getConcepts: () => {
              return {}
            },
            getCategories: () => {
              return categoryMock
            }
          },
          content: {
            getListingResults: () => {
              return {
                data: mockedProductSearch
              }
            }
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

const getWrapper = (opt = options) => shallowMount(ListingWithFiltering, opt)
const wrapper = getWrapper()

describe('ProductListing component [data props]', () => {
  it('shows/hides facets based on prop', async () => {
    expect(wrapper.find('[data-test-id="listing-facets"]').exists()).toBe(true)
    await wrapper.setProps({hideFilters: true})
    expect(wrapper.find('[data-test-id="listing-facets"]').exists()).toBe(false)
  })

  it('displays proper results count', async () => {
    const count = wrapper.find('[data-test-id="listing-results__count"]')
    expect(count.text()).toEqual('' + mockedProductSearch.total)
  })

  it('populates list of sorting elements', async () => {
    const sortingOptions = wrapper.find('[data-test-id="listing-sorting"]').attributes('options')
    expect(sortingOptions.length).toBeGreaterThan(0)
  })

  it('displays pagination', async () => {
    const pagination = wrapper.find('[data-test-id="listing-pagination"]')
    expect(pagination.exists()).toBe(true)
  })

  it('hide pagination on 1 page', async () => {
    await wrapper.setData({perPage: '45'})
    const pagination = wrapper.find('[data-test-id="listing-pagination"]')
    expect(pagination.exists()).toBe(false)
  })

  it('shows/hides sorting based on prop', async () => {
    expect(wrapper.find('[data-test-id="listing-sorting"]').exists()).toBe(true)
    await wrapper.setProps({hideSorting: true})
    expect(wrapper.find('[data-test-id="listing-sorting"]').exists()).toBe(false)
  })

  it('shows/hides pagination view based on prop', async () => {
    wrapper.setData({total: 10})
    expect(wrapper.find('[data-test-id="listing-perPage"]').exists()).toBe(true)
    await wrapper.setProps({hideView: true})
    expect(wrapper.find('[data-test-id="listing-perPage"]').exists()).toBe(false)
  })
})
