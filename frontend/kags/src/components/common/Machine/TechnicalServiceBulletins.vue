<template>
  <section class="mb-4">
    <h2 class="mb-3">
      {{ capitalize(translate('title', 'Bulletins')) }}
    </h2>

    <ErrorInput
      class="border rounded mb-3"
      ajax
      submit-on-close
      :disabled="loading || items.all.length === 0 || error.present"
      :keep-button="false"
      :empty-label="translate('emptySearchButton', 'CommonFunctional')"
      :search-label="translate('searchButton', 'CommonFunctional')"
      :placeholder="translate('inputPlaceholder', 'Bulletins')"
      @submit="_k => { applyKeyword(_k) }"
    />

    <div class="d-md-flex align-items-center mb-3">
      <div class="me-3">{{ translate('filterBy', 'Bulletins') }}:</div>

      <BasicFilterCheckboxes v-if="filters.bulletin_type"
                             :title="translate('typeLabel', 'CommonFunctional')"
                             :options="filters.bulletin_type"
      />

      <BasicFilterCheckboxes v-if="filters.status"
                             :title="translate('statusLabel', 'CommonFunctional')"
                             :options="filters.status"
      />

      <BasicFilterDate v-if="dateFilter"
                       :title="translate('expirationDateLabel', 'CommonFunctional')"
                       :options="dateFilter"
                       :allow-future-dates="true"
      />
      <b-col cols="auto" class="ps-md-0 d-none d-md-flex">
        <b-link class="font-condensed"
                @click.prevent="clearFilters"
        >
          {{ translate('clearAll', 'CommonFunctional') }}
        </b-link>
      </b-col>
    </div>

    <overlay-wrapper :show="loading">
      <b-error v-if="error.present" :code="error.code" data-test-id="service-error">
        {{ translate(error.text.ck, error.text.cp) }}
      </b-error>

      <template v-else>
        <b-row>
          <b-col cols="12" md="2" class="d-flex mb-2 align-items-center">
            <div class="d-inline-block text-start">
              <span>{{ loading ? '__' : items.filtered.length }} {{ translate("resultsLabel", 'CommonFunctional') }}</span>
            </div>
          </b-col>
        </b-row>

        <BasicGridTable :disabled="loading" header-class="d-none d-md-grid" grid-name="bulletins" :class="{'no-status':!showStatus}">
          <template #header>
            <div class="d-flex align-items-center header__bulletins">
              <span>{{ translate("typeLabel", 'CommonFunctional') }}</span>
            </div>
            <div v-if="showStatus" class="d-flex align-items-center header__bulletins">
              <span>{{ translate("statusLabel", 'CommonFunctional') }}</span>
            </div>
            <div class="d-flex align-items-center header__bulletins">
              <span>{{ translate("descriptionLabel", 'CommonFunctional') }}</span>
            </div>
            <div class="d-flex align-items-center header__bulletins">
              <span>{{ translate("publishDateLabel", 'CommonFunctional') }}</span>
            </div>
            <div class="d-flex align-items-center header__bulletins">
              <span>{{ translate("languageLabel", 'CommonFunctional') }}</span>
            </div>
            <div v-if="showStatus" class="d-flex align-items-center header__bulletins">
              <span>{{ translate("expirationDateLabel", 'CommonFunctional') }}</span>
            </div>
            <div class="d-flex align-items-center header__bulletins justify-content-center">
              <span>{{ translate("downloadLabel", 'CommonFunctional') }}</span>
            </div>
          </template>
          <template v-if="items.filtered.length" #rows>
            <BulletinTableItem v-for="(item, i) in items.displayed"
                               :key="'bulletin_item' + i"
                               :item="item"
                               :show-status="showStatus"
            />
          </template>
          <template v-else-if="items.all.length && !items.filtered.length" #rows>
            <b-notification>{{ translate("noResultsFiltered", 'Bulletins') }}</b-notification>
          </template>
          <template v-else #rows>
            <b-notification>{{ translate("noDocuments", 'Bulletins') }}</b-notification>
          </template>
        </BasicGridTable>
        <div class="d-none d-md-flex flex-wrap justify-content-end align-items-center mt-3 px-0">
          <div class="col-6 col-sm-auto order-1 order-sm-0 ps-sm-0 d-none d-lg-block">
            {{ translate('showLabel', 'CommonFunctional') }}:

            <b-dropdown variant="link" wrapper-class="btn-group" toggle-class="text-decoration-none font-base text-dark page-dropdown">
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
                        class="col-12 col-sm-auto order-0 order-sm-1 mb-2 mb-sm-0 pe-0"
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
            <input v-model="inputPage" :disabled="total <= perPage" class="flex-grow-0 page-input pagination-control form-control" @change="handlePageInput" @keydown.enter="handlePageInput">
          </div>
        </div>

        <b-button v-if="items.displayed.length < items.filtered.length" variant="primary" class="font-condensed d-md-none w-100 mt-3" @click="perPage += 10">
          {{ translate('loadMore', 'CommonFunctional') }}
        </b-button>
      </template>
    </overlay-wrapper>

    <slot />
  </section>
</template>

<script>
import { useFormatters } from '@/composables/useFormatters'
import { useLabels } from '@/composables/useLabels'
import BulletinTableItem from './BulletinTableItem'
import ErrorInput from '@/components/common/ErrorInput'
import BError from '../BError'
import BNotification from '@/components/common/BNotification'
import BasicGridTable from '@/components/common/BasicGridTable'
import {bdService} from '@/services'
import langs from 'langs'
import BasicFilterCheckboxes from '@/components/common/BasicFilterCheckboxes'
import BasicFilterDate from '@/components/common/BasicFilterDate'

const DATE_ALL = 'all'
const DATE_NEXT_WEEK = (() => { const n = new Date(); n.setHours(0, 0, 0, 0); n.setDate(n.getDate() + 6); return n.toISOString() })()
const DATE_NEXT_MONTH = (() => { const n = new Date(); n.setHours(0, 0, 0, 0); n.setDate(n.getDate() + 30); return n.toISOString() })()

export const STATUS_TABLE = {
  'expired': 'light-grey',
  'active': 'active'
}

export const INITIAL_FILTERS = Object.freeze({
  date: {
    all: DATE_ALL,
    week: DATE_NEXT_WEEK,
    weekLabel: 'nextWeek',
    month: DATE_NEXT_MONTH,
    monthLabel: 'nextMonth',
    custom: [null, null],
    value: DATE_ALL
  }
})

export default {
  conceptName: ['Bulletins', 'CommonFunctional', 'CommonDateTime', 'CommonErrors'],
  setup() { return { ...useLabels(), ...useFormatters() } },
  components: {
    BulletinTableItem,
    BError,
    BNotification,
    BasicGridTable,
    ErrorInput,
    BasicFilterCheckboxes,
    BasicFilterDate
  },
  props: {
    sn: {
      type: String
    }
  },
  data() {
    return {
      opened: false,
      keyword: '',
      items: {
        all: [],
        filtered: [],
        displayed: []
      },
      error: {
        present: false,
        text: null
      },
      apiError: {
        present: false,
        text: null
      },
      total: 0,
      perPage: 10,
      inputPage: 1,
      currentPage: 1,
      loading: true,
      dateFilter: INITIAL_FILTERS.date,
      filters: {
        current: [],
        bulletin_type: [
          {label: 'Quality Alert', value: false},
          {label: 'Field Modification', value: false}
        ],
        status: [
          {label: 'Active', value: false},
          {label: 'COMPLETE', value: false}
        ],
        selectedValues: [],
        original: []
      },
      showInternals: false,
      overlaidGrid: false
    }
  },
  computed: {
    activeFilters(){
      let currentFilters = [...this.filters.current]
      const statusFilters = this.filters.status || []
      const isAllStatusSelected = statusFilters.every(item => currentFilters.includes(item))

      if (isAllStatusSelected) currentFilters = currentFilters.filter(item => !statusFilters.includes(item))

      const typeFilters = this.filters.bulletin_type || []
      const isAllTypeSelected = typeFilters.every(item => currentFilters.includes(item))

      if (isAllTypeSelected) currentFilters = currentFilters.filter(item => !typeFilters.includes(item))

      currentFilters = currentFilters.map(filterItem => ({
        translation: this.translateActiveFilter(filterItem),
        value: filterItem
      }))
      if (this.filterDate) {
        let _o = {label: '', value: this.filterDate.options.value, type: 'date'}
        switch(_o.value) {
        case 'custom':
          if (this.filterDate.options.custom[0] || this.filterDate.options.custom[1]) {
            _o.label += this.filterDate.options.custom[0] ?
              (this.filterDate.options.custom[1] ? new Date(this.filterDate.options.custom[0]).toLocaleDateString() + ' - ' : this.$options.filters.translate('afterLabel', this.labels, 'CommonDateTime') + ' ')
              : this.$options.filters.translate('beforeLabel', this.labels, 'CommonDateTime') + ' '
            _o.label += this.filterDate.options.custom[1] ? new Date(this.filterDate.options.custom[1]).toLocaleDateString() : new Date(this.filterDate.options.custom[0]).toLocaleDateString()
          }
          break
        case INITIAL_FILTERS.date.month:
          _o.label = this.translate('last30days', 'CommonDateTime')
          break
        case INITIAL_FILTERS.date.week:
          _o.label = this.translate('last7days', 'CommonDateTime')
          break
        default:
          break
        }
        if (_o.label.length) currentFilters.push(
          {
            translation: _o.type + ': ' + (_o.label),
            ..._o
          }
        )
      }
      return currentFilters
    },
    filterDate() {
      return {
        title: this.translate('expirationDateLabel', 'CommonFunctional'),
        options: this.dateFilter,
        isDefault: this.dateFilter.value === INITIAL_FILTERS.date.all
      }
    },
    showStatus() {
      return this.items.all.length && !!this.items.all[0].status
    }
  },
  watch: {
    currentPage(p) {
      this.inputPage = p
      this.items.displayed = this.items.filtered.slice((p - 1) * this.perPage, p * this.perPage)
    },
    perPage() {
      this.currentPage = 1
      this.inputPage = 1
      this.items.displayed = this.items.filtered.slice(0, this.perPage)
    },
    sn: function () {
      this.getBulletins()
    },
    getAppReady: function () {
      this.getBulletins()
    },
    'filters.status': {
      handler() {
        this.updateFilters()
      },
      deep: true
    },
    'filters.bulletin_type': {
      handler() {
        this.updateFilters()
      },
      deep: true
    },
    'dateFilter.value': {
      handler() {
        this.updateFilters()
      }
    },
    'dateFilter.custom': {
      handler() {
        this.updateFilters()
      },
      deep: true
    }
  },
  created() {
    this.getBulletins()
  },

  methods: {
    async getBulletins() {
      this.loading = true
      this.error.present = false

      this.items = {
        all: [],
        filtered: [],
        displayed: []
      }
      this.total = 0

      bdService.fetchBulletins(this.sn).then(r => {
        this.items.all = r.data.items
        this.error.present = false
        this.error.text = null
        this.loading = false

        const bulletinStates = this.labels['mymachine_BulletinStates'] || {}
        const bulletinTypes = this.labels['mymachine_BulletinTypes'] || {}

        // translate states and types
        this.items.all = this.items.all.map(x => {
          x.status = bulletinStates[x.status.replaceAll(' ', '-')] || x.status
          x.bulletin_type = bulletinTypes[x.bulletin_type.replaceAll(' ', '-')] || x.bulletin_type
          x.language = langs.where('1', x.language_code).local
          return x
        }).sort((x, y) => new Date(y.createdAt).getTime() - new Date(x.createdAt).getTime())
        this.total = this.items.all.length
        this.items.displayed = this.items.all.slice(0, this.perPage)
        this.items.filtered = this.items.all.slice()

        this.extractFilters(this.items.all)
        this.updateFilters([])
      }).catch(e => {
        if (e.message !== 'isCancelled') {
          this.error = {present: true, text: e.code === 403 ? {ck: 'unauthorizedError', cp: 'CommonErrors'} : {ck: 'defaultError', cp: 'CommonErrors'}, code: e.code || null}
        }
      }).finally(() => {
        this.loading = false
      })
    },
    translateActiveFilter(label) {
      const statusTranslation = this.translate('statusLabel', 'CommonFunctional')
      const typeTranslation = this.translate('typeLabel', 'CommonFunctional')

      if (this.filters.status.includes(label)) return `${statusTranslation}: ${label}`
      else if (this.filters.bulletin_type.includes(label)) return `${typeTranslation}: ${label}`
      return label
    },
    extractFilters(_oi) {
      const _typeSet = new Set()
      const _statusSet = new Set()
      for (const x of _oi) {
        _typeSet.add(x.bulletin_type)
        if (x.status) _statusSet.add(x.status)
      }
      this.filters = {
        current: [],
        original: [..._statusSet],
        bulletin_type: [..._typeSet].map(bulletin_type => ({label: bulletin_type, value: false})) || [],
        status: [..._statusSet].map(status => ({label: status, value: false})) || []
      }
    },
    applyKeyword(keyword) {
      this.keyword = keyword
      this.updateFilters(this.filters.current)
    },
    clearFilters() {
      this.updateFilters([])
    },
    updateFilters(s) {
      if(s) {
        this.filters.status.forEach(status => status.value = false)
        this.filters.bulletin_type.forEach(bulletin_type => bulletin_type.value = false)
      }
      this.items.filtered = this.items.all.filter(item => {
        let allow = true //allow all
        // if there is any filter in current selected filters that comes from type or state list
        const statusFilters = this.filters.status.filter(status => status.value === true).map(status => status.label) || []
        if (statusFilters.length) allow = item.status ? statusFilters.includes(item.status) : true

        const typeFilters = this.filters.bulletin_type.filter(bulletin_type => bulletin_type.value === true).map(bulletin_type => bulletin_type.label) || []
        if (typeFilters.length) allow = item.bulletin_type ? typeFilters.includes(item.bulletin_type) : true

        if (this.filters.current.some(r => this.filters.bulletin_type.includes(r))) allow = allow && this.filters.current.includes(item.bulletin_type)

        // filter by keyword
        allow = allow && (
          item.description.toLowerCase().includes(this.keyword.toLowerCase()) ||
            item.status.toLowerCase().includes(this.keyword.toLowerCase()) ||
            item.bulletin_type.toLowerCase().includes(this.keyword.toLowerCase())
        )
        // filter by expiration date
        if (item.inactive_date) {
          let itemTS = new Date(item.inactive_date)
          const timeDiff = itemTS.getTimezoneOffset() * 60000
          itemTS = new Date(itemTS.valueOf() + timeDiff).getTime()
          if (this.dateFilter.value === 'custom') {
            const [startDate, endDate] = this.dateFilter.custom

            if (startDate) {
              const filterStart = new Date(startDate).getTime()
              allow = allow && itemTS >= filterStart
            }

            if (endDate) {
              const filterEnd = new Date(endDate).getTime()
              allow = allow && itemTS <= filterEnd
            }
          } else if (this.dateFilter.value !== DATE_ALL) {
            const filterTS = new Date(this.dateFilter.value).getTime()
            const todayTS = new Date().setHours(0, 0, 0, 0)
            allow = allow && itemTS >= todayTS && itemTS <= filterTS
          }
        }
        return allow
      })
      this.total = this.items.filtered.length
      this.currentPage = 1
      this.inputPage = 1
      this.items.displayed = this.items.filtered.slice(0, this.perPage)
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
    }
  }
}
</script>

  <style scoped lang="scss">
  @import "../../../scss/colors";
  </style>
