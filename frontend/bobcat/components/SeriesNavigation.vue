<template>
  <section data-test-id="series-navigation" class="py-4">
    <LazyHydrate never>
      <div v-if="gridTitle" class="container">
        <div class="row">
          <div class="col-12">
            <div class="border-bottom mb-4">
              <component :is="htag" :class="htag" data-test-id="series-navigation-title">{{ gridTitle }}</component>
            </div>
          </div>
        </div>
      </div>
    </LazyHydrate>

    <div class="container">
      <div class="row">
        <div class="col-12">
          <div v-if="allCards && allCards.length" data-test-id="series-navigation-cards-wrapper" class="cards d-flex d-sm-block justify-content-center">
            <div ref="slider" data-test-id="series-navigation-cards-container" class="row slider flex-nowrap flex-sm-wrap transition-base py-3 py-sm-0">
              <div v-for="(card) in allCards"
                   :key="card['@id']"
                   :class="variant"
                   class="col-12"
              >
                <div class="row align-items-center justify-content-center h-100">
                  <div class="col-10 py-sm-3 col-sm-12 h-100"
                       data-test-id="series-navigation-card"
                  >
                    <div class="d-flex flex-column flex-grow-1 shadow rounded h-100">
                      <div class="position-relative">
                        <ImageSet
                          v-if="card.asset && card.asset.src"
                          class="w-100 h-100"
                          data-test-id="series-navigation-card-img"
                          :src="card.asset.src"
                          :title="card.asset.title"
                          :alt="card.asset.alt"
                          :img-width="card.asset.imageWidth"
                          :img-height="card.asset.imageHeight"
                        />
                        <div v-if="card.newIndicator === 'true'"
                             data-test-id="series-navigation-card-indicator"
                             class="card-indicator text-white px-2 position-absolute bg-primary font-weight-bold text-uppercase"
                        >
                          {{ labels.new || 'New' }}
                        </div>
                      </div>
                      <div v-if="card.title" class="bg-dark w-100 px-3 py-2 text-white">
                        <component :is="card.htag" data-test-id="series-navigation-card-title" class="m-0 text-break">{{ card.title }}</component>
                      </div>
                      <div class="d-flex flex-column p-3 h-100">
                        <component :is="card.htagSubtitle" v-if="card.subtitle" data-test-id="series-navigation-card-subtitle">{{ card.subtitle }}</component>
                        <div class="d-flex flex-grow-1">
                          <ul v-if="card.points && card.points.length" data-test-id="series-navigation-card-points" class="pl-3">
                            <li v-for="(point) in card.points" :key="point['@id']">
                              <p class="m-0">{{ point.text }}</p>
                            </li>
                          </ul>
                        </div>
                        <div v-if="card.links && card.links.length" class="d-flex flex-wrap align-items-center">
                          <p v-if="card.preLinksLabel" class="mb-0 mr-1 font-weight-bold">{{ card.preLinksLabel }}</p>
                          <div v-for="(link, index) in card.links" :key="link['@id']" data-test-id="series-navigation-card-links" class="d-flex flex-wrap align-items-center">
                            <a
                              class="font-weight-bold"
                              :rel="link.target === '_blank' ? 'noopener nofollow' : null"
                              :href="link.url"
                              :target="link.target === '_blank_follow' ? '_blank' : link.target"
                              @click="handleLinkClick($event, link)"
                            >{{ link.label }}</a><span
                              v-if="index !== card.links.length - 1"
                            >,&nbsp;</span>
                          </div>
                        </div>
                      </div>
                      <b-button
                        v-if="card.cta && card.cta.url"
                        :href="card.cta.url"
                        :target="card.cta.target === '_blank_follow' ? '_blank' : card.cta.target"
                        block
                        variant="primary"
                        data-test-id="cta-button"
                        class="series-navigation__cta-button text-nowrap text-large py-2 mt-0 rounded-0 d-flex justify-content-center align-items-center"
                        @click="handleLinkClick($event, card.cta)"
                      >
                        {{ card.cta.label || 'View all' }}
                      </b-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <WarningAlert v-else-if="inEditorEdit" data-test-id="series-navigation-alert">No cards slides configured in component.</WarningAlert>
        </div>
        <button
          v-if="allCards.length > 1"
          type="button"
          class="d-flex d-sm-none col-1 bg-transparent position-absolute arrow arrow-l"
          :class="{'border-color-gray-light' : !prevSlideActive}"
          :style="!prevSlideActive ? 'cursor: initial;' : null"
          data-test-id="content-carousel-arrow-left"
          title="Previous"
          :disabled="!prevSlideActive"
          @click.prevent="goToSlide(currentSlide - 1)"
        />

        <button
          v-if="allCards.length > 1"
          type="button"
          class="d-flex d-sm-none col-1 bg-transparent position-absolute arrow arrow-r"
          data-test-id="content-carousel-arrow-right"
          title="Next"
          :class="{'border-color-gray-light' : !nextSlideActive}"
          :style="!nextSlideActive ? 'cursor: initial;' : null"
          :disabled="!nextSlideActive"
          @click.prevent="goToSlide(currentSlide + 1)"
        />
      </div>
      <div ref="dots"
           class="d-flex d-sm-none align-items-center mb-2 position-sticky dots px-3 w-100"
           :class="[allCards.length > 9 ? 'overflow-auto' : 'justify-content-center']"
      >
        <a v-for="i in allCards.length"
           :key="`gallery-indicator-${nodeId}-${i}`"
           href="#"
           class="border border-primary rounded-circle d-flex justify-content-center align-items-center p-2 m-2"
           :class="{'bg-primary text-white' : currentSlide === i - 1}"
           :title="i"
           @click.prevent="goToSlide(i - 1)"
        />
      </div>
    </div>
    </div>
  </section>
</template>

<script>
import {nextTick, onBeforeUnmount, onMounted, ref, useContext, useRouter} from '@nuxtjs/composition-api'
import {inEditorEdit} from '~/helpers/vue-editor'
import useMounted from '~/helpers/composables/useMounted'

export default {
  props: {
    nodeId: {
      type: String
    },
    gridTitle: {
      type: String
    },
    htag: {
      type: String,
      default: 'h2'
    },
    cards: {
      type: Object,
      required: true
    }
  },
  setup(props) {
    const {$utils, $regions, $breakpoints, $services} = useContext()
    const router = useRouter()
    const prevSlideActive = ref(false)
    const nextSlideActive = ref(false)
    const slider = ref(null)
    const dots = ref(null)
    const currentTranslate = ref(0)
    const currentSlide = ref(0)
    const touchstartX = ref(0)
    const touchstartY = ref(0)
    const touchendX = ref(0)
    const touchendY = ref(0)
    const allCards = ref([])
    const variant = ref('col-sm-4 col-md-3')
    const labels = ref({})
    useMounted(props.nodeId)

    allCards.value = props.cards
      ? props.cards['@nodes']?.map(x => ({
        ...props.cards[x],
        asset: {
          alt: props.cards[x]?.asset?.imageAlt,
          title: props.cards[x]?.asset?.imageTitle,
          imageWidth: props.cards[x]?.asset?.imageWidth || null,
          imageHeight: props.cards[x]?.asset?.imageHeight || null,
          src: props.cards[x]?.asset?.src
        },
        links: props.cards[x]?.links
          ? props.cards[x]?.links['@nodes']?.map(link => $utils.getLinkField(props.cards[x]?.links[link].link, $regions.current.language.name)).filter(x => x)
          : [],
        points: props.cards[x]?.points
          ? props.cards[x]?.points['@nodes']?.map(point => props.cards[x]?.points[point]).filter(point => point.text)
          : [],
        cta: $utils.getLinkField(props.cards[x]?.cta, $regions.current.language.name)
      }))
      : []

    const cardsLength = allCards.value.length

    if (cardsLength === 3) {
      variant.value = 'col-sm-4'
    } else if (cardsLength === 2) {
      variant.value = 'col-sm-6'
    } else if (cardsLength === 1) {
      variant.value = 'col-12'
    }

    // Same-page parameter-based anchors (e.g. ?facets=...#models) must go through Vue Router
    // so listing components can apply filters; plain href clicks are intercepted and only scroll.
    const handleLinkClick = (event, linkItem) => {
      if (!linkItem?.url) {
        return
      }
      const target = linkItem.target === '_blank_follow' ? '_blank' : linkItem.target || '_self'
      if (target !== '_self') {
        return
      }
      event.preventDefault()
      try {
        const url = new URL(linkItem.url, window.location.href)
        if (url.origin === window.location.origin) {
          router.push({
            path: url.pathname,
            query: Object.fromEntries(url.searchParams),
            hash: url.hash
          }).catch(() => {})
        } else {
          window.location.href = linkItem.url
        }
      } catch (e) {
        window.location.href = linkItem.url
      }
    }

    const initialize = () => {
      nextSlideActive.value = true
      prevSlideActive.value = false
      currentSlide.value = 0
    }

    const resetCarousel = () => {
      prevSlideActive.value = true
      nextSlideActive.value = false
      currentSlide.value = 0
      currentTranslate.value = 0
      if (slider.value) {
        slider.value.style.transform = `translateX(${currentTranslate.value}px)`
      }
    }

    const goToSlide = (num = currentSlide.value + 1) => {
      currentSlide.value = num < 0 ? allCards.value?.length - 1 : num % (allCards.value?.length)
      prevSlideActive.value = currentSlide.value > 0
      nextSlideActive.value = currentSlide.value < allCards.value?.length - 1

      let cardWidth = (slider.value?.firstChild?.clientWidth || 0) * currentSlide.value

      // correction for tablets where we have half card visible
      if (window.innerWidth < 992 && window.innerWidth > 576 && currentSlide.value === allCards.value?.length - 1) {
        cardWidth = slider.value?.firstChild?.clientWidth * (currentSlide.value - 0.5) + 32
      }

      currentTranslate.value = cardWidth

      slider.value.style.transform = `translateX(-${cardWidth}px)`
    }

    const handleGesure = () => {
      const isClicked = touchendX.value - touchstartX.value === 0
      if (!isClicked && Math.abs(touchstartY.value - touchendY.value) <= 50) {
        if (touchendX.value <= touchstartX.value) {
          goToSlide(currentSlide.value + 1)
        } else if (touchendX.value >= touchstartX.value) {
          goToSlide(currentSlide.value - 1)
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
      onMounted(async () => {
        labels.value = await $services.bobcatapi.getConcepts(['/common/labels/specifications'])
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
    }

    return {
      allCards,
      prevSlideActive,
      nextSlideActive,
      goToSlide,
      slider,
      dots,
      currentSlide,
      variant,
      inEditorEdit,
      labels,
      handleLinkClick
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

.card {
  &-indicator {
    left: 0;
    top: 1rem;
    font-size: .875rem;
    line-height: 1.75;
  }
}

.cards {
  @include media-breakpoint-down(sm) {
    overflow: hidden;

    .slider {
      width: 100%;
    }
  }
}

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

  &[stuck] {
    @extend .border-top;
    background-color: rgba(255, 255, 255, 0.9);
    z-index: 1;
  }
}

</style>
