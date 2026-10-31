<template>
  <b-overlay :show="loading" blur="0">
    <section class="transition-base py-4 min-h300" data-test-id="listing-with-filtering">
      <div class="container">
        <LazyHydrate v-if="!hideHeader && title" never>
          <div class="row">
            <div class="col-12">
              <component :is="htag" class="mb-2 mb-md-4 h2">{{ title }}</component>
            </div>
          </div>
        </LazyHydrate>

        <div v-if="error" class="row">
          <LazyHydrate never>
            <div class="col-12">
              <WarningAlert>{{ error }}</WarningAlert>
            </div>
          </LazyHydrate>
        </div>

        <div v-else-if="!hideNumberOfResults || translatedChips.length || !hideSorting" class="row mb-2 mb-md-3">
          <div class="col-12 col-md-4 d-none d-md-block">
            <div class="my-2">
              <div v-if="!hideNumberOfResults" :title="generateConceptHint('results')" class="d-none d-md-block">
                <span
                  data-test-id="listing-results__count"
                >{{ total }}</span> {{ labels.results || 'results' }}
              </div>

              <SearchModelFacets
                v-if="!!searchByModel"
                :selected-type="selectedType"
                :selected-models="selectedModels"
                :is-default-product-type="!!defaultProductType"
                :labels="listingLabels"
                @removeType="removeType"
                @removeModel="removeModel"
              />

              <FacetsChips class="d-none d-md-block"
                           :chips="translatedChips"
                           @removeFilteringSelection="removeFilteringSelection"
              />
            </div>
          </div>

          <div class="col-12 col-md-8 d-flex flex-column flex-md-row justify-content-center justify-content-md-end">
            <LazyHydrate v-if="!hideSorting && sortingOptionsWithHiddenFilter.length" when-visible>
              <SwitchDropdown v-model="sortingObject"
                              :label="labels.sortBy || 'Sort by'"
                              :title="generateConceptHint('sortBy')"
                              class="d-flex align-items-baseline flex "
                              data-test-id="listing-sorting"
                              :options="sortingOptionsWithHiddenFilter"
                              :selected-option="sortingObject"
                              :config="{right: true}"
                              icon="sort"
                              fuse-label-and-options-for-mobile
                              @changed="changeSorting"
              />
            </LazyHydrate>

            <SwitchDropdown v-if="!useShowMore && !((maxPageNumber === 1) && hideView)"
                            v-model="perPageObject"
                            :label="labels.view || 'View'"
                            class="d-flex align-items-baseline flex ml-0 ml-md-2 mt-2 mt-md-0"
                            data-test-id="listing-perPage"
                            :options="perPageOptions"
                            :config="{right: true}"
                            fuse-label-and-options-for-mobile
                            @changed="changePerPage"
            />

            <SwitchToggle v-if="showCurrencySelector"
                          v-model="currency.selected"
                          :title="generateConceptHint('selectCurrency')"
                          :label="labels.selectCurrency || 'Select currency'"
                          :options="[currency.cad, currency.us]"
                          class="pl-2 pt-2 d-flex justify-content-center"
            />
          </div>
        </div>
        <div class="row">
          <div v-if="!hideFilters" class="col-12 col-md-3 mb-3">
            <FacetsFilter v-if="initialized && !isLoadingFacets"
                          v-model="filteringSelected"
                          :facets-to-display="facetsToDisplay"
                          :labels="labels"
                          :type="selectedType"
                          :models="selectedModels"
                          :empty-filters="filtersEmpty"
                          :chips="translatedChips"
                          :expandable="expandable"
                          data-test-id="listing-facets"
                          :primary-facets-to-hide="primaryFacetsToHide"
                          :show-apply-button="true"
                          :show-empty-facets="true"
                          :include-facet-label="includeFacetLabel"
                          @input="$emit('clearCompare')"
                          @removeFilteringSelection="removeFilteringSelection"
                          @clearFilteringSelection="clearFilters"
                          @clearFilteringCategory="(_cat) => {$emit('clearCompare'); clearFilteringSelection(_cat)}"
            >
              <template v-if="searchByModel" #search-facets>
                <SearchModelFacets
                  v-if="!!searchByModel"
                  class="d-block d-md-none my-2"
                  :selected-type="selectedType"
                  :selected-models="selectedModels"
                  :is-default-product-type="!!defaultProductType"
                  :labels="listingLabels"
                  @removeType="removeType"
                  @removeModel="removeModel"
                />
              </template>
              <template v-if="searchByModel" #search>
                <SearchModel
                  :reset="reset"
                  :product-type-label="productTypeLabel"
                  :model-field-label="modelFieldLabel"
                  :selected-type="selectedType"
                  :selected-models="selectedModels"
                  :product-types="productTypes"
                  :product-models="productModels"
                  :default-product-type="defaultProductType"
                  :labels="listingLabels"
                  @handleTypeChange="handleProductTypeChangeAndAddToGA4"
                  @handleModelChange="handleModelChangeAndAddToGA4"
                  @search="search"
                />
              </template>
            </FacetsFilter>
            <slot v-else name="facets-loading">
              <ListingSkeleton :cols="1" :items="2" />
            </slot>
          </div>
          <div v-if="!hideNumberOfResults" :title="generateConceptHint('results')" class="col-12 d-md-none mb-3">
            <span
              data-test-id="listing-results__count"
            >{{ total }}</span> {{ labels.results || 'results' }}
          </div>
          <div :class="`col-md-${hideFilters || (filtersEmpty && initialized && !searchByModel) ? '12' : '9'}`"
               class="col-12 mb-2"
               data-test-id="listing__results"
          >
            <slot name="results">
              <ListingSkeleton :cols="hideFilters ? 4 : 3" />
            </slot>
          </div>
          <div v-if="total && maxPageNumber > 1" class="col-12 d-flex justify-content-center listing-pagination">
            <div v-if="!useShowMore"
                 class="d-flex align-content-center align-content-md-around justify-content-center justify-content-md-around  flex-column flex-md-row"
            >
              <SwitchDropdown v-model="perPageObject"
                              class="d-flex align-items-baseline flex ml-0 ml-md-2 mt-2 mt-md-0 mb-2 mb-md-0"
                              data-test-id="listing-perPage"
                              :label="labels.view || 'View'"
                              :options="perPageOptions"
                              :config="{right: true}"
                              fuse-label-and-options-for-mobile
                              @changed="changePerPage"
              />

              <BPaginationNav v-model="page"
                              :number-of-pages="maxPageNumber"
                              :link-gen="linkGenFunction"
                              class="mb-md-0"
                              align="center"
                              no-page-detect
                              hide-goto-end-buttons
                              label-page="Go to page"
                              data-test-id="listing-pagination"
                              @click.prevent.stop
              >
                <template #prev-text><span @click.prevent.stop>&#8249;</span></template>
                <template #page="paginationCfg"><span @click.prevent.stop>{{ paginationCfg.page }}</span></template>
                <template #next-text><span @click.prevent.stop>&#8250;</span></template>
              </BPaginationNav>

              <b-form class="d-flex align-items-center justify-content-center" @submit.prevent="updatePageNumber">
                <label for="goToPage"
                       class="m-0"
                       :title="generateConceptHint('goToPage')"
                >{{ labels.goToPage || 'Go to page' }}</label>
                <b-form-input id="goToPage"
                              v-model="goToPage"
                              class="listing-pagination-goToPage mx-2 py-2 text-center rounded-0"
                              data-test-id="listing-pagination-goToPage"
                              type="text"
                              trim
                              size="sm"
                              number
                />
              </b-form>
            </div>

            <DynamicButton
              v-else-if="results.length < total"
              :variant="showMoreVariantName || BUTTON_NAMES.CLASSIC"
              :href="`?perPage=${(page + 1) * perPage}`"
              :color="showMoreVariantColor"
              :title="generateConceptHint('showMore')"
              @click.prevent="page++"
            >
              {{ labels.showMore || 'Show More' }}
            </DynamicButton>
          </div>
        </div>
      </div>
      <slot name="extra" />
    </section>
  </b-overlay>
</template>

<script>
import {
  computed,
  inject,
  onMounted,
  onServerPrefetch,
  ref,
  ssrRef,
  useContext,
  useFetch,
  useRoute,
  useRouter,
  watch
} from '@nuxtjs/composition-api'
import {BPaginationNav} from 'bootstrap-vue'
import useSearchModel from '../../helpers/composables/useSearchModel'
import SearchModel from './Form/SearchModel.vue'
import SearchModelFacets from './Facets/SearchModelFacets.vue'
import FacetsFilter from '~/components/elements/FacetsFilter'
import useFilteringElements from '@/helpers/composables/useFilteringElements'
import useConcepts from '~/helpers/composables/useConcepts'
import useQueryParams from '@/helpers/composables/useQueryParams'
import useDebounce from '@/helpers/composables/useDebounce'
import FACET_TYPES from '@/constants/facets'
import {BUTTON_NAMES, BUTTON_STYLES} from '~/constants/buttons'

export default {
  components: {
    SearchModelFacets,
    SearchModel,
    BPaginationNav,
    FacetsFilter
  },
  props: {
    title: {
      type: String,
      default: ''
    },
    htag: {
      type: String,
      default: 'h3'
    },
    group: {
      type: [Object, String],
      default: null
    },
    facet: {
      type: [Object, String],
      default: null
    },
    pageType: {
      type: Object,
      default: null
    },
    searchText: {
      type: String,
      default: null
    },
    hideFilters: {
      type: Boolean,
      default: true
    },
    useShowMore: {
      type: Boolean,
      default: false
    },
    hideSorting: {
      type: Boolean,
      default: false
    },
    displayAtGroupLevel: {
      type: String,
      default: 'false'
    },
    hideNumberOfResults: {
      type: Boolean,
      default: false
    },
    showCurrencySelector: {
      type: Boolean,
      default: false
    },
    labels: {
      type: Object,
      default: () => ({})
    },
    expandable: {
      type: Boolean,
      default: true
    },
    showAllPages: {
      type: Boolean,
      default: false
    },
    hideView: {
      type: Boolean,
      default: false
    },
    resultsPerPage: {
      type: String
    },
    hideSelectedVariants: {
      type: String
    },
    showMoreVariant: {
      type: Object
    },
    hideHeader: {
      type: Boolean
    },
    includeFacetLabel: {
      type: Boolean,
      default: false
    }
  },
  setup(props, {emit}) {
    const {$services, $ga4} = useContext()
    const $router = useRouter()
    const route = useRoute()
    const initialized = ssrRef(false, 'listing-initialized')
    const loading = ref(false)
    let scrollEnabled = false
    const loadingTimer = ssrRef(null, 'listing-loading-timer')
    const total = ref(0)
    const results = ref([])
    const error = ref(false)
    const primaryFacetsToHide = ref([])
    // [NGB-4299] cutting 's' below for backward compatibility
    const group = props.group ? props.group.field.endsWith('s') ? props.group.field.slice(0, -1) : props.group.field : 'product'
    const pageType = props.pageType ? props.pageType.field : null
    const productCategory = ref(null)
    const category = ref(null)
    const subcategory = ref(null)
    const isAttachmentOrProduct = ref(['attachment', 'product'].includes(group))
    const facetCategory = props.facet?.['@path'] || false
    const facetsNodes = []
    const campaigns = inject('campaigns', [])
    const {showAllPages, resultsPerPage} = props
    const showMoreVariantName = ref(props?.showMoreVariant?.field)
    const showMoreVariantColor = ref(props?.showMoreVariant?.color)

    const {
      defaultProductType,
      clearFacetsOnSearch,
      searchByModel,
      modelFieldLabel,
      multiSelectRef,
      productModels,
      productTypeLabel,
      productTypes,
      reset,
      selectedModels,
      selectedType,
      removeModel,
      labels: listingLabels,
      removeType,
      handleModelChange,
      handleTypeChange,
      fetchModelsForProduct
    } = useSearchModel(props)

    const {generateConceptHint} = useConcepts()
    const {debounce} = useDebounce(loadingTimer)
    const {
      queryParams,
      updateQueryParams
    } = useQueryParams()

    const page = ref(queryParams.value?.page || 1)
    const goToPage = ref(page.value)

    const sortingSelected = ref(queryParams.value?.sort || '')
    const sortingOptions = ref([])
    const sortingOptionsWithHiddenFilter = ref([])

    const isLoadingFacets = ref(false)

    const productTypeFromQuery = decodeURIComponent(queryParams.value?.['product-type'] || '')
    const modelFromQuery = decodeURIComponent(queryParams.value?.model || '').split(',').filter(Boolean)

    const {
      chips,
      filteringSelected,
      initialFilterFacets,
      currentFilterFacets,
      perPage,
      perPageInitial,
      perPageObject,
      perPageOptions,
      changePerPage,
      removeFilteringSelection,
      clearFilteringSelection,
      currency,
      filtersEmpty,
      getFilteringOrderValue
    } = useFilteringElements([], queryParams.value, campaigns.value?.length, resultsPerPage, props.includeFacetLabel)

    const updateAllQueryParams = () => {
      updateQueryParams({
        'product-type': selectedType?.value?.label ? Object.keys(selectedType.value) && encodeURI(selectedType.value.label) : '',
        model: selectedModels.value.length && encodeURI(selectedModels.value.map(x => x.label))
      })
    }

    const handleProductTypeChangeAndAddToGA4 = (v) => {
      if (isAttachmentOrProduct.value) {
        handleTypeChange(v)
      }
    }

    const handleModelChangeAndAddToGA4 = (v) => {
      if (isAttachmentOrProduct.value) {
        handleModelChange(v)
      }
    }

    const removeProductType = async () => {
      removeType()

      debounce(fetchOnChange)
    }

    const removeProductModel = async (v, i) => {
      removeModel(v, i)

      debounce(fetchOnChange)
    }

    const search = () => {
      if (clearFacetsOnSearch.value) {
        clearFilteringSelection()
      }

      $ga4.events.product_listing_filter(sortingObject.value?.label, 'search by model', productTypeLabel || 'Model', undefined, selectedModels.value?.map(m => m?.label).join(', '))

      updateAllQueryParams()
      fetchOnChange()
    }

    const facetsToDisplay = ref([])

    const changeSorting = (s) => {
      if (s && sortingSelected.value !== s?.value) {
        sortingSelected.value = s?.value
        if (isAttachmentOrProduct.value) {
          $ga4.events.product_listing_filter(sortingObject.value.label, 'change sort by')
        }
      }
    }

    const findFacet = (val) => {
      if (!val) {
        return
      }
      const selectedFacetKey = val.split(':').shift().split('~').shift()
      let facetValue = val.split(':').pop()

      let filterFacet = currentFilterFacets?.value?.find(f => f.filterField === selectedFacetKey && f.values?.some(v => v.name === facetValue))
      if (!filterFacet) {
        filterFacet = currentFilterFacets?.value?.find(f => f.filterField === selectedFacetKey)
      }

      const facetLabel = filterFacet ? filterFacet.label || filterFacet.name : selectedFacetKey

      if (filterFacet?.facetType === FACET_TYPES.NUMERIC) {
        const values = facetValue.split('!')
        facetValue = [values[0], filterFacet.unit, '-', values[1], filterFacet.unit].join(' ')
        return {
          facetValue,
          facetLabel,
          numeric: true
        }
      } else {
        facetValue.replace('-', ' ')
        return {
          facetValue,
          facetLabel
        }
      }
    }

    const ga4filterEvent = (o, n) => {
      if (!isAttachmentOrProduct.value) {
        return
      }
      if (!o || !n) {
        return
      }
      const newFacet = n.find(n => !o.includes(n))
      const oldFacet = o.find(o => !n.includes(o))

      if (n.length === 0 && o.length > 1) {
        $ga4.events.product_listing_filter(undefined, 'clear all')
        return
      }

      if (n.length > o.length) { // add
        const facet = findFacet(newFacet)
        if (facet.numeric) { // range
          $ga4.events.product_listing_filter(undefined, `add range ${facet?.facetLabel}`, facet?.facetLabel, facet?.facetValue)
        } else {
          $ga4.events.product_listing_filter(undefined, `add ${facet?.facetLabel}`, facet?.facetLabel, undefined, facet?.facetValue)
        }
      } else if (n.length < o.length) { // remove
        const facet = findFacet(oldFacet)
        if (facet.numeric) { // range
          $ga4.events.product_listing_filter(undefined, `remove range ${facet?.facetLabel}`, facet?.facetLabel, facet?.facetValue)
        } else {
          $ga4.events.product_listing_filter(undefined, `remove ${facet?.facetLabel}`, facet?.facetLabel, undefined, facet?.facetValue)
        }
      } else { // change numeric only
        const facet = findFacet(newFacet)
        const newValues = newFacet?.split(':')?.pop()
        const oldValues = oldFacet?.split(':')?.pop()
        if (newValues?.split('!')?.[0] === oldValues?.split('!')?.[0]) {
          $ga4.events.product_listing_filter(undefined, `update ${facet?.facetLabel} max`, facet?.facetLabel, facet?.facetValue)
        } else {
          $ga4.events.product_listing_filter(undefined, `update ${facet?.facetLabel} min`, facet?.facetLabel, facet?.facetValue)
        }
      }
    }

    const linkGenFunction = (pageNum) => {
      const url = $router?.currentRoute?.path || ''
      return url + pageNum === 1 ? '' : `?page=${pageNum}`
    }

    const fetchListingResults = async (options = {}, isInitial = false) => {
      const filtersString = filteringSelected.value?.join('|') || false

      if (!isInitial) {
        updateQueryParams({
          facets: filteringSelected.value?.length && filteringSelected.value,
          sort: sortingOptions.value?.[0]?.value !== sortingSelected.value && sortingSelected.value,
          page: !props.useShowMore && parseInt(page.value) > 1 && parseInt(page.value),
          perPage: perPageInitial !== perPage.value && perPage.value,
          q: !!props.searchText && props.searchText
        })
      }

      const selectionConfig = {
        sort: sortingSelected.value || sortingObject.value?.value,
        page: parseInt(page.value) - 1,
        perPage: (perPage.value - campaigns.value?.length) || props.resultsPerPage,
        searchText: props.searchText,
        filters: filtersString,
        hideSelectedVariants: props.hideSelectedVariants,
        displayOnGroupLevel: props.displayAtGroupLevel === 'true',
        pageType,
        showAllPages,
        ...options
      }

      let results
      if (defaultProductType) {
        selectionConfig.productType = defaultProductType.type
        if (selectedModels.value.length > 0) { selectionConfig.modelIds = selectedModels.value.map(x => x?.value) }
        const {data} = await $services.content.getListingResults('productRelatedAttachments', facetCategory, category.value, subcategory.value, selectionConfig)
        results = data
      } else if (selectedType.value?.value) {
        selectionConfig.productType = selectedType.value.value
        if (selectedModels.value.length > 0) { selectionConfig.modelIds = selectedModels.value.map(x => x?.value) }
        const {data} = await $services.content.getListingResults('productRelatedAttachments', facetCategory, category.value, subcategory.value, selectionConfig)
        results = data
      } else {
        const {data} = await $services.content.getListingResults(group, facetCategory, category.value, subcategory.value, selectionConfig)
        results = data
      }

      return results
    }

    const updateDropdownUI = (unwatch) => {
      if (unwatch) {
        unwatch()
      } else {
        reset.value = true

        setTimeout(() => {
          reset.value = false
        }, 50)
      }

      // update dropdown UI after hydration
      selectedType.value = {...selectedType.value}
      selectedModels.value = [...selectedModels.value]
    }

    onMounted(() => updateDropdownUI())

    const updateFacetsToDisplay = () => {
      facetsToDisplay.value = initialFilterFacets.value?.map((facet) => {
        facet.label = facet.id
        facet.facetType = facet.facetType || FACET_TYPES.DEFAULT
        facet.values = facet.values?.map((facetCategory) => {
          const matchingCurrentFilter = currentFilterFacets.value.find(x => x.id === facet.id)
          facetCategory.count = matchingCurrentFilter?.values?.filter(y => y.id === facetCategory.id)?.[0]?.count || 0
          return facetCategory
        })
        facet.switchOffSortAlphanumeric = facetsNodes.find(x => x.label === facet.label && (x.id === facet.filterField || facet.filterField === 'tags.value'))?.switchOffSortAlphanumeric === 'true'
        return facet
      }) || []
    }

    const fetchAllFacets = async () => {
      return (await fetchListingResults({
        perPage: 1,
        page: 0,
        filters: false
      })).facets
    }

    const ensureInitialFacets = async () => {
      if (!initialFilterFacets.value?.length && !isLoadingFacets.value) {
        isLoadingFacets.value = true
        try {
          initialFilterFacets.value = currentFilterFacets.value
          if (filteringSelected.value?.length) {
            // fetch ALL filters
            initialFilterFacets.value = await fetchAllFacets()
          }
        } catch (e) {
          // eslint-disable-next-line no-console
          console.error('[ListingWithFiltering] Failed to fetch all facets. Falling back to partial facets...')
        }
        isLoadingFacets.value = false
      }
    }
    const initializeProductsByQueryParams = async () => {
      if (productTypeFromQuery) {
        const productTypeToPrefill = productTypes.value.find(type => type.label === productTypeFromQuery) || {}
        if (!productTypeToPrefill.value) { return }

        await fetchModelsForProduct(productTypeToPrefill)

        selectedType.value = productTypeToPrefill
        selectedModels.value = productModels.value.filter(model => modelFromQuery.includes(model.label))

        if (!process.server) {
          const unwatch = watch(reset, () => {
            if (!reset.value) {
              setTimeout(() => {
                updateDropdownUI(unwatch)
              }, 50)
            }
          })
        }
      }
    }

    const initializeBasedOnQueryParams = async () => {
      const params = queryParams.value || {}
      if (Object.keys(params).length > 0) {
        await initializeProductsByQueryParams()

        const facetsArray = Array.isArray(queryParams.value?.facets) ? queryParams.value?.facets : queryParams.value?.facets?.split(',')
        const facetsFromQuery = []
        facetsArray?.forEach((x) => {
          const selectedFacetKey = x?.split(':')?.shift()?.split('~')?.shift()
          const lastValue = x?.split(':')?.pop()?.split('|')?.pop()
          const found = initialFilterFacets.value?.find(f => f?.filterField === selectedFacetKey && (!f?.values || f?.values?.some(v => v.name === lastValue)))
          if (found) {
            // enforce new facet query format
            const facetLabelPart = props.includeFacetLabel ? `~${found.name}` : ''
            facetsFromQuery.push(`${found.filterField}${facetLabelPart}:${x?.split(':')?.[1]}`)
          } else {
            facetsFromQuery.push(x)
          }
        })
        filteringSelected.value = facetsFromQuery
        params.sort && sortingOptions.value?.find(x => x.value === params.sort) && changeSorting({value: params.sort})
        params.perPage && (Number(params.perPage) % perPageInitial === 0 && params.perPage < 300) && (perPage.value = Number(params.perPage)) // 300 taken as maxLimit to disallow slowing down api
        params.page && !props.useShowMore && (page.value = Number(params.page))
      }
    }

    const getResultsForSelection = async (options = {}, resetFacets = false, initial = false) => {
      const isInitial = initial || !(initialFilterFacets.value?.length && initialized.value)
      loading.value = !isInitial
      error.value = false

      try {
        let data = await fetchListingResults(options, isInitial)
        currentFilterFacets.value = data.facets || []

        await ensureInitialFacets()

        if (isInitial || resetFacets) {
          if (!initialized.value) {
            await initializeBasedOnQueryParams()
            data = await fetchListingResults(options, false)
            currentFilterFacets.value = data.facets || []
          }

          initialized.value = true
        }

        total.value = data.total || 0
        if (total.value < perPage) {
          page.value = 1
        }

        const newData = data.results || []
        if (props.useShowMore) {
          results.value = page.value === 1 ? newData : [...results.value, ...newData]
        } else {
          results.value = newData
        }

        emit('resultListUpdate', {
          results: results.value,
          page: page.value,
          total: total.value,
          initial: isInitial,
          currentFilterFacets: currentFilterFacets.value,
          currentSorting: sortingSelected.value
        })

        updateFacetsToDisplay()
      } catch (e) {
        error.value = `[Listing: ${group}] Unable to get results`
        // eslint-disable-next-line no-console
        console.error(error.value, e)
        loading.value = false
        $ga4?.events?.error_general({
          error_details: error.value
        }, 'data error')
      }
    }

    const clearResults = () => {
      results.value = []
      total.value = 0
      page.value = 1

      emit('resultListUpdate', {
        results: [],
        page: 1,
        total: 0
      })
    }

    const fetchOnChange = async (resetPage = false, resetFacets = false, initial = false) => {
      if (group === 'product' && !category.value) {
        return
      }
      if (props.searchText === '' && group !== 'product') {
        clearResults()
        return
      }

      if (sortingOptions.value?.length) {
        loading.value = true
        if (resetPage && page.value !== 1) {
          page.value = 1
        } else {
          await getResultsForSelection({}, resetFacets, initial)
        }
        loading.value = false
      } else {
        error.value = `[Listing: ${group}] Configuration not found for sorting options`
        // eslint-disable-next-line no-console
        console.error(error.value)
        loading.value = false
        $ga4?.events?.error_general({
          error_details: error.value
        }, 'data error')
      }

      if (process.client && !props.useShowMore && !resetPage && scrollEnabled) {
        setTimeout(() => {
          const bodyRect = document.body.getBoundingClientRect().top
          const listing = document.querySelector('section[data-test-id="listing-with-filtering"]').getBoundingClientRect().top
          const header = document.querySelector('header[data-test-id="main-header"]')?.clientHeight - 1 || 0
          const elementPosition = listing - bodyRect
          const offsetPosition = elementPosition - header
          window.scrollTo({behavior: 'smooth', top: offsetPosition})
        }, 200)
      }

      if (process.client && !scrollEnabled && !initial) {
        scrollEnabled = true
      }
    }

    const maxPageNumber = computed({
      set: () => {
      },
      get: () => Math.ceil((total.value + (total.value / perPage.value * (campaigns.value?.length) || 0)) / perPage.value)
    })

    const updatePageNumber = () => {
      if (goToPage.value < 1) {
        goToPage.value = 1
      } else if (goToPage.value > maxPageNumber.value) {
        goToPage.value = maxPageNumber.value
      }
      if (goToPage.value === page.value) {
        return
      }
      page.value = goToPage.value
    }

    const clearFilters = (category = null) => {
      clearFilteringSelection(category)

      reset.value = true
      selectedModels.value = []
      selectedType.value = {}
      updateAllQueryParams()
      clearFacetsOnSearch.value = true
      setTimeout(() => {
        reset.value = false
      }, 50)

      fetchOnChange()
    }

    watch(() => sortingOptions.value, (s) => {
      if (!sortingSelected.value?.length && s?.[0]) {
        sortingSelected.value = s[0]?.value
      }
    })

    if (process.client) {
      watch(sortingSelected, (n, o) => {
        if (n?.length && o?.length && n !== o) {
          debounce(() => fetchOnChange(true, false, false))
        }
      })

      watch(page, () => {
        goToPage.value = page.value
        debounce(() => fetchOnChange())
      })

      watch(filteringSelected, (n, o) => {
        if (JSON.stringify(n) !== JSON.stringify(o)) {
          clearFacetsOnSearch.value = true
          ga4filterEvent(o, n)
          debounce(() => fetchOnChange(true))
        }
      })

      watch(perPage, () => {
        debounce(() => fetchOnChange(true))
      })

      watch(() => props.searchText, (n, o) => {
        if (n !== o) {
          clearFilteringSelection()
          debounce(() => fetchOnChange(true, true))
        }
      })

      // Apply filters when CTA/router navigates with ?facets=...#anchor on the same page.
      // Listing filter updates use history.pushState (not Vue Router), so this only runs for external nav.
      watch(() => route.value?.query, async (newQuery, oldQuery) => {
        if (!initialized.value || !newQuery) {
          return
        }
        const relevantKeys = ['facets', 'product-type', 'model', 'sort', 'page', 'perPage']
        const changed = relevantKeys.some(key => newQuery[key] !== oldQuery?.[key])
        if (!changed) {
          return
        }
        queryParams.value = {...newQuery}
        await initializeBasedOnQueryParams()
      })
    }

    const sortingObject = computed({
      get: () => {
        if (process.server && !sortingSelected.value?.length) {
          sortingSelected.value = sortingOptions.value?.[0]?.value
        }
        return sortingOptions.value?.find(x => x.value === sortingSelected.value) || sortingOptions.value?.[0]
      },
      set: () => {
      }
    })

    const translatedChips = computed({
      get: () => {
        return chips.value.map((chip) => {
          const translatedFaceValue = props.labels?.[chip.facetValue] || chip.facetValue
          return {
            label: chip.facetLabel.replace(/\s/g, '&nbsp;') + ': ' + translatedFaceValue.replace(/\s/g, '&nbsp;'),
            value: chip.value
          }
        })
      },
      set: () => {
      }
    })

    const {fetch} = useFetch(async () => {
      error.value = false

      try {
        if (facetCategory) {
          const {data} = await $services.bobcatapi.getFacets(facetCategory)
          primaryFacetsToHide.value = data?.filter(x => x?.hideFacet === 'true')
          const extractSortingOptions = (data, additionalFilterCondition = () => true) => {
            return data?.filter(x => x.sorting === 'true' && additionalFilterCondition(x))?.map(x => ({
              label: x.label,
              value: getFilteringOrderValue(x)
            })) || []
          }

          sortingOptions.value = extractSortingOptions(data)
          sortingOptionsWithHiddenFilter.value = extractSortingOptions(data, x => x?.hideFacet !== 'true')
        }
      } catch (e) {
        error.value = `[Listing: ${group}] Unable to get configuration`
        // eslint-disable-next-line no-console
        console.error(error.value, e)
        $ga4?.events?.error_general({
          error_details: error.value
        }, 'data error')
      }

      try {
        const categories = [props.group?.productCategory]
        productCategory.value = (await $services.bobcatapi.getCategories(categories.filter(x => x)))?.[0]
      } catch (e) {
        // eslint-disable-next-line no-console
        console.error(`[Listing] unable to fetch productCategory ${props.group?.productCategory}`)
        $ga4?.events?.error_general({
          error_details: `[Listing] unable to fetch productCategory ${props.group?.productCategory}`
        }, 'data error')
      }
      const categoryPath = productCategory.value?.['@path']?.split('/') || []
      const isSubcategory = productCategory.value?.['@nodeType'] === 'dxp:subcategory'
      category.value = categoryPath[categoryPath?.length - (isSubcategory ? 2 : 1)] || ''
      subcategory.value = isSubcategory && productCategory.value.title

      if (!facetCategory) {
        error.value = '[Listing] Missing facet category'
        $ga4?.events?.error_general({
          error_details: error.value
        }, 'data error')
        return
      }

      if (props.searchText === null && group === 'product' && !productCategory.value) {
        error.value = '[Listing] Missing product category'
        $ga4?.events?.error_general({
          error_details: error.value
        }, 'data error')
        return
      }

      initializeBasedOnQueryParams()
      await fetchOnChange(false, false, initialized.value)
    })

    props.facet?.['@nodes']?.forEach(node => facetsNodes.push(props.facet[node]))

    onServerPrefetch(async () => {
      await fetch()
    })

    onMounted(async () => {
      if (!initialized.value) {
        await fetch()
      } else {
        emit('resultListUpdate', {
          total: total.value,
          results: results.value,
          currentFilterFacets: currentFilterFacets.value,
          currentSorting: sortingSelected.value
        })
      }
    })

    return {
      currentFilterFacets,
      filteringSelected,
      translatedChips,
      removeFilteringSelection,
      clearFilteringSelection,
      sortingOptions,
      sortingSelected,
      sortingObject,
      changeSorting,
      error,
      facetsToDisplay,
      loading,
      initialized,
      total,
      results,
      page,
      perPage,
      perPageObject,
      perPageOptions,
      productCategory,
      changePerPage,
      linkGenFunction,
      maxPageNumber,
      currency,
      category,
      subcategory,
      facetCategory,
      goToPage,
      updatePageNumber,
      generateConceptHint,
      initialFilterFacets,
      filtersEmpty,
      primaryFacetsToHide,
      sortingOptionsWithHiddenFilter,
      fetchListingResults,
      searchByModel,
      reset,
      productModels,
      clearFilters,
      selectedType,
      selectedModels,
      productTypes,
      handleProductTypeChangeAndAddToGA4,
      handleModelChangeAndAddToGA4,
      removeType: removeProductType,
      removeModel: removeProductModel,
      multiSelectRef,
      search,
      defaultProductType,
      productTypeLabel,
      modelFieldLabel,
      listingLabels,
      isLoadingFacets,
      showMoreVariantName,
      showMoreVariantColor,
      BUTTON_NAMES,
      BUTTON_STYLES
    }
  },
  head() {
    const headObj = {}
    const fullUrl = `${this.$config.PUBLIC_HOST}${this?.$route.path}`

    if (this?.useShowMore && (parseInt(this?.page) !== 1 || this?.$route?.query?.perPage)) {
      headObj.link = [
        {rel: 'canonical', href: `${fullUrl}?perPage=${this.page * this.perPage}`, hid: 'canonical'}
      ]
    } else if (parseInt(this?.page) !== 1) {
      headObj.link = [
        {rel: 'canonical', href: `${fullUrl}?page=${this.page}`, hid: 'canonical'}
      ]
    }
    return headObj
  }
}
</script>

<style lang="scss">
@import '~/assets/sass/pagination';
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~bootstrap/scss/mixins';

ul.pagination li.page-item .page-link {
  border: 1px solid $gray-600;
}

[data-test-id*='-result-item__'] {
  padding-left: 0.5rem !important;
  padding-right: 0.5rem !important;
}
</style>
