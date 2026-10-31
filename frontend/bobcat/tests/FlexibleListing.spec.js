import {shallowMount} from '@vue/test-utils'
import cloudinary from './utils/cloudinary.mock'
import FlexibleListing from '@/components/FlexibleListing'
import WarningAlert from '@/components/elements/WarningAlert'
import mock from '~/server/responses/bobcat-api/pages/components/flexiblelisting'
import linkUtils from '~/plugins/utils/linkUtils'

const props = mock.main['0']
const options = {
  propsData: {
    cards: props.cards,
    showChips: props.showChips,
    indicators: props.indicators,
    horizontal: props.horizontal,
    title: props.title,
    link: props.link,
    htag: props.htag,
    metadata: props.metadata
  },
  stubs: {
    'b-icon': true,
    ImageSet: true,
    LazyHydrate: true,
    ProductListingItem: true,
    WarningAlert
  },
  mocks: {
    ...cloudinary,
    $nuxt: {
      context: {
        $utils: {
          ...cloudinary.$utils,
          getLinkField: linkUtils.getLinkField
        },
        $regions: {
          current: {
            name: 'en',
            label: 'english',
            language: 'en'
          }
        },
        $ga4: {
          events: {
            flexible_listing_click: jest.fn()
          }
        },
        $services: {
          bobcatapi: {
            getConcepts: jest.fn().mockResolvedValue({})
          }
        }
      }
    }
  }
}

const getWrapper = (opt = options) => shallowMount(FlexibleListing, opt)
const wrapper = getWrapper()

describe('FlexibleListing component [data props]', () => {
  it('renders properly by default', () => {
    expect(wrapper.find('[data-test-uid^="flexible-listing__"]').exists()).toBe(true)
    expect(wrapper.find('[data-test-id="flexible-listing__cards"]').exists()).toBe(true)
    expect(wrapper.find('[data-test-id="flexible-listing__empty"]').exists()).toBe(false)
    expect(wrapper.findAll('[data-test-id^="flexible-listing-item__"]').length).toBe(3)
  })

  it('eager-loads the first 2 card images', () => {
    const cards = wrapper.findAll('productlistingitem-stub')
    expect(cards.at(0).attributes('preload')).toBe('true')
    expect(cards.at(1).attributes('preload')).toBe('true')
    expect(cards.at(2).attributes('preload')).toBeUndefined()
  })

  it('eager-loads every card when Magnolia Eager Load is set', () => {
    const flagged = getWrapper({
      ...options,
      propsData: {...options.propsData, eagerLoad: 'true'}
    })
    const cards = flagged.findAll('productlistingitem-stub')
    expect(cards.at(0).attributes('preload')).toBe('true')
    expect(cards.at(1).attributes('preload')).toBe('true')
    expect(cards.at(2).attributes('preload')).toBe('true')
  })

  it('renders error if no cards', async () => {
    const _wrapper = getWrapper({...options, propsData: {...options.propsData, cards: null}})
    await _wrapper.setData({inEditorEdit: true})
    await _wrapper.vm.$forceUpdate()
    expect(_wrapper.find('[data-test-id="flexible-listing__empty"]').exists()).toBe(true)
    expect(_wrapper.find('[data-test-id="flexible-listing__empty"]').text()).toEqual('No cards configured in component.')
  })
})
