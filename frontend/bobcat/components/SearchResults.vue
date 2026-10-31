<template>
  <b-overlay :show="isFetching">
    <section id="searchResults" data-test-id="dinv-search-results" class="bg-gray-lighter py-4">
      <div class="container">
        <div class="row mt-lg-4">
          <div class="col-12 mb-3">
            <div class="row justify-content-between align-items-center">
              <div class="col-12 col-md">
                <p data-test-id="dinv-search-results-text-desktop" class="d-none d-md-block mb-0">{{ total }} Results in radius of {{ distance }} {{ metric }} near {{ zipCode }}</p>
              </div>
              <div class="col-12 col-md-auto">
                <LazyHydrate when-visible>
                  <SwitchDropdown v-if="sortingDropdownOptions.length"
                                  :label="labels.sortBy || 'Sort by'"
                                  :title="generateConceptHint('sortBy')"
                                  class="d-flex align-items-baseline flex"
                                  data-test-id="dinv-search-sorting"
                                  :disabled="!products.length"
                                  :options="sortingDropdownOptions"
                                  :config="{right: true}"
                                  icon="sort"
                                  fuse-label-and-options-for-mobile
                                  @changed="changeSorting"
                  />
                </LazyHydrate>
              </div>
            </div>
          </div>

          <div class="col-12">
            <b-badge
              v-if="selectedType && selectedType.label"
              data-test-id="dinv-search-results-selected-type"
              pill
              variant="dark"
              class="mr-2 mb-3"
              :class="{'cursor-pointer': !isFetching}"
              @click.prevent="removeSelectedType"
            >
              <span class="d-flex align-items-center">
                Product Type: {{ selectedType.label }}
                <b-icon icon="remove" class="close-icon ml-2" />
              </span>
            </b-badge>

            <b-badge v-for="(chip, index) in selectedModels"
                     :key="`item-chips__${chip.value}_${index}`"
                     data-test-id="dinv-search-results-selected-model"
                     pill
                     variant="dark"
                     :class="{'cursor-pointer': !isFetching}"
                     class="mr-2 mb-3"
                     @click.prevent="removeFromList(selectedModels, chip, removeModel)"
            >
              <span class="d-flex align-items-center">
                Model: {{ chip.label }}
                <b-icon icon="remove" class="close-icon ml-2" />
              </span>
            </b-badge>
          </div>

          <div class="col-12">
            <div class="my-2">
              <FacetsChips class="d-none d-md-block" :chips="chips" @removeFilteringSelection="removeFilteringSelection" />
            </div>
          </div>
          <div class="col-12">
            <div class="row">
              <div v-if="facetsToDisplay.length" class="col-12 col-md-3 mb-2">
                <FacetsFilter
                  v-model="filteringSelected"
                  :facets-to-display="facetsToDisplay"
                  :labels="labels"
                  :chips="chips"
                  expandable
                  data-test-id="listing-facets"
                  @input="$emit('clearCompare')"
                  @removeFilteringSelection="removeFilteringSelection"
                  @clearFilteringSelection="clearFilteringSelection"
                  @clearFilteringCategory="(_cat) => {$emit('clearCompare'); clearFilteringSelection(_cat)}"
                />
              </div>

              <div class="col-12 d-block d-md-none">
                <p data-test-id="dinv-search-results-text-mobile">{{ total }} Results near {{ zipCode }}</p>
              </div>

              <div v-if="productsToDisplay.length" :class="`col-md-${hideFilters ? '12' : '9'}`" class="col-12 mb-2">
                <div class="row">
                  <div v-for="(product, i) in productsToDisplay" :key="`${product.dealer.arNumber}-${product.serialNumber}__${i}`" class="col-12 col-md-4 mb-4" :class="{'col-lg-3' : hideFilters}">
                    <LazyHydrate never>
                      <SearchProduct :labels="labels"
                                     :cta-settings="ctaSettings"
                                     :product="product"
                                     :category="selectedType"
                                     :location="zipCode"
                      />
                    </LazyHydrate>
                  </div>
                </div>
              </div>

              <div v-else :class="`col-md-${hideFilters ? '12' : '9'}`" class="col-12 order-3 d-flex justify-content-center align-items-center">
                <WarningAlert type="info" class="col-12 col-md-6" data-test-id="dinv-search-noresults">
                  <div v-html="noResultsMsg" />
                </warningalert>
              </div>

              <div v-if="productsToDisplay.length" class="col-12 d-flex justify-content-center listing-pagination">
                <div class="d-flex align-content-center align-content-md-around justify-content-center justify-content-md-around  flex-column flex-md-row">
                  <SwitchDropdown v-model="perPageObject"
                                  class="d-flex align-items-baseline flex ml-0 ml-md-2 mt-2 mt-md-0 mb-2 mb-md-0"
                                  data-test-id="dinv-search-pagination-perPage"
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
                               data-test-id="dinv-search-pagination"
                               :total-rows="total"
                               :per-page="perPage"
                               :disabled="total && total < perPage"
                               @change="(currentPage) => { goToPage = currentPage; $emit('pageUpdated', currentPage)}"
                  >
                    <template #prev-text><span @click.prevent.stop>&#8249;</span></template>
                    <template #page="paginationCfg"><span @click.prevent.stop>{{ paginationCfg.page }}</span></template>
                    <template #next-text><span @click.prevent.stop>&#8250;</span></template>
                  </BPagination>

                  <b-form class="d-flex align-items-center justify-content-center" @submit.prevent="updatePageNumber">
                    <label for="goToPage" class="m-0" :title="generateConceptHint('goToPage')">{{ labels.goToPage || 'Go to page' }}</label>
                    <b-form-input id="goToPage"
                                  v-model="goToPage"
                                  :disabled="total && total < perPage"
                                  class="listing-pagination-goToPage mx-2 py-2 text-center rounded-0"
                                  data-test-id="dinv-search-pagination-goToPage"
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
        </div>
      </div>
    </section>
  </b-overlay>
</template>

<script>
import {computed, ref, watch} from '@nuxtjs/composition-api'
import {BPagination} from 'bootstrap-vue'
import SearchProduct from './elements/Dealer/SearchProduct.vue'
import useConcepts from '~/helpers/composables/useConcepts'
import useInventorySorting from '~/helpers/composables/useInventorySorting'
import useFilteringElements from '~/helpers/composables/useFilteringElements'
import FACET_TYPES from '@/constants/facets'
import FacetsFilter from '~/components/elements/FacetsFilter'

export default {
  components: {
    SearchProduct,
    BPagination,
    FacetsFilter
  },
  props: {
    results: {
      type: Array,
      required: true
    },
    selectedModels: {
      type: Array
    },
    facets: {
      type: Array
    },
    filters: {
      type: Array
    },
    noResultsMsg: {
      type: String
    },
    selectedType: {
      type: Object
    },
    removeModel: {
      type: Function,
      required: true
    },
    removeType: {
      type: Function,
      required: true
    },
    ctaSettings: {
      type: Object
    },
    isFetching: {type: Boolean},
    distance: {type: Number},
    metric: {type: String},
    zipCode: {type: String},
    labels: {
      type: Object,
      default: () => ({})
    },
    sorting: {
      type: Array,
      default: null
    },
    initialValues: {
      type: Object,
      default: () => {
        return {}
      }
    }
  },
  setup(props, {emit}) {
    const {removeType, sorting, initialValues, filters} = props
    const {generateConceptHint} = useConcepts()
    const {sortProducts} = useInventorySorting()
    const products = ref([])
    const productsSorted = ref([])
    const facetsNodes = []
    const currentPage = ref(initialValues?.page || 1)
    const goToPage = ref(currentPage.value)
    const total = ref(null)
    const facetsToDisplay = ref([])
    const hideFilters = ref(true)

    const {
      chips,
      filteringSelected,
      currentFilterFacets,
      perPage,
      perPageObject,
      perPageOptions,
      changePerPage,
      removeFilteringSelection,
      clearFilteringSelection,
      initialFilterFacets
    } = useFilteringElements([], filters)

    const maxPageNumber = computed({set: () => {}, get: () => Math.ceil(total.value / perPage.value)})

    props.facets?.['@nodes']?.forEach(node => facetsNodes.push(props.facets?.[node]))

    const updatePageNumber = () => {
      if (goToPage.value < 1) { goToPage.value = 1 } else if (goToPage.value > maxPageNumber.value) { goToPage.value = maxPageNumber.value }
      if (goToPage.value === currentPage.value) { return }
      currentPage.value = goToPage.value
      emit('pageUpdated', currentPage.value)
    }

    const sortingDropdownOptions = computed({
      get: () => sorting?.map((x) => {
        return {
          label: x.label, value: x.id, selected: x.selected
        }
      }) || [],
      set: () => {}
    })
    const sortingObject = ref(sortingDropdownOptions.value.find(s => s.selected) || sortingDropdownOptions.value[0])

    const productsToDisplay = computed({
      get: () => productsSorted.value.length ? productsSorted.value.slice((currentPage.value - 1) * perPage.value, Math.min(currentPage.value * perPage.value, total.value)) : [],
      set: () => {}
    })

    watch(() => props.results, (newVal) => {
      if (Array.isArray(newVal)) {
        products.value = newVal
        productsSorted.value = [...newVal]
        sortProducts(productsSorted.value, sortingObject.value?.value)
        total.value = newVal.length
        currentPage.value = 1
      }
    })

    watch(() => props.filters, (newVal) => {
      if (Array.isArray(newVal) && newVal.length === 0) {
        filteringSelected.value = []
      } else if (Array.isArray(newVal)) {
        filteringSelected.value = newVal
      }
    })

    watch(() => props.facets, (newVal) => {
      currentFilterFacets.value = newVal || []
      if (currentFilterFacets.value?.length > 0) {
        // dealer inventory does not have dynamic filters, so initial = current
        initialFilterFacets.value = currentFilterFacets.value
      }
      hideFilters.value = !(newVal?.length > 0)

      facetsToDisplay.value = newVal?.map((facet) => {
        facet.label = facet.id
        facet.facetType = facet.facetType || FACET_TYPES.DEFAULT
        facet.values = facet.values?.map((facetCategory) => {
          const matchingCurrentFilter = currentFilterFacets.value.find(x => x.id === facet.id)
          facetCategory.count = matchingCurrentFilter?.values?.filter(y => y.id === facetCategory.id)?.[0]?.count || 0
          return facetCategory
        })
        facet.switchOffSortAlphanumeric = facetsNodes.find(x => x.label === facet.label && x.id === facet.filterField)?.switchOffSortAlphanumeric === 'true'
        return facet
      }) || []
    })

    const removeSelectedType = () => {
      if (!props.isFetching && removeType) { removeType({}) }
    }

    const removeFromList = (list, item, callback) => {
      if (!props.isFetching) { callback(list.filter(m => m.label !== item.label), item) }
    }

    const changeSorting = (val) => {
      sortingObject.value = val
      emit('sortingUpdated', sortingObject.value)
      sortProducts(productsSorted.value, sortingObject.value?.value)
    }

    const handlePerPageChange = (s) => {
      changePerPage(s)
      currentPage.value = 1
      goToPage.value = 1
      emit('pageUpdated', currentPage.value, s.value)
    }

    watch(() => props.initialValues, (val) => {
      if (Object.keys(val).length && props.results?.length) {
        if (val.perPage) { changePerPage({label: val.perPage, value: parseInt(val.perPage)}) }
        if (val.page && currentPage.value !== parseInt(val.page)) {
          currentPage.value = Math.min(Math.ceil(props.results?.length / (val.perPage || perPage.value)), parseInt(val.page)) || 1
          emit('pageUpdated', currentPage.value)
        }
      }
    })

    watch(filteringSelected, (n, o) => {
      if (JSON.stringify(n) !== JSON.stringify(o)) {
        emit('filterResults', n)
      }
    })

    return {
      currentPage,
      goToPage,
      perPage,
      perPageObject,
      perPageOptions,
      handlePerPageChange,
      total,
      removeFromList,
      products,
      productsSorted,
      productsToDisplay,
      removeSelectedType,
      sortingDropdownOptions,
      changeSorting,
      sortingObject,
      generateConceptHint,
      updatePageNumber,
      chips,
      filteringSelected,
      facetsToDisplay,
      removeFilteringSelection,
      clearFilteringSelection,
      hideFilters,
      initialFilterFacets
    }
  }
}
</script>

<style lang="scss">
@import '~/assets/sass/pagination';

div[data-test-id="dinv-search-noresults"] p:last-child {
  margin-bottom: 0;
}
</style>
