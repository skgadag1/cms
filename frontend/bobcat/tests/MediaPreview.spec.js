import {shallowMount} from '@vue/test-utils'
import MediaPreview from '@/components/MediaPreview'
import mock from '~/server/responses/bobcat-api/pages/components/captionedImage'

const mediaPreviewPropsWithVideo = mock.main['0'].cards.cards0
const mediaPreviewPropsWithImage = mock.main['0'].cards.cards2
const mediaPreviewPropsWithYouTube = mock.main['0'].cards.cards1

const getOptions = (mock) => {
  return {
    propsData: {
      assetItem: {
        video: mock.asset?.video?.src,
        img: mock.asset?.img?.src,
        imageAlt: mock.asset?.img?.imageAlt,
        imageTitle: mock.asset?.img?.imageTitle,
        youtube: mock.asset?.youtube
      },
      title: mock.title,
      paragraph: mock.paragraph
    },
    stubs: {
      YoutubeIframe: true,
      ImageSet: true,
      GTMVideo: true,
      'b-icon': true
    },
    data() {
      return {
        isHidden: false
      }
    }
  }
}

const videoWrapperOptions = getOptions(mediaPreviewPropsWithVideo)
const imageWrapperOptions = getOptions(mediaPreviewPropsWithImage)
const youtubeWrapperOptions = getOptions(mediaPreviewPropsWithYouTube)

const getWrapper = opt => shallowMount(MediaPreview, opt)

const videoWrapper = getWrapper(videoWrapperOptions)
const imageWrapper = getWrapper(imageWrapperOptions)
const youtubeWrapper = getWrapper(youtubeWrapperOptions)

describe('Media Preview component [data props]', () => {
  it('has h2 with mocked label', () => {
    expect(videoWrapper.find('h2').text()).toEqual(mediaPreviewPropsWithVideo.title)
  })

  it('has p with mocked paragraph', () => {
    expect(videoWrapper.find('[data-test-id="card-paragraph"]').text()).toEqual(mediaPreviewPropsWithVideo.paragraph)
  })

  it('has video tag', () => {
    expect(videoWrapper.find('gtmvideo-stub').exists()).toBe(true)
  })

  it('video tag has src attribute', () => {
    expect(videoWrapper.find('gtmvideo-stub').attributes().src).toEqual(mediaPreviewPropsWithVideo.asset.video.src)
  })

  it('has YouTube video', () => {
    expect(youtubeWrapper.find('youtubeiframe-stub').exists()).toBe(true)
  })

  it('has img tag', () => {
    expect(imageWrapper.find('imageset-stub').exists()).toBe(true)
  })

  it('img tag has src attribute', () => {
    expect(imageWrapper.find('imageset-stub').attributes().src).toEqual(mediaPreviewPropsWithImage.asset.img.src)
  })

  it('img tag has alt attribute', () => {
    expect(imageWrapper.find('imageset-stub').attributes().alt).toEqual(mediaPreviewPropsWithImage.asset.img.imageAlt)
  })
})

describe('Media Preview component [functionality]', () => {
  it('can be closed', async () => {
    await imageWrapper.setData({isHidden: true})
    expect(imageWrapper.vm.isHidden && !imageWrapper.find('div.overlay').exists()).toBe(true)
  })

  it('can be opened', async () => {
    await imageWrapper.setData({isHidden: false})
    expect((!imageWrapper.vm.isHidden && imageWrapper.find('div[data-test-id="media-preview-container"]').exists())).toBe(true)
  })
})
