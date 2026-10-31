<template>
  <b-col tag="section" cols="12" :class="{ 'text-gray' : disabled }">
    <overlay-wrapper :show="loading && !contentOverlayOnly && !disabled">
      <slot name="header">
        <h2>
          {{ translate("title", 'HistoryWidget') }}
          <span v-if="widgetMode"
                class="ms-2 small"
          >
            {{ items.displayed.length }}<span class="small fw-normal font-base">/{{ items.all.length }}</span>
          </span>
          <b-link v-if="widgetMode"
                  class="float-end small"
                  data-test-id="show-all-link"
                  :href="historyPageUrl"
                  :disabled="disabled || !items.all.length"
                  :class="{ 'text-gray' : disabled || !items.all.length }"
          >
            <span class="small">{{ translate("viewAll", 'CommonFunctional') }} <b-icon :icon="['fal', 'plus']" class="small" /></span>
          </b-link>
        </h2>
      </slot>

      <slot name="top-content" />

      <overlay-wrapper :show="loading && contentOverlayOnly && !disabled">
        <DataFilter v-if="filtering"
                    :disabled="loading"
                    :show-internals="getCurrentUserInternal"
                    :filters="filters.original"
                    @update="updateFilters"
        />

        <b-row v-if="error">
          <b-col cols="12">
            <div class="box">{{ translate('searchingError', 'CommonErrors') }}</div>
          </b-col>
        </b-row>

        <b-row v-else-if="!disabled">
          <b-col cols="12">
            <BasicGridTable :disabled="loading" header-class="d-none d-md-grid" grid-name="diagnostichistory">
              <template #header>
                <div class="d-flex align-items-center header__diagnosichistory">
                  <span>{{ translate("statusLabel", 'CommonFunctional') }}</span>
                </div>
                <div class="d-flex align-items-center header__diagnosichistory">
                  <span>{{ translate("titleLabel", 'CommonFunctional') }}</span>
                </div>
                <div class="d-flex align-items-center header__diagnosichistory">
                  <span>{{ translate("dateLabel", 'CommonFunctional') }}</span>
                </div>
                <div class="d-flex align-items-center header__diagnosichistory">
                  <span>{{ translate("descriptionLabel", 'CommonFunctional') }}</span>
                </div>
                <div v-if="getCurrentUserInternal" class="d-flex align-items-center header__diagnosichistory">
                  <span>{{ translate("originLabel", 'CommonFunctional') }}</span>
                </div>
              </template>

              <template v-if="items.filtered.length" #rows>
                <HistoryItem v-for="(item, i) in items.displayed"
                             :key="'historyItem' + i + '_' + item._id"
                             :ref="'historyItem__' + item.session_id"
                             :show-internals="getCurrentUserInternal"
                             :item="item"
                             :data-test-id="'history-item__' + item.session_id"
                             @details="openDetails"
                />
              </template>
              <template v-else-if="!loading" #rows>
                <div class="box">
                  {{ translate("noHistoryRecords", 'HistoryWidget') }}
                </div>
              </template>
            </BasicGridTable>
          </b-col>

          <HistorySessionDetailsModal ref="historyItemModal"
                                      :details="modal.session_summary"
                                      :loading="modal.loading"
                                      @download="downloadPdf"
          />

          <b-col v-if="!widgetMode" cols="12" class="d-flex flex-wrap justify-content-between align-items-center mt-3">
            <div class="col-6 col-sm-auto order-1 order-sm-0 ps-sm-0 d-none d-lg-block">
              {{ translate('showLabel', 'CommonFunctional') }}:

              <b-dropdown variant="link" wrapper-class="btn-group" toggle-class="text-decoration-none text-dark page-dropdown">
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
              <b-form-input v-model.lazy="inputPage" :disabled="total <= perPage" class="flex-grow-0 page-input" @update:model-value="handlePageInput" />
            </div>
          </b-col>
        </b-row>

        <div v-else-if="disabled">
          <ListEmptyWidget :range="6" />
        </div>
      </overlay-wrapper>
    </overlay-wrapper>
  </b-col>
</template>

<script>
import { useLabels } from '@/composables/useLabels'
import HistoryItem from '@/components/common/MachineHistory/HistoryItem'
import ListEmptyWidget from '@/components/common/ListEmptyWidget'
import BasicGridTable from '@/components/common/BasicGridTable'
import HistorySessionDetailsModal from '@/components/common/MachineHistory/HistorySessionDetailsModal'
import DataFilter from '@/components/common/MachineHistory/DataFilter'
import {mapGetters} from 'vuex'
import {bdService} from '@/services'

export const FILTER_TYPES = {
  'In Progress': 'In Progress',
  'Finished New Solution': 'Completed',
  'Canceled': 'Canceled',
  'Finished No Trouble Found': 'Completed',
  'Paused': 'Paused'
}

export default {
  conceptName: ['HistoryWidget', 'CommonFunctional', 'CommonErrors'],
  setup() { return useLabels() },
  components: {
    DataFilter,
    HistoryItem,
    ListEmptyWidget,
    BasicGridTable,
    HistorySessionDetailsModal
  },
  props: {
    widgetMode: {
      type: Boolean,
      request: false,
      default: false
    },
    disabled: {
      type: Boolean,
      request: false,
      default: false
    },
    filtering: {
      type: Boolean,
      request: false,
      default: true
    },
    historyPageUrl: {
      type: String,
      required: false,
      default: null
    },
    contentOverlayOnly: {
      type: Boolean,
      required: false,
      default: false
    }
  },
  data() {
    return {
      items: {
        all: [],
        filtered: [],
        displayed: []
      },
      total: 0,
      perPage: 10,
      inputPage: 1,
      currentPage: 1,
      loading: true,
      widgetMaxElements: 5,
      filters: {
        current: [],
        original: []
      },
      modal: {
        loading: false,
        session_summary: null
      },
      error: false
    }
  },
  computed: {
    ...mapGetters([
      'getCurrentSerial',
      'getCurrentUserInternal'
    ])
  },
  watch: {
    getCurrentSerial(n) {
      if (!this.disabled && n) {
        this.clearResults()
        this.getHistory()
      }
    },
    currentPage(p) {
      this.inputPage = p
      this.items.displayed = this.items.filtered.slice((p - 1) * this.perPage, p * this.perPage)
    },
    perPage() {
      this.currentPage = 1
      this.inputPage = 1
      this.items.displayed = this.items.filtered.slice(0, this.perPage)
    }
  },
  created() {
    if (!this.disabled) this.getHistory()
  },
  methods: {
    handlePageInput(v) {
      let _v = parseInt(v)
      const _maxPage = Math.ceil(this.total / this.perPage)
      if (_v < 1) {
        _v = 1
      } else if (_v > _maxPage) {
        _v = _maxPage
      }
      this.inputPage = _v
      this.currentPage = _v
    },
    clearResults() {
      this.total = 0
      this.perPage = 10
      this.inputPage = 1
      this.currentPage = 1
      this.items = {
        all: [],
        filtered: [],
        displayed: []
      }
    },
    async getHistory(serial = this.getCurrentSerial) {
      this.loading = true
      this.error = false
      bdService.fetchTroubleshootingSessionList(serial).then(r => {
        this.items.all = r.data.filter(x => x.state !== 'Canceled').map(x => {
          x.state = FILTER_TYPES[x.state] || x.state
          return x
        }).sort((x, y) => new Date(y.createdAt).getTime() - new Date(x.createdAt).getTime())

        this.total = this.items.all.length
        this.items.displayed = this.items.all.slice(0, this.widgetMode ? this.widgetMaxElements : this.perPage)
        this.items.filtered = this.items.all.slice()
      }).catch(error => {
        if (error && !error.isCancelled) {
          this.error = true
        }
      }).finally(() => {
        this.loading = false
        this.extractFilters(this.items.all)
        this.updateFilters(this.filters.current)
      })
    },
    extractFilters(_oi) {
      const _set = new Set()
      for (const x of _oi) _set.add(x.state)
      const _nf = {
        original: [..._set],
        current: [..._set, 'external', 'internal']
      }
      this.filters = _nf
    },
    updateFilters(s) {
      this.filters.current = s
      this.items.filtered = this.items.all.filter(item => {
        let allow = this.filters.current.includes(item.state)
        if (this.getCurrentUserInternal) { //internal users can filter by internal/external
          allow = allow && this.filters.current.includes(item.isInternal ? 'internal' : 'external')
        } else {
          allow = allow && item.isInternal === false
        }
        return allow
      })
      this.total = this.items.filtered.length
      this.currentPage = 1
      this.inputPage = 1
      this.items.displayed = this.items.filtered.slice(0, this.widgetMode ? this.widgetMaxElements : this.perPage)
    },
    openDetails(_sid) {
      this.modal.loading = true
      this.modal.session_summary = null
      this.$refs['historyItemModal'].show()

      bdService.troubleshootingSessionSummary(_sid).then(r => {
        this.modal.session_summary = r.data
        this.modal.loading = false
      })
    },
    downloadPdf(_id) {
      if (_id) this.$refs['historyItem__' + _id][0].downloadFile()
    }
  }
}
</script>
