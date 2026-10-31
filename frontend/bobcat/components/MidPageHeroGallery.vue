<template>
  <section class="wrapper" data-test-id="mid-page-herospot" :class="{'mb-5': spacingBottom === 'true'}">
    <div ref="heroSpot"
         class="herospot d-flex flex-column align-items-center justify-content-center w-100 hero-wrapper position-relative overflow-hidden"
         :style="{aspectRatio: computedAspectRatio}"
         data-test-id="mid-page-herospot"
         tabindex="0"
    >
      <template v-if="heroSlides.length">
        <div class="position-relative w-100 d-flex justify-content-md-end justify-content-center">
          <button
            v-if="doCarousel"
            type="button"
            class="position-absolute top-50 arrow-wrapper arrow-wrapper-l d-flex align-items-center justify-content-center p-3 cursor-pointer border-0 text-white font-weight-bold"
            data-test-id="mid-page-herospot-arrow-left"
            :disabled="sliding"
            @click.prevent="setSlideAndStop(prev)"
          >
            &lt;
          </button>
          <button
            v-if="doCarousel"
            type="button"
            class="position-absolute top-50 arrow-wrapper arrow-wrapper-r d-flex align-items-center justify-content-center p-3 cursor-pointer border-0 text-white font-weight-bold"
            data-test-id="mid-page-herospot-arrow-right"
            :disabled="sliding"
            @click.prevent="setSlideAndStop(next)"
          >
            &gt;
          </button>
        </div>

        <div class="d-flex flex-column justify-content-center w-100 h-100 position-absolute carousel">
          <BCarousel ref="carousel"
                     v-model="slide"
                     class="w-100 h-100 carousel"
                     :interval="slideGap"
                     @sliding-start="onSlideStart"
                     @sliding-end="onSlideEnd"
          >
            <div v-for="(item, i) in heroSlides"
                 :key="item['@id']"
                 class="carousel-item"
                 :class="i === 0 ? 'active' : ''"
            >
              <div v-if="item.video" class="d-flex w-100 h-100">
                <video class="w-100 h-100 bg-movie"
                       autoplay
                       muted
                       loop
                       :data-src="item.video"
                       :preload="i === 0 ? 'auto' : 'none'"
                       playsinline
                       data-test-id="mid-page-herospot-video"
                       :poster="item.poster"
                >
                  <source :src="item.video" type="video/webm">
                  Your browser does not support HTML5 video.
                </video>
              </div>

              <ImageSet v-else-if="item.img"
                        class="img-fluid w-100 h-100"
                        :data-test-id="`mid-page-herospot-image__${i}`"
                        :options="{...(i === 0 && {loading: 'eager', fetchpriority: 'highest'})}"
                        :preload="i === 0"
                        :src="item.img"
                        :alt="item.alt"
                        :title="item.title"
                        :img-width="item.imgWidth"
                        :img-height="item.imgHeight"
                        :expected-width="item.expectedWidth"
                        :aspect-ratio="null"
              />
            </div>
          </BCarousel>
        </div>
      </template>
      <WarningAlert v-else-if="inEditorEdit" data-test-id="mid-page-herospot-no-slides-msg">No slides defined</WarningAlert>
    </div>

    <div>
      <div v-if="heroSlides[slide].header || heroSlides[slide].button.title || heroSlides[slide].text" class="w-100 d-flex flex-column flex-md-row bg-gray-darker p-3 p-md-4">
        <div class="text-break col-12 col-md-10 mid-page-content">
          <component :is="htag" class="text-white h3">
            {{ heroSlides[slide].header }}
          </component>
          <p class="text-white mb-3" v-html="heroSlides[slide].text" />
        </div>
        <div class="ml-auto d-md-flex align-items-center">
          <a
            v-if="heroSlides[slide].button.link && heroSlides[slide].button.link.length"
            :href="heroSlides[slide].button.link"
            :target="heroSlides[slide].button.target === '_blank_follow' ? '_blank' : heroSlides[slide].button.target || '_self'"
            :title="heroSlides[slide].button.title"
            :rel="heroSlides[slide].button.target === '_blank' ? 'noopener nofollow' : null"
            class="text-capitalize btn btn-primary btn-lg"
            data-test-id="mid-page-herospot-button"
            @click="ga4click"
          >
            {{ heroSlides[slide].button.title }}
          </a>
        </div>
      </div>

      <ul v-if="doCarousel"
          class="d-flex list-unstyled mb-0 w-full py-2 d-flex justify-content-center"
          data-test-id="mid-page-herospot-dots"
      >
        <li
          v-for="(item, index) in heroSlides"
          :key="item['@id']"
          class="mr-3 d-flex dot border border-1 border-primary transition-slide"
          :class="[slide === index ? 'bg-primary' : 'bg-transparent']"
          :disabled="sliding"
          @click.prevent="setSlide(index)"
        />
      </ul>
    </div>
  </section>
</template>

<script>
import {computed, useContext} from '@nuxtjs/composition-api'
import {BCarousel} from 'bootstrap-vue'
import {inEditorEdit} from '~/helpers/vue-editor'
import useMounted from '~/helpers/composables/useMounted'
import useHeroCarousel from '~/helpers/composables/useHeroCarousel'

export default {
  components: {
    BCarousel
  },
  props: {
    nodeId: {
      type: String
    },
    slides: {
      type: Object
    },
    interval: {
      type: String
    },
    spacingBottom: {
      type: String,
      default: 'false'
    },
    htag: {
      type: String,
      default: 'h1'
    },
    aspectRatio: {
      type: Object
    }
  },
  setup(props) {
    const {
      heroSlides,
      doCarousel,
      carousel,
      setSlide,
      slide,
      slideGap,
      onSlideEnd,
      next,
      prev,
      setSlideAndStop,
      sliding,
      onSlideStart,
      heroSpot,
      startAutoPlay,
      startAutoPlayOnBlur,
      stopAutoPlay,
      stopAutoPlayOnFocus,
      stopAutoPlayPermanent,
      isFocused,
      assetsSchema,
      getCurrentSlideData
    } = useHeroCarousel(props, true)
    const computedAspectRatio = computed(() => {
      const [width, height] = props.aspectRatio.field.split(':').map(Number)
      return width / height
    })
    const {$ga4} = useContext()

    const ga4click = () => {
      const currentSlide = getCurrentSlideData()
      $ga4.events.hero_spot_cta('click', currentSlide)
    }

    useMounted(props.nodeId)

    return {
      heroSlides,
      doCarousel,
      carousel,
      slide,
      sliding,
      slideGap,
      heroSpot,
      isFocused,
      inEditorEdit,
      assetsSchema,
      computedAspectRatio,
      onSlideStart,
      onSlideEnd,
      next,
      prev,
      setSlide,
      setSlideAndStop,
      stopAutoPlay,
      startAutoPlay,
      stopAutoPlayPermanent,
      stopAutoPlayOnFocus,
      startAutoPlayOnBlur,
      ga4click
    }
  },
  head() {
    return this?.heroSlides?.[0]?.video
      ? {
        link: [
          {
            rel: 'preload',
            as: 'image',
            fetchpriority: 'highest',
            href: this?.heroSlides?.[0]?.poster
          },
          {
            rel: 'preload',
            as: 'video',
            fetchpriority: 'highest',
            href: this?.heroSlides?.[0]?.video,
            type: 'video/mp4'
          }
        ]
      }
      : {}
  },
  jsonld() {
    return this.assetsSchema.filter(x => x)
  }
}
</script>

<style lang="scss" scoped>
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~bootstrap/scss/mixins';
@import '~bootstrap/scss/carousel';

.wrapper {
  max-width: 1440px;
  margin: 0 auto;
}
section[data-test-id='mid-page-herospot'] {
  ::v-deep iframe {
    width: 100%;
  }
}

.hero-wrapper {

  .arrow-wrapper {
    width: 3rem;
    height: 3rem;
    background: rgba(0, 0, 0, .3);
    font-size: 1.5rem;
    transform: translateY(-50%);

    &-l {
      left: 0;
    }

    &-r {
      right: 0;
    }

  }

  .carousel {
    top: 0;
    left: 0;
    z-index: -2;

    img, video {
      object-fit: cover;
    }
  }
}

.mid-page-content {
  max-width: 45rem
}

.dot {
  border-radius: 60%;
  width: 1rem;
  height: 1rem;
  cursor: pointer;
}
</style>

<style lang="scss">
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~/assets/sass/utils';
@import '~bootstrap/scss/mixins';

.herospot {
  .carousel
  .carousel-inner,
  .carousel-item.active,
  .carousel-item-next,
  .carousel-item-prev {
    height: 100%;
    width: 100%;
    @include media-breakpoint-down(sm) {
      max-height: 36rem;
    }
  }
}
</style>
