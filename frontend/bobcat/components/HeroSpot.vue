<template>
  <section ref="heroSpot"
           class="herospot d-flex flex-column align-items-center justify-content-center w-100 hero-wrapper position-relative overflow-hidden"
           :class="{'mb-5': spacingBottom === 'true'}"
           data-test-id="herospot"
           tabindex="0"
           :title="slideTitle"
           @focus="stopAutoPlayOnFocus"
           @blur="startAutoPlayOnBlur"
           @mouseenter="stopAutoPlay"
           @mouseleave="startAutoPlay"
  >
    <template v-if="heroSlides.length">
      <div class="overlay w-100 h-100 position-absolute" />
      <div class="position-relative container w-100 d-flex justify-content-md-end justify-content-center">
        <div class="text-break row d-flex flex-column justify-content-center col-12 col-md-6 col-lg-8 mr-sm-2">
          <component :is="htag" class="text-white h3">
            {{ heroSlides[slide].header }}
          </component>
          <p class="text-white mb-3">{{ heroSlides[slide].text }}</p>
          <a
            v-if="heroSlides[slide].button.link && heroSlides[slide].button.link.length"
            :href="heroSlides[slide].button.link"
            :target="heroSlides[slide].button.target === '_blank_follow' ? '_blank' : heroSlides[slide].button.target || '_self'"
            :title="heroSlides[slide].button.title"
            :rel="heroSlides[slide].button.target === '_blank' ? 'noopener nofollow' : null"
            class="d-lg-flex justify-content-center btn-mobile btn-lg mb-3 text-capitalize align-self-start btn btn-primary"
            data-test-id="herospot-button"
            @click="ga4click"
          >
            {{ heroSlides[slide].button.title }}
          </a>
          <ul v-if="doCarousel" class="d-flex list-unstyled mb-0" data-test-id="herospot-dots">
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
        <button
          v-if="doCarousel"
          type="button"
          class="d-md-block d-none col-1 bg-transparent position-absolute arrow arrow-l"
          data-test-id="herospot-arrow-left"
          :disabled="sliding"
          @click.prevent="setSlideAndStop(prev)"
        />
        <button
          v-if="doCarousel"
          type="button"
          class="d-md-block d-none col-1 bg-transparent position-absolute arrow arrow-r"
          data-test-id="herospot-arrow-right"
          :disabled="sliding"
          @click.prevent="setSlideAndStop(next)"
        />
      </div>

      <LazyHydrate never :trigger-hydration="heroSlides.length > 1">
        <div class="d-flex flex-column justify-content-center w-100 h-100 position-absolute carousel">
          <BCarousel id="heroSpot"
                     ref="carousel"
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
                       data-test-id="herospot-video"
                       :poster="item.poster"
                >
                  <source :src="item.video" type="video/webm">
                  <source :src="item.video" type="video/mp4">
                  Your browser does not support HTML5 video.
                </video>
              </div>
              <ImageSet v-else-if="item.img"
                        class="img-fluid w-100 h-100"
                        :data-test-id="`herospot-image__${i}`"
                        :options="{...(i === 0 && {loading: 'eager', fetchpriority: 'highest'})}"
                        :preload="i === 0"
                        :src="item.img"
                        :alt="item.alt"
                        :title="item.title"
                        :img-width="item.imgWidth"
                        :img-height="item.imgHeight"
                        :aspect-ratio="null"
                        :cloudinary-options="{height: 480}"
                        :expected-width="item.expectedWidth"
              />
            </div>
          </BCarousel>
        </div>
      </LazyHydrate>
    </template>
    <WarningAlert v-else-if="inEditorEdit" data-test-id="herospot-no-slides-msg">No slides defined</WarningAlert>
  </section>
</template>

<script>
import {BCarousel} from 'bootstrap-vue'
import {useContext, computed} from '@nuxtjs/composition-api'
import useHeroCarousel from '~/helpers/composables/useHeroCarousel'
import {inEditorEdit} from '~/helpers/vue-editor'
import useMounted from '~/helpers/composables/useMounted'

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
    title: {
      type: String
    }
  },
  setup(props) {
    const {heroSlides, doCarousel, carousel, setSlide, slide, slideGap, onSlideEnd, next, prev, setSlideAndStop, sliding, onSlideStart, heroSpot, startAutoPlay, startAutoPlayOnBlur, stopAutoPlay, stopAutoPlayOnFocus, stopAutoPlayPermanent, isFocused, assetsSchema, getCurrentSlideData} = useHeroCarousel(props)
    useMounted(props.nodeId)
    const {$ga4} = useContext()

    const ga4click = () => {
      const currentSlide = getCurrentSlideData()
      $ga4.events.hero_spot_cta('click', currentSlide)
    }

    const slideTitle = computed(() => {
      if (heroSlides.length > 0) {
        const currentSlide = getCurrentSlideData()
        return currentSlide.title
      }
      return props.title
    })

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
      inEditorEdit,
      assetsSchema,
      ga4click,
      slideTitle
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
@import '~/assets/sass/utils';
@import '~bootstrap/scss/mixins';
@import '~bootstrap/scss/carousel';

.hero-wrapper {
  height: 30vw;
  min-height: 30rem;

  .overlay {
    background: linear-gradient(270deg, $black 0%, rgba(0, 0, 0, 0) 100%);
    z-index: -1;
    top: 0;
    left: 0;

    @include media-breakpoint-down(sm) {
      background: linear-gradient(0, $black 0%, rgba(0, 0, 0, 0) 100%);
    }
  }

  h2 {
    line-height: 3.375rem;
  }

  .arrow {
    width: 1.25rem;
    height: 1.25rem;
    border: solid $white;
    border-width: 0 4px 4px 0;
    cursor: pointer;
    padding: 4px;
    top: 50%;

    &-l {
      left: 1rem;
      transform: rotate(135deg);
      -webkit-transform: rotate(135deg);
    }

    &-r {
      right: 1rem;
      transform: rotate(-45deg);
      -webkit-transform: rotate(-45deg);
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

  @media (max-width: 340px) {
    height: 36rem;
  }
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

.dot {
  border-radius: 60%;
  width: 1rem;
  height: 1rem;
  cursor: pointer;
}
</style>
