<template>
  <article :class="selectedColorVariant.background">
    <section data-test-id="evolution-content-carousel" class="py-4">
      <div class="container">
        <div class="row">
          <div class="col-12">
            <div class="w-100">
              <div v-if="evolutionTitle" data-test-id="evolution-carousel-title" class="evolution-title mb-3 mt-3 w-100 no-margins" :class="selectedColorVariant.headline" v-html="evolutionTitle" />
            </div>
          </div>
        </div>
        <div v-if="linkField"
             class="d-flex align-items-baseline justify-content-between mx-3 mb-5 mx-lg-0"
        >
          <a v-if="linkField && linkField.label"
             data-test-id="evolution-content-carousel-desktop-link"
             :href="linkField.url"
             :target="linkField.target === '_blank_follow' ? '_blank' : linkField.target"
             class="h5 mb-0 d-none d-md-inline-block"
             :class="selectedColorVariant.headline"
             @click="ga4click"
          >{{ linkField.label }}
            <b-icon icon="chevron-right" />
          </a>
        </div>
        <b-overlay :show="$fetchState.pending">
          <div class="position-relative overflow-hidden">
            <div class="d-flex justify-content-center overflow-hidden pb-3">
              <ul ref="slider"
                  data-test-id="evolution-content-carousel-cards-container"
                  class="row flex-nowrap transition-evolution w-100 list-unstyled mb-0"
              >
                <li v-for="(itm, i) in allCards"
                    :key="'card-' + (itm['@id'] || i)"
                    :data-test-id="`evolution-content-carousel-card-${i}`"
                    class="col-12 col-sm-5 col-lg-4 px-sm-1 mx-lg-0 pr-lg-2"
                >
                  <LazyHydrate :on-interaction="['click', 'touchstart']">
                    <div class="row align-items-center justify-content-center h-100">
                      <div class="col-12 h-100">
                        <ProductListingItem v-if="itm"
                                            :item="itm"
                                            :link-label="linkLabel"
                                            :hide-images="showImages === 'false'"
                                            :horizontal="false"
                                            :labels="labels"
                                            :show-indicator="indicators === 'true'"
                                            :show-chips="showChips === 'true'"
                                            :show-publish-date="showPublishDate === 'true'"
                                            :hide-description="hideDescription === 'true'"
                                            :hide-caption="hideCaption === 'true'"
                                            is-carousel-card
                                            show-view-details-link
                                            :tiles-variant="selectedColorVariant.tiles"
                                            :amounts-variant="selectedColorVariant.amounts"
                                            :text-variant="selectedColorVariant.text"
                                            :chips-variant="selectedColorVariant.chips"
                                            :caption-variant="selectedColorVariant.caption"
                        />
                      </div>
                    </div>
                  </LazyHydrate>
                </li>
              </ul>
              <WarningAlert v-if="!(allCards && allCards.length) && inEditorEdit" data-test-id="evolution-content-carousel__error">
                <span v-if="slidesSource.field === 'manual'">No cards slides configured in component.</span>
                <span v-else>No slides to be displayed</span>
              </WarningAlert>
            </div>
          </div>
          <div class="d-flex w-100 mt-2">
            <div class="button-wrapper">
              <button
                v-if="allCards?.length > visibleSlideAmount"
                type="button"
                class="col-1 bg-transparent arrow arrow-l"
                :class="!prevSlideActive ? 'border-color-gray-light' : ''"
                :style="!prevSlideActive ? 'cursor: initial;' : null"
                data-test-id="evolution-content-carousel-arrow-left"
                title="Previous"
                :disabled="!prevSlideActive"
                @click.prevent="(e) => {ga4click(e); goToSlide(currentSlidePosition - 1)}"
              />
            </div>

            <div v-if="allCards?.length > visibleSlideAmount"
                 ref="dots"
                 class="flex-grow-1 d-flex justify-content-center align-items-center mb-1 position-sticky dots px-3"
                 :class="{'overflow-auto' : paginationSlidesCount > 9}"
            >
              <a v-for="i in paginationSlidesCount"
                 :key="`gallery-indicator-${nodeId}-${i}`"
                 href="#"
                 data-test-id="evolution-content-carousel-dot"
                 class="evolution-dot d-block p-2 m-1"
                 :class="[currentSlidePosition === i - 1 ? 'bg-primary' : 'bg-gray-dark-medium']"
                 :title="i"
                 @click.prevent="(e) => {ga4click(e); goToSlide(i - 1)}"
              />
            </div>
            <div class="button-wrapper">
              <button
                v-if="allCards?.length > visibleSlideAmount"
                type="button"
                class="col-1 bg-transparent arrow arrow-r"
                data-test-id="evolution-content-carousel-arrow-right"
                title="Next"
                :class="!nextSlideActive ? 'border-color-gray-light' : ''"
                :style="!nextSlideActive ? 'cursor: initial;' : null"
                :disabled="!nextSlideActive"
                @click.prevent="(e) => {ga4click(e); goToSlide(currentSlidePosition + 1)}"
              />
            </div>
          </div>
        </b-overlay>
        <div v-if="linkField && linkField.label" class="d-md-none mt-2 px-4">
          <a data-test-id="evolution-content-carousel-mobile-link"
             :href="linkField.url"
             :target="linkField.target === '_blank_follow' ? '_blank' : linkField.target"
             :rel="linkField.target === '_blank' ? 'noopener nofollow' : null"
             class="h5 mb-0"
             :class="selectedColorVariant.headline"
             @click="ga4click"
          >{{ linkField.label }}
            <b-icon icon="chevron-right" />
          </a>
        </div>
      </div>
    </section>
  </article>
</template>

<script>
import {
  computed,
  inject,
  nextTick,
  onBeforeUnmount,
  onMounted,
  onServerPrefetch,
  ref,
  ssrRef,
  useContext,
  useFetch,
  watch
} from '@nuxtjs/composition-api'
import debounce from 'lodash/debounce'
import {inEditorEdit} from '~/helpers/vue-editor'
import {MODEL_TYPE} from '~/constants/products'
import usePageLoader from '~/helpers/composables/usePageLoader'
import useMounted from '~/helpers/composables/useMounted'
import {DARK_VARIANT, WHITE_VARIANT} from '~/constants/variants/carouselVariants'
import useVariants from '~/helpers/composables/useVariants'
import {resolveLegacyListSource} from '~/helpers/i18nCompat'

export default {
  props: {
    nodeId: {
      type: String
    },
    showChips: {
      type: String,
      default: 'false'
    },
    evolutionTitle: {
      type: String
    },
    showImages: {
      type: String,
      default: 'true'
    },
    showPublishDate: {
      type: String,
      default: 'false'
    },
    hideDescription: {
      type: String,
      default: 'false'
    },
    hideCaption: {
      type: String,
      default: 'true'
    },
    indicators: {
      type: String,
      default: 'false'
    },
    title: String,
    link: Object,
    htag: {
      type: String,
      default: 'h2'
    },
    cards: {
      type: Object,
      default: null
    },
    variant: {
      type: Object,
      default: () => {
        return {
          field: WHITE_VARIANT,
          tiles: WHITE_VARIANT
        }
      }
    },
    slidesSource: {
      type: Object,
      default: () => {
        return {
          field: 'manual'
        }
      }
    }
  },
  setup(props) {
    const {$utils, $regions, $services, $breakpoints, route, redirect, $ga4, $config} = useContext()
    const prevSlideActive = ref(false)
    const nextSlideActive = ref(false)
    const linkLabel = ref(null)
    const slider = ref(null)
    const dots = ref(null)
    const labels = ref({})
    const currentTranslate = ref(0)
    const currentSlidePosition = ref(0)
    const touchstartX = ref(0)
    const touchstartY = ref(0)
    const touchendX = ref(0)
    const touchendY = ref(0)
    const hideSelectedVariants = ref(props?.slidesSource?.contentType?.hideSelectedVariants === 'true')
    const displayAtGroupLevel = ref(props?.slidesSource?.contentType?.displayAtGroupLevel === 'true')
    const visibleSlideAmount = ref(3)
    const model = inject('model')
    const {getDxpFacets} = usePageLoader({$utils, $services, $regions, route, redirect, $config})
    const {getColorVariantStylesForListing} = useVariants()
    useMounted(props.nodeId)

    const selectedColorVariant = ref(getColorVariantStylesForListing(props.variant))

    const setSlideAmount = () => {
      if ($breakpoints?.desktop || $utils.isDesktopLG() || $utils.isDesktopXL()) {
        visibleSlideAmount.value = 3
      } else if ($breakpoints?.tablet || ($utils.isTablet() && !$utils.isMobile())) {
        visibleSlideAmount.value = 2
      } else {
        visibleSlideAmount.value = 1
      }
    }

    const getInitCards = () => {
      if (props.slidesSource.field === 'manual') {
        const cardsNew = props.cards && !props.slidesSource.cards ? props.cards : props.slidesSource.cards
        const cards = resolveLegacyListSource(
          cardsNew, props.slidesSource, 'cards', $regions.current.language.name, $config.USE_LEGACY_I18N_STRUCTURE
        )

        return cards
          ? cards?.['@nodes']?.map((x) => {
            return cards[x]
          })
          : []
      } else if (props.slidesSource.field === 'mgnlPages') {
        const cards = props.cards && !props.slidesSource.cards ? props.cards : props.slidesSource.cards
        linkLabel.value = props?.slidesSource?.linkLabel
        const maxItems = parseInt(props.slidesSource?.maxItems)

        return cards
          ? cards?.['@nodes']?.map((x) => {
            return ({
              description: cards[x]?.mgnlPage?.description,
              title: cards[x]?.mgnlPage?.title,
              image: cards[x]?.mgnlPage?.primaryPageImage,
              ...$utils.getLinkField(cards[x]?.mgnlPage?.pageLink, $regions.current.language.name)
            })
          }).slice(0, maxItems)
          : []
      }
    }

    const allCards = ssrRef(getInitCards(), props.slidesSource?.['@id'] || Math.random().toString(16).slice(2, 8))

    const paginationSlidesCount = computed(() => {
      const cardsCount = allCards.value?.length || 0
      const isDesktop = $breakpoints?.desktop || $utils.isDesktopLG() || $utils.isDesktopXL()
      if (cardsCount <= visibleSlideAmount.value) {
        return 0
      }
      return isDesktop ? cardsCount - visibleSlideAmount.value + 1 : cardsCount
    })

    const processApiProps = async (config) => {
      const maxItems = parseInt(config.maxItems)
      linkLabel.value = config?.linkLabel
      const facetProcessed = config.facet['@path']?.split('/') || []
      if (facetProcessed.length) {
        facetProcessed.splice(-1)
      }
      const sortingKey = config.facet.id
      const sortDir = config.sortingDirection.field || 'asc'
      const sort = `${sortingKey}:${sortDir}`
      const facet = facetProcessed.join('/')
      const contentType = config.contentType.field
      const pageType = contentType === 'page' ? (config.contentType.pageType?.['@nodes']?.map(x => config.contentType.pageType[x].field).join(',') || config.contentType.pageType.field) : null

      let productCategory, productSubcategory
      if (contentType === MODEL_TYPE.PRODUCT) {
        const productCategoryConfig = (await $services.bobcatapi.getCategories([config.contentType.productCategory]))?.[0]
        const categoryPath = productCategoryConfig?.['@path']?.split('/') || []
        const isSubcategory = productCategoryConfig?.['@nodeType'] === 'dxp:subcategory'
        productCategory = categoryPath[categoryPath.length - (isSubcategory ? 2 : 1)] || ''
        productSubcategory = isSubcategory && productCategoryConfig.title
      }

      const tags = (await getDxpFacets(config.dxpTags?.['@nodes']?.map(x => config.dxpTags[x].dxpTag)))?.map(tag => tag['@path'])

      const useOnlyShortDescription = [MODEL_TYPE.PRODUCT, MODEL_TYPE.ATTACHMENT].includes(contentType)

      let modelParam = null
      if (contentType === MODEL_TYPE.ATTACHMENT) {
        switch (model?.['@nodeType']) {
        case `dxp:${MODEL_TYPE.PRODUCT}`:
          modelParam = model.id
          break
        case `dxp:${MODEL_TYPE.PRODUCT_GROUP}`:
          modelParam = model['@name']
          break
        default:
          break
        }
      }

      return {
        maxItems,
        sort,
        facet,
        contentType,
        productCategory,
        productSubcategory,
        pageType,
        tags,
        useOnlyShortDescription,
        modelParam
      }
    }

    const {fetch} = useFetch(async () => {
      labels.value = await $services.bobcatapi.getConcepts(['/common/labels/specifications'])

      if (props.slidesSource.field !== 'api') {
        return
      }
      try {
        const {
          maxItems,
          sort,
          facet,
          contentType,
          pageType,
          productCategory,
          productSubcategory,
          tags,
          useOnlyShortDescription,
          modelParam
        } = await processApiProps(props.slidesSource)

        const searchConfig = {
          contentType,
          productCategory,
          productSubcategory,
          facet,
          sort,
          page: 0,
          maxItems,
          pageType,
          displayOnGroupLevel: displayAtGroupLevel.value,
          hideSelectedVariants: hideSelectedVariants.value,
          tags,
          model: modelParam
        }

        const r = await $services.content.getSearchResultsByType(searchConfig)

        allCards.value = r.data?.results?.map((x) => {
          return {
            ...x,
            title: x.title || x.name,
            description: useOnlyShortDescription ? x.shortDescription : x.shortDescription || x.description,
            ...(x.pageType === 'video' && {
              videoIndicator: 'true'
            }),
            ...(x.keyProperties && {
              properties: x.keyProperties?.map((p) => {
                return {
                  ...p,
                  ...(p?.value && {value: p?.value + '&nbsp;' + p?.unit})
                }
              }).filter(x => x)
            })
          }
        }).filter(y => y)

        if (inEditorEdit) {
          resetCarousel()
        }
      } catch (e) {
        // eslint-disable-next-line no-console
        console.error('[Content API] Problem while fetching results by type: ', e)
      }
    })

    const linkField = ref($utils.getLinkField(props.link, $regions.current.language.name))

    const initialize = () => {
      setSlideAmount()
      if (allCards.value?.length > visibleSlideAmount.value) {
        nextSlideActive.value = true
        prevSlideActive.value = true
        currentSlidePosition.value = 0
      }
    }

    const goToSlide = (num = currentSlidePosition.value + 1) => {
      const isDesktop = $breakpoints?.desktop || $utils.isDesktopLG() || $utils.isDesktopXL()

      const maxIndex = isDesktop
        ? allCards.value?.length - visibleSlideAmount.value
        : allCards.value?.length - 1

      if (isDesktop) {
        if (num < 0) {
          currentSlidePosition.value = maxIndex
        } else if (num > maxIndex) {
          currentSlidePosition.value = 0
        } else {
          currentSlidePosition.value = num
        }
      } else {
        currentSlidePosition.value = num < 0 ? allCards.value?.length - 1 : num % allCards.value?.length
      }

      let cardWidth = (slider.value?.firstChild?.clientWidth || 0) * currentSlidePosition.value

      // correction for tablets where we have half card visible
      if (window.innerWidth < 992 && window.innerWidth > 576 && currentSlidePosition.value === allCards.value?.length - visibleSlideAmount.value) {
        cardWidth = slider.value?.firstChild?.clientWidth * (currentSlidePosition.value - 0.5) + 32
      }

      currentTranslate.value = cardWidth

      slider.value.style.transform = `translateX(-${cardWidth}px)`
    }

    const resetCarousel = () => {
      setSlideAmount()
      prevSlideActive.value = allCards.value?.length > visibleSlideAmount.value
      nextSlideActive.value = allCards.value?.length > visibleSlideAmount.value
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

        const handleResize = debounce(() => {
          setSlideAmount() // check desktop/tablet/mobile
          resetCarousel() // apply new slide amount + reset
        }, 200)

        nextTick(() => {
          window.addEventListener('resize', handleResize, {passive: true})
          window.addEventListener('orientationchange', handleResize, {passive: true})
          initiateSlider()
        })

        onBeforeUnmount(() => {
          window.removeEventListener('resize', handleResize)
          window.removeEventListener('orientationchange', handleResize)
        })
      })

      watch(() => allCards.value, (cards) => {
        if (cards?.length) {
          resetCarousel()
        }
      })
    }

    onServerPrefetch(async () => {
      await fetch()
    })

    const ga4click = (event) => {
      $ga4.events.carousel_click(event, model)
    }

    return {
      allCards,
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
      ga4click,
      selectedColorVariant,
      DARK_VARIANT,
      labels,
      paginationSlidesCount
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

section[data-test-id="evolution-content-carousel"] {
  ::v-deep iframe {
    width: 100%;
  }
}

::v-deep {
  .evolution-title {
    h1, h2, h3, h4, h5, h6, p {
      margin-bottom: 0;
      line-height: 1.1;
    }
  }

  .card-indicator {
    top: 11rem;

    + .card-indicator {
      top: 13rem;
    }
  }
}

.button-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 3rem;
}

.arrow {
  width: 0.75rem;
  height: 0.75rem;
  border: solid $black;
  border-width: 0 3px 3px 0;
  border-color: $primary;
  transition: $transition-base;
  cursor: pointer;
  padding: 6px;
  top: 50%;
  margin: -1rem 1rem 1rem 1rem;

  &-l {
    left: 1rem;
    transform: rotate(135deg);
  }

  &-r {
    right: 1rem;
    transform: rotate(-45deg);
  }

  &-l:before,
  &-r:before {
    display: block;
    position: relative;
    content: '';
    width: 1.5rem;
    height: 1.5rem;
    transform: rotate(45deg);
    top: calc(2px - 1rem);
    left: calc(2px - 1rem);
  }

  &:hover:enabled {
    border-color: $light;
  }
}

.dots {
  bottom: -1px;

  .evolution-dot {
    width: 1rem;
    height: 1rem;
    transition: $transition-base;
    clip-path: polygon(
        0 0,
        calc(100% - 4px) 0,
        100% 4px,
        100% 100%,
        4px 100%,
        0 calc(100% - 4px)
    );

    &:hover {
      opacity: 0.7;
    }
  }

  &[stuck] {
    @extend .border-top;
    background-color: rgba(255, 255, 255, 0.9);
    z-index: 1;
  }
}
</style>
