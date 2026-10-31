<template>
  <div>
    <section ref="heroSpot"
             class="evolution-offers-banner d-flex flex-column align-items-center justify-content-center w-100 overflow-hidden"
             :class="
               [
                 VARIANTS_CLASSES[variant]?.background || '',
                 VARIANTS_CLASSES[variant]?.text || ''
               ]"
             data-test-id="evolution-offers-banner"
             tabindex="0"
             @focus="stopAutoPlayOnFocus"
             @blur="startAutoPlayOnBlur"
             @mouseenter="stopAutoPlay"
             @mouseleave="startAutoPlay"
    >
      <template v-if="heroSlides.length">
        <button
          v-if="doCarousel"
          type="button"
          class="col-1 bg-transparent position-absolute arrow arrow-l z-index-1"
          data-test-id="offers-banner-arrow-left"
          :disabled="sliding"
          @click.prevent="(event) => {ga4click(event, 'left arrow click'); setSlideAndStop(prev)}"
        />
        <button
          v-if="doCarousel"
          type="button"
          class="col-1 bg-transparent position-absolute arrow arrow-r z-index-1"
          data-test-id="offers-banner-arrow-right"
          :disabled="sliding"
          @click.prevent="(event) => {ga4click(event, 'right arrow click'); setSlideAndStop(next)}"
        />

        <LazyHydrate never :trigger-hydration="heroSlides.length > 1">
          <div class="d-flex flex-column justify-content-center w-100 carousel">
            <BCarousel id="heroSpot"
                       ref="carousel"
                       v-model="slide"
                       class="w-100 carousel"
                       :interval="slideGap"
                       @sliding-start="onSlideStart"
                       @sliding-end="onSlideEnd"
            >
              <div v-for="(item, i) in heroSlides"
                   :key="item['@id']"
                   class="carousel-item"
                   :class="i === 0 ? 'active' : ''"
              >
                <div class="container content-wrapper px-6 px-xl-3 d-flex justify-content-center align-items-center">
                  <div class="my-2 banner-text" v-html="item.text" />
                </div>
              </div>
            </BCarousel>
          </div>
        </LazyHydrate>
      </template>
      <WarningAlert v-else-if="inEditorEdit" data-test-id="offers-banner-no-slides-msg">No slides defined</WarningAlert>
    </section>
  </div>
</template>

<script>
import {BCarousel} from 'bootstrap-vue'
import {onBeforeUnmount, onMounted, useContext, inject, ref} from '@nuxtjs/composition-api'
import useHeroCarousel from '~/helpers/composables/useHeroCarousel'
import {VARIANTS_CLASSES} from '@/constants/variants/evolutionOffersBannerVariants'
import {inEditorEdit} from '~/helpers/vue-editor'
import useMounted from '~/helpers/composables/useMounted'
import useProductSalesProgram from '~/helpers/composables/useProductSalesProgram'
import {MODEL_LIFECYCLE} from '~/constants'

export default {
  components: {
    BCarousel
  },
  props: {
    nodeId: {
      type: String
    },
    variant: {
      type: String,
      default: 'black'
    },
    slides: {
      type: Object
    },
    autoPlay: {
      type: String,
      default: 'true'
    },
    interval: {
      type: String
    }
  },
  setup(props) {
    const model = inject('model')
    const pageTemplateId = inject('pageTemplateId')
    const isHistoricalDetails = pageTemplateId?.includes(MODEL_LIFECYCLE.HISTORICAL)
    const historicalContext = ref(model?.lifecycle?.toLowerCase() === MODEL_LIFECYCLE.HISTORICAL)
    const {
      specialOfferDetails,
      specialOfferDetails2
    } = useProductSalesProgram(historicalContext || isHistoricalDetails)
    useMounted(props.nodeId)
    const {heroSlides, doCarousel, carousel, setSlide, slide, slideGap, onSlideEnd, next, prev, setSlideAndStop, sliding, onSlideStart, heroSpot, startAutoPlay, startAutoPlayOnBlur, stopAutoPlay, stopAutoPlayOnFocus, stopAutoPlayPermanent, isFocused, assetsSchema, getCurrentSlideData} = useHeroCarousel(props, false, [specialOfferDetails.value, specialOfferDetails2.value])
    const {$ga4} = useContext()

    const ga4click = (event, action = 'link click') => {
      const currentSlide = getCurrentSlideData()
      $ga4.events.offers_banner_click(currentSlide.text, event?.target?.innerText, action)
    }

    const heroSpotClick = (event) => {
      if (event.target.tagName.toLowerCase() === 'a') {
        ga4click(event)
      }
    }

    onMounted(() => {
      if (!heroSpot.value) { return }
      heroSpot.value.addEventListener('click', heroSpotClick)
    })

    onBeforeUnmount(() => {
      heroSpot.value.removeEventListener('click', heroSpotClick)
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
      VARIANTS_CLASSES,
      ga4click
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~/assets/sass/utils';
@import '~bootstrap/scss/mixins';
@import '~bootstrap/scss/carousel';

$banner-offset-sm: 500px;
$banner-offset-xs: 250px;

body {
  .evolution-offers-banner {
    transition: $transition-base;
    min-height: 3.5rem;
  }

  &.tablet {
    .evolution-offers-banner {
      height: 5rem;
    }
  }

  &.mobile {
    .evolution-offers-banner {
      height: 8rem;
    }
  }
}

.evolution-offers-banner {
  z-index: $zindex-modal-backdrop;

  @include media-breakpoint-down(sm) {
    position: relative;
    z-index: $zindex-sticky;
  }
  .content-wrapper {
    max-width: 720px !important;
  }

  &.bottom-separator {
    border-bottom: 3px solid $tertiary;
  }

  .arrow {
    position: absolute;
    width: 1.25rem;
    height: 1.25rem;
    border: solid $black;
    border-width: 0 4px 4px 0;
    cursor: pointer;
    padding: 4px;
    top: calc(50% - 0.625rem);

    &-l {
      left: 0.5rem;
      transform: rotate(135deg);
      -webkit-transform: rotate(135deg);
    }

    &-r {
      right: 0.5rem;
      transform: rotate(-45deg);
      -webkit-transform: rotate(-45deg);
    }

    @include media-breakpoint-up(md) {
      &-l { left: calc(40% - #{$banner-offset-xs} - 1rem); }
      &-r { right: calc(40% - #{$banner-offset-xs} - 1rem); }
    }

    @include media-breakpoint-up(xl) {
      &-l { left: calc(60% - #{$banner-offset-sm} - 1rem); }
      &-r { right: calc(60% - #{$banner-offset-sm} - 1rem); }
    }

    @include media-breakpoint-up(xxl) {
      &-l { left: calc(50% - #{$banner-offset-sm} - 1rem); }
      &-r { right: calc(50% - #{$banner-offset-sm} - 1rem); }
    }
  }

  &.text-white {
    .arrow {
      border-color: $white;
    }
  }

  .carousel {
    top: 0;
    left: 0;

    .container {
      :deep(h1),
      :deep(h2),
      :deep(h3),
      :deep(h4),
      :deep(h5),
      :deep(h6),
      :deep(p) {
        margin-bottom: 0;
      }
    }
  }

  .banner-text {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: normal;
      text-align: center !important;
      word-break: break-word;
      p {
        margin-bottom: 0;
      }
    }
}

</style>
