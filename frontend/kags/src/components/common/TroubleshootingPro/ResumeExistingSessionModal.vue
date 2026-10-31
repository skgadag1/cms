<template>
  <div>
    <b-modal ref="modal"
             size="lg"
             modal-class="resume-existing-session-modal"
             no-close-on-backdrop
             :title="translate('resumeExistingSessionTitle', 'TroubleshootingProcedures')"
    >
      <p>{{ translate('resumeExistingSessionDescription', 'TroubleshootingProcedures') }}</p>
      <p class="mb-1">
        <strong>{{ translate('procedureName', 'TroubleshootingProcedures') }}:</strong>
        {{ details && details.title }}
      </p>
      <p>
        <strong>{{ translate('description', 'TroubleshootingProcedures') }}:</strong>
        {{ details && details.short_description }}
      </p>

      <div class="d-flex align-items-center mb-3">
        <b-badge pill class="badge-critical me-2">{{ matchingSessions.length }}</b-badge>
        <h3 class="mb-0">
          {{ translate('openSession', 'TroubleshootingProcedures') }}
        </h3>
      </div>

      <BasicGridTable grid-name="resume-existing-session"
                      header-class="d-none d-md-grid"
      >
        <template #header>
          <div class="header__date_opened">{{ translate('dateOpened', 'TroubleshootingProcedures') }}</div>
          <div class="header__actions">{{ translate('action', 'TroubleshootingProcedures') }}</div>
        </template>
        <template #rows>
          <div v-for="itm in displayedSessions"
               :key="'resume-existing-session_' + itm.session_id"
               class="grid-table-row"
          >
            <div class="item__date_opened d-flex d-md-block justify-content-between">
              <label class="d-block d-md-none me-auto">{{ translate('dateOpened', 'TroubleshootingProcedures') }}</label>
              <div class="text-end text-md-start">
                <div class="item__date_value">{{ formatDate(itm.createdAt) }}</div>
                <div class="item__days_ago d-block d-md-none m-1">
                  ({{ daysSince(itm.createdAt) }} {{ translate('daysAgo', 'TroubleshootingProcedures') }})
                </div>
              </div>
            </div>
            <div class="item__session_actions d-flex justify-content-end">
              <b-button variant="outline-primary"
                     size="sm"
                     class="font-condensed text-nowrap"
                     :data-test-id="'resume-existing-session__review__' + itm.session_id"
                     @click.stop.prevent="onReview(itm.session_id)"
              >
                {{ translate('review', 'TroubleshootingProcedures') }}
              </b-button>
              <b-button v-if="itm.isResumingAllowed"
                     variant="primary"
                     size="sm"
                     class="font-condensed text-nowrap"
                     :disabled="session.resuming === itm.session_id"
                     :data-test-id="'resume-existing-session__resume__' + itm.session_id"
                     @click.stop.prevent="onResume(itm.session_id)"
              >
                {{ translate('resume', 'TroubleshootingProcedures') }}
              </b-button>
            </div>
          </div>
        </template>
      </BasicGridTable>

      <div v-if="matchingSessions.length > perPage" class="pagination-data text-center">
        <div class="d-none d-md-flex justify-content-center mt-3">
          <b-pagination v-model="currentPage"
                        align="center"
                        class="col-12 col-sm-auto order-0 order-sm-1 mb-2 mb-sm-0 pe-0"
                        first-number
                        last-number
                        no-goto-end-buttons
                        :total-rows="Math.max(matchingSessions.length, 1)"
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

      <template #footer>
        <b-button variant="outline-primary"
               class="font-condensed"
               data-test-id="resume-existing-session__cancel"
               @click.prevent="cancel"
        >
          {{ translate('cancelButton', 'TroubleshootingProcedures') }}
        </b-button>
        <b-button variant="primary"
               class="font-condensed"
               data-test-id="resume-existing-session__start-new"
               @click.prevent="onStartNewSession"
        >
          {{ translate('startNewSession', 'TroubleshootingProcedures') }}
        </b-button>
      </template>
    </b-modal>

    <HistorySessionDetailsModal ref="historyItemModal"
                                :details="modal.session_summary"
                                :loading="modal.loading"
                                @download="onDownloadPdf"
    />
  </div>
</template>

<script>
import { useFormatters } from '@/composables/useFormatters'
import { useLabels } from '@/composables/useLabels'
import BasicGridTable from '@/components/common/BasicGridTable'
import HistorySessionDetailsModal from '@/components/common/MachineHistory/HistorySessionDetailsModal'
import { bdService } from '@/services'
import { daysSince } from '@/helpers/dateTimeUtils'

export default {
  name: 'ResumeExistingSessionModal',
  conceptName: ['TroubleshootingProcedures', 'HistoryWidget', 'CommonFunctional'],
  setup() { return { ...useLabels(), ...useFormatters() } },
  components: {
    BasicGridTable,
    HistorySessionDetailsModal
  },
  props: {
    details: {
      type: Object,
      required: false,
      default: null
    },
    sessions: {
      type: Array,
      required: false,
      default: () => []
    }
  },
  data() {
    return {
      perPage: 5,
      currentPage: 1,
      loadMoreCount: 0,
      displayedSessions: [],
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
    matchingSessions() {
      return this.sessions.filter(itm => itm.title === (this.details && this.details.title))
    }
  },
  watch: {
    matchingSessions() {
      this.currentPage = 1
      this.loadMoreCount = 0
      this.updatePagination()
    },
    currentPage() {
      this.updatePagination()
    }
  },
  created() {
    this.updatePagination()
  },
  methods: {
    daysSince,
    updatePagination() {
      const page = this.currentPage || 1
      const start = (page - 1) * this.perPage
      const end = page * this.perPage
      this.displayedSessions = this.matchingSessions.slice(start, end)
    },
    loadMore() {
      if (!this.loadMoreCount) this.loadMoreCount = this.perPage
      this.loadMoreCount += this.perPage
      this.displayedSessions = this.matchingSessions.slice(0, this.loadMoreCount)
    },
    show() {
      this.$refs.modal.show()
    },
    close() {
      this.$refs.modal.hide()
    },
    cancel() {
      this.close()
    },
    onReview(sessionId) {
      this.close()
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
      this.close()
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
    onStartNewSession() {
      this.close()
      this.$emit('start-new-session')
    }
  }
}
</script>
