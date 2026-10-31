<template>
  <section ref="observerRef" data-test-id="side-nav-gallery" class="min-h300">
    <div
      :class="selectedColorVariant.background"
      class="py-5"
    >
      <div class="container">
        <div class="px-3 px-lg-0">
          <div class="side-nav-content" :class="selectedColorVariant.text" v-html="content" />

          <DynamicButton v-if="showCTA === 'true' && ctaLink"
                         class="shadow-none pl-0 text-left"
                         :variant="BUTTON_NAMES.LINEAR"
                         :color="buttonColor"
                         :href="ctaLink.url"
                         :target="ctaLink.target === '_blank_follow' ? '_blank' : ctaLink.target"
                         :rel="ctaLink.target === '_blank' ? 'noopener nofollow' : null"
                         data-test-id="side-nav-gallery-cta"
                         @click="ga4click($event, 'link click')"
          >
            {{ ctaLink.label }}
            <b-icon class="ml-2" icon="chevron-right" />
          </DynamicButton>
        </div>

        <transition name="slide-bottom">
          <div
            v-if="isVisible"
            class="features-section p-0 d-flex justify-content-between align-items-start flex-column flex-lg-row"
          >
            <div class="features-list d-flex flex-column flex-sm-row w-100 px-3 px-lg-0 pr-lg-2">
              <ul class="list-unstyled w-100">
                <li v-for="(feature, index) in features"
                    :key="feature.id"
                    :data-test-id="`feature-item-${index}`"
                    class="feature-item position-relative align-items-center"
                    :class="[activeIndex === index ? 'is-active' : selectedColorVariant.text]"
                >
                  <DynamicButton class="shadow-none pl-0 d-flex flex-nowrap text-left text-decoration-none"
                                 :variant="BUTTON_NAMES.LINEAR"
                                 :color="selectedColorVariant.arrowIcon"
                                 @click="changeSlide($event, index)"
                  >
                    <span class="d-flex align-items-center justify-content-center transition-slide chevron-icon"
                          :style="activeIndex === index ? 'transform: rotate(180deg)' : null"
                    >
                      <svg class="feature-item-arrow-icon" viewBox="0 0 24 24" aria-hidden="true">
                        <path d="M17 9L12 15L7 9H17Z" />
                      </svg>
                    </span>
                    <div class="side-nav-content-no-space" :class="selectedColorVariant.text" v-html="feature.title" />
                  </DynamicButton>

                  <div class="overflow-hidden position-relative">
                    <transition name="accordion-bounce">
                      <div v-show="activeIndex === index"
                           class="accordion-content mb-3"
                           :class="selectedColorVariant.text"
                      >
                        <div class="mb-3" :class="selectedColorVariant.text" v-html="feature.subtitle" />
                        <DynamicButton v-if="feature.link"
                                       size="sm"
                                       class="shadow-none text-left pl-0"
                                       :variant="BUTTON_NAMES.LINEAR"
                                       :color="buttonColor"
                                       :href="feature.link.url"
                                       :target="feature.link.target === '_blank_follow' ? '_blank' : feature.link.target"
                                       :rel="feature.link.target === '_blank' ? 'noopener nofollow' : null"
                                       @click="ga4click($event, 'link click')"
                        >
                          {{ feature.link.label }}
                          <b-icon class="ml-2" icon="chevron-right" />
                        </DynamicButton>
                      </div>
                    </transition>
                  </div>

                  <template v-if="activeIndex === index">
                    <transition name="fadeInUp">
                      <div
                        :key="'mobile-slide-' + index"
                        class="d-block d-lg-none position-relative pb-4 feature-item-mobile z-index-1"
                        :class="selectedColorVariant.background"
                      >
                        <div class="position-relative">
                          <b-carousel v-if="currentSlides.length > 0"
                                      v-model="slide"
                                      :interval="0"
                                      :class="selectedColorVariant.arrows"
                                      @sliding-start="onSlideStart"
                                      @sliding-end="onSlideEnd"
                          >
                            <b-carousel-slide v-for="(asset, index1) in currentSlides" :key="index1">
                              <template #img>
                                <video
                                  v-if="asset.video"
                                  class="w-100 h-100"
                                  :data-src="asset.video"
                                  :poster="asset.poster"
                                  controls
                                >
                                  <source :src="asset.video" type="video/webm">
                                  <source :src="asset.video" type="video/mp4">
                                  Your browser does not support HTML5 video.
                                </video>
                                <ImageSet
                                  v-else-if="asset.img"
                                  class="w-100 h-100 cursor-pointer"
                                  crop="pad"
                                  :preload="true"
                                  :src="asset.img"
                                  :alt="asset.alt"
                                  :title="asset.title"
                                  :img-width="asset.imgWidth"
                                  :img-height="asset.imgHeight"
                                  :aspect-ratio="asset.aspectRation"
                                  :expected-width="asset.expectedWidth"
                                  @click="nextSlide"
                                />
                                <div v-else-if="asset.youtube" class="side-nav-gallery-youtube-container">
                                  <YoutubeIframe
                                    :src="asset.youtube"
                                    :full-height="true"
                                    class="side-nav-gallery-youtube-iframe"
                                    data-test-id="side-nav-gallery-youtube"
                                  />
                                </div>
                              </template>
                              <div v-if="currentSlides.length > 1"
                                   class="carousel-indicators d-flex justify-content-center mt-3"
                              >
                                <span v-for="(slideData, slideIndex) in currentSlides"
                                      :key="slideIndex"
                                      class="dot mx-1 border border-primary"
                                      :class="{
                                        active: slide === slideIndex,
                                        'bg-primary': slide === slideIndex
                                      }"
                                      @click="(event) => {slide = slideIndex; ga4click(event, 'dot click')}"
                                />
                              </div>
                            </b-carousel-slide>
                          </b-carousel>
                          <div v-if="currentSlides.length > 1"
                               class="arrow arrow-l border border-dark cursor-pointer d-sm-block z-index-1 bg-transparent position-absolute p-1"
                               role="button"
                               @click.stop="previousSlide"
                          />
                          <div v-if="currentSlides.length > 1"
                               class="arrow arrow-r border border-dark cursor-pointer d-sm-block z-index-1 bg-transparent position-absolute p-1"
                               role="button"
                               @click.stop="nextSlide"
                          />
                        </div>
                        <div class="feature-description mt-3"
                             :class="selectedColorVariant.text"
                             v-html="features[activeIndex].description"
                        />
                      </div>
                    </transition>
                  </template>
                </li>
              </ul>
            </div>

            <transition name="fadeInUp">
              <div :key="'carousel-' + activeIndex"
                   class="carousel-wrapper d-none d-lg-block position-relative mx-auto"
              >
                <div class="position-relative">
                  <b-carousel v-if="currentSlides.length > 0"
                              v-model="slide"
                              :interval="0"
                              :class="selectedColorVariant.arrows"
                              @sliding-start="onSlideStart"
                              @sliding-end="onSlideEnd"
                  >
                    <b-carousel-slide v-for="(asset, index) in currentSlides" :key="index">
                      <template #img>
                        <video
                          v-if="asset.video"
                          class="w-100 h-100"
                          :data-src="asset.video"
                          :poster="asset.poster"
                          controls
                        >
                          <source :src="asset.video" type="video/webm">
                          <source :src="asset.video" type="video/mp4">
                          Your browser does not support HTML5 video.
                        </video>
                        <ImageSet
                          v-else-if="asset.img"
                          class="w-100 h-100 cursor-pointer"
                          :preload="true"
                          :src="asset.img"
                          :alt="asset.alt"
                          :title="asset.title"
                          :img-width="asset.imgWidth"
                          :img-height="asset.imgHeight"
                          :aspect-ratio="asset.aspectRation"
                          :expected-width="asset.expectedWidth"
                          :maintain-aspect-ratio="asset.maintainAspectRatio"
                          @click="nextSlide"
                        />
                        <div v-else-if="asset.youtube" class="side-nav-gallery-youtube-container">
                          <YoutubeIframe
                            :src="asset.youtube"
                            :full-height="true"
                            class="side-nav-gallery-youtube-iframe"
                            data-test-id="side-nav-gallery-youtube"
                          />
                        </div>
                      </template>
                      <div v-if="currentSlides.length > 1"
                           class="carousel-indicators d-flex justify-content-center mt-3"
                      >
                        <span v-for="(slideData, slideIndex) in currentSlides"
                              :key="slideIndex"
                              class="dot mx-1 border border-primary cursor: pointer"
                              :class="{
                                active: slide === slideIndex,
                                'bg-primary': slide === slideIndex
                              }"
                              @click="(event) => {slide = slideIndex; ga4click(event, 'dot click')}"
                        />
                      </div>
                    </b-carousel-slide>
                  </b-carousel>

                  <div v-if="currentSlides.length > 1"
                       class="arrow arrow-l border border-dark cursor-pointer z-index-1 bg-transparent position-absolute p-1"
                       role="button"
                       @click="previousSlide"
                  />
                  <div v-if="currentSlides.length > 1"
                       class="arrow arrow-r border border-dark cursor-pointer z-index-1 bg-transparent position-absolute p-1"
                       role="button"
                       @click="nextSlide"
                  />
                </div>
                <transition-group tag="div"
                                  class="w-100"
                                  data-test-id="flexible-listing__cards"
                                  :name="animationEnabled === 'true' ? 'slide-right' : ''"
                >
                  <div v-for="(feature, index) in features"
                       v-show="activeIndex === index"
                       :key="feature.id"
                       :data-test-id="`feature-description-${index}`"
                       class="carousel-description feature-description mt-3"
                  >
                    <div :class="selectedColorVariant.text" v-html="feature.description" />
                  </div>
                </transition-group>
              </div>
            </transition>
          </div>
        </transition>
      </div>
    </div>
  </section>
</template>

<script>
import {computed, inject, nextTick, onMounted, ref, useContext} from '@nuxtjs/composition-api'
import {LIGHT_VARIANT, VARIANTS_CLASSES} from '~/constants/variants/sideNavGalleryVariants.js'
import {BUTTON_COLORS, BUTTON_NAMES} from '~/constants/buttons'
import useBreakpoints from '~/helpers/composables/useBreakpoints'

export default {
  props: {
    content: {
      type: String
    },
    cta: {
      type: Object
    },
    showCTA: {
      type: String
    },
    tabs: {
      type: Object,
      required: true
    },
    variant: {
      type: String,
      default: LIGHT_VARIANT
    },
    buttonColor: {
      type: String,
      default: BUTTON_COLORS.PRIMARY
    },
    animationEnabled: {
      type: String,
      default: 'false'
    }
  },
  setup(props) {
    const {$utils, $regions, $ga4} = useContext()
    const {tabs, variant} = props
    const {mobile, tablet, desktop} = useBreakpoints()
    const isVisible = ref(false)
    const observerRef = ref(null)

    onMounted(() => {
      nextTick(() => {
        if (!observerRef.value) {
          return
        }

        const animationOffset = 200
        let startScroll = null

        const scrollHandler = () => {
          if (startScroll === null) {
            return
          }

          const scrolled = window.scrollY - startScroll

          if (scrolled >= animationOffset) {
            isVisible.value = true
            window.removeEventListener('scroll', scrollHandler)
          }
        }

        const observer = new IntersectionObserver(([entry], obs) => {
          if (entry.isIntersecting) {
            const offsetFromBottom = window.innerHeight - observerRef.value.getBoundingClientRect().top
            if (offsetFromBottom >= animationOffset) {
              // element already sufficiently visible
              isVisible.value = true
            } else {
              startScroll = window.scrollY
              window.addEventListener('scroll', scrollHandler)
              scrollHandler()
            }
            obs.disconnect()
          }
        }, {
          threshold: 0.1
        })

        observer.observe(observerRef.value)
      })
    })

    const model = inject('model')

    const ctaLink = computed(() => {
      if (props.cta?.link) {
        return $utils.getLinkField(props.cta, $regions.current.language.name)
      }
      return null
    })

    const slide = ref(0)
    const activeIndex = ref(0)
    const sliding = ref(null)

    const features = computed(() => {
      return tabs['@nodes'].map((nodeKey, index) => {
        const tabData = tabs[nodeKey]
        return {
          id: nodeKey + tabData.title + tabData['mgnl:lastModified'] + tabData['@id'],
          title: tabData.title,
          subtitle: tabData.subtitle,
          description: tabData.description,
          link: (tabData.tabLink?.link) ? $utils.getLinkField(tabData.tabLink, $regions.current.language.name) : null,
          assets: tabData.galleryImages?.['@nodes'].map(imageNodeKey => getAsset(tabData.galleryImages[imageNodeKey]))
        }
      })
    })

    const getAsset = (asset) => {
      if (asset?.img?.src) {
        const obj = {}
        const {metadata} = $utils.getCloudinaryAssetMetadata(asset?.img?.src)
        Object.assign(obj, {
          img: asset?.img?.src,
          title: asset?.img?.imageTitle || metadata?.context?.caption || metadata?.title,
          alt: asset?.img?.imageAlt || metadata?.context?.alt || metadata?.description || metadata?.['alt_' + $regions.current.language.name],
          imgWidth: asset?.img?.imageWidth || null,
          imgHeight: asset?.img?.imageHeight || null,
          expectedWidth: !desktop.value ? (mobile.value ? 480 : 640) : 640,
          aspectRation: (32 / 21),
          maintainAspectRatio: asset?.img?.maintainAspectRatio === 'true',
          ...metadata
        })
        return obj
      }

      if (asset?.video?.src) {
        const obj = {}
        const {metadata} = $utils.getCloudinaryAssetMetadata(asset?.video?.src)
        Object.assign(obj, {
          video: $utils.getCloudinaryAsset(asset?.video?.src, false),
          poster: $utils.getCloudinaryAsset(asset?.video?.poster) || $utils.getCloudinaryAsset(asset?.video?.src, false, {
            startOffset: 0,
            effect: 'preview',
            resource_type: 'video'
          }),
          title: metadata?.context?.caption || metadata?.title,
          alt: metadata?.context?.alt || metadata?.description,
          ...metadata
        })
        return obj
      }

      if (asset?.youtube) {
        const id = asset.youtube
          .split(asset.youtube.includes('/embed/') ? '/embed/' : 'watch?v=')
          .pop()
        return {
          youtube: `https://www.youtube.com/embed/${id}`
        }
      }

      return {}
    }

    const selectedColorVariant = ref(VARIANTS_CLASSES[variant])

    const currentSlides = ref([])
    currentSlides.value = features.value[0]?.assets || []

    const changeSlide = (event, index) => {
      const li = process.client ? event?.target?.closest?.('[data-test-id^="feature-item-"]') : null

      activeIndex.value = index
      currentSlides.value = features.value[index]?.assets || []
      slide.value = 0
      ga4click(event, 'expand')

      // AV-3257: scroll the opened feature into view on the <992px in-list layout.
      if (process.client && (mobile.value || tablet.value) && li) {
        const header = document.querySelector('header[data-test-id="main-header"]')
        const notificationBar = document.querySelector('[data-test-id="notification-bar-content"]')
        const headerOffset = (header ? (header.clientHeight - 1) : 0) +
          (!$utils.isSmallMobile() && notificationBar ? notificationBar.clientHeight : 0)
        let stableFrames = 0
        let lastHeight = -1

        const settleThenScroll = (budget) => {
          const h = document.documentElement.scrollHeight
          stableFrames = (h === lastHeight) ? stableFrames + 1 : 0
          lastHeight = h

          if (stableFrames < 3 && budget > 0) {
            requestAnimationFrame(() => settleThenScroll(budget - 1))
            return
          }
          if (stableFrames < 3) { return }

          const rectTop = li.getBoundingClientRect().top
          if (rectTop >= headerOffset && li.getBoundingClientRect().bottom <= window.innerHeight) {
            return
          }
          const targetY = window.scrollY + rectTop - headerOffset
          window.scrollTo({top: targetY > 0 ? targetY : 0, behavior: 'smooth'})
        }

        nextTick(() => requestAnimationFrame(() => settleThenScroll(60)))
      }
    }

    const previousSlide = (event) => {
      slide.value = slide.value > 0 ? slide.value - 1 : currentSlides.value.length - 1
      ga4click(event, 'left arrow click')
    }

    const nextSlide = (event) => {
      slide.value = slide.value < currentSlides.value.length - 1 ? slide.value + 1 : 0
      ga4click(event, 'right arrow click')
    }

    const onSlideStart = () => {
      sliding.value = true
    }

    const onSlideEnd = () => {
      sliding.value = false
    }

    const ga4click = (event, action) => {
      $ga4.events.side_nav_gallery_click(event, model, props.content, action)
    }

    return {
      BUTTON_NAMES,
      slide,
      activeIndex,
      sliding,
      features,
      currentSlides,
      changeSlide,
      onSlideStart,
      onSlideEnd,
      previousSlide,
      nextSlide,
      selectedColorVariant,
      ctaLink,
      ga4click,
      isVisible,
      observerRef
    }
  }
}
</script>

<style lang="scss">
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '[media-breakpoint-up] from ~bootstrap/scss/mixins';

section[data-test-id="side-nav-gallery"] {

  &.evolution-gradient {
    background-image: url('~/assets/evolution-gradient.png');
    background-size: 500px;
    background-repeat: no-repeat;
    background-position: top right;
  }

  .border-left-selected {
    border-left: 0.375rem solid $primary;
  }

  .side-nav-gallery-youtube {
    &-container {
      position: relative;
      overflow: hidden;
      width: 100%;
      padding-top: 56.25%; /* 16:9 Aspect Ratio (divide 9 by 16 = 0.5625) */
    }

    &-iframe {
      position: absolute;
      top: 0;
      left: 0;
      bottom: 0;
      right: 0;
      width: 100%;
      height: 100%;
    }
  }

  .dot {
    border-radius: 60%;
    width: 1rem;
    height: 1rem;
  }

  .carousel-wrapper {
    width: 40rem;
    max-width: 40rem;
    flex-shrink: 0;
  }

  .carousel-description {
    font-size: 0.875rem;
    font-weight: 400;
    line-height: 1.25rem;
  }

  .feature-description,
  .feature-description * {
    text-align: left !important;
  }

  .features-section {
    // AV-3205: gap between CTA link and first list item is 32px (padding-top).
    // The extra 8px (pb-2) previously on the content wrapper was removed.
    padding-top: 2rem !important;
  }

  .features-list {
    min-width: 50%;
  }

  .arrow {
    width: 1.25rem;
    height: 1.25rem;
    border-width: 0 4px 4px 0 !important;
    top: 50%;
    transform: translateY(-50%);

    &-l {
      left: 1rem;
      transform: rotate(135deg);
    }

    &-r {
      right: 1rem;
      transform: rotate(-45deg);
    }
  }

  .carousel-control-prev-icon,
  .carousel-control-next-icon {
    background-image: none;
  }

  .container {
    padding: 0 !important;

    @include media-breakpoint-up(lg) {
      padding: 0 .9375rem !important;
    }
  }

  iframe {
    width: 100%;
  }

  .feature-item {
    border-left: 0.375rem solid transparent;
    padding: 0.5rem 0.5rem 0.5rem 2rem;

    &:not(:last-child) {
      margin-bottom: 0.5rem;
    }

    &.is-active > .overflow-hidden {
      margin-top: 1rem;
    }

    > .btn {
      padding-top: 0 !important;
      padding-bottom: 0 !important;
    }

    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: -6px;
      width: 6px;
      height: 0;
      background-color: $primary;
      transition: height 0.4s cubic-bezier(0.215, 0.61, 0.355, 1);
      z-index: 1;
      transform-origin: top;
    }

    &.is-active {
      &::before {
        animation: borderBounce 0.6s cubic-bezier(0.215, 0.61, 0.355, 1);
        height: 100%;
      }
    }

    &:not(.is-active) {
      &::before {
        transition: height 0.25s ease-out;
        height: 0;
      }
    }
  }

  .feature-item-arrow-icon {
    width: 1.5rem;
    height: 1.5rem;
    display: block;
    fill: currentColor;
  }

  .chevron-icon {
    width: 1.5rem;
    height: 1.5rem;
    align-self: center;
    flex-shrink: 0;
  }

  .feature-item-mobile {
    margin-left: -1.4rem;
  }

  .side-nav-content {
    h1, h2, h3, h4, h5, h6, p {
      margin: 0 0 0.75rem 0;
      padding: 0;
    }
    h1, h2, h3 {
      line-height: 1.25;
    }
    h4, h5, h6, p {
      line-height: 1;
    }
    h1 {
        font-size: 3rem;
    }
  }
  .side-nav-content-no-space {
    h1, h2, h3, h4, h5, h6, p {
      margin: 0;
      padding: 0;
    }
  }
}
</style>
