<template>
  <b-col :md="fullWidth ? 12 : 6" sm="12" data-test-id="fault-codes-widget" class="px-0 px-sm-3">
    <overlay-wrapper v-if="!disabled"
                     class="p-3 rounded"
                     :class="[(error.present && !loading) || !isCurrentMachineValidSubscription || !isCurrentMachineConnected ? 'bg-gray-400' : 'bg-gray-800']"
                     :show="loading"
    >
      <b-row v-if="!error.present && isCurrentMachineValidSubscription && isCurrentMachineConnected">
        <b-col cols="12" class="mb-2">
          <h2 class="text-white d-flex align-items-center">
            {{ translate("title", 'FaultCodes') }}
            <div class="d-inline-flex flex-grow-1 ms-2 ms-lg-2 ms-md-0">
              <transition name="fade">
                <CounterRounded v-if="codes.critical.length"
                                :tooltip="codes.critical.length + ' ' + translate('codesCriticalLabel', 'FaultCodes') + ' ' + translate('codesOccured', 'FaultCodes')"
                >
                  {{ codes.critical.length }}
                </CounterRounded>
              </transition>
              <transition name="fade">
                <CounterRounded v-if="codes.warning.length"
                                variant="warning"
                                :tooltip="codes.warning.length + ' ' + translate('codesWarningLabel', 'FaultCodes') + ' ' + translate('codesOccured', 'FaultCodes')"
                >
                  {{ codes.warning.length }}
                </CounterRounded>
              </transition>
              <transition name="fade">
                <CounterRounded v-if="codes.info.length"
                                variant="secondary"
                                :tooltip="codes.info.length + ' ' + translate('codesInformationLabel', labels, 'FaultCodes') + ' ' + translate('codesOccured', 'FaultCodes')"
                >
                  {{ codes.info.length }}
                </CounterRounded>
              </transition>
            </div>

            <b-link class="align-self-end small text-white" :href="faultCodesPageUrl">
              <span class="small">{{ translate("viewAll", 'CommonFunctional') }} <b-icon :icon="['fal', 'plus']" class="small" /></span>
            </b-link>
          </h2>

          <b-row>
            <hr class="title-divider w-100 mt-2">
          </b-row>

          <h3 class="text-white d-flex">
            {{ translate("activeCodesTitle", 'FaultCodes') }}
          </h3>
        </b-col>

        <transition-group name="fade" class="w-100">
          <FaultCodeItem v-for="x in displayedCodes"
                         :key="'fault-code-item__'+x.faultCode"
                         :code="x"
                         :search-label="translate('searchLabel', 'FaultCodes')"
                         :url="errorInputUrl"
          />
        </transition-group>

        <b-col v-if="codes.displayed.length > 3"
               cols="12"
               class="text-white font-condensed text-uppercase mt-3 d-flex align-items-center justify-content-center cursor-pointer"
               @click.prevent="isExpanded = !isExpanded"
        >
          <template v-if="!isExpanded">
            <CounterRounded variant="warning"
                            class="me-2"
            >
              +{{ codes.displayed.length - 3 }}
            </CounterRounded>
            {{ translate("showMore", 'CommonFunctional') }}
            <b-icon icon="sort-down" class="ms-2 pb-2" />
          </template>
          <template v-else>
            {{ translate("showLess", 'CommonFunctional') }}
            <b-icon icon="sort-up" class="ms-2 mt-1 pt-1" />
          </template>
        </b-col>

        <b-col v-else-if="!codes.displayed.length && !error.present && !loading"
               cols="12"
        >
          <div class="px-4 mt-4 mb-5 mx-5 font-condensed text-center text-white">
            <b-icon icon="check" class="text-green h2" /><br>
            {{ translate("noCodes", 'FaultCodes') }}
          </div>
        </b-col>
      </b-row>

      <b-row v-else-if="!loading && (!isCurrentMachineConnected || !isCurrentMachineValidSubscription || error.present)">
        <b-col cols="12">
          <h2 class="d-flex justify-content-between mb-0">
            {{ translate("title", 'FaultCodes') }}
            <b-icon icon="lock-alt" class="small align-self-end text-gray-600" />
          </h2>
        </b-col>

        <template>
          <hr class="title-divider w-100 mt-3 border-gray-600">
          <div class="px-4 my-4 mx-4 font-condensed text-center tests-error">
            <template v-if="!isCurrentMachineConnected">{{ translate('errorNotConnected', 'CommonErrors') }}</template>
            <template v-else-if="!isCurrentMachineValidSubscription">{{ translate('notSubscribedError', 'CommonErrors') }}</template>
            <template v-else-if="error.text">{{ translate(error.text.ck, error.text.cp) }}</template>
            <template v-else>{{ translate('defaultError', 'CommonErrors') }}</template>
          </div>
        </template>
      </b-row>

      <b-row v-else>
        <b-col cols="12">
          <h2 class="text-gray">
            {{ translate("title", 'FaultCodes') }}
            <small>
              <span class="float-end small">{{ translate("viewAll", 'CommonFunctional') }} <b-icon :icon="['fal', 'plus']" class="small" /></span>
            </small>
          </h2>
          <ListEmptyWidget :range="3" />
        </b-col>
      </b-row>
    </overlay-wrapper>

    <div v-else>
      <h2 class="text-gray">
        {{ translate("title", 'FaultCodes') }}
        <small>
          <span class="float-end small">{{ translate("viewAll", 'CommonFunctional') }} <b-icon :icon="['fal', 'plus']" class="small" /></span>
        </small>
      </h2>
      <ListEmptyWidget :range="3" />
    </div>
  </b-col>
</template>

<script>
import { useLabels } from '@/composables/useLabels'
import ListEmptyWidget from '@/components/common/ListEmptyWidget'
import CounterRounded from '@/components/common/CounterRounded'
import FaultCodeItem from '@/components/common/FaultCodes/FaultCodeItem'
import {mapGetters} from 'vuex'
import {bdService} from '@/services'
import {INITIAL_FILTERS} from '@/components/common/FaultCodes/FaultCodesComponent'

export default {
  conceptName: ['FaultCodes', 'CommonErrors', 'CommonFunctional'],
  setup() { return useLabels() },
  components: {
    FaultCodeItem,
    CounterRounded,
    ListEmptyWidget
  },
  props: {
    disabled: {
      type: Boolean,
      required: false,
      default: false
    },
    faultCodesPageUrl: {
      type: String,
      required: false,
      default: null
    },
    errorInputUrl: {
      type: String,
      required: false,
      default: null
    },
    fullWidth: {
      type: Boolean,
      required: false,
      default: true
    },
    config: {
      type: Object,
      required: false
    }
  },
  data() {
    return {
      loading: true,
      error: {
        present: false,
        text: null
      },
      codes: {
        all: [],
        displayed: [],
        critical: [],
        warning: [],
        info: []
      },
      isExpanded: false
    }
  },
  computed: {
    displayedCodes() {
      return this.isExpanded ? this.codes.displayed : this.codes.displayed.slice(0, 3)
    },
    ...mapGetters([
      'getCurrentMachine',
      'getCurrentSerial',
      'isCurrentMachineValidSubscription',
      'isCurrentMachineConnected'
    ])
  },
  watch: {
    getCurrentSerial(n, o) {
      if (n && n !== o) {
        this.fetchActiveCodes()
        this.fetchIndicatorData()
      }
    },
    getCurrentMachine(n, o) {
      if (n && n.engineHours !== (o && o.engineHours)) {
        this.fetchIndicatorData()
      }
    }
  },
  created() {
    if (this.isCurrentMachineValidSubscription) {
      this.fetchActiveCodes()
      this.fetchIndicatorData()
    } else {
      this.loading = false
    }
  },
  methods: {
    fetchActiveCodes() {
      this.$store.dispatch('fetchMachineConnection')
      this.codes = {
        all: [],
        displayed: [],
        critical: [],
        warning: [],
        info: []
      }
      if (this.getCurrentSerial && !this.disabled) {
        this.loading = true
        this.error.present = false
        this.error.text = null
        const serial = '' + this.getCurrentSerial
        const params = {
          start_date_time: new Date(null),
          end_date_time: new Date(),
          fault_status: 'ACTIVE',
          fault_severity: 'CRITICAL,WARNING'
        }

        bdService.fetchFaultCodes(serial, 20, 0, params, '-engineHoursStart', this.config.paginationLimit || null).then(r => {
          this.codes.all = r.items
          this.codes.displayed = r.items.sort(this.compare)
        }).catch(e => {
          if (!e || !e.isCancelled) {
            this.error = {present: true, text: this.determineErrorText(e), code: e && e.code || null}
          }
        }).finally(r => { if (r && !r.isCancelled || !r) this.loading = false })
      }
    },
    fetchIndicatorData() {
      this.codes.critical = this.codes.warning = this.codes.info = []
      const engineHours = (this.getCurrentMachine || {}).engineHours || false

      if (this.getCurrentSerial && !this.disabled && engineHours) {
        const serial = '' + this.getCurrentSerial
        const params = {
          start_date_time: new Date(null),
          end_date_time: (() => { const d = new Date(); d.setSeconds(0); d.setMilliseconds(0); return d })(),
          engine_hours_from: Math.ceil(engineHours) - 1,
          engine_hours_to: Math.ceil(engineHours),
          fault_status: INITIAL_FILTERS.status.map(x => x.label.toUpperCase()).join(','),
          fault_sort_key: '-engineHoursStart'
        }

        bdService.fetchFaultCodes(serial, 1000, 0, params, '-engineHoursStart', null).then(r => {
          if (serial === this.getCurrentSerial) {
            const c = r.items
            this.codes.critical = c.filter(x => x.severity.toUpperCase() === 'CRITICAL')
            this.codes.warning = c.filter(x => x.severity.toUpperCase() === 'WARNING')
            this.codes.info = c.filter(x => x.severity.toUpperCase() === 'INFORMATION')
          }
        }).catch(() => {
          // nothing here, we ignore errors and not display indicators
          this.codes.critical = []
          this.codes.warning = []
          this.codes.info = []
        })
      }
    },
    compare(a, b) {
      const weights = {'CRITICAL': 1, 'WARNING': 0}
      if (weights[a.severity] < weights[b.severity]) return 1
      else if (weights[a.severity] > weights[b.severity]) return -1
      else return new Date(b.startTime) - new Date(a.startTime)
    },
    determineErrorText(e) {
      const message = e && typeof e.message === 'string' ? e.message : ''
      if (message.startsWith('Machine with key')) return {ck: 'errorNotConnected', cp: 'CommonErrors'}
      else if (e && e.code === 403) return {ck: 'unauthorizedError', cp: 'CommonErrors'}
      else return {ck: 'defaultError', cp: 'CommonErrors'}
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../../scss/_colors.scss';

.bg-gray-800 {
  min-height: 16rem;
}

.title-divider {
  border-top: 2px solid $black;
}
</style>
