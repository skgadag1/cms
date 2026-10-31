import {shallowMount} from '@vue/test-utils'
import cloudinary from '../utils/cloudinary.mock'
import ProductListingItem from '@/components/elements/ProductListingItem'
import mock from '~/server/responses/bobcat-api/pages/components/flexiblelisting'
import pathUtils from '~/plugins/utils/path'
import linkUtils from '~/plugins/utils/linkUtils'

const _props = mock.main['0']
const _card = mock.main['0'].cards.cards0

const options = {
  propsData: {
    item: _card,
    horizontal: true,
    showIndicator: _props.showIndicator === 'true',
    showChips: _props.showChips === 'true',
    isCarouselCard: _props.isCarouselCard === 'true'
  },
  stubs: {
    'b-card': true,
    'b-card-body': true,
    'b-card-header': true,
    'b-card-text': true,
    'b-card-title': true,
    'b-col': true,
    'b-row': true,
    'b-icon': true,
    'b-badge': true,
    ImageSet: true,
    LazyHydrate: true
  },
  mocks: {
    $nuxt: {
      context: {
        ...cloudinary,
        $utils: {
          ...cloudinary.$utils,
          ...cloudinary,
          ...linkUtils,
          ...pathUtils
        },
        $regions: {
          current: {
            name: 'en',
            label: 'english',
            locale: 'en-us',
            language: 'en'
          }
        }
      }
    }
  }
}

const getWrapper = (opt = options) => shallowMount(ProductListingItem, opt)

describe('ProductListingItem component [data props]', () => {
  const wrapper = getWrapper()

  it('is rendered', async () => {
    const wrapper = getWrapper()
    expect(wrapper.find('.card-horizontal').exists()).toBe(true)
    await wrapper.setProps({horizontal: false})
    await wrapper.vm.$nextTick()
    expect(wrapper.find('.card-horizontal').exists()).toBe(false)
    expect(wrapper.find('[data-test-id="product-listing-item-title"]').exists()).toBe(true)
    expect(wrapper.find('[data-test-id="product-listing-item-title"]').text()).toEqual(_card.title)
  })

  it('renders chips', async () => {
    await wrapper.setProps({isCarouselCard: false})
    await wrapper.vm.$nextTick()
    expect(wrapper.findAll('[data-test-id^="product-listing-item-badge-"]').length).toEqual(1)
    await wrapper.setProps({isCarouselCard: true})
    await wrapper.vm.$nextTick()
    expect(wrapper.findAll('[data-test-id^="content-carousel-item-badge-"]').length).toEqual(1)
  })

  it('placeholder image rendered', async () => {
    const wrapper = getWrapper()

    expect(wrapper.findAll('[data-test-id="product-listing-item__image"]').length).toEqual(1)
    await wrapper.setData({imageUrl: null})
    expect(wrapper.findAll('[data-test-id="product-listing-item__image"]').length).toEqual(1)
  })

  it('visually hides the duplicate rich-text title inside the stretched link', () => {
    const evolutionTitle = '<h4>Rich-text listing title</h4>'
    const wrapper = getWrapper({
      ...options,
      propsData: {...options.propsData, item: {..._card, evolutionTitle, url: '/rich-text-listing'}}
    })
    const linkTitle = wrapper.find('.stretched-link > .sr-only')

    expect(linkTitle.exists()).toBe(true)
    expect(linkTitle.html()).toContain(evolutionTitle)
    expect(wrapper.find('[data-test-id="product-listing-item-evolution-title"]').classes()).not.toContain('sr-only')
  })

  it('visually hides the duplicate fallback title inside the stretched link', () => {
    const title = 'Fallback listing title'
    const wrapper = getWrapper({
      ...options,
      propsData: {...options.propsData, item: {..._card, evolutionTitle: null, title}}
    })
    const linkTitle = wrapper.find('.stretched-link > .sr-only')

    expect(linkTitle.exists()).toBe(true)
    expect(linkTitle.text()).toBe(title)
    expect(wrapper.find('[data-test-id="product-listing-item-title"]').classes()).not.toContain('sr-only')
  })
})

describe('ProductListingItem vertical', () => {
  const wrapper = getWrapper({...options, propsData: {...(options.propsData), horizontal: false}})

  it('renders new indicator when present and on', async () => {
    await wrapper.setProps({isCarouselCard: false, showIndicator: true})
    expect(wrapper.find('[data-test-id="product-listing-item-new-indicator"]').exists()).toBe(true)
  })

  it('renders video indicator when present and on', async () => {
    await wrapper.setProps({showIndicator: true, videoIndicator: true})
    expect(wrapper.find('[data-test-id="product-listing-item-video-indicator"]').exists()).toBe(true)
  })

  it('not renders video indicator when not present and on', async () => {
    const wrapper = getWrapper({...options, propsData: {...(options.propsData), horizontal: false, item: {..._card, videoIndicator: false}}})
    expect(wrapper.find('[data-test-id="product-listing-item-video-indicator"]').exists()).toBe(false)
  })

  it('not renders new indicator when not present and on', async () => {
    const wrapper = getWrapper({...options, propsData: {...(options.propsData), horizontal: false, item: {..._card, newIndicator: false}}})
    expect(wrapper.find('[data-test-id="product-listing-item-new-indicator"]').exists()).toBe(false)
  })

  it('not renders indicators when turned off', async () => {
    const wrapper = getWrapper({...options, propsData: {...(options.propsData), showIndicator: false}})
    expect(wrapper.find('[data-test-id="product-listing-item-video-indicator"]').exists()).toBe(false)
    expect(wrapper.find('[data-test-id="product-listing-item-new-indicator"]').exists()).toBe(false)
  })

  it('renders image', () => {
    const wrapper = getWrapper({...options, propsData: {...(options.propsData), horizontal: false}})
    expect(wrapper.find('[data-test-id="product-listing-item__image"]').attributes('src')).toBeTruthy()
  })

  it('passes preload through to ImageSet', () => {
    const wrapper = getWrapper({
      ...options,
      propsData: {...options.propsData, horizontal: false, preload: true}
    })
    expect(wrapper.find('[data-test-id="product-listing-item__image"]').attributes('preload')).toBe('true')
  })

  it('image contains alt', () => {
    const wrapper = getWrapper({...options, propsData: {...(options.propsData), horizontal: false}})
    expect(wrapper.find('[data-test-id="product-listing-item__image"]').attributes('alt')).toBeTruthy()
  })

  it('image contains title', () => {
    const wrapper = getWrapper({...options, propsData: {...(options.propsData), horizontal: false}})
    expect(wrapper.find('[data-test-id="product-listing-item__image"]').attributes('title')).toEqual(_card.image.imageTitle)
  })

  it('has no caption when not present', () => {
    expect(wrapper.find('[data-test-id="product-listing-item-caption"]').exists()).toBe(false)
  })

  it('has caption when present', async () => {
    const wrapper = getWrapper({...options, propsData: {...(options.propsData), item: {..._card, caption: 'my test'}}})
    expect(wrapper.find('[data-test-id="product-listing-item-caption"]').exists()).toBe(true)
    expect(wrapper.find('[data-test-id="product-listing-item-caption"]').text()).toEqual('my test')
  })

  it('renders info', async () => {
    expect(wrapper.find('[data-test-id="product-listing-item-info"]').exists()).toBe(false)
    await wrapper.setProps({item: {..._card, info: 'my test'}})
    expect(wrapper.find('[data-test-id="product-listing-item-info"]').exists()).toBe(true)
    expect(wrapper.find('[data-test-id="product-listing-item-info"]').text()).toEqual('my test')
  })

  it('renders description', async () => {
    await wrapper.setProps({item: _card})
    expect(wrapper.find('[data-test-id="product-listing-item-description"]').exists()).toBe(true)
    expect(wrapper.find('[data-test-id="product-listing-item-description"]').text()).toEqual(_card.description)
    await wrapper.setProps({item: {..._card, description: null}})
    expect(wrapper.find('[data-test-id="product-listing-item-description"]').exists()).toBe(false)
  })

  it('renders link with proper rel', () => {
    expect(wrapper.find('[data-test-id="product-listing-item-link"]').exists()).toBe(true)
    expect(wrapper.find('[data-test-id="product-listing-item-link"]').attributes('rel')).toBe('noopener nofollow')
  })
})

describe('ProductListingItem horizontal', () => {
  const wrapper = getWrapper()

  it('renders two columns', async () => {
    await wrapper.setProps({isCarouselCard: true, horizontal: true})
    expect(wrapper.find('[data-test-id="content-carousel-item__image-wrapper"] + [data-test-id="content-carousel-item__content"]').exists()).toBe(true)
  })

  it('renders new indicator when present and on', async () => {
    await wrapper.setProps({showIndicator: true, newIndicator: true})
    expect(wrapper.find('[data-test-id="content-carousel-item__image-wrapper"] [data-test-id="content-carousel-item-new-indicator"]').exists()).toBe(true)
  })

  it('renders video indicator when present and on', async () => {
    await wrapper.setProps({showIndicator: true, videoIndicator: true})
    expect(wrapper.find('[data-test-id="content-carousel-item__image-wrapper"] [data-test-id="content-carousel-item-video-indicator"]').exists()).toBe(true)
  })

  it('not renders video indicator when not present and on', async () => {
    const wrapper = getWrapper({...options, propsData: {...(options.propsData), item: {..._card, videoIndicator: false}}})
    expect(wrapper.find('[data-test-id="content-carousel-item__image-wrapper"] [data-test-id="content-carousel-item-video-indicator"]').exists()).toBe(false)
  })

  it('not renders new indicator when not present and on', async () => {
    const wrapper = getWrapper({...options, propsData: {...(options.propsData), item: {..._card, newIndicator: false}}})
    expect(wrapper.find('[data-test-id="product-listing-item__image"] [data-test-id="content-carousel-item-new-indicator"]').exists()).toBe(false)
  })

  it('not renders indicators when turned off', async () => {
    const wrapper = getWrapper({...options, propsData: {...(options.propsData), showIndicator: false}})
    expect(wrapper.find('[data-test-id="product-listing-item__image"] [data-test-id="content-carousel-item-video-indicator"]').exists()).toBe(false)
    expect(wrapper.find('[data-test-id="product-listing-item__image"] [data-test-id="content-carousel-item-new-indicator"]').exists()).toBe(false)
  })

  it('renders image', () => {
    expect(wrapper.find('[data-test-id="content-carousel-item__image"]').attributes('src')).toBeTruthy()
  })

  it('image contains alt', () => {
    expect(wrapper.find('[data-test-id="content-carousel-item__image"]').attributes('alt')).toEqual(_card.image.imageAlt)
  })

  it('image contains title', () => {
    expect(wrapper.find('[data-test-id="content-carousel-item__image"]').attributes('title')).toEqual(_card.image.imageTitle)
  })

  it('has no caption when not present', () => {
    expect(wrapper.find('[data-test-id="product-listing-item__image"] [data-test-id="content-carousel-item-caption"]').exists()).toBe(false)
  })

  it('has caption when present', async () => {
    const wrapper = getWrapper({...options, propsData: {...(options.propsData), item: {..._card, caption: 'test caption'}}})
    expect(wrapper.find('[data-test-id="product-listing-item__image-wrapper"] [data-test-id="product-listing-item-caption"]').exists()).toBe(true)
    expect(wrapper.find('[data-test-id="product-listing-item__image-wrapper"] [data-test-id="product-listing-item-caption"]').text()).toEqual('test caption')
  })

  it('renders info', async () => {
    expect(wrapper.find('[data-test-id="content-carousel-item-info"]').exists()).toBe(false)
    await wrapper.setProps({item: {..._card, info: 'my test'}})
    expect(wrapper.find('[data-test-id="content-carousel-item-info"]').exists()).toBe(true)
    expect(wrapper.find('[data-test-id="content-carousel-item-info"]').text()).toEqual('my test')
  })

  it('renders description', async () => {
    await wrapper.setProps({isCarouselCard: false, item: _card})
    expect(wrapper.find('[data-test-id="product-listing-item-description"]').exists()).toBe(true)
    expect(wrapper.find('[data-test-id="product-listing-item-description"]').text()).toEqual(_card.description)
    await wrapper.setProps({item: {..._card, description: null}})
    expect(wrapper.find('[data-test-id="product-listing-item-description"]').exists()).toBe(false)
  })

  it('renders link with proper rel', () => {
    expect(wrapper.find('[data-test-id="product-listing-item-link"]').exists()).toBe(true)
    expect(wrapper.find('[data-test-id="product-listing-item-link"]').attributes('rel')).toBe('noopener nofollow')
  })
})
