<template>
  <b-modal ref="modal"
           size="lg"
           no-footer
           :title="translate('vehicleSessionSummary', 'HistoryWidget')"
           @show="handleMainScrollbar"
           @hidden="handleMainScrollbar(false)"
  >
    <div v-if="loading" class="d-flex w-100 align-items-center justify-content-center py-5 my-5">
      <b-spinner variant="primary" />
    </div>

    <div v-else-if="details" class="m-4">
      <b-row v-for="(field, idx) in display_fields"
             :key="'display_fields_'+idx"
      >
        <b-col cols="4" class="text-gray-600">{{ field.label }}</b-col>
        <b-col cols="8" class="fw-bold">{{ formatSessionDates(field.variable) }}</b-col>
      </b-row>
      <hr>
      <h2 class="mb-4">{{ translate("diagnosticSessionHistory", 'HistoryWidget') }}</h2>

      <b-row v-if="author">
        <b-col cols="12"
               class="text-red fst-italic small mb-1"
        >
          {{ translate("diagnosticSessionStarted", 'HistoryWidget') }} <b>{{ author }}</b>
        </b-col>
      </b-row>

      <b-row v-for="(log, i) in filteredLogs"
             :key="'session_summary_'+i"
             class="pb-4 mt-2 bg-dotted me-0"
             :style="bgIfNotLast(i)"
      >
        <b-col v-if="log.event_name.toLowerCase() !== 'user metadata changed'" cols="12" md="auto" class="font-condensed">
          <b-row>
            <b-col cols="2" md="auto" class="pe-0">
              <b-icon icon="clock" class="bg-white" />
            </b-col>
            <b-col class="bg-white pb-1 ps-2 pe-4">
              {{ log.localeDatetime.split(',')[0] }}<br>{{ log.localeDatetime.split(',')[1] }}
            </b-col>
          </b-row>
        </b-col>

        <b-col v-if="log.event_name.toLowerCase() !== 'user metadata changed'" cols="10" md offset="2" offset-md="0">
          <div v-if="log.event_name.toLowerCase() !== 'question' && log.event_name.toLowerCase() !== 'finished new solution'
            && log.event_name.toLowerCase() !== 'paused' && log.event_name.toLowerCase() !== 'solution'"
          >
            {{ log.event_name }}
          </div>
          <h3 v-else-if="log.event_name.toLowerCase() === 'finished new solution'">
            <b-icon icon="check" class="text-green me-2" />
            {{ translate("diagnosticSessionFinished", 'HistoryWidget') }}
          </h3>
          <h3 v-else-if="log.event_name.toLowerCase() === 'paused'">
            <b-icon icon="pause" class="text-yellow me-2" />
            {{ translate("diagnosticSessionPaused", 'HistoryWidget') }}
          </h3>
          <div v-if="log.data">
            <b-row v-for="(logData, j) in log.data"
                   :key="'log-data_'+ j"
                   class="bg-gray-100 mb-1 py-1"
            >
              <b-col cols="12"
                     lg="2"
                     class="font-condensed pe-0"
              >
                {{ j }}
              </b-col>
              <b-col cols="12"
                     lg="10"
              >
                <template v-if="isFormInput(j)">
                  <div v-if="normalizeFormInputs(logData).length">
                    <div v-for="(input, inputIndex) in normalizeFormInputs(logData)"
                      :key="'form-input_' + i + '_' + inputIndex"
                      class="mb-1"
                    >
                      <span class="fw-bold">{{ input.label }}:</span>
                      <span>{{ input.value }}</span>
                    </div>
                  </div>
                  <span v-else>-</span>
                </template>
                <span v-else>{{ formatLogValue(logData) }}</span>
              </b-col>
            </b-row>
          </div>
        </b-col>
        <b-col v-else-if="authordate != log.localeDatetime"
               cols="12"
               class="text-red fst-italic small bg-white pt-2 pb-3"
        >
          {{ translate("diagnosticSessionResumed", 'HistoryWidget') }} <b>{{ log.data.firstName }} {{ log.data.lastName }}</b>
        </b-col>
      </b-row>

      <b-row>
        <b-col cols="12" class="text-md-end">
          <b-button class="font-condensed me-1 mb-3 mb-md-0"
                    variant="primary"
                    @click.stop="$emit('download', details.id)"
          >
            <b-icon icon="download" class="me-1" /> {{ translate("exportToPDF", 'HistoryWidget') }}
          </b-button>
          <b-button v-if="details.state.toLowerCase() === 'in progress' || details.state.toLowerCase() === 'paused'"
                    target="_blank"
                    class="font-condensed mb-3 mb-md-0"
                    :variant="isResumeDisabled ? 'light' : 'primary'"
                    :disabled="isResumeDisabled"
                    @click.prevent="resumeSession(details.id)"
          >
            <template v-if="session.resuming !== null">
              <b-spinner variant="light" small>{{ translate("resumeSession", 'HistoryWidget') }}</b-spinner>
            </template>
            <b-icon v-else icon="redo" class="me-1" /> {{ translate("resumeSession", 'HistoryWidget') }}
          </b-button>
        </b-col>
      </b-row>
    </div>
  </b-modal>
</template>

<script>
import { useLabels } from '@/composables/useLabels'
import {bdService} from '@/services'
import {mapGetters} from 'vuex'

export default {
  conceptName: ['HistoryWidget'],
  setup() { return useLabels() },
  props: {
    details: {
      required: true
    },
    loading: {
      type: Boolean,
      required: false,
      default: true
    }
  },
  data() {
    return {
      display_fields: [
        {
          label: 'Session ID',
          variable: 'id'
        },
        {
          label: 'Title',
          variable: 'friendly_tree_title'
        },
        {
          label: 'Description',
          variable: 'short_description'
        },
        {
          label: 'Status',
          variable: 'state'
        },
        {
          label: 'Serial Number (VIN)',
          variable: 'sn'
        },
        {
          label: 'Product',
          variable: 'product'
        },
        {
          label: 'Model',
          variable: 'model'
        },
        {
          label: 'Hours',
          variable: 'hours'
        },
        {
          label: 'Session Start',
          variable: 'session_start'
        },
        {
          label: 'Session End',
          variable: 'session_end'
        },
        {
          label: 'Total Session Time',
          variable: 'total_session_time'
        }
      ],
      session: {
        resuming: null
      }
    }
  },
  computed: {
    sortedLogs() {
      return this.details.logs
        .map(sessionLog => {
          if (sessionLog.datetime) sessionLog.localeDatetime = (new Date(sessionLog.datetime)).toLocaleString()
          if(sessionLog.data) {
            delete sessionLog.data['Session State']
            delete sessionLog.data['Engine Hours']
          }
          return sessionLog
        }).sort((x, y) => (x.datetime > y.datetime) - (x.datetime < y.datetime))
    },
    authorLog() {
      return this.sortedLogs.find(x => x.event_name.toLowerCase() === 'user metadata changed') || null
    },
    author() {
      return this.authorLog ? this.authorLog.data.firstName + ' ' + this.authorLog.data.lastName : null
    },
    authordate() {
      return this.authorLog ? this.authorLog.localeDatetime : null
    },
    filteredLogs() {
      const notAllowedEvents = ['vehicle initialized', 'diagnostic started']
      return this.sortedLogs
        .filter(sessionLog => !notAllowedEvents.includes(sessionLog.event_name.toLowerCase()))
    },
    isResumeDisabled() {
      return !this.details.isResumingAllowed || this.session.resuming !== null
    },
    ...mapGetters([
      'getCurrentSerial'
    ])
  },
  methods: {
    show() {
      this.$refs.modal.show()
    },
    formatSessionDates(fieldName) {
      if (fieldName === 'session_start') {
        return (new Date(this.details[fieldName])).toLocaleString()
      } else if (fieldName === 'session_end' && this.details[fieldName] !== '') {
        return (new Date(this.details[fieldName])).toLocaleString()
      }
      return this.details[fieldName]
    },
    resumeSession(id) {
      this.session.resuming = id
      bdService.troubleshootingSessionResume(id).then(r => {
        window.open(r.data.url)
        this.session.resuming = null
      }).catch(() => {
        this.session.resuming = null
      })
    },
    bgIfNotLast(i) {
      return i !== this.filteredLogs.length - 1 ? 'background-image: url(' + this.assetServer + '/assets/img/bg-dotted.png)' : ''
    },
    handleMainScrollbar(_flag = true) {
      const _html = document.getElementsByTagName('html')[0]
      if (_flag) _html.classList.add('scrollbar-hidden')
      else _html.classList.remove('scrollbar-hidden')
    },
    isFormInput(key) {
      return String(key).toLowerCase() === 'forminput'
    },
    normalizeFormInputs(formInput) {
      let inputs = formInput

      // Some older/session responses may contain formInput as a JSON string.
      if (typeof inputs === 'string') {
        try {
          inputs = JSON.parse(inputs)
        } catch (e) {
          return []
        }
      }

      if (!Array.isArray(inputs)) return []

      return inputs.map((input, index) => {
        const label = input && input.label ? String(input.label) : `Input ${index + 1}`
        const rawValue = input ? input.value : null

        let value = '-'
        if (Array.isArray(rawValue)) {
          value = rawValue.filter(item => item !== null && item !== undefined && item !== '').join(', ') || '-'
        } else if (rawValue && typeof rawValue === 'object') {
          value = JSON.stringify(rawValue)
        } else if (rawValue !== null && rawValue !== undefined && rawValue !== '') {
          value = String(rawValue)
        }

        return { label, value }
      })
    },
    formatLogValue(value) {
      if (value === null || value === undefined || value === '') return '-'
      if (Array.isArray(value) || typeof value === 'object') return JSON.stringify(value)
      return String(value)
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../../scss/colors';
@import '../../../scss/grid';

.bg-dotted {
  background-repeat: repeat-y;
  background-position: 1.3rem 50%;

  @include media-breakpoint-up('md') {
    background-position: 4.5rem 0;
  }
}
</style>
