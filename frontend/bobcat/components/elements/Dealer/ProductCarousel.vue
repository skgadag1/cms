<template>
  <b-overlay :show="isFetching">
    <div class="container position-relative px-sm-6 px-lg-5 overflow-hidden" data-test-id="product-carousel">
      <div class="d-flex justify-content-center overflow-hidden pb-3">
        <ul ref="slider"
            data-test-id="product-carousel-container"
            class="row flex-nowrap transition-base w-100 list-unstyled mb-0"
        >
          <li v-for="(item, i) in results"
              :key="item['@id'] || Math.random().toString(16).slice(2, 8)"
              :data-test-id="`product-carousel-item-${i}`"
              class="col-12 col-md-4 col-lg-3"
              :class="{
                'offset-md-2 offset-lg-3': i === 0 && results.length === 2,
                'col-lg-4': results.length === 3,
              }"
          >
            <LazyHydrate :on-interaction="['click', 'touchstart']">
              <div class="row align-items-center justify-content-center h-100">
                <div class="col-10 col-sm-12 h-100">
                  <DealerInventorySearchProduct :product="item"
                                                :category="category"
                                                :location="location"
                                                :distance="distance"
                                                is-product-carousel
                                                :cta-settings="ctaSettings"
                                                :labels="labels"
                  />
                </div>
              </div>
            </LazyHydrate>
          </li>
        </ul>
      </div>

      <button
        v-if="results?.length > visibleSlideAmount"
        type="button"
        class="col-1 bg-transparent position-absolute arrow arrow-l"
        :class="!prevSlideActive ? 'border-color-gray-light' : null"
        :style="!prevSlideActive ? 'cursor: initial;' : null"
        data-test-id="product-carousel-arrow-left"
        title="Previous"
        :disabled="!prevSlideActive"
        @click.prevent="(e) => {ga4click(e); goToSlide(currentSlidePosition - 1)}"
      />

      <button
        v-if="results?.length > visibleSlideAmount"
        type="button"
        class="col-1 bg-transparent position-absolute arrow arrow-r"
        data-test-id="product-carousel-arrow-right"
        title="Next"
        :class="!nextSlideActive ? 'border-color-gray-light' : null"
        :style="!nextSlideActive ? 'cursor: initial;' : null"
        :disabled="!nextSlideActive"
        @click.prevent="(e) => {ga4click(e); goToSlide(currentSlidePosition + 1)}"
      />
    </div>
    <div v-if="results?.length - visibleSlideAmount >= 1"
         ref="dots"
         class="d-flex mb-2 position-sticky dots overflow-auto px-3"
    >
      <button v-for="i in results?.length + 1 - visibleSlideAmount"
              :key="`gallery-indicator-products-${i}`"
              href="#"
              data-test-id="product-carousel-dot"
              class="border rounded-circle d-flex justify-content-center align-items-center p-2 my-2 border-primary"
              :class="currentSlidePosition === i - 1 ? 'bg-primary text-white' : 'bg-white'"
              :title="i"
              @click.prevent="(e) => {ga4click(e); goToSlide(i - 1)}"
      />
    </div>
  </b-overlay>
</template>

<script>
import {
  nextTick,
  onBeforeUnmount,
  onMounted,
  ref,
  useContext,
  watch
} from '@nuxtjs/composition-api'
import DealerInventorySearchProduct from './DealerInventorySearchProduct.vue'
import {inEditorEdit} from '~/helpers/vue-editor'

export default {
  components: {DealerInventorySearchProduct},
  props: {
    results: {
      type: Array,
      default: null
    },
    category: {
      type: Object,
      required: true
    },
    location: {
      type: String
    },
    distance: {
      type: Number
    },
    labels: {
      type: Object,
      default: () => ({})
    },
    isFetching: {type: Boolean},
    ctaSettings: {
      type: Object
    }
  },
  setup(props) {
    const {$utils, $regions, $breakpoints, $ga4} = useContext()
    const prevSlideActive = ref(false)
    const nextSlideActive = ref(false)
    const linkLabel = ref(null)
    const slider = ref(null)
    const dots = ref(null)
    const currentTranslate = ref(0)
    const currentSlidePosition = ref(0)
    const touchstartX = ref(0)
    const touchstartY = ref(0)
    const touchendX = ref(0)
    const touchendY = ref(0)
    const visibleSlideAmount = ref(3)

    const slideOnScreen = {
      SM: 1,
      MD: 3,
      LG: 4
    }

    const setSlideAmount = () => {
      if ($utils.isDesktopLG() || $utils.isDesktopXL()) {
        visibleSlideAmount.value = slideOnScreen.LG
      } else if (($utils.isTablet() && !$utils.isMobile())) {
        visibleSlideAmount.value = slideOnScreen.MD
      } else {
        visibleSlideAmount.value = slideOnScreen.SM
      }
    }

    const linkField = ref($utils.getLinkField(props.link, $regions.current.language.name))

    const initialize = () => {
      setSlideAmount()
      if (props.results?.length > visibleSlideAmount.value) {
        nextSlideActive.value = true
        prevSlideActive.value = true
        currentSlidePosition.value = 0
      }
    }

    const goToSlide = (num = currentSlidePosition.value + 1) => {
      currentSlidePosition.value = num < 0 ? props.results?.length - 1 : num % (props.results?.length)
      prevSlideActive.value = currentSlidePosition.value > 0
      nextSlideActive.value = currentSlidePosition.value < props.results?.length - visibleSlideAmount.value

      const cardWidth = (slider.value?.firstChild?.clientWidth || 0) * currentSlidePosition.value

      currentTranslate.value = cardWidth

      slider.value.style.transform = `translateX(-${cardWidth}px)`
    }

    const resetCarousel = () => {
      setSlideAmount()
      prevSlideActive.value = props.results?.length > visibleSlideAmount.value
      nextSlideActive.value = props.results?.length > visibleSlideAmount.value
      currentSlidePosition.value = 0
      currentTranslate.value = 0
      if (slider.value) {
        slider.value.style.transform = `translateX(${currentTranslate.value}px)`
      }
    }

    const handleGesure = () => {
      const isClicked = touchendX.value - touchstartX.value === 0
      if (!isClicked && Math.abs(touchstartY.value - touchendY.value) <= 50) {
        if (touchendX.value <= touchstartX.value) {
          goToSlide(currentSlidePosition.value + 1)
        } else if (touchendX.value >= touchstartX.value) {
          goToSlide(currentSlidePosition.value - 1)
        }
      }
    }

    const initiateSlider = () => {
      slider.value?.addEventListener('touchstart', (event) => {
        touchstartX.value = event.changedTouches['0'].screenX
        touchstartY.value = event.changedTouches['0'].screenY
      }, false)

      slider.value?.addEventListener('touchend', (event) => {
        touchendX.value = event.changedTouches['0'].screenX
        touchendY.value = event.changedTouches['0'].screenY
        handleGesure()
      }, false)
      nextTick(() => {
        const observer = new IntersectionObserver(
          ([e]) => e.target.toggleAttribute('stuck', e.intersectionRatio < 1),
          {threshold: [1]}
        )

        if (dots.value) {
          observer.observe(dots.value)
        }
      })
    }

    if (process.client) {
      onMounted(() => {
        initialize()
        nextTick(() => {
          if ($breakpoints?.mobile || $utils.isMobile()) {
            window.addEventListener('orientationchange', resetCarousel, {passive: true})
          } else {
            window.addEventListener('resize', resetCarousel, {passive: true})
          }
          initiateSlider()
        })
      })
      onBeforeUnmount(() => {
        window.removeEventListener('orientationchange', resetCarousel)
        window.removeEventListener('resize', resetCarousel)
      })

      watch(() => props.results, (items) => {
        if (items?.length) {
          resetCarousel()
        }
      })
    }

    const ga4click = (event) => {
      $ga4.events.carousel_click(event)
    }

    return {
      linkField,
      prevSlideActive,
      nextSlideActive,
      visibleSlideAmount,
      goToSlide,
      slider,
      dots,
      currentSlidePosition,
      inEditorEdit,
      linkLabel,
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
@import '{.border-top} from ~bootstrap/scss/utilities/_borders.scss';

.arrow {
  width: 1.25rem;
  height: 1.25rem;
  border: solid $black;
  border-width: 0 4px 4px 0;
  cursor: pointer;
  padding: 4px;
  top: 50%;
  margin: -1rem 1rem 1rem 1rem;

  &-l {
    left: 0;
    transform: rotate(135deg);

    @include media-breakpoint-up(lg) {
      left: -.5rem;
    }
  }

  &-r {
    right: 0;
    transform: rotate(-45deg);

    @include media-breakpoint-up(lg) {
      right: -.5rem;
    }
  }

  &-l:before,
  &-r:before {
    display: block;
    position: relative;
    content: '';
    width: 3rem;
    height: 3rem;
    transform: rotate(45deg);
    top: calc(2px - 1rem);
    left: calc(2px - 1rem);
  }

  &:hover:enabled {
    border-color: $primary;
  }
}

.dots {
  bottom: -1px;

  button {
    margin: 0.5rem;
  }

  button:first-child {
    margin-left: auto;
  }

  button:last-child {
    margin-right: auto;
  }

  &[stuck] {
    @extend .border-top;
    background-color: rgba(255, 255, 255, 0.9);
    z-index: 1;
  }
}
</style>
