<template>
  <div data-test-id="dl-inv">
    <section data-test-id="dl-inv-search" class="container py-4">
      <div class="row">
        <LazyHydrate never>
          <div class="col-12 mb-4">
            <component :is="htag" v-if="title" :class="htag">
              {{ title }}
            </component>
            <div v-if="description" v-html="addYoutubeNoCookie(description)" />
          </div>
        </LazyHydrate>

        <div v-if="category && category.irwName" class="col-12">
          <p v-if="formTitle" class="mb-4 h4">{{ formTitle }}</p>
          <form data-test-id="dl-inv-search-form" class="ngb-form d-flex align-items-start flex-sm-row flex-column">
            <div class="d-flex flex-column w-100 w-md-auto mb-3 mb-sm-0">
              <div class="mr-0 mr-sm-2 dinv-location">
                <AddressAutocompleteSimple
                  id="dealer-inventory-autocomplete"
                  :key="address"
                  :api-key="apiKey"
                  icon="pin"
                  :placeholder="locationPlaceholder"
                  :label="locationLabel"
                  :autodetect-label="autoDetectLabel"
                  :validation-message="validationMessage"
                  :init-value="address"
                  :disabled="isFetching"
                  @place-changed="handleAddressSelected"
                  @text-changed="handleAddressTextChanged"
                />
              </div>
            </div>
            <div class="d-flex flex-column w-100 mx-0 mx-sm-4 mb-3 mb-sm-0">
              <LazyHydrate never><label>{{ distanceLabel }}</label></LazyHydrate>
              <client-only>
                <VueSlider
                  v-if="renderSlider"
                  v-model="locationRange"
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
            <div class="d-flex align-items-center mt-0 mt-sm-5 ml-0 ml-md-2 w-100">
              <b-button
                variant="primary"
                type="button"
                :disabled="isFetching || !(address && category && category.irwName && locationRange)"
                class="btn dinv-btn w-100 px-3 mt-3 mt-sm-0"
                @click.prevent="() => { if (!isFetching) debounce(search) }"
              >
                <span class="d-flex align-items-center justify-content-center">
                  <b-spinner v-if="isFetching" class="mr-2" small />
                  <LazyHydrate never><span>{{ searchLabel }}</span></LazyHydrate>
                </span>
              </b-button>
            </div>
          </form>
        </div>
        <div v-else class="col-12">
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
    <div v-if="!showResults" class="bg-gray-lighter dinv-search-info">
      <div class="container">
        <div class="row">
          <div class="col-12">
            <WarningAlert
              type="info"
              class="border-0 d-flex justify-content-center"
              :title="generateConceptHint('dealerinventory/search-info')"
              data-test-id="dinv-search-info"
            >
              <div v-html="(labels.searchInfo || 'Please enter your location')" />
            </WarningAlert>
          </div>
        </div>
      </div>
    </div>
    <b-collapse v-model="showResults">
      <div v-if="totalResults && totalResults > limit" class="container mb-4">
        <LazyHydrate never>
          <div class="row">
            <div class="col-12">
              <WarningAlert
                type="info"
                :title="generateConceptHint('dealerinventory/moreResults')"
                data-test-id="dinv-more-results"
              >
                <div v-html="(labels.moreResults || '').replace('%LIMIT%', limit)" />
              </WarningAlert>
            </div>
          </div>
        </LazyHydrate>
      </div>

      <DealerInventorySearchResults
        v-if="category"
        :distance="locationRange"
        :metric="metricName"
        :no-results-msg="noResultsMsg"
        :address="address"
        :facets="facetsResults"
        :cta-settings="{ctaLabel, showCTA}"
        :filters="filters"
        :results="results"
        :sorting="sortingOptions"
        :is-fetching="isFetching"
        :initial-values="{ perPage: currentPerPage, page: currentPage }"
        :category="category"
        @pageUpdated="(pageNum, perPage) => { currentPage = pageNum; currentPerPage = perPage || currentPerPage; debounce(updateAllQueryParams); scrollToResults() }"
        @sortingUpdated="(sortObj) => { currentSort = sortObj.value; debounce(updateAllQueryParams) }"
        @filterResults="(f) => { filters = f; debounce(updateAllQueryParams); filterOutResults() }"
      />
    </b-collapse>
  </div>
</template>

<script>
import {computed, nextTick, onBeforeMount, onMounted, ref, useContext, useFetch, watch} from '@nuxtjs/composition-api'
import {STOCK_STATUSES} from '../constants/dealerInventory'
import FACET from '../constants/facets'
import {COUNTRY_CODES} from '../constants/countries'
import {getWithExpiry, removeWithExpiry} from '../helpers/functional/AppHelpers'
import {DEALER_INVENTORY_KEYS} from '../constants/localStorageKeys'
import {LOCATION_KEYS} from '~/constants'
import useLocation from '~/helpers/composables/useLocation'
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
  productCategory,
  distanceLabel,
  searchLabel,
  min, max, interval, apiKey,
  validationMessage,
  noResultsMsg,
  locationPlaceholder,
  locationLabel,
  autoDetectLabel,
  modelFacetLabel,
  dealerFacetLabel,
  reservationFacetLabel,
  reservationFacetValueLabel,
  facet,
  limit,
  ctaLabel,
  showCTA,
  nodeId,
  formTitle
} = config

export default {
  components: {
    VueSlider: () => import('vue-slider-component')
  },
  props: {
    htag,
    title,
    description,
    productCategory,
    distanceLabel,
    searchLabel,
    min,
    max,
    interval,
    apiKey,
    validationMessage,
    noResultsMsg,
    locationPlaceholder,
    locationLabel,
    autoDetectLabel,
    modelFacetLabel,
    dealerFacetLabel,
    reservationFacetLabel,
    reservationFacetValueLabel,
    facet,
    limit,
    ctaLabel,
    showCTA,
    nodeId,
    formTitle
  },
  setup(props) {
    const {$services, $utils, $ga4} = useContext()
    const {min, max, interval, facet, limit} = props
    const {saveLocation, address, countryCode, range, locationChanged, notify} = useLocation()
    const category = ref(null)
    const reset = ref(false)
    const metric = ref(DISTANCE_METRICS.MI)
    const labels = ref({})
    const minimum = ref(Number(min))
    const maximum = ref(Number(max))
    const skipInterval = ref(Number(interval))
    const steps = ref([])
    const renderSlider = ref(false)
    const showResults = ref(false)
    const isFetching = ref(false)
    const locationRange = ref(minimum.value)
    const allResults = ref([])
    const results = ref([])
    const facetsResults = ref([])
    const totalResults = ref(null)
    const onceSubmitted = ref(false)
    const isDragging = ref(false)
    const multiSelectRef = ref(null)
    const {generateConceptHint} = useConcepts()
    let searchTimer = null
    const currentPage = ref(null)
    const currentSort = ref(null)
    const filters = ref([])
    const currentPerPage = ref(null)

    watch(range, () => {
      locationRange.value = range.value
    })

    watch(locationChanged, () => {
      search()
    })

    useMounted(props.nodeId)

    if (!Number.isInteger((maximum.value - minimum.value) / skipInterval.value)) {
      skipInterval.value = 1
    }

    const debounce = (callback, timeout = 400) => {
      clearTimeout(searchTimer)
      searchTimer = setTimeout(callback, timeout)
    }

    const {
      queryParams,
      updateQueryParams
    } = useQueryParams()

    const sortingOptions = facet?.['@nodes'].map(name => facet[name]).filter(x => x.sorting === 'true')?.map((x) => {
      x.selected = x.id.toLowerCase() === decodeURI(queryParams.value?.sort || '').toLowerCase()
      return x
    })

    const resetAll = () => {
      reset.value = true
      showResults.value = false
      results.value = []
      nextTick(() => {
        onceSubmitted.value = false
        updateAllQueryParams()
      })
      clearTimeout(searchTimer)
      setTimeout(() => {
        reset.value = false
      }, 50)
      $ga4?.events?.form_submission_attempt('Dealer Inventory')
    }

    const filterOutResults = () => {
      results.value = allResults.value.filter((item) => {
        if (!filters.value.length) { // no filters - return all
          return true
        }
        const passed = {}
        filters.value.forEach((f) => {
          const key = f.split(':').shift()
          const value = f.split(':').pop()
          switch (key) {
          case 'model': passed.model = passed.model ? passed.model : item.name === value; break
          case 'dealer': passed.dealer = passed.dealer ? passed.dealer : item.dealer?.arNumber === value; break
          case 'reservation': passed.reservation = item.dealer?.commerceOptIn && item.status !== STOCK_STATUSES.COMING_SOON.key
          }
        })
        return Object.values(passed).reduce((res, cur) => res && cur, true)
      })
      totalResults.value = results.value.length
    }

    const extractFacets = (items) => {
      const modelsFacet = []
      const dealersFacet = []
      let hasReservation = 0
      items
        .filter(({dealer}) => dealer?.arNumber)
        .forEach(({name, dealer, status}) => {
          modelsFacet.push(name)

          dealersFacet.push({
            id: dealer.arNumber,
            name: dealer.name
          })

          hasReservation += (
            dealer.commerceOptIn &&
            status !== STOCK_STATUSES.COMING_SOON.key
          )
            ? 1
            : 0
        })

      const dealersFacetCounted = {}
      for (const d of dealersFacet) {
        dealersFacetCounted[d.id] = {name: d.name, count: dealersFacetCounted[d.id] !== undefined ? dealersFacetCounted[d.id].count + 1 : 1}
      }
      const extractedFacets = []
      if (Object.keys(modelsFacet).length > 1) {
        extractedFacets.push({
          id: 'model',
          label: props.modelFacetLabel,
          filterField: 'model',
          values: Object.entries(modelsFacet.reduce((acc, curr) => {
            acc[curr] = (acc[curr] || 0) + 1
            return acc
          }, {})).map(i => ({id: i[0], name: i[0], count: i[1]}))
        })
      }
      if (Object.keys(dealersFacetCounted).length > 1) {
        extractedFacets.push({
          id: 'dealer',
          facetType: FACET.STATIC,
          label: props.dealerFacetLabel,
          filterField: 'dealer',
          values: Object.entries(dealersFacetCounted).map(i => ({id: i[0], name: i[1].name, count: i[1].count}))
        })
      }
      if (hasReservation > 0) {
        extractedFacets.push({
          id: 'reservation',
          label: props.reservationFacetLabel,
          filterField: 'reservation',
          values: [
            {
              id: props.reservationFacetValueLabel,
              name: props.reservationFacetValueLabel,
              count: hasReservation
            }
          ]
        })
      }
      facetsResults.value = extractedFacets
    }

    const search = async () => {
      if (!category?.value?.irwName) {
        return
      }
      locationRange.value = range.value
      try {
        isFetching.value = true
        const unitOfLength = metric.value === DISTANCE_METRICS.MI ? DISTANCE_METRICS.MI_NAME : DISTANCE_METRICS.KM_NAME
        $ga4.events.search_inventory(category.value.irwName, undefined, range.value, address.value)
        facetsResults.value = []
        const {
          results: items
        } = await $services.dealerinventory.searchResultsV2(address.value, countryCode.value, range.value, unitOfLength, category.value, [], limit, [])

        if (items) {
          allResults.value = items
          extractFacets(items)

          const lastVisitedModel = getWithExpiry(DEALER_INVENTORY_KEYS.LAST_VISITED_MODEL)
          const modelName = lastVisitedModel?.name || lastVisitedModel?.title || lastVisitedModel?.['@name']

          if (modelName) {
            const modelExistsInFacets = facetsResults.value?.find(f => f.id === 'model' && f.values.find(v => v.id === modelName))
            if (modelExistsInFacets) {
              filters.value = [...filters.value, `model:${modelName}`]
              removeWithExpiry(DEALER_INVENTORY_KEYS.LAST_VISITED_MODEL)
            }
          }

          filterOutResults()
        } else {
          totalResults.value = null
        }
        onceSubmitted.value = true
        showResults.value = true

        updateAllQueryParams()
        scrollToResults()
      } catch (error) {
        // eslint-disable-next-line no-console
        console.error(error)
        results.value = []
        totalResults.value = null
        isFetching.value = false
        showResults.value = true
        $ga4?.events?.form_submission_failure('Dealer Inventory', error)
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

    watch(address, (newVal) => {
      if (!newVal) {
        resetAll()
        return
      }
      if (countryCode.value?.toUpperCase() === COUNTRY_CODES.UnitedStates) {
        metric.value = DISTANCE_METRICS.MI
        if (onceSubmitted.value) {
          debounce(search)
        }
      } else {
        metric.value = DISTANCE_METRICS.KM
        if (onceSubmitted.value) {
          debounce(search)
        }
      }
      $ga4?.events?.form_submission_attempt('Dealer Inventory')
    })

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
      $ga4?.events?.form_submission_attempt('Dealer Inventory')
    }

    const updateAllQueryParams = () => {
      updateQueryParams({
        location: address.value.length && address.value,
        country: countryCode.value.length && countryCode.value,
        distance: locationRange.value,
        perPage: currentPerPage.value,
        page: currentPage.value,
        sort: currentSort.value,
        filters: filters.value.join('|')
      })
    }

    useFetch(async () => {
      labels.value = await $services.bobcatapi.getConcepts(['/common/labels/dealerinventory', '/%region%/common/labels/dealerinventory'])
      const productTypes = await $services.dealerinventory.getProductTypes()
      const magnoliaNode = await $services.bobcatapi.getCategories([props.productCategory])
      category.value = productTypes.find(productType => productType.irwName === magnoliaNode[0].IRWProductName)
      debounce(search)
    })

    onBeforeMount(async () => {
      if (Object.keys(queryParams.value)?.length) {
        // restoring initial values from query params
        const addressFromQuery = queryParams.value?.location ? decodeURI(queryParams.value?.location) : null
        const countryCodeFromQuery = queryParams.value?.country ? decodeURI(queryParams.value?.country) : null
        const locationRangeFromQuery = queryParams.value?.distance ? Math.max(minimum.value, Math.min(parseInt(decodeURI(queryParams.value?.distance)), maximum.value)) : null

        if (addressFromQuery) { saveLocation(LOCATION_KEYS.ADDRESS, addressFromQuery) }
        if (locationRangeFromQuery) { saveLocation(LOCATION_KEYS.RANGE, locationRangeFromQuery) }
        if (countryCodeFromQuery) { saveLocation(LOCATION_KEYS.COUNTRY_CODE, countryCodeFromQuery) }

        currentPage.value = queryParams.value?.page ? decodeURI(queryParams.value?.page) : currentPage.value
        currentPerPage.value = queryParams.value?.perPage ? decodeURI(queryParams.value?.perPage) : currentPerPage.value
        currentSort.value = queryParams.value?.sort ? decodeURI(queryParams.value?.sort) : currentSort.value
        filters.value = queryParams.value?.filters ? queryParams.value?.filters?.split('|') : filters.value

        if (address.value && countryCode.value && range.value && category.value) {
          debounce(search)
        } else if (addressFromQuery || locationRangeFromQuery || countryCodeFromQuery) {
          notify()
        }
      }
    })

    onMounted(() => {
      if (typeof window !== 'undefined' && window.document) {
        renderSlider.value = true
      }

      filters.value = queryParams.value?.filters ? queryParams.value?.filters?.split('|') : filters.value

      const searchCondition = Boolean(address.value && countryCode.value && range.value && category.value)
      if (searchCondition) {
        search()
      }
    })

    const handleAddressSelected = (fullAddress, country, zip) => {
      saveLocation(LOCATION_KEYS.COUNTRY_CODE, country)
      saveLocation(LOCATION_KEYS.ADDRESS, fullAddress)
      saveLocation(LOCATION_KEYS.ZIP_CODE, zip)
    }

    const handleAddressTextChanged = (fullAddress) => {
      if (!fullAddress) {
        address.value = ''
        countryCode.value = ''
        showResults.value = false
      }
    }

    const resolveMetricName = (metric, labels) => {
      if (metric === DISTANCE_METRICS.MI) {
        return labels.mi || DISTANCE_METRICS.MI_NAME
      }
      return labels.km || DISTANCE_METRICS.KM_NAME
    }

    const metricName = computed(() => resolveMetricName(metric.value, labels.value))

    return {
      locationRange,
      category,
      totalResults,
      reset,
      resetAll,
      metric,
      minimum,
      maximum,
      skipInterval,
      steps,
      renderSlider,
      showResults,
      search,
      isFetching,
      results,
      DISTANCE_METRICS,
      onceSubmitted,
      sortingOptions,
      handleDragStart,
      handleDragEnd,
      multiSelectRef,
      generateConceptHint,
      labels,
      scrollToResults,
      currentPage,
      currentPerPage,
      currentSort,
      updateAllQueryParams,
      debounce,
      filterOutResults,
      facetsResults,
      filters,
      addYoutubeNoCookie,
      handleAddressSelected,
      address,
      handleAddressTextChanged,
      resolveMetricName,
      metricName
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

div[data-test-id="dl-inv"] {
  .dinv-btn {
    min-width: auto;
    max-height: 2.5rem;

    @include media-breakpoint-up(sm) {
      width: auto !important;
    }
  }

  .dinv-location {
    min-width: 16rem;

    @include media-breakpoint-up(lg) {
      min-width: 22rem;
    }
  }

  .dinv-search-info {
    min-height: 20rem;
  }
}

div[data-test-id="dinv-more-results"] p:last-child {
  margin-bottom: 0;
}

section[data-test-id='dl-inv-search'] {
  iframe {
    width: 100%;
  }
}
</style>
