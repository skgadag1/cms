import {shallowMount} from '@vue/test-utils'
import cloudinary from './utils/cloudinary.mock'
import ImageSet from '@/components/elements/ImageSet'

const mountImageSet = (props = {}) => shallowMount(ImageSet, {
  propsData: {
    src: 'cloudinary:sample#eyJhc3BlY3RfcmF0aW8iOjEuNSwiZm9ybWF0IjoianBnIiwicmVzb3VyY2VfdHlwZSI6ImltYWdlIiwicHVibGljX2lkIjoic2FtcGxlIn0=',
    ...props
  },
  mocks: {
    $nuxt: {
      context: {
        ...cloudinary,
        $utils: {
          ...cloudinary.$utils
        },
        $regions: {
          current: {
            name: 'en',
            language: {name: 'en'}
          }
        },
        $config: {
          PLACEHOLDER_IMAGE: 'cloudinary:placeholder'
        }
      }
    }
  },
  stubs: {
    'b-img': true
  }
})

describe('ImageSet LCP attributes', () => {
  it('uses lazy loading by default without fetchpriority', () => {
    const wrapper = mountImageSet()
    const img = wrapper.find('b-img-stub')
    expect(img.attributes('loading')).toBe('lazy')
    expect(img.attributes('fetchpriority')).toBeUndefined()
  })

  it('sets loading=eager and fetchpriority=high when preload is true', () => {
    const wrapper = mountImageSet({preload: true})
    const img = wrapper.find('b-img-stub')
    expect(img.attributes('loading')).toBe('eager')
    expect(img.attributes('fetchpriority')).toBe('high')
  })

  it('keeps an explicit fetchpriority from options on LCP images', () => {
    const wrapper = mountImageSet({
      preload: true,
      options: {loading: 'eager', fetchpriority: 'highest'}
    })
    const img = wrapper.find('b-img-stub')
    expect(img.attributes('loading')).toBe('eager')
    expect(img.attributes('fetchpriority')).toBe('highest')
  })

  it('preloads LCP images with fetchpriority=high', () => {
    const wrapper = mountImageSet({preload: true})
    const head = wrapper.vm.$options.head.call(wrapper.vm)
    expect(head.link[0]).toEqual(expect.objectContaining({
      rel: 'preload',
      as: 'image',
      fetchpriority: 'high',
      href: wrapper.vm.img
    }))
  })
})
