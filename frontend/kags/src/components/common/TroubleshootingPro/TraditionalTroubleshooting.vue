<template>
  <div>
    <ErrorSearch ajax
                 class="mb-4 py-4"
                 :disabled="disabled"
                 :action="config.errorInputUrl"
                 is-troubleshooting-search
    />

    <OpenSessionsForSerial v-if="!disabled && getTroubleshootingKeyword === ''"
                           ref="openSessionsRef"
                           @sessions-updated="openSessions = $event"
    />

    <overlay-wrapper v-if="!disabled && !error.present" :show="loading">
      <h2 v-if="!showRank">
        {{ translate('topGuidesForModel', 'TroubleshootingProcedures') }}
      </h2>

      <b-row>
        <b-col cols="12" class="d-none d-md-flex">
          <b-table v-if="items.loaded.length"
                   borderless
                   small
                   hover
                   head-variant="dark"
                   class="troubleshooting_table"
                   thead-tr-class="small"
                   :items="items.loaded"
                   :fields="fields"
                   @row-clicked="({item, event}) => sessionDetails(item, event)"
          >
            <template v-slot:cell(title)="data">
              <span class="font-condensed">{{ data.value }}</span>
            </template>

            <template v-slot:cell(view)="data">
              <b-link class="font-condensed text-nowrap" @click.prevent="sessionDetails(data.item)">
                {{ translate('faultDetails', 'TroubleshootingProcedures') }}
                <b-icon icon="caret-right" class="ms-1" />
              </b-link>
            </template>

            <template v-slot:cell(score)="data">
              <b-progress v-b-tooltip="{ title: 'Rank: ' + data.value }"
                          height=".5rem"
                          variant="success"
                          style="min-width: 3.5rem"
                          class="me-2"
                          :value="data.value * 100"
              />
            </template>

            <template v-slot:cell(tree_id)="data">
              <b-button size="sm"
                     variant="primary"
                     class="font-condensed text-nowrap w-100"
                     :disabled="session.starting === activeItem.tree_id || data.item.sessionError"
                     :href="data.value"
                     @click.prevent="startSession(data.item)"
              >
                <template v-if="session.starting === data.item.tree_id">
                  <b-spinner variant="light" small />
                </template>
                {{ translate('startSession', 'TroubleshootingProcedures') }}
              </b-button>
            </template>
          </b-table>
        </b-col>

        <!-- START mobile version -->
        <ProcedureItemMobile v-for="itm in items.loaded"
                             :key="'troubleshooting-results__mobile_'+itm.tree_id"
                             :item="itm"
                             @sessionStart="startSession"
                             @sessionDetails="sessionDetails"
        />
        <!-- END mobile version -->

        <b-notification v-if="notification">
          {{ replace(translate(notification.ck, notification.cp), '[sn]', getCurrentSerial) }}
        </b-notification>

        <b-notification v-else-if="getTroubleshootingKeyword === '' && items.all.length === 0 && !loading">
          {{ translate('emptyQuery', 'TroubleshootingProcedures') }}
        </b-notification>

        <b-col v-else-if="!items.all.length">
          <div v-if="!loading" class="box">{{ translate('noProceduresFound', 'TroubleshootingProcedures') }}</div>
          <ListEmptyWidget v-else />
        </b-col>
      </b-row>
    </overlay-wrapper>

    <b-error v-else-if="error.present" :code="error.code" data-test-id="troubleshooting-error">
      {{ translate(error.text.ck, error.text.cp) }}
    </b-error>

    <template v-else-if="disabled">
      <ListEmptyWidget />
    </template>

    <b-col v-else class="d-flex align-self-center justify-content-center py-5 box">
      <b-spinner variant="primary" data-test-id="history-loading" />
    </b-col>

    <b-modal ref="sessionDetailsModal"
             ok-only
             scrollable
             size="lg"
             :title="`${activeItem.title} -${ activeItem.short_description}`"
             @hidden="restoreRowFocus"
    >
      <template v-slot:default>
        <b-col cols="12">
          <b-row align-v="center">
            <b-col cols="auto"
                   class="py-3 pe-0 font-condensed"
            >
              {{ translate('rank', 'TroubleshootingProcedures') }}
            </b-col>
            <b-col v-b-tooltip.right="{ title: 'Rank: ' + activeItem.score }" cols="6" sm="5" md="4" lg="3">
              <b-progress height=".5rem"
                          variant="success"
                          :value="activeItem.scoreValue"
              />
            </b-col>
          </b-row>
        </b-col>
        <b-col cols="12"
               class="py-3 width_override"
               v-html="activeItem.long_description"
        />
      </template>

      <template #footer>
        <div class="d-flex w-100 px-3" :class="(activeItem.sessionError) ? 'justify-content-between' : 'justify-content-end'">
          <div v-if="activeItem.sessionError" class="box bg-gray-400">
            <b-icon icon="lock-alt" class="small align-self-end text-gray-600 me-2" />
            {{ translate("sessionStartError", 'TroubleshootingProcedures') }}
          </div>
          <div class="my-3 text-end">
            <b-button variant="primary"
                   class="font-condensed text-nowrap"
                   :disabled="session.starting === activeItem.tree_id || activeItem.sessionError"
                   @click.prevent="startSession(activeItem)"
            >
              <template v-if="session.starting === activeItem.tree_id">
                <b-spinner variant="light" small />
              </template>
              {{ translate('startSession', 'TroubleshootingProcedures') }}
            </b-button>
          </div>
        </div>
      </template>
    </b-modal>

    <ResumeExistingSessionModal ref="resumeModal"
                                :details="pendingItem"
                                :sessions="openSessions"
                                @start-new-session="proceedStartSession(pendingItem)"
    />

    <LazyVisibleRenderer v-if="rendered"
                         @visible="renderMore"
    />
    <div v-if="rendered" class="text-center mt-3 d-none d-block-ie-only">
      <b-button variant="primary" class="font-condensed" @click.prevent="renderMore">{{ translate('loadMore', 'CommonFunctional') }}</b-button>
    </div>
  </div>
</template>

<script>
import { useFormatters } from '@/composables/useFormatters'
import { useLabels } from '@/composables/useLabels'
import { mapGetters } from 'vuex'
import ListEmptyWidget from '@/components/common/ListEmptyWidget'
import ErrorSearch from '@/components/common/Machine/ErrorSearch'
import LazyVisibleRenderer from '@/components/common/LazyVisibleRenderer'
import ProcedureItemMobile from '@/components/common/TroubleshootingPro/ProcedureItemMobile'
import OpenSessionsForSerial from '@/components/common/TroubleshootingPro/OpenSessionsForSerial'
import ResumeExistingSessionModal from '@/components/common/TroubleshootingPro/ResumeExistingSessionModal'
import BError from '@/components/common/BError'
import BNotification from '@/components/common/BNotification'
import { bdService } from '@/services'

export default {
  name: 'TraditionalTroubleshooting',
  conceptName: ['TroubleshootingProcedures', 'CommonErrors', 'CommonFunctional', 'TroubleshootingErrors'],
  setup() { return { ...useLabels(), ...useFormatters() } },
  components: {
    ErrorSearch,
    ListEmptyWidget,
    ProcedureItemMobile,
    OpenSessionsForSerial,
    ResumeExistingSessionModal,
    LazyVisibleRenderer,
    BError,
    BNotification
  },
  props: {
    disabled: {
      type: Boolean,
      required: false,
      default: false
    },
    config: {
      type: Object,
      required: false,
      default: () => ({})
    }
  },
  data() {
    return {
      loading: false,
      notification: null,
      error: {
        present: false,
        text: null,
        code: null
      },
      showAll: true,
      activeItem: {},
      items: {
        all: [],
        loaded: [],
        step: 40,
        currentIndex: 0
      },
      session: {
        starting: null,
        error: null
      },
      rendered: false,
      showRank: true,
      openSessions: [],
      pendingItem: null
    }
  },
  computed: {
    fields() {
      const allFields = [
        {key: 'title', label: this.translate('procedureName', 'TroubleshootingProcedures')},
        {key: 'short_description', label: this.translate('description', 'TroubleshootingProcedures')},
        {key: 'view', label: '', sortable: false, tdClass: !this.showRank ? 'text-end' : ''},
        {key: 'score', label: this.translate('rank', 'TroubleshootingProcedures'), sortable: false},
        {key: 'tree_id', label: this.translate('actions', 'TroubleshootingProcedures'), sortable: false}
      ]
      return this.showRank ? allFields : allFields.filter(f => f.key !== 'score')
    },
    ...mapGetters([
      'getCurrentSerial',
      'getCurrentAbbreviation',
      'getTroubleshootingKeyword',
      'getTroubleshootingSearchType'
    ])
  },
  watch: {
    getTroubleshootingKeyword() {
      this.fetchResults(false)
    },
    getTroubleshootingSearchType() {
      this.fetchResults(false)
    },
    getCurrentSerial(s) {
      if (s) this.fetchResults(false)
    }
  },
  created() {
    this.fetchResults(true)
  },
  methods: {
    startSession(itm) {
      const existingSessions = this.openSessions.filter(s => s.title === itm.title)
      if (existingSessions.length) {
        this.pendingItem = itm
        this.$refs.resumeModal.show()
        return
      }
      this.proceedStartSession(itm)
    },
    proceedStartSession(itm) {
      this.session.starting = itm.tree_id
      bdService.troubleshootingSessionStart(this.getCurrentSerial, itm.tree_id).then(r => {
        window.open(r.data.url)
        this.session.starting = null
        setTimeout(() => {
          if (this.$refs.openSessionsRef) this.$refs.openSessionsRef.refresh()
        }, 3000)
      }).catch(() => {
        this.session.starting = null
        this.$set(itm, 'sessionError', true)
        this.sessionDetails(itm)
      })
    },
    sessionDetails(itm, rowEvent) {
      this.activeItem = itm
      // Remember the clicked table row so focus (and scroll) can return to it on close, like it
      // does for the "Fault Details" link -- bootstrap-vue-next rows aren't focusable on their own.
      this.detailsRow = rowEvent && rowEvent.target ? rowEvent.target.closest('tr') : null
      this.$refs['sessionDetailsModal'].show()
    },
    restoreRowFocus() {
      const row = this.detailsRow
      this.detailsRow = null
      if (!row || !row.isConnected) return
      if (!row.hasAttribute('tabindex')) row.setAttribute('tabindex', '-1')
      row.focus()
    },
    getErrorText(error) {
      if (error.error_code === 403) return {ck: 'unauthorizedError', cp: 'CommonErrors'}
      return {ck: 'searchingError', cp: 'CommonErrors'}
    },
    fetchTopGuides() {
      this.loading = true
      this.rendered = false
      this.notification = null
      this.error.present = false

      bdService.fetchTroubleshootingTopGuides(this.getCurrentSerial, {
        params: {limit: 10}
      }).then(r => {
        this.items.loaded = []
        this.items.all = r.data.map(x => {
          x.scoreValue = undefined
          return x
        })
        this.items.loaded = this.items.all.slice(0, this.items.step)
        this.showRank = false
        this.rendered = true
        this.loading = false
      }).catch(e => {
        if (e.error_message && e.error_message.match(/Serial Number [A-Za-z0-9]+ could not be found./g)) {
          const abbreviationErrors = this.labels['TroubleshootingErrors'] || {}
          if (abbreviationErrors['notSupported' + this.getCurrentAbbreviation]) {
            this.notification = {ck: 'notSupported' + this.getCurrentAbbreviation, cp: 'TroubleshootingErrors'}
          } else {
            this.notification = {ck: 'notSupported', cp: 'TroubleshootingErrors'}
          }
          this.loading = false
        } else if (!e.isCancelled) {
          this.error = {present: true, text: this.getErrorText(e), code: e.error_code || null}
          this.loading = false
        }
      })
    },
    fetchResults(isInitLoad) {
      if (!this.disabled && this.getCurrentSerial) {
        this.loading = true
        this.error = {present: false, text: null, code: null}
        this.items.all = []
        this.items.loaded = []

        if (!this.getTroubleshootingKeyword) {
          this.fetchTopGuides()
          return
        }

        this.showRank = true
        bdService.fetchTroubleshootingGuides(this.getCurrentSerial, {
          params: {
            symptom: this.getTroubleshootingKeyword,
            isExactMatch: this.getTroubleshootingSearchType
          }
        }).then(r => {
          if (!isInitLoad) this.showAll = false
          if (!isInitLoad || (isInitLoad && this.showAll)) {
            this.items.all = r.data.map(x => {
              x.scoreValue = x.score * 100
              return x
            })
            this.items.loaded = this.items.all.slice(0, this.items.step)
            this.loading = false
            this.rendered = true
          }
        }).catch(e => {
          if (e.error_message && e.error_message.match(/Serial Number [A-Za-z0-9]+ could not be found./g)) {
            const abbreviationErrors = this.labels['TroubleshootingErrors'] || {}
            if (abbreviationErrors['notSupported' + this.getCurrentAbbreviation]) {
              this.notification = {ck: 'notSupported' + this.getCurrentAbbreviation, cp: 'TroubleshootingErrors'}
            } else {
              this.notification = {ck: 'notSupported', cp: 'TroubleshootingErrors'}
            }
            this.loading = false
          } else if (!e.isCancelled) {
            this.error = {present: true, text: this.getErrorText(e), code: e.error_code || null}
            this.loading = false
          }
        })
      }
    },
    renderMore(isVisible) {
      if (isVisible && this.items.loaded.length < this.items.all.length) {
        this.rendered = false
        this.items.currentIndex = this.items.loaded.length
        this.items.loaded.push(...(this.items.all.slice(this.items.currentIndex, this.items.currentIndex + this.items.step)))
        this.$nextTick(() => {
          this.rendered = true
        })
      }
    }
  }
}
</script>

<style lang="scss">
.width_override {
  table, td {
    width: auto !important;
  }
}

.troubleshooting_table {
  td[aria-colindex="1"] {
    max-width: 150px !important;
    width: 150px;
  }

  td[aria-colindex="4"] {
    text-align: center;
    width: 135px !important;
  }

  tr {
    th:first-child {
      padding-left: .6125rem;
    }
  }
}
</style>
