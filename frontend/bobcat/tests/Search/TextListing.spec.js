import {shallowMount} from '@vue/test-utils'
import {BButton, BFormInput} from 'bootstrap-vue'
import ListingWithFiltering from '@/components/elements/ListingWithFiltering'
import TextListing from '@/components/TextListing'
import TextListingItem from '@/components/elements/TextListingItem'
import mockedFacets from '~/server/responses/bobcat-api/content/facets'
import mockedFullTextSearch from '~/server/responses/content-api/fulltextSearch'
import categoryMock from '~/server/responses/bobcat-api/products/categories'
import useConcepts from '~/helpers/composables/useConcepts'

const {generateConceptHint} = useConcepts()

const _props = {
  group: {
    field: 'pages'
  },
  perPage: 20,
  hideFilters: 'true',
  hideSorting: 'true',
  facet: {
    '@path': '/bobcat/na/pages/Buying-Resources'
  }
}

const mockedUpdate = jest.fn()

const options = {
  propsData: _props,
  provide: {
    campaigns: []
  },
  stubs: {
    'b-overlay': true,
    'b-collapse': true,
    'b-card-text': true,
    'b-form-checkbox-group': true,
    'b-form-checkbox': true,
    'b-pagination': true,
    'b-form': true,
    'b-card': true,
    'b-img-lazy': true,
    'b-button': BButton,
    'b-card-group': true,
    'b-card-body': true,
    'b-badge': true,
    'b-row': true,
    'b-col': true,
    'b-form-input': BFormInput,
    'b-icon': true,
    'b-form-group': true,
    'b-input-group': true,
    ImageSet: true,
    LazyHydrate: true,
    ListingSkeleton: true,
    SwitchDropdown: true,
    FacetsFilter: true,
    ListingWithFiltering,
    TextListingItem,
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
        $utils: {
          isMobile: () => false,
          getCloudinaryAsset: () => null
        },
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
            getListingResults: (group, facetCategory, category, subcategory, filtersString, sortingSelected, page, perPage, pageType, props) => {
              return {
                data: mockedFullTextSearch
              }
            }
          }
        },
        $regions: {
          current: {
            name: 'en',
            label: 'english',
            language: {
              name: 'en'
            }
          }
        }
      }
    }
  }
}

const getWrapper = (opt = options) => shallowMount(TextListing, opt)
const wrapper = getWrapper()

describe('TextListing component [data props]', () => {
  it('searches data on button click by calling method', async () => {
    const wrapper = await getWrapper({...options, methods: {updateResults: mockedUpdate}})
    expect(wrapper.vm.results.length).toEqual(0)
    const inputField = wrapper.find('[data-test-id="text-listing__input"]')
    expect(inputField.exists()).toBe(true)
    inputField.setValue('bobcat')
    await wrapper.vm.$nextTick()
    const searchButton = wrapper.find('[data-test-id="text-listing__search"]')
    expect(searchButton.exists()).toBe(true)
    await searchButton.trigger('click')
    expect(mockedUpdate).toHaveBeenCalled()
  })

  it('searches data on button click and showing results', async () => {
    expect(wrapper.vm.results.length).toEqual(0)
    const inputField = wrapper.find('[data-test-id="text-listing__input"]')
    expect(inputField.exists()).toBe(true)
    inputField.setValue('bobcat')
    wrapper.vm.updateResults({data: mockedFullTextSearch})
    await wrapper.vm.$nextTick()
    const searchButton = wrapper.find('[data-test-id="text-listing__search"]')
    expect(searchButton.exists()).toBe(true)
    await searchButton.trigger('click')
    expect(wrapper.find('[data-test-id^="text-listing__results"]').exists()).toBe(true)
  })

  it('clears result list on icon click', async () => {
    const inputField = wrapper.find('[data-test-id="text-listing__input"]')
    expect(inputField.exists()).toBe(true)
    inputField.setValue('bobcat')
    wrapper.vm.updateResults({data: mockedFullTextSearch})
    await wrapper.vm.$nextTick()
    const clearIcon = wrapper.find('[data-test-id="text-listing__clear"]')
    await clearIcon.trigger('click')
    await wrapper.vm.$nextTick()
    expect(wrapper.find('[data-test-id^="text-listing-result-item__"]').exists()).toBe(false)
  })
})
