import {shallowMount} from '@vue/test-utils'
import cloudinary from './utils/cloudinary.mock'
import PromoModel from '@/components/FullWidthPromoModel'
import mock from '~/server/responses/bobcat-api/pages/components/promoModel'
import linkUtils from '~/plugins/utils/linkUtils'

const wrapper = shallowMount(PromoModel, {
  propsData: {
    htag: mock.main['0'].htag,
    marketingCampaigns: mock.main['0'].configuration
  },
  provide: {
    campaigns: mock.campaigns
  },
  mocks: {
    $nuxt: {
      context: {
        ...cloudinary,
        $utils: {
          ...cloudinary.$utils,
          ...linkUtils
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
  },
  stubs: {
    LazyHydrate: true,
    ImageSet: true
  }
})

const link = wrapper.find('a')

describe('PromoModel component [data props]', () => {
  it('has LazyHydrate component', () => {
    const lh = wrapper.find('lazyhydrate-stub')
    expect(lh.exists()).toBe(true)
  })

  it('has wrapper section', () => {
    expect(wrapper.find('section[data-test-id="full-width-promo-model"]').exists()).toBe(true)
  })

  it('has overlay', () => {
    expect(wrapper.find('div.promo-overlay').exists()).toBe(true)
  })

  it('has img', () => {
    expect(wrapper.find('imageset-stub').exists()).toBe(true)
  })

  it('has htag', () => {
    expect(wrapper.find(mock.main['0'].htag).exists()).toBe(true)
  })

  it('has <a/> tag with mocked label', () => {
    expect(link.text()).toEqual(mock.main['0'].configuration.link.link.customLabel)
  })

  it('<a/> tag has mocked href attribute', () => {
    expect(link.attributes().href).toBe(mock.main['0'].configuration.link.link.customUrl)
  })

  it('<a/> tag has mocked target attribute', () => {
    expect(link.attributes().target).toBe(mock.main['0'].configuration.link.link.target)
  })
})
