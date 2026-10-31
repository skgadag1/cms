import {shallowMount} from '@vue/test-utils'
import cloudinary from './utils/cloudinary.mock'
import PromotionalContent from '@/components/PromotionalContent'
import linkUtils from '~/plugins/utils/linkUtils'
import mock from '~/server/responses/bobcat-api/pages/components/promotionalContent'

const promotionalContentProps = mock.main['0']

const options = {
  propsData: {
    title: promotionalContentProps.title,
    htag: promotionalContentProps.htag,
    paragraph: promotionalContentProps.paragraph,
    link: promotionalContentProps.link,
    avatar: promotionalContentProps.avatar,
    backgroundImg: promotionalContentProps.backgroundImg,
    variant: promotionalContentProps.variant,
    opacity: promotionalContentProps.opacity
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
    'b-form-checkbox': true,
    'b-avatar': true
  }
}

const getWrapper = (opt = options) => shallowMount(PromotionalContent, opt)
const wrapper = getWrapper()
const header = wrapper.find('h2')
const paragraph = wrapper.find('p')
const link = wrapper.find('a')
const backgroundImage = cloudinary.$utils.getCloudinaryAsset(promotionalContentProps.backgroundImg, true, {crop: 'fill', width: 1440, height: 692})
const avatarImage = cloudinary.$utils.getCloudinaryAsset(promotionalContentProps.avatar.src, true, {crop: 'fill', width: 40, height: 40})

describe('Promotional Content component [data props]', () => {
  it('has h2 with mocked label', () => {
    expect(header.text()).toEqual(promotionalContentProps.title)
  })

  it('has paragraph with mocked label', () => {
    expect(paragraph.text()).toEqual(promotionalContentProps.paragraph)
  })

  it('has <a/> tag with mocked label', () => {
    expect(link.text()).toEqual(promotionalContentProps.link.link.customLabel)
  })

  it('<a/> tag has mocked href attribute', () => {
    expect(link.attributes().href).toBe(promotionalContentProps.link.link.customUrl)
  })

  it('<a/> tag has mocked target attribute', () => {
    expect(link.attributes().target).toBe(promotionalContentProps.link.link.target)
  })

  it('<a/> is not displaying self when not present', async () => {
    await wrapper.setData({linkField: undefined})
    expect(wrapper.find('a').exists()).toBe(false)
  })

  it('has mocked background image', () => {
    expect(wrapper.find('.promo-bg-img').attributes('style')).toEqual(`background-image: url(${backgroundImage});`)
  })

  it('has mocked avatar image', () => {
    expect(wrapper.find('b-avatar-stub').attributes('src')).toEqual(avatarImage)
  })

  it('has mocked avatar alt', () => {
    expect(wrapper.find('b-avatar-stub').attributes('alt')).toEqual(promotionalContentProps.avatar.imageAlt)
  })

  it('has mocked overlay', () => {
    expect(wrapper.find('.overlay').exists()).toBe(true)
  })
})

describe('Promotional Content component [functionality]', () => {
  it('has opacity property', () => {
    expect(wrapper.vm.opacity).toEqual(promotionalContentProps.opacity)
  })

  it('has variant property', () => {
    expect(wrapper.vm.variant).toEqual(promotionalContentProps.variant)
  })

  it('center variant property triggers center alignment style', () => {
    const currentVariant = wrapper.vm.currentVariant
    expect(currentVariant.text === 'text-center' && currentVariant.content === 'align-items-center' && currentVariant.overlay === 't').toBe(true)
  })

  it('left variant property triggers left alignment style', () => {
    const currentVariant = getWrapper({...options, propsData: {...options.propsData, variant: 'left'}}).vm.currentVariant
    expect(currentVariant.text === 'text-left' && currentVariant.content === 'align-items-start' && currentVariant.overlay === 'l').toBe(true)
  })

  it('right variant property triggers right alignment style', () => {
    const currentVariant = getWrapper({...options, propsData: {...options.propsData, variant: 'right'}}).vm.currentVariant
    expect(currentVariant.text === 'text-right' && currentVariant.content === 'align-items-end' && currentVariant.overlay === 'r').toBe(true)
  })
})
