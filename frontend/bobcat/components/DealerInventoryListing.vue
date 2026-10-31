<template>
  <section :class="colorVariant.background" class="dealer-inventory-listing py-5" data-test-id="dl-inv-listing">
    <div class="container">
      <LazyHydrate v-if="content" never>
        <div class="row mb-4" :class="alignmentClass">
          <div class="col-12" :class="colorVariant.headline" data-test-id="dl-inv-listing-content" v-html="addYoutubeNoCookie(content)" />
        </div>
      </LazyHydrate>

      <b-overlay v-if="mdmId" :show="isFetching" blur="0" class="min-h300">
        <DealerInventoryProductSearch v-if="isLoaded && !hasSearched && productTypeOptions.length"
                                      :options="productTypeOptions"
                                      :product-default-values="productDefaultValues"
                                      :others-category-label="othersCategoryLabel"
                                      :headline="searchHeadline"
                                      :description="searchDescription"
                                      :product-label="searchProductLabel"
                                      :placeholder="searchPlaceholder"
                                      :search-label="searchButtonLabel"
                                      :search-placeholder="searchKeywordPlaceholder"
                                      :no-results-label="searchNoResultsLabel"
                                      :text-class="colorVariant.headline"
                                      @search="searchProduct"
        />
        <div v-else-if="isLoaded">
          <div id="dealerInventoryListingResults" class="row align-items-center mb-3" :class="colorVariant.headline">
            <div class="col-12 col-md mb-3 mb-md-0">
              <span :title="generateConceptHint('results')" data-test-id="dl-inv-listing-results-count">{{ total }} {{ labels.results || 'Results' }}</span>
            </div>
            <div class="col-12 col-md-auto d-flex flex-column flex-md-row align-items-md-center">
              <SwitchDropdown :label="labels.sortBy || 'Sort by'"
                              :title="generateConceptHint('sortBy')"
                              :options="sortingOptions"
                              :config="{right: true}"
                              :disabled="!total"
                              class="d-flex align-items-baseline"
                              data-test-id="dl-inv-listing-sorting"
                              icon="sort"
                              fuse-label-and-options-for-mobile
                              @changed="changeSorting"
              />
              <SwitchToggle :value="currency.selected"
                            :title="generateConceptHint('selectCurrency')"
                            :label="labels.selectCurrency || 'Select currency'"
                            :options="[currency.us, currency.cad]"
                            class="d-flex align-items-center mt-3 mt-md-0 ml-md-4"
                            data-test-id="dl-inv-listing-currency"
                            @input="changeCurrency"
              />
            </div>
          </div>

          <FacetsChips class="d-none d-md-block mb-2"
                       :chips="chips"
                       :variant="isDarkVariant ? 'light' : 'dark'"
                       @removeFilteringSelection="removeFiltering"
          />

          <div class="row">
            <div v-if="facetsToDisplay.length" class="col-12 col-md-3 mb-3">
              <FacetsFilter :value="filteringSelected"
                            :facets-to-display="facetsToDisplay"
                            :labels="labels"
                            :chips="chips"
                            data-test-id="dl-inv-listing-facets"
                            show-apply-button
                            @input="changeFiltering"
                            @removeFilteringSelection="removeFiltering"
                            @clearFilteringSelection="clearFiltering"
                            @clearFilteringCategory="clearFiltering"
              />
            </div>

            <div class="col-12 mb-2" :class="{'col-md-9': facetsToDisplay.length}">
              <div v-if="productsToDisplay.length" class="row" data-test-id="dl-inv-listing-results">
                <div v-for="(product, i) in productsToDisplay"
                     :key="`${product.serialNumber}-${currency.selected}__${i}`"
                     class="col-12 col-sm-6 col-lg-4 mb-4"
                >
                  <DealerInventorySearchProduct :product="product"
                                                :category="product.productType || selectedProductType"
                                                :cta-settings="{ctaLabel, showCTA}"
                                                :labels="labels"
                                                :currency="currencyCode"
                                                is-dealer-page
                  />
                </div>
              </div>

              <div v-else-if="!isFetching" class="d-flex justify-content-center">
                <WarningAlert type="info" class="col-12 col-md-8" data-test-id="dl-inv-listing-noresults">
                  <div v-html="selectedProductType || !dealerInventory.length ? noResultsMsg : selectProductMsg" />
                </WarningAlert>
              </div>
            </div>

            <div v-if="total"
                 class="col-12 d-flex justify-content-center listing-pagination"
                 :class="colorVariant.headline"
            >
              <div class="d-flex align-content-center align-content-md-around justify-content-center justify-content-md-around flex-column flex-md-row">
                <SwitchDropdown v-model="perPageObject"
                                class="d-flex align-items-baseline flex ml-0 ml-md-2 mt-2 mt-md-0 mb-2 mb-md-0"
                                data-test-id="dl-inv-listing-pagination-perPage"
                                :label="labels.view || 'View'"
                                :options="perPageOptions"
                                :config="{right: true}"
                                fuse-label-and-options-for-mobile
                                @changed="handlePerPageChange"
                />

                <BPagination v-model="currentPage"
                             align="center"
                             class="mb-md-0"
                             first-number
                             last-number
                             hide-goto-end-buttons
                             no-page-detect
                             label-page="Go to page"
                             data-test-id="dl-inv-listing-pagination"
                             :total-rows="total"
                             :per-page="perPage"
                             @change="handlePageChange"
                >
                  <template #prev-text>&#8249;</template>
                  <template #next-text>&#8250;</template>
                </BPagination>

                <b-form class="d-flex align-items-center justify-content-center" @submit.prevent="updatePageNumber">
                  <label for="dealerInventoryListingGoToPage"
                         class="m-0"
                         :title="generateConceptHint('goToPage')"
                  >{{ labels.goToPage || 'Go to page' }}</label>
                  <b-form-input id="dealerInventoryListingGoToPage"
                                v-model="goToPage"
                                :disabled="total <= perPage"
                                class="listing-pagination-goToPage mx-2 py-2 text-center rounded-0"
                                data-test-id="dl-inv-listing-pagination-goToPage"
                                type="text"
                                trim
                                size="sm"
                                number
                  />
                </b-form>
              </div>
            </div>
          </div>
        </div>
      </b-overlay>
    </div>
  </section>
</template>

<script>
import {computed, nextTick, onMounted, ref, useContext, useFetch, useRouter, watch} from '@nuxtjs/composition-api'
import {BPagination} from 'bootstrap-vue'
import DealerInventoryProductSearch from '~/components/elements/Dealer/DealerInventoryProductSearch.vue'
import DealerInventorySearchProduct from '~/components/elements/Dealer/DealerInventorySearchProduct.vue'
import FacetsFilter from '~/components/elements/FacetsFilter'
import FACET_TYPES from '~/constants/facets'
import {COUNTRY_CODES} from '~/constants/countries'
import {USER_COVEO_METADATA} from '~/constants/coveoItems'
import {DEALER_LOCATOR_PAGE_TEMPLATE_ROUTE} from '~/constants/staticRoutes'
import {ALIGNMENT_CLASSES, LEFT_ALIGNMENT, LIGHT_VARIANT, VARIANTS_CLASSES} from '~/constants/variants/sectionTextVariants'
import {addYoutubeNoCookie} from '~/helpers/functional/AppHelpers'
import useConcepts from '~/helpers/composables/useConcepts'
import useFilteringElements from '~/helpers/composables/useFilteringElements'
import useMounted from '~/helpers/composables/useMounted'

const PRODUCT_FILTER_FIELD = 'productType'
const MODEL_FILTER_FIELD = 'model'
const NEWEST_SORTING = 'newest'
const PER_PAGE_VALUES = [12, 24]
const DARK_VARIANTS = ['dark', 'black']
const CANADA_COUNTRY_VALUES = [COUNTRY_CODES.Canada, 'CANADA']

const getFilterField = filter => filter.split(':').shift().split('~').shift()
const getFilterValues = (selection, field) => selection
  .filter(filter => getFilterField(filter) === field)
  .flatMap(filter => filter.split(':').pop().split(';'))
const isProductFilter = filter => getFilterField(filter) === PRODUCT_FILTER_FIELD
const isDynamicFilter = filter => ![PRODUCT_FILTER_FIELD, MODEL_FILTER_FIELD].includes(getFilterField(filter))
const toFilterValues = selection => selection.flatMap(filter => filter.split(':').pop().split(';').map(value => `${getFilterField(filter)}:${value}`))

export default {
  components: {
    BPagination,
    DealerInventoryProductSearch,
    DealerInventorySearchProduct,
    FacetsFilter
  },
  props: {
    nodeId: {
      type: String
    },
    content: {
      type: String
    },
    productFacetLabel: {
      type: String
    },
    modelFacetLabel: {
      type: String
    },
    noResultsMsg: {
      type: String
    },
    selectProductMsg: {
      type: String
    },
    showCTA: {
      type: String
    },
    ctaLabel: {
      type: String
    },
    variant: {
      type: String,
      default: LIGHT_VARIANT
    },
    sectionAlignment: {
      type: String,
      default: LEFT_ALIGNMENT
    },
    searchHeadline: {
      type: String
    },
    searchDescription: {
      type: String
    },
    searchProductLabel: {
      type: String
    },
    searchPlaceholder: {
      type: String
    },
    searchButtonLabel: {
      type: String
    },
    searchKeywordPlaceholder: {
      type: String
    },
    searchNoResultsLabel: {
      type: String
    },
    productDefaultValues: {
      type: String
    },
    othersCategoryLabel: {
      type: String
    }
  },
  setup(props) {
    const {$services, $utils, $ga4} = useContext()
    const router = useRouter()
    const {generateConceptHint} = useConcepts()
    const labels = ref({})
    const mdmId = ref(null)
    const isLoaded = ref(false)
    const isFetching = ref(false)
    const hasSearched = ref(false)
    const productTypes = ref([])
    const dealerInventory = ref([])
    const productResults = ref([])
    const productFacets = ref([])
    const sorting = ref(NEWEST_SORTING)
    const currentPage = ref(1)
    const goToPage = ref(1)
    let lastRequestId = 0

    const {
      chips,
      filteringSelected,
      initialFilterFacets,
      currency,
      perPage,
      perPageObject,
      changePerPage,
      removeFilteringSelection,
      clearFilteringSelection
    } = useFilteringElements()

    const colorVariant = VARIANTS_CLASSES[props.variant] || VARIANTS_CLASSES[LIGHT_VARIANT]
    const alignmentClass = ALIGNMENT_CLASSES[props.sectionAlignment] ?? ALIGNMENT_CLASSES[LEFT_ALIGNMENT]
    const isDarkVariant = DARK_VARIANTS.includes(props.variant)
    const currencyCode = computed(() => currency.value[currency.value.selected]?.label)

    useMounted(props.nodeId)

    useFetch(async () => {
      labels.value = await $services.bobcatapi.getConcepts(['/common/labels/functional', '/common/labels/dealerinventory', '/%region%/common/labels/dealerinventory'])
    })

    const productTypeOptions = computed(() => {
      const ranking = productTypes.value.map(type => type.irwName)
      const getRank = irwName => (ranking.includes(irwName) ? ranking.indexOf(irwName) : ranking.length)
      const options = new Map()
      dealerInventory.value.forEach(({productType}) => {
        if (productType?.irwName) {
          const option = options.get(productType.irwName) || {
            ...productType,
            facetCategory: productTypes.value.find(type => type.irwName === productType.irwName)?.facetCategory,
            count: 0
          }
          options.set(productType.irwName, {...option, count: option.count + 1})
        }
      })
      return [...options.values()].sort((a, b) => getRank(a.irwName) - getRank(b.irwName) || (a.name || '').localeCompare(b.name || ''))
    })

    const selectedProductType = computed(() => {
      const [irwName] = getFilterValues(filteringSelected.value, PRODUCT_FILTER_FIELD)
      return productTypeOptions.value.find(option => option.irwName === irwName) || null
    })

    const productFacet = computed(() => ({
      id: PRODUCT_FILTER_FIELD,
      label: props.productFacetLabel || labels.value.product || 'Product',
      filterField: PRODUCT_FILTER_FIELD,
      facetType: FACET_TYPES.STATIC,
      switchOffSortAlphanumeric: true,
      values: productTypeOptions.value.map(option => ({id: option.irwName, name: option.name, count: option.count}))
    }))

    const modelFacet = computed(() => {
      const counts = productResults.value.reduce((result, {name}) => (name ? {...result, [name]: (result[name] || 0) + 1} : result), {})
      return {
        id: MODEL_FILTER_FIELD,
        label: props.modelFacetLabel || labels.value.model || 'Model',
        filterField: MODEL_FILTER_FIELD,
        facetType: FACET_TYPES.DEFAULT,
        values: Object.entries(counts).map(([name, count]) => ({id: name, name, count}))
      }
    })

    const facetsToDisplay = computed(() => {
      if (!productTypeOptions.value.length) {
        return []
      }
      if (!selectedProductType.value) {
        return [productFacet.value]
      }
      return [
        productFacet.value,
        ...(modelFacet.value.values.length ? [modelFacet.value] : []),
        ...productFacets.value.map(facet => ({
          ...facet,
          label: facet.label || facet.id,
          facetType: facet.facetType || FACET_TYPES.DEFAULT
        }))
      ]
    })

    watch(facetsToDisplay, (facets) => {
      initialFilterFacets.value = facets
    }, {immediate: true})

    const filteredResults = computed(() => {
      if (!selectedProductType.value) {
        return []
      }
      const models = getFilterValues(filteringSelected.value, MODEL_FILTER_FIELD)
      return models.length ? productResults.value.filter(({name}) => models.includes(name)) : productResults.value
    })

    const getPrice = product => product.pricing?.estimatedNetPrices?.find(price => price.currency === currencyCode.value)?.value
    const comparePrices = (a, b, direction) => {
      const priceA = getPrice(a)
      const priceB = getPrice(b)
      if (!priceA || !priceB) {
        return Number(!priceA) - Number(!priceB)
      }
      return direction * (priceA - priceB)
    }
    const compareNames = (a, b) => (a.name || a.label || '').localeCompare(b.name || b.label || '')
    const sorters = {
      'price:asc': (a, b) => comparePrices(a, b, 1),
      'price:desc': (a, b) => comparePrices(a, b, -1),
      'name:asc': compareNames,
      'name:desc': (a, b) => compareNames(b, a)
    }

    const sortedResults = computed(() => {
      const sorter = sorters[sorting.value]
      return sorter ? [...filteredResults.value].sort(sorter) : filteredResults.value
    })

    const total = computed(() => sortedResults.value.length)
    const maxPageNumber = computed(() => Math.max(1, Math.ceil(total.value / perPage.value)))
    const productsToDisplay = computed(() => sortedResults.value.slice((currentPage.value - 1) * perPage.value, currentPage.value * perPage.value))

    const sortingOptions = computed(() => [
      {label: labels.value.newestToOldest || 'Newest to Oldest', value: NEWEST_SORTING},
      {label: labels.value.priceLowest || 'Price - Lowest', value: 'price:asc'},
      {label: labels.value.priceHighest || 'Price - Highest', value: 'price:desc'},
      {label: labels.value.modelNameAscending || 'Model Name - Ascending', value: 'name:asc'},
      {label: labels.value.modelNameDescending || 'Model Name - Descending', value: 'name:desc'}
    ].map(option => ({...option, selected: option.value === sorting.value})))

    const perPageOptions = computed(() => PER_PAGE_VALUES.map(value => ({label: value, value, selected: value === perPage.value})))

    const resetPage = () => {
      currentPage.value = 1
      goToPage.value = 1
    }

    const scrollToResults = () => {
      if (process.client) {
        $utils.scrollToElement('dealerInventoryListingResults')
      }
    }

    const getFacetByField = field => facetsToDisplay.value.find(facet => facet.filterField === field)
    const getFacetLabel = field => getFacetByField(field)?.label || field
    const getFacetSection = (field) => {
      const facet = getFacetByField(field)
      return facet ? `facet-${facet.facetType}-${facet.filterField}` : 'dl-inv-listing-facets'
    }

    const pushFilterEvent = async (action, section) => {
      // wait for the filteringSelected watcher, which can reset the selection on product type change
      await nextTick()
      $ga4.events.product_listing_filter(
        sortingOptions.value.find(option => option.selected)?.label,
        action,
        section,
        currencyCode.value,
        chips.value.map(chip => chip.facetValue).join(', ')
      )
    }

    const changeFiltering = (selection) => {
      const previousValues = toFilterValues(filteringSelected.value)
      const currentValues = toFilterValues(selection)
      const appliedFields = new Set(currentValues.filter(value => !previousValues.includes(value)).map(getFilterField))
      const removedFields = new Set(previousValues.filter(value => !currentValues.includes(value)).map(getFilterField))
      const events = [
        ...[...appliedFields].map(field => [`apply ${getFacetLabel(field)}`, getFacetSection(field)]),
        ...[...removedFields].map(field => [`remove ${getFacetLabel(field)}`, getFacetSection(field)])
      ]
      filteringSelected.value = selection
      events.forEach(([action, section]) => pushFilterEvent(action, section))
    }

    const removeFiltering = (item) => {
      const field = getFilterField(item)
      const action = `remove ${getFacetLabel(field)}`
      const section = getFacetSection(field)
      removeFilteringSelection(item)
      pushFilterEvent(action, section)
    }

    const clearFiltering = (category = null) => {
      const facet = category && facetsToDisplay.value.find(f => f.id === category)
      const action = facet ? `remove ${facet.label}` : 'clear all'
      const section = facet ? getFacetSection(facet.filterField) : 'dl-inv-listing-facets'
      clearFilteringSelection(category)
      pushFilterEvent(action, section)
    }

    const changeSorting = (option) => {
      if (option.value === sorting.value) {
        return
      }
      sorting.value = option.value
      resetPage()
      pushFilterEvent('change sort by', 'dl-inv-listing-sorting')
    }

    const changeCurrency = (value) => {
      currency.value.selected = value
      pushFilterEvent('change currency', 'dl-inv-listing-currency')
    }

    const handlePerPageChange = (option) => {
      changePerPage(option)
      resetPage()
      scrollToResults()
    }

    const handlePageChange = (page) => {
      goToPage.value = page
      scrollToResults()
    }

    const updatePageNumber = () => {
      goToPage.value = Math.min(Math.max(parseInt(goToPage.value) || 1, 1), maxPageNumber.value)
      if (goToPage.value !== currentPage.value) {
        currentPage.value = goToPage.value
        scrollToResults()
      }
    }

    const loadProductInventory = async () => {
      const requestId = ++lastRequestId
      const productType = selectedProductType.value
      if (!productType?.facetCategory) {
        productResults.value = productType ? dealerInventory.value.filter(item => item.productType?.irwName === productType.irwName) : []
        productFacets.value = []
        isFetching.value = false
        return
      }
      isFetching.value = true
      let response = null
      try {
        response = await $services.dealerinventory.searchDealerInventory(mdmId.value, productType, filteringSelected.value.filter(isDynamicFilter))
      } catch {
        response = null
      }
      if (requestId === lastRequestId) {
        productResults.value = response?.results || []
        productFacets.value = response?.facets || []
        isFetching.value = false
      }
    }

    watch(filteringSelected, (selection, previous = []) => {
      const previousProduct = previous.find(isProductFilter)
      const products = selection.filter(isProductFilter)
      const product = products.find(filter => filter !== previousProduct) || products[0]
      resetPage()
      if (product !== previousProduct) {
        const productSelection = product ? [product] : []
        if (selection.length !== productSelection.length) {
          filteringSelected.value = productSelection
          return
        }
        loadProductInventory()
        return
      }
      if (selection.filter(isDynamicFilter).join('|') !== previous.filter(isDynamicFilter).join('|')) {
        loadProductInventory()
      }
    })

    const loadDealerInventory = async () => {
      isFetching.value = true
      try {
        const [types, inventory] = await Promise.all([
          $services.dealerinventory.getProductTypes().catch(() => []),
          $services.dealerinventory.searchDealerInventory(mdmId.value)
        ])
        productTypes.value = types || []
        dealerInventory.value = inventory?.results || []
      } catch {
        dealerInventory.value = []
      }
      const country = dealerInventory.value[0]?.dealer?.shippingAddress?.country?.toUpperCase()
      currency.value.selected = CANADA_COUNTRY_VALUES.includes(country) ? currency.value.cad.value : currency.value.us.value
      isLoaded.value = true
      isFetching.value = false
    }

    const getSearchLocation = () => {
      const {location, zip} = router.currentRoute.query || {}
      if (location || zip) {
        return location || zip
      }
      try {
        return JSON.parse(localStorage.getItem(USER_COVEO_METADATA))?.q || undefined
      } catch {
        return undefined
      }
    }

    const searchProduct = (irwName) => {
      const productType = productTypeOptions.value.find(option => option.irwName === irwName)
      $ga4.events.search_inventory(productType?.name || irwName, undefined, undefined, getSearchLocation())
      hasSearched.value = true
      filteringSelected.value = [`${PRODUCT_FILTER_FIELD}:${irwName}`]
      pushFilterEvent(`apply ${getFacetLabel(PRODUCT_FILTER_FIELD)}`, 'dl-inv-listing-search')
    }

    onMounted(() => {
      const urlParam = router.currentRoute.path.split(DEALER_LOCATOR_PAGE_TEMPLATE_ROUTE)[1]?.replace('/', '')
      if (Number.isInteger(parseInt(urlParam))) {
        mdmId.value = urlParam
        loadDealerInventory()
      }
    })

    return {
      labels,
      mdmId,
      isLoaded,
      isFetching,
      hasSearched,
      dealerInventory,
      productTypeOptions,
      colorVariant,
      alignmentClass,
      isDarkVariant,
      currency,
      currencyCode,
      chips,
      filteringSelected,
      facetsToDisplay,
      selectedProductType,
      productsToDisplay,
      total,
      sortingOptions,
      perPage,
      perPageObject,
      perPageOptions,
      currentPage,
      goToPage,
      searchProduct,
      changeSorting,
      changeCurrency,
      changeFiltering,
      handlePerPageChange,
      handlePageChange,
      updatePageNumber,
      removeFiltering,
      clearFiltering,
      generateConceptHint,
      addYoutubeNoCookie
    }
  }
}
</script>

<style lang="scss">
@import '~/assets/sass/pagination';

.dealer-inventory-listing {
  .row.constrained > div {
    max-width: 41.875rem;
  }

  iframe {
    width: 100%;
  }

  div[data-test-id="dl-inv-listing-noresults"] p:last-child {
    margin-bottom: 0;
  }
}
</style>
