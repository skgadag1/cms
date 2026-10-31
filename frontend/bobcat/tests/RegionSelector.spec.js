import {shallowMount} from '@vue/test-utils'
import cloudinary from './utils/cloudinary.mock'
import RegionSelector from '@/components/RegionSelector'
import mock from '~/server/responses/bobcat-api/pages/components/regionSelector'
import linkUtils from '~/plugins/utils/linkUtils'

const props = mock.main['0']

const wrapper = shallowMount(RegionSelector, {
  propsData: {
    title: props.title,
    htag: props.htag,
    logo: props.logo,
    footer: props.footer,
    regions: props.regions,
    htagCaption: props.htagCaption,
    caption: props.caption,
    cards: props.cards
  },
  mocks: {
    $nuxt: {
      context: {
        $utils: {
          ...cloudinary.$utils,
          getLinkField: linkUtils.getLinkField
        }
      }
    }
  },
  stubs: {
    LazyHydrate: true,
    'b-icon': true,
    'b-img-lazy': true
  }
})

const mappedRegions = props.regions
  ? props.regions['@nodes']?.map(x => ({
    ...props.regions[x],
    asset: cloudinary.$utils.getCloudinaryAsset(props.regions[x]?.asset, true, {width: 382}),
    links: props.regions[x]?.links
      ? props.regions[x]?.links['@nodes']?.map(link => linkUtils.getLinkField(props.regions[x]?.links[link].link))
      : []
  }))
  : []

describe('Region Selector component [data props]', () => {
  it('has wrapper region-selector', () => {
    expect(wrapper.find('section[data-test-id="region-selector"]').exists()).toBe(true)
  })

  it('has nav', () => {
    expect(wrapper.find('div[data-test-id="region-selector-nav"]').exists()).toBe(true)
  })

  it('nav has logo with mocked src, alt and title', () => {
    expect(wrapper.find('div[data-test-id="region-selector-nav"] img').attributes('src')).toEqual(cloudinary.$utils.getCloudinaryAsset(props.logo.src, true, {height: 38}))
    expect(wrapper.find('div[data-test-id="region-selector-nav"] img').attributes('alt')).toEqual(props.logo.imageAlt)
    expect(wrapper.find('div[data-test-id="region-selector-nav"] img').attributes('title')).toEqual(props.logo.imageTitle)
  })

  it('nav has title', () => {
    expect(wrapper.find('div[data-test-id="region-selector-nav"] h2').text()).toEqual(props.title)
  })

  it('has caption', () => {
    expect(wrapper.find('h6[data-test-id="region-selector-caption"]').text()).toEqual(props.caption)
  })

  it('renders regions', () => {
    expect(wrapper.findAll('div[data-test-id="region-selector-grid-item"]').length).toEqual(mappedRegions.length)
  })

  it('region has languages (links)', () => {
    expect(wrapper.find('div[data-test-id="region-selector-grid-item"]').findAll('a').length).toEqual(mappedRegions[0].links.length)
  })

  it('region <a/> tag has mocked label', () => {
    expect(wrapper.find('div[data-test-id="region-selector-grid-item"] a').text()).toEqual(mappedRegions[0].links[0].label)
  })

  it('region <a/> tag has mocked href attribute', () => {
    expect(wrapper.find('div[data-test-id="region-selector-grid-item"] a').attributes().href).toEqual(mappedRegions[0].links[0].url)
  })

  it('region <a/> tag has mocked target attribute', () => {
    expect(wrapper.find('div[data-test-id="region-selector-grid-item"] a').attributes().target).toEqual(mappedRegions[0].links[0].target)
  })

  it('region <a/> tag has mocked rel attribute', () => {
    expect(wrapper.find('div[data-test-id="region-selector-grid-item"] a').attributes().rel).toEqual(mappedRegions[0].links[0].target === '_blank' ? 'noopener nofollow' : undefined)
  })

  it('has LazyHydrate component', () => {
    expect(wrapper.find('lazyhydrate-stub').exists()).toBe(true)
  })
})
