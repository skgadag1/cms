import {shallowMount} from '@vue/test-utils'
import cloudinary from './utils/cloudinary.mock'
import ProductsNavigation from '@/components/ProductsNavigation'
import mock from '~/server/responses/bobcat-api/pages/components/productsNavigation'
import linkUtils from '~/plugins/utils/linkUtils'

const productsProps = mock.main['0']

const wrapper = shallowMount(ProductsNavigation, {
  propsData: {
    items: productsProps.items,
    title: productsProps.title,
    htag: productsProps.htag
  },
  mocks: {
    $nuxt: {
      context: {
        $utils: {
          getLinkField: linkUtils.getLinkField,
          ...cloudinary.$utils
        },
        $regions: {
          current: {
            name: 'en',
            label: 'english',
            language: {
              name: 'en'
            }
          }
        },
        $ga4: {
          events: {
            product_navigation_click: jest.fn()
          }
        },
        $config: {
          USE_LEGACY_I18N_STRUCTURE: false
        }
      }
    }
  },
  provide: {
    model: {}
  },
  stubs: {
    LazyHydrate: true,
    ImageSet: true,
    'b-icon': true,
    'b-img': true
  }
})

const navItems = productsProps.items
  ? productsProps.items['@nodes'].map((x) => {
    const _sItem = productsProps.items[x]

    const item = {
      '@id': _sItem['@id'],
      link: linkUtils.getLinkField(_sItem?.link, 'en') || null,
      icon: _sItem?.asset?.icon || null,
      img: {
        src:
            cloudinary.$utils.getCloudinaryAsset(_sItem?.asset?.src, true, {
              crop: 'fill',
              width: 486,
              height: 486
            }) || null,
        alt: _sItem?.asset?.alt,
        title: _sItem?.asset?.title
      }
    }

    return item
  })
  : []

describe('Products Navigation component', () => {
  it('has LazyHydrate component', () => {
    const lh = wrapper.find('lazyhydrate-stub')
    expect(lh.exists()).toBe(true)
  })

  it('has section wrapper', () => {
    expect(wrapper.find('div[data-test-id="products-navigation"]').exists()).toBe(true)
  })

  it('has title with mocked label', () => {
    expect(wrapper.find('h2[data-test-id="products-navigation-title"]').text()).toEqual(productsProps.title)
  })

  it('renders nav items', () => {
    expect(wrapper.findAll('a[data-test-id="products-navigation-item"]').length).toEqual(navItems.length)
  })
})

describe('Navigation item', () => {
  it('exists', () => {
    expect(wrapper.find('a[data-test-id="products-navigation-item"]').exists()).toBe(true)
  })

  it('has arrow icon on mobile', () => {
    expect(wrapper.find('a[data-test-id="products-navigation-item"] b-icon-stub').exists()).toBe(true)
  })

  it('has specific icon', () => {
    expect(wrapper.find('a[data-test-id="products-navigation-item"] b-icon-stub[data-test-id="item-icon"]').exists()).toBe(true)
  })

  it('has title', () => {
    expect(wrapper.find('a[data-test-id="products-navigation-item"] h6').text()).toEqual(navItems[0].link.label)
  })
})
