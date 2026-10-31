<template>
  <div class="open-sessions-section mb-4">
    <div class="d-flex align-items-center mb-3">
      <b-badge pill class="badge-critical me-2">{{ sessionItems.length }}</b-badge>
      <h2 class="mb-0">
        {{ translate('openSession', 'TroubleshootingProcedures') }}
      </h2>
    </div>

    <div v-if="loading" class="d-flex align-items-center justify-content-center mb-3">
      <b-spinner variant="warning" class="me-2" />
    </div>

    <BasicGridTable v-else
                    grid-name="open-sessions"
                    header-class="d-none d-md-grid"
    >
      <template #header>
        <div class="header__title">{{ translate('procedureName', 'TroubleshootingProcedures') }}</div>
        <div class="header__description">{{ translate('description', 'TroubleshootingProcedures') }}</div>
        <div class="header__date_opened">{{ translate('dateOpened', 'TroubleshootingProcedures') }}</div>
        <div class="header__actions">{{ translate('sessionActions', 'TroubleshootingProcedures') }}</div>
      </template>
      <template #rows>
        <template v-if="sessionItems.length">
          <OpenSessionItem v-for="itm in displayedSessions"
                           :key="'open-session_' + itm.session_id"
                           :item="itm"
                           :resuming="session.resuming === itm.session_id"
                           @review="onReview"
                           @resume="onResume"
          />
        </template>
        <div v-else class="box">
          {{ translate('noSessionResult', 'TroubleshootingProcedures') }}
        </div>
      </template>
    </BasicGridTable>

    <div v-if="sessionItems.length > perPage" class="pagination-data text-center">
      <div class="d-none d-md-flex justify-content-center mt-3">
        <b-pagination v-model="currentPage"
                      align="center"
                      class="col-12 col-sm-auto order-0 order-sm-1 mb-2 mb-sm-0 pe-0"
                      first-number
                      last-number
                      no-goto-end-buttons
                      :total-rows="Math.max(sessionItems.length, 1)"
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
      </div>
      <div class="d-block d-md-none">
        <b-button size="sm"
               variant="primary"
               class="font-condensed text-nowrap mt-4 px-4 py-2"
               @click.stop.prevent="loadMore"
        >
          {{ translate('loadMore', 'CommonFunctional') }}
        </b-button>
      </div>
    </div>

    <HistorySessionDetailsModal ref="historyItemModal"
                                :details="modal.session_summary"
                                :loading="modal.loading"
                                @download="onDownloadPdf"
    />
  </div>
</template>

<script>
import { useLabels } from '@/composables/useLabels'
import { mapGetters } from 'vuex'
import BasicGridTable from '@/components/common/BasicGridTable'
import OpenSessionItem from '@/components/common/TroubleshootingPro/OpenSessionItem'
import HistorySessionDetailsModal from '@/components/common/MachineHistory/HistorySessionDetailsModal'
import { bdService } from '@/services'

export default {
  name: 'OpenSessionsForSerial',
  conceptName: ['TroubleshootingProcedures', 'CommonFunctional'],
  setup() { return useLabels() },
  components: {
    BasicGridTable,
    OpenSessionItem,
    HistorySessionDetailsModal
  },
  data() {
    return {
      loading: false,
      sessionItems: [],
      displayedSessions: [],
      perPage: 5,
      currentPage: 1,
      loadMoreCount: 0,
      session: {
        resuming: null
      },
      modal: {
        loading: false,
        session_summary: null
      }
    }
  },
  computed: {
    ...mapGetters(['getCurrentSerial'])
  },
  watch: {
    getCurrentSerial(s) {
      if (s) this.fetchOpenSessions()
    },
    currentPage() {
      this.updatePagination()
    },
    sessionItems(val) {
      this.$emit('sessions-updated', val)
    }
  },
  created() {
    this.fetchOpenSessions()
  },
  methods: {
    fetchOpenSessions() {
      if (!this.getCurrentSerial) {
        this.sessionItems = []
        this.displayedSessions = []
        return
      }
      this.loading = true
      bdService.fetchTroubleshootingSessionList(this.getCurrentSerial).then(r => {
        this.sessionItems = (r.data || [])
          .filter(x => x.state === 'In Progress' || x.state === 'Paused')
          .sort((x, y) => new Date(y.createdAt).getTime() - new Date(x.createdAt).getTime())
        this.currentPage = 1
        this.loadMoreCount = 0
        this.updatePagination()
        this.loading = false
      }).catch(() => {
        this.sessionItems = []
        this.displayedSessions = []
        this.loading = false
      })
    },
    updatePagination() {
      const page = this.currentPage || 1
      const start = (page - 1) * this.perPage
      const end = page * this.perPage
      this.displayedSessions = this.sessionItems.slice(start, end)
    },
    loadMore() {
      if (!this.loadMoreCount) this.loadMoreCount = this.perPage
      this.loadMoreCount += this.perPage
      this.displayedSessions = this.sessionItems.slice(0, this.loadMoreCount)
    },
    onReview(sessionId) {
      this.modal.loading = true
      this.modal.session_summary = null
      this.$refs.historyItemModal.show()

      bdService.troubleshootingSessionSummary(sessionId).then(r => {
        this.modal.session_summary = r.data
        this.modal.loading = false
      }).catch(() => {
        this.modal.loading = false
        this.$refs.historyItemModal.hide()
      })
    },
    onResume(sessionId) {
      this.session.resuming = sessionId
      bdService.troubleshootingSessionResume(sessionId).then(r => {
        window.open(r.data.url)
        this.session.resuming = null
      }).catch(() => {
        this.session.resuming = null
      })
    },
    onDownloadPdf(id) {
      bdService.troubleshootingSessionSummaryPDF(id).then(r => {
        if (window.navigator && window.navigator.msSaveOrOpenBlob) {
          window.navigator.msSaveOrOpenBlob(new Blob([r.data], {type: 'application/pdf'}))
          return
        }
        const link = document.createElement('a')
        link.href = window.URL.createObjectURL(new Blob([r.data], {type: 'application/pdf'}))
        link.download = 'session_' + id + '.pdf'
        link.click()
      })
    },
    refresh() {
      this.fetchOpenSessions()
    }
  }
}
</script>
