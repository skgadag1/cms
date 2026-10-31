<template>
  <div data-test-id="text-listing">
    <section class="clearfix bg-gray-lighter">
      <div class="container py-5" data-test-id="text-listing-search-area">
        <h2 :total-search-count="total" :title="generateConceptHint('searchResultsFor')">
          {{
            searchTextValue.length ? `${labels.searchResultsFor || 'Search Results for'} "${searchTextValue}" ${totalText}` : labels.searchResults || 'Search Results'
          }}
        </h2>
        <b-row>
          <b-col cols="12" md>
            <b-form @submit.prevent="updateSearchText">
              <b-form-group data-test-id="text-listing-input" class="mb-0">
                <b-input-group class="position-relative">
                  <b-form-input
                    ref="searchTextInput"
                    v-model="searchTextModel"
                    data-test-id="text-listing__input"
                    trim
                    class="form-control px-6 py-4 bg-white border border-color-gray-600"
                  />
                  <span class="position-absolute input-icon d-flex align-items-center justify-content-center z-index-3">
                    <b-icon icon="search" class="text-black" />
                  </span>
                  <span v-if="searchTextModel.length"
                        class="text-listing__clear cursor-pointer input-icon position-absolute d-flex align-items-end z-index-3"
                        data-test-id="text-listing__clear"
                        @click="clearSearchText"
                  >
                    <b-icon icon="close" class="text-black" />
                  </span>
                </b-input-group>
              </b-form-group>
            </b-form>
          </b-col>
          <b-col cols="12" md="auto" class="pl-md-0 mt-2 mt-md-0">
            <b-button block
                      variant="primary"
                      size="lg"
                      class="h-100"
                      data-test-id="text-listing__search"
                      :title="generateConceptHint('search')"
                      @click="updateSearchText"
            >
              {{ labels.search || 'Search' }}
            </b-button>
          </b-col>
        </b-row>
      </div>
    </section>
    <section class="clearfix" :class="{'d-none': searchTextValue === ''}">
      <ListingWithFiltering :group="group"
                            :labels="labels"
                            :hide-filters="hideFiltersOpt"
                            :search-text="searchTextValue"
                            :hide-sorting="hideSortingOpt"
                            :use-show-more="useShowMoreOpt"
                            :expandable="expandable"
                            :show-all-pages="showAllPages"
                            hide-number-of-results
                            :facet="facet"
                            @resultListUpdate="updateResults"
      >
        <template #results>
          <div v-if="loaded || $isBot" class="row" data-test-id="text-listing__results">
            <template v-if="results.length">
              <div v-for="(itm, i) in results"
                   :key="`text-listing-result-${i}-${itm['publishDate']}`"
                   class="col-12 mb-3"
                   :class="{'pl-md-0' : !hideFiltersOpt}"
                   :data-test-id="`text-listing-result-item__${i}-${itm.publishDate}`"
                   @click="$ga4.events.search_result_selected($event, searchTextValue, itm)"
              >
                <TextListingItem
                  :horizontal="false"
                  :item="itm"
                  :labels="labels"
                  :show-chips="false"
                  :show-indicator="true"
                />
              </div>
            </template>
            <WarningAlert v-else-if="displayNoResultsInfo"
                          type="info"
                          class="mx-2"
                          :title="generateConceptHint('noResults')"
            >
              {{ labels.noResults || 'No results' }}
            </WarningAlert>
          </div>
          <ListingSkeleton v-else :cols="1" :items="5" img-placement="left" />
        </template>

        <template #facets-loading>
          <ListingSkeleton v-if="!loaded" :cols="1" :items="1" img-placement="none" />
        </template>
      </ListingWithFiltering>
    </section>
  </div>
</template>

<script>
import {computed, ref, useContext, useFetch, useRoute} from '@nuxtjs/composition-api'
import useConcepts from '~/helpers/composables/useConcepts'
import useMounted from '~/helpers/composables/useMounted'

export default {
  props: {
    nodeId: {
      type: String
    },
    hideFilters: {
      type: String,
      default: 'false'
    },
    hideSorting: {
      type: String,
      default: 'false'
    },
    useShowMore: {
      type: String,
      default: 'false'
    },
    useCollapse: {
      type: String,
      default: 'true'
    },
    ignoreExcludedPages: {
      type: String,
      default: 'false'
    },
    facet: Object
  },
  setup(props) {
    const group = {
      field: 'page'
    }
    const results = ref([])
    const route = useRoute()
    const loaded = ref(false)
    useMounted(props.nodeId)

    const searchTextModel = ref(route?.value?.query?.q || '')
    const searchTextValue = ref(route?.value?.query?.q || '')
    const total = ref(null)
    const searchTextInput = ref(null)

    const hideFiltersOpt = props.hideFilters === 'true'
    const useShowMoreOpt = props.useShowMore === 'true'
    const hideSortingOpt = props.hideSorting === 'true'
    const expandable = props.useCollapse === 'true'
    const showAllPages = props.ignoreExcludedPages === 'true'

    const labels = ref({})
    const {generateConceptHint} = useConcepts()
    const {$services, $ga4} = useContext()

    useFetch(async () => {
      labels.value = await $services.bobcatapi.getConcepts(['/%region%/common/labels/pageTypes'])
    })

    const updateSearchText = () => {
      if (searchTextValue.value === searchTextModel.value) {
        return
      }
      total.value = null
      searchTextValue.value = searchTextModel.value
      if (searchTextInput.value?.$el) {
        searchTextInput.value.$el.setAttribute('value', searchTextValue.value)
      }
    }

    const clearSearchText = () => {
      searchTextModel.value = ''
      updateSearchText()
    }

    const totalText = computed({
      get: () => {
        return total.value !== null ? `(${total.value})` : ''
      },
      set: () => {
      }
    })

    const displayNoResultsInfo = computed({
      get: () => {
        return total.value === 0 && searchTextValue.value !== ''
      },
      set: () => {
      }
    })

    const updateResults = (data) => {
      const filters = []
      const sorting = data?.currentSorting
      data?.currentFilterFacets?.forEach((f) => {
        f?.values?.forEach(v => filters.push(`${(f?.name || f?.id || f?.filterField)}:${(v?.name || v?.id)}`))
      })
      total.value = data?.total
      data = data?.results || []
      results.value = data.map((x) => {
        return {
          ...x,
          title: x.highlights ? x.highlights.title : x.title,
          content: x.highlights?.content ? x.highlights.content : false,
          breadcrumbs: x.breadcrumbs?.join(' / '),
          pageType: labels.value?.[x.pageType] || x.pageType
        }
      })
      loaded.value = true
      setTimeout(() => {
        $ga4?.events?.view_search_results(event, searchTextValue.value, total.value, 'site', 'text-listing-search-area')
        $ga4?.events?.search(event, searchTextValue.value, 'site', 'text-listing-search-area', sorting, filters?.join())
      }, 300)
    }

    return {
      group,
      searchTextModel,
      searchTextValue,
      loaded,
      results,
      hideFiltersOpt,
      useShowMoreOpt,
      hideSortingOpt,
      updateResults,
      updateSearchText,
      clearSearchText,
      labels,
      totalText,
      total,
      generateConceptHint,
      displayNoResultsInfo,
      searchTextInput,
      expandable,
      showAllPages
    }
  }
}
</script>

<style lang="scss" scoped>
.text-listing__clear {
  left: auto;
  right: 1rem;
}
</style>
