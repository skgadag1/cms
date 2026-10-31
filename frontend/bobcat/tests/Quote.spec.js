import {shallowMount} from '@vue/test-utils'
import cloudinary from './utils/cloudinary.mock'
import Quote from '@/components/Quote'
import mock from '~/server/responses/bobcat-api/pages/components/quote'

const props = mock.main['0']

const wrapper = shallowMount(Quote, {
  propsData: {
    quote: props.quote,
    authorName: props.authorName,
    authorSubtitle: props.authorSubtitle,
    avatar: props.avatar
  },
  mocks: {
    ...cloudinary,
    $nuxt: {
      context: {
        $utils: {
          ...cloudinary.$utils
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
    'b-avatar': true,
    'b-icon': true,
    'b-img': true,
    'b-button': true,
    LazyHydrate: true
  }
})

const quote = wrapper.find('h4')
const authorName = wrapper.find('h5')
const authorSubtitle = wrapper.find('p')

const avatarImage = cloudinary.$utils.getCloudinaryAsset(props.avatar.src, true, {
  crop: 'fill',
  width: 40,
  height: 40
})

describe('Quote component [data props]', () => {
  it('has h4 quote with mocked label', () => {
    expect(quote.text()).toEqual(props.quote)
  })

  it('has h5 person authorName with mocked label', () => {
    expect(authorName.text()).toEqual(props.authorName)
  })

  it('has authorSubtitle with mocked label', () => {
    expect(authorSubtitle.text()).toEqual(props.authorSubtitle)
  })

  it('has mocked avatar image', () => {
    expect(wrapper.find('b-avatar-stub').attributes('src')).toEqual(avatarImage)
  })

  it('has mocked avatar alt', () => {
    expect(wrapper.find('b-avatar-stub').attributes('alt')).toEqual(props.avatar.imageAlt)
  })

  it('has LazyHydrate component', () => {
    const lh = wrapper.find('lazyhydrate-stub')
    expect(lh.exists()).toBe(true)
  })
})
