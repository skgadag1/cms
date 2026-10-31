<template>
  <div class="box pb-0 pb-md-3 mb-4 position-relative zindex-1" :class="{ 'overflow-hidden' : loading || disabled }">
    <div v-if="loading || disabled" class="mask disabled zindex-3 rounded" />

    <b-row class="mb-3 mb-md-2" align-v="center">
      <b-col>
        <ErrorInput class="border rounded"
                    ajax
                    submit-on-close
                    :keep-button="!isMobile"
                    :initial-keyword="filterKeyword"
                    :disabled="disabled"
                    :empty-label="translate('searchButton', 'CommonFunctional')"
                    :search-label="translate('searchButton', 'CommonFunctional')"
                    :placeholder="translatedConfig('descriptionSearchLabel')"
                    @submit="_k => { $emit('search', _k) }"
        />
      </b-col>
      <b-col cols="auto" class="ps-md-0 d-none d-md-flex">
        <b-link class="font-condensed"
                :disabled="disabled"
                @click.prevent="$emit('clear')"
        >
          {{ translate('clearAll', 'CommonFunctional') }}
        </b-link>
      </b-col>
    </b-row>

    <b-row class="d-flex d-md-none pb-3 small">
      <b-col class="d-flex align-items-center justify-content-center cursor-pointer text-uppercase font-condensed">
        <b-button :disabled="disabled" class="w-100 font-condensed" :variant="isExpanded ? 'danger' : 'primary'" @click="isExpanded = !isExpanded">
          <b-icon icon="filter" class="float-start me-1" />{{ translate('selectFiltersLabel', 'CommonFunctional') }}
        </b-button>
      </b-col>
    </b-row>

    <transition name="fade" :duration="{ enter: 350, leave: 100 }">
      <div v-show="isExpanded">
        <BasicFilterCheckboxes v-if="filterStatus"
                               :disabled="disabled"
                               :title="filterStatus.title"
                               :options="filterStatus.options"
                               @updated="$emit('updated')"
        />

        <BasicFilterCheckboxes v-if="filterSeverity"
                               :disabled="disabled"
                               :title="filterSeverity.title"
                               :options="filterSeverity.options"
                               @updated="$emit('updated')"
        />

        <BasicFilterCode v-if="filterCode"
                         :disabled="disabled"
                         :title="filterCode.title"
                         :placeholder="filterCode.placeholder"
                         :options="filterCode.options"
                         @updated="$emit('updated')"
        />

        <BasicFilterDate v-if="filterDate"
                         :disabled="disabled"
                         :title="filterDate.title"
                         :options="filterDate.options"
                         @updated="$emit('updated')"
        />

        <BasicFilterRange v-if="filterRange"
                          :disabled="disabled"
                          :title="filterRange.title"
                          :min-label="filterRange.minLabel"
                          :max-label="filterRange.maxLabel"
                          :options="filterRange.options.values"
                          @updated="$emit('updated')"
        />
      </div>
    </transition>

    <div class="mt-2 filter-chips">
      <b-badge v-for="(v, i) in filtersToDisplay"
               :key="'filter-pill_'+i+'_'+v"
               v-b-tooltip="{placement: 'top', title: 'Remove ' + v.type.toLowerCase() + ': ' + v.label.toLowerCase() + ' filter', delay: 200}"
               variant="dark"
               pill
               class="fw-normal me-2 me-md-1 my-1 my-md-2 cursor-pointer"
               @click="handleRemove(v)"
      >
        {{ capitalize(v.label) }} <b-icon icon="times-circle" class="small ps-1 bg-transparent" />
      </b-badge>
    </div>

    <b-row v-if="isExpanded || getFaultCodesKeyword || filtersToDisplay.length" class="d-flex d-md-none">
      <b-col cols="12" class="text-end mt-1 mb-3">
        <b-link class="font-condensed" @click.prevent="$emit('clear')">{{ translate('clearFilters', 'CommonFunctional') }}</b-link>
      </b-col>
    </b-row>

    <slot v-if="isExpanded" />
  </div>
</template>

<script>
import { useFormatters } from '@/composables/useFormatters'
import { useLabels } from '@/composables/useLabels'
import BasicFilterCheckboxes from '@/components/common/BasicFilterCheckboxes'
import BasicFilterDate from '@/components/common/BasicFilterDate'
import ErrorInput from '@/components/common/ErrorInput'
import BasicFilterRange from '@/components/common/BasicFilterRange'
import BasicFilterCode from '@/components/common/BasicFilterCode'
import {mapGetters} from 'vuex'
import {INITIAL_FILTERS} from '@/components/common/FaultCodes/FaultCodesComponent'

export default {
  conceptName: ['CommonFunctional', 'CommonDateTime'],
  setup() { return { ...useLabels(), ...useFormatters() } },
  components: {
    ErrorInput,
    BasicFilterDate,
    BasicFilterCheckboxes,
    BasicFilterRange,
    BasicFilterCode
  },
  emits: ['search', 'clear', 'updated', 'remove'],
  props: {
    disabled: {
      type: Boolean,
      required: false,
      default: false
    },
    loading: {
      type: Boolean,
      required: false,
      default: false
    },
    filterStatus: {
      type: Object,
      required: false,
      default: null
    },
    filterSeverity: {
      type: Object,
      required: false,
      default: null
    },
    filterCode: {
      type: Object,
      required: false,
      default: null
    },
    filterKeyword: {
      type: String,
      required: false
    },
    filterDate: {
      type: Object,
      required: false
    },
    filterRange: {
      type: Object,
      required: false
    },
    config: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      isExpanded: true
    }
  },
  computed: {
    isMobile() {
      return typeof window.matchMedia === 'function' && window.matchMedia('(max-width: 767px)').matches
    },
    filtersToDisplay() {
      const _filters = []

      if (this.filterStatus && this.filterStatus.options.filter(x => x.value).length !== INITIAL_FILTERS.status.length) {
        for (const o of this.filterStatus.options.filter(x => x.value)) {
          o.type = 'status'
          _filters.push(o)
        }
      }

      if (this.filterSeverity && this.filterSeverity.options.filter(x => x.value).length !== INITIAL_FILTERS.severity.length) {
        for (const o of this.filterSeverity.options.filter(x => x.value)) {
          o.type = 'severity'
          _filters.push(o)
        }
      }

      if (this.filterCode && this.filterCode.options.length) {
        for (const b of this.filterCode.options) _filters.push({label: b.label, type: 'code'})
      }

      if (this.filterDate) {
        let _o = {label: '', value: this.filterDate.options.value, type: 'date'}
        switch(_o.value) {
        case 'custom':
          if (this.filterDate.options.custom[0] || this.filterDate.options.custom[1]) {
            _o.label += this.filterDate.options.custom[0] ? new Date(this.filterDate.options.custom[0]).toLocaleDateString() + ' - ' : this.translate('beforeLabel', 'CommonDateTime') + ' '
            _o.label += this.filterDate.options.custom[1] ? new Date(this.filterDate.options.custom[1]).toLocaleDateString() : this.translate('todayLabel', 'CommonDateTime')
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
        if (_o.label.length) _filters.push(_o)
      }

      if (this.filterRange && (this.filterRange.options.values.minValue.length || this.filterRange.options.values.maxValue.length)) {
        let _or = {type: 'engineHours'}
        if (this.filterRange.options.values.minValue.length) {
          if (this.filterRange.options.values.maxValue.length) {
            _or.label = this.filterRange.options.values.minValue + ' h - ' + this.filterRange.options.values.maxValue + ' h'
          } else {
            _or.label = '>= ' + this.filterRange.options.values.minValue + ' h'
          }
        } else if (this.filterRange.options.values.maxValue.length) {
          _or.label = '<= ' + this.filterRange.options.values.maxValue + ' h'
        }
        if (_or.label.length) _filters.push(_or)
      }

      return _filters
    },
    ...mapGetters([
      'getFaultCodesKeyword'
    ])
  },
  created() {
    this.isExpanded = !this.isMobile
  },
  methods: {
    handleRemove(i) {
      this.$emit('remove', i)
    }
  }
}
</script>

<style lang="scss">
@import '../../../scss/colors';
@import '../../../scss/grid';

.filter-chips .badge {
  font-size: 100%;

  @include media-breakpoint-up('md') {
    &:hover {
      background: $gray-600;
    }
  }
}
</style>
