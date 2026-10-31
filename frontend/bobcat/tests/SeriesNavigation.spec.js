import {shallowMount} from '@vue/test-utils'
import cloudinary from './utils/cloudinary.mock'
import SeriesNavigation from '@/components/SeriesNavigation'
import mock from '~/server/responses/bobcat-api/pages/components/seriesNavigation'
import linkUtils from '~/plugins/utils/linkUtils'

const props = mock.main['0']
const wrapper = shallowMount(SeriesNavigation, {
  propsData: {
    gridTitle: props.gridTitle,
    cards: props.cards
  },
  mocks: {
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
        }
      }
    }
  },
  stubs: {
    LazyHydrate: true,
    ImageSet: true
  }
})

const mappedCards = props?.cards
  ? props?.cards['@nodes']?.map(x => ({
    ...props.cards[x],
    asset: {
      alt: props.cards[x]?.asset?.imageAlt,
      src: cloudinary.$utils.getCloudinaryAsset(props.cards[x]?.asset?.src, true, {crop: 'fill'})
    },
    links: props.cards[x]?.links
      ? props.cards[x]?.links['@nodes']?.map(link => linkUtils.getLinkField(props.cards[x]?.links[link].link))
      : [],
    points: props.cards[x]?.points
      ? props.cards[x]?.points['@nodes']?.map(point => props.cards[x]?.points[point])
      : []
  }))
  : []

describe('Series Navigation component [elements]', () => {
  it('has wrapper series-navigation', () => {
    expect(wrapper.find('section[data-test-id="series-navigation"]').exists()).toBe(true)
  })

  it('renders cards', () => {
    expect(wrapper.findAll('div[data-test-id="series-navigation-card"]').length).toEqual(mappedCards.length)
  })

  it('has new indicator when card has that prop and it is set to true', async () => {
    await wrapper.setData({allCards: [{...mappedCards[0], newIndicator: 'true'}]})
    expect(wrapper.find('div[data-test-id="series-navigation-card-indicator"]').exists()).toBe(true)
  })

  it('card has a title', () => {
    expect(wrapper.find('h4[data-test-id="series-navigation-card-title"]').text()).toEqual(mappedCards[0].title)
  })

  it('card has a subtitle', () => {
    expect(wrapper.find('h5[data-test-id="series-navigation-card-subtitle"]').text()).toEqual(mappedCards[0].subtitle)
  })

  it('card has an image', () => {
    expect(wrapper.find('imageset-stub[data-test-id="series-navigation-card-img"]').exists()).toBe(true)
  })

  it('image has an alt', () => {
    expect(wrapper.find('imageset-stub[data-test-id="series-navigation-card-img"]').attributes('alt')).toEqual(mappedCards[0].asset.alt)
  })

  it('image has an title', () => {
    expect(wrapper.find('imageset-stub[data-test-id="series-navigation-card-img"]').attributes('title')).toEqual(mappedCards[0].asset.imageTitle)
  })

  it('image has a src', () => {
    expect(wrapper.find('imageset-stub[data-test-id="series-navigation-card-img"]').attributes('src')).toEqual(mappedCards[0].asset.src)
  })

  it('card has links', () => {
    expect(wrapper.findAll('div[data-test-id="series-navigation-card-links"] a').length).toEqual(mappedCards[0].links.length)
  })

  it('card <a/> tag has mocked label', () => {
    expect(wrapper.find('div[data-test-id="series-navigation-card-links"] a').text()).toEqual(mappedCards[0].links[0].label)
  })

  it('card <a/> tag has mocked href attribute', () => {
    expect(wrapper.find('div[data-test-id="series-navigation-card-links"] a').attributes().href).toEqual(mappedCards[0].links[0].url)
  })

  it('card <a/> tag has mocked target attribute', () => {
    expect(wrapper.find('div[data-test-id="series-navigation-card-links"] a').attributes().target).toEqual(mappedCards[0].links[0].target)
  })

  it('card <a/> tag has mocked rel attribute', () => {
    expect(wrapper.find('div[data-test-id="series-navigation-card-links"] a').attributes().rel).toEqual(mappedCards[0].links[0].target === '_blank' ? 'noopener nofollow' : undefined)
  })

  it('card has points', () => {
    expect(wrapper.findAll('ul[data-test-id="series-navigation-card-points"] li').length).toEqual(mappedCards[0].points.length)
  })

  it('has LazyHydrate component', () => {
    expect(wrapper.find('lazyhydrate-stub').exists()).toBe(true)
  })
})
