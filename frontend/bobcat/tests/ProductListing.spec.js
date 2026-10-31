import {shallowMount} from '@vue/test-utils'
import ProductListing from '@/components/ProductListing'

const results = Array.from({length: 6}, (_, i) => ({
  name: `Model ${i}`,
  '@id': `id-${i}`,
  shortDescription: '',
  keyProperties: []
}))

const mountListing = (extra = {}) => shallowMount(ProductListing, {
  propsData: {
    title: 'Models',
    ...extra.propsData
  },
  provide: {
    campaigns: {value: []}
  },
  mocks: {
    $nuxt: {
      context: {
        $utils: {
          isMobile: () => false
        },
        $regions: {
          current: {
            name: 'en',
            language: {name: 'en'}
          }
        },
        $ga4: {
          events: {
            product_compare_click: jest.fn(),
            product_compare_cta: jest.fn()
          }
        },
        $services: {
          bobcatapi: {
            getConcepts: jest.fn().mockResolvedValue({})
          }
        },
        $breakpoints: {
          mobile: extra.mobile || false
        }
      }
    }
  },
  stubs: {
    ListingWithFiltering: {
      name: 'ListingWithFiltering',
      template: '<div><slot name="results" /><slot name="extra" /></div>'
    },
    PromoModel: true,
    ProductListingItem: true,
    RegionalizedProductListingItem: true,
    CompareStripe: true,
    CompareTableWrapper: true,
    SwitchDropdown: true
  }
})

describe('ProductListing eager images', () => {
  const loadResults = async (wrapper) => {
    await wrapper.vm.updateResults({page: 1, results})
    await wrapper.vm.$nextTick()
  }

  it('eager-loads the first 4 cards on desktop', async () => {
    const wrapper = mountListing()
    await loadResults(wrapper)
    const cards = wrapper.findAll('productlistingitem-stub')
    expect(cards.length).toBe(6)
    expect(cards.at(0).attributes('preload')).toBe('true')
    expect(cards.at(3).attributes('preload')).toBe('true')
    expect(cards.at(4).attributes('preload')).toBeUndefined()
  })

  it('eager-loads the first 2 cards on mobile', async () => {
    const wrapper = mountListing({mobile: true})
    wrapper.vm.isMobile = true
    await loadResults(wrapper)
    const cards = wrapper.findAll('productlistingitem-stub')
    expect(cards.at(0).attributes('preload')).toBe('true')
    expect(cards.at(1).attributes('preload')).toBe('true')
    expect(cards.at(2).attributes('preload')).toBeUndefined()
  })

  it('keeps the above-the-fold eager count when Eager Load is flagged', async () => {
    const wrapper = mountListing({propsData: {eagerLoad: 'true'}})
    await loadResults(wrapper)
    const cards = wrapper.findAll('productlistingitem-stub')
    expect(cards.at(0).attributes('preload')).toBe('true')
    expect(cards.at(3).attributes('preload')).toBe('true')
    expect(cards.at(4).attributes('preload')).toBeUndefined()
  })
})
