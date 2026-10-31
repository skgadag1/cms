<template>
  <section class="px-3">
    <slot name="header" />

    <slot name="top-content" />

    <overlay-wrapper :show="loading && !disabled">
      <div class="box position-relative">
        <div v-if="!loading && (disabled || !isCurrentMachineConnected || lastSeen === null)" class="mask disabled zindex-3 rounded" />
        <b-row class="m-0">
          <b-col col md="4" class="p-0 d-block d-md-flex align-items-md-center">
            <div class="pe-2">{{ translate('fromLabel', 'CommonDateTime') }}</div>
            <div class="pe-2">
              <date-picker v-model="inputStartDate"
                           class="location-history__datepicker"
                           :placeholder="translatedConfig('startDateLabel')"
                           :format="dateFormat"
                           :text-input="true"
                           :enable-time-picker="false"
                           :auto-apply="true"
                           :clearable="false"
                           :disabled-dates="startDateValidation"
                           @update:model-value="handleChangeDate"
              />
            </div>
          </b-col>

          <b-col col md="4" class="p-0 d-block d-md-flex align-items-md-center">
            <div class="pe-2">{{ translate('toLabel', 'CommonDateTime') }}</div>
            <div class="pe-2">
              <date-picker v-model="inputEndDate"
                           class="location-history__datepicker"
                           :placeholder="translatedConfig('endDateLabel')"
                           :format="dateFormat"
                           :text-input="true"
                           :enable-time-picker="false"
                           :auto-apply="true"
                           :clearable="false"
                           :disabled-dates="endDateValidation"
                           @update:model-value="handleChangeDate"
              />
            </div>
          </b-col>

          <b-col cols="auto" md="4" class="p-0 d-flex flex-column justify-content-end">
            <b-link class="font-condensed mb-1" @click.prevent="resetDate">{{ translate('clearFilters', 'CommonFunctional') }}</b-link>
          </b-col>
        </b-row>
      </div>

      <GMapLocationHistory ref="gmap"
                           :disabled="disabled || error.present || !isCurrentMachineConnected"
                           :asset-server="assetServer"
                           :api-key="config.googleMapsAPIKey"
                           :items="items.all"
                           :arrow-dir="sortingDir"
                           :item-now="lastPosition"
                           :highlighted="highlighted"
                           :local-labels="config"
                           @hover="highlightTile"
                           @clicked="i => clicked = i"
      />

      <div v-if="!disabled && !error.present && isCurrentMachineConnected">
        <BasicGridTable v-if="items.all.length" :disabled="disabled" grid-name="locationhistory">
          <template #header>
            <BasicGridTableHeader v-for="f of fields"
                                  :key="'item__' + f.key"
                                  :sortable="f.sortable"
                                  :name="f.key"
                                  :sorting-active="f.key === 'dateTime'"
                                  :sorting-dir="sortingDir"
                                  @click="f.sortable ? toggleSorting() : null"
            >
              {{ f.label }}
            </BasicGridTableHeader>
          </template>

          <template #rows>
            <div class="location-history__rows overflow-y-scroll bg-white p-2">
              <LocationHistoryTableItem v-for="(item, i) of items.all"
                                        ref="locationItems"
                                        :key="'locationhistory-table__item__' + i"
                                        :item="item"
                                        :highlighted="item.startTs === highlighted || item.startTs === clicked"
                                        @mouseover.native="highlightTile(i, true)"
                                        @mouseout.native="highlightTile(i, false)"
                                        @click.native="clickTile(i, item)"
              />
            </div>
          </template>
        </BasicGridTable>

        <div v-else>
          <div v-if="!loading" class="box">{{ translate('noResults', 'CommonErrors') }}</div>
          <ListEmptyWidget v-else class="mt-2" />
        </div>
      </div>

      <div v-else-if="disabled">
        <ListEmptyWidget class="mt-2" />
      </div>

      <div v-else-if="error.present || !isCurrentMachineConnected">
        <div v-if="!isCurrentMachineConnected" class="box bg-gray-400"><b-icon icon="lock-alt" class="small align-self-end text-gray-600 me-2" /> {{ translate('errorNotConnected', 'CommonErrors') }}</div>
        <div v-else-if="error.text" class="box">{{ translate(error.text.ck, error.text.cp) }}</div>
        <div v-else class="box">{{ translate('searchingError', 'CommonErrors') }} </div>
      </div>
    </overlay-wrapper>
  </section>
</template>

<script>
import { useLabels } from '@/composables/useLabels'
import {mapGetters} from 'vuex'
import ListEmptyWidget from '@/components/common/ListEmptyWidget'
import BasicGridTable from '@/components/common/BasicGridTable'
import BasicGridTableHeader from '@/components/common/BasicGridTableHeader'
import {bdService} from '@/services'
import GMapLocationHistory from './GMapLocationHistory'
import LocationHistoryTableItem from '@/components/common/LocationHistory/LocationHistoryTableItem'

export default {
  conceptName: ['CommonErrors', 'CommonFunctional', 'CommonDateTime'],
  setup() { return useLabels() },
  components: {
    LocationHistoryTableItem,
    ListEmptyWidget,
    BasicGridTable,
    BasicGridTableHeader,
    GMapLocationHistory
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
      loading: true,
      activeItem: {},
      startDate: null,
      inputStartDate: null,
      endDate: null,
      inputEndDate: null,
      lastSeen: null,
      lastPosition: {},
      weekBefore: null,
      sortingDir: '-',
      highlighted: null,
      clicked: null,
      dateFormat: 'yyyy-MM-dd',
      items: {
        all: [],
        step: 2000,
        offset: 0,
        finished: false
      },
      error: {
        present: false,
        text: null
      }
    }
  },
  computed: {
    fields() {
      return [
        {key: 'dateTime', label: this.translatedConfig('dateTimeLabel'), sortable: true},
        {key: 'location', label: this.translatedConfig('locationLabel'), sortable: false}
      ]
    },
    ...mapGetters([
      'getCurrentSerial',
      'getCurrentMachine',
      'getCurrentMachineError',
      'isCurrentMachineConnected'
    ])
  },
  watch: {
    getCurrentMachineError(v) {
      if (v) {
        this.loading = false
        this.clearResults()
        this.error.present = true
      }
    },
    getCurrentSerial(s) {
      this.clearResults()
      this.lastSeen = null
      this.lastPosition = {}
      if (s) this.initLoad()
    }
  },
  created() {
    this.initLoad()
  },
  methods: {
    resetDate() {
      // Fresh copies, so the pickers always see a new model value and the
      // weekBefore/lastSeen defaults can never be mutated through them
      if (this.weekBefore && this.lastSeen) {
        this.inputStartDate = this.startDate = new Date(this.weekBefore)
        this.inputEndDate = this.endDate = new Date(this.lastSeen)
      } else {
        this.inputStartDate = this.startDate = null
        this.inputEndDate = this.endDate = null
      }
      this.clearResults()
      this.fetchResults()
    },
    handleChangeDate() {
      // v-model is updated before this handler runs; re-apply the previous value when a
      // typed-in date could not be parsed
      this.inputStartDate = this.inputStartDate || this.startDate
      this.inputEndDate = this.inputEndDate || this.endDate
      const start = this.inputStartDate || this.weekBefore
      const end = this.inputEndDate || this.lastSeen || new Date()
      // Copies: never mutate the pickers' model values (or lastSeen) with setHours
      this.startDate = start ? new Date(start) : null
      this.endDate = new Date(end)
      if (this.startDate) this.startDate.setHours(0, 0, 0, 0)
      this.endDate.setHours(23, 59, 59, 999)
      this.clearResults()
      this.fetchResults()
    },
    initLoad() {
      if (!this.disabled && this.getCurrentSerial) {
        this.$store.dispatch('fetchMachine')
        this.$store.dispatch('fetchMachineAuth')
        this.$store.dispatch('fetchMachineConnection')
        this.loading = true
        bdService.fetchLastKnownLocation(this.getCurrentSerial).then(r => {
          if (r && r.startTs) {
            this.lastPosition = r
            this.lastSeen = new Date(r.startTs)
            this.weekBefore = new Date(r.startTs)
            this.weekBefore.setDate(this.weekBefore.getDate() - 6)
            this.inputStartDate = this.startDate = this.weekBefore
            this.inputEndDate = this.endDate = this.lastSeen
            this.fetchResults()
          }
        }).catch(e => {
          if (e && !e.isCancelled) {
            // eslint-disable-next-line no-console
            console.log('No last known location', e)
          }
        }).finally(r => { if (r && !r.isCancelled || !r) this.loading = false })
      }
    },
    clearResults() {
      this.items = {
        all: [],
        step: 2000,
        offset: 0,
        finished: false
      }
      this.error.present = false
      this.error.text = null
      this.$refs.gmap.closeInfoWindow()
    },
    highlightTile(i, v) {
      this.highlighted = v && i || null
    },
    clickTile(i, item) {
      this.clicked = i
      this.$refs.gmap.toggleInfoWindow(item)
    },
    validateFilters() {
      this.error.present = false
      this.error.text = null
      if (this.startDate > this.endDate) this.error.text = this.translatedConfig('startDateGreaterError')
      if (this.error.text) {
        this.error.present = true
        return false
      } else return true
    },
    async fetchResults() {
      if (this.validateFilters() && !this.disabled && this.getCurrentSerial && !this.items.finished) {
        this.loading = true
        await bdService.fetchLocationHistory(this.getCurrentSerial, {
          includeAddress: true,
          limit: this.items.step,
          offset: this.items.offset,
          start_date_time: this.startDate,
          end_date_time: this.endDate
        }).then(r => {
          this.items.all = this.items.all.concat(r.data)
          this.items.finished = r.data.length < this.items.step
          if (this.items.finished) {
            if (this.sortingDir === '-') this.items.all.reverse()
          } else {
            this.items.offset += this.items.step
            this.fetchResults()
          }
        }).catch(e => {
          if (!e.isCancelled) this.error.present = true
        }).finally(r => {
          if (r && !r.isCancelled || !r) {
            this.items.displayed = this.items.all.slice(0, this.items.offset)
            this.loading = false
          }
        })
      }
    },
    startDateValidation(d) {
      const today = new Date()
      const endDate = new Date(this.endDate)
      return this.endDate ? d > today || d > endDate : d > today
    },
    endDateValidation(d) {
      const today = new Date()
      const startDate = new Date(this.startDate)
      return this.startDate ? d > today || d < startDate : d > today
    },
    toggleSorting() {
      this.sortingDir = this.sortingDir === '-' ? '+' : '-'
      this.items.all.reverse()
    }
  }
}
</script>

<style lang="scss" scoped>
  // Keep the pre-migration look: plain date input with the calendar icon on the right
  .location-history__datepicker {
    width: 15rem;
    max-width: 100%;

    :deep(.dp__input) {
      border-radius: 0.125rem;
    }

    :deep(.dp__input_icon) {
      inset-inline-start: auto;
      inset-inline-end: 0.5rem;
    }

    :deep(.dp__input_icon_pad) {
      padding-inline-start: 0.75rem;
      padding-inline-end: var(--dp-input-icon-padding);
    }
  }

  .location-history__rows {
    max-height: 21.25rem;
  }
</style>
