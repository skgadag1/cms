import {computed, onBeforeUnmount, onMounted, ref, useContext} from '@nuxtjs/composition-api'
import useBreakpoints from '~/helpers/composables/useBreakpoints'
import useSchema from '~/helpers/composables/useSchema'

const useHeroCarousel = (props, isStaticInterval = false, specialOffersSlides = []) => {
  const {$utils, $regions, $config} = useContext()
  const {mobile, desktop} = useBreakpoints()
  const {slides, interval} = props
  const slide = ref(0)
  const slideGap = ref(parseInt(interval))
  const carousel = ref(null)
  const sliding = ref(null)
  const heroSpot = ref(null)
  const touchstartX = ref(0)
  const touchendX = ref(0)
  const permanentStop = ref(false)
  const isFocused = ref(false)
  const assetsSchema = ref([])

  const countIntervalFromWords = (title, text) => {
    const titleCount = title ? title.match(/(\w+)/g)?.length : 0
    const textCount = text ? text.match(/(\w+)/g)?.length : 0
    // one word = 333ms and minimum value for carousel interval is 1000ms
    const sum = (titleCount + textCount) * 333
    return sum >= 1000 ? sum : parseInt(interval)
  }

  const propSlides = slides
    ? slides['@nodes']?.map((x) => {
      const sItem = slides[x]
      const linkField = $utils.getLinkField(sItem.link, $regions.current.language.name)
      const slide = {
        '@id': sItem['@id'],
        header: sItem.header,
        button: {
          link: linkField?.url || '',
          title: linkField?.label || '',
          target: linkField?.target || 'self'
        },
        text: sItem.text,
        interval: isStaticInterval ? parseInt(interval) : countIntervalFromWords(sItem.header, sItem.text)
      }

      if (sItem.asset?.img?.src) {
        const {metadata} = $utils.getCloudinaryAssetMetadata(sItem.asset.img.src)
        Object.assign(slide, {
          img: sItem.asset.img.src,
          title: sItem.asset.img.imageTitle || metadata?.context?.caption || metadata?.title,
          alt: sItem.asset.img.imageAlt || metadata?.context?.alt || metadata?.description || metadata?.['alt_' + $regions.current.language.name],
          imgWidth: sItem.asset.img.imageWidth || null,
          imgHeight: sItem.asset.img.imageHeight || null,
          expectedWidth: !desktop.value ? (mobile.value ? 480 : 1140) : 2048,
          ...metadata
        })
      } else if (sItem.asset?.video?.src) {
        const {metadata} = $utils.getCloudinaryAssetMetadata(sItem.asset.video.src)
        Object.assign(slide, {
          video: $utils.getCloudinaryAsset(sItem.asset.video.src, false, {
            quality: 'auto:good',
            fetch_format: 'auto',
            flags: 'streaming_attachment',
            crop: 'fit',
            height: 480,
            ...(!desktop.value && {
              width: mobile.value ? 576 : 992
            })
          }),
          poster: $utils.getCloudinaryAsset(sItem.asset.video.poster) || $utils.getCloudinaryAsset(sItem.asset.video.src, false, {
            startOffset: 0,
            effect: 'preview',
            resource_type: 'video'
          }),
          title: metadata?.context?.caption || metadata?.title,
          alt: metadata?.context?.alt || metadata?.description,
          ...metadata,
          metadata
        })
      }

      return slide
    })
    : []
  const heroSlides = specialOffersSlides.filter(s => !!s).map(s => ({text: s})).concat(propSlides)

  const doCarousel = computed(() => heroSlides?.length > 1)

  // init interval value
  slideGap.value = doCarousel.value ? heroSlides[0]?.interval : 0

  const onSlideStart = (slide) => {
    if (!permanentStop.value && doCarousel.value) {
      slideGap.value = heroSlides[slide].interval
    }
    sliding.value = doCarousel.value
  }

  const onSlideEnd = () => {
    sliding.value = false
  }

  const next = () => {
    carousel.value.next()
  }

  const prev = () => {
    carousel.value.prev()
  }

  const stopAutoPlay = () => {
    // eslint-disable-next-line no-console
    console.log('stopAutoPlay')
    if (!isFocused.value) {
      slideGap.value = 0
    }
  }

  const stopAutoPlayPermanent = () => {
    // eslint-disable-next-line no-console
    console.log('Stopautoplay permanent')
    permanentStop.value = true
    slideGap.value = 0
  }

  const stopAutoPlayOnFocus = () => {
    // eslint-disable-next-line no-console
    console.log('stopAutoPlayOnFocus')
    isFocused.value = true
    slideGap.value = 0
  }

  const startAutoPlayOnBlur = () => {
    // eslint-disable-next-line no-console
    console.log('startAutoPlayOnBlur')
    if (!permanentStop.value && doCarousel.value) {
      isFocused.value = false
      slideGap.value = heroSlides[slide.value]?.interval
    }
  }

  const startAutoPlay = () => {
    // eslint-disable-next-line no-console
    console.log('startAutoPlay')
    if (!permanentStop.value && !isFocused.value && doCarousel.value) {
      slideGap.value = heroSlides[slide.value]?.interval
    }
  }

  const setSlideAndStop = (callback) => {
    stopAutoPlayPermanent()
    callback()
  }

  const setSlide = (index) => {
    stopAutoPlayPermanent()
    carousel.value.setSlide(index)
  }

  if (process.client && doCarousel.value && slides?.['@nodes'].length) {
    const handleGesure = () => {
      if (touchendX.value < touchstartX.value) {
        next()
        stopAutoPlayPermanent()
      }
      if (touchendX.value > touchstartX.value) {
        prev()
        stopAutoPlayPermanent()
      }
    }

    onMounted(() => heroSpot.value.addEventListener('touchstart', function (event) {
      touchstartX.value = event.changedTouches['0'].screenX
    }, {passive: true}))

    onMounted(() => heroSpot.value.addEventListener('touchend', function (event) {
      touchendX.value = event.changedTouches['0'].screenX
      handleGesure()
    }, {passive: true}))

    // hack to initiate properly first slide animation [NGB-4610]
    onMounted(() => {
      stopAutoPlay()
      startAutoPlay()
    })
  }

  // Remove role="list" from bootstrap element if it's not a carousel
  onMounted(() => {
    const carouselInner = heroSpot.value.querySelector('.carousel-inner')
    if (!doCarousel.value && carouselInner) {
      carouselInner.removeAttribute('role')
    }
  })

  onMounted(() => {
    if (!heroSpot.value) { return }
    heroSpot.value.addEventListener('focus', stopAutoPlayOnFocus)
    heroSpot.value.addEventListener('blur', startAutoPlayOnBlur)
    heroSpot.value.addEventListener('mouseenter', stopAutoPlay)
    heroSpot.value.addEventListener('mouseleave', startAutoPlay)
  })

  onBeforeUnmount(() => {
    heroSpot.value.removeEventListener('focus', stopAutoPlayOnFocus)
    heroSpot.value.removeEventListener('blur', startAutoPlayOnBlur)
    heroSpot.value.removeEventListener('mouseenter', stopAutoPlay)
    heroSpot.value.removeEventListener('mouseleave', startAutoPlay)
  })

  const {createVideoObject} = useSchema({}, $utils, $regions, $config)

  heroSlides.forEach(s => assetsSchema.value.push(s?.video ? createVideoObject(s) : false))

  const getCurrentSlideData = () => heroSlides[slide.value]

  return {
    heroSlides,
    doCarousel,
    carousel,
    slide,
    sliding,
    slideGap,
    onSlideStart,
    onSlideEnd,
    next,
    prev,
    setSlide,
    setSlideAndStop,
    heroSpot,
    stopAutoPlay,
    startAutoPlay,
    stopAutoPlayPermanent,
    stopAutoPlayOnFocus,
    startAutoPlayOnBlur,
    isFocused,
    assetsSchema,
    getCurrentSlideData
  }
}

export default useHeroCarousel
