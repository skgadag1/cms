import {shallowMount} from '@vue/test-utils'
import cloudinary from './utils/cloudinary.mock'
import EvolutionHeroSpotV2 from '@/components/EvolutionHeroSpotV2'
import heroSpotMock from '~/server/responses/bobcat-api/pages/components/herospot'

const videoSrc = heroSpotMock.main['0'].slides.slides2.asset.video.src
const posterSrc = heroSpotMock.main['0'].slides.slides0.asset.img.src
const imageSrc = heroSpotMock.main['0'].slides.slides1.asset.img.src

const posterOptions = {
  fetch_format: 'auto',
  quality: 'auto:low',
  width: 960,
  dpr: 1,
  crop: 'fill',
  gravity: 'auto',
  aspect_ratio: 1.77778
}

const getCloudinaryAsset = jest.fn((...args) => cloudinary.$utils.getCloudinaryAsset(...args))

const mountOptions = asset => ({
  propsData: {
    title: '<h1>Hero title</h1>',
    description: '<p>Hero description</p>',
    asset
  },
  provide: {
    model: {}
  },
  mocks: {
    $nuxt: {
      context: {
        $utils: {
          ...cloudinary.$utils,
          getCloudinaryAsset,
          getLinkField: () => ({})
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
        $ga4: {
          events: {
            hero_spot_cta: jest.fn()
          }
        },
        $config: {
          USE_LEGACY_I18N_STRUCTURE: false
        }
      }
    }
  },
  stubs: {
    ImageSet: true,
    DynamicButton: true
  }
})

const getWrapper = asset => shallowMount(EvolutionHeroSpotV2, mountOptions(asset))

describe('EvolutionHeroSpotV2 video poster', () => {
  beforeEach(() => {
    getCloudinaryAsset.mockClear()
  })

  it('sets poster from the authored still with Cloudinary image optimizations', () => {
    const wrapper = getWrapper({
      video: {
        src: videoSrc,
        poster: posterSrc
      }
    })
    const poster = wrapper.find('video').attributes('poster')
    const expected = cloudinary.$utils.getCloudinaryAsset(posterSrc, true, posterOptions)

    expect(poster).toEqual(expected)
    expect(poster).toContain('/image/upload/')
    expect(poster).toMatch(/f_auto/)
    expect(poster).toMatch(/q_auto:low/)
    expect(poster).not.toMatch(/e_preview/)
    expect(getCloudinaryAsset).toHaveBeenCalledWith(posterSrc, true, posterOptions)
    expect(getCloudinaryAsset.mock.calls.some(([, isImg, options]) => options?.effect === 'preview')).toBe(false)
  })

  it('matches poster crop to the video aspect ratio and reserves intrinsic size', () => {
    const wrapper = getWrapper({
      video: {
        src: videoSrc,
        poster: posterSrc
      }
    })
    const video = wrapper.find('video')

    expect(video.attributes('width')).toBe('1920')
    expect(video.attributes('height')).toBe('1080')
    expect(video.attributes('preload')).toBe('metadata')
    expect(getCloudinaryAsset).toHaveBeenCalledWith(
      posterSrc,
      true,
      expect.objectContaining({
        aspect_ratio: 1.77778,
        crop: 'fill',
        width: 960
      })
    )
  })

  it('does not fall back to an e_preview video frame when poster is missing', () => {
    const wrapper = getWrapper({
      video: {
        src: videoSrc
      }
    })

    expect(wrapper.find('video').attributes('poster')).toBeUndefined()
    expect(getCloudinaryAsset.mock.calls.some(([, isImg, options]) => options?.effect === 'preview')).toBe(false)
  })

  it('preloads the poster image for slow connections', () => {
    const wrapper = getWrapper({
      video: {
        src: videoSrc,
        poster: posterSrc
      }
    })
    const head = wrapper.vm.$options.head.call(wrapper.vm)

    expect(head.link[0]).toEqual(expect.objectContaining({
      rel: 'preload',
      as: 'image',
      href: wrapper.find('video').attributes('poster')
    }))
    expect(head.link).toEqual(expect.arrayContaining([
      expect.objectContaining({
        rel: 'preload',
        as: 'video',
        href: wrapper.find('video source').attributes('src')
      })
    ]))
  })

  it('preloads the hero image with fetchpriority=high for LCP discovery', () => {
    const wrapper = getWrapper({
      img: {
        src: imageSrc,
        imageTitle: 'title',
        imageAlt: 'alt'
      }
    })
    const head = wrapper.vm.$options.head.call(wrapper.vm)
    const expected = cloudinary.$utils.getCloudinaryAsset(imageSrc, true, {
      fetch_format: 'auto',
      crop: 'fill',
      gravity: 'auto',
      type: 'upload'
    })

    expect(head.link[0]).toEqual({
      rel: 'preload',
      as: 'image',
      fetchpriority: 'high',
      href: expected
    })
    expect(wrapper.find('imageset-stub').attributes('preload')).toBe('true')
  })

  it('still renders an image asset without a video poster', () => {
    const wrapper = getWrapper({
      img: {
        src: imageSrc,
        imageTitle: 'title',
        imageAlt: 'alt'
      }
    })

    expect(wrapper.find('video').exists()).toBe(false)
    expect(wrapper.find('imageset-stub').exists()).toBe(true)
    expect(wrapper.find('imageset-stub').attributes('src')).toBe(imageSrc)
  })
})
