import {shallowMount} from '@vue/test-utils'
import Breadcrumb from '@/components/Breadcrumb'
import mock from '~/server/responses/bobcat-api/pages/components/breadcrumb'
import pathUtils from '~/plugins/utils/path'

const _model = {id: 'M0115', '@name': 'test', overview: '<p class="test-class">Sth very mocked</p>'}

const mountBreadcrumb = (mobile = false) => shallowMount(Breadcrumb, {
  provide: {
    breadcrumbs: mock.breadcrumbs,
    model: _model
  },
  mocks: {
    $nuxt: {
      context: {
        $utils: {
          ...pathUtils,
          isSmallMobile: () => mobile,
          isMobile: () => mobile,
          isTablet: () => false,
          isDesktopLG: () => false,
          isDesktopXL: () => false
        },
        $breakpoints: {
          mobile
        },
        $regions: {
          current: {
            name: 'na',
            label: 'NA',
            language: {
              name: 'en'
            }
          }
        },
        $config: {},
        $services: {
          dealerlocator: {
            getDealerDetails: jest.fn()
          }
        }
      }
    },
    $router: {
      currentRoute: {
        path: '/na/en/equipment/custom-loaders/skid-steer/s740-skid-steer-loader'
      }
    }
  },
  stubs: {
    BBreadcrumb: true,
    BBreadcrumbItem: true,
    LazyHydrate: true
  }
})

const wrapper = mountBreadcrumb(false)
const list = mock.breadcrumbs

describe('Breadcrumb component [data props]', () => {
  it('has breadcrumb vue component', () => {
    expect(wrapper.find('bbreadcrumb-stub').exists()).toBe(true)
  })

  it('has breadcrumb item vue component', () => {
    expect(wrapper.find('bbreadcrumbitem-stub').exists()).toBe(true)
  })

  it('<a/> tag has mocked href attribute', () => {
    expect(wrapper.find('[data-test-id=breadcrumb-item__0]').attributes().href).toEqual(mock.breadcrumbs[0].url.endsWith('/') ? mock.breadcrumbs[0].url.slice(0, -1) : mock.breadcrumbs[0].url)
  })

  it('renders as many breadcrumbs as pages', () => {
    expect(wrapper.findAll('bbreadcrumbitem-stub').length).toEqual(list.length)
  })

  it('has desktop breadcrumb wrapper', () => {
    expect(wrapper.find('div[data-test-id="desktop-breadcrumb-wrapper"]').exists()).toBe(true)
  })

  it('hides desktop breadcrumbs on mobile', async () => {
    const mobileWrapper = mountBreadcrumb(true)
    mobileWrapper.vm.currentRoute = {text: 'Parent', href: '/na/en'}
    await mobileWrapper.vm.$nextTick()
    expect(mobileWrapper.find('div[data-test-id="desktop-breadcrumb-wrapper"]').exists()).toBe(false)
    expect(mobileWrapper.find('div[data-test-id="mobile-breadcrumb-wrapper"]').exists()).toBe(true)
  })
})
