import {shallowMount} from '@vue/test-utils'
import {BCarousel, BCarouselSlide} from 'bootstrap-vue'
import cloudinary from './utils/cloudinary.mock'
import HeroSpot from '@/components/HeroSpot'
import mock from '~/server/responses/bobcat-api/pages/components/herospot'
import deviceUtils from '~/plugins/utils/devices'
import WarningAlert from '@/components/elements/WarningAlert'

const heroSpotProps = mock.main['0']

const defaultOpt = {
  propsData: {
    slides: heroSpotProps.slides,
    interval: heroSpotProps.interval
  },
  mocks: {
    ...cloudinary,
    $nuxt: {
      context: {
        $utils: {
          ...cloudinary,
          ...cloudinary.$utils,
          ...deviceUtils,
          getLinkField: (node) => {
            const link = node?.link || node
            return {
              url: link?.customUrl || '',
              label: link?.customLabel || '',
              target: link?.target || '_self'
            }
          }
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
        $config: {
          USE_LEGACY_I18N_STRUCTURE: false
        },
        $ga4: {
          events: {
            hero_spot_cta: jest.fn()
          }
        },
        $breakpoints: {
          mobile: false,
          tablet: false,
          desktop: true
        }
      }
    }
  },
  stubs: {
    BCarousel,
    BCarouselSlide,
    ImageSet: true,
    WarningAlert,
    LazyHydrate: true
  }
}

const getWrapper = (opt = defaultOpt) => shallowMount(HeroSpot, opt)

describe('Herospot component [data props]', () => {
  const wrapper = getWrapper()
  const header = wrapper.find('h1')
  const paragraph = wrapper.find('p')
  const link = wrapper.find('a')
  const arrowRight = wrapper.find('.arrow-r')
  const arrowLeft = wrapper.find('.arrow-l')
  const backgroundOverlay = wrapper.find('.overlay')
  const slideImg = wrapper.find('imageset-stub')
  const slideVideo = wrapper.find('video')
  const heroDots = wrapper.find('ul[data-test-id="herospot-dots"]')

  it('has h1 with mocked label', () => {
    expect(header.text()).toEqual(heroSpotProps.slides.slides0.header)
  })

  it('has paragraph with mocked label', () => {
    expect(paragraph.text()).toEqual(heroSpotProps.slides.slides0.text)
  })

  it('has <a/> tag with mocked label', () => {
    expect(link.text()).toEqual(heroSpotProps.slides.slides0.link.link.customLabel)
  })

  it('<a/> tag has mocked href attribute', () => {
    expect(link.attributes().href).toBe(heroSpotProps.slides.slides0.link.link.customUrl)
  })

  it('<a/> tag has mocked target attribute', () => {
    expect(link.attributes().target).toBe(heroSpotProps.slides.slides0.link.link.target)
  })

  it('has right arrow button', () => {
    expect(arrowRight.exists()).toBe(true)
  })

  it('has left arrow button', () => {
    expect(arrowLeft.exists()).toBe(true)
  })

  it('has background overlay', () => {
    expect(backgroundOverlay.exists()).toBe(true)
  })

  it('has at least one hero image', () => {
    expect(slideImg.exists()).toBe(true)
  })

  it('has at least one hero video', () => {
    expect(slideVideo.exists()).toBe(true)
  })

  it('has the same number of dots as slides', () => {
    const dots = heroDots.findAll('li')
    expect(dots.length).toEqual(heroSpotProps.slides['@nodes'].length)
  })
})

describe('Herospot component [style props]', () => {
  const wrapper = getWrapper()
  const header = wrapper.find('h1')
  const paragraph = wrapper.find('p')
  const heroDots = wrapper.find('ul[data-test-id="herospot-dots"]')

  it('h1 has white color', () => {
    expect(header.classes('text-white')).toBe(true)
  })

  it('paragraph has white color', () => {
    expect(paragraph.classes('text-white')).toBe(true)
  })

  it('dots wrapper has one active dot', () => {
    const dots = heroDots.findAll('li.bg-primary')
    expect(dots.length).toEqual(1)
  })
})

describe('Herospot component [functionality]', () => {
  const wrapper = getWrapper({...defaultOpt, attachTo: document.body})
  const heroDots = wrapper.find('ul[data-test-id="herospot-dots"]')

  afterAll(() => {
    wrapper.destroy()
  })

  it('arrows are not shown when only one slide', () => {
    const newWrapper = getWrapper({...defaultOpt, propsData: {slides: {...heroSpotProps.slides, '@nodes': ['slides0']}}})
    expect(newWrapper.find('.arrow').exists()).toBe(false)
  })

  it('when no slides the message displays', async () => {
    const newWrapper = getWrapper({...defaultOpt, propsData: {slides: undefined}})
    await newWrapper.setData({inEditorEdit: true})
    await newWrapper.vm.$forceUpdate()
    expect(newWrapper.find('div[data-test-id="herospot-no-slides-msg"]').exists()).toBe(true)
  })

  it('button is optional, when it\'s not provided it\'s not displayed', () => {
    const newWrapper = getWrapper({...defaultOpt, propsData: {slides: {...heroSpotProps.slides.slides0, link: undefined}}})
    expect(newWrapper.find('a').exists()).toBe(false)
  })

  it('text is optional, when it\'s not provided it\'s not displayed', () => {
    const newWrapper = getWrapper({...defaultOpt, propsData: {slides: {...heroSpotProps.slides.slides0, text: undefined}}})
    expect(newWrapper.find('p').exists()).toBe(false)
  })

  it('text is optional, when it\'s not provided it\'s not displayed', () => {
    const newWrapper = getWrapper({...defaultOpt, propsData: {slides: {...heroSpotProps.slides.slides0, header: undefined}}})
    expect(newWrapper.find('h1').exists()).toBe(false)
  })

  it('dots wrapper has one active dot', async () => {
    const dots = heroDots.findAll('li.bg-primary')
    expect(dots.length).toEqual(1)
  })

  it('start method is fired on Herospot mouseleave', async () => {
    await wrapper.trigger('mouseleave')
    expect(wrapper.vm.slideGap).toEqual(wrapper.vm.heroSlides[wrapper.vm.slide].interval)
  })

  it('stop method is fired on Herospot mouseenter', async () => {
    await wrapper.trigger('mouseenter')
    expect(wrapper.vm.slideGap).toEqual(0)
  })

  it('start method is fired on Herospot blur', async () => {
    await wrapper.trigger('blur')
    expect(wrapper.vm.slideGap === wrapper.vm.heroSlides[wrapper.vm.slide].interval && !wrapper.vm.isFocused).toBe(true)
  })

  it('stop method is fired on Herospot focus', async () => {
    await wrapper.trigger('focus')
    expect(wrapper.vm.slideGap === 0 && wrapper.vm.isFocused).toBe(true)
  })
})
