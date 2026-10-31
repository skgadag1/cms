import {shallowMount} from '@vue/test-utils'
import cloudinary from './utils/cloudinary.mock'
import CaptionedImage from '@/components/CaptionedImage'
import LightWeight from '@/components/elements/LightWeight'
import mock from '~/server/responses/bobcat-api/pages/components/captionedImage'

const captionedImageProps = mock.main['0']

const wrapper = shallowMount(CaptionedImage, {
  propsData: {
    gridTitle: captionedImageProps.gridTitle,
    cards: captionedImageProps.cards,
    columns: captionedImageProps.columns
  },
  mocks: {
    $nuxt: {
      context: {
        ...cloudinary,
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
    LightWeight,
    ImageSet: true,
    MediaPreview: true,
    LazyHydrate: true,
    'b-icon': true,
    'b-img': true,
    WarningAlert: true
  }
})

const cards = captionedImageProps.cards['@nodes'].map(x => captionedImageProps.cards[x])
const header = wrapper.find('h2')
const singleCard = wrapper.find('div[data-test-id="captioned-image-0"]')

describe('Captioned Image component [data props]', () => {
  it('has h2 with mocked label', () => {
    expect(header.text()).toEqual(captionedImageProps.gridTitle)
  })

  it('has card with mocked label', () => {
    expect(singleCard.find('h4').text()).toEqual(cards[0].title)
  })

  it('has card with mocked paragraph', () => {
    expect(singleCard.find('[data-test-id="card-paragraph"]').text()).toEqual(cards[0].paragraph)
  })

  it('has card with mocked image or video or YouTube video', () => {
    const imgFound = singleCard.find('[data-test-id="light-weight-img"]').exists()
    const videoFound = singleCard.find('[data-test-id="light-weight-video"]').exists()
    const youtubeFound = singleCard.find('[data-test-id="light-weight-youtube"]').exists()
    expect(imgFound || videoFound || youtubeFound).toBe(true)
  })

  it('has many mocked cards', () => {
    const items = wrapper.findAll('div[data-test-id^="captioned-image-"]').filter(w => w.attributes('data-test-id') !== 'captioned-image-container')
    expect(items.length).toEqual(cards.length)
  })

  it('has card with mocked expand icon', () => {
    expect(wrapper.find('b-icon-stub[icon="expand"]').exists()).toBe(true)
  })

  it('has LazyHydrate component', () => {
    expect(wrapper.find('lazyhydrate-stub').exists()).toBe(true)
  })
})
