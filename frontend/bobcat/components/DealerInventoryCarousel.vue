<template>
  <article data-test-id="dl-inv-carousel">
    <section data-test-id="dl-inv-search" :class="[selectedColorVariant.background, 'py-4']">
      <div class="container">
        <div class="row">
          <div class="col-12 d-flex justify-content-between align-items-md-end flex-column flex-md-row border-bottom">
            <LazyHydrate never>
              <component :is="htag" v-if="title" :class="htag" class="mb-0" data-test-id="product-carousel-title">
                {{ title }}
              </component>
            </LazyHydrate>
            <div class="d-flex flex-column flex-md-row">
              <div v-if="zipCode || address" class="product-carousel-address mb-2 text-nowrap">
                <b-icon icon="pin" class="mr-1" />
                <span class="font-weight-bold">{{ zipCode || address }}</span> {{ labels.in || 'in' }} {{ locationRange }} {{ metric }}
              </div>
              <a v-if="!showForm"
                 class="mb-0 ml-2 font-weight-bold cursor-pointer"
                 @click="resetAll"
              >
                <span class="d-flex align-items-center justify-content-between text-nowrap">
                  {{ changeLocationLabel }}
                  <b-icon icon="sort-down" class="dropdown ml-2" />
                </span>
              </a>
            </div>
          </div>
        </div>
      </div>
    </section>
    <b-collapse v-model="showForm" :class="selectedColorVariant.background">
      <section class="container carousel-form px-3">
        <div class="row">
          <LazyHydrate when-visible>
            <div class="col-12 col-md-6 col-lg-5 offset-lg-1 d-flex justify-content-center flex-column">
              <div v-if="results && results.length === 0 && noResultsDescription && onceSubmitted && !isFetching" v-html="addYoutubeNoCookie(noResultsDescription)" />
              <div v-else-if="description" v-html="addYoutubeNoCookie(description)" />
            </div>
          </LazyHydrate>
          <div v-if="category && category.irwName" class="col-12 col-md-6 col-lg-5">
            <form data-test-id="dl-inv-carousel-form" class="ngb-form d-flex p-4 shadow align-items-start flex-column" :class="selectedColorVariant.formBackground">
              <div class="d-flex flex-column w-100 mb-3 mb-sm-0">
                <div class="mr-0 mr-sm-2 dinv-location">
                  <AddressAutocompleteSimple
                    id="dealer-inventory-carousel-address"
                    ref="addressAutocompleteElement"
                    :key="address"
                    :api-key="apiKey"
                    icon="pin"
                    :placeholder="locationPlaceholder"
                    :label="locationLabel"
                    :autodetect-label="autoDetect"
                    :validation-message="validationMessage"
                    :init-value="address"
                    :disabled="isFetching"
                    @place-changed="handleAddressSelected"
                    @text-changed="handleAddressTextChanged"
                  />
                </div>
              </div>
              <div class="d-flex flex-column w-100 mb-3 mb-sm-0">
                <LazyHydrate never><label>{{ distanceLabel }}</label></LazyHydrate>
                <client-only>
                  <VueSlider
                    v-if="renderSlider"
                    v-model="locationRange"
                    class="px-2"
                    :disabled="isFetching || !address"
                    :min="minimum"
                    :max="maximum"
                    :interval="skipInterval"
                    drag-on-click
                    @drag-start="handleDragStart"
                    @drag-end="handleDragEnd"
                  />
                </client-only>
                <div v-if="steps && steps.length" class="d-flex justify-content-between">
                  <span v-for="(step, i) in steps" :key="`${step}_${i}`" :class="{'mr-2': i === steps.length}">{{
                    step
                  }} {{ metric }}</span>
                </div>
              </div>
              <div class="d-flex align-items-center mt-0 mt-sm-5 ml-0 w-100">
                <b-button
                  variant="primary"
                  type="button"
                  :disabled="isFetching || !(address && category && category.irwName && locationRange)"
                  class="btn dinv-btn-carousel w-100 px-3 mt-3 mt-sm-0"
                  @click.prevent="() => {if (!isFetching) debounce(search)}"
                >
                  <span class="d-flex align-items-center justify-content-center">
                    <b-spinner v-if="isFetching" class="mr-2" small />
                    <LazyHydrate never><span>{{ searchLabel }}</span></LazyHydrate>
                  </span>
                </b-button>
              </div>
            </form>
          </div>
          <div v-else class="col-12 col-md-6 col-lg-4">
            <WarningAlert
              class="d-flex justify-content-center"
              :title="generateConceptHint('dealerinventory/search-info')"
              data-test-id="dinv-missing-category-alert"
            >
              <div v-html="(labels.missingCategory || 'Missing category or something went wrong')" />
            </WarningAlert>
          </div>
        </div>
      </section>
    </b-collapse>
    <b-collapse v-model="showResults" :class="selectedColorVariant.background">
      <div class="py-3">
        <ProductCarousel v-if="results.length"
                         :labels="labels"
                         :results="results"
                         :is-fetching="isFetching"
                         :category="category"
                         :location="address"
                         :distance="locationRange"
                         :cta-settings="{ctaLabel, showCTA}"
        />
        <WarningAlert
          v-else
          class="d-flex justify-content-center"
          :title="generateConceptHint('locationSearchError')"
        >
          {{ labels.locationSearchError || 'Something went wrong, please pick different location and try again.' }}
        </WarningAlert>
      </div>
    </b-collapse>
    <section class="container py-3">
      <div class="row justify-content-center justify-content-md-end px-3">
        <LazyHydrate never>
          <a v-if="pageLink && exploreInventoryLabel"
             :href="pageLink.pageUrl || null"
             :target="pageLink.target || '_self'"
             class="h5 mb-0"
             :class="selectedColorVariant.headline"
          >
            {{ exploreInventoryLabel }} <b-icon icon="chevron-right" />
          </a>
        </lazyhydrate>
      </div>
    </section>
  </article>
</template>

<script>
import {
  inject,
  nextTick,
  onMounted,
  ref,
  useContext,
  useFetch,
  useRouter,
  watch
} from '@nuxtjs/composition-api'
import {VARIANTS_CLASSES} from '../constants/variants/dinvCarouselVariants'
import {COUNTRY_CODES} from '../constants/countries'
import {DEALER_INVENTORY_ROUTE} from '../constants/staticRoutes'
import ProductCarousel from './elements/Dealer/ProductCarousel.vue'
import {LOCATION_KEYS} from '~/constants'
import useLocation from '~/helpers/composables/useLocation'
import useInventorySorting from '~/helpers/composables/useInventorySorting'
import {addYoutubeNoCookie} from '~/helpers/functional/AppHelpers'
import config from '~/constants/props/form.config'
import useConcepts from '~/helpers/composables/useConcepts'
import DISTANCE_METRICS from '~/constants/distance'
import useQueryParams from '@/helpers/composables/useQueryParams'
import useMounted from '~/helpers/composables/useMounted'

const {
  htag,
  title,
  description,
  noResultsDescription,
  productCategory,
  model,
  distanceLabel,
  searchLabel,
  autoDetect,
  min, max, interval, apiKey,
  validationMessage,
  changeLocationLabel,
  locationPlaceholder,
  locationLabel,
  limit,
  nodeId,
  variant,
  exploreInventoryLabel,
  ctaLabel,
  showCTA
} = config

export default {
  components: {
    VueSlider: () => import('vue-slider-component'),
    ProductCarousel
  },
  props: {
    htag,
    title,
    description,
    noResultsDescription,
    productCategory,
    model,
    distanceLabel,
    searchLabel,
    autoDetect,
    min,
    max,
    interval,
    apiKey,
    validationMessage,
    changeLocationLabel,
    locationPlaceholder,
    locationLabel,
    limit,
    nodeId,
    variant,
    exploreInventoryLabel,
    ctaLabel,
    showCTA
  },
  setup(props) {
    const {$services, $regions, $utils, $ga4} = useContext()
    const router = useRouter()
    const {sortProducts} = useInventorySorting()
    const {min, max, interval, limit, variant} = props
    const {saveLocation, address, countryCode, range, locationChanged, zipCode} = useLocation()
    const category = ref(null)
    const pageLink = ref(null)
    const reset = ref(false)
    const metric = ref(DISTANCE_METRICS.MI)
    const labels = ref({})
    const minimum = ref(Number(min))
    const maximum = ref(Number(max))
    const skipInterval = ref(Number(interval))
    const steps = ref([])
    const showResults = ref(false)
    const showForm = ref(true)
    const isFetching = ref(false)
    const locationRange = ref(minimum.value)
    const results = ref([])
    const facetsResults = ref([])
    const onceSubmitted = ref(false)
    const isDragging = ref(false)
    const multiSelectRef = ref(null)
    const {generateConceptHint} = useConcepts()
    let searchTimer = null
    const currentPage = ref(null)
    const currentSort = ref(null)
    const currentPerPage = ref(null)
    const addressAutocompleteElement = ref(null)
    const renderSlider = ref(false)
    const model = inject('model')

    watch(range, (val) => {
      if (val) { locationRange.value = range.value }
    })

    watch(locationChanged, () => {
      search()
    })

    useMounted(props.nodeId)
    const selectedColorVariant = ref(VARIANTS_CLASSES[variant])

    if (!Number.isInteger((maximum.value - minimum.value) / skipInterval.value)) {
      skipInterval.value = 1
    }

    const debounce = (callback, timeout = 400) => {
      clearTimeout(searchTimer)
      searchTimer = setTimeout(callback, timeout)
    }

    const {
      queryParams
    } = useQueryParams()

    const resetAll = () => {
      reset.value = true
      showForm.value = true
      showResults.value = false
      results.value = []
      locationRange.value = minimum.value
      address.value = ''
      countryCode.value = ''
      nextTick(() => {
        onceSubmitted.value = false
      })
      clearTimeout(searchTimer)
      setTimeout(() => {
        reset.value = false
      }, 50)
    }

    const search = async () => {
      if (!category?.value?.irwName || !address.value) {
        return
      }
      if (!countryCode.value) {
        addressAutocompleteElement.value?.parseAddress(address.value)
        debounce(search)
        return
      }
      try {
        isFetching.value = true
        setMetrics(countryCode.value)
        locationRange.value = range.value
        const models = model?.label || props.model.IRWModelName
        const unitOfLength = metric.value === DISTANCE_METRICS.MI ? DISTANCE_METRICS.MI_NAME : DISTANCE_METRICS.KM_NAME
        const {
          results: items
        } = await $services.dealerinventory.searchResultsV2(address.value, countryCode.value, range.value, unitOfLength, category.value, models || [], limit, [])
        $ga4.events.search_inventory(category.value.irwName, models, range.value, address.value)
        isFetching.value = false
        onceSubmitted.value = true

        if (items) {
          results.value = items
          sortProducts(results.value)
          if (items.length > 0) {
            showForm.value = false
            showResults.value = true
          }
        }
      } catch (error) {
        // eslint-disable-next-line no-console
        console.error(error)
        results.value = []
        isFetching.value = false
        showForm.value = false
        showResults.value = true
        $ga4?.events?.error_general({
          error_details: error
        }, 'data error')
      }
    }

    const scrollToResults = (_id = 'searchResults') => {
      setTimeout(() => {
        if (process.client && _id) {
          $utils.scrollToElement(_id)
        }
        isFetching.value = false
      }, 300)
    }

    const skipStep = maximum.value / skipInterval.value <= 4 ? skipInterval.value : Math.round(maximum.value / 4)

    for (let x = minimum.value; x <= maximum.value; x += skipStep) {
      if (x + skipStep > maximum.value) {
        steps.value.push(maximum.value)
      } else {
        steps.value.push(x)
      }
    }

    const setMetrics = (countryCode) => {
      if (countryCode === COUNTRY_CODES.Canada) {
        metric.value = DISTANCE_METRICS.KM
      } else {
        metric.value = DISTANCE_METRICS.MI
      }
    }

    watch(address, (newVal) => {
      if (!newVal) {
        resetAll()
        return
      }
      if (!countryCode.value) {
        addressAutocompleteElement.value?.parseAddress(newVal)
      }
      if (onceSubmitted.value) {
        debounce(search)
      }
    })

    watch(countryCode, setMetrics)

    const handleDragStart = () => {
      isDragging.value = true
      clearTimeout(searchTimer)
    }

    const handleDragEnd = () => {
      setTimeout(() => {
        isDragging.value = false
        if (onceSubmitted.value && !isDragging.value) {
          debounce(search)
        }
      }, 200)
      saveLocation(LOCATION_KEYS.RANGE, locationRange.value)
    }

    useFetch(async () => {
      labels.value = await $services.bobcatapi.getConcepts(['/common/labels/dealerinventory', '/%region%/common/labels/dealerinventory'])
      const productTypes = await $services.dealerinventory.getProductTypes()
      if (queryParams.value?.['product-type']) { // Prioritize product-type is in query params
        const queryCategory = decodeURI(queryParams.value?.['product-type'])?.toLowerCase()
        category.value = productTypes.find(x => (x.label?.toLowerCase() === queryCategory || x.irwName?.toLowerCase() === queryCategory))
        if (router.currentRoute.path.includes(`${DEALER_INVENTORY_ROUTE}`)) {
          const params = router.currentRoute.path.split(DEALER_INVENTORY_ROUTE)[1]
          const segments = params?.split('/')?.filter(segment => segment?.length > 0)
          if (segments.length === 3) {
            const categoryName = segments[0]
            pageLink.value = {pageUrl: `/${$regions.current.name}/${$regions.current.language.name}${DEALER_INVENTORY_ROUTE}/${categoryName}`}
          }
        }
      } else if (props.model) {
        if (['dxp:category', 'dxp:subcategory'].includes(props.model['@nodeType'])) {
          category.value = productTypes.find(productType => productType.irwName === props.model.IRWProductName)
          const magnoliaNode = await $services.bobcatapi.getCategories([props.model['@id']])
          pageLink.value = magnoliaNode[0]?.pageLink?.link
        } else {
          const modelProductType = await $services.dealerinventory.getModelProductType(true, props.model.id)
          category.value = modelProductType
        }
      } else { // for backwards compatibility take productCategory
        const magnoliaNode = await $services.bobcatapi.getCategories([props.productCategory])
        pageLink.value = magnoliaNode[0]?.pageLink?.link
        category.value = productTypes.find(productType => productType.irwName === magnoliaNode[0].IRWProductName)
      }
      debounce(search)
    })

    onMounted(() => {
      if (typeof window !== 'undefined' && window.document) {
        renderSlider.value = true
      }

      const searchCondition = Boolean(address.value && countryCode.value && range.value && category.value)
      if (searchCondition) {
        search()
      }
    })

    const handleAddressSelected = (fullAddress, country, zip) => {
      saveLocation(LOCATION_KEYS.COUNTRY_CODE, country)
      saveLocation(LOCATION_KEYS.ADDRESS, fullAddress)
      saveLocation(LOCATION_KEYS.ZIP_CODE, zip)
      saveLocation(LOCATION_KEYS.RANGE, locationRange.value)
    }

    const handleAddressTextChanged = (fullAddress) => {
      if (!fullAddress) {
        address.value = ''
        countryCode.value = ''
        showResults.value = false
      }
    }

    return {
      addressAutocompleteElement,
      locationRange,
      selectedColorVariant,
      pageLink,
      category,
      reset,
      resetAll,
      metric,
      minimum,
      maximum,
      skipInterval,
      steps,
      showResults,
      showForm,
      search,
      isFetching,
      results,
      DISTANCE_METRICS,
      onceSubmitted,
      handleDragStart,
      handleDragEnd,
      multiSelectRef,
      generateConceptHint,
      labels,
      scrollToResults,
      currentPage,
      currentPerPage,
      currentSort,
      debounce,
      facetsResults,
      addYoutubeNoCookie,
      handleAddressSelected,
      address,
      handleAddressTextChanged,
      renderSlider,
      zipCode
    }
  }
}
</script>

<style lang="scss">
@import '~/assets/sass/bootstrap_forms';
@import '~bootstrap/scss/progress';
@import '~bootstrap/scss/functions';
@import '[media-breakpoint-up] from ~bootstrap/scss/mixins';
@import '~/assets/sass/slider_theme';

.carousel-form {
  padding: 1rem 0;

  @include media-breakpoint-up(md) {
    padding: 5rem 0;
  }

  @include media-breakpoint-up(lg) {
    padding: 6.5rem 0;
  }
}

.product-carousel-address {
  max-width: 16rem;
  text-overflow: ellipsis;
  overflow: hidden;
}

.carousel-form {
  iframe {
    width: 100%;
  }
}

div[data-test-id="dl-inv-carousel"] {
  .dinv-btn {
    min-width: auto;
    max-height: 2.5rem;
  }

  .dinv-location {
    min-width: 16rem;
    @include media-breakpoint-up(lg) {
      min-width: 22rem;
    }
  }
}
</style>
