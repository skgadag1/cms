<template>
  <b-col tag="section" cols="12" :class="{ 'text-gray disabled' : disabled }" class="fault-codes-component" vcloak>
    <slot name="header" />

    <slot name="top-content" />


    <overlay-wrapper :show="loading && !disabled">
      <FaultCodesFilters :disabled="disabled || !isCurrentMachineAuthorized || !isCurrentMachineValidSubscription"
                         :filter-keyword="typeof filtering.keyword === 'string' ? filtering.keyword : ''"
                         :filter-status="{ title: translatedConfig('statusLabel'), options: filtering.status}"
                         :filter-severity="{ title: translatedConfig('severityLabel'), options: filtering.severity}"
                         :filter-date="{ title: translatedConfig('filterStartDateLabel'), options: filtering.date}"
                         :filter-code="{ title: translatedConfig('faultCodeLabel'), placeholder: translatedConfig('codeSearchLabel'), options: filtering.code}"
                         :filter-range="{
                           title: translatedConfig('filterEngineHoursLabel'),
                           options: filtering.engineHours,
                           minLabel: translatedConfig('filterEngineHoursMinLabel'),
                           maxLabel: translatedConfig('filterEngineHoursMaxLabel')
                         }"
                         :config="config"
                         @clear="clearFilters"
                         @search="handleSearch"
                         @remove="handleRemoveFilterItem"
                         @updated="clearAndLoadCodes"
      >
        <b-row>
          <b-col cols="12" class="mb-3 d-md-none">
            <b-form-select v-model="sortingPair" class="select-center" :options="sortingOptions" @update:model-value="(v) => applySorting(v[1], v[0])" />
          </b-col>
        </b-row>
      </FaultCodesFilters>

      <b-row v-if="!disabled && !error.present && isCurrentMachineValidSubscription && isCurrentMachineConnected">
        <b-col cols="12" class="mb-3 d-flex align-items-center justify-content-end">
          <b-link class="font-condensed small" :disabled="exportLoading || !items.all.length" @click.prevent="exportAllCodes">
            <b-icon v-if="!exportLoading" icon="external-link-alt" class="me-1" />
            <b-spinner v-else variant="primary" small />
            {{ translate('exportCodesLabel') }}
          </b-link>
        </b-col>

        <b-col cols="12">
          <FaultCodesTable :disabled="disabled"
                           :codes="items.all"
                           :config="config"
                           :fields="fields"
                           :sorting-dir="sortingPair[0]"
                           :sorting-key="sortingPair[1]"
                           @sorting="applySorting"
          />

          <div v-if="!loading && !items.loaded" class="text-center mt-3">
            <b-button variant="primary" class="font-condensed" @click="loadCodes(false)">{{ translate('loadMore', 'CommonFunctional') }}</b-button>
          </div>
        </b-col>

        <b-col v-if="!loading && !items.all.length" cols="12">
          <div class="box">{{ translate('noResults') }}</div>
        </b-col>
      </b-row>

      <template v-else-if="disabled">
        <ListEmptyWidget />
      </template>

      <b-row v-else class="tests-error">
        <b-col cols="12">
          <div v-if="!isCurrentMachineConnected" class="box bg-gray-400"><b-icon icon="lock-alt" class="small align-self-end text-gray-600 me-2" /> {{ translate('errorNotConnected', 'CommonErrors') }}</div>
          <div v-else-if="!isCurrentMachineAuthorized" class="box bg-gray-400"><b-icon icon="lock-alt" class="small align-self-end text-gray-600 me-2" /> {{ translate('unauthorizedError', 'CommonErrors') }}</div>
          <div v-else-if="!isCurrentMachineValidSubscription" class="box bg-gray-400"><b-icon icon="lock-alt" class="small align-self-end text-gray-600 me-2" /> {{ translate('notSubscribedError', 'CommonErrors') }}</div>
          <div v-else-if="error.text" class="box">{{ error.text }}</div>
          <div v-else class="box">{{ translate('searchingError', 'CommonErrors') }}</div>
        </b-col>
      </b-row>
    </overlay-wrapper>
  </b-col>
</template>

<script>
import { useLabels } from '@/composables/useLabels'
import {mapGetters} from 'vuex'
import ListEmptyWidget from '@/components/common/ListEmptyWidget'
import FaultCodesTable from '@/components/common/FaultCodes/FaultCodesTable'
import FaultCodesFilters from '@/components/common/FaultCodes/FaultCodesFilters'
import {FAULTCODES_KEYWORD_SAVE} from '@/store/mutations'
import {bdService} from '@/services'
import qs from 'qs'

export const SEVERITY_TABLE = {
  'CRITICAL': 'danger',
  'WARNING': 'warning',
  'INFORMATION': 'secondary'
}

const FILTERS = ['keyword', 'status', 'severity', 'date', 'code']

const DATE_ALL_BEFORE = new Date(null)
const DATE_WEEK_BEFORE = (() => { const n = new Date(); n.setHours(0, 0, 0, 0); n.setDate(n.getDate() - 6); return n.toISOString() })()
const DATE_MONTH_BEFORE = (() => { const n = new Date(); n.setHours(0, 0, 0, 0); n.setDate(n.getDate() - 30); return n.toISOString() })()

export const INITIAL_FILTERS = {
  keyword: '',
  status: [{label: 'Active', value: false}, {label: 'Inactive', value: false}],
  severity: Object.keys(SEVERITY_TABLE).map(x => { return {label: x.toLowerCase(), value: false}}),
  code: [],
  engineHours: {minValueLabel: '', maxValueLabel: 'filterEngineHoursMaxLabel', values: {minValue: '', maxValue: ''}},
  date: {
    all: DATE_ALL_BEFORE,
    week: DATE_WEEK_BEFORE,
    month: DATE_MONTH_BEFORE,
    custom: [null, null],
    value: DATE_ALL_BEFORE
  }
}

export default {
  conceptName: ['CommonErrors', 'CommonFunctional'],
  setup() { return useLabels() },
  components: {
    FaultCodesTable,
    FaultCodesFilters,
    ListEmptyWidget
  },
  props: {
    disabled: {
      type: Boolean,
      required: false,
      default: false
    },
    config: {
      type: Object,
      required: false
    }
  },
  data() {
    return {
      items: {
        all: [],
        limit: 10,
        loaded: false
      },
      exportLoading: false,
      currentPage: 1,
      sortingPair: ['-', 'incidentStart'],
      loading: true,
      error: {
        present: false,
        text: null
      },
      severityTable: SEVERITY_TABLE,
      filtering: JSON.parse(JSON.stringify(INITIAL_FILTERS))
    }
  },
  computed: {
    sortingOptions() {
      return [
        {value: ['+', 'faultStatus'], text: this.translatedConfig('statusLabel') + ' ' + this.translate('ascendingLabel', 'CommonFunctional')},
        {value: ['-', 'faultStatus'], text: this.translatedConfig('statusLabel') + ' ' + this.translate('descendingLabel', 'CommonFunctional')},
        {value: ['+', 'faultSeverity'], text: this.translatedConfig('severityLabel') + ' ' + this.translate('ascendingLabel', 'CommonFunctional')},
        {value: ['-', 'faultSeverity'], text: this.translatedConfig('severityLabel') + ' ' + this.translate('descendingLabel', 'CommonFunctional')},
        {value: ['+', 'faultCode'], text: this.translatedConfig('faultCodeLabel') + ' ' + this.translate('ascendingLabel', 'CommonFunctional')},
        {value: ['-', 'faultCode'], text: this.translatedConfig('faultCodeLabel') + ' ' + this.translate('descendingLabel', 'CommonFunctional')},
        {value: ['+', 'incidentStart'], text: this.translatedConfig('startTimeLabel') + ' ' + this.translate('ascendingLabel', 'CommonFunctional')},
        {value: ['-', 'incidentStart'], text: this.translatedConfig('startTimeLabel') + ' ' + this.translate('descendingLabel', 'CommonFunctional')},
        {value: ['+', 'engineHoursStart'], text: this.translatedConfig('engineHoursLabel') + ' ' + this.translate('ascendingLabel', 'CommonFunctional')},
        {value: ['-', 'engineHoursStart'], text: this.translatedConfig('engineHoursLabel') + ' ' + this.translate('descendingLabel', 'CommonFunctional')}
      ]
    },
    fields() {
      return [
        {key: 'faultStatus', label: this.translatedConfig('statusLabel'), sortable: true},
        {key: 'faultSeverity', label: this.translatedConfig('severityLabel'), sortable: true},
        {key: 'faultCode', label: this.translatedConfig('faultCodeLabel'), sortable: true},
        {key: 'name', label: this.translatedConfig('descriptionLabel'), sortable: false},
        {key: 'incidentStart', label: this.translatedConfig('startTimeLabel'), sortable: true},
        {key: 'engineHoursStart', label: this.translatedConfig('engineHoursLabel'), thClass: 'engine', sortable: true}
      ]
    },
    sortingParam() {
      return this.sortingPair && this.sortingPair.length ? this.sortingPair.join('') : '-incidentStart' // KP-1034
    },
    ...mapGetters([
      'getCurrentSerial',
      'isCurrentMachineAuthorized',
      'isCurrentMachineConnected',
      'isCurrentMachineValidSubscription'
    ])
  },
  watch: {
    getFaultCodesKeyword(n, o) {
      if (n !== o) this.clearAndLoadCodes()
    },
    getCurrentSerial() {
      this.clearFilters(true) // will also load codes
    }
  },
  created() {
    this.$store.dispatch('fetchMachineDetails')
    this.filtering.date.value = DATE_WEEK_BEFORE
    if (location.href.indexOf('?') > 0) {
      let items = qs.parse(location.search.substr(1), {parseNumbers: true})
      if (Object.keys(items).some(r => FILTERS.includes(r))) this.loadFilters(items)
    }
    this.loadCodes()
  },
  methods: {
    validateFilters() {
      const $_f = this.filtering
      const endDate = $_f.date.custom[1] || new Date()
      this.error.present = false
      this.error.text = null
      if (($_f.engineHours.values.minValue && $_f.engineHours.values.minValue <= 0) || ($_f.engineHours.values.maxValue && $_f.engineHours.values.maxValue <= 0)) this.error.text = this.translatedConfig('engineHoursNegativeError')
      if ($_f.engineHours.values.maxValue && (parseFloat($_f.engineHours.values.maxValue) < parseFloat($_f.engineHours.values.minValue))) this.error.text = this.translatedConfig('engineHoursStartGreaterError')
      if ($_f.date.custom[0] > endDate) this.error.text = this.translatedConfig('startDateGreaterError')
      if (this.error.text) {
        this.loading = false
        this.error.present = true
        return false
      } else return true
    },
    loadCodes(reset = false) {
      this.$store.dispatch('fetchMachineAuth')
      this.$store.dispatch('fetchMachineConnection')
      if (this.validateFilters() && !this.disabled && this.getCurrentSerial) {
        this.saveFilters()
        this.loading = true
        if (reset) {
          this.items.all = []
          this.currentPage = 1
        }
        bdService.fetchFaultCodes(this.getCurrentSerial, this.items.limit, this.items.limit * (this.currentPage - 1), this.getParams(), this.sortingParam).then(r => {
          this.error.present = false
          const _new = r.items.map(x => {
            const st = new Date(x.startTime)
            x.startTimeLocalized = st.toLocaleString()
            x.status = x.status.toLowerCase()
            return x
          })
          this.items.all.push(..._new)
          this.items.loaded = this.items.all.length < this.items.limit
          if (_new.length) this.currentPage++ // there are more items, next time load another page
        }).catch(e => {
          this.items.all = []
          if (!e.isCancelled) {
            this.error.present = true
            this.error.text = e.response && e.response.data && e.response.data.error_message || ''
          }
        }).finally(r => { if (r && !r.isCancelled || !r) this.loading = false })
      }
    },
    applySorting(s, v = '-') {
      this.sortingPair = [v, s]
      this.clearAndLoadCodes()
    },
    saveFilters() {
      const paramsToSave = {
        engineHours: JSON.stringify(this.filtering.engineHours.values),
        limit: this.items.limit,
        sorting: this.sortingPair
      }
      FILTERS.forEach(name => paramsToSave[name] = JSON.stringify(this.filtering[name]))

      for (const [key, value] of Object.entries(paramsToSave)) {
        this.updateUrlParam(key, value)
      }
    },
    loadFilters(savedParams) {
      FILTERS.forEach(name => this.filtering[name] = (savedParams[name] && JSON.parse(savedParams[name])) || this.filtering[name])
      this.filtering.date.custom[0] = this.filtering.date.custom[0] ? new Date(this.filtering.date.custom[0]) : null
      this.filtering.date.custom[1] = this.filtering.date.custom[1] ? new Date(this.filtering.date.custom[1]) : null
      this.filtering.engineHours.values = (savedParams.engineHours && JSON.parse(savedParams.engineHours)) || this.filtering.engineHours.values
      this.items.limit = parseInt(savedParams.limit) || this.items.limit
      this.sortingPair = savedParams.sorting || this.sortingPair
    },
    getParams() {
      const $_f = this.filtering
      const $_params = {}
      const $_status = $_f.status.filter(x => x.value).length ? $_f.status.filter(x => x.value) : INITIAL_FILTERS.status
      const $_severity = $_f.severity.filter(x => x.value).length ? $_f.severity.filter(x => x.value) : INITIAL_FILTERS.severity
      const $_code = $_f.code.filter(x => x.value)
      if ($_f.keyword) $_params.fault_name = $_f.keyword
      $_params.fault_status = $_status.map(x => x.label.toUpperCase()).join(',')
      $_params.fault_severity = $_severity.map(x => x.label.toUpperCase()).join(',')
      if ($_code.length > 0) $_params.fault_code = $_code.map(x => x.label.toUpperCase())
      if ($_f.date.value === 'custom') {
        $_params.start_date_time = $_f.date.custom[0] || DATE_ALL_BEFORE
        $_params.end_date_time = $_f.date.custom[1] || new Date()
      } else {
        $_params.start_date_time = $_f.date.value
        $_params.end_date_time = new Date()
      }
      if (this.filtering.engineHours.values.minValue !== '') $_params.engine_hours_from = this.filtering.engineHours.values.minValue
      if (this.filtering.engineHours.values.maxValue !== '') $_params.engine_hours_to = this.filtering.engineHours.values.maxValue
      return $_params
    },
    clearAndLoadCodes() {
      this.currentPage = 1
      this.items.loaded = false
      this.loadCodes(true)
    },
    handleSearch(_k = this.filtering.keyword) {
      this.filtering.keyword = _k
      this.$store.commit(FAULTCODES_KEYWORD_SAVE, _k)
      this.clearAndLoadCodes()
    },
    clearSorting() {
      this.sortingPair = ['-', 'incidentStart']
    },
    clearFilters(onlyWeekBefore) {
      this.clearSorting()
      this.filtering = JSON.parse(JSON.stringify(INITIAL_FILTERS))
      if (onlyWeekBefore) this.filtering.date.value = DATE_WEEK_BEFORE
      this.$store.commit(FAULTCODES_KEYWORD_SAVE, '')
      this.items.loaded = false
      this.clearAndLoadCodes()
    },
    handleRemoveFilterItem(i) {
      switch(i.type) {
      case 'status':
        this.filtering.status.map(x => { if (x.label === i.label) x.value = false })
        break
      case 'severity':
        this.filtering.severity.map(x => { if (x.label === i.label) x.value = false })
        break
      case 'code':
        this.filtering.code = this.filtering.code.filter(x => x.label !== i.label)
        break
      case 'date':
        this.filtering.date = JSON.parse(JSON.stringify(INITIAL_FILTERS.date))
        break
      case 'engineHours':
        this.filtering.engineHours = JSON.parse(JSON.stringify(INITIAL_FILTERS.engineHours))
        break
      }
      this.clearAndLoadCodes()
    },
    exportAllCodes() {
      const link = document.createElement('a')
      let allCodes = []
      let csvContent = ''
      let data
      this.exportLoading = true

      bdService.fetchFaultCodes(this.getCurrentSerial, 10000, 0, this.getParams(), this.sortingParam).then(r => {
        if (r.items && r.items.length > 0) {
          allCodes = r.items.map(x => {
            return {
              'Code': x.faultCode || '',
              'Status': x.status || '',
              'Severity': x.severity || '',
              'Name': x.name || '',
              'Start time': x.startTime || '',
              'End time': x.endTime || '',
              'Engine hours start': x.engineHoursStart || '',
              'Engine hours end': x.engineHoursEnd || ''
            }
          })
          csvContent += [
            Object.keys(allCodes[0]).map(x => x === null ? '' : x).join(','),
            ...allCodes.map(item => Object.values(item).filter(x => x !== 'description').map(x => x === null ? '' : x.toString().replace(/\r?\n|\r/g)).join(','))
          ].join('\r\n').replace(/(^\[)|(\]$)|#/gm, '')

          const filename = 'FaultCodes_' + this.getCurrentSerial + '.csv'
          if (window.navigator.msSaveOrOpenBlob && window.Blob) {
            window.navigator.msSaveOrOpenBlob(new Blob([csvContent], {type: 'text/csv;charset=utf-8;'}), filename)
          } else {
            data = encodeURI('data:text/csv;charset=utf-8,' + csvContent)
            link.setAttribute('href', data)
            link.setAttribute('download', filename)
            link.click()
          }
        } // TODO do something when there are no codes to export
      }).finally(() => this.exportLoading = false)
    }
  }
}
</script>

<style lang="scss">
@import '../../../scss/colors';
@import '../../../scss/grid';
@import '../../../scss/z-index';

.engine {
  max-width: 3rem;
}

.fault-codes-component {
  z-index: $zindex-fixed + 3;

  &.disabled {
    z-index: auto;
  }
}
</style>
