import {shallowMount} from '@vue/test-utils'
import cloudinary from './utils/cloudinary.mock'
import Video from '@/components/Video'
import mock from '~/server/responses/bobcat-api/pages/components/video'

const mediaPreviewPropsWithVideo = mock.main['0'].asset
const mediaPreviewPropsWithYouTube = mock.main['0'].asset1

const getOptions = (mock) => {
  return {
    propsData: {
      asset: mock
    },
    mocks: {
      $nuxt: {
        context: {
          $utils: {
            ...cloudinary,
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
      LazyHydrate: true,
      'b-icon': true,
      YoutubeIframe: true
    }
  }
}

const videoWrapperOptions = getOptions(mediaPreviewPropsWithVideo)
const youtubeWrapperOptions = getOptions(mediaPreviewPropsWithYouTube)

const getWrapper = opt => shallowMount(Video, opt)

const videoWrapper = getWrapper(videoWrapperOptions)
const youtubeWrapper = getWrapper(youtubeWrapperOptions)

describe('Video component [data props]', () => {
  it('has video tag', () => {
    expect(videoWrapper.find('video').exists()).toBe(true)
  })

  it('video tag has src attribute', () => {
    expect(videoWrapper.find('video source').attributes().src).toEqual(cloudinary.$utils.getCloudinaryAsset(mediaPreviewPropsWithVideo.video, false))
  })

  it('has YouTube video', () => {
    expect(youtubeWrapper.find('youtubeiframe-stub').exists()).toBe(true)
  })

  it('has LazyHydrate component', () => {
    const lh = videoWrapper.find('lazyhydrate-stub')
    expect(lh.exists()).toBe(true)
  })
})
