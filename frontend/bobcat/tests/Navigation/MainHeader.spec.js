import {shallowMount} from '@vue/test-utils'
import cloudinary from '../utils/cloudinary.mock'
import MainHeader from '@/components/MainHeader'
import MegaMenu from '@/components/elements/Navigation/MegaMenu'
import mock from '~/server/responses/bobcat-api/pages/components/mainheader'
import _navigationMock from '~/server/responses/content-api/navigationResponse'
import devicesUtils from '~/plugins/utils/devices'
import linkUtils from '~/plugins/utils/linkUtils'
import pathUtils from '~/plugins/utils/path'
import {BUILD_AND_PRICE_PAGE_TEMPLATE_NAME} from '~/constants/pageTemplates'

const mainHeaderProps = mock.header['0']

const regionSelectorLink = {
  id: 'a48462d7-3cc7-45e9-9e11-71f938d5b20e',
  field: 'custom',
  link: {
    customUrl: '/na/en',
    customLabel: 'North America (English)',
    target: '_self'
  },
  '@nodes': ['link']
}

const defaultOptions = {
  propsData: {
    logo: mainHeaderProps.logo,
    topLinks: mainHeaderProps.topLinks
  },
  mocks: {
    $nuxt: {
      context: {
        ...cloudinary,
        $utils: {
          ...linkUtils,
          ...pathUtils,
          ...devicesUtils,
          ...cloudinary.$utils
        },
        $services: {
          bobcatapi: {
            fetchNavigation: () => { return _navigationMock }
          }
        },
        $regions: {
          current: {
            language: {
              name: 'en'
            },
            name: 'na'
          }
        },
        $breakpoints: {
          mobile: true,
          tablet: false,
          desktop: true
        }
      }
    },
    $router: {
      currentRoute: {
        path: '/bobcat/na/equipment/subpage/Compact-Track-Loaders'
      }
    }
  },
  directives: {
    'b-toggle': jest.fn()
  },
  stubs: {
    MegaMenu,
    LazyHydrate: true,
    SearchInput: true,
    SubnavigationMain: true,
    SubnavigationMobile: true,
    'b-navbar-brand': true,
    'b-icon': true,
    'b-sidebar': true
  }
}

const getWrapper = (_props = {}, _mocks = {}, _provide = {}) => shallowMount(MainHeader, {...defaultOptions, propsData: {...defaultOptions.propsData, ..._props}, mocks: {...defaultOptions.mocks, ..._mocks}, provide: _provide})

const desktopMocks = {
  $nuxt: {
    context: {
      ...defaultOptions.mocks.$nuxt.context,
      $breakpoints: {mobile: false, tablet: false, desktop: true}
    }
  }
}

const getDesktopWrapper = () => getWrapper({}, desktopMocks)

const getCollapsedNavWrapper = () => getWrapper({}, {}, {pageTemplateId: BUILD_AND_PRICE_PAGE_TEMPLATE_NAME})

const getCollapsedNavDesktopWrapper = () => getWrapper({}, desktopMocks, {pageTemplateId: BUILD_AND_PRICE_PAGE_TEMPLATE_NAME})

const hamburger = _wrapper => _wrapper.find('button[aria-label="Toggle navigation"]')

const wrapper = getWrapper()
const logo = cloudinary.$utils.getCloudinaryAsset(mainHeaderProps.logo.src, true, {height: 152, crop: 'scale'})
const toplinks = mainHeaderProps.topLinks
  ? mainHeaderProps.topLinks['@nodes'].map(x => ({
    ...linkUtils.getLinkField(mainHeaderProps.topLinks[x].link),
    icon: mainHeaderProps.topLinks[x].icon
  }))
  : []

describe('MainHeader component [data props]', () => {
  it('has LazyHydrate component', () => {
    const lh = wrapper.find('lazyhydrate-stub')
    expect(lh.exists()).toBe(true)
  })

  it('has logo', () => {
    expect(wrapper.find('b-navbar-brand-stub img').exists()).toBe(true)
  })

  it('logo has src attribute', () => {
    expect(wrapper.find('b-navbar-brand-stub img').attributes('src')).toEqual(logo)
  })

  it('logo has title attribute', () => {
    expect(wrapper.find('b-navbar-brand-stub img').attributes('title')).toEqual(mainHeaderProps.logo.imageTitle)
  })

  it('logo has alt attribute', () => {
    expect(wrapper.find('b-navbar-brand-stub img').attributes('alt')).toEqual(mainHeaderProps.logo.imageAlt)
  })

  it('logo is an LCP image with eager loading and high fetchpriority', () => {
    const logoImg = wrapper.find('b-navbar-brand-stub img')
    expect(logoImg.attributes('loading')).toBe('eager')
    expect(logoImg.attributes('fetchpriority')).toBe('high')
    expect(logoImg.attributes('loading')).not.toBe('lazy')
  })

  it('has mobile region selector', () => {
    const _wrapper = getWrapper({regionLink: regionSelectorLink})
    expect(_wrapper.find('div[data-test-id="region-selector-picker"]').exists()).toBe(true)
    expect(_wrapper.find('a[data-test-id="region-selector-picker__link"]').exists()).toBe(true)
  })

  it('region selector has globe icon', () => {
    const _wrapper = getWrapper({regionLink: regionSelectorLink})
    expect(_wrapper.find('div[data-test-id="region-selector-picker"] b-icon-stub').attributes('icon')).toEqual('globe')
  })

  it('has nav links', () => {
    expect(wrapper.findAll('ul[data-test-id="main-header-sub-nav-links"] li').length).toEqual(toplinks.length)
    const _wrapper = getWrapper({regionLink: regionSelectorLink})
    expect(_wrapper.findAll('ul[data-test-id="main-header-sub-nav-links"] li').length).toEqual(toplinks.length + 1)
  })

  it('has mobile button to toggle search ', () => {
    expect(wrapper.find('button[data-test-id="main-header-toggle-search"]').exists()).toBe(true)
  })

  it('button to toggle search has search icon ', () => {
    expect(wrapper.find('button[data-test-id="main-header-toggle-search"] b-icon-stub').attributes('icon')).toEqual('search')
  })

  it('has proper search inputs for mobile, tablet and desktop ', async () => {
    const _wrapper = getWrapper()
    _wrapper.setData({_mobile: true, _tablet: true, _desktop: false})
    expect(_wrapper.findAll('searchinput-stub').length).toEqual(2)
    expect(_wrapper.find('[data-test-id=main-header-mobile-search] searchinput-stub').isVisible()).toEqual(true)

    _wrapper.setData({_mobile: true, _tablet: false, _desktop: false})
    expect(_wrapper.findAll('searchinput-stub').length).toEqual(2)
    expect(_wrapper.find('[data-test-id=main-header-mobile-search] searchinput-stub').isVisible()).toEqual(true)

    _wrapper.setData({_mobile: false, _tablet: false, _desktop: true})
    expect(_wrapper.findAll('searchinput-stub').length).toEqual(2)
    expect(_wrapper.find('[data-test-id=main-header-search] searchinput-stub').isVisible()).toEqual(true)
  })

  it('has first mobile nav link', () => {
    expect(wrapper.find('div[data-test-id="main-header-sub-nav-mobile-link-1"]').exists()).toBe(true)
  })

  it('first mobile nav link has tag with mocked label', () => {
    expect(wrapper.find('div[data-test-id="main-header-sub-nav-mobile-link-1"] a span').text()).toEqual(toplinks[1].label)
  })

  it('first mobile nav link tag has mocked href attribute', () => {
    expect(wrapper.find('div[data-test-id="main-header-sub-nav-mobile-link-1"] a').attributes('href')).toEqual(toplinks[1].url)
  })

  it('first mobile nav link tag has mocked target attribute', () => {
    expect(wrapper.find('div[data-test-id="main-header-sub-nav-mobile-link-1"] a').attributes('target')).toEqual(toplinks[1].target)
  })

  it('first mobile nav link tag has mocked icon', () => {
    expect(wrapper.find('div[data-test-id="main-header-sub-nav-mobile-link-1"] b-icon-stub').attributes('icon')).toEqual(toplinks[1].icon)
  })

  it('has second mobile nav link', () => {
    expect(wrapper.find('div[data-test-id="main-header-sub-nav-mobile-link-0"]').exists()).toBe(true)
  })

  it('second mobile nav link has tag with mocked label', () => {
    expect(wrapper.find('div[data-test-id="main-header-sub-nav-mobile-link-0"] a span').text()).toEqual(toplinks[0].label)
  })

  it('second mobile nav link tag has mocked href attribute', () => {
    expect(wrapper.find('div[data-test-id="main-header-sub-nav-mobile-link-0"] a').attributes('href')).toEqual(toplinks[0].url)
  })

  it('second mobile nav link tag has mocked target attribute', () => {
    expect(wrapper.find('div[data-test-id="main-header-sub-nav-mobile-link-0"] a').attributes('target')).toEqual(toplinks[0].target)
  })

  it('second mobile nav link tag has mocked icon', () => {
    expect(wrapper.find('div[data-test-id="main-header-sub-nav-mobile-link-0"] b-icon-stub').attributes('icon')).toEqual(toplinks[0].icon)
  })

  it('has subnavigation for desktop', () => {
    expect(getDesktopWrapper().find('subnavigationmain-stub').exists()).toBe(true)
  })

  it('has subnavigation for mobile', () => {
    expect(wrapper.find('subnavigationmobile-stub').exists()).toBe(true)
  })

  it('has header space for fixed menu', () => {
    expect(wrapper.find('div.header-space').exists()).toBe(true)
  })
})

describe('MainHeader component [collapsed navigation template]', () => {
  it('renders both navbars on a standard page template', () => {
    expect(getDesktopWrapper().find('subnavigationmain-stub').exists()).toBe(true)
    expect(wrapper.find('subnavigationmobile-stub').exists()).toBe(true)
  })

  it('keeps the hamburger mobile-only on a standard page template', () => {
    expect(hamburger(wrapper).classes()).toContain('d-lg-none')
  })

  it('keeps the black strips on a standard page template', () => {
    expect(wrapper.find('nav[data-test-id="main-header-mobile-region-selector"]').exists()).toBe(true)
    expect(wrapper.find('div[data-test-id="main-header-sub-nav-mobile-link-0"]').exists()).toBe(true)
  })

  it('hides the secondary navbar until the hamburger is clicked on build & price', () => {
    expect(getCollapsedNavDesktopWrapper().find('subnavigationmain-stub').exists()).toBe(false)
  })

  it('shows the hamburger at every breakpoint on build & price', () => {
    expect(hamburger(getCollapsedNavWrapper()).classes()).not.toContain('d-lg-none')
  })

  it('drops the region strip and the top-link row on build & price', () => {
    const _wrapper = getCollapsedNavWrapper()
    expect(_wrapper.find('nav[data-test-id="main-header-mobile-region-selector"]').exists()).toBe(false)
    expect(_wrapper.find('div[data-test-id="main-header-sub-nav-mobile-link-0"]').exists()).toBe(false)
  })

  it('hands the dropped links to the hamburger panel on build & price', () => {
    const sidebar = getCollapsedNavWrapper().find('subnavigationmobile-stub')
    expect(sidebar.exists()).toBe(true)
    expect(sidebar.attributes('quick-links')).toBeTruthy()
  })

  it('reveals the secondary navbar when the hamburger is clicked on build & price', async () => {
    const _wrapper = getCollapsedNavDesktopWrapper()
    expect(_wrapper.find('subnavigationmain-stub').exists()).toBe(false)
    expect(hamburger(_wrapper).find('b-icon-stub').attributes('icon')).toEqual('menu')

    await hamburger(_wrapper).trigger('click')
    expect(_wrapper.find('subnavigationmain-stub').exists()).toBe(true)
    expect(hamburger(_wrapper).find('b-icon-stub').attributes('icon')).toEqual('times')

    await hamburger(_wrapper).trigger('click')
    expect(_wrapper.find('subnavigationmain-stub').exists()).toBe(false)
  })

  it('narrows the search column so the hamburger does not wrap the bar on build & price', () => {
    expect(wrapper.find('[data-test-id="main-header-search"]').classes()).toContain('col-md-4')
    expect(getCollapsedNavWrapper().find('[data-test-id="main-header-search"]').classes()).toContain('col-md-3')
  })

  it('marks the header spacer so it can drop the secondary navbar height on build & price', () => {
    expect(getCollapsedNavWrapper().find('div.header-space').classes()).toContain('header-space--collapsed-nav')
    expect(wrapper.find('div.header-space').classes()).not.toContain('header-space--collapsed-nav')
  })
})
