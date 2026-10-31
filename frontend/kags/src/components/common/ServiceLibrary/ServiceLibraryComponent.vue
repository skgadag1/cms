<template>
  <div>
    <h2 class="mb-4 text-center text-sm-start" v-html="translatedConfig('welcome')" />
    <b-row>
      <b-col cols="3" class="d-none d-md-block pe-0">
        <div class="bg-white">
          <overlay-wrapper :show="loading && !disabled">
            <ServiceLibraryFilters v-if="Object.keys(aggregations).length !== 0"
                                   :aggregations="aggregations"
                                   :facet-filter.sync="facetFilter"
                                   @clear="handleClear"
                                   @clearAll="handleClearAll"
            />

            <div v-else class="py-5" />
          </overlay-wrapper>
        </div>
      </b-col>
      <b-col cols="12" md="9" class="p-0 px-sm-3 pb-3">
        <BasicSearch ajax
                     class="pb-2 py-xl-3"
                     mobile-light
                     :action="''"
                     :disabled="loading || disabled"
                     :initial-keyword="query"
                     :message="translatedConfig('searchLabel')"
                     :placeholder="translatedConfig('searchInputPlaceholder')"
                     @search="saveKeyword"
        />

        <overlay-wrapper class="d-block d-md-none" :show="loading && !disabled">
          <ServiceLibraryFiltersMobile :disabled="disabled"
                                       :aggregations="aggregations"
                                       :facet-filter.sync="facetFilter"
                                       @clear="handleClear"
                                       @clearAll="handleClearAll"
          >
            <b-form-select v-model="sortingPair" class="select-center my-2" :placeholder="'Test'" :options="sortingOptions" @update:model-value="(v) => applySorting(v[1], v[0])">
              <template v-slot:first>
                <b-form-select-option :value="[]" disabled>{{ translate('sortByLabel', 'CommonFunctional') }}</b-form-select-option>
              </template>
            </b-form-select>
          </ServiceLibraryFiltersMobile>
        </overlay-wrapper>

        <template v-if="!error && !disabled">
          <overlay-wrapper :show="loading && !disabled">
            <p class="mt-3 ms-3 ms-sm-0">
              <span v-if="total && gteLabel" key="greater-than">
                {{ replace(translate('moreThanResultsLabel', 'config'), '[total]', total) }}
              </span>
              <span v-else-if="total" key="total">
                {{ total }} {{ translate('resultsLabel', 'config') }}
              </span>
              <span v-else key="empty" class="text-gray-100">_</span>
            </p>

            <ServiceLibraryTable :items="items"
                                 :sorting-dir="sortingPair[0]"
                                 :sorting-key="sortingPair[1]"
                                 @sorting="applySorting"
            />

            <div class="d-flex flex-wrap justify-content-between align-items-center mt-3">
              <div class="col-6 col-sm-auto order-1 order-sm-0 ps-sm-0 d-none d-lg-block">
                {{ translate('showLabel', 'CommonFunctional') }}:

                <b-dropdown variant="link" wrapper-class="btn-group" toggle-class="text-decoration-none text-dark font-base page-dropdown">
                  <template v-slot:button-content>
                    <span>{{ perPage }}</span>
                  </template>
                  <b-dropdown-item @click.prevent="perPage = 10">10</b-dropdown-item>
                  <b-dropdown-item @click.prevent="perPage = 25">25</b-dropdown-item>
                  <b-dropdown-item @click.prevent="perPage = 100">100</b-dropdown-item>
                </b-dropdown>
              </div>

              <b-pagination v-model="currentPage"
                            align="center"
                            class="col-12 col-sm-auto order-0 order-sm-1 mb-2 mb-sm-0 pe-0 mx-auto"
                            first-number
                            last-number
                            no-goto-end-buttons
                            :total-rows="Math.max(total, 1)"
                            :per-page="perPage"
              >
                <template v-slot:prev-text>
                  <b-icon :icon="['far', 'chevron-left']" />
                </template>

                <template v-slot:next-text>
                  <b-icon :icon="['far', 'chevron-right']" />
                </template>

                <template v-slot:ellipsis-text>
                  <div class="d-flex align-items-center justify-content-center fw-bold text-center">
                    …
                  </div>
                </template>
              </b-pagination>

              <div class="col-6 col-sm-auto order-2 align-items-center justify-content-end pe-sm-0 d-none d-lg-flex">
                <div class="flex-grow-0 me-2">{{ translate('gotoPageLabel', 'CommonFunctional') }}:</div>
                <input v-model="inputPage" :disabled="total <= perPage" class="form-control flex-grow-0 page-input" @change="handlePageInput" @keydown.enter="handlePageInput">
              </div>
            </div>
          </overlay-wrapper>
        </template>

        <b-row v-else-if="error">
          <b-col cols="12">
            <div key="searching-error" class="box">{{ translate('searchingError', 'CommonErrors') }}</div>
          </b-col>
        </b-row>

        <ListEmptyWidget v-else class="mt-3" />
      </b-col>
    </b-row>
  </div>
</template>

<script>
import { useFormatters } from '@/composables/useFormatters'
import { useLabels } from '@/composables/useLabels'
import ServiceLibraryFilters from '@/components/common/ServiceLibrary/ServiceLibraryFilters'
import ServiceLibraryFiltersMobile from '@/components/common/ServiceLibrary/ServiceLibraryFiltersMobile'
import BasicSearch from '@/components/common/BasicSearch'
import ListEmptyWidget from '@/components/common/ListEmptyWidget'
import ServiceLibraryTable from '@/components/common/ServiceLibrary/ServiceLibraryTable'
import {bdService} from '../../../services'
import qs from 'qs'

export default {
  conceptName: ['CommonErrors', 'CommonFunctional'],
  setup() { return { ...useLabels(), ...useFormatters() } },
  components: {
    ServiceLibraryTable,
    BasicSearch,
    ServiceLibraryFilters,
    ServiceLibraryFiltersMobile,
    ListEmptyWidget
  },
  props: {
    disabled: {
      required: false,
      default: false
    }
  },
  data() {
    const _f = this.$root.config && this.$root.config.defaultEnglishSelected === 'true' ? {'locale_filter': ['enUS', 'enGB']} : {}

    return {
      loading: true,
      total: null,
      gteLabel: null,
      items: [],
      aggregations: {
        'manual_filter': {},
        'bulletin_filter': {},
        'instruction_filter': {},
        'locale_filter': {},
        'product_filter': {}
      },
      facetFilter: _f,
      facetQuery: _f,
      currentPage: 1,
      inputPage: 1,
      perPage: 25,
      query: '',
      sortingPair: [],
      error: false
    }
  },
  computed: {
    sortingOptions() {
      return [
        {value: ['+', 'title'], text: this.translatedConfig('titleHeader') + ' ' + this.translate('ascendingLabel', 'CommonFunctional')},
        {value: ['-', 'title'], text: this.translatedConfig('titleHeader') + ' ' + this.translate('descendingLabel', 'CommonFunctional')},
        {value: ['+', 'description'], text: this.translatedConfig('descriptionHeader') + ' ' + this.translate('ascendingLabel', 'CommonFunctional')},
        {value: ['-', 'description'], text: this.translatedConfig('descriptionHeader') + ' ' + this.translate('descendingLabel', 'CommonFunctional')},
        {value: ['+', 'category'], text: this.translatedConfig('categoryHeader') + ' ' + this.translate('ascendingLabel', 'CommonFunctional')},
        {value: ['-', 'category'], text: this.translatedConfig('categoryHeader') + ' ' + this.translate('descendingLabel', 'CommonFunctional')}
      ]
    }
  },
  watch: {
    currentPage(v) {
      this.inputPage = v
      this.fetchDocuments()
    },
    perPage() {
      this.currentPage = 1
      this.fetchDocuments()
    },
    facetFilter: {
      deep: true,
      handler(v) {
        let _filter = {}
        for (let name in v) {
          if (v[name].length > 0) _filter[name] = v[name].join(',')
        }
        if (JSON.stringify(_filter) !== JSON.stringify(this.facetQuery)) {
          this.facetQuery = _filter
          this.currentPage = 1
          this.fetchDocuments()
        }
      }
    }
  },
  async created() {
    await this.fetchDocuments(true)
    let items = {}
    if (location.href.indexOf('?') > 0) items = qs.parse(location.search.substr(1))
    this.query = items.keyword || ''
    this.fetchDocuments()
  },
  methods: {
    saveKeyword(k) {
      this.query = k
      this.currentPage = 1
      this.fetchDocuments()
    },
    handlePageInput(v) {
      let _v = parseInt(v.target.value)
      const _maxPage = Math.ceil(this.total / this.perPage)
      if (_v < 1) {
        _v = 1
      } else if (_v > _maxPage) {
        _v = _maxPage
      }
      this.inputPage = _v
      this.currentPage = _v
    },
    handleClearAll() {
      this.clearSorting()
      this.currentPage = 1
      for (let name in this.facetFilter) this.facetFilter[name] = []
    },
    handleClear(a) {
      this.currentPage = 1
      this.facetFilter[a] = []
    },
    clearSorting() {
      this.sortingPair = []
    },
    applySorting(sorter, direction = '-') {
      if (sorter) this.sortingPair = [direction, sorter]
      else this.sortingPair = []
      this.fetchDocuments()
    },
    async fetchDocuments(onlyFacets = false) {
      if (!this.disabled) {
        this.loading = true
        this.gteLabel = null
        await bdService.fetchDocuments({
          query: this.query,
          limit: this.perPage,
          offset: (this.currentPage - 1) * this.perPage,
          sort: this.sortingPair.join(''),
          ...this.facetQuery
        }).then(data => {
          this.total = data.total
          this.gteLabel = (data.relation && data.relation === 'gte')
          this.items = data.items
          // zero all counters
          for (let name in this.aggregations) {
            for (let k in this.aggregations[name]) this.aggregations[name][k] = 0
            this.aggregations[name]._counter = 0
          }
          // update counters and filters
          for (let name in data.aggr) {
            if (name === 'language_filter') continue
            this.aggregations[name] = Object.assign(this.aggregations[name] || {}, {'_counter': onlyFacets ? 0 : data.aggr[name].doc_count})
            for (let a of data.aggr[name].buckets) {
              if (!(name in this.facetFilter)) this.facetFilter[name] = []
              if (name in this.aggregations) this.aggregations[name] = Object.assign(this.aggregations[name] || {}, {[a.key]: onlyFacets ? 0 : a.doc_count})
              let ordered = {}
              Object.keys(this.aggregations.product_filter).sort().forEach(key => ordered[key] = this.aggregations.product_filter[key])
              this.aggregations.product_filter = ordered
              //order locale_filter by selected
              if (this.facetFilter['locale_filter']) {
                ordered = {}
                const sel_keys = [...this.facetFilter['locale_filter']]
                const other_keys = Object.keys(this.aggregations.locale_filter).filter(key => !sel_keys.includes(key))
                sel_keys.forEach(key => ordered[key] = this.aggregations.locale_filter[key])
                other_keys.forEach(key => ordered[key] = this.aggregations.locale_filter[key])
                this.aggregations.locale_filter = ordered
              }
            }
          }
          if (!onlyFacets) this.facetFilter = {...this.facetFilter}
        }).catch(r => {
          this.error = r
        }).finally(() => this.loading = false)
      }
    }
  }
}
</script>

<style lang="scss">
@import '../../../scss/_z-index.scss';

.page-input {
  width: 3.2rem;
}

.dropdown-menu {
  z-index: $zindex-fixed + 1;
}
</style>
