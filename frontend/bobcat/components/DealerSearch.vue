<template>
  <div data-test-id="dl-inv">
    <section data-test-id="dl-inv-search" class="container py-4">
      <div class="row">
        <LazyHydrate never>
          <div class="col-12 mb-5">
            <component :is="htag" v-if="title" :class="htag">
              {{ title }}
            </component>
            <div v-if="description" v-html="addYoutubeNoCookie(description)" />
          </div>
        </LazyHydrate>

        <div class="col-12">
          <form data-test-id="dl-inv-search-form" class="ngb-form d-flex align-items-start flex-md-row flex-column">
            <div class="d-flex flex-column w-100 w-md-auto">
              <div class="mr-0 mr-md-2">
                <ZipCode
                  ref="zipCodeComponent"
                  :auto-detect="autoDetect"
                  :reset="reset"
                  name="zip_code"
                  :init-value="zipCode"
                  :disabled="isFetching"
                  icon="pin"
                  :api-key="apiKey"
                  required
                  required-sign
                  :label="zipCodeLabel"
                  :placeholder="zipCodePlaceholder"
                  :validation-message="validationMessage"
                  :on-change="handleZipCodeChange"
                  :on-enter="onKeyDownEnter"
                />
                <LazyHydrate never><label>{{ distanceLabel }}</label></LazyHydrate>
                <client-only>
                  <VueSlider
                    v-if="renderSlider"
                    v-model="locationRange"
                    :disabled="isFetching || !isZipCodeValid"
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
            </div>
            <div
              class="d-flex align-items-start flex-lg-row flex-column w-100 mt-3 mt-md-0 w-md-auto mx-0 mx-md-2 mx-lg-0"
            >
              <div class="mx-0 mx-md-2 w-100 product-type-wrapper">
                <Dropdown
                  :reset="reset"
                  :validation-message="productTypeValidationMessage"
                  :label="productTypeLabel"
                  name="product_type"
                  related-dropdowns
                  :init-value="selectedType"
                  :placeholder="productTypePlaceholder"
                  :options="productTypes"
                  required
                  required-sign
                  :on-change="handleTypeChange"
                  get-whole-value
                  :disabled-dropdown="isFetching || !isZipCodeValid"
                  :force-validation="isSubmit"
                />
              </div>
              <div class="mx-0 mx-md-2 w-100">
                <MultiSelect
                  ref="multiSelectRef"
                  :reset="reset"
                  related-dropdowns
                  default-id="value"
                  :validation-message="modelValidationMessage"
                  :label="productModelLabel"
                  name="product_model"
                  :init-value="selectedModels"
                  :placeholder="productModelPlaceholder"
                  :options="productModels"
                  :on-change="() => {}"
                  :disabled-dropdown="isFetching || Object.keys(selectedType).length === 0"
                  get-whole-value
                  @changed="handleModelChange"
                />
              </div>
            </div>
            <div class="d-flex align-items-center mt-0 mt-md-5 ml-0 ml-md-2 w-100 w-sm-auto flex-column flex-sm-row">
              <b-button
                variant="primary"
                size="lg"
                type="button"
                :disabled="!isZipCodeValid || isFetching || !(zipCode && selectedTypeNotExists && locationRange)"
                class="btn submit-button w-sm-auto"
                @click.prevent="() => {if (!isFetching) debounce(search)}"
              >
                <span class="d-flex align-items-center justify-content-center">
                  <b-spinner v-if="isFetching" class="mr-2" small />
                  <LazyHydrate never><span>{{ searchLabel }}</span></LazyHydrate>
                </span>
              </b-button>
              <button v-if="zipCode || selectedTypeNotExists || (selectedModels && selectedModels.length)"
                      :class="isFetching ? 'text-gray-400' : 'text-primary'"
                      :disabled="isFetching"
                      class="btn font-weight-bold border-0 bg-transparent d-flex align-items-center justify-content-center p-0 mt-3 mt-sm-0 w-100 w-sm-auto"
                      @click.prevent="resetAll"
              >
                <b-icon icon="reload" class="mr-1 h4 m-0" />
                <LazyHydrate never><span>{{ resetLabel }}</span></LazyHydrate>
              </button>
            </div>
          </form>
        </div>
      </div>
    </section>
    <b-collapse v-model="showResults">
      <div v-if="totalResults && totalResults > limit" class="container mb-4">
        <LazyHydrate never>
          <div class="row">
            <div class="col-12">
              <WarningAlert type="info"
                            :title="generateConceptHint('dealerinventory/moreResults')"
                            data-test-id="dinv-more-results"
              >
                <div v-html="(labels.moreResults || '').replace('%LIMIT%', limit)" />
              </WarningAlert>
            </div>
          </div>
        </LazyHydrate>
      </div>

      <SearchResults :distance="locationRange"
                     :metric="metric === DISTANCE_METRICS.MI ? DISTANCE_METRICS.MI_NAME : DISTANCE_METRICS.KM_NAME"
                     :zip-code="zipCode"
                     :no-results-msg="noResultsMsg"
                     :facets="facetsResults"
                     :labels="labels"
                     :cta-settings="{ctaLabel, showCTA}"
                     :filters="filters"
                     :results="results"
                     :sorting="sortingOptions"
                     :is-fetching="isFetching"
                     :initial-values="{perPage: currentPerPage, page: currentPage}"
                     :selected-type="selectedType"
                     :selected-models="selectedModels"
                     :remove-type="removeType"
                     :remove-model="removeModel"
                     @pageUpdated="(pageNum, perPage) => { currentPage = pageNum; currentPerPage = perPage || currentPerPage; debounce(updateAllQueryParams); scrollToResults() }"
                     @sortingUpdated="(sortObj) => { currentSort = sortObj.value; debounce(updateAllQueryParams) }"
                     @filterResults="(f) => {filters = f; debounce(updateAllQueryParams); debounce(search)}"
      />
    </b-collapse>
  </div>
</template>

<script>
import {computed, nextTick, onBeforeMount, onMounted, ref, useContext, useFetch, watch} from '@nuxtjs/composition-api'
import {LOCATION_KEYS} from '~/constants'
import useLocation from '~/helpers/composables/useLocation'
import {addYoutubeNoCookie} from '~/helpers/functional/AppHelpers'
import config from '~/constants/props/form.config'
import useConcepts from '~/helpers/composables/useConcepts'
import DISTANCE_METRICS from '~/constants/distance'
import {caValidator, usValidator} from '~/constants/validators'
import useQueryParams from '@/helpers/composables/useQueryParams'
import {inEditorEdit} from '~/helpers/vue-editor'
import useMounted from '~/helpers/composables/useMounted'

const {
  htag, title, description, zipCodeLabel,
  distanceLabel, productTypeLabel, productModelLabel,
  zipCodePlaceholder, productTypePlaceholder,
  productModelPlaceholder, searchLabel,
  resetLabel, min, max, interval, apiKey,
  validationMessage, noResultsMsg, autoDetect, facet, limit, ctaLabel, showCTA, hideFilters, nodeId,
  productTypeValidationMessage, modelValidationMessage
} = config

export default {
  components: {
    VueSlider: () => import('vue-slider-component')
  },
  props: {
    htag,
    title,
    description,
    zipCodeLabel,
    distanceLabel,
    productTypeLabel,
    productModelLabel,
    zipCodePlaceholder,
    productTypePlaceholder,
    productModelPlaceholder,
    searchLabel,
    resetLabel,
    min,
    max,
    interval,
    apiKey,
    validationMessage,
    noResultsMsg,
    autoDetect,
    facet,
    limit,
    ctaLabel,
    showCTA,
    hideFilters,
    nodeId,
    modelValidationMessage,
    productTypeValidationMessage
  },
  setup(props) {
    const {min, max, interval, facet, limit} = props
    const {$services, $utils, $ga4} = useContext()
    const {saveLocation, zipCode} = useLocation()
    const reset = ref(false)
    const selectedType = ref({})
    const selectedModels = ref([])
    const metric = ref(DISTANCE_METRICS.MI)
    const caRegexExpression = new RegExp(caValidator)
    const usRegexExpression = new RegExp(usValidator)
    const labels = ref({})
    const minimum = ref(Number(min))
    const maximum = ref(Number(max))
    const skipInterval = ref(Number(interval))
    const steps = ref([])
    const renderSlider = ref(false)
    const isZipCodeValid = ref(false)
    const isSubmit = ref(false)
    const showResults = ref(false)
    const isFetching = ref(false)
    const locationRange = ref(minimum.value)
    const productTypes = ref([])
    const productModels = ref([])
    const results = ref([])
    const facetsResults = ref([])
    const totalResults = ref(null)
    const onceSubmitted = ref(false)
    const isDragging = ref(false)
    const multiSelectRef = ref(null)
    const {generateConceptHint} = useConcepts()
    const zipCodeComponent = ref(null)
    let searchTimer = null
    const currentPage = ref(null)
    const currentSort = ref(null)
    const filters = ref([])
    const currentPerPage = ref(null)
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
      locationRange.value = minimum.value
      nextTick(() => {
        onceSubmitted.value = false
        selectedModels.value = []
        zipCode.value = ''
        selectedType.value = {}
        updateAllQueryParams()
      })
      clearTimeout(searchTimer)
      setTimeout(() => {
        reset.value = false
      }, 50)
      $ga4?.events?.form_submission_attempt('Dealer Inventory')
    }

    const search = async (facetsReset = false) => {
      if (!selectedType.value?.value) {
        return
      }
      try {
        isFetching.value = true
        const unitOfLength = metric.value === DISTANCE_METRICS.MI ? DISTANCE_METRICS.MI_NAME : DISTANCE_METRICS.KM_NAME
        const models = selectedModels.value.map(model => model.value).join(',')
        $ga4.events.search_inventory(selectedType.value?.value, models, locationRange.value, zipCode.value)
        const {
          results: items,
          total,
          facets
        } = await $services.dealerinventory.searchResults(zipCode.value, locationRange.value, unitOfLength, selectedType.value, models, limit, filters.value)

        // if props.hideFilters is not set we need to save facets
        // if filters are on, we need to fetch facets separately
        if (props.hideFilters !== 'true' && (!facetsResults.value.length || facetsReset)) {
          if (filters.value.length) {
            const {facets: baseFacets} = await $services.dealerinventory.searchResults(zipCode.value, locationRange.value, unitOfLength, selectedType.value, [], limit, [])
            facetsResults.value = baseFacets
          } else {
            facetsResults.value = facets
          }
        }

        if (items) {
          results.value = items
          totalResults.value = total
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

    watch(zipCode, (newVal) => {
      if (!newVal) {
        resetAll()
      }
      if (usRegexExpression.test(newVal)) {
        metric.value = DISTANCE_METRICS.MI
        isZipCodeValid.value = true
        if (onceSubmitted.value) {
          debounce(search)
        }
      } else if (caRegexExpression.test(newVal)) {
        metric.value = DISTANCE_METRICS.KM
        isZipCodeValid.value = true
        if (onceSubmitted.value) {
          debounce(search)
        }
      } else {
        isZipCodeValid.value = false
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
      $ga4?.events?.form_submission_attempt('Dealer Inventory')
    }

    const handleZipCodeChange = (v) => {
      saveLocation(LOCATION_KEYS.ZIP_CODE, v)
    }

    const onKeyDownEnter = () => {
      isSubmit.value = isZipCodeValid.value && !selectedType.value?.value
      if (isZipCodeValid.value && selectedType.value?.value) {
        debounce(() => search(true))
      }
    }

    const handleTypeChange = async (v, name, modelsToSelect = [], isFirstTime = false) => {
      if (isFirstTime) {
        reset.value = true
        setTimeout(() => {
          reset.value = false
        }, 50)
      } else {
        filters.value = []
      }
      if (v?.value !== selectedType.value?.value) {
        selectedType.value = v
        updateAllQueryParams()
        const models = await $services.dealerinventory.getModels(v?.productCatalogName || v.value, v.level)
        productModels.value = models?.map(model => ({
          value: model.irwName,
          label: model.name,
          selected: Array.isArray(modelsToSelect) && modelsToSelect.includes(model.name) ? 'true' : 'false',
          disabled: 'false'
        }))?.sort((a, b) => (a.label).localeCompare(b.label))

        selectedModels.value = productModels.value.filter(m => m.selected === 'true' || m.selected === true)

        if (onceSubmitted.value) {
          debounce(() => search(true))
        }
      }
      $ga4?.events?.form_submission_attempt('Dealer Inventory')
    }

    const updateAllQueryParams = () => {
      updateQueryParams({
        location: zipCode.value.length && zipCode.value,
        distance: locationRange.value,
        'product-type': selectedType?.value?.label ? Object.keys(selectedType.value) && encodeURI(selectedType.value.label) : '',
        model: selectedModels.value.length && encodeURI(selectedModels.value.map(x => x.label)),
        perPage: currentPerPage.value,
        page: currentPage.value,
        sort: currentSort.value,
        filters: filters.value.join('|')
      })
    }

    useFetch(async () => {
      labels.value = await $services.bobcatapi.getConcepts(['/common/labels/dealerinventory', '/%region%/common/labels/dealerinventory'])
    })

    const handleModelChange = (v) => {
      if (v) {
        selectedModels.value = v
        if (onceSubmitted.value) {
          multiSelectRef.value?.clickOutside()
          debounce(() => search(true))
        }
      }
      $ga4?.events?.form_submission_attempt('Dealer Inventory')
    }

    const removeType = async () => {
      reset.value = true
      selectedType.value = {}
      setTimeout(() => {
        reset.value = false
      }, 50)

      if (onceSubmitted.value) {
        await debounce(search)
      }
      $ga4?.events?.form_submission_attempt('Dealer Inventory')
    }

    const removeModel = async (v, i) => {
      productModels.value = productModels.value?.map((model) => {
        if (model.value === i.value) {
          model.selected = 'false'
        } else {
          model.selected = v?.find(m => m.value === model.value)?.selected || 'false'
        }
        return model
      })
      selectedModels.value = v?.filter(m => m.selected === true || m.selected === 'true') || []

      if (onceSubmitted.value) {
        await debounce(search)
      }
      $ga4?.events?.form_submission_attempt('Dealer Inventory')
    }

    onBeforeMount(async () => {
      const types = await $services.dealerinventory.getProductTypes()
      productTypes.value = types?.map(type => ({
        ...type,
        value: type.name,
        label: type.name
      }))?.sort((a, b) => a?.sortRanking - b?.sortRanking)?.reverse()
      if (Object.keys(queryParams.value)?.length) {
        // restoring initial values from query params
        if (queryParams.value?.location) { zipCode.value = queryParams.value?.location }
        currentPage.value = queryParams.value?.page ? decodeURI(queryParams.value?.page) : currentPage.value
        currentPerPage.value = queryParams.value?.perPage ? decodeURI(queryParams.value?.perPage) : currentPerPage.value
        currentSort.value = queryParams.value?.sort ? decodeURI(queryParams.value?.sort) : currentSort.value
        locationRange.value = queryParams.value?.distance ? Math.max(minimum.value, Math.min(parseInt(decodeURI(queryParams.value?.distance)), maximum.value)) : locationRange.value
        filters.value = queryParams.value?.filters ? queryParams.value?.filters?.split(',') : filters.value
        if (queryParams.value?.['product-type']) {
          const models = queryParams.value?.model ? decodeURI(queryParams.value?.model)?.split(',') : []
          onceSubmitted.value = true
          await handleTypeChange(productTypes.value.find(x => (x.label.toLowerCase() === decodeURI(queryParams.value?.['product-type'])?.toLowerCase()) || x.irwName.toLowerCase() === decodeURI(queryParams.value?.['product-type'])?.toLowerCase()), '', models, true)
        }
      }
    })

    onMounted(() => {
      if (typeof window !== 'undefined' && window.document) {
        renderSlider.value = true

        if (!queryParams.value?.location && !inEditorEdit && !zipCode.value) {
          setTimeout(() => {
            zipCodeComponent.value?.getCurrentLocation()
          }, 200)
        }
      }
    })

    const selectedTypeNotExists = computed({
      get: () => Object.keys(selectedType.value).length !== 0,
      set: () => {
      }
    })

    return {
      locationRange,
      zipCode,
      selectedType,
      selectedModels,
      handleZipCodeChange,
      onKeyDownEnter,
      handleTypeChange,
      handleModelChange,
      totalResults,
      reset,
      resetAll,
      metric,
      minimum,
      maximum,
      skipInterval,
      steps,
      renderSlider,
      isZipCodeValid,
      isSubmit,
      removeType,
      removeModel,
      showResults,
      search,
      productTypes,
      productModels,
      isFetching,
      results,
      DISTANCE_METRICS,
      onceSubmitted,
      selectedTypeNotExists,
      sortingOptions,
      handleDragStart,
      handleDragEnd,
      multiSelectRef,
      generateConceptHint,
      labels,
      zipCodeComponent,
      scrollToResults,
      currentPage,
      currentPerPage,
      currentSort,
      updateAllQueryParams,
      debounce,
      facetsResults,
      filters,
      addYoutubeNoCookie
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
  .submit-button {
    width: 100%;
  }

  .product-type-wrapper {
    @include media-breakpoint-up(sm) {
      max-width: 16rem;
    }
  }
}

section[data-test-id='dl-inv-search'] {
  iframe {
    width: 100%;
  }
}

div[data-test-id="dinv-more-results"] p:last-child {
  margin-bottom: 0;
}
</style>
